<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.util.*" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>
<%!
    private long countSql(String sql) {
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    private List<Map<String, String>> rowsSql(String sql, int limit) {
        List<Map<String, String>> rows = new ArrayList<>();
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        Object value = rs.getObject(i);
                        row.put(meta.getColumnLabel(i), value == null ? "" : String.valueOf(value));
                    }
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("estado", "Sin datos o migracion pendiente");
            rows.add(row);
        }
        return rows;
    }
%>
<%
    // Restringir acceso solo a usuarios logueados y administradores
    Integer uIdAttr = (Integer) session.getAttribute("usuarioId");
    if (uIdAttr == null) {
        response.sendRedirect("../login.jsp");
        return;
    }
    com.importease.proyecto.repository.UsuarioRepositorio uDao = new com.importease.proyecto.repository.UsuarioRepositorio();
    com.importease.proyecto.model.Usuario currentUser = uDao.buscarPorId(uIdAttr);
    if (currentUser == null || !"admin".equalsIgnoreCase(currentUser.getPerfil())) {
        response.sendRedirect("../dashboard.jsp");
        return;
    }

    long fuentesOk = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE resultado IN ('OK','CACHE')");
    long fuentesError = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE resultado IN ('ERROR','TIMEOUT')");
    long fallbacks = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE resultado = 'FALLBACK'");
    long simulados = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE resultado = 'SIMULADO'");
    long apiOficial = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE tipo_fuente = 'API_OFICIAL'");
    long webScraping = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE tipo_fuente = 'WEB_SCRAPING'");
    long webTracking = countSql("SELECT COUNT(*) FROM fuente_eventos WHERE tipo_fuente = 'WEB_TRACKING'");
    long busquedasSinResultado = countSql("SELECT COUNT(*) FROM log_busquedas WHERE resultado_hs_code IS NULL OR resultado_hs_code = ''");
    long datosSinFuente = countSql("SELECT COUNT(*) FROM tipo_cambio_diario WHERE fuente_dato IS NULL OR fuente_dato = ''");

    List<Map<String, String>> ultimosErrores = rowsSql(
        "SELECT fuente, COALESCE(tipo_fuente,'BD_LOCAL') tipo_fuente, tipo_evento, resultado, COALESCE(source_type,'BD_LOCAL') source_type, COALESCE(status_http, 0) status_http, LEFT(COALESCE(mensaje_error,''), 120) mensaje, fecha_evento FROM fuente_eventos WHERE resultado IN ('ERROR','TIMEOUT','FALLBACK') ORDER BY fecha_evento DESC LIMIT ?",
        8
    );
    List<Map<String, String>> topBusquedas = rowsSql(
        "SELECT termino, COUNT(*) total, COALESCE(MAX(resultado_hs_code),'') hs FROM log_busquedas GROUP BY termino ORDER BY total DESC LIMIT ?",
        8
    );
    List<Map<String, String>> tipoCambio = rowsSql(
        "SELECT fecha, venta, COALESCE(fuente_dato,'BD_LOCAL') fuente_dato, COALESCE(confianza,0) confianza, COALESCE(es_fallback,0) es_fallback FROM tipo_cambio_diario ORDER BY fecha DESC LIMIT ?",
        5
    );
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <base href="<%= request.getContextPath() %>/">
    <title>ImportEase - Calidad de datos</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
