package com.transur.sigem.pagos.entity;

import java.util.Arrays;
import java.util.List;

/**
 * Medios de pago que acepta TRANSUR. SIGEM no procesa el cobro (no es una pasarela):
 * registra cómo pagó el cliente y el número de operación o voucher que lo respalda.
 */
public enum MetodoPago {

    EFECTIVO("Efectivo", false, false),
    TRANSFERENCIA("Transferencia bancaria", true, true),
    DEPOSITO("Depósito en cuenta", true, true),
    YAPE("Yape", true, true),
    PLIN("Plin", true, true),
    TARJETA("Tarjeta (POS)", true, false);

    private final String etiqueta;
    private final boolean requiereOperacion;
    private final boolean remoto;

    MetodoPago(String etiqueta, boolean requiereOperacion, boolean remoto) {
        this.etiqueta = etiqueta;
        this.requiereOperacion = requiereOperacion;
        this.remoto = remoto;
    }

    public String getEtiqueta() { return etiqueta; }

    /** Todos salvo el efectivo dejan un número de operación, voucher o código de aprobación. */
    public boolean isRequiereOperacion() { return requiereOperacion; }

    /** Medios que el cliente puede usar sin acudir a la oficina y, por tanto, reportar desde el portal. */
    public boolean isRemoto() { return remoto; }

    public static List<MetodoPago> remotos() {
        return Arrays.stream(values()).filter(MetodoPago::isRemoto).toList();
    }
}
