package com.transur.sigem.pagos.entity;

import com.transur.sigem.comercial.entity.Contrato;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Pago (total o a cuenta) de un contrato. Cada alta y cambio de estado queda en la bitácora de auditoría. */
@Entity
@Table(name = "pagos", indexes = {
        @Index(name = "idx_pagos_contrato", columnList = "contrato_id"),
        @Index(name = "idx_pagos_estado", columnList = "estado")
})
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrato_id", nullable = false)
    private Contrato contrato;

    /** Correlativo del recibo interno; se asigna al confirmar el pago (REC-2026-00001). */
    @Column(name = "numero_recibo", unique = true, length = 20)
    private String numeroRecibo;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDate fechaPago;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetodoPago metodo;

    /** Número de operación bancaria, código Yape/Plin o voucher del POS. */
    @Column(name = "numero_operacion", length = 40)
    private String numeroOperacion;

    @Column(length = 255)
    private String observacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    /** VENTAS (registrado en oficina) o CLIENTE (reportado desde el portal). */
    @Column(nullable = false, length = 10)
    private String origen;

    @Column(name = "registrado_por", nullable = false, length = 50)
    private String registradoPor;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "revisado_por", length = 50)
    private String revisadoPor;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    /** Motivo del rechazo o de la anulación. */
    @Column(length = 255)
    private String motivo;
}
