package com.importease.proyecto.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.PageSize;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import com.importease.proyecto.model.Importacion;

/**
 * Servicio premium para la generaciÃ³n de documentos PDF referenciales (PRE-DAM) emulando el formato formal de SUNAT.
 */
public class PdfService {

    public byte[] generarPdfDam(Importacion imp, String nombreUsuario) {
        return generarPdfDam(imp, nombreUsuario, "N/A");
    }

    public byte[] generarPdfDam(Importacion imp, String nombreUsuario, String ruc) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // Configurar el documento A4
            Document document = new Document(com.lowagie.text.PageSize.A4, 36, 36, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // QA-010: Watermark diagonal "NO OFICIAL - SIMULACIÃ“N ACADÃ‰MICA"
            PdfContentByte canvas = writer.getDirectContentUnder();
            canvas.saveState();
            PdfGState gstate = new PdfGState();
            gstate.setFillOpacity(0.15f);
            canvas.setGState(gstate);
            canvas.beginText();
            canvas.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED), 48);
            canvas.setColorFill(new Color(100, 100, 100));
            float pageW = PageSize.A4.getWidth();
            float pageH = PageSize.A4.getHeight();
            canvas.showTextAligned(Element.ALIGN_CENTER, "NO OFICIAL - SIMULACIÃ“N ACADÃ‰MICA", pageW / 2, pageH / 2, 45);
            canvas.endText();
            canvas.restoreState();

            // Colores Institucionales de SUNAT
            Color sunatBlue = new Color(15, 23, 42); // Navy oscuro premium
            Color sunatLightGray = new Color(245, 245, 245);
            Color textDark = new Color(31, 41, 55);

            // Fuentes Estilizadas
            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, sunatBlue);
            Font sectionTitleFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
            Font labelFont = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);
            Font valueFont = new Font(Font.HELVETICA, 9, Font.BOLD, textDark);
            Font textFont = new Font(Font.HELVETICA, 8, Font.NORMAL, textDark);

            // 1. Logotipo Referencial y TÃ­tulo
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{60, 40});

            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            Paragraph logoSunat = new Paragraph("SUNAT PRE-DAM", new Font(Font.HELVETICA, 18, Font.BOLD, sunatBlue));
            Paragraph subLogo = new Paragraph("SUPERINTENDENCIA NACIONAL DE ADUANAS Y DE ADMINISTRACIÃ“N TRIBUTARIA", new Font(Font.HELVETICA, 6, Font.NORMAL, Color.GRAY));
            logoCell.addElement(logoSunat);
            logoCell.addElement(subLogo);
            headerTable.addCell(logoCell);

            PdfPCell titleCell = new PdfPCell();
            titleCell.setBorder(Rectangle.NO_BORDER);
            titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph damTitle = new Paragraph("DECLARACIÃ“N ADUANERA DE MERCANCÃAS", new Font(Font.HELVETICA, 10, Font.BOLD, sunatBlue));
            Paragraph preDamText = new Paragraph("EXPEDIENTE PRE-DAM REFERENCIAL", new Font(Font.HELVETICA, 8, Font.BOLD, new Color(59, 130, 246)));
            damTitle.setAlignment(Element.ALIGN_RIGHT);
            preDamText.setAlignment(Element.ALIGN_RIGHT);
            titleCell.addElement(damTitle);
            titleCell.addElement(preDamText);
            headerTable.addCell(titleCell);

            document.add(headerTable);
            document.add(new Paragraph(" "));

            // 2. SECCIÃ“N A: IDENTIFICACIÃ“N DEL IMPORTADOR
            PdfPTable secATable = new PdfPTable(3);
            secATable.setWidthPercentage(100);
            secATable.setWidths(new float[]{45, 25, 30});

            // Encabezado de SecciÃ³n A
            PdfPCell headerA = new PdfPCell(new Paragraph("A. IDENTIFICACIÃ“N DEL IMPORTADOR", sectionTitleFont));
            headerA.setColspan(3);
            headerA.setBackgroundColor(sunatBlue);
            headerA.setPadding(6);
            secATable.addCell(headerA);

            secATable.addCell(createCell("1.1 IMPORTADOR (RAZÃ“N SOCIAL)", nombreUsuario, labelFont, valueFont));
            secATable.addCell(createCell("1.2 RUC", ruc != null ? ruc : "N/A", labelFont, valueFont));
            secATable.addCell(createCell("1.3 ADUANA JURISDICCIONAL", "118 - MARÃTIMA DEL CALLAO", labelFont, valueFont));

            document.add(secATable);
            document.add(new Paragraph("\n"));

            // 3. SECCIÃ“N B: DESTINACIÃ“N Y TRANSPORTE
            PdfPTable secBTable = new PdfPTable(4);
            secBTable.setWidthPercentage(100);
            secBTable.setWidths(new float[]{25, 25, 25, 25});

            // Encabezado de SecciÃ³n B
            PdfPCell headerB = new PdfPCell(new Paragraph("B. DESTINACIÃ“N, PROCEDENCIA Y LOGÃSTICA", sectionTitleFont));
            headerB.setColspan(4);
            headerB.setBackgroundColor(sunatBlue);
            headerB.setPadding(6);
            secBTable.addCell(headerB);

            String viaTransporte = imp.getIncoterm() != null && imp.getIncoterm().equalsIgnoreCase("CIP") ? "2 - AÃ‰REA" : "1 - MARÃTIMA";
            String puertoArribo = imp.getIncoterm() != null && imp.getIncoterm().equalsIgnoreCase("CIP") ? "LIMA - JORGE CHÃVEZ (PEAP1)" : "CALLAO - MUELLE SUR/NORTE (PECEL)";
            String depositoTemp = imp.getIncoterm() != null && imp.getIncoterm().equalsIgnoreCase("CIP") ? "Talma Servicios Aeroportuarios" : "APM Terminals / DP World Callao";

            secBTable.addCell(createCell("2.1 INCOTERM PACTADO", imp.getIncoterm() != null ? imp.getIncoterm().toUpperCase() : "FOB", labelFont, valueFont));
            secBTable.addCell(createCell("2.2 PAÃS DE ORIGEN", imp.getPaisOrigen() != null ? imp.getPaisOrigen().toUpperCase() : "CHINA", labelFont, valueFont));
            secBTable.addCell(createCell("2.3 VÃA DE TRANSPORTE", viaTransporte, labelFont, valueFont));
            secBTable.addCell(createCell("2.4 PUERTO DE ARRIBO", puertoArribo, labelFont, valueFont));

            PdfPCell depCell = createCell("2.5 DEPÃ“SITO TEMPORAL AUTORIZADO", depositoTemp, labelFont, valueFont);
            depCell.setColspan(4);
            secBTable.addCell(depCell);

            document.add(secBTable);
            document.add(new Paragraph("\n"));

            // 4. SECCIÃ“N C: VALORES DECLARADOS (USD)
            PdfPTable secCTable = new PdfPTable(4);
            secCTable.setWidthPercentage(100);
            secCTable.setWidths(new float[]{25, 25, 25, 25});

            // Encabezado de SecciÃ³n C
            PdfPCell headerC = new PdfPCell(new Paragraph("C. VALORES COMERCIALES DECLARADOS (DÃ“LARES AMERICANOS)", sectionTitleFont));
            headerC.setColspan(4);
            headerC.setBackgroundColor(sunatBlue);
            headerC.setPadding(6);
            secCTable.addCell(headerC);

            secCTable.addCell(createCell("3.1 VALOR FOB COMERCIAL", String.format("USD %,.2f", imp.getValorFob()), labelFont, valueFont));
            secCTable.addCell(createCell("3.2 FLETE INTERNACIONAL", String.format("USD %,.2f", imp.getFlete()), labelFont, valueFont));
            secCTable.addCell(createCell("3.3 SEGURO INTERNACIONAL", String.format("USD %,.2f", imp.getSeguro()), labelFont, valueFont));
            
            PdfPCell cifCell = createCell("3.4 VALOR CIF TOTAL", String.format("USD %,.2f", imp.getValorCif()), labelFont, valueFont);
            cifCell.setBackgroundColor(sunatLightGray);
            secCTable.addCell(cifCell);

            document.add(secCTable);
            document.add(new Paragraph("\n"));

            // 5. SECCIÃ“N D: LIQUIDACIÃ“N TRIBUTARIA ADUANERA (PEN/USD)
            PdfPTable secDTable = new PdfPTable(4);
            secDTable.setWidthPercentage(100);
            secDTable.setWidths(new float[]{35, 25, 20, 20});

            // Encabezado de SecciÃ³n D
            PdfPCell headerD = new PdfPCell(new Paragraph("D. LIQUIDACIÃ“N DE LA DEUDA TRIBUTARIA ADUANERA", sectionTitleFont));
            headerD.setColspan(4);
            headerD.setBackgroundColor(sunatBlue);
            headerD.setPadding(6);
            secDTable.addCell(headerD);

            // Cabeceras de tabla de liquidaciÃ³n
            PdfPCell th1 = new PdfPCell(new Paragraph("CONCEPTO GRAVADO", labelFont)); th1.setBackgroundColor(sunatLightGray); secDTable.addCell(th1);
            PdfPCell th2 = new PdfPCell(new Paragraph("BASE IMPONIBLE", labelFont)); th2.setBackgroundColor(sunatLightGray); secDTable.addCell(th2);
            PdfPCell th3 = new PdfPCell(new Paragraph("TASA ADUANA", labelFont)); th3.setBackgroundColor(sunatLightGray); secDTable.addCell(th3);
            PdfPCell th4 = new PdfPCell(new Paragraph("MONTO (USD)", labelFont)); th4.setBackgroundColor(sunatLightGray); secDTable.addCell(th4);

            // Ad-valorem
            secDTable.addCell(new PdfPCell(new Paragraph("AD-VALOREM", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph(String.format("CIF (USD %,.2f)", imp.getValorCif()), textFont)));
            String avTasa = imp.getMontoAdValorem() > 0 ? "6%" : "0%";
            secDTable.addCell(new PdfPCell(new Paragraph(avTasa, textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph(String.format("USD %,.2f", imp.getMontoAdValorem()), textFont)));

            // ISC
            secDTable.addCell(new PdfPCell(new Paragraph("ISC (IMPUESTO SELECTIVO)", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("CIF + Ad-Valorem", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("0%", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("USD 0.00", textFont)));

            // IGV
            secDTable.addCell(new PdfPCell(new Paragraph("IGV (IMPUESTO GENERAL TASA)", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("CIF + Ad-Valorem", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("16%", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph(String.format("USD %,.2f", imp.getMontoIgb()), textFont)));

            // IPM
            secDTable.addCell(new PdfPCell(new Paragraph("IPM (PROMICIÃ“N MUNICIPAL)", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("CIF + Ad-Valorem", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("2%", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph(String.format("USD %,.2f", imp.getMontoIpm()), textFont)));

            // PercepciÃ³n
            secDTable.addCell(new PdfPCell(new Paragraph("PERCEPCIÃ“N SUNAT", textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph("CIF + Impuestos", textFont)));
            double basePercepcion = imp.getValorCif() + imp.getMontoAdValorem() + imp.getMontoIgb() + imp.getMontoIpm();
            String perTasa = "0%";
            if (imp.getMontoPercepcion() > 0 && basePercepcion > 0) {
                perTasa = (imp.getMontoPercepcion() / basePercepcion > 0.05) ? "10%" : "3.5%";
            }
            secDTable.addCell(new PdfPCell(new Paragraph(perTasa, textFont)));
            secDTable.addCell(new PdfPCell(new Paragraph(String.format("USD %,.2f", imp.getMontoPercepcion()), textFont)));

            // Fila Total
            PdfPCell totalLabel = new PdfPCell(new Paragraph("DEUDA TRIBUTARIA TOTAL ESTIMADA (USD / PEN)", valueFont));
            totalLabel.setColspan(2);
            totalLabel.setBackgroundColor(sunatLightGray);
            secDTable.addCell(totalLabel);

            PdfPCell tcCell = new PdfPCell(new Paragraph(String.format("T/C: %,.3f", imp.getTipoCambio()), textFont));
            tcCell.setBackgroundColor(sunatLightGray);
            secDTable.addCell(tcCell);

            BigDecimal totalSoles = imp.getTotalImpuestosBD().multiply(imp.getTipoCambioBD()).setScale(2, RoundingMode.HALF_UP);
            PdfPCell totalVal = new PdfPCell(new Paragraph(String.format("S/ %,.2f\n(USD %,.2f)", totalSoles.doubleValue(), imp.getTotalImpuestos()), valueFont));
            totalVal.setBackgroundColor(sunatLightGray);
            totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            secDTable.addCell(totalVal);

            document.add(secDTable);
            document.add(new Paragraph("\n"));

            // 6. SECCIÃ“N E: FIRMA DIGITAL Y TRAZABILIDAD
            // Cryptographic SHA-256 fingerprint signature
            String dataString = "DAM|" + imp.getId() + "|" + imp.getNumeroDam() + "|" + imp.getTotalImpuestos() + "|" + ruc;
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
            } catch (Exception e) { LoggerUtil.error("Error computing SHA-256 signature for PDF", e); }

            PdfPTable secETable = new PdfPTable(2);
            secETable.setWidthPercentage(100);
            secETable.setWidths(new float[]{80, 20});

            PdfPCell headerE = new PdfPCell(new Paragraph("E. TRAZABILIDAD Y FIRMA DIGITAL DE LA PRE-DAM", sectionTitleFont));
            headerE.setColspan(2);
            headerE.setBackgroundColor(sunatBlue);
            headerE.setPadding(6);
            secETable.addCell(headerE);

            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setPadding(8);
            signatureCell.addElement(new Paragraph("CÃ“DIGO DE FIRMA DIGITAL (SHA-256)", labelFont));
            Paragraph sigPara = new Paragraph(signatureHash, new Font(Font.COURIER, 8, Font.BOLD, sunatBlue));
            signatureCell.addElement(sigPara);
            signatureCell.addElement(new Paragraph("\nADVERTENCIA FORMAL: Este es un documento referencial e informativo para pre-embarque y soporte del importador. La DAM definitiva debe ser tramitada de acuerdo al Arancel de Aduanas por un Agente de Aduanas debidamente autorizado ante SUNAT.", labelFont));
            secETable.addCell(signatureCell);

            PdfPCell qrCell = new PdfPCell();
            qrCell.setPadding(10);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            
            // Simular un QR Code mediante un patrÃ³n estructurado de texto mono
            Paragraph qrPlaceholder = new Paragraph("[QR CODE]\nVERIFY PRE-DAM\nVALIDATION", new Font(Font.COURIER, 6, Font.NORMAL, Color.GRAY));
            qrPlaceholder.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrPlaceholder);
            secETable.addCell(qrCell);

            document.add(secETable);

            // Footer
            document.add(new Paragraph("\n"));
            Paragraph footer = new Paragraph("Generado el: " + new java.util.Date().toString() + " Â· ImportEase Copiloto Aduanero", labelFont);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
        } catch (Exception e) {
            LoggerUtil.error("Error al generar PDF PRE-DAM: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    private PdfPCell createCell(String label, String value, Font lFont, Font vFont) {
        PdfPCell cell = new PdfPCell();
        cell.addElement(new Paragraph(label, lFont));
        cell.addElement(new Paragraph(value != null ? value : "N/A", vFont));
        cell.setPadding(5);
        return cell;
    }
}


