package com.transur.sigem;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas de integración de seguridad (MVC + Spring Security + PostgreSQL).
 * Requieren la base de datos: DB_URL=jdbc:postgresql://localhost:5432/getion_sigem DB_PASSWORD=... mvn test
 */
@SpringBootTest
@AutoConfigureMockMvc(print = MockMvcPrint.NONE)
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
class SeguridadIntegracionTest {

    @Autowired MockMvc mvc;
    @Autowired com.transur.sigem.security.repository.UsuarioRepository usuarioRepository;

    @Test
    void anonimoEsRedirigidoAlLogin() throws Exception {
        mvc.perform(get("/operaciones/servicios")).andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
        mvc.perform(get("/notificaciones")).andExpect(status().is3xxRedirection());
    }

    @Test
    void laPaginaPublicaEsAccesible() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
    }

    @Test
    void laApiSinTokenResponde401() throws Exception {
        mvc.perform(post("/api/gps").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "operaciones", roles = "OPERACIONES")
    void operacionesNoAccedeAAuditoria() throws Exception {
        mvc.perform(get("/seguridad/auditoria")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "operaciones", roles = "OPERACIONES")
    void operacionesVeSusReportesYMantenimiento() throws Exception {
        mvc.perform(get("/reportes").param("tipo", "flota")).andExpect(status().isOk())
                .andExpect(model().attributeExists("tabla", "kpi"));
        mvc.perform(get("/operaciones/mantenimiento/planes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "gerente", roles = "GERENCIA")
    void gerenciaExportaReportesOperativos() throws Exception {
        mvc.perform(get("/reportes/exportar/pdf").param("tipo", "servicios")
                        .param("desde", "2026-01-01").param("hasta", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/pdf"));
        mvc.perform(get("/operaciones/servicios")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminConsultaLaBitacora() throws Exception {
        mvc.perform(get("/seguridad/auditoria").param("accion", "CREAR"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("registros"));
    }

    @Test
    @WithMockUser(username = "operaciones", roles = "OPERACIONES")
    void unPostSinTokenCsrfEsRechazado() throws Exception {
        mvc.perform(post("/notificaciones/leer-todas")).andExpect(status().isForbidden());
        mvc.perform(post("/notificaciones/leer-todas").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notificaciones"));
    }

    @Test
    void laApiFuncionaConJwt() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operaciones\",\"password\":\"Operaciones123*\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JsonNode json = new ObjectMapper().readTree(body);
        mvc.perform(get("/api/gps/ultimas").header("Authorization", "Bearer " + json.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void laApiValidaElDtoDeIncidencia() throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"operaciones\",\"password\":\"Operaciones123*\"}"))
                .andReturn().getResponse().getContentAsString();
        String token = new ObjectMapper().readTree(body).get("token").asText();
        mvc.perform(post("/api/incidencias").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"tipo\":\"INVENTADO\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "operaciones", roles = "OPERACIONES")
    void unUsuarioNoLeeNotificacionesAjenasNiLaAuditoria() throws Exception {
        Long admin = usuarioRepository.findByUsername("admin").orElseThrow().getId();
        Long propio = usuarioRepository.findByUsername("operaciones").orElseThrow().getId();
        mvc.perform(get("/api/sistema/notificaciones/count/" + admin)).andExpect(status().isForbidden());
        mvc.perform(get("/api/sistema/notificaciones/count/" + propio)).andExpect(status().isOk());
        mvc.perform(get("/api/sistema/auditoria/ultimas")).andExpect(status().isForbidden());
    }
}
