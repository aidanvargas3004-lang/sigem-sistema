package com.transur.sigem.pagos.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.pagos.config.DatosCobroProperties;
import com.transur.sigem.pagos.dto.ResumenPago;
import com.transur.sigem.pagos.entity.EstadoPago;
import com.transur.sigem.pagos.entity.Pago;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

/** Recibo interno de pago en PDF (no reemplaza al comprobante electrónico de SUNAT). */
@Component
@RequiredArgsConstructor
public class ReciboPagoPdf {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Color OSCURO = new Color(30, 41, 59);

    private final DatosCobroProperties empresa;

    public byte[] generar(Pago pago, ResumenPago resumen) {
        Contrato contrato = pago.getContrato();
        Cliente cliente = contrato.getCliente();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A5.rotate(), 30, 30, 26, 26);
        PdfWriter.getInstance(doc, out);
        doc.open();

        PdfPTable cabecera = new PdfPTable(new float[]{3, 2});
        cabecera.setWidthPercentage(100);
        PdfPCell izq = sinBorde();
        izq.addElement(new Paragraph(empresa.getTitular(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, OSCURO)));
        if (!empresa.getRuc().isBlank()) izq.addElement(new Paragraph("RUC " + empresa.getRuc(), fuente(9, Color.DARK_GRAY)));
        izq.addElement(new Paragraph("Sistema Integral de Gestión de Flotas y Maquinaria", fuente(8, Color.GRAY)));
        cabecera.addCell(izq);
        PdfPCell der = new PdfPCell();
        der.setBorderColor(OSCURO);
        der.setPadding(8);
        der.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph titulo = new Paragraph("RECIBO DE PAGO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, OSCURO));
        titulo.setAlignment(Element.ALIGN_CENTER);
        Paragraph numero = new Paragraph(pago.getNumeroRecibo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
        numero.setAlignment(Element.ALIGN_CENTER);
        der.addElement(titulo);
        der.addElement(numero);
        cabecera.addCell(der);
        doc.add(cabecera);

        if (pago.getEstado() == EstadoPago.ANULADO) {
            Paragraph anulado = new Paragraph("ANULADO: " + pago.getMotivo(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(185, 28, 28)));
            anulado.setSpacingBefore(6);
            doc.add(anulado);
        }

        PdfPTable datos = new PdfPTable(new float[]{1.3f, 3, 1.3f, 2});
        datos.setWidthPercentage(100);
        datos.setSpacingBefore(12);
        fila(datos, "Cliente", cliente.getRazonSocial(), "Documento", cliente.getDocumento());
        fila(datos, "Contrato", contrato.getNumero() + " (" + contrato.getTipoContrato() + ")", "Fecha de pago", pago.getFechaPago().format(FECHA));
        fila(datos, "Medio de pago", pago.getMetodo().getEtiqueta(), "N° operación", pago.getNumeroOperacion() == null ? "-" : pago.getNumeroOperacion());
        if (pago.getObservacion() != null) fila(datos, "Observación", pago.getObservacion(), "", "");
        doc.add(datos);

        PdfPTable importe = new PdfPTable(new float[]{3, 2});
        importe.setWidthPercentage(100);
        importe.setSpacingBefore(10);
        PdfPCell etiqueta = new PdfPCell(new Phrase("IMPORTE RECIBIDO", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE)));
        etiqueta.setBackgroundColor(OSCURO);
        etiqueta.setPadding(8);
        importe.addCell(etiqueta);
        PdfPCell valor = new PdfPCell(new Phrase("S/ " + PagoService.soles(pago.getMonto()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13)));
        valor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valor.setPadding(8);
        importe.addCell(valor);
        doc.add(importe);

        Paragraph saldo = new Paragraph("Monto del contrato: S/ " + PagoService.soles(resumen.total())
                + "   ·   Total pagado: S/ " + PagoService.soles(resumen.pagado())
                + "   ·   Saldo pendiente: S/ " + PagoService.soles(resumen.saldo()), fuente(9, Color.DARK_GRAY));
        saldo.setSpacingBefore(8);
        doc.add(saldo);

        Paragraph pie = new Paragraph("Registrado por " + pago.getRegistradoPor()
                + (pago.getRevisadoPor() != null ? " · Validado por " + pago.getRevisadoPor() : "")
                + ". Recibo interno de control; el comprobante de pago electrónico (boleta o factura) se emite por separado.",
                fuente(7.5f, Color.GRAY));
        pie.setSpacingBefore(14);
        doc.add(pie);

        doc.close();
        return out.toByteArray();
    }

    private static void fila(PdfPTable t, String e1, String v1, String e2, String v2) {
        t.addCell(celda(e1, true));
        t.addCell(celda(v1, false));
        t.addCell(celda(e2, true));
        t.addCell(celda(v2, false));
    }

    private static PdfPCell celda(String texto, boolean etiqueta) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto,
                etiqueta ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, Color.DARK_GRAY) : fuente(9, Color.BLACK)));
        c.setPadding(5);
        c.setBorderColor(new Color(226, 232, 240));
        if (etiqueta) c.setBackgroundColor(new Color(248, 250, 252));
        return c;
    }

    private static PdfPCell sinBorde() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static Font fuente(float tam, Color color) {
        return FontFactory.getFont(FontFactory.HELVETICA, tam, color);
    }
}
