package com.transur.sigem.cliente.controller;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.service.ClienteSesionService;
import com.transur.sigem.comercial.dto.SolicitudRequest;
import com.transur.sigem.comercial.entity.Cotizacion;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.repository.AlquilerRepository;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import com.transur.sigem.comercial.service.SolicitudService;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.pagos.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClientePortalController {

    private final SolicitudRepository solicitudRepository;
    private final CotizacionRepository cotizacionRepository;
    private final ContratoRepository contratoRepository;
    private final ServicioRepository servicioRepository;
    private final AlquilerRepository alquilerRepository;
    private final SolicitudService solicitudService;
    private final ClienteSesionService clienteSesionService;
    private final PagoService pagoService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        List<Solicitud> solicitudes = solicitudRepository.findByClienteId(cliente.getId());
        List<Cotizacion> cotizaciones = cotizacionRepository.findByClienteIdOrderByFechaEmisionDesc(cliente.getId());
        var contratos = contratoRepository.findByClienteIdOrderByFechaInicioDesc(cliente.getId());
        var servicios = servicioRepository.findByContratoClienteId(cliente.getId());
        var alquileres = alquilerRepository.findByContratoClienteId(cliente.getId());
        model.addAttribute("cliente", cliente);
        model.addAttribute("solicitudes", solicitudes);
        model.addAttribute("cotizaciones", cotizaciones);
        model.addAttribute("contratos", contratos);
        model.addAttribute("servicios", servicios);
        model.addAttribute("alquileres", alquileres);
        model.addAttribute("totalSolicitudes", solicitudes.size());
        model.addAttribute("totalCotizaciones", cotizaciones.size());
        model.addAttribute("contratosActivos", contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).count());
        model.addAttribute("serviciosActivos", servicios.stream().filter(s -> !List.of("FINALIZADO", "CANCELADO").contains(s.getEstado())).count());
        return "cliente/dashboard";
    }

    @GetMapping("/solicitudes")
    public String solicitudes(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        model.addAttribute("cliente", cliente);
        model.addAttribute("solicitudes", solicitudRepository.findByClienteId(cliente.getId()));
        return "cliente/solicitudes";
    }

    @GetMapping("/solicitudes/nueva")
    public String nuevaSolicitud(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        SolicitudRequest request = new SolicitudRequest();
        request.setClienteId(cliente.getId());
        model.addAttribute("cliente", cliente);
        model.addAttribute("solicitud", request);
        return "cliente/solicitud-form";
    }

    @PostMapping("/solicitudes")
    public String crearSolicitud(Authentication authentication,
                                 @ModelAttribute SolicitudRequest request,
                                 RedirectAttributes redirect) {
        try {
            Cliente cliente = clienteActual(authentication);
            request.setClienteId(cliente.getId());
            solicitudService.crear(request);
            redirect.addFlashAttribute("success", "Solicitud enviada correctamente. Ventas la revisará para preparar la cotización.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/solicitudes";
    }

    @GetMapping("/cotizaciones")
    public String cotizaciones(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        model.addAttribute("cliente", cliente);
        model.addAttribute("cotizaciones", cotizacionRepository.findByClienteIdOrderByFechaEmisionDesc(cliente.getId()));
        return "cliente/cotizaciones";
    }

    @PostMapping("/cotizaciones/{id}/respuesta")
    public String responderCotizacion(Authentication authentication,
                                      @PathVariable Long id,
                                      @RequestParam String respuesta,
                                      RedirectAttributes redirect) {
        try {
            Cliente cliente = clienteActual(authentication);
            Cotizacion cotizacion = cotizacionRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Cotización no encontrada"));
            if (!cotizacion.getCliente().getId().equals(cliente.getId())) {
                throw new IllegalArgumentException("No tienes acceso a esta cotización");
            }
            if (!"ENVIADA".equals(cotizacion.getEstado())) {
                throw new IllegalArgumentException("La cotización no está pendiente de respuesta");
            }
            if (cotizacion.getVigencia() != null && cotizacion.getVigencia().isBefore(java.time.LocalDate.now())) {
                cotizacion.setEstado("EXPIRADA");
                if (cotizacion.getSolicitud() != null) {
                    cotizacion.getSolicitud().setEstado("EXPIRADA");
                    solicitudRepository.save(cotizacion.getSolicitud());
                }
                cotizacionRepository.save(cotizacion);
                throw new IllegalArgumentException("La cotización ya venció y fue marcada como EXPIRADA");
            }
            if (!List.of("APROBAR", "RECHAZAR").contains(respuesta == null ? "" : respuesta.toUpperCase())) {
                throw new IllegalArgumentException("Respuesta de cotización no válida");
            }
            String estado = "APROBAR".equalsIgnoreCase(respuesta) ? "APROBADA" : "RECHAZADA";
            cotizacion.setEstado(estado);
            if (cotizacion.getSolicitud() != null) {
                cotizacion.getSolicitud().setEstado(estado);
                solicitudRepository.save(cotizacion.getSolicitud());
            }
            cotizacionRepository.save(cotizacion);
            redirect.addFlashAttribute("success", "La cotización fue " + ("APROBADA".equals(estado) ? "aprobada" : "rechazada") + ".");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cliente/cotizaciones";
    }

    @GetMapping("/contratos")
    public String contratos(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        model.addAttribute("cliente", cliente);
        var contratos = contratoRepository.findByClienteIdOrderByFechaInicioDesc(cliente.getId());
        model.addAttribute("contratos", contratos);
        model.addAttribute("resumenes", pagoService.resumenes(contratos));
        return "cliente/contratos";
    }

    @GetMapping("/servicios")
    public String servicios(Authentication authentication, Model model) {
        Cliente cliente = clienteActual(authentication);
        model.addAttribute("cliente", cliente);
        model.addAttribute("servicios", servicioRepository.findByContratoClienteId(cliente.getId()));
        model.addAttribute("alquileres", alquilerRepository.findByContratoClienteId(cliente.getId()));
        return "cliente/servicios";
    }

    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        model.addAttribute("cliente", clienteActual(authentication));
        return "cliente/perfil";
    }

    private Cliente clienteActual(Authentication authentication) {
        return clienteSesionService.clienteActual(authentication);
    }
}
