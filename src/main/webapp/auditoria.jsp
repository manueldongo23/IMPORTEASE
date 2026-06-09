<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%@ page import="com.importease.proyecto.dto.AuditoriaDTO" %>
<%@ page import="com.importease.proyecto.dto.AuditoriaDTO.OperacionAuditoriaDTO" %>
<%@ page import="com.importease.proyecto.dto.AuditoriaDTO.PermisoAuditoriaDTO" %>
<%@ page import="com.importease.proyecto.dto.AuditoriaDTO.PartidaAuditoriaDTO" %>
<%@ page import="com.importease.proyecto.dto.AuditoriaDTO.IncidenciaDTO" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    int usuarioId = (int) session.getAttribute("usuarioId");
    String userNombre = (String) session.getAttribute("usuarioNombre");
    if (userNombre == null) userNombre = "Manuel";
    String userRuc = (String) session.getAttribute("usuarioRuc");

    // Datos obtenidos limpiamente del DTO
    AuditoriaDTO dto = com.importease.proyecto.service.AuditoriaServicio.obtenerDatosAuditoria(usuarioId, userRuc);
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Centro de Control y Auditoría</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <% request.setAttribute("activePage", "auditoria"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-hidden relative">
        <!-- Header -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-20 shrink-0">
            <div class="flex items-center gap-4">
                <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                    <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                    <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Centro de Auditoría y Control Fiscal Aduanero</span>
                </div>
            </div>
            
            <div class="text-[11px] font-bold text-[var(--text-secondary)] font-mono">
                OPERADOR: <strong class="text-[var(--accent)]"><%= com.importease.proyecto.service.HtmlUtil.escape(userNombre) %></strong> (RUC: <%= com.importease.proyecto.service.HtmlUtil.escape(userRuc) %>)
            </div>
        </header>

        <!-- Banner de Advertencia Referencial -->
        <div class="px-8 pt-6 shrink-0">
            <div class="bg-[var(--warning-soft)] border border-[var(--warning)]/20 rounded-2xl p-4 flex items-center gap-4 backdrop-blur-md">
                <span class="text-xl">📊</span>
                <div class="text-[11px] text-[var(--warning)] font-semibold">
                    <strong class="text-[var(--warning)] font-bold uppercase tracking-widest text-[9px] block mb-0.5">Control Didáctico de Cumplimiento</strong>
                    Esta consola de control muestra la simulación integral del pre-expediente VUCE, el cálculo fiscal referencial aduanero y la validez documental de sus operaciones. Ninguna de estas simulaciones representa trámites formales oficiales ante aduanas o la VUCE.
                </div>
            </div>
        </div>

        <!-- KPIs Generales -->
        <div class="px-8 pt-6 grid grid-cols-2 md:grid-cols-4 gap-4 shrink-0">
            <!-- 1. Operaciones Evaluadas -->
            <div class="glass-card p-5 flex flex-col justify-between relative overflow-hidden group hover:border-[var(--accent)] transition-all">
                <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest block">Evaluaciones Totales</span>
                <p class="text-3xl font-black text-[var(--text-primary)] mt-2 font-mono"><%= dto.getTotalOperaciones() %></p>
                <span class="text-[8px] text-[var(--text-secondary)] font-bold uppercase mt-2">Operaciones Registradas</span>
                <div class="absolute bottom-0 left-0 h-1 w-1/3 bg-[var(--accent)] group-hover:w-full transition-all duration-500"></div>
            </div>
            <!-- 2. Permisos Pre-VUCE -->
            <div class="glass-card p-5 flex flex-col justify-between relative overflow-hidden group hover:border-[var(--accent)] transition-all">
                <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest block">Expedientes Pre-VUCE</span>
                <p class="text-3xl font-black text-[var(--text-primary)] mt-2 font-mono"><%= dto.getTotalPermisos() %></p>
                <span class="text-[8px] text-[var(--text-secondary)] font-bold uppercase mt-2">Licencias y Trámites VUCE</span>
                <div class="absolute bottom-0 left-0 h-1 w-1/3 bg-[var(--accent)] group-hover:w-full transition-all duration-500"></div>
            </div>
            <!-- 3. Liquidación Total Referencial -->
            <div class="glass-card p-5 flex flex-col justify-between relative overflow-hidden group hover:border-[var(--accent)] transition-all">
                <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest block">Liquidación Fiscal</span>
                <p class="text-2xl font-black text-[var(--success)] mt-2 font-mono">S/ <%= String.format("%,.2f", dto.getTotalImpuestos()) %></p>
                <span class="text-[8px] text-[var(--text-secondary)] font-bold uppercase mt-2">Tributos Totales Estimados</span>
                <div class="absolute bottom-0 left-0 h-1 w-1/3 bg-[var(--success)] group-hover:w-full transition-all duration-500"></div>
            </div>
            <!-- 4. Alertas de Cumplimiento -->
            <div class="glass-card p-5 flex flex-col justify-between relative overflow-hidden group hover:border-[var(--accent)] transition-all">
                <span class="text-[9px] font-black text-[var(--danger)] uppercase tracking-widest block">Alertas Documentales</span>
                <p class="text-3xl font-black text-[var(--danger)] mt-2 font-mono"><%= dto.getTotalAlertas() %></p>
                <span class="text-[8px] text-[var(--text-secondary)] font-bold uppercase mt-2">Checklists Incompletos</span>
                <div class="absolute bottom-0 left-0 h-1 w-1/3 bg-[var(--danger)] group-hover:w-full transition-all duration-500"></div>
            </div>
        </div>

        <!-- Split Layout Panel -->
        <div class="flex-1 overflow-y-auto p-8 grid grid-cols-1 lg:grid-cols-3 gap-6 custom-scrollbar pb-24">
            
            <!-- Left Panel: Operations Audit Trail -->
            <div class="lg:col-span-2 space-y-6">
                <div class="glass-card p-6">
                    <h3 class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest mb-4">Trazabilidad Reguladora de Operaciones</h3>
                    <div class="overflow-x-auto rounded-2xl border border-[var(--border)] bg-[var(--surface-2)]/20">
                        <table class="w-full text-left text-xs border-collapse font-sans">
                            <thead>
                                <tr class="bg-[var(--surface-2)] border-b border-[var(--border)] font-bold uppercase tracking-wider text-[var(--text-secondary)]">
                                    <th class="p-3.5">Operación / Producto</th>
                                    <th class="p-3.5">Código HS</th>
                                    <th class="p-3.5">Entidad Control</th>
                                    <th class="p-3.5 text-right">Impuestos</th>
                                    <th class="p-3.5 text-center">Canal Referencial</th>
                                </tr>
                            </thead>
                            <tbody class="divide-y divide-[var(--border)] font-semibold text-[var(--text-secondary)]">
                                <% if (dto.getOperaciones().isEmpty()) { %>
                                    <tr>
                                        <td colspan="5" class="p-6 text-center text-[var(--text-tertiary)] font-medium">No se han registrado evaluaciones de preimportación.</td>
                                    </tr>
                                <% } else { 
                                    for (OperacionAuditoriaDTO op : dto.getOperaciones()) { 
                                        String canal = op.getCanalAsignado();
                                        String canalClass = "text-gray-400 bg-gray-100 border-gray-200";
                                        if ("VERDE".equalsIgnoreCase(canal)) canalClass = "text-[var(--success)] bg-[var(--success-soft)] border-[var(--success)]";
                                        else if ("NARANJA".equalsIgnoreCase(canal)) canalClass = "text-[var(--warning)] bg-[var(--warning-soft)] border-[var(--warning)]";
                                        else if ("ROJO".equalsIgnoreCase(canal)) canalClass = "text-[var(--danger)] bg-[var(--danger-soft)] border-[var(--danger)]";
                                %>
                                    <tr class="hover:bg-[var(--surface-2)]/30 transition-colors">
                                        <td class="p-3.5">
                                            <div class="font-bold text-[var(--text-primary)]"><%= com.importease.proyecto.service.HtmlUtil.escape(op.getProductoDesc()) %></div>
                                            <div class="text-[9px] text-[var(--text-tertiary)] font-bold tracking-wider uppercase mt-0.5">OP-<%= String.format("%05d", op.getId()) %> · Origen: <%= com.importease.proyecto.service.HtmlUtil.escape(op.getPaisOrigen()) %></div>
                                        </td>
                                        <td class="p-3.5 font-mono text-[10px] text-[var(--text-secondary)] font-bold"><%= com.importease.proyecto.service.HtmlUtil.escape(op.getHsCode()) %></td>
                                        <td class="p-3.5">
                                            <span class="px-2 py-0.5 rounded text-[10px] font-black uppercase bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-secondary)]"><%= com.importease.proyecto.service.HtmlUtil.escape(op.getEntidad()) %></span>
                                        </td>
                                        <td class="p-3.5 text-right font-mono text-[var(--text-primary)]">S/ <%= String.format("%,.2f", op.getTotalImpuestos()) %></td>
                                        <td class="p-3.5 text-center">
                                            <span class="px-2.5 py-1 rounded-full text-[9px] font-black uppercase border <%= canalClass %>"><%= com.importease.proyecto.service.HtmlUtil.escape(canal) %></span>
                                        </td>
                                    </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Right Panel: Compliance and Permisos VUCE -->
            <div class="space-y-6 shrink-0">
                <!-- VUCE Permits Status -->
                <div class="glass-card p-6">
                    <h3 class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest mb-4">Expedientes & SUCE Pre-VUCE</h3>
                    <div class="space-y-3 max-h-[300px] overflow-y-auto custom-scrollbar pr-1">
                        <% if (dto.getPermisos().isEmpty()) { %>
                            <p class="text-xs text-[var(--text-tertiary)] text-center py-4 font-semibold">Ninguna solicitud de permiso pre-VUCE iniciada.</p>
                        <% } else {
                            for (PermisoAuditoriaDTO p : dto.getPermisos()) {
                                String est = p.getEstado();
                                String estClass = "text-[var(--text-tertiary)] bg-[var(--surface-2)] border-[var(--border)]";
                                if ("APROBADO".equals(est)) estClass = "text-[var(--success)] bg-[var(--success-soft)] border-[var(--success)]";
                                else if ("ENVIADO_A_VUCE".equals(est) || "EN_EVALUACION".equals(est)) estClass = "text-[var(--warning)] bg-[var(--warning-soft)] border-[var(--warning)]";
                                else if ("OBSERVADO".equals(est)) estClass = "text-[var(--danger)] bg-[var(--danger-soft)] border-[var(--danger)]";
                        %>
                            <div class="p-4 rounded-2xl bg-[var(--surface-2)]/40 border border-[var(--border)] hover:border-[var(--accent)] transition-all flex flex-col justify-between gap-2 shadow-sm">
                                <div class="flex justify-between items-start">
                                    <div>
                                        <h5 class="text-xs font-black text-[var(--text-primary)]"><%= com.importease.proyecto.service.HtmlUtil.escape(p.getCodigoEntidad()) %> — <%= com.importease.proyecto.service.HtmlUtil.escape(p.getTipoPermiso()) %></h5>
                                        <p class="text-[9px] text-[var(--text-tertiary)] font-bold tracking-wider mt-0.5">Operación: OP-<%= String.format("%05d", p.getOperacionId()) %></p>
                                    </div>
                                    <span class="px-2 py-0.5 rounded text-[8px] font-black uppercase border <%= estClass %> shrink-0"><%= com.importease.proyecto.service.HtmlUtil.escape(est) %></span>
                                </div>
                                <div class="flex justify-between items-center text-[9px] font-mono text-[var(--text-secondary)] pt-2 border-t border-[var(--border)] mt-2">
                                    <span>SUCE: <%= p.getNumeroSuce() != null ? com.importease.proyecto.service.HtmlUtil.escape(p.getNumeroSuce()) : "En borrador" %></span>
                                    <span>R.D.: <%= p.getNumeroDocumentoResolutivo() != null ? com.importease.proyecto.service.HtmlUtil.escape(p.getNumeroDocumentoResolutivo()) : "Pendiente" %></span>
                                </div>
                            </div>
                        <% } } %>
                    </div>
                </div>

                <!-- HS Top Searches -->
                <div class="glass-card p-6">
                    <h3 class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest mb-4">Ranking Partidas HS Buscadas</h3>
                    <div class="space-y-2">
                        <% if (dto.getTopPartidas().isEmpty()) { %>
                            <div class="p-3 bg-[var(--surface-2)]/50 border border-[var(--border)] rounded-xl text-center text-xs text-[var(--text-tertiary)] font-medium">No hay registros de consultas.</div>
                        <% } else {
                            int rank = 1;
                            for (PartidaAuditoriaDTO tp : dto.getTopPartidas()) {
                        %>
                            <div class="p-3 bg-[var(--surface-2)]/40 border border-[var(--border)] rounded-xl flex items-center justify-between text-xs font-semibold">
                                <div class="flex items-center gap-2">
                                    <span class="w-5 h-5 rounded-full bg-white border border-[var(--border)] flex items-center justify-center font-bold text-[9px] text-[var(--text-tertiary)]"><%= rank++ %></span>
                                    <span class="text-[var(--accent)] font-mono font-bold"><%= com.importease.proyecto.service.HtmlUtil.escape(tp.getCodigo()) %></span>
                                </div>
                                <span class="text-[10px] text-[var(--text-tertiary)] font-bold"><%= tp.getBusquedas() %> consultas</span>
                            </div>
                        <% } } %>
                    </div>
                </div>

                <!-- Compliance policy logs -->
                <div class="glass-card p-6">
                    <h3 class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest mb-4">Incidencias de Cumplimiento</h3>
                    <div class="space-y-3 text-[10px] font-semibold text-[var(--text-secondary)]">
                        <% for (IncidenciaDTO log : dto.getLogsIncidencias()) { %>
                            <div class="p-3 rounded-xl bg-[var(--surface-2)]/40 border border-[var(--border)] flex flex-col gap-1 shadow-sm">
                                <div class="flex justify-between items-center">
                                    <strong class="text-[var(--text-primary)] uppercase tracking-wider text-[9px]"><%= log.getModulo() %></strong>
                                    <span class="px-1.5 py-0.5 rounded text-[8px] bg-white border border-[var(--border)] font-mono text-[var(--accent)]"><%= log.getOrigen() %></span>
                                </div>
                                <p class="text-[var(--text-secondary)] leading-normal mt-1"><%= log.getDesc() %></p>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>

        </div>
    </main>
</body>
</html>
