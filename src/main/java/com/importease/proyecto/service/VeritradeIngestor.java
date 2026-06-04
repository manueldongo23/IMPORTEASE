package com.importease.proyecto.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.sql.*;
import java.util.*;

public class VeritradeIngestor {

    public Map<String, Object> importarXLSX(InputStream fileStream, String nombreArchivo, int usuarioId) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> registros = new ArrayList<>();
        int importados = 0;
        int errores = 0;

        try (Workbook wb = new XSSFWorkbook(fileStream)) {
            Sheet sheet = wb.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                result.put("status", "error");
                result.put("mensaje", "El archivo no tiene fila de encabezados");
                return result;
            }

            // Mapear columnas esperadas de Veritrade
            Map<Integer, String> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String val = getCellValue(cell).toLowerCase().trim();
                if (val.contains("partida") || val.contains("hs") || val.contains("codigo"))
                    colMap.put(cell.getColumnIndex(), "hsCode");
                else if (val.contains("descripcion") || val.contains("producto"))
                    colMap.put(cell.getColumnIndex(), "descripcion");
                else if (val.contains("importador") || val.contains("importador"))
                    colMap.put(cell.getColumnIndex(), "importador");
                else if (val.contains("exportador") || val.contains("exportador") || val.contains("proveedor"))
                    colMap.put(cell.getColumnIndex(), "exportador");
                else if (val.contains("origen") || val.contains("pais"))
                    colMap.put(cell.getColumnIndex(), "paisOrigen");
                else if (val.contains("fob") || val.contains("valor"))
                    colMap.put(cell.getColumnIndex(), "valorFob");
                else if (val.contains("cif"))
                    colMap.put(cell.getColumnIndex(), "valorCif");
                else if (val.contains("peso"))
                    colMap.put(cell.getColumnIndex(), "peso");
                else if (val.contains("transporte") || val.contains("via"))
                    colMap.put(cell.getColumnIndex(), "viaTransporte");
            }

            String sql = "INSERT INTO historico_importaciones_partida "
                    + "(hs_code, descripcion, importador, exportador, pais_origen, valor_fob, valor_cif, peso, via_transporte, fuente, usuario_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'VERITRADE', ?)";

            try (Connection con = ConexionDB.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;

                    Map<String, String> reg = new LinkedHashMap<>();
                    try {
                        for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
                            String val = getCellValue(row.getCell(entry.getKey()));
                            reg.put(entry.getValue(), val);
                            switch (entry.getValue()) {
                                case "hsCode":     ps.setString(1, val); break;
                                case "descripcion": ps.setString(2, val); break;
                                case "importador":  ps.setString(3, val); break;
                                case "exportador":  ps.setString(4, val); break;
                                case "paisOrigen":  ps.setString(5, val); break;
                                case "valorFob":    ps.setBigDecimal(6, parseDecimal(val)); break;
                                case "valorCif":    ps.setBigDecimal(7, parseDecimal(val)); break;
                                case "peso":        ps.setBigDecimal(8, parseDecimal(val)); break;
                                case "viaTransporte": ps.setString(9, val); break;
                            }
                        }
                        ps.setInt(10, usuarioId);
                        ps.executeUpdate();
                        registros.add(reg);
                        importados++;
                    } catch (Exception e) {
                        errores++;
                    }
                }
            }

            result.put("status", "ok");
            result.put("importados", importados);
            result.put("errores", errores);
            result.put("registros", registros);
            result.put("mensaje", importados + " registros importados desde " + nombreArchivo);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("mensaje", "Error procesando archivo: " + e.getMessage());
            LoggerUtil.error("Error importando Veritrade XLSX", e);
        }
        return result;
    }

    public Map<String, Object> importarCSV(InputStream fileStream, String nombreArchivo, int usuarioId) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> registros = new ArrayList<>();
        int importados = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(fileStream, "UTF-8"))) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                result.put("status", "error");
                result.put("mensaje", "Archivo CSV vacÃ­o");
                return result;
            }

            String[] headers = headerLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            Map<Integer, String> colMap = new HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].toLowerCase().trim().replaceAll("\"", "");
                if (h.contains("partida") || h.contains("hs"))
                    colMap.put(i, "hsCode");
                else if (h.contains("descripcion"))
                    colMap.put(i, "descripcion");
                else if (h.contains("importador"))
                    colMap.put(i, "importador");
                else if (h.contains("exportador") || h.contains("proveedor"))
                    colMap.put(i, "exportador");
                else if (h.contains("origen") || h.contains("pais"))
                    colMap.put(i, "paisOrigen");
                else if (h.contains("fob"))
                    colMap.put(i, "valorFob");
                else if (h.contains("cif"))
                    colMap.put(i, "valorCif");
            }

            String sql = "INSERT INTO historico_importaciones_partida "
                    + "(hs_code, descripcion, importador, exportador, pais_origen, fuente, usuario_id) "
                    + "VALUES (?, ?, ?, ?, ?, 'VERITRADE', ?)";

            try (Connection con = ConexionDB.obtenerConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                    Map<String, String> reg = new LinkedHashMap<>();
                    try {
                        for (Map.Entry<Integer, String> entry : colMap.entrySet()) {
                            if (entry.getKey() < cols.length) {
                                String val = cols[entry.getKey()].trim().replaceAll("\"", "");
                                reg.put(entry.getValue(), val);
                                switch (entry.getValue()) {
                                    case "hsCode":     ps.setString(1, val); break;
                                    case "descripcion": ps.setString(2, val); break;
                                    case "importador":  ps.setString(3, val); break;
                                    case "exportador":  ps.setString(4, val); break;
                                    case "paisOrigen":  ps.setString(5, val); break;
                                }
                            }
                        }
                        ps.setInt(6, usuarioId);
                        ps.executeUpdate();
                        registros.add(reg);
                        importados++;
                    } catch (Exception e) { LoggerUtil.error("Error importing row from XLSX", e); }
                }
            }

            result.put("status", "ok");
            result.put("importados", importados);
            result.put("registros", registros);
            result.put("mensaje", importados + " registros importados desde " + nombreArchivo);

        } catch (Exception e) {
            result.put("status", "error");
            result.put("mensaje", "Error procesando CSV: " + e.getMessage());
        }
        return result;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield (v == Math.floor(v)) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private java.math.BigDecimal parseDecimal(String val) {
        if (val == null || val.isBlank()) return java.math.BigDecimal.ZERO;
        try {
            return new java.math.BigDecimal(val.replaceAll("[^\\d.,]", "").replace(",", "."));
        } catch (Exception e) {
            return java.math.BigDecimal.ZERO;
        }
    }
}


