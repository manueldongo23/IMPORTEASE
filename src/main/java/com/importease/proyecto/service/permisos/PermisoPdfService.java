package com.importease.proyecto.service.permisos;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.importease.proyecto.model.DocumentoPermiso;
import com.importease.proyecto.model.EntidadControl;
import com.importease.proyecto.model.SolicitudPermiso;
import com.importease.proyecto.model.SolicitudPermisoDato;
import com.importease.proyecto.repository.PermisoDAO;
import com.importease.proyecto.service.LoggerUtil;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermisoPdfService {
    private final PermisoDAO permisoDAO;

    public PermisoPdfService() {
        this(new PermisoDAO());
    }

    public PermisoPdfService(PermisoDAO permisoDAO) {
        this.permisoDAO = permisoDAO;
    }

    public byte[] generarExpedientePDF(int solicitudId) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            SolicitudPermiso sol = permisoDAO.obtenerSolicitud(solicitudId);
            if (sol == null) {
                LoggerUtil.warn("Solicitud no encontrada para generar PDF: " + solicitudId);
                return out.toByteArray();
            }

            Map<String, String> datosMap = construirDatosMap(sol.getDatos());
            EntidadControl entidad = permisoDAO.obtenerEntidad(sol.getCodigoEntidad());
            List<DocumentoPermiso> documentos = permisoDAO.obtenerDocumentosPorPermiso(sol.getCodigoEntidad(), sol.getTipoPermiso());

            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 11, Font.ITALIC);
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font labelFont = new Font(Font.HELVETICA, 8, Font.NORMAL);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL);

            agregarCabecera(document, titleFont, subtitleFont);
            agregarImportador(document, datosMap, sectionFont, labelFont, valueFont);
            agregarProducto(document, datosMap, sectionFont, labelFont, valueFont);
            agregarLogistica(document, datosMap, sectionFont, labelFont, valueFont);
            agregarEntidad(document, sol, entidad, sectionFont, labelFont, valueFont);
            agregarChecklist(document, documentos, sectionFont, labelFont, valueFont);
            agregarFirma(document, sol);
            Paragraph footer = new Paragraph("Documento generado por ImportEase | Fecha: " + LocalDate.now(), footerFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(new Paragraph("\n"));
            document.add(footer);
            document.close();
        } catch (Exception e) {
            LoggerUtil.error("Error al generar PDF de expediente de permiso", e);
        }
        return out.toByteArray();
    }

    private Map<String, String> construirDatosMap(List<SolicitudPermisoDato> datos) {
        Map<String, String> datosMap = new HashMap<>();
        if (datos != null) {
            for (SolicitudPermisoDato d : datos) {
                datosMap.put(d.getCampo(), d.getValor() != null ? d.getValor() : "N/A");
            }
        }
        return datosMap;
    }

    private void agregarCabecera(Document document, Font titleFont, Font subtitleFont) throws Exception {
        Paragraph title = new Paragraph("SOLICITUD DE PERMISO PREVIO PARA IMPORTACION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        Paragraph subtitle = new Paragraph("Sistema ImportEase - Expediente Digital Pre-VUCE", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);
        document.add(new Paragraph(" "));
    }

    private void agregarImportador(Document document, Map<String, String> datosMap, Font sectionFont, Font labelFont, Font valueFont) throws Exception {
        document.add(new Paragraph("1. DATOS DEL IMPORTADOR", sectionFont));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createCell("RUC", datosMap.getOrDefault("ruc_importador", "N/A"), labelFont, valueFont));
        table.addCell(createCell("RAZON SOCIAL", datosMap.getOrDefault("razon_social", "N/A"), labelFont, valueFont));
        table.addCell(createCell("EMAIL DE CONTACTO", datosMap.getOrDefault("email_contacto", "N/A"), labelFont, valueFont));
        table.addCell(createCell("FECHA DE SOLICITUD", datosMap.getOrDefault("fecha_solicitud", "N/A"), labelFont, valueFont));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void agregarProducto(Document document, Map<String, String> datosMap, Font sectionFont, Font labelFont, Font valueFont) throws Exception {
        document.add(new Paragraph("2. DATOS DEL PRODUCTO", sectionFont));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createCell("DESCRIPCION", datosMap.getOrDefault("producto_descripcion", "N/A"), labelFont, valueFont));
        table.addCell(createCell("CODIGO HS", datosMap.getOrDefault("codigo_hs", "N/A"), labelFont, valueFont));
        table.addCell(createCell("PAIS DE ORIGEN", datosMap.getOrDefault("pais_origen", "N/A"), labelFont, valueFont));
        table.addCell(new PdfPCell(new Paragraph(" ")));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void agregarLogistica(Document document, Map<String, String> datosMap, Font sectionFont, Font labelFont, Font valueFont) throws Exception {
        document.add(new Paragraph("3. INFORMACION LOGISTICA", sectionFont));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.addCell(createCell("INCOTERM", datosMap.getOrDefault("incoterm", "N/A"), labelFont, valueFont));
        table.addCell(createCell("VALOR FOB (USD)", datosMap.getOrDefault("valor_fob_usd", "N/A"), labelFont, valueFont));
        table.addCell(createCell("VALOR FLETE (USD)", datosMap.getOrDefault("valor_flete_usd", "N/A"), labelFont, valueFont));
        table.addCell(createCell("VALOR SEGURO (USD)", datosMap.getOrDefault("valor_seguro_usd", "N/A"), labelFont, valueFont));
        table.addCell(createCell("VALOR CIF (USD)", datosMap.getOrDefault("valor_cif_usd", "N/A"), labelFont, valueFont));
        table.addCell(new PdfPCell(new Paragraph(" ")));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void agregarEntidad(Document document, SolicitudPermiso sol, EntidadControl entidad, Font sectionFont, Font labelFont, Font valueFont) throws Exception {
        document.add(new Paragraph("4. ENTIDAD COMPETENTE", sectionFont));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        String nombreEntidad = entidad != null ? entidad.getNombreEntidad() : sol.getCodigoEntidad();
        table.addCell(createCell("ENTIDAD", nombreEntidad, labelFont, valueFont));
        table.addCell(createCell("TIPO DE PERMISO", sol.getTipoPermiso() != null ? sol.getTipoPermiso() : "N/A", labelFont, valueFont));
        table.addCell(createCell("ESTADO", sol.getEstado() != null ? sol.getEstado() : "N/A", labelFont, valueFont));
        table.addCell(createCell("N° SUCE", sol.getNumeroSuce() != null ? sol.getNumeroSuce() : "Pendiente", labelFont, valueFont));
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void agregarChecklist(Document document, List<DocumentoPermiso> documentos, Font sectionFont, Font labelFont, Font valueFont) throws Exception {
        document.add(new Paragraph("5. CHECKLIST DOCUMENTAL", sectionFont));
        document.add(new Paragraph(" "));
        if (documentos == null || documentos.isEmpty()) {
            document.add(new Paragraph("No se encontraron documentos requeridos para este tipo de permiso.", labelFont));
            return;
        }
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 5, 2});
        table.addCell(new PdfPCell(new Paragraph("DOCUMENTO", labelFont)));
        table.addCell(new PdfPCell(new Paragraph("DESCRIPCION", labelFont)));
        table.addCell(new PdfPCell(new Paragraph("OBLIGATORIO", labelFont)));
        for (DocumentoPermiso doc : documentos) {
            table.addCell(new PdfPCell(new Paragraph(doc.getNombreDocumento(), valueFont)));
            table.addCell(new PdfPCell(new Paragraph(doc.getDescripcion() != null ? doc.getDescripcion() : "", labelFont)));
            table.addCell(new PdfPCell(new Paragraph(doc.isObligatorio() ? "SI" : "NO", valueFont)));
        }
        document.add(table);
    }

    private void agregarFirma(Document document, SolicitudPermiso sol) throws Exception {
        String dataString = "SOLICITUD|" + sol.getId() + "|" + sol.getUsuarioId() + "|" + sol.getCodigoEntidad() + "|" + sol.getEstado();
        String signatureHash = "A5F9B7E31D2C8E4A0B9C8D7E6F5A4B3C";
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataString.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            signatureHash = hexString.toString().toUpperCase().substring(0, 32);
        } catch (Exception e) {
            LoggerUtil.error("Error computing SHA-256 signature for permit PDF", e);
        }
        document.add(new Paragraph("\n"));
        Paragraph sigPara = new Paragraph("CODIGO DE FIRMA DIGITAL (SHA-256): " + signatureHash, new Font(Font.COURIER, 7, Font.NORMAL));
        sigPara.setAlignment(Element.ALIGN_CENTER);
        document.add(sigPara);
    }

    private PdfPCell createCell(String label, String value, Font lFont, Font vFont) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Paragraph(label, lFont));
        cell.addElement(new Paragraph(value != null ? value : "N/A", vFont));
        cell.setPadding(5);
        return cell;
    }
}
