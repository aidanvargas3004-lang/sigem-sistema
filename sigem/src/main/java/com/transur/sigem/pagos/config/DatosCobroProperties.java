package com.transur.sigem.pagos.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Cuentas y números donde el cliente puede pagar; se muestran en el portal (propiedades sigem.pagos.*). */
@Data
@Component
@ConfigurationProperties(prefix = "sigem.pagos")
public class DatosCobroProperties {

    private String titular = "TRANSUR S.A.C.";
    private String ruc = "";
    private String banco = "";
    private String cuenta = "";
    private String cci = "";
    private String yape = "";
    private String plin = "";
}
