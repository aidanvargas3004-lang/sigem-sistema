package com.transur.sigem.config;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;
import com.transur.sigem.pagos.entity.Pago;
import com.transur.sigem.pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Pagos demo sobre los contratos existentes: pagados al 100 %, a cuenta, con un pago reportado
 * por el cliente pendiente de validar y sin pagos. Solo se carga si la tabla de pagos está vacía.
 */
@Component
@Order(4)
@RequiredArgsConstructor
public class DemoPagosSeeder implements CommandLineRunner {

    private final ContratoRepository contratoRepo;
    private final PagoRepository pagoRepo;

    @Override
    @Transactional
    public void run(String... args) {
        if (pagoRepo.count() > 0) return;
        List<Contrato> contratos = contratoRepo.findAllByOrderByFechaInicioDesc().stream()
                .filter(c -> !"CANCELADO".equals(c.getEstado()) && c.getMontoTotal() != null && c.getMontoTotal().signum() > 0)
                .toList();
        for (int i = 0; i < contratos.size(); i++) {
            Contrato c = contratos.get(i);
            BigDecimal total = c.getMontoTotal();
            switch (i % 4) {
                case 0 -> confirmado(c, total, MetodoPago.TRANSFERENCIA, 1);
                case 1 -> confirmado(c, porcentaje(total, 50), MetodoPago.YAPE, 1);
                case 2 -> {
                    confirmado(c, porcentaje(total, 30), MetodoPago.EFECTIVO, 1);
                    reportado(c, porcentaje(total, 20), MetodoPago.PLIN, 2);
                }
                default -> { /* contrato sin pagos: queda PENDIENTE */ }
            }
        }
    }

    private void confirmado(Contrato c, BigDecimal monto, MetodoPago metodo, int n) {
        Pago p = pago(c, monto, metodo, n, "VENTAS", "ventas");
        p.setEstado(EstadoPago.CONFIRMADO);
        p.setRevisadoPor("ventas");
        p.setFechaRevision(p.getFechaRegistro());
        pagoRepo.save(p);
        p.setNumeroRecibo(String.format("REC-%d-%05d", p.getFechaPago().getYear(), p.getId()));
        pagoRepo.save(p);
    }

    private void reportado(Contrato c, BigDecimal monto, MetodoPago metodo, int n) {
        String usuario = c.getCliente().getUsuario() != null ? c.getCliente().getUsuario().getUsername() : "cliente";
        Pago p = pago(c, monto, metodo, n, "CLIENTE", usuario);
        p.setEstado(EstadoPago.POR_VALIDAR);
        p.setObservacion("Pago reportado desde el portal");
        pagoRepo.save(p);
    }

    private Pago pago(Contrato c, BigDecimal monto, MetodoPago metodo, int n, String origen, String usuario) {
        LocalDate fecha = c.getFechaInicio().plusDays(2L * n);
        if (fecha.isAfter(LocalDate.now())) fecha = LocalDate.now();
        Pago p = new Pago();
        p.setContrato(c);
        p.setFechaPago(fecha);
        p.setMonto(monto);
        p.setMetodo(metodo);
        p.setNumeroOperacion(metodo.isRequiereOperacion() ? String.format("%08d", 10_000_000 + c.getId() * 10 + n) : null);
        p.setOrigen(origen);
        p.setRegistradoPor(usuario);
        p.setFechaRegistro(LocalDateTime.of(fecha, java.time.LocalTime.of(10, 30)));
        return p;
    }

    private static BigDecimal porcentaje(BigDecimal total, int pct) {
        return total.multiply(BigDecimal.valueOf(pct)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
