package com.transur.sigem.pagos.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Situación de pago de un contrato.
 *
 * @param pagado    suma de pagos confirmados
 * @param porValidar suma de pagos reportados por el cliente que Ventas aún no revisa
 * @param saldo     lo que falta pagar, descontando lo confirmado y lo reportado
 */
public record ResumenPago(BigDecimal total, BigDecimal pagado, BigDecimal porValidar, BigDecimal saldo) {

    public static ResumenPago de(BigDecimal total, BigDecimal pagado, BigDecimal porValidar) {
        BigDecimal t = total == null ? BigDecimal.ZERO : total;
        BigDecimal saldo = t.subtract(pagado).subtract(porValidar).max(BigDecimal.ZERO);
        return new ResumenPago(t, pagado, porValidar, saldo);
    }

    /** PENDIENTE (sin pagos), PARCIAL o PAGADO. */
    public String estado() {
        if (total.signum() > 0 && pagado.compareTo(total) >= 0) return "PAGADO";
        return pagado.signum() > 0 ? "PARCIAL" : "PENDIENTE";
    }

    public int porcentaje() {
        if (total.signum() <= 0) return 0;
        return pagado.multiply(BigDecimal.valueOf(100)).divide(total, 0, RoundingMode.DOWN).min(BigDecimal.valueOf(100)).intValue();
    }

    public boolean tieneSaldo() {
        return saldo.signum() > 0;
    }
}
