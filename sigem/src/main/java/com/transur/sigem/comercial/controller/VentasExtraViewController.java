package com.transur.sigem.comercial.controller;

import com.transur.sigem.comercial.dto.ContratoRequest;
import com.transur.sigem.comercial.dto.AlquilerRequest;
import com.transur.sigem.comercial.dto.AlquilerDetalleDTO;
import com.transur.sigem.comercial.dto.VentaRequest;
import com.transur.sigem.comercial.dto.VentaDetalleDTO;
import com.transur.sigem.comercial.service.AlquilerService;
import com.transur.sigem.comercial.service.VentaService;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import com.transur.sigem.comercial.service.ContratoService;
import com.transur.sigem.comercial.service.ReporteExportService;
import com.transur.sigem.comercial.service.ReporteVentasService;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentasExtraViewController {
    private final ContratoService contratoService;
    private final ReporteVentasService reporteVentasService;
    private final ReporteExportService reporteExportService;
    private final AlquilerService alquilerService;
    private final VentaService ventaService;
    private final MaquinariaRepository maquinariaRepository;
    private final TarifaRepository tarifaRepository;
    private final PagoService pagoService;
    private final ContratoRepository contratoRepository;

    @GetMapping("/contratos")
    public String contratos(Model model) {
        var contratos = contratoService.listar();
        long vigentes = contratos.stream().filter(c -> "VIGENTE".equals(c.getEstado())).count();
        long porFinalizar = contratos.stream().filter(c -> c.getFechaFin() != null && !c.getFechaFin().isBefore(LocalDate.now()) && !c.getFechaFin().isAfter(LocalDate.now().plusDays(30)) && "VIGENTE".equals(c.getEstado())).count();
        var monto = contratos.stream().map(c -> c.getMontoTotal() == null ? java.math.BigDecimal.ZERO : c.getMontoTotal()).reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        model.addAttribute("contratos", contratos);
        model.addAttribute("vigentes", vigentes);
        model.addAttribute("porFinalizar", porFinalizar);
        model.addAttribute("montoContratado", monto);
        model.addAttribute("resumenesPago", pagoService.resumenes(contratoRepository.findAllByOrderByFechaInicioDesc()));
        return "ventas/contratos";
    }

    @GetMapping("/contratos/nuevo")
    public String nuevoContrato(@RequestParam(required = false) Long cotizacionId, Model model) {
        ContratoRequest request = new ContratoRequest();
        request.setFechaInicio(LocalDate.now());
        request.setCotizacionId(cotizacionId);
        model.addAttribute("contrato", request);
        model.addAttribute("cotizaciones", contratoService.cotizacionesAprobadasDisponibles());
        model.addAttribute("modo", "crear");
        return "ventas/contrato-form";
    }

    @PostMapping("/contratos")
    public String guardarContrato(@Valid @ModelAttribute("contrato") ContratoRequest request,
                                  BindingResult result,
                                  Model model,
                                  RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("cotizaciones", contratoService.cotizacionesAprobadasDisponibles());
            model.addAttribute("modo", "crear");
            return "ventas/contrato-form";
        }
        try {
            var creado = contratoService.crear(request);
            redirect.addFlashAttribute("mensaje", "Contrato " + creado.getNumero() + " creado correctamente.");
            return "redirect:/ventas/contratos/" + creado.getId();
        } catch (Exception e) {
            model.addAttribute("cotizaciones", contratoService.cotizacionesAprobadasDisponibles());
            model.addAttribute("modo", "crear");
            model.addAttribute("error", e.getMessage());
            return "ventas/contrato-form";
        }
    }

    @GetMapping("/contratos/{id}")
    public String detalleContrato(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        try {
            var detalle = contratoService.obtenerDetalle(id);
            model.addAttribute("contrato", detalle);
            model.addAttribute("resumenPago", pagoService.resumen(detalle.getId(), detalle.getMontoTotal()));
            model.addAttribute("pagos", pagoService.listarPorContrato(detalle.getId()));
            return "ventas/contrato-detalle";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/ventas/contratos";
        }
    }

    @PostMapping("/contratos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                RedirectAttributes redirect) {
        try {
            contratoService.cambiarEstado(id, estado);
            redirect.addFlashAttribute("mensaje", "Estado del contrato actualizado correctamente.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/contratos/" + id;
    }

    @GetMapping("/contratos/{id}/editar")
    public String editarContrato(@PathVariable Long id, Model model) {
        var detalle = contratoService.obtenerDetalle(id);
        ContratoRequest req = new ContratoRequest();
        req.setNumero(detalle.getNumero());
        req.setTipoContrato(detalle.getTipoContrato());
        req.setFechaInicio(detalle.getFechaInicio());
        req.setFechaFin(detalle.getFechaFin());
        req.setCotizacionId(detalle.getCotizacionId());
        model.addAttribute("contrato", req);
        model.addAttribute("contratoId", id);
        model.addAttribute("cotizacionNumero", detalle.getCotizacionNumero());
        return "ventas/contrato-editar";
    }

    @PostMapping("/contratos/{id}/editar")
    public String actualizarContrato(@PathVariable Long id,
                                     @Valid @ModelAttribute("contrato") ContratoRequest request,
                                     BindingResult result,
                                     Model model,
                                     RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("contratoId", id);
            model.addAttribute("cotizacionNumero", "Cotización vinculada");
            return "ventas/contrato-editar";
        }
        try {
            contratoService.actualizar(id, request);
            redirect.addFlashAttribute("mensaje", "Contrato actualizado correctamente.");
            return "redirect:/ventas/contratos/" + id;
        } catch (Exception e) {
            model.addAttribute("contratoId", id);
            model.addAttribute("cotizacionNumero", "Cotización vinculada");
            model.addAttribute("error", e.getMessage());
            return "ventas/contrato-editar";
        }
    }

    @GetMapping("/alquileres")
    public String alquileres(Model model) {
        model.addAttribute("alquileres", alquilerService.listar());
        return "ventas/alquileres";
    }

    @GetMapping("/alquileres/nuevo")
    public String nuevoAlquiler(Model model) {
        AlquilerRequest request = new AlquilerRequest();
        request.setDetalles(java.util.List.of(new AlquilerDetalleDTO()));
        model.addAttribute("alquiler", request);
        model.addAttribute("contratos", contratoService.listar().stream().filter(c -> "ALQUILER".equals(c.getTipoContrato()) && "VIGENTE".equals(c.getEstado())).toList());
        model.addAttribute("maquinarias", maquinariaRepository.findByEstadoOperativo("OPERATIVO"));
        model.addAttribute("tarifas", tarifaRepository.findAllWithMaquinaria());
        return "ventas/alquiler-form";
    }

    @PostMapping("/alquileres")
    public String guardarAlquiler(@ModelAttribute AlquilerRequest request, RedirectAttributes redirect) {
        try { alquilerService.crear(request); redirect.addFlashAttribute("mensaje", "Alquiler iniciado correctamente."); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ventas/alquileres";
    }

    @PostMapping("/alquileres/{id}/finalizar")
    public String finalizarAlquiler(@PathVariable Long id, RedirectAttributes redirect) {
        try { alquilerService.finalizar(id); redirect.addFlashAttribute("mensaje", "Alquiler finalizado y maquinaria liberada."); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ventas/alquileres";
    }

    @GetMapping("/ventas")
    public String ventas(Model model) {
        model.addAttribute("ventas", ventaService.listar());
        return "ventas/ventas";
    }

    @GetMapping("/ventas/nueva")
    public String nuevaVenta(Model model) {
        VentaRequest request = new VentaRequest();
        request.setDetalles(java.util.List.of(new VentaDetalleDTO()));
        model.addAttribute("venta", request);
        model.addAttribute("contratos", contratoService.listar().stream().filter(c -> "VENTA".equals(c.getTipoContrato()) && "VIGENTE".equals(c.getEstado())).toList());
        model.addAttribute("maquinarias", maquinariaRepository.findByEstadoOperativo("OPERATIVO"));
        return "ventas/venta-form";
    }

    @PostMapping("/ventas")
    public String guardarVenta(@ModelAttribute VentaRequest request, RedirectAttributes redirect) {
        try { ventaService.crear(request); redirect.addFlashAttribute("mensaje", "Venta registrada correctamente."); }
        catch (Exception e) { redirect.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/ventas/ventas";
    }

    @GetMapping("/reportes")
    public String reportes(@RequestParam(required = false) LocalDate desde,
                           @RequestParam(required = false) LocalDate hasta,
                           @RequestParam(required = false) String tipoContrato,
                           @RequestParam(required = false) String estado,
                           Model model) {
        try {
            var reporte = reporteVentasService.generar(desde, hasta, tipoContrato, estado);
            model.addAttribute("reporte", reporte);
            return "ventas/reportes";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            var reporte = reporteVentasService.generar(LocalDate.now().withDayOfMonth(1), LocalDate.now(), null, null);
            model.addAttribute("reporte", reporte);
            return "ventas/reportes";
        }
    }

    @GetMapping("/reportes/exportar/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) LocalDate desde,
                                                @RequestParam(required = false) LocalDate hasta,
                                                @RequestParam(required = false) String tipoContrato,
                                                @RequestParam(required = false) String estado) {
        var reporte = reporteVentasService.generar(desde, hasta, tipoContrato, estado);
        byte[] bytes = reporteExportService.excel(reporte);
        return archivo(bytes, "reporte-ventas.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    @GetMapping("/reportes/exportar/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(required = false) LocalDate desde,
                                              @RequestParam(required = false) LocalDate hasta,
                                              @RequestParam(required = false) String tipoContrato,
                                              @RequestParam(required = false) String estado) {
        var reporte = reporteVentasService.generar(desde, hasta, tipoContrato, estado);
        byte[] bytes = reporteExportService.pdf(reporte);
        return archivo(bytes, "reporte-ventas.pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> archivo(byte[] bytes, String nombre, String contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentDisposition(ContentDisposition.attachment().filename(nombre).build());
        headers.setContentLength(bytes.length);
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
