package com.transur.sigem.pagos;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Módulo de pagos sobre MVC + Spring Security + PostgreSQL.
 * Requiere la base de datos: DB_URL=jdbc:postgresql://localhost:5432/sigem_db DB_PASSWORD=... mvn test
 */
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class PagosIntegracionTest {

    @Autowired MockMvc mvc;

    @Test
    @WithMockUser(username = "ventas", roles = "VENTAS")
    void ventasConsultaPagosYExportaElReporte() throws Exception {
        mvc.perform(get("/ventas/pagos").param("estado", "CONFIRMADO")).andExpect(status().isOk())
                .andExpect(model().attributeExists("pagos", "cobradoMes", "saldoPendiente", "porValidar"));
        mvc.perform(get("/ventas/pagos/nuevo")).andExpect(status().isOk()).andExpect(model().attributeExists("contratos", "metodos"));
        mvc.perform(get("/ventas/pagos/exportar/excel")).andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @WithMockUser(username = "ventas", roles = "VENTAS")
    void soloElAdministradorAnulaRecibos() throws Exception {
        mvc.perform(post("/ventas/pagos/1/anular").param("motivo", "prueba").with(csrf())).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ventas", roles = "VENTAS")
    void confirmarSinTokenCsrfSeRechaza() throws Exception {
        mvc.perform(post("/ventas/pagos/1/confirmar")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ventas", roles = "VENTAS")
    void unMontoInvalidoVuelveAlFormularioConErrores() throws Exception {
        mvc.perform(post("/ventas/pagos").with(csrf()).param("contratoId", "1").param("monto", "-10")
                        .param("metodo", "EFECTIVO").param("fechaPago", "2026-01-01"))
                .andExpect(status().isOk()).andExpect(view().name("ventas/pago-form"))
                .andExpect(model().attributeHasFieldErrors("pago", "monto"));
    }

    @Test
    @WithMockUser(username = "cliente", roles = "CLIENTE")
    void elClienteVeSusPagosPeroNoLosDeVentas() throws Exception {
        mvc.perform(get("/cliente/pagos")).andExpect(status().isOk())
                .andExpect(model().attributeExists("contratos", "pagos", "datosCobro", "totalSaldo"));
        mvc.perform(get("/ventas/pagos")).andExpect(status().isForbidden());
    }

    @Test
    void anonimoEsRedirigidoAlLogin() throws Exception {
        mvc.perform(get("/cliente/pagos")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login"));
    }
}
