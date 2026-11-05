package com.transur.sigem.pagos.controller;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.reporte.ExportadorReporteFactory;
import com.transur.sigem.pagos.dto.FiltroPago;
import com.transur.sigem.pagos.dto.PagoRequest;
import com.transur.sigem.pagos.dto.ResumenPago;
import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;
import com.transur.sigem.pagos.entity.Pago;
import com.transur.sigem.pagos.service.PagoService;
import com.transur.sigem.pagos.service.ReciboPagoPdf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Cobranza de contratos para Ventas: registro, validación de pagos reportados, recibos y reporte. */
@Controller
@RequestMapping("/ventas/pagos")
@RequiredArgsConstructor
public class PagoViewController {

    private final PagoService pagoService;
    private final ContratoRepository contratoRepository;
    private final ReciboPagoPdf reciboPagoPdf;
    private final ExportadorReporteFactory exportadores;

    @GetMapping
    public String listar(@RequestParam(required = false) EstadoPago estado,
                         @RequestParam(required = false) MetodoPago metodo,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        FiltroPago filtro = new FiltroPago(estado, metodo, desde, hasta);
        List<Pago> pagos = pagoService.listar(filtro);
        List<Contrato> contratos = contratoRepository.findAllByOrderByFechaInicioDesc().stream()
                .filter(c -> !"CANCELADO".equals(c.getEstado())).toList();
        Map<Long, ResumenPago> resumenes = pagoService.resumenes(contratos);
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);

        model.addAttribute("pagos", pagos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("estados", EstadoPago.values());
        model.addAttribute("metodos", MetodoPago.values());
        model.addAttribute("cobradoMes", pagoService.listar(new FiltroPago(EstadoPago.CONFIRMADO, null, inicioMes, null))
                .stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("porValidar", pagoService.contarPorValidar());
        model.addAttribute("saldoPendiente", resumenes.values().stream().map(ResumenPago::saldo).reduce(BigDecimal.ZERO, BigDecimal::add));
        model.addAttribute("contratosPagados", resumenes.values().stream().filter(r -> "PAGADO".equals(r.estado())).count());
        model.addAttribute("totalContratos", contratos.size());
        model.addAttribute("totalListado", pagos.stream().filter(p -> p.getEstado() == EstadoPago.CONFIRMADO)
                .map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add));
        return "ventas/pagos";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(required = false) Long contratoId, Model model) {
        PagoRequest request = new PagoRequest();
        request.setContratoId(contratoId);
        prepararFormulario(model, request);
        return "ventas/pago-form";
    }

    @PostMapping
    public String registrar(@Valid @ModelAttribute("pago") PagoRequest request, BindingResult result,
                            Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            prepararFormulario(model, request);
            return "ventas/pago-form";
        }
        try {
            Pago pago = pagoService.registrar(request);
            redirect.addFlashAttribute("mensaje", "Pago registrado. Recibo " + pago.getNumeroRecibo() + ".");
            return "redirect:/ventas/contratos/" + request.getContratoId();
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            prepararFormulario(model, request);
            return "ventas/pago-form";
        }
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes redirect) {
        return accion(redirect, () -> "Pago confirmado. Recibo " + pagoService.confirmar(id).getNumeroRecibo() + ".");
    }

    @PostMapping("/{id}/rechazar")
    public String rechazar(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes redirect) {
        return accion(redirect, () -> {
            pagoService.rechazar(id, motivo);
            return "Pago rechazado. El cliente verá el motivo en su portal.";
        });
    }

    /** Revertir un cobro confirmado es una operación sensible: solo el administrador. */
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public String anular(@PathVariable Long id, @RequestParam(required = false) String motivo, RedirectAttributes redirect) {
        return accion(redirect, () -> "Recibo " + pagoService.anular(id, motivo).getNumeroRecibo() + " anulado.");
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> recibo(@PathVariable Long id) {
        Pago pago;
        try {
            pago = pagoService.obtener(id);
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
        if (pago.getNumeroRecibo() == null) return ResponseEntity.notFound().build();
        return pdf(pago.getNumeroRecibo(), reciboPagoPdf.generar(pago, pagoService.resumen(pago.getContrato())));
    }

    @GetMapping("/exportar/{formato}")
    public ResponseEntity<byte[]> exportar(@PathVariable String formato,
                                           @RequestParam(required = false) EstadoPago estado,
                                           @RequestParam(required = false) MetodoPago metodo,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return exportadores.descargar(formato, "pagos", pagoService.reporte(new FiltroPago(estado, metodo, desde, hasta)));
    }

    private void prepararFormulario(Model model, PagoRequest request) {
        List<Contrato> contratos = pagoService.contratosConSaldo(contratoRepository.findAllByOrderByFechaInicioDesc());
        model.addAttribute("pago", request);
        model.addAttribute("contratos", contratos);
        model.addAttribute("resumenes", pagoService.resumenes(contratos));
        model.addAttribute("metodos", pagoService.metodosVentas());
    }

    private String accion(RedirectAttributes redirect, java.util.function.Supplier<String> operacion) {
        try {
            redirect.addFlashAttribute("mensaje", operacion.get());
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/ventas/pagos";
    }

    static ResponseEntity<byte[]> pdf(String nombre, byte[] contenido) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(nombre + ".pdf").build().toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(contenido);
    }
}
