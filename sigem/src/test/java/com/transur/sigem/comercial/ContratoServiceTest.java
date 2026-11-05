package com.transur.sigem.comercial;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.comercial.dto.ContratoRequest;
import com.transur.sigem.comercial.dto.ContratoResponse;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.entity.Cotizacion;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import com.transur.sigem.comercial.service.ContratoService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Reglas del contrato: solo nace de una cotización aprobada por el cliente y no reabre contratos cerrados. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContratoServiceTest {

    @Mock ContratoRepository contratoRepository;
    @Mock CotizacionRepository cotizacionRepository;
    @Mock SolicitudRepository solicitudRepository;
    @InjectMocks ContratoService contratoService;

    private Cotizacion cotizacion;
    private Solicitud solicitud;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setRazonSocial("Constructora Andes SAC");

        solicitud = new Solicitud();
        solicitud.setId(1L);
        solicitud.setCliente(cliente);
        solicitud.setTipoSolicitud("ALQUILER");
        solicitud.setEstado("APROBADA");

        cotizacion = new Cotizacion();
        cotizacion.setId(3L);
        cotizacion.setNumero("COT-2026-001");
        cotizacion.setSolicitud(solicitud);
        cotizacion.setCliente(cliente);
        cotizacion.setEstado("APROBADA");
        cotizacion.setTotal(new BigDecimal("3540.00"));

        when(cotizacionRepository.findById(3L)).thenReturn(Optional.of(cotizacion));
        when(contratoRepository.existsByNumero(anyString())).thenReturn(false);
        when(contratoRepository.save(any(Contrato.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private ContratoRequest contratoDe(String tipo) {
        ContratoRequest request = new ContratoRequest();
        request.setCotizacionId(3L);
        request.setTipoContrato(tipo);
        request.setFechaInicio(LocalDate.now());
        request.setFechaFin(LocalDate.now().plusDays(30));
        return request;
    }

    @Test
    void creaElContratoVigenteConElMontoDeLaCotizacion() {
        ContratoResponse respuesta = contratoService.crear(contratoDe("alquiler"));

        assertThat(respuesta.getEstado()).isEqualTo("VIGENTE");
        assertThat(respuesta.getTipoContrato()).isEqualTo("ALQUILER");
        assertThat(respuesta.getMontoTotal()).isEqualByComparingTo("3540.00");
        assertThat(respuesta.getNumero()).startsWith("CTR-" + LocalDate.now().getYear());
    }

    @Test
    void noCreaContratoSiElClienteNoAproboLaCotizacion() {
        cotizacion.setEstado("ENVIADA");

        assertThatThrownBy(() -> contratoService.crear(contratoDe("ALQUILER")))
                .hasMessageContaining("APROBADAS por el cliente");
        verify(contratoRepository, never()).save(any());
    }

    @Test
    void elTipoDeContratoDebeCoincidirConLaSolicitud() {
        assertThatThrownBy(() -> contratoService.crear(contratoDe("VENTA")))
                .hasMessageContaining("debe coincidir");
    }

    @Test
    void laFechaFinNoPuedeSerAnteriorAlInicio() {
        ContratoRequest request = contratoDe("ALQUILER");
        request.setFechaFin(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> contratoService.crear(request))
                .hasMessageContaining("fecha fin");
    }

    @Test
    void finalizarElContratoCierraLaSolicitud() {
        Contrato contrato = new Contrato();
        contrato.setId(4L);
        contrato.setEstado("VIGENTE");
        contrato.setCotizacion(cotizacion);
        when(contratoRepository.findById(4L)).thenReturn(Optional.of(contrato));

        contratoService.cambiarEstado(4L, "FINALIZADO");

        assertThat(contrato.getEstado()).isEqualTo("FINALIZADO");
        assertThat(solicitud.getEstado()).isEqualTo("CERRADA");
    }

    @Test
    void unContratoCanceladoNoVuelveAEstarVigente() {
        Contrato contrato = new Contrato();
        contrato.setId(4L);
        contrato.setEstado("CANCELADO");
        when(contratoRepository.findById(4L)).thenReturn(Optional.of(contrato));

        assertThatThrownBy(() -> contratoService.cambiarEstado(4L, "VIGENTE"))
                .hasMessageContaining("no puede volver a un estado activo");
    }
}
