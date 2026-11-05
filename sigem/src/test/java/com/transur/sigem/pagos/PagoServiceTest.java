package com.transur.sigem.pagos;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.pagos.dto.PagoRequest;
import com.transur.sigem.pagos.dto.ResumenPago;
import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;
import com.transur.sigem.pagos.entity.Pago;
import com.transur.sigem.pagos.repository.PagoRepository;
import com.transur.sigem.pagos.service.PagoService;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.service.NotificacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Reglas de cobranza: saldo, número de operación, aislamiento por cliente y ciclo de validación. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PagoServiceTest {

    @Mock PagoRepository pagoRepository;
    @Mock ContratoRepository contratoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock NotificacionService notificacionService;
    @InjectMocks PagoService pagoService;

    private Contrato contrato;
    private BigDecimal confirmado = BigDecimal.ZERO;
    private BigDecimal porValidar = BigDecimal.ZERO;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setRazonSocial("Constructora Andes SAC");
        Usuario usuarioCliente = new Usuario();
        usuarioCliente.setId(99L);
        cliente.setUsuario(usuarioCliente);

        contrato = new Contrato();
        contrato.setId(1L);
        contrato.setNumero("CTR-2026-001");
        contrato.setCliente(cliente);
        contrato.setMontoTotal(new BigDecimal("1000.00"));
        contrato.setEstado("VIGENTE");

        when(contratoRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            if (p.getId() == null) p.setId(7L);
            return p;
        });
        when(pagoRepository.sumarPorContrato(eq(1L), anyCollection())).thenAnswer(inv -> {
            Collection<EstadoPago> estados = inv.getArgument(1);
            BigDecimal suma = BigDecimal.ZERO;
            if (estados.contains(EstadoPago.CONFIRMADO)) suma = suma.add(confirmado);
            if (estados.contains(EstadoPago.POR_VALIDAR)) suma = suma.add(porValidar);
            return suma;
        });
    }

    private PagoRequest solicitud(String monto, MetodoPago metodo, String operacion) {
        PagoRequest r = new PagoRequest();
        r.setContratoId(1L);
        r.setFechaPago(LocalDate.now());
        r.setMonto(new BigDecimal(monto));
        r.setMetodo(metodo);
        r.setNumeroOperacion(operacion);
        return r;
    }

    @Test
    void pagoEnEfectivoDeVentasQuedaConfirmadoConReciboYAvisaAlCliente() {
        Pago p = pagoService.registrar(solicitud("400.00", MetodoPago.EFECTIVO, null));

        assertThat(p.getEstado()).isEqualTo(EstadoPago.CONFIRMADO);
        assertThat(p.getNumeroRecibo()).isEqualTo("REC-" + LocalDate.now().getYear() + "-00007");
        assertThat(p.getOrigen()).isEqualTo("VENTAS");
        verify(notificacionService).notificar(eq(contrato.getCliente().getUsuario()), eq("Pago registrado"), anyString(),
                eq("SUCCESS"), anyString(), eq("/cliente/pagos"));
    }

    @Test
    void noSeAceptaUnMontoMayorAlSaldo() {
        confirmado = new BigDecimal("700.00");
        porValidar = new BigDecimal("200.00");

        assertThatThrownBy(() -> pagoService.registrar(solicitud("100.01", MetodoPago.EFECTIVO, null)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("excede el saldo").hasMessageContaining("100.00");
        verify(pagoRepository, never()).save(any());
    }

    @Test
    void yapeTransferenciaYTarjetaExigenNumeroDeOperacion() {
        for (MetodoPago m : List.of(MetodoPago.YAPE, MetodoPago.TRANSFERENCIA, MetodoPago.TARJETA)) {
            assertThatThrownBy(() -> pagoService.registrar(solicitud("50.00", m, "  ")))
                    .isInstanceOf(NegocioException.class).hasMessageContaining("número de operación");
        }
    }

    @Test
    void noSeRepiteUnaOperacionYaRegistrada() {
        when(pagoRepository.existsByMetodoAndNumeroOperacionIgnoreCaseAndEstadoIn(eq(MetodoPago.YAPE), eq("12345"), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> pagoService.registrar(solicitud("50.00", MetodoPago.YAPE, " 12345 ")))
                .isInstanceOf(NegocioException.class).hasMessageContaining("Ya existe un pago");
    }

    @Test
    void unContratoCanceladoNoRecibePagos() {
        contrato.setEstado("CANCELADO");

        assertThatThrownBy(() -> pagoService.registrar(solicitud("50.00", MetodoPago.EFECTIVO, null)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("cancelado");
    }

    @Test
    void elClienteSoloReportaPagosDeSusContratos() {
        assertThatThrownBy(() -> pagoService.reportarPorCliente(55L, solicitud("50.00", MetodoPago.YAPE, "111")))
                .isInstanceOf(NegocioException.class).hasMessageContaining("no pertenece");
    }

    @Test
    void elClienteNoReportaEfectivoNiTarjeta() {
        assertThatThrownBy(() -> pagoService.reportarPorCliente(10L, solicitud("50.00", MetodoPago.EFECTIVO, null)))
                .isInstanceOf(NegocioException.class).hasMessageContaining("oficinas");
        assertThatThrownBy(() -> pagoService.reportarPorCliente(10L, solicitud("50.00", MetodoPago.TARJETA, "V1")))
                .isInstanceOf(NegocioException.class).hasMessageContaining("oficinas");
    }

    @Test
    void elPagoReportadoQuedaPorValidarSinReciboYAvisaAVentas() {
        Usuario vendedor = new Usuario();
        vendedor.setId(5L);
        when(usuarioRepository.findActivosPorRoles(List.of("VENTAS", "ADMIN"))).thenReturn(List.of(vendedor));

        Pago p = pagoService.reportarPorCliente(10L, solicitud("300.00", MetodoPago.PLIN, "PL-778"));

        assertThat(p.getEstado()).isEqualTo(EstadoPago.POR_VALIDAR);
        assertThat(p.getNumeroRecibo()).isNull();
        assertThat(p.getOrigen()).isEqualTo("CLIENTE");
        verify(notificacionService).notificar(eq(vendedor), eq("Pago por validar"), contains("PL-778"),
                eq("WARNING"), eq("PAGO-7-REPORTADO"), eq("/ventas/pagos?estado=POR_VALIDAR"));
    }

    @Test
    void confirmarAsignaReciboPeroNuncaSuperaElMontoDelContrato() {
        Pago reportado = pagoReportado("300.00");
        when(pagoRepository.findById(7L)).thenReturn(Optional.of(reportado));

        confirmado = new BigDecimal("800.00");
        assertThatThrownBy(() -> pagoService.confirmar(7L)).isInstanceOf(NegocioException.class).hasMessageContaining("excede");

        confirmado = new BigDecimal("700.00");
        Pago p = pagoService.confirmar(7L);
        assertThat(p.getEstado()).isEqualTo(EstadoPago.CONFIRMADO);
        assertThat(p.getNumeroRecibo()).startsWith("REC-");
    }

    @Test
    void rechazarYAnularExigenMotivoYRespetanElEstado() {
        Pago reportado = pagoReportado("100.00");
        when(pagoRepository.findById(7L)).thenReturn(Optional.of(reportado));

        assertThatThrownBy(() -> pagoService.rechazar(7L, " ")).hasMessageContaining("motivo del rechazo");
        assertThatThrownBy(() -> pagoService.anular(7L, "x")).hasMessageContaining("Solo se anulan pagos confirmados");

        Pago p = pagoService.rechazar(7L, "No figura en el banco");
        assertThat(p.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        assertThat(p.getMotivo()).isEqualTo("No figura en el banco");
        assertThatThrownBy(() -> pagoService.confirmar(7L)).hasMessageContaining("Solo se confirman pagos por validar");
    }

    @Test
    void elResumenCalculaSaldoEstadoYAvance() {
        ResumenPago pendiente = ResumenPago.de(new BigDecimal("1000"), BigDecimal.ZERO, BigDecimal.ZERO);
        ResumenPago parcial = ResumenPago.de(new BigDecimal("1000"), new BigDecimal("250"), new BigDecimal("100"));
        ResumenPago pagado = ResumenPago.de(new BigDecimal("1000"), new BigDecimal("1000"), BigDecimal.ZERO);

        assertThat(pendiente.estado()).isEqualTo("PENDIENTE");
        assertThat(parcial.estado()).isEqualTo("PARCIAL");
        assertThat(parcial.saldo()).isEqualByComparingTo("650");
        assertThat(parcial.porcentaje()).isEqualTo(25);
        assertThat(pagado.estado()).isEqualTo("PAGADO");
        assertThat(pagado.tieneSaldo()).isFalse();
    }

    private Pago pagoReportado(String monto) {
        Pago p = new Pago();
        p.setId(7L);
        p.setContrato(contrato);
        p.setMonto(new BigDecimal(monto));
        p.setMetodo(MetodoPago.YAPE);
        p.setNumeroOperacion("Y-1");
        p.setFechaPago(LocalDate.now());
        p.setEstado(EstadoPago.POR_VALIDAR);
        p.setOrigen("CLIENTE");
        return p;
    }
}
