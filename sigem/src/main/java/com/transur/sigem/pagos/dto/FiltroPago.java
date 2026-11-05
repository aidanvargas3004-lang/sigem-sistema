package com.transur.sigem.pagos.dto;

import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;

import java.time.LocalDate;

/** Filtros opcionales del listado y del reporte de pagos. */
public record FiltroPago(EstadoPago estado, MetodoPago metodo, LocalDate desde, LocalDate hasta) {

    public static FiltroPago vacio() {
        return new FiltroPago(null, null, null, null);
    }
}
