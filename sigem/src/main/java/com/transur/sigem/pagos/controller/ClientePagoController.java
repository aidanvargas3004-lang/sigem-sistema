package com.transur.sigem.pagos.controller;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.service.ClienteSesionService;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.pagos.config.DatosCobroProperties;
import com.transur.sigem.pagos.dto.PagoRequest;
import com.transur.sigem.pagos.dto.ResumenPago;
import com.transur.sigem.pagos.entity.Pago;
import com.transur.sigem.pagos.service.PagoService;
import com.transur.sigem.pagos.service.ReciboPagoPdf;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** "Mis pagos" del portal: saldo de cada contrato, datos para pagar, reporte de pagos remotos y recibos. */
@Controller
@RequestMapping("/cliente/pagos")
@RequiredArgsConstructor
public class ClientePagoController {

    private final ClienteSesionService clienteSesionService;
    private final ContratoRepository contratoRepository;
    private final PagoService pagoService;
    private final ReciboPagoPdf reciboPagoPdf;
    private final DatosCobroProperties datosCobro;

    @GetMapping
    public String misPagos(Authentication authentication, Model model) {
        if (!model.containsAttribute("pago")) {
            model.addAttribute("pago", new PagoRequest());
        }
        preparar(clienteSesionService.clienteActual(authentication), model);
        return "cliente/pagos";
    }

    @PostMapping
    public String reportar(Authentication authentication, @Valid @ModelAttribute("pago") PagoRequest request,
                           BindingResult result, Model model, RedirectAttributes redirect) {
        Cliente cliente = clienteSesionService.clienteActual(authentication);
        if (result.hasErrors()) {
            model.addAttribute("error", result.getAllErrors().get(0).getDefaultMessage());
            preparar(cliente, model);
            return "cliente/pagos";
        }
        try {
            pagoService.reportarPorCliente(cliente.getId(), request);
            redirect.addFlashAttribute("success", "Pago reportado. Ventas lo validará y recibirás una notificación con tu recibo.");
            return "redirect:/cliente/pagos";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            preparar(cliente, model);
            return "cliente/pagos";
        }
    }

    @GetMapping("/{id}/recibo")
    public ResponseEntity<byte[]> recibo(Authentication authentication, @PathVariable Long id) {
        Cliente cliente = clienteSesionService.clienteActual(authentication);
        Pago pago;
        try {
            pago = pagoService.obtenerReciboDeCliente(id, cliente.getId());
        } catch (RecursoNoEncontradoException e) {
            return ResponseEntity.notFound().build();
        }
        return PagoViewController.pdf(pago.getNumeroRecibo(), reciboPagoPdf.generar(pago, pagoService.resumen(pago.getContrato())));
    }

    private void preparar(Cliente cliente, Model model) {
        List<Contrato> contratos = contratoRepository.findByClienteIdOrderByFechaInicioDesc(cliente.getId());
        Map<Long, ResumenPago> resumenes = pagoService.resumenes(contratos);
        model.addAttribute("cliente", cliente);
        model.addAttribute("contratos", contratos);
        model.addAttribute("resumenes", resumenes);
        model.addAttribute("contratosConSaldo", pagoService.contratosConSaldo(contratos));
        model.addAttribute("pagos", pagoService.listarPorCliente(cliente.getId()));
        model.addAttribute("metodos", pagoService.metodosCliente());
        model.addAttribute("datosCobro", datosCobro);
        // Los contratos cancelados no suman a lo contratado ni al saldo
        List<ResumenPago> activos = contratos.stream().filter(c -> !"CANCELADO".equals(c.getEstado()))
                .map(c -> resumenes.get(c.getId())).toList();
        model.addAttribute("totalContratado", suma(activos, ResumenPago::total));
        model.addAttribute("totalPagado", suma(activos, ResumenPago::pagado));
        model.addAttribute("totalPorValidar", suma(activos, ResumenPago::porValidar));
        model.addAttribute("totalSaldo", suma(activos, ResumenPago::saldo));
    }

    private static BigDecimal suma(List<ResumenPago> r, java.util.function.Function<ResumenPago, BigDecimal> campo) {
        return r.stream().map(campo).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
