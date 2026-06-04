<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.importease.proyecto.model.Importacion" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%@ page import="com.importease.proyecto.service.PlazoCriticoService" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>

<%
    // Restringir acceso solo a usuarios logueados y administradores
    Integer uIdAttr = (Integer) session.getAttribute("usuarioId");
    if (uIdAttr == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    com.importease.proyecto.repository.UsuarioDAO uDao = new com.importease.proyecto.repository.UsuarioDAO();
    com.importease.proyecto.model.Usuario currentUser = uDao.buscarPorId(uIdAttr);
    if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getPerfil())) {
        response.sendRedirect("../dashboard.jsp");
        return;
    }

    // Listas para las columnas Kanban
    List<Map<String, Object>> colCritico = new ArrayList<>();
    List<Map<String, Object>> colWarning = new ArrayList<>();
    List<Map<String, Object>> colEnPlazo = new ArrayList<>();

    String sql = "SELECT o.*, u.ruc, u.razon_social FROM operaciones o JOIN usuarios u ON o.usuario_id = u.id ORDER BY o.fecha_creacion DESC";

    try (Connection con = ConexionDB.obtenerConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            int opId = rs.getInt("id");
            Importacion imp = new Importacion();
            imp.setId(opId);
            imp.setUsuarioId(rs.getInt("usuario_id"));
            imp.setProductoDesc(rs.getString("producto_desc"));
            imp.setHsCode(rs.getString("hs_code"));
            imp.setIncoterm(rs.getString("incoterm"));
            imp.setValorFob(rs.getDouble("fob"));
            imp.setFlete(rs.getDouble("flete"));
            imp.setSeguro(rs.getDouble("seguro"));
            imp.setValorCif(rs.getDouble("cif"));
            imp.setTotalImpuestos(rs.getDouble("total_impuestos"));
            imp.setEstado(rs.getString("estado"));
            imp.setCanalAsignado(rs.getString("canal_asignado"));
            imp.setNumeroDam(rs.getString("numero_dam"));
            imp.setFechaNumeracion(rs.getTimestamp("fecha_numeracion"));
            imp.setFechaCreacion(rs.getTimestamp("fecha_creacion"));

            // Consultar régimen y modalidad de dam_cabecera
            String regimenCodigo = "10";
            String modalidadCodigo = "ANTICIPADO";
            String sqlDam = "SELECT regimen_codigo, modalidad_codigo FROM dam_cabecera WHERE operacion_id = ? LIMIT 1";
            try (PreparedStatement psDam = con.prepareStatement(sqlDam)) {
                psDam.setInt(1, opId);
                try (ResultSet rsDam = psDam.executeQuery()) {
                    if (rsDam.next()) {
                        regimenCodigo = rsDam.getString("regimen_codigo");
                        modalidadCodigo = rsDam.getString("modalidad_codigo");
                    }
                }
            } catch (Exception e) {
                // Usar fallback predeterminado
            }

            // Calcular plazos reales con la base de datos
            List<Map<String, Object>> plazos = PlazoCriticoService.calcularPlazos(con, imp, regimenCodigo, modalidadCodigo);
            
            // Determinar la máxima criticidad
            long minDays = Long.MAX_VALUE;
            String overallStatus = "OK"; // OK, WARNING, CRITICAL, EXPIRED, PENDIENTE
            String plazoLabel = "En plazo";

            if (plazos != null && !plazos.isEmpty()) {
                for (Map<String, Object> plazo : plazos) {
                    Boolean isReg = (Boolean) plazo.get("isRegistered");
                    if (isReg != null && isReg) {
                        Long days = (Long) plazo.get("daysRemaining");
                        if (days != null && days < minDays) {
                            minDays = days;
                            overallStatus = (String) plazo.get("status");
                            plazoLabel = (String) plazo.get("label");
                        }
                    } else {
                        // Si está pendiente de dato real
                        if ("OK".equals(overallStatus)) {
                            overallStatus = "PENDIENTE";
                        }
                    }
                }
            }

            // Crear mapa de la tarjeta Kanban
            Map<String, Object> card = new HashMap<>();
            card.put("op", imp);
            card.put("ruc", rs.getString("ruc"));
            card.put("razon_social", rs.getString("razon_social"));
            card.put("regimen", regimenCodigo);
            card.put("modalidad", modalidadCodigo);
            card.put("daysRemaining", minDays == Long.MAX_VALUE ? null : minDays);
            card.put("status", overallStatus);
            card.put("plazoLabel", plazoLabel);

            // Clasificar en la columna correspondiente
            if ("CRITICAL".equals(overallStatus) || "EXPIRED".equals(overallStatus)) {
                colCritico.add(card);
            } else if ("WARNING".equals(overallStatus)) {
                colWarning.add(card);
            } else {
                colEnPlazo.add(card);
            }
        }
    } catch (Exception e) {
        LoggerUtil.error("Error cargando Mesa de Control Kanban: " + e.getMessage(), e);
    }
