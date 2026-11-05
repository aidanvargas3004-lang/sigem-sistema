package com.transur.sigem.pagos.dto;

import com.transur.sigem.pagos.entity.MetodoPago;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Datos de un pago, tanto el que registra Ventas como el que reporta el cliente. */
@Data
public class PagoRequest {

    @NotNull(message = "Seleccione el contrato")
    private Long contratoId;

    @NotNull(message = "Ingrese la fecha del pago")
    @PastOrPresent(message = "La fecha del pago no puede ser futura")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaPago = LocalDate.now();

    @NotNull(message = "Ingrese el monto")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El monto admite hasta 2 decimales")
    private BigDecimal monto;

    @NotNull(message = "Seleccione el medio de pago")
    private MetodoPago metodo;

    @Size(max = 40, message = "El número de operación admite hasta 40 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9-]*$", message = "El número de operación solo admite letras, números y guiones")
    private String numeroOperacion;

    @Size(max = 255, message = "La observación admite hasta 255 caracteres")
    private String observacion;
}
