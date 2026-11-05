package com.transur.sigem.pagos.entity;

import java.util.EnumSet;
import java.util.Set;

/**
 * Ciclo de vida de un pago.
 * <pre>
 * Cliente reporta ──► POR_VALIDAR ──► CONFIRMADO ──► ANULADO (solo ADMIN)
 *                          └──────► RECHAZADO
 * Ventas registra ─────────────────► CONFIRMADO
 * </pre>
 */
public enum EstadoPago {

    POR_VALIDAR("Por validar"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    ANULADO("Anulado");

    /** Estados que descuentan del saldo: lo confirmado y lo reportado pendiente de revisión. */
    public static final Set<EstadoPago> COMPROMETIDOS = EnumSet.of(POR_VALIDAR, CONFIRMADO);

    private final String etiqueta;

    EstadoPago(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() { return etiqueta; }
}