</head>
<body class="flex h-screen overflow-hidden bg-[#FAF6F0] bg-grid font-['Outfit'] text-[#1F2937]">
    <% request.setAttribute("activePage", "dashboard"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar">
        <header class="h-16 border-b border-[#E6E2D8] px-10 flex items-center justify-between bg-white/70 backdrop-blur-xl sticky top-0 z-10">
            <div class="px-4 py-1.5 bg-[#0A5C4A]/5 rounded-full flex items-center gap-3 border border-[#0A5C4A]/10">
                <span class="w-2 h-2 rounded-full bg-[#0A5C4A]"></span>
                <span class="text-[11px] font-black text-[#0A5C4A] uppercase tracking-widest">Panel QA de datos</span>
            </div>
            <a href="dashboard.jsp" class="px-5 py-2.5 rounded-2xl text-xs font-black uppercase tracking-widest text-white bg-gradient-to-r from-[#17cfc2] to-[#0ea89e]">Volver</a>
        </header>

        <section class="p-10 max-w-7xl mx-auto space-y-8">
            <div class="glass-card p-8">
                <p class="pill-heading">Principio de confianza</p>
                <h1 class="text-4xl font-black tracking-tight mt-3">ImportEase no oculta incertidumbre.</h1>
                <p class="text-sm text-gray-500 font-semibold mt-3 max-w-3xl">Este panel resume APIs oficiales, web scraping controlado, web tracking, errores, fallbacks, simulaciones y datos sin fuente para defender la calidad del sistema en QA.</p>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-6 gap-4">
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Fuentes OK/cache</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= fuentesOk %></p>
                    <span class="source-chip source-chip--official mt-3">OK</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Errores fuente</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= fuentesError %></p>
                    <span class="source-chip source-chip--fallback mt-3">ERROR</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Fallbacks</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= fallbacks %></p>
                    <span class="source-chip source-chip--fallback mt-3">FALLBACK</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Simulados</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= simulados %></p>
                    <span class="source-chip source-chip--simulated mt-3">SIMULADO</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Busquedas sin HS</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= busquedasSinResultado %></p>
                    <span class="source-chip source-chip--estimated mt-3">REVISAR</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Datos sin fuente</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= datosSinFuente %></p>
                    <span class="source-chip source-chip--pending mt-3">PENDIENTE</span>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">API oficial</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= apiOficial %></p>
                    <span class="source-chip source-chip--official mt-3">BCRP / COMTRADE</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Web scraping controlado</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= webScraping %></p>
                    <span class="source-chip source-chip--cache mt-3">SUNAT / VUCE</span>
                </div>
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-5">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-500">Web tracking</p>
                    <p class="text-3xl font-black text-[#0A5C4A] mt-2"><%= webTracking %></p>
                    <span class="source-chip source-chip--pending mt-3">LOGISTICO</span>
                </div>
            </div>

            <div class="grid xl:grid-cols-2 gap-6">
                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-6">
                    <h2 class="text-lg font-black">Ultimos errores externos</h2>
                    <div class="mt-5 space-y-3">
                        <% for (Map<String, String> row : ultimosErrores) { %>
                            <div class="border border-[#E6E2D8] rounded-xl p-4 text-xs font-semibold">
                                <% for (Map.Entry<String, String> entry : row.entrySet()) { %>
                                    <p><span class="text-gray-500 uppercase text-[10px] font-black"><%= HtmlUtil.escape(entry.getKey()) %>:</span> <%= HtmlUtil.escape(entry.getValue()) %></p>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                </div>

                <div class="bg-white border border-[#E6E2D8] rounded-2xl p-6">
                    <h2 class="text-lg font-black">Top busquedas internas</h2>
                    <div class="mt-5 space-y-3">
                        <% for (Map<String, String> row : topBusquedas) { %>
                            <div class="flex items-center justify-between gap-4 border border-[#E6E2D8] rounded-xl p-4">
                                <div>
                                    <p class="text-sm font-black"><%= HtmlUtil.escape(row.getOrDefault("termino", "")) %></p>
                                    <p class="text-[10px] font-bold text-gray-500">HS: <%= HtmlUtil.escape(row.getOrDefault("hs", "sin resultado")) %></p>
                                </div>
                                <span class="source-chip source-chip--bd"><%= HtmlUtil.escape(row.getOrDefault("total", "0")) %> eventos</span>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>

            <div class="bg-white border border-[#E6E2D8] rounded-2xl p-6">
                <h2 class="text-lg font-black">Tipo de cambio y trazabilidad</h2>
                <div class="mt-5 grid md:grid-cols-2 xl:grid-cols-5 gap-3">
                    <% for (Map<String, String> row : tipoCambio) { %>
                        <div class="border border-[#E6E2D8] rounded-xl p-4">
                            <p class="text-[10px] font-black uppercase tracking-widest text-gray-500"><%= HtmlUtil.escape(row.getOrDefault("fecha", "")) %></p>
                            <p class="text-2xl font-black text-[#0A5C4A] mt-1"><%= HtmlUtil.escape(row.getOrDefault("venta", "")) %></p>
                            <span class="source-chip source-chip--bd mt-3"><%= HtmlUtil.escape(row.getOrDefault("fuente_dato", "BD_LOCAL")) %></span>
                            <p class="text-[10px] text-gray-500 font-bold mt-2">Confianza: <%= HtmlUtil.escape(row.getOrDefault("confianza", "0")) %></p>
                        </div>
                    <% } %>
                </div>
            </div>
        </section>
    </main>
</body>
</html>