%>

<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <base href="<%= request.getContextPath() %>/">
    <title>ImportEase - Mesa de Control Aduanero</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Outfit', sans-serif;
            background-color: #0A0E17;
            color: #E2E8F0;
        }
        .mono {
            font-family: 'JetBrains Mono', monospace;
        }
        .glass-panel {
            background: rgba(26, 36, 57, 0.4);
            backdrop-filter: blur(16px);
            -webkit-backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 255, 255, 0.05);
        }
        .glass-card {
            background: rgba(30, 41, 67, 0.6);
            backdrop-filter: blur(8px);
            -webkit-backdrop-filter: blur(8px);
            border: 1px solid rgba(255, 255, 255, 0.08);
            transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        }
        .glass-card:hover {
            transform: translateY(-4px);
            border-color: rgba(23, 207, 194, 0.3);
            box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5), 0 0 15px rgba(23, 207, 194, 0.1);
        }
        .glow-red {
            box-shadow: 0 0 10px rgba(239, 68, 68, 0.3);
            animation: pulse-red 2s infinite;
        }
        .glow-yellow {
            box-shadow: 0 0 10px rgba(245, 158, 11, 0.3);
            animation: pulse-yellow 2s infinite;
        }
        @keyframes pulse-red {
            0%, 100% { box-shadow: 0 0 8px rgba(239, 68, 68, 0.2); }
            50% { box-shadow: 0 0 16px rgba(239, 68, 68, 0.5); }
        }
        @keyframes pulse-yellow {
            0%, 100% { box-shadow: 0 0 8px rgba(245, 158, 11, 0.2); }
            50% { box-shadow: 0 0 16px rgba(245, 158, 11, 0.5); }
        }
        /* Custom scrollbar */
        ::-webkit-scrollbar {
            width: 6px;
            height: 6px;
        }
        ::-webkit-scrollbar-track {
            background: rgba(10, 14, 23, 0.8);
        }
        ::-webkit-scrollbar-thumb {
            background: rgba(255, 255, 255, 0.1);
            border-radius: 3px;
        }
        ::-webkit-scrollbar-thumb:hover {
            background: rgba(255, 255, 255, 0.2);
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden">
    <% request.setAttribute("activePage", "dashboard"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-hidden">
        <!-- Header Cockpit -->
        <header class="h-20 border-b border-white/5 px-10 flex items-center justify-between bg-[#0A0E17]/80 backdrop-blur-xl z-20">
            <div class="flex items-center gap-4">
                <div class="px-3 py-1.5 bg-[#17CFC2]/10 rounded-full flex items-center gap-2.5 border border-[#17CFC2]/20">
                    <span class="w-2.5 h-2.5 rounded-full bg-[#17CFC2] animate-pulse"></span>
                    <span class="text-[11px] font-black text-[#17CFC2] uppercase tracking-widest">Mesa de Control Real-Time</span>
                </div>
                <h1 class="text-xl font-bold tracking-tight">Consola de Criticidad y SLA de Operaciones</h1>
            </div>
            <div class="flex items-center gap-4">
                <a href="dashboard.jsp" class="px-5 py-2.5 rounded-xl text-xs font-bold uppercase tracking-widest text-[#94A3B8] border border-white/10 hover:bg-white/5 transition-all">Regresar</a>
            </div>
        </header>

        <!-- Kanban Cockpit Area -->
        <div class="flex-1 overflow-x-auto p-10 flex gap-8 items-start bg-[#080B12]">
            
            <!-- COLUMN 1: CRÍTICO / VENCE HOY -->
            <div class="w-[420px] flex-shrink-0 flex flex-col max-h-full glass-panel rounded-2xl p-5">
                <div class="flex items-center justify-between border-b border-white/5 pb-4 mb-4">
                    <div class="flex items-center gap-3">
                        <span class="w-3 h-3 rounded-full bg-red-500 glow-red"></span>
                        <h2 class="text-sm font-bold uppercase tracking-wider text-red-400">Crítico / Vence Hoy</h2>
                    </div>
                    <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-red-500/10 text-red-400"><%= colCritico.size() %></span>
                </div>

                <div class="flex-1 overflow-y-auto space-y-4 pr-1">
                    <% if (colCritico.isEmpty()) { %>
                        <div class="text-center py-10 border border-dashed border-white/5 rounded-xl text-xs text-slate-500 font-semibold">
                            Ningún expediente en estado crítico.
                        </div>
                    <% } else { %>
                        <% for (Map<String, Object> card : colCritico) {
                            Importacion o = (Importacion) card.get("op");
                            Long days = (Long) card.get("daysRemaining");
                            String status = (String) card.get("status");
                        %>
                            <a href="expediente-aduanero.jsp?id=<%= o.getId() %>" class="block glass-card rounded-xl p-5 space-y-4">
                                <div class="flex justify-between items-start">
                                    <span class="mono text-xs font-bold text-[#17CFC2] bg-[#17CFC2]/10 px-2.5 py-0.5 rounded-full">OP-<%= o.getId() %></span>
                                    <span class="text-[10px] font-black uppercase px-2.5 py-0.5 rounded bg-red-500/10 text-red-400 border border-red-500/20">
                                        <%= "EXPIRED".equals(status) ? "PLAZO VENCIDO" : "CRÍTICO" %>
                                    </span>
                                </div>

                                <div class="space-y-1">
                                    <h3 class="text-sm font-bold text-white line-clamp-1"><%= HtmlUtil.escape(o.getProductoDesc()) %></h3>
                                    <p class="text-[10px] text-slate-400 font-semibold uppercase"><%= HtmlUtil.escape((String) card.get("razon_social")) %> (RUC: <span class="mono"><%= HtmlUtil.escape((String) card.get("ruc")) %></span>)</p>
                                </div>

                                <div class="grid grid-cols-2 gap-3 border-t border-white/5 pt-3 text-xs font-semibold">
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Régimen</p>
                                        <p class="text-slate-300 mt-0.5"><%= card.get("regimen") %></p>
                                    </div>
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Canal Probable</p>
                                        <p class="mt-0.5 <%= "ROJO".equals(o.getCanalAsignado()) ? "text-red-400" : "text-emerald-400" %>"><%= o.getCanalAsignado() %></p>
                                    </div>
                                </div>

                                <div class="border-t border-white/5 pt-3 flex items-center justify-between text-xs font-bold">
                                    <span class="text-slate-400 text-[10px]"><%= card.get("plazoLabel") %></span>
                                    <span class="text-red-400 mono">
                                        <%= days != null ? (days < 0 ? "Vencido hace " + Math.abs(days) + "d" : "Falta 1 día") : "Pendiente de dato" %>
                                    </span>
                                </div>
                            </a>
                        <% } %>
                    <% } %>
                </div>
            </div>

            <!-- COLUMN 2: PRÓXIMOS (48 HORAS / WARNING) -->
            <div class="w-[420px] flex-shrink-0 flex flex-col max-h-full glass-panel rounded-2xl p-5">
                <div class="flex items-center justify-between border-b border-white/5 pb-4 mb-4">
                    <div class="flex items-center gap-3">
                        <span class="w-3 h-3 rounded-full bg-amber-500 glow-yellow"></span>
                        <h2 class="text-sm font-bold uppercase tracking-wider text-amber-400">Advertencia / Próximos</h2>
                    </div>
                    <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-amber-500/10 text-amber-400"><%= colWarning.size() %></span>
                </div>

                <div class="flex-1 overflow-y-auto space-y-4 pr-1">
                    <% if (colWarning.isEmpty()) { %>
                        <div class="text-center py-10 border border-dashed border-white/5 rounded-xl text-xs text-slate-500 font-semibold">
                            Ningún expediente en advertencia.
                        </div>
                    <% } else { %>
                        <% for (Map<String, Object> card : colWarning) {
                            Importacion o = (Importacion) card.get("op");
                            Long days = (Long) card.get("daysRemaining");
                        %>
                            <a href="expediente-aduanero.jsp?id=<%= o.getId() %>" class="block glass-card rounded-xl p-5 space-y-4">
                                <div class="flex justify-between items-start">
                                    <span class="mono text-xs font-bold text-[#17CFC2] bg-[#17CFC2]/10 px-2.5 py-0.5 rounded-full">OP-<%= o.getId() %></span>
                                    <span class="text-[10px] font-black uppercase px-2.5 py-0.5 rounded bg-amber-500/10 text-amber-400 border border-amber-500/20">WARNING</span>
                                </div>

                                <div class="space-y-1">
                                    <h3 class="text-sm font-bold text-white line-clamp-1"><%= HtmlUtil.escape(o.getProductoDesc()) %></h3>
                                    <p class="text-[10px] text-slate-400 font-semibold uppercase"><%= HtmlUtil.escape((String) card.get("razon_social")) %> (RUC: <span class="mono"><%= HtmlUtil.escape((String) card.get("ruc")) %></span>)</p>
                                </div>

                                <div class="grid grid-cols-2 gap-3 border-t border-white/5 pt-3 text-xs font-semibold">
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Régimen</p>
                                        <p class="text-slate-300 mt-0.5"><%= card.get("regimen") %></p>
                                    </div>
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Canal Probable</p>
                                        <p class="mt-0.5 <%= "ROJO".equals(o.getCanalAsignado()) ? "text-red-400" : "text-emerald-400" %>"><%= o.getCanalAsignado() %></p>
                                    </div>
                                </div>

                                <div class="border-t border-white/5 pt-3 flex items-center justify-between text-xs font-bold">
                                    <span class="text-slate-400 text-[10px]"><%= card.get("plazoLabel") %></span>
                                    <span class="text-amber-400 mono">Faltan <%= days %> días</span>
                                </div>
                            </a>
                        <% } %>
                    <% } %>
                </div>
            </div>

            <!-- COLUMN 3: EN PLAZO / OK -->
            <div class="w-[420px] flex-shrink-0 flex flex-col max-h-full glass-panel rounded-2xl p-5">
                <div class="flex items-center justify-between border-b border-white/5 pb-4 mb-4">
                    <div class="flex items-center gap-3">
                        <span class="w-3 h-3 rounded-full bg-emerald-500"></span>
                        <h2 class="text-sm font-bold uppercase tracking-wider text-emerald-400">En Plazo / Procesando</h2>
                    </div>
                    <span class="px-2.5 py-0.5 rounded-full text-xs font-bold bg-emerald-500/10 text-emerald-400"><%= colEnPlazo.size() %></span>
                </div>

                <div class="flex-1 overflow-y-auto space-y-4 pr-1">
                    <% if (colEnPlazo.isEmpty()) { %>
                        <div class="text-center py-10 border border-dashed border-white/5 rounded-xl text-xs text-slate-500 font-semibold">
                            Ninguna operación en plazo.
                        </div>
                    <% } else { %>
                        <% for (Map<String, Object> card : colEnPlazo) {
                            Importacion o = (Importacion) card.get("op");
                            Long days = (Long) card.get("daysRemaining");
                            String status = (String) card.get("status");
                        %>
                            <a href="expediente-aduanero.jsp?id=<%= o.getId() %>" class="block glass-card rounded-xl p-5 space-y-4">
                                <div class="flex justify-between items-start">
                                    <span class="mono text-xs font-bold text-[#17CFC2] bg-[#17CFC2]/10 px-2.5 py-0.5 rounded-full">OP-<%= o.getId() %></span>
                                    <% if ("PENDIENTE".equals(status)) { %>
                                        <span class="text-[10px] font-black uppercase px-2.5 py-0.5 rounded bg-slate-500/10 text-slate-400 border border-white/5">PENDIENTE FECHA</span>
                                    <% } else { %>
                                        <span class="text-[10px] font-black uppercase px-2.5 py-0.5 rounded bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">OK</span>
                                    <% } %>
                                </div>

                                <div class="space-y-1">
                                    <h3 class="text-sm font-bold text-white line-clamp-1"><%= HtmlUtil.escape(o.getProductoDesc()) %></h3>
                                    <p class="text-[10px] text-slate-400 font-semibold uppercase"><%= HtmlUtil.escape((String) card.get("razon_social")) %> (RUC: <span class="mono"><%= HtmlUtil.escape((String) card.get("ruc")) %></span>)</p>
                                </div>

                                <div class="grid grid-cols-2 gap-3 border-t border-white/5 pt-3 text-xs font-semibold">
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Régimen</p>
                                        <p class="text-slate-300 mt-0.5"><%= card.get("regimen") %></p>
                                    </div>
                                    <div>
                                        <p class="text-[9px] uppercase font-black text-slate-500">Canal Probable</p>
                                        <p class="mt-0.5 <%= "ROJO".equals(o.getCanalAsignado()) ? "text-red-400" : "text-emerald-400" %>"><%= o.getCanalAsignado() %></p>
                                    </div>
                                </div>

                                <div class="border-t border-white/5 pt-3 flex items-center justify-between text-xs font-bold">
                                    <span class="text-slate-400 text-[10px]"><%= card.get("plazoLabel") %></span>
                                    <span class="text-emerald-400 mono">
                                        <%= days != null ? "Faltan " + days + " días" : "Sin fecha aduanera" %>
                                    </span>
                                </div>
                            </a>
                        <% } %>
                    <% } %>
                </div>
            </div>

        </div>
    </main>
</body>
</html>
