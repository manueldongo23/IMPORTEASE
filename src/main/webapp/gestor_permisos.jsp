<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List" %>
<%@ page import="com.importease.proyecto.model.Importacion" %>
<%@ page import="com.importease.proyecto.repository.ImportacionDAO" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%@ page import="java.sql.Connection" %>
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
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <style>
        .check-item input[type="checkbox"]:checked + span { text-decoration: line-through; opacity: 0.5; }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
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
            <div class="flex flex-wrap gap-2 fade-in" style="animation-delay: 0.05s">
                <button onclick="filterPermits('Pendientes')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-[var(--accent)] text-white transition-all permit-pill active" id="permit-pill-Pendientes">Pendientes</button>
                <button onclick="filterPermits('En trámite')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Tramite">En trámite</button>
                <button onclick="filterPermits('Aprobados')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Aprobados">Aprobados</button>
                <button onclick="filterPermits('Rechazados')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill" id="permit-pill-Rechazados">Rechazados</button>
            </div>

            <!-- Dynamic Permits Data Table -->
            <div class="glass-card overflow-hidden fade-in" style="animation-delay: 0.1s">
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
                            com.importease.proyecto.service.ImportacionService importService = new com.importease.proyecto.service.ImportacionService();
                            List<Importacion> lista = importService.listarPorUsuario(usuarioId);

                            int countPermisos = 0;
                            if (lista != null && !lista.isEmpty()) {
                                for (Importacion imp : lista) {
                                    String code = imp.getHsCode();
                                    
                                    // Filter out non-permitted items
                                    String entity = "--";
                                    String permitName = "Licencia Especial";
                                    if (code != null) {
                                        if (code.startsWith("8517")) { entity = "MTC"; permitName = "Homologación"; }
                                        else if (code.startsWith("2106") || code.startsWith("1901")) { entity = "DIGESA"; permitName = "Registro sanitario"; }
                                        else if (code.startsWith("3004") || code.startsWith("3304") || code.startsWith("9018")) { entity = "DIGEMID"; permitName = "Registro sanitario"; }
                                        else if (code.startsWith("0602") || code.startsWith("1209")) { entity = "SENASA"; permitName = "Permiso fitosanitario"; }
                                        else if (code.startsWith("4407")) { entity = "SERFOR"; permitName = "Permiso forestal"; }
                                    }
                                    
                                    if (entity.equals("--")) continue;
                                    countPermisos++;
                                    
                                    String rawState = imp.getEstado();
                                    if (rawState == null) rawState = "BORRADOR";
                                    
                                    String stateLabel = "Pendiente";
                                    String stateClass = "text-orange-600 bg-orange-50 border-orange-200";
                                    String actionLabel = "Preparar";
                                    String actionClass = "soft-button text-[10px]";
                                    
                                    if (rawState.equals("BORRADOR") || rawState.equals("COTIZACION")) {
                                        stateLabel = "Borrador";
                                        stateClass = "text-gray-500 bg-gray-50 border-gray-200";
                                        actionLabel = "Editar";
                                        actionClass = "soft-button text-[10px]";
                                    } else if (rawState.equals("PENDIENTE_DOCS")) {
                                        stateLabel = "En revisión";
                                        stateClass = "text-blue-600 bg-blue-50 border-blue-200";
                                        actionLabel = "Continuar";
                                        actionClass = "soft-button text-[10px]";
                                    } else if (rawState.equals("LISTA_DESPACHO") || rawState.equals("NACIONALIZADA")) {
                                        stateLabel = "Aprobado";
                                        stateClass = "text-emerald-600 bg-emerald-50 border-emerald-200";
                                        actionLabel = "Descargar";
                                        actionClass = "primary-button text-[10px]";
                                    }
                                    
                                    String fecha = imp.getFechaCreacion() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(imp.getFechaCreacion()) : "--";
                        %>
                        <tr class="hover:bg-[var(--surface-2)]/50 transition-all permit-row-item border-b border-[var(--border)]" data-state-label="<%= stateLabel %>" data-raw-state="<%= rawState %>">
                            <td class="px-8 py-5">
                                <span class="font-bold text-[var(--text-primary)] block truncate max-w-[200px]"><%= HtmlUtil.escape(imp.getProductoDesc() != null ? imp.getProductoDesc() : "Restringida") %></span>
                                <span class="text-[8px] text-[var(--text-tertiary)] font-bold uppercase block mt-0.5">OP-<%= String.format("%05d", imp.getId()) %></span>
                            </td>
                            <td class="px-8 py-5 font-black text-[var(--text-tertiary)] uppercase">
                                <%= entity %>
                            </td>
                            <td class="px-8 py-5 font-semibold text-[var(--text-secondary)]">
                                <%= permitName %>
                            </td>
                            <td class="px-8 py-5">
                                <span class="px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase border <%= stateClass %>"><%= stateLabel %></span>
                            </td>
                            <td class="px-8 py-5 text-[var(--text-secondary)] font-semibold">
                                <%= fecha %>
                            </td>
                            <td class="px-8 py-5 text-center">
                                <button onclick="selectAndEvaluateOp('<%= imp.getId() %>')" class="<%= actionClass %>">
                                    <%= actionLabel %>
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
            <div class="mb-8 fade-in border-t border-[var(--border)] pt-8" style="animation-delay: 0.15s">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">1</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Elige una importacion para revisar permisos</h2>
                    </div>
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">
                        <div class="lg:col-span-2">
                            <select id="selectOperacion" onchange="onOperacionChange()" class="w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3.5 text-xs text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all cursor-pointer font-semibold">
                                <option value="">Selecciona una importacion registrada</option>
                            </select>
                        </div>
                        <button onclick="evaluarOperacion()" id="btnEvaluar" disabled class="primary-button text-xs flex items-center justify-center gap-2">
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
            <div id="resultadoSection" class="hidden mb-8 fade-in" style="animation-delay: 0.15s">
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
            <div id="cuestionarioSection" class="hidden mb-8 fade-in" style="animation-delay: 0.2s">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">3</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Preguntas de confirmacion</h2>
                        <span id="cuestionarioEntidad" class="ml-auto text-[10px] font-black px-3 py-1 rounded-lg bg-blue-50 text-[var(--accent)] border border-[var(--accent-glow)] uppercase tracking-widest"></span>
                    </div>
                    <div id="preguntasContainer" class="space-y-4"></div>
                    <div class="mt-6 flex justify-end">
                        <button onclick="submitCuestionario()" class="primary-button text-xs flex items-center gap-2">
                            Confirmar respuestas
                        </button>
                    </div>
                </div>
            </div>

            <!-- Step 4: Expediente Digital -->
            <div id="expedienteSection" class="hidden mb-8 fade-in" style="animation-delay: 0.25s">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">4</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Carpeta de documentos del permiso</h2>
                    </div>
                    
                    <!-- Actions bar -->
                    <div class="flex flex-wrap gap-3 mb-6">
                        <button onclick="autorrellenarExpediente()" id="btnAutorrellenar" class="primary-button text-xs flex items-center gap-2">
                            Autorrellenar Expediente
                        </button>
                        <button onclick="descargarPDF()" id="btnPdf" disabled class="soft-button text-xs flex items-center gap-2 disabled:opacity-30 disabled:cursor-not-allowed">
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
            <div id="checklistSection" class="hidden mb-8 fade-in" style="animation-delay: 0.3s">
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
            <div id="suceSection" class="hidden mb-8 fade-in" style="animation-delay: 0.35s">
                <div class="glass-card p-6">
                    <div class="flex items-center gap-3 mb-5">
                        <div class="w-8 h-8 rounded-xl bg-[var(--accent)] text-white flex items-center justify-center text-sm font-bold">6</div>
                        <h2 class="text-lg font-bold text-[var(--text-primary)]">Registro del tramite</h2>
                    </div>
                    <p class="text-xs text-[var(--text-secondary)] font-semibold mb-5">Registra el número de trámite SUCE obtenido en la Ventanilla Única de Comercio Exterior para vincular tu expediente con la operación oficial.</p>
                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-5 font-semibold text-xs">
                        <div>
                            <label class="block text-[9px] text-[var(--text-tertiary)] font-bold mb-2 uppercase tracking-widest">Número SUCE (Solicitud Única)</label>
                            <input type="text" id="inputSuce" placeholder="Ej: SUCE-2026-00001" class="custom-input w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3 text-sm text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all placeholder-gray-400">
                        </div>
                        <div>
                            <label class="block text-[9px] text-[var(--text-tertiary)] font-bold mb-2 uppercase tracking-widest">Resolución Directoral (si aprobado)</label>
                            <input type="text" id="inputResolucion" placeholder="Ej: RD-2026-000123-MTC" class="custom-input w-full bg-white border border-[var(--border)] rounded-xl px-4 py-3 text-sm text-[var(--text-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--accent-glow)] focus:border-[var(--accent)] transition-all placeholder-gray-400">
                        </div>
                    </div>
                    <button onclick="registrarSuce()" class="primary-button text-xs">
                        Registrar tramite
                    </button>
                </div>
            </div>

            <!-- Historial de solicitudes -->
            <div id="historialSection" class="hidden mb-8 fade-in" style="animation-delay: 0.1s">
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
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
    const CTX = '<%= request.getContextPath() %>';
    const CSRF = window.csrfToken || '';
    let currentOperacion = null;
    let currentSolicitudes = [];
    let activeSolicitudId = null;

    // Triggered when dynamic row buttons are clicked
    function selectAndEvaluateOp(opId) {
        const select = document.getElementById('selectOperacion');
        select.value = opId;
        onOperacionChange();
        
        // Auto Scroll to evaluation section
        document.getElementById('selectOperacion').scrollIntoView({ behavior: 'smooth', block: 'start' });
        
        // Auto trigger evaluation
        setTimeout(() => {
            evaluarOperacion();
        }, 300);
    }

    async function cargarOperaciones() {
        try {
            const resp = await fetch(CTX + '/api/importacion/listar');
            if (!resp.ok) throw new Error('Error cargando operaciones');
            const ops = await resp.json();
            const select = document.getElementById('selectOperacion');
            select.innerHTML = '<option value="">— Selecciona una operación registrada —</option>';
            
            ops.forEach(op => {
                const opt = document.createElement('option');
                opt.value = op.id;
                const fecha = op.fechaCreacion ? new Date(op.fechaCreacion).toLocaleDateString('es-PE') : '';
                const cif = op.cif ? `CIF $${parseFloat(op.cif).toFixed(2)}` : '';
                opt.textContent = `#${op.id} — ${op.productoDesc || 'Sin descripcion'} | Codigo: ${op.hsCode || 'N/A'} | ${cif} | ${fecha}`;
                opt.dataset.json = JSON.stringify(op);
                select.appendChild(opt);
            });

            document.getElementById('historialSection').classList.remove('hidden');
            cargarHistorial();
        } catch(e) {
            console.error('Error:', e);
        }
    }

    function onOperacionChange() {
        const select = document.getElementById('selectOperacion');
        const btn = document.getElementById('btnEvaluar');
        const details = document.getElementById('opDetails');
        
        if (select.value) {
            btn.disabled = false;
            const op = JSON.parse(select.selectedOptions[0].dataset.json);
            currentOperacion = op;
            
            document.getElementById('opProducto').textContent = op.productoDesc || '—';
            document.getElementById('opHsCode').textContent = op.hsCode || '—';
            document.getElementById('opPais').textContent = op.paisOrigen || '—';
            document.getElementById('opCif').textContent = op.cif ? `$ ${parseFloat(op.cif).toFixed(2)}` : '—';
            details.classList.remove('hidden');
        } else {
            btn.disabled = true;
            currentOperacion = null;
            details.classList.add('hidden');
        }
        
        ['resultadoSection','cuestionarioSection','expedienteSection','checklistSection','suceSection'].forEach(id => {
            document.getElementById(id).classList.add('hidden');
        });
    }

    async function evaluarOperacion() {
        if (!currentOperacion) return;
        
        const loading = document.getElementById('loadingState');
        const resultado = document.getElementById('resultadoSection');
        loading.classList.remove('hidden');
        resultado.classList.add('hidden');

        try {
            const resp = await fetch(CTX + `/api/permisos/evaluar?operacionId=${currentOperacion.id}`);
            if (!resp.ok) throw new Error('Error evaluando');
            const data = await resp.json();
            
            loading.classList.add('hidden');
            renderResultado(data);
            resultado.classList.remove('hidden');

            currentSolicitudes = data.solicitudes || [];
            if (currentSolicitudes.length > 0) {
                activeSolicitudId = currentSolicitudes[0].id;
            }

            if (!data.libre) {
                if (data.entidades && data.entidades.length > 0) {
                    cargarCuestionario(data.entidades[0].codigoEntidad);
                    document.getElementById('cuestionarioSection').classList.remove('hidden');
                }
                document.getElementById('expedienteSection').classList.remove('hidden');
                document.getElementById('checklistSection').classList.remove('hidden');
                document.getElementById('suceSection').classList.remove('hidden');

                if (data.restricciones && data.restricciones.length > 0) {
                    cargarChecklist(data.restricciones[0].codigoEntidad, data.restricciones[0].tipoPermiso);
                }
            }
        } catch(e) {
            loading.classList.add('hidden');
            console.error('Error:', e);
            showNotification('Error', 'Error al evaluar la operación', false);
        }
    }

    function renderResultado(data) {
        const box = document.getElementById('semaforoBox');
        const icon = document.getElementById('semaforoIcon');
        const title = document.getElementById('semaforoTitle');
        const desc = document.getElementById('semaforoDesc');
        const container = document.getElementById('restriccionesContainer');
        const restricciones = data.restricciones || [];
        const requierePermiso = !data.libre && restricciones.length > 0 && data.nivelRiesgoMax !== 'MEDIO';
        const requiereRevision = !data.libre && !requierePermiso;

        if (data.libre) {
            box.className = 'rounded-xl p-5 mb-6 border border-emerald-200 bg-emerald-50';
            icon.className = 'w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 bg-emerald-100';
            icon.innerHTML = 'OK';
            title.textContent = 'No requiere permiso';
            desc.textContent = 'No vemos una restriccion evidente con la informacion actual. Puedes seguir con costo y expediente.';
            container.innerHTML = '';
        } else if (requiereRevision) {
            box.className = 'rounded-xl p-5 mb-6 border border-amber-200 bg-amber-50';
            icon.className = 'w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 bg-amber-100';
            icon.innerHTML = '?';
            title.textContent = 'Revisar';
            desc.textContent = 'El producto tiene puntos que conviene confirmar antes de asumir que ya requiere permiso formal.';
            container.innerHTML = '';
            restricciones.forEach((regla) => {
                const card = document.createElement('div');
                card.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] hover:border-[var(--accent)] transition-all font-semibold text-xs';
                card.innerHTML = `
                    <div class="flex items-start gap-4">
                        <div class="w-10 h-10 rounded-xl bg-white border border-[var(--border)] flex items-center justify-center shrink-0 mt-0.5">
                            <span class="text-xs font-black text-[var(--accent)]">${regla.codigoEntidad}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap mb-1">
                                <h4 class="text-xs font-bold text-[var(--text-primary)]">${regla.tipoPermiso || 'Validacion previa'}</h4>
                                <span class="text-[9px] font-black px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 border border-amber-200">Revisar</span>
                            </div>
                            <p class="text-[11px] text-gray-500 leading-relaxed mt-1">${regla.mensajeUsuario || 'Confirma la ficha tecnica o el uso real antes de preparar el expediente.'}</p>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        } else {
            box.className = `rounded-xl p-5 mb-6 border border-orange-200 bg-orange-50`;
            icon.className = `w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 bg-orange-100`;
            icon.innerHTML = `!!`;
            title.textContent = `Requiere permiso`;
            desc.textContent = `Detectamos ${(restricciones || []).length} restriccion(es) y conviene preparar expediente antes de embarcar.`;

            container.innerHTML = '';
            restricciones.forEach((regla, i) => {
                const card = document.createElement('div');
                card.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] hover:border-[var(--accent)] transition-all font-semibold text-xs';
                card.innerHTML = `
                    <div class="flex items-start gap-4">
                        <div class="w-10 h-10 rounded-xl bg-white border border-[var(--border)] flex items-center justify-center shrink-0 mt-0.5">
                            <span class="text-xs font-black text-[var(--accent)]">${regla.codigoEntidad}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap mb-1">
                                <h4 class="text-xs font-bold text-[var(--text-primary)]">${regla.tipoPermiso}</h4>
                                <span class="text-[9px] font-black px-2 py-0.5 rounded-full bg-orange-50 text-orange-600 border border-orange-200">${regla.nivelRiesgo}</span>
                            </div>
                            <p class="text-[11px] text-gray-500 leading-relaxed mt-1">${regla.mensajeUsuario || ''}</p>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        }
    }

    async function cargarCuestionario(codigoEntidad) {
        try {
            document.getElementById('cuestionarioEntidad').textContent = codigoEntidad;
            const resp = await fetch(CTX + `/api/permisos/preguntas?entidad=${codigoEntidad}`);
            if (!resp.ok) throw new Error('Error cargando preguntas');
            const preguntas = await resp.json();
            
            const container = document.getElementById('preguntasContainer');
            container.innerHTML = '';
            
            preguntas.forEach((p, i) => {
                const div = document.createElement('div');
                div.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] font-semibold text-xs';
                
                let inputHtml = `
                    <div class="flex gap-4 mt-2">
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="pregunta_${p.id}" value="true" class="accent-[var(--accent)]">
                            <span class="text-sm text-gray-600">Sí</span>
                        </label>
                        <label class="flex items-center gap-2 cursor-pointer">
                            <input type="radio" name="pregunta_${p.id}" value="false" class="accent-[var(--accent)]">
                            <span class="text-sm text-gray-600">No</span>
                        </label>
                    </div>`;
                
                div.innerHTML = `
                    <div class="flex items-start gap-3">
                        <span class="w-6 h-6 rounded-lg bg-white border border-[var(--border)] flex items-center justify-center text-xs font-bold text-[var(--accent)] shrink-0 mt-0.5">${i + 1}</span>
                        <div class="flex-1">
                            <p class="text-xs text-[var(--text-primary)] font-bold">${p.pregunta}</p>
                            ${p.obligatoria ? '<span class="text-[9px] text-red-500 font-bold">* Obligatoria</span>' : ''}
                            ${inputHtml}
                        </div>
                    </div>
                `;
                container.appendChild(div);
            });

            document.getElementById('cuestionarioSection').classList.remove('hidden');
        } catch(e) {
            console.error('Error:', e);
        }
    }



    async function submitCuestionario() {
        const opId = currentOperacion ? currentOperacion.id : null;
        if (!opId) return;

        const respuestas = {};
        const inputs = document.querySelectorAll('#preguntasContainer input, #preguntasContainer select');
        
        inputs.forEach(input => {
            const name = input.name;
            if (!name) return;
            const preguntaId = name.replace('pregunta_', '');
            
            if (input.type === 'radio') {
                if (input.checked) {
                    respuestas[preguntaId] = input.value;
                }
            } else {
                respuestas[preguntaId] = input.value;
            }
        });

        if (Object.keys(respuestas).length === 0) {
            showNotification('Atención', 'Por favor, complete las preguntas del cuestionario.', false);
            return;
        }

        try {
            const resp = await fetch(CTX + '/api/permisos/guardar-respuestas', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': CSRF
                },
                body: JSON.stringify({
                    operacionId: opId,
                    respuestas: respuestas
                })
            });

            if (!resp.ok) {
                const errData = await resp.json();
                throw new Error(errData.error || 'Error al guardar respuestas');
            }

            showNotification('Éxito', '¡Respuestas guardadas correctamente para auditoría aduanera!', true);
            document.getElementById('expedienteSection').scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (e) {
            showNotification('Error', e.message, false);
        }
    }

    async function autorrellenarExpediente() {
        if (!activeSolicitudId) return;
        
        const btn = document.getElementById('btnAutorrellenar');
        btn.disabled = true;

        try {
            const resp = await fetch(CTX + `/api/permisos/autorrellenar?solicitudId=${activeSolicitudId}`, { method: 'POST', headers: {'X-CSRF-TOKEN': CSRF} });
            if (!resp.ok) throw new Error('Error autorellenando');
            const data = await resp.json();
            
            const grid = document.getElementById('datosGrid');
            const datosSection = document.getElementById('datosExpediente');
            grid.innerHTML = '';
            
            const labels = {
                'ruc_importador': 'RUC Importador', 'razon_social': 'Razón Social', 'email_contacto': 'Email',
                'producto_descripcion': 'Producto', 'codigo_hs': 'Codigo del producto', 'pais_origen': 'Pais de origen',
                'incoterm': 'Incoterm', 'valor_fob_usd': 'FOB (USD)', 'valor_flete_usd': 'Flete (USD)',
                'valor_seguro_usd': 'Seguro (USD)', 'valor_cif_usd': 'CIF (USD)', 'fecha_solicitud': 'Fecha Solicitud'
            };

            (data.datos || []).forEach(d => {
                const cell = document.createElement('div');
                cell.className = 'bg-[var(--surface-2)] rounded-xl p-3 border border-[var(--border)] font-semibold text-xs';
                cell.innerHTML = `
                    <span class="text-[9px] text-gray-400 uppercase tracking-widest font-semibold block mb-1">${labels[d.campo] || d.campo}</span>
                    <span class="text-xs text-[var(--text-primary)] font-bold">${d.valor || '—'}</span>
                `;
                grid.appendChild(cell);
            });
            
            datosSection.classList.remove('hidden');
            document.getElementById('btnPdf').disabled = false;
            btn.disabled = false;
            
            showNotification('Éxito', '¡Expediente digital autocompletado con éxito!', true);
        } catch(e) {
            btn.disabled = false;
            showNotification('Error', 'Error autorrellenando expediente', false);
        }
    }

    async function descargarPDF() {
        if (!activeSolicitudId) return;
        window.location.href = CTX + `/api/permisos/pdf?id=${activeSolicitudId}`;
        showNotification('Éxito', 'Descargando expediente digital PDF', true);
    }

    async function cargarChecklist(entidad, tipoPermiso) {
        const container = document.getElementById('checklistContainer');
        container.innerHTML = '';
        
        const docs = [
            { id: 'FC', nombre: 'Factura Comercial Proforma', desc: 'Sustenta la compra y composición' },
            { id: 'FT', nombre: 'Ficha Técnica del Fabricante', desc: 'Detalla componentes, rango y frecuencia' },
            { id: 'MU', nombre: 'Manual de Uso o Operación', desc: 'Indica funcionalidad y potencia' }
        ];

        document.getElementById('checklistProgress').innerText = "0 de 3 completados";

        docs.forEach(doc => {
            const div = document.createElement('div');
            div.className = 'p-3 rounded-xl bg-[var(--surface-2)] border border-[var(--border)] flex items-center justify-between font-semibold text-xs';
            div.innerHTML = `
                <div class="flex items-center gap-3">
                    <span class="text-base">📄</span>
                    <div>
                        <h6 class="text-xs font-bold text-[var(--text-primary)]">${doc.nombre}</h6>
                        <p class="text-[10px] text-gray-500">${doc.desc}</p>
                    </div>
                </div>
                <button class="bg-white border border-[var(--border)] hover:bg-[var(--surface-2)] text-[var(--accent)] text-[9px] font-black px-4 py-1.5 rounded-lg uppercase tracking-wider transition-all">Subir</button>
            `;
            container.appendChild(div);
        });
    }

    async function registrarSuce() {
        const opId = currentOperacion ? currentOperacion.id : null;
        if (!opId) return;
        
        const suce = document.getElementById('inputSuce').value;
        const res = document.getElementById('inputResolucion').value;
        
        if (!suce) {
            showNotification('Atención', 'Ingrese el número SUCE de validación', false);
            return;
        }

        try {
            await fetch(CTX + `/api/importacion/cambiarEstado?id=${opId}&nuevoEstado=LISTA_DESPACHO`, { method: 'POST', headers: {'X-CSRF-TOKEN': CSRF} });
            showNotification('Éxito', '¡Número SUCE registrado y expediente vinculado correctamente!', true);
            setTimeout(() => window.location.reload(), 1500);
        } catch(e) {
            showNotification('Error', 'Error vinculando SUCE', false);
        }
    }

    async function cargarHistorial() {
        const container = document.getElementById('historialContainer');
        try {
            const resp = await fetch(CTX + '/api/permisos/listar');
            if (!resp.ok) throw new Error('Error al listar');
            const list = await resp.json();
            
            if (list.length === 0) {
                container.innerHTML = '<div class="text-center py-8 text-gray-400 font-semibold">No se registran tramites anteriores en la bitacora</div>';
                return;
            }

            container.innerHTML = '';
            const listGrid = document.createElement('div');
            listGrid.className = 'grid grid-cols-1 md:grid-cols-2 gap-4';
            
            list.forEach(sol => {
                const item = document.createElement('div');
                item.className = 'bg-[var(--surface-2)] border border-[var(--border)] rounded-2xl p-4 font-semibold text-xs space-y-3';
                item.innerHTML = `
                    <div class="flex justify-between items-start">
                        <div>
                            <h4 class="text-sm font-bold text-[var(--text-primary)]">Tramite #${sol.id}</h4>
                            <p class="text-[9px] text-[var(--accent)] font-bold uppercase mt-0.5">Operación: OP-${sol.operacionId.toString().padStart(5, '0')}</p>
                        </div>
                        <span class="px-2 py-0.5 rounded-full text-[8px] font-black uppercase border border-teal-200 bg-teal-50 text-[var(--accent)]">${sol.estado}</span>
                    </div>
                    <div class="grid grid-cols-2 gap-2 text-[10px] border-t border-[var(--border)]/50 pt-2 font-medium">
                        <div><span class="text-gray-400">Entidad:</span> <strong class="text-[var(--text-primary)]">${sol.codigoEntidad}</strong></div>
                        <div><span class="text-gray-400">Permiso:</span> <strong class="text-[var(--text-primary)]">${sol.tipoPermiso}</strong></div>
                        <div><span class="text-gray-400">SUCE:</span> <strong class="text-purple-600 font-mono">${sol.suce || 'En borrador'}</strong></div>
                        <div><span class="text-gray-400">Resolución:</span> <strong class="text-[var(--text-primary)]">${sol.resolucion || 'Pendiente'}</strong></div>
                    </div>
                `;
                listGrid.appendChild(item);
            });
            container.appendChild(listGrid);
        } catch(e) {
            container.innerHTML = '<div class="text-center py-8 text-rose-500 font-semibold">Error al cargar historial de trámites</div>';
        }
    }

    function filterPermits(filter) {
        const pills = document.querySelectorAll('.permit-pill');
        pills.forEach(p => p.className = "px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all permit-pill");
        
        let activeId = "permit-pill-Pendientes";
        if (filter === 'En trámite') activeId = "permit-pill-Tramite";
        else if (filter === 'Aprobados') activeId = "permit-pill-Aprobados";
        else if (filter === 'Rechazados') activeId = "permit-pill-Rechazados";
        
        const activePill = document.getElementById(activeId);
        if (activePill) {
            activePill.className = "px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-[var(--accent)] text-white transition-all permit-pill active";
        }
        
        const rows = document.querySelectorAll('.permit-row-item');
        rows.forEach(row => {
            const state = row.getAttribute('data-state-label');
            const rawState = row.getAttribute('data-raw-state');
            
            let show = false;
            if (filter === 'Pendientes' && state === 'Pendiente') {
                show = true;
            } else if (filter === 'En trámite' && state === 'En revisión') {
                show = true;
            } else if (filter === 'Aprobados' && state === 'Aprobado') {
                show = true;
            } else if (filter === 'Rechazados' && state === 'Borrador') {
                show = true;
            }
            
            row.style.display = show ? '' : 'none';
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        cargarOperaciones();
        
        // Auto default filter to Pending
        setTimeout(() => {
            filterPermits('Pendientes');
        }, 300);
    });
    </script>
</body>
</html>

