package com.transur.sigem.comercial;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.comercial.dto.CotizacionDetalleDTO;
import com.transur.sigem.comercial.dto.CotizacionRequest;
import com.transur.sigem.comercial.dto.CotizacionResponse;
import com.transur.sigem.comercial.entity.Cotizacion;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import com.transur.sigem.comercial.service.CotizacionService;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Reglas de la cotización: estado de la solicitud, tarifas, IGV y cambios de estado permitidos a Ventas. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CotizacionServiceTest {

    @Mock CotizacionRepository cotizacionRepository;
    @Mock SolicitudRepository solicitudRepository;
    @Mock MaquinariaRepository maquinariaRepository;
    @Mock TarifaRepository tarifaRepository;
    @InjectMocks CotizacionService cotizacionService;

    private Solicitud solicitud;
    private Maquinaria maquinaria;
    private Tarifa tarifa;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setRazonSocial("Constructora Andes SAC");

        solicitud = new Solicitud();
        solicitud.setId(1L);
        solicitud.setCliente(cliente);
        solicitud.setTipoSolicitud("ALQUILER");
        solicitud.setEstado("PENDIENTE");

        maquinaria = new Maquinaria();
        maquinaria.setId(5L);
        maquinaria.setCodigoInterno("EXC-001");
        maquinaria.setMarca("CAT");
        maquinaria.setModelo("320");

        tarifa = new Tarifa();
        tarifa.setId(8L);
        tarifa.setMaquinaria(maquinaria);
        tarifa.setTipoTarifa("DIA");
        tarifa.setPrecio(new BigDecimal("1000.00"));

        when(solicitudRepository.findById(1L)).thenReturn(Optional.of(solicitud));
        when(maquinariaRepository.findById(5L)).thenReturn(Optional.of(maquinaria));
        when(tarifaRepository.findById(8L)).thenReturn(Optional.of(tarifa));
        when(cotizacionRepository.existsByNumero(anyString())).thenReturn(false);
        when(cotizacionRepository.save(any(Cotizacion.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private CotizacionRequest solicitudDe(int cantidad) {
        CotizacionDetalleDTO detalle = new CotizacionDetalleDTO();
        detalle.setMaquinariaId(5L);
        detalle.setTarifaId(8L);
        detalle.setCantidad(cantidad);
        CotizacionRequest request = new CotizacionRequest();
        request.setSolicitudId(1L);
        request.setVigencia(LocalDate.now().plusDays(15));
        request.setDetalles(List.of(detalle));
        return request;
    }

    @Test
    void crearCalculaSubtotalIgvYTotalConLaTarifa() {
        CotizacionResponse respuesta = cotizacionService.crear(solicitudDe(3));

        assertThat(respuesta.getTotal()).isEqualByComparingTo("3540.00"); // 3 x 1000 + 18 % de IGV
        assertThat(respuesta.getEstado()).isEqualTo("ENVIADA");
        assertThat(respuesta.getNumero()).startsWith("COT-" + LocalDate.now().getYear());
        assertThat(solicitud.getEstado()).isEqualTo("EN_COTIZACION");
    }

    @Test
    void noCotizaUnaSolicitudQueNoEstaPendiente() {
        solicitud.setEstado("APROBADA");

        assertThatThrownBy(() -> cotizacionService.crear(solicitudDe(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está disponible");
        verify(cotizacionRepository, never()).save(any());
    }

    @Test
    void noPermiteDosCotizacionesParaLaMismaSolicitud() {
        when(cotizacionRepository.existsBySolicitudId(1L)).thenReturn(true);

        assertThatThrownBy(() -> cotizacionService.crear(solicitudDe(1)))
                .hasMessageContaining("ya tiene una cotización");
    }

    @Test
    void rechazaUnaTarifaDeOtraMaquinaria() {
        Maquinaria otra = new Maquinaria();
        otra.setId(6L);
        tarifa.setMaquinaria(otra);

        assertThatThrownBy(() -> cotizacionService.crear(solicitudDe(1)))
                .hasMessageContaining("no corresponde a la maquinaria");
    }

    @Test
    void ventasNoPuedeAprobarUnaCotizacion() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(3L);
        cotizacion.setEstado("ENVIADA");
        when(cotizacionRepository.findById(3L)).thenReturn(Optional.of(cotizacion));

        assertThatThrownBy(() -> cotizacionService.cambiarEstado(3L, "APROBADA"))
                .hasMessageContaining("la aprobación corresponde al cliente");
        assertThat(cotizacion.getEstado()).isEqualTo("ENVIADA");
    }

    @Test
    void rechazarLaCotizacionTambienRechazaLaSolicitud() {
        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setId(3L);
        cotizacion.setEstado("ENVIADA");
        cotizacion.setSolicitud(solicitud);
        when(cotizacionRepository.findById(3L)).thenReturn(Optional.of(cotizacion));

        cotizacionService.cambiarEstado(3L, "rechazada");

        assertThat(cotizacion.getEstado()).isEqualTo("RECHAZADA");
        assertThat(solicitud.getEstado()).isEqualTo("RECHAZADA");
    }
}
