<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List" %>
<%@ page import="com.importease.proyecto.dto.PermisoDTO" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>
<%!
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("</", "<\\/");
    }
%>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String userNombre = (String) session.getAttribute("usuarioNombre");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Permisos necesarios - ImportEase</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/permisos.css" rel="stylesheet">
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/toast.js"></script>
</head>
<body class="permisos-page flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "permisos"); %>
    <jsp:include page="/fragments/sidebar.jsp" />
        
    <main class="flex-1 overflow-y-auto custom-scrollbar">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Permisos antes de importar</span>
            </div>
        </header>

        <!-- Main Content Area -->
        <div class="p-12 max-w-7xl mx-auto space-y-8">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">&larr; Volver a Importar paso a paso</a>

            <!-- Page Title -->
            <div class="fade-in">
                <h1 class="text-4xl font-black text-[var(--text-primary)] tracking-tight">Permisos necesarios</h1>
                <p class="text-[var(--text-secondary)] text-sm font-semibold mt-1">Aqui veras si alguna importacion necesita autorizacion antes de ingresar al pais.</p>
            </div>

            <!-- Filters tabs matching image 9 -->
            <div id="filtersContainer" class="flex flex-wrap gap-2 fade-in delay-50">
                <button data-filter="Pendientes" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-[var(--accent)] text-white transition-all permit-pill active" id="permit-pill-Pendientes">Pendientes</button>
                <button data-filter="En trámite" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Tramite">En trámite</button>
                <button data-filter="Aprobados" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Aprobados">Aprobados</button>
                <button data-filter="Rechazados" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Rechazados">Rechazados</button>
            </div>

            <!-- Dynamic Permits Data Table -->
            <div class="glass-card overflow-hidden fade-in delay-100">
                <table class="w-full text-left border-collapse">
                    <thead class="bg-[var(--surface-2)] text-[var(--text-secondary)] text-[10px] uppercase font-black tracking-widest border-b border-[var(--border)]">
                        <tr>
                            <th class="px-8 py-5">Producto</th>
                            <th class="px-8 py-5">Quien revisa</th>
                            <th class="px-8 py-5">Documento posible</th>
                            <th class="px-8 py-5">Estado</th>
                            <th class="px-8 py-5">Última actualización</th>
                            <th class="px-8 py-5 text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[var(--border)] text-[var(--text-secondary)] font-semibold text-xs" id="permitsTableBody">
                        <%
                            Integer usuarioId = (Integer) session.getAttribute("usuarioId");
                            com.importease.proyecto.service.ImportacionServicio importService = new com.importease.proyecto.service.ImportacionServicio();
                            List<PermisoDTO> lista = importService.obtenerPermisos(usuarioId);

                            int countPermisos = 0;
                            if (lista != null && !lista.isEmpty()) {
                                for (PermisoDTO dto : lista) {
                                    countPermisos++;
                        %>
                        <tr class="hover:bg-[var(--surface-2)]/50 transition-all permit-row-item border-b border-[var(--border)]" data-state-label="<%= dto.getStateLabel() %>" data-raw-state="<%= dto.getRawState() %>">
                            <td class="px-8 py-5">
                                <span class="font-bold text-[var(--text-primary)] block truncate max-w-[200px]"><%= HtmlUtil.escape(dto.getProductoDesc()) %></span>
                                <span class="text-[8px] text-[var(--text-tertiary)] font-bold uppercase block mt-0.5">OP-<%= String.format("%05d", dto.getId()) %></span>
                            </td>
                            <td class="px-8 py-5 font-black text-[var(--text-tertiary)] uppercase">
                                <%= dto.getEntidad() %>
                            </td>
                            <td class="px-8 py-5 font-semibold text-[var(--text-secondary)]">
                                <%= dto.getPermitName() %>
                            </td>
                            <td class="px-8 py-5">
                                <span class="px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase border <%= dto.getStateClass() %>"><%= dto.getStateLabel() %></span>
                            </td>
                            <td class="px-8 py-5 text-[var(--text-secondary)] font-semibold">
                                <%= dto.getFecha() %>
                            </td>
                            <td class="px-8 py-5 text-center">
                                <button data-op-id="<%= dto.getId() %>" data-action="select-eval" class="<%= dto.getActionClass() %>">
                                    <%= dto.getActionLabel() %>
                                </button>
                            </td>
                        </tr>
                        <%
                                }
                            }
                            
                            if (countPermisos == 0) {
                        %>
                        <tr>
                            <td colspan="6" class="px-8 py-24 text-center">
                                <div class="flex flex-col items-center justify-center opacity-60 gap-4">
                                    <svg class="w-16 h-16 text-[var(--text-tertiary)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                                    </svg>
                                    <p class="text-base font-bold italic text-[var(--text-secondary)]">No hay importaciones con permiso pendiente.</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>

            <!-- Step 1: Seleccionar Operación (Para Detalles VUCE) -->
            <div class="mb-8 fade-in border-t border-[var(--border)] pt-8 delay-150">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">1</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Elige una importacion para revisar permisos</h2>
                    </div>
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
                        <div class="lg:col-span-2">
                            <select id="selectOperacion" class="w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3.5 text-xs text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all cursor-pointer font-semibold">
                                <option value="">Selecciona una importacion registrada</option>
                            </select>
                        </div>
                        <button id="btnEvaluar" disabled class="primary-button text-xs flex items-center justify-center gap-2">
                            Revisar requisitos
                        </button>
                    </div>
                    
                    <!-- Selected operation details -->
                    <div id="opDetails" class="hidden mt-5 bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)]">
                        <div class="grid grid-cols-2 lg:grid-cols-4 gap-4 text-xs font-semibold">
                            <div><span class="text-[var(--text-tertiary)] block mb-1">Producto</span><span id="opProducto" class="text-[var(--text-primary)] font-bold">—</span></div>
                            <div><span class="text-[var(--text-tertiary)] block mb-1">Código aduanero</span><span id="opHsCode" class="text-[var(--accent)] font-mono font-bold">—</span></div>
                            <div><span class="text-[var(--text-tertiary)] block mb-1">País Origen</span><span id="opPais" class="text-[var(--text-primary)] font-bold">—</span></div>
                            <div><span class="text-[var(--text-tertiary)] block mb-1">Valor CIF</span><span id="opCif" class="text-[var(--success)] font-bold">—</span></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Loading state -->
            <div id="loadingState" class="hidden mb-8">
                <div class="glass-card p-8 text-center">
                    <div class="w-16 h-16 mx-auto mb-4 rounded-2xl bg-blue-50 flex items-center justify-center">
                        <svg class="w-8 h-8 text-[var(--accent)] animate-spin" fill="none" viewBox="0 0 24 24"><circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/><path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/></svg>
                    </div>
                    <h3 class="text-[var(--text-primary)] font-bold mb-1">Revisando permisos...</h3>
                    <p class="text-[var(--text-secondary)] text-xs font-semibold">Comparando el codigo del producto con reglas de entidades peruanas.</p>
                </div>
            </div>

            <!-- Step 2: Resultado de Evaluación (Semáforo) -->
            <div id="resultadoSection" class="hidden mb-8 fade-in delay-150">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">2</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Resultado de permisos</h2>
                    </div>
                    
                    <!-- Traffic light indicator -->
                    <div id="semaforoBox" class="rounded-xl p-5 mb-6 border">
                        <div class="flex items-center gap-4">
                            <div id="semaforoIcon" class="w-14 h-14 rounded-2xl flex items-center justify-center shrink-0"></div>
                            <div>
                                <h3 id="semaforoTitle" class="text-base font-black text-[var(--text-primary)]"></h3>
                                <p id="semaforoDesc" class="text-xs text-[var(--text-secondary)] font-semibold mt-1"></p>
                            </div>
                        </div>
                    </div>

                    <!-- Restricciones detectadas -->
                    <div id="restriccionesContainer" class="space-y-3"></div>
                </div>
            </div>

            <!-- Step 3: Cuestionario Técnico -->
            <div id="cuestionarioSection" class="hidden mb-8 fade-in delay-200">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">3</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Preguntas de confirmacion</h2>
                        <span id="cuestionarioEntidad" class="ml-auto text-[10px] font-black px-3 py-1 rounded-lg bg-blue-50 text-[var(--accent)] border border-[var(--accent-glow)] uppercase tracking-widest"></span>
                    </div>
                    <div id="preguntasContainer" class="space-y-4"></div>
                    <div class="mt-6 flex justify-end">
                        <button id="btnSubmitCuestionario" class="primary-button text-xs flex items-center gap-2">
                            Confirmar respuestas
                        </button>
                    </div>
                </div>
            </div>

            <!-- Step 4: Expediente Digital -->
            <div id="expedienteSection" class="hidden mb-8 fade-in delay-250">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">4</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Carpeta de documentos del permiso</h2>
                    </div>
                    
                    <!-- Actions bar -->
                    <div class="flex flex-wrap gap-3 mb-6">
                        <button id="btnAutorrellenar" class="primary-button text-xs flex items-center gap-2">
                            Autorrellenar Expediente
                        </button>
                        <button id="btnPdf" disabled class="soft-button text-xs flex items-center gap-2 disabled:opacity-30 disabled:cursor-not-allowed">
                            Descargar Solicitud PDF
                        </button>
                    </div>

                    <!-- Auto-filled data preview -->
                    <div id="datosExpediente" class="hidden">
                        <h4 class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest mb-3">Datos Autocompletados</h4>
                        <div id="datosGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3"></div>
                    </div>
                </div>
            </div>

            <!-- Step 5: Checklist Documental -->
            <div id="checklistSection" class="hidden mb-8 fade-in delay-300">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">5</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Checklist Documental</h2>
                        <span id="checklistProgress" class="ml-auto text-[10px] font-black px-3 py-1 rounded-lg bg-[var(--surface-2)] text-[var(--text-secondary)]"></span>
                    </div>
                    <div id="checklistContainer" class="space-y-2"></div>
                </div>
            </div>

            <!-- Step 6: Registro de tramite -->
            <div id="suceSection" class="hidden mb-8 fade-in delay-350">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">6</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Registro del tramite</h2>
                    </div>
                    <p class="text-xs text-[var(--text-secondary)] font-semibold mb-5">Registra el número de trámite SUCE obtenido en la Ventanilla Única de Comercio Exterior para vincular tu expediente con la operación oficial.</p>
                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-5 font-semibold text-xs">
                        <div>
                            <label class="block text-[9px] text-[var(--text-tertiary)] font-bold mb-2 uppercase tracking-widest">Número SUCE (Código del trámite registrado en VUCE)</label>
                            <input type="text" id="inputSuce" placeholder="Ej: SUCE-2026-00001" class="custom-input w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3 text-sm text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all placeholder-gray-400">
                        </div>
                        <div>
                            <label class="block text-[9px] text-[var(--text-tertiary)] font-bold mb-2 uppercase tracking-widest">Resolución Directoral (si aprobado)</label>
                            <input type="text" id="inputResolucion" placeholder="Ej: RD-2026-000123-MTC" class="custom-input w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3 text-sm text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all placeholder-gray-400">
                        </div>
                    </div>
                    <button id="btnRegistrarSuce" class="primary-button text-xs">
                        Registrar tramite
                    </button>
                </div>
            </div>

            <!-- Historial de solicitudes -->
            <div id="historialSection" class="hidden mb-8 fade-in delay-100">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--surface-2)] flex items-center justify-center">
                            <svg class="w-6 h-6 text-[var(--accent)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
                            </svg>
                        </div>
                        <div>
                            <h2 class="text-lg font-bold text-[var(--text-primary)]">Historial de Solicitudes</h2>
                            <p class="text-xs text-[var(--text-secondary)] font-semibold">Solicitudes gestionadas en tus operaciones</p>
                        </div>
                    </div>
                    <div id="historialContainer">
                        <div class="text-center py-8 text-[var(--text-tertiary)] text-sm font-semibold">Selecciona una operación para ver su historial de permisos</div>
                    </div>
                </div>
            </div>

        </div>
    </main>

    <!-- Global state scripts -->
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
      window.ImportEase = window.ImportEase || {};
      window.ImportEase.ctx = '<%= escapeJs(request.getContextPath()) %>';
      window.ImportEase.csrfToken = '<%= escapeJs(request.getAttribute("csrfToken") != null ? String.valueOf(request.getAttribute("csrfToken")) : "") %>';
      window.ImportEase.csrfHeader = '<%= escapeJs(request.getAttribute("csrfHeader") != null ? String.valueOf(request.getAttribute("csrfHeader")) : "X-CSRF-TOKEN") %>';

      // Compatibilidad legacy
      window.ctx = window.ImportEase.ctx;
      window.csrfToken = window.ImportEase.csrfToken;
    </script>

    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/permisos.js" defer></script>
</body>
</html>

