package com.transur.sigem.pagos.service;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.reporte.TablaReporte;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.pagos.dto.FiltroPago;
import com.transur.sigem.pagos.dto.PagoRequest;
import com.transur.sigem.pagos.dto.ResumenPago;
import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.MetodoPago;
import com.transur.sigem.pagos.entity.Pago;
import com.transur.sigem.pagos.repository.PagoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Registro y validación de pagos de contratos (cobranza). SIGEM no cobra: registra el medio
 * (efectivo, transferencia, depósito, Yape, Plin o tarjeta en POS), el número de operación y
 * controla el saldo de cada contrato.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PagoService {

    private static final String SALDO_EXCEDIDO = "El monto excede el saldo pendiente del contrato (S/ %s)";

    private final PagoRepository pagoRepository;
    private final ContratoRepository contratoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    /** Pago recibido por Ventas (en oficina o verificado en el banco): queda CONFIRMADO con su recibo. */
    @Transactional
    public Pago registrar(PagoRequest request) {
        Contrato contrato = contrato(request.getContratoId());
        Pago pago = nuevo(contrato, request, "VENTAS");
        pago.setEstado(EstadoPago.CONFIRMADO);
        pago.setRevisadoPor(pago.getRegistradoPor());
        pago.setFechaRevision(LocalDateTime.now());
        pagoRepository.save(pago);
        asignarRecibo(pago);
        avisarCliente(pago, "Pago registrado", "Se registró tu pago de S/ " + soles(pago.getMonto())
                + " del contrato " + contrato.getNumero() + " (recibo " + pago.getNumeroRecibo() + ").", "SUCCESS");
        return pago;
    }

    /** Pago que el cliente hizo por un medio remoto y reporta desde el portal: queda POR_VALIDAR. */
    @Transactional
    public Pago reportarPorCliente(Long clienteId, PagoRequest request) {
        Contrato contrato = contrato(request.getContratoId());
        if (contrato.getCliente() == null || !contrato.getCliente().getId().equals(clienteId)) {
            throw new NegocioException("El contrato seleccionado no pertenece a tu cuenta");
        }
        if (request.getMetodo() != null && !request.getMetodo().isRemoto()) {
            throw new NegocioException(request.getMetodo().getEtiqueta()
                    + " se paga en nuestras oficinas; desde el portal reporta transferencias, depósitos, Yape o Plin");
        }
        Pago pago = pagoRepository.save(nuevo(contrato, request, "CLIENTE"));
        String mensaje = contrato.getCliente().getRazonSocial() + " reportó un pago de S/ " + soles(pago.getMonto())
                + " por " + pago.getMetodo().getEtiqueta() + " (operación " + pago.getNumeroOperacion()
                + ") del contrato " + contrato.getNumero() + ".";
        for (Usuario u : usuarioRepository.findActivosPorRoles(List.of("VENTAS", "ADMIN"))) {
            notificacionService.notificar(u, "Pago por validar", mensaje, "WARNING",
                    "PAGO-" + pago.getId() + "-REPORTADO", "/ventas/pagos?estado=POR_VALIDAR");
        }
        return pago;
    }

    /** Ventas comprobó el abono en el banco o en la app: el pago pasa a CONFIRMADO. */
    @Transactional
    public Pago confirmar(Long id) {
        Pago pago = pagoEnEstado(id, EstadoPago.POR_VALIDAR, "Solo se confirman pagos por validar");
        BigDecimal confirmado = pagoRepository.sumarPorContrato(pago.getContrato().getId(), EnumSet.of(EstadoPago.CONFIRMADO));
        BigDecimal disponible = pago.getContrato().getMontoTotal().subtract(confirmado);
        if (pago.getMonto().compareTo(disponible) > 0) {
            throw new NegocioException(String.format(SALDO_EXCEDIDO, soles(disponible.max(BigDecimal.ZERO))));
        }
        revisar(pago, EstadoPago.CONFIRMADO, null);
        asignarRecibo(pago);
        avisarCliente(pago, "Pago confirmado", "Confirmamos tu pago de S/ " + soles(pago.getMonto())
                + " del contrato " + pago.getContrato().getNumero() + " (recibo " + pago.getNumeroRecibo() + ").", "SUCCESS");
        return pago;
    }

    /** El abono reportado no se encontró o no coincide: se rechaza con un motivo visible para el cliente. */
    @Transactional
    public Pago rechazar(Long id, String motivo) {
        Pago pago = pagoEnEstado(id, EstadoPago.POR_VALIDAR, "Solo se rechazan pagos por validar");
        revisar(pago, EstadoPago.RECHAZADO, motivoObligatorio(motivo, "Indique el motivo del rechazo"));
        avisarCliente(pago, "Pago rechazado", "No pudimos validar tu pago de S/ " + soles(pago.getMonto())
                + " (operación " + pago.getNumeroOperacion() + "): " + pago.getMotivo(), "ERROR");
        return pago;
    }

    /** Revierte un pago confirmado por error (solo ADMIN, ver controlador). El recibo queda anulado, no se borra. */
    @Transactional
    public Pago anular(Long id, String motivo) {
        Pago pago = pagoEnEstado(id, EstadoPago.CONFIRMADO, "Solo se anulan pagos confirmados");
        revisar(pago, EstadoPago.ANULADO, motivoObligatorio(motivo, "Indique el motivo de la anulación"));
        avisarCliente(pago, "Pago anulado", "Se anuló el recibo " + pago.getNumeroRecibo() + " de S/ "
                + soles(pago.getMonto()) + ": " + pago.getMotivo(), "WARNING");
        return pago;
    }

    public ResumenPago resumen(Contrato contrato) {
        return resumen(contrato.getId(), contrato.getMontoTotal());
    }

    public ResumenPago resumen(Long contratoId, BigDecimal montoTotal) {
        return ResumenPago.de(montoTotal,
                pagoRepository.sumarPorContrato(contratoId, EnumSet.of(EstadoPago.CONFIRMADO)),
                pagoRepository.sumarPorContrato(contratoId, EnumSet.of(EstadoPago.POR_VALIDAR)));
    }

    /** Resumen de varios contratos con una sola consulta agregada. */
    public Map<Long, ResumenPago> resumenes(Collection<Contrato> contratos) {
        Map<Long, BigDecimal[]> sumas = new HashMap<>();
        for (Object[] fila : pagoRepository.totalesPorContratoYEstado()) {
            BigDecimal[] s = sumas.computeIfAbsent((Long) fila[0], k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if (fila[1] == EstadoPago.CONFIRMADO) s[0] = (BigDecimal) fila[2];
            if (fila[1] == EstadoPago.POR_VALIDAR) s[1] = (BigDecimal) fila[2];
        }
        Map<Long, ResumenPago> resultado = new LinkedHashMap<>();
        for (Contrato c : contratos) {
            BigDecimal[] s = sumas.getOrDefault(c.getId(), new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            resultado.put(c.getId(), ResumenPago.de(c.getMontoTotal(), s[0], s[1]));
        }
        return resultado;
    }

    /** Contratos a los que aún se les puede registrar un pago (no cancelados y con saldo). */
    public List<Contrato> contratosConSaldo(List<Contrato> contratos) {
        Map<Long, ResumenPago> r = resumenes(contratos);
        return contratos.stream()
                .filter(c -> !"CANCELADO".equals(c.getEstado()))
                .filter(c -> r.get(c.getId()).tieneSaldo())
                .toList();
    }

    public List<Pago> listar(FiltroPago f) {
        return pagoRepository.findAllByOrderByFechaPagoDescIdDesc().stream()
                .filter(p -> f.estado() == null || p.getEstado() == f.estado())
                .filter(p -> f.metodo() == null || p.getMetodo() == f.metodo())
                .filter(p -> f.desde() == null || !p.getFechaPago().isBefore(f.desde()))
                .filter(p -> f.hasta() == null || !p.getFechaPago().isAfter(f.hasta()))
                .toList();
    }

    public List<Pago> listarPorContrato(Long contratoId) {
        return pagoRepository.findByContratoIdOrderByFechaPagoDescIdDesc(contratoId);
    }

    public List<Pago> listarPorCliente(Long clienteId) {
        return pagoRepository.findByContratoClienteIdOrderByFechaPagoDescIdDesc(clienteId);
    }

    public long contarPorValidar() {
        return pagoRepository.countByEstado(EstadoPago.POR_VALIDAR);
    }

    public Pago obtener(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Pago", id));
    }

    /** Pago confirmado del propio cliente; para cualquier otro se responde "no encontrado" sin revelar que existe. */
    public Pago obtenerReciboDeCliente(Long id, Long clienteId) {
        Pago pago = obtener(id);
        if (!pago.getContrato().getCliente().getId().equals(clienteId) || pago.getNumeroRecibo() == null) {
            throw new RecursoNoEncontradoException("Pago", id);
        }
        return pago;
    }

    public TablaReporte reporte(FiltroPago f) {
        List<List<Object>> filas = listar(f).stream().map(p -> List.<Object>of(
                p.getNumeroRecibo() == null ? "-" : p.getNumeroRecibo(),
                p.getFechaPago().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                p.getContrato().getNumero(),
                p.getContrato().getCliente().getRazonSocial(),
                p.getMetodo().getEtiqueta(),
                p.getNumeroOperacion() == null ? "-" : p.getNumeroOperacion(),
                p.getEstado().getEtiqueta(),
                p.getMonto())).toList();
        String rango = (f.desde() == null ? "inicio" : f.desde().toString()) + " a " + (f.hasta() == null ? "hoy" : f.hasta().toString());
        return new TablaReporte("Reporte de pagos y cobranzas", "Periodo: " + rango,
                List.of("Recibo", "Fecha", "Contrato", "Cliente", "Medio", "N° operación", "Estado", "Monto (S/)"), filas);
    }

    // ----------------------------------------------------------------------------------------

    private Pago nuevo(Contrato contrato, PagoRequest r, String origen) {
        if ("CANCELADO".equals(contrato.getEstado())) {
            throw new NegocioException("No se registran pagos de un contrato cancelado");
        }
        if (r.getMonto() == null || r.getMonto().signum() <= 0) {
            throw new NegocioException("El monto debe ser mayor a 0");
        }
        if (r.getFechaPago() == null || r.getFechaPago().isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha del pago no puede ser futura");
        }
        if (r.getMetodo() == null) {
            throw new NegocioException("Seleccione el medio de pago");
        }
        String operacion = r.getNumeroOperacion() == null || r.getNumeroOperacion().isBlank() ? null : r.getNumeroOperacion().trim();
        if (r.getMetodo().isRequiereOperacion() && operacion == null) {
            throw new NegocioException("Para " + r.getMetodo().getEtiqueta() + " ingrese el número de operación o voucher");
        }
        if (operacion != null && pagoRepository.existsByMetodoAndNumeroOperacionIgnoreCaseAndEstadoIn(
                r.getMetodo(), operacion, EstadoPago.COMPROMETIDOS)) {
            throw new NegocioException("Ya existe un pago por " + r.getMetodo().getEtiqueta() + " con la operación " + operacion);
        }
        ResumenPago resumen = resumen(contrato);
        if (r.getMonto().compareTo(resumen.saldo()) > 0) {
            throw new NegocioException(String.format(SALDO_EXCEDIDO, soles(resumen.saldo())));
        }
        Pago p = new Pago();
        p.setContrato(contrato);
        p.setFechaPago(r.getFechaPago());
        p.setMonto(r.getMonto());
        p.setMetodo(r.getMetodo());
        p.setNumeroOperacion(operacion);
        p.setObservacion(r.getObservacion() == null || r.getObservacion().isBlank() ? null : r.getObservacion().trim());
        p.setEstado(EstadoPago.POR_VALIDAR);
        p.setOrigen(origen);
        p.setRegistradoPor(UsuarioActual.username().orElse("sistema"));
        p.setFechaRegistro(LocalDateTime.now());
        return p;
    }

    private Contrato contrato(Long id) {
        if (id == null) throw new NegocioException("Seleccione el contrato");
        return contratoRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Contrato", id));
    }

    private Pago pagoEnEstado(Long id, EstadoPago esperado, String error) {
        Pago pago = obtener(id);
        if (pago.getEstado() != esperado) throw new NegocioException(error + " (estado actual: " + pago.getEstado().getEtiqueta() + ")");
        return pago;
    }

    private void revisar(Pago pago, EstadoPago estado, String motivo) {
        pago.setEstado(estado);
        pago.setMotivo(motivo);
        pago.setRevisadoPor(UsuarioActual.username().orElse("sistema"));
        pago.setFechaRevision(LocalDateTime.now());
        pagoRepository.save(pago);
    }

    private void asignarRecibo(Pago pago) {
        if (pago.getNumeroRecibo() == null) {
            pago.setNumeroRecibo(String.format("REC-%d-%05d", pago.getFechaPago().getYear(), pago.getId()));
            pagoRepository.save(pago);
        }
    }

    private static String motivoObligatorio(String motivo, String mensajeSiFalta) {
        if (motivo == null || motivo.isBlank()) throw new NegocioException(mensajeSiFalta);
        String m = motivo.trim();
        return m.length() > 255 ? m.substring(0, 255) : m;
    }

    private void avisarCliente(Pago pago, String titulo, String mensaje, String tipo) {
        Cliente cliente = pago.getContrato().getCliente();
        Optional<Usuario> usuario = Optional.ofNullable(cliente.getUsuario())
                .or(() -> usuarioRepository.findByClienteId(cliente.getId()));
        usuario.ifPresent(u -> notificacionService.notificar(u, titulo, mensaje, tipo,
                "PAGO-" + pago.getId() + "-" + pago.getEstado(), "/cliente/pagos"));
    }

    static String soles(BigDecimal monto) {
        return String.format(Locale.US, "%,.2f", monto);
    }

    /** Medios ofrecidos en cada formulario. */
    public List<MetodoPago> metodosVentas() {
        return List.of(MetodoPago.values());
    }

    public List<MetodoPago> metodosCliente() {
        return MetodoPago.remotos();
    }
}
