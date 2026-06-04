package com.importease.proyecto.controller;

import com.google.gson.Gson;
import com.importease.proyecto.repository.documentos.DocumentoRepository;
import com.importease.proyecto.service.LoggerUtil;
import com.importease.proyecto.service.OperacionAuthorizationService;
import com.importease.proyecto.service.documentos.DocumentoFileService;
import com.importease.proyecto.service.documentos.DocumentoResponseBuilder;
import com.importease.proyecto.service.documentos.DocumentoValidationService;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@WebServlet("/api/documentos/*")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2, // 2MB
    maxFileSize = 1024 * 1024 * 5,       // 5MB (LÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­mite estricto de auditorÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­a)
    maxRequestSize = 1024 * 1024 * 10    // 10MB
)
public class DocumentoController extends HttpServlet {
    private final Gson gson = new Gson();
    private final OperacionAuthorizationService authorizationService = new OperacionAuthorizationService();
    private final DocumentoRepository documentoRepository = new DocumentoRepository();
    private final DocumentoValidationService validationService = new DocumentoValidationService();
    private final DocumentoFileService fileService = new DocumentoFileService();
    private final DocumentoResponseBuilder responseBuilder = new DocumentoResponseBuilder();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.setStatus(401);
            resp.getWriter().print("{\"error\":\"No autenticado\"}");
            return;
        }
        int usuarioId = (Integer) session.getAttribute("usuarioId");

        if ("/listar".equals(path)) {
            String impIdStr = req.getParameter("importacionId");
            if (impIdStr == null || impIdStr.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":\"ParÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡metro importacionId requerido\"}");
                return;
            }
            int importacionId = validationService.parsePositiveInt(impIdStr, -1);
            if (importacionId <= 0) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":\"Parametro importacionId invalido\"}");
                return;
            }
            
            // Validar IDOR: asegurar que la operaciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n pertenece al usuario
            if (!authorizationService.isOperacionOwnedByUser(importacionId, usuarioId)) {
                resp.setStatus(403);
                resp.getWriter().print("{\"error\":\"Acceso no autorizado\"}");
                return;
            }

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            List<Map<String, Object>> docs = documentoRepository.listarDocumentos(importacionId);
            resp.getWriter().print(gson.toJson(docs));
        }
        else if ("/descargar".equals(path)) {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":\"ID de documento requerido\"}");
                return;
            }
            int documentoId = validationService.parsePositiveInt(idStr, -1);
            if (documentoId <= 0) {
                resp.setStatus(400);
                resp.getWriter().print("{\"error\":\"ID de documento invalido\"}");
                return;
            }

            Map<String, Object> doc = documentoRepository.obtenerDocumentoPorId(documentoId);
            if (doc == null) {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"Documento no encontrado\"}");
                return;
            }

            int importacionId = (Integer) doc.get("importacion_id");
            if (!authorizationService.isOperacionOwnedByUser(importacionId, usuarioId)) {
                resp.setStatus(403);
                resp.getWriter().print("{\"error\":\"Acceso no autorizado (IDOR)\"}");
                return;
            }

            String relativePath = (String) doc.get("ruta_archivo");
            if (relativePath == null || relativePath.isEmpty()) {
                resp.setStatus(404);
                resp.getWriter().print("{\"error\":\"El archivo fÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­sico no ha sido cargado\"}");
                return;
            }

            File file;
            try {
                file = fileService.resolveExistingFile(relativePath);
                String storedHash = (String) doc.get("checksum_hash");
                if (!fileService.checksumMatches(file, storedHash)) {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    resp.setContentType("application/json");
                    resp.getWriter().print("{\"error\":\"El archivo ha sido modificado (checksum mismatch)\"}");
                    return;
                }
            } catch (SecurityException e) {
                resp.setStatus(403);
                resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
                return;
            } catch (Exception e) {
                resp.setStatus(404);
                resp.getWriter().print(gson.toJson(Map.of("error", e.getMessage())));
                return;
            }
            String mimeType = getServletContext().getMimeType(file.getAbsolutePath());
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            resp.setContentType(mimeType);
            resp.setContentLength((int) file.length());
            String headerValue = String.format("attachment; filename=\"%s\"", file.getName().substring(file.getName().indexOf('_') + 1));
            resp.setHeader("Content-Disposition", headerValue);

            try (FileInputStream inStream = new FileInputStream(file);
                 OutputStream outStream = resp.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }
        }
        else {
            resp.setStatus(404);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        HttpSession session = req.getSession(false);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        if (session == null || session.getAttribute("usuarioId") == null) {
            resp.setStatus(401);
            out.print("{\"error\":\"No autenticado\"}");
            return;
        }
        int usuarioId = (Integer) session.getAttribute("usuarioId");

        if ("/subir".equals(path)) {
            try {
                String impIdStr = req.getParameter("importacionId");
                String tipoDocRaw = req.getParameter("tipoDocumento");
                
                if (impIdStr == null || impIdStr.isEmpty() || tipoDocRaw == null || tipoDocRaw.isEmpty()) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"ParÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡metros importacionId y tipoDocumento requeridos\"}");
                    return;
                }
                String tipoDoc = validationService.normalizarTipoDocumento(tipoDocRaw);
                if (!validationService.isTipoDocumentoPermitido(tipoDoc)) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"tipoDocumento invalido. Valores permitidos: FACTURA_COMERCIAL, BILL_OF_LADING, CERTIFICADO_ORIGEN\"}");
                    return;
                }
                int importacionId = validationService.parsePositiveInt(impIdStr, -1);
                if (importacionId <= 0) {
                    resp.setStatus(400);
                    out.print("{\"error\":\"Parametro importacionId invalido\"}");
                    return;
                }

                // Validar IDOR: asegurar que la operaciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n pertenece al usuario
                if (!authorizationService.isOperacionOwnedByUser(importacionId, usuarioId)) {
                    resp.setStatus(403);
                    out.print("{\"error\":\"Acceso no autorizado\"}");
                    return;
                }
                Part filePart = req.getPart("file");
                String validationError = validationService.validarArchivo(filePart);
                if (validationError != null) {
                    resp.setStatus(400);
                    out.print(gson.toJson(Map.of("error", validationError)));
                    return;
                }

                String submittedFileName = validationService.getSubmittedFileName(filePart);
                DocumentoFileService.StoredDocument storedDocument = fileService.store(filePart, submittedFileName);
                String relativePath = storedDocument.getRelativePath();
                String checksumHash = storedDocument.getChecksumHash();
                boolean guardado = documentoRepository.registrarDocumento(importacionId, tipoDoc, relativePath, checksumHash);

                if (guardado) {
                    // Registrar en bitÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¡cora de auditorÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­a
                    documentoRepository.registrarEventoAuditoria(usuarioId, "CARGAR_DOCUMENTO", "operaciones", importacionId, 
                                             "Carga de documento: " + tipoDoc + " para la operaciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n " + importacionId, req.getRemoteAddr());

                    out.print(gson.toJson(responseBuilder.uploadSuccess(relativePath, submittedFileName, checksumHash)));
                } else {
                    resp.setStatus(500);
                    out.print("{\"error\":\"Error al registrar el documento en la base de datos\"}");
                }
            } catch (Exception e) {
                LoggerUtil.error("Error al procesar subida de archivo", e);
                resp.setStatus(500);
                out.print("{\"error\":\"Error interno al subir el archivo. Intente nuevamente.\"}");
            }
        }
        else if ("/eliminar".equals(path)) {
            String idStr = req.getParameter("id");
            if (idStr == null || idStr.isEmpty()) {
                resp.setStatus(400);
                out.print("{\"error\":\"ID de documento requerido\"}");
                return;
            }
            int documentoId = validationService.parsePositiveInt(idStr, -1);
            if (documentoId <= 0) {
                resp.setStatus(400);
                out.print("{\"error\":\"ID de documento invalido\"}");
                return;
            }

            Map<String, Object> doc = documentoRepository.obtenerDocumentoPorId(documentoId);
            if (doc == null) {
                resp.setStatus(404);
                out.print("{\"error\":\"Documento no encontrado\"}");
                return;
            }

            int importacionId = (Integer) doc.get("importacion_id");
            if (!authorizationService.isOperacionOwnedByUser(importacionId, usuarioId)) {
                resp.setStatus(403);
                out.print("{\"error\":\"Acceso no autorizado\"}");
                return;
            }

            boolean eliminado = documentoRepository.eliminarDocumento(documentoId, importacionId, (String) doc.get("tipo_documento"));

            if (eliminado) {
                // Conservamos el archivo fÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â­sico para mantener la inmutabilidad y la cadena de custodia (Soft-Delete)
                documentoRepository.registrarEventoAuditoria(usuarioId, "ELIMINAR_DOCUMENTO", "operaciones", importacionId, 
                                         "EliminaciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n de documento " + doc.get("tipo_documento") + " (Soft-Delete) en la operaciÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Â ÃƒÂ¢Ã¢â€šÂ¬Ã¢â€žÂ¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬ÃƒÂ¢Ã¢â‚¬Å¾Ã‚Â¢ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã‚Â¡ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â³n " + importacionId, req.getRemoteAddr());

                out.print("{\"mensaje\":\"Documento eliminado exitosamente\"}");
            } else {
                resp.setStatus(500);
                out.print("{\"error\":\"Error al eliminar el documento en la base de datos\"}");
            }
        }
        else {
            resp.setStatus(404);
        }
    }


}
