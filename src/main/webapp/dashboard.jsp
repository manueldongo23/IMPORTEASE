<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    String userRuc = (String) session.getAttribute("usuarioRuc");
    String userNombre = (String) session.getAttribute("usuarioNombre");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Inicio</title>
    <link rel="preconnect" href="https://cdn.jsdelivr.net">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" async src="https://cdn.jsdelivr.net/npm/chart.js@4.4.4/dist/chart.umd.min.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
</head>
<body class="flex h-screen overflow-hidden bg-[var(--surface-0)] font-['Outfit'] text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "dashboard"); %>
<% 
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Panel"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-hidden relative">
        <div id="draftAlert" class="hidden fixed top-20 right-10 z-[110] bg-[var(--surface-1)] border border-[var(--border)] p-4 rounded-2xl shadow-xl flex items-center gap-4 animate-fadeUp">
            <div class="w-10 h-10 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] flex items-center justify-center text-[var(--accent)] font-black">D</div>
            <div>
                <p class="text-[10px] font-black uppercase text-[var(--accent)] tracking-[0.2em]">Borrador detectado</p>
                <a href="evaluacion.jsp?step=1" class="text-xs font-bold underline text-[var(--accent)]">Retomar importación</a>
            </div>
            <button onclick="borrarBorrador()" class="text-[var(--text-tertiary)] ml-4 hover:text-[var(--text-primary)]">✕</button>
        </div>

        <header class="h-16 border-b border-[var(--border)] px-8 flex items-center justify-between bg-[var(--surface-1)]/80 backdrop-blur-xl z-20">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Guia para empezar</span>
            </div>

            <a href="evaluacion.jsp?step=1" class="bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black px-5 py-2.5 rounded-xl text-xs uppercase tracking-[0.16em] transition-all shadow-lg active:scale-98 inline-flex items-center justify-center">
                Importar paso a paso
            </a>
        </header>
        
        <jsp:include page="/fragments/breadcrumb.jsp" />

        <div class="flex-1 overflow-y-auto p-8 space-y-6 custom-scrollbar pb-24">
            <!-- Hero + Fast Links Row -->
            <section class="grid grid-cols-1 xl:grid-cols-3 gap-6">
                <!-- Hero section -->
                <div class="xl:col-span-2 hero-banner p-8 space-y-6 relative fade-up">
                    <div class="space-y-3 relative z-10">
                        <p class="text-[11px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">No necesitas experiencia previa</p>
                        <h2 class="text-3xl font-black tracking-tight text-[var(--text-primary)]">Hola, <%= com.importease.proyecto.service.HtmlUtil.escape(userNombre != null ? userNombre : "Manuel") %></h2>
                        <p class="text-4xl font-black tracking-tight text-[var(--accent)] leading-tight">Importa con una ruta clara.</p>
                        <p class="text-sm text-[var(--text-secondary)] font-semibold max-w-2xl">Dinos que producto quieres traer. ImportEase te explica el codigo aduanero, los permisos, los impuestos y los documentos en el orden correcto.</p>
                    </div>

                    <div id="heroCtaNoDraft" class="flex flex-col sm:flex-row gap-3 relative z-10">
                        <a href="evaluacion.jsp?step=1" class="btn-primary px-8 py-4 text-sm font-black uppercase tracking-widest shadow-lg">Nueva importación</a>
                    </div>
                    <div id="heroCtaDraft" class="hidden flex flex-col sm:flex-row gap-3 relative z-10">
                        <a href="evaluacion.jsp?step=1" class="btn-primary px-8 py-4 text-sm font-black uppercase tracking-widest shadow-lg">Continuar donde lo dejaste</a>
                        <button onclick="borrarBorrador()" class="text-[var(--text-tertiary)] ml-4 hover:text-[var(--text-primary)] underline text-xs">Empezar de cero</button>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 relative z-10 border-t border-[var(--border)]">
                        <div class="p-4 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)]">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 1</p>
                            <p class="text-xs font-bold text-[var(--text-secondary)] mt-1.5">Describe el producto con tus palabras.</p>
                        </div>
                        <div class="p-4 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)]">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 2</p>
                            <p class="text-xs font-bold text-[var(--text-secondary)] mt-1.5">El sistema traduce eso a codigo y permisos.</p>
                        </div>
                        <div class="p-4 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)]">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 3</p>
                            <p class="text-xs font-bold text-[var(--text-secondary)] mt-1.5">Revisa costos, documentos y siguiente accion.</p>
                        </div>
                    </div>
                </div>

                <!-- Fast Links -->
                <div class="card-shell fade-up" style="animation-delay: 100ms">
                    <div class="card-shell__body space-y-4">
                        <div>
                            <p class="text-[11px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">Accesos útiles</p>
                            <h3 class="text-xl font-black text-[var(--text-primary)] mt-1">Enlaces rapidos</h3>
                        </div>
                        <a href="seguimiento.jsp" class="block p-4 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] transition-all">
                            <p class="text-sm font-black text-[var(--text-primary)]">Ver mis importaciones</p>
                            <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Revisa el estado de tus operaciones.</p>
                        </a>
                        <a href="buscador.jsp" class="block p-4 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] transition-all">
                            <p class="text-sm font-black text-[var(--text-primary)]">Buscar codigo</p>
                            <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Busca como aduanas identifica tu producto.</p>
                        </a>
                    </div>
                </div>
            </section>

            <!-- KPI Skeleton Loading -->
            <div id="kpiSkeleton" class="kpi-grid grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                <div class="kpi-card skeleton animate-pulse h-24 rounded-2xl bg-[var(--surface-2)]"></div>
                <div class="kpi-card skeleton animate-pulse h-24 rounded-2xl bg-[var(--surface-2)]"></div>
                <div class="kpi-card skeleton animate-pulse h-24 rounded-2xl bg-[var(--surface-2)]"></div>
                <div class="kpi-card skeleton animate-pulse h-24 rounded-2xl bg-[var(--surface-2)]"></div>
            </div>

            <!-- KPI Cards -->
            <section class="kpi-grid stagger-children">
                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">Operaciones</p>
                            <span id="kpi-operaciones-badge" class="kpi-card__value kpi-card__value--accent">0</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--accent">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75z"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--accent"></div>
                    <a href="seguimiento.jsp" class="kpi-card__action">Ver todas →</a>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">FOB Total</p>
                            <span id="kpi-fob" class="kpi-card__value">$ --</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--info">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--info"></div>
                    <span class="kpi-card__footer">Valor acumulado</span>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">Tributos</p>
                            <span id="kpi-tributos" class="kpi-card__value kpi-card__value--accent">S/ --</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--accent">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.375m0-10.5h3.375c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125H9.75"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--accent"></div>
                    <a href="calculadora-negocio.jsp" class="kpi-card__action">Calcular →</a>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">Restringidos</p>
                            <span id="kpi-permisos-badge" class="kpi-card__value kpi-card__value--danger">0</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--danger">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m9-.75a9 9 0 11-18 0 9 9 0 0118 0zm-9 3.75h.008v.008H12v-.008z"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--danger"></div>
                    <a href="gestor_permisos.jsp" class="kpi-card__action">Ver permisos</a>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">T/C Hoy</p>
                            <span id="kpi-tc" class="kpi-card__value kpi-card__value--success">S/ --</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--success">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15 9h3.75M15 12h3.75M15 15h3.75M4.5 19.5h15a2.25 2.25 0 002.25-2.25V6.75A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25v10.5A2.25 2.25 0 004.5 19.5z"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--success"></div>
                    <span id="kpi-tc-fuente" class="kpi-card__footer">BCRP</span>
                </div>

                <div class="kpi-card">
                    <div class="kpi-card__header">
                        <div>
                            <p class="kpi-card__label">Plazos activos</p>
                            <span id="kpi-plazos" class="kpi-card__value kpi-card__value--warning">0</span>
                        </div>
                        <div class="kpi-card__icon kpi-card__icon--warning">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                            </svg>
                        </div>
                    </div>
                    <div class="kpi-card__bar kpi-card__bar--warning"></div>
                    <a href="expediente-aduanero.jsp" class="kpi-card__action">Ver revision</a>
                </div>
            </section>

            <!-- Row 2: Canales + Top HS + Estado -->
            <section class="grid grid-cols-1 lg:grid-cols-3 gap-6 stagger-children">
                <!-- Distribución de Canales -->
                <div class="card-shell">
                    <div class="card-shell__body">
                        <h3 class="section-title">Distribución de canales</h3>
                        <div class="flex items-center justify-center" style="height: 180px;">
                            <canvas id="chartCanales" width="180" height="180"></canvas>
                        </div>
                        <div id="canalLegend" class="flex flex-wrap gap-2 mt-4 justify-center text-[10px] font-bold"></div>
                    </div>
                </div>

                <!-- Top 5 Subpartidas -->
                <div class="card-shell">
                    <div class="card-shell__body">
                        <h3 class="section-title">Productos mas revisados</h3>
                        <div id="topHsContainer" class="space-y-3">
                            <p class="text-xs text-[var(--text-tertiary)] text-center py-6">Sin datos aún</p>
                        </div>
                    </div>
                </div>

                <!-- Operaciones por Estado -->
                <div class="card-shell">
                    <div class="card-shell__body">
                        <h3 class="section-title">Operaciones por estado</h3>
                        <div id="estadoContainer" class="space-y-3">
                            <p class="text-xs text-[var(--text-tertiary)] text-center py-6">Sin datos aún</p>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Row 3: Siguiente paso + Últimas operaciones -->
            <section class="grid grid-cols-1 lg:grid-cols-5 gap-6 stagger-children">
                <!-- Recommended Next Step -->
                <div class="lg:col-span-3 card-shell">
                    <div class="card-shell__body flex flex-col justify-between h-full">
                        <div>
                            <h3 class="section-title">Siguiente paso recomendado</h3>

                            <div id="pendientesListContainer" class="divide-y divide-[var(--border)]/50 space-y-4">
                                <p class="text-xs text-[var(--text-tertiary)] font-medium py-6 text-center">Cargando tus tareas pendientes...</p>
                            </div>
                        </div>

                        <div class="mt-6 border-t border-[var(--border)] pt-6">
                            <h3 class="section-title">Alertas importantes</h3>
                            <div id="alertasListContainer" class="space-y-3">
                                <p class="text-xs text-[var(--text-tertiary)] font-medium py-4 text-center">Cargando alertas críticas...</p>
                            </div>
                        </div>

                        <div class="mt-6 p-4 rounded-xl bg-[var(--surface-0)] border border-[var(--border)] flex items-center gap-3">
                            <span class="w-7 h-7 rounded-full bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[var(--accent)] flex items-center justify-center font-black text-xs shrink-0">i</span>
                            <p class="text-[10px] text-[var(--text-secondary)] font-semibold leading-relaxed">
                                <strong>Consejo:</strong> Si no sabes por donde empezar, usa <a href="evaluacion.jsp?step=1" class="text-[var(--accent)] font-bold hover:underline">Importar paso a paso</a>. El sistema te dira que dato falta, que significa y cual es la siguiente accion.
                            </p>
                        </div>
                    </div>
                </div>

                <!-- Last Operations -->
                <div class="lg:col-span-2 card-shell">
                    <div class="card-shell__body flex flex-col justify-between h-full">
                        <div>
                            <div class="flex justify-between items-center mb-4">
                                <h3 class="section-title mb-0">Últimas operaciones</h3>
                                <a href="seguimiento.jsp" class="kpi-card__action">Ver todas</a>
                            </div>

                            <div class="table-container">
                                <table class="table-custom text-[11px]">
                                    <thead>
                                        <tr>
                                            <th>Producto</th>
                                            <th>Canal</th>
                                            <th class="text-center">Estado</th>
                                        </tr>
                                    </thead>
                                    <tbody id="recentActivityTableBody" class="font-semibold text-[var(--text-secondary)]">
                                        <tr>
                                            <td colspan="3" class="text-center text-[var(--text-tertiary)] font-medium">Cargando actividad reciente...</td>
                                        </tr>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';

        function escapeHtml(value) {
            if (value === null || value === undefined) return '';
            return value.toString()
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/\"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        function iniciarNuevaRuta(event) {
            if (event) event.preventDefault();
            window.location.href = 'evaluacion.jsp?step=1';
        }

        function borrarBorrador() {
            localStorage.removeItem('importease_wizard_draft');
            document.getElementById('draftAlert').classList.add('hidden');
            actualizarHeroCta();
        }

        function actualizarHeroCta() {
            const noDraft = document.getElementById('heroCtaNoDraft');
            const draft = document.getElementById('heroCtaDraft');
            if (!noDraft || !draft) return;
            if (localStorage.getItem('importease_wizard_draft')) {
                noDraft.classList.add('hidden');
                draft.classList.remove('hidden');
            } else {
                noDraft.classList.remove('hidden');
                draft.classList.add('hidden');
            }
        }

        async function fetchWithTimeout(url, options = {}, timeoutMs = 3500) {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), timeoutMs);
            try {
                return await fetch(url, { ...options, signal: controller.signal });
            } finally {
                clearTimeout(timeoutId);
            }
        }

        function hideDashboardSkeleton() {
            const skeleton = document.getElementById('kpiSkeleton');
            if (skeleton) skeleton.classList.add('hidden');
        }

        async function loadDashboardStats() {
            try {
                const res = await fetchWithTimeout(`${window.ctx}/api/dashboard/stats`, {}, 3500);
                if (res.ok) {
                    const stats = await res.json();

                    hideDashboardSkeleton();

                    const operationsBadge = document.getElementById('kpi-operaciones-badge');
                    if (operationsBadge) operationsBadge.innerText = stats.totalOps || 0;

                    const permisosBadge = document.getElementById('kpi-permisos-badge');
                    if (permisosBadge) permisosBadge.innerText = stats.restringidos || 0;

                    // FOB total
                    const fobKpi = document.getElementById('kpi-fob');
                    if (fobKpi) fobKpi.innerText = `$ ${Number(stats.fobTotal || 0).toLocaleString('en-US', {minimumFractionDigits: 0, maximumFractionDigits: 0})}`;

                    // Tributos
                    const tributosKpi = document.getElementById('kpi-tributos');
                    if (tributosKpi) tributosKpi.innerText = `S/ ${Number(stats.tributosTotal || 0).toLocaleString('es-PE', {minimumFractionDigits: 0, maximumFractionDigits: 0})}`;

                    // Tipo de cambio
                    const tcKpi = document.getElementById('kpi-tc');
                    const tcFuente = document.getElementById('kpi-tc-fuente');
                    if (stats.tipoCambio && tcKpi) {
                        tcKpi.innerText = `S/ ${Number(stats.tipoCambio.tipoCambio || 3.75).toFixed(3)}`;
                        if (tcFuente) tcFuente.innerText = stats.tipoCambio.fuente || 'BCRP';
                    }

                    // Plazos activos
                    const plazosKpi = document.getElementById('kpi-plazos');
                    if (plazosKpi) plazosKpi.innerText = stats.plazosActivos || 0;

                    // Gráfico de canales
                    renderCanalChart(stats.porCanal || {});

                    // Top HS Codes
                    renderTopHs(stats.topHsCodes || []);

                    // Operaciones por estado
                    renderEstados(stats.porEstado || {});

                    // Alertas Críticas
                    renderAlertasCriticas(stats.alertasCriticas || []);
                }
            } catch (e) {
                console.error('Error al cargar estadisticas del panel', e);
                hideDashboardSkeleton();
                renderAlertasCriticas([]);
            }
        }

        function renderAlertasCriticas(alertas) {
            const container = document.getElementById('alertasListContainer');
            if (!container) return;

            if (!alertas || !alertas.length) {
                container.innerHTML = `<p class="text-xs text-[var(--text-tertiary)] font-medium text-center py-4">No hay alertas activas</p>`;
                return;
            }

            container.innerHTML = alertas.map(alert => {
                let toneClass = 'bg-blue-50 text-blue-700 border-blue-100 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-950/45';
                let icon = 'ℹ️';
                if (alert.tipo === 'CRITICAL') {
                    toneClass = 'bg-rose-50 text-rose-700 border-rose-100 dark:bg-rose-950/20 dark:text-rose-400 dark:border-rose-950/45';
                    icon = '🚨';
                } else if (alert.tipo === 'WARNING') {
                    toneClass = 'bg-amber-50 text-amber-700 border-amber-100 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-950/45';
                    icon = '⚠️';
                }

                return `
                    <div class="p-4 rounded-2xl border ${toneClass} flex items-start gap-3">
                        <span class="text-lg shrink-0">${icon}</span>
                        <div class="min-w-0 flex-1">
                            <h5 class="text-xs font-black uppercase tracking-wider">${escapeHtml(alert.titulo)}</h5>
                            <p class="text-[11px] font-semibold mt-0.5 leading-relaxed">${escapeHtml(alert.mensaje)}</p>
                        </div>
                    </div>
                `;
            }).join('');
        }

        function renderCanalChart(porCanal) {
            const canvas = document.getElementById('chartCanales');
            if (!canvas) return;
            const labels = Object.keys(porCanal);
            const values = Object.values(porCanal);
            const colors = { 'VERDE': '#22c55e', 'NARANJA': '#f97316', 'ROJO': '#ef4444', 'SIN CANAL': '#9ca3af' };
            const bgColors = labels.map(l => colors[l] || '#6366f1');

            if (labels.length === 0) {
                canvas.parentElement.innerHTML = '<p class="text-xs text-[var(--text-tertiary)] text-center">Sin datos de canal</p>';
                return;
            }

            if (typeof Chart === 'undefined') {
                canvas.parentElement.innerHTML = labels.map((l, i) => `
                    <div class="w-full flex items-center justify-between px-4 py-2 rounded-xl bg-[var(--surface-0)] border border-[var(--border)]">
                        <span class="flex items-center gap-2 text-[11px] font-black uppercase">
                            <span class="w-2 h-2 rounded-full" style="background:${bgColors[i]}"></span>${escapeHtml(l)}
                        </span>
                        <span class="text-sm font-black">${values[i]}</span>
                    </div>
                `).join('');
                return;
            }

            new Chart(canvas, {
                type: 'doughnut',
                data: { labels, datasets: [{ data: values, backgroundColor: bgColors, borderWidth: 0 }] },
                options: { responsive: false, plugins: { legend: { display: false } }, cutout: '65%' }
            });

            const legend = document.getElementById('canalLegend');
            if (legend) {
                legend.innerHTML = labels.map((l, i) => `<span class="flex items-center gap-1"><span class="w-2 h-2 rounded-full" style="background:${bgColors[i]}"></span>${l}: ${values[i]}</span>`).join('');
            }
        }

        function renderTopHs(list) {
            const container = document.getElementById('topHsContainer');
            if (!container || !list.length) return;
            container.innerHTML = list.map((item, i) => `
                <div class="flex items-center gap-3 p-3 rounded-xl bg-[var(--surface-0)] border border-[var(--border)]">
                    <span class="w-7 h-7 rounded-lg bg-[var(--accent-soft)] text-[var(--accent)] flex items-center justify-center text-[10px] font-black shrink-0">${i + 1}</span>
                    <div class="min-w-0 flex-1">
                        <p class="text-[10px] font-black text-[var(--accent)] tracking-wider">${escapeHtml(item.hsCode)}</p>
                        <p class="text-[11px] font-semibold text-[var(--text-secondary)] truncate">${escapeHtml(item.descripcion || 'Sin descripción')}</p>
                    </div>
                    <span class="text-xs font-black text-[var(--text-primary)]">${item.usos}x</span>
                </div>
            `).join('');
        }

        function renderEstados(porEstado) {
            const container = document.getElementById('estadoContainer');
            if (!container || !Object.keys(porEstado).length) return;
            const stateColors = { 'COTIZACION': 'bg-gray-100 text-gray-700', 'TRAMITE': 'bg-blue-50 text-blue-700', 'NACIONALIZADA': 'bg-emerald-50 text-emerald-700', 'PENDIENTE_DOCS': 'bg-rose-50 text-rose-700', 'LISTA_DESPACHO': 'bg-amber-50 text-amber-700' };
            container.innerHTML = Object.entries(porEstado).map(([estado, count]) => {
                const cls = stateColors[estado] || 'bg-[var(--surface-2)] text-[var(--text-secondary)]';
                return `<div class="flex items-center justify-between p-3 rounded-xl ${cls} border border-current/10">
                    <span class="text-[11px] font-black uppercase tracking-wider">${escapeHtml(estado)}</span>
                    <span class="text-lg font-black">${count}</span>
                </div>`;
            }).join('');
        }

        async function loadKanban() {
            try {
                const res = await fetchWithTimeout(`${window.ctx}/api/importacion/listar`, {}, 3500);
                const data = await res.json();
                renderRecentTable(data);
                renderPendientesList(data);
            } catch (e) {
                console.error('Error al cargar listado', e);
                renderRecentTable([]);
                renderPendientesList([]);
            }
        }

        function getProductTag(hsCode, desc) {
            const d = (desc || '').toLowerCase();
            const code = hsCode || '';
            if (code.startsWith('8517') || d.includes('celular') || d.includes('phone')) return 'TEL';
            if (code.startsWith('2106') || code.startsWith('1901') || d.includes('suplemento') || d.includes('alimento')) return 'FOOD';
            if (d.includes('laptop') || d.includes('computadora') || d.includes('pc')) return 'IT';
            if (code.startsWith('3004') || d.includes('medicina')) return 'SAL';
            if (code.startsWith('3304') || d.includes('cosmetico') || d.includes('perfume')) return 'COS';
            return 'BOX';
        }

        function renderPendientesList(list) {
            const container = document.getElementById('pendientesListContainer');
            if (!container) return;

            const activeOps = list.filter(imp => imp.estado !== 'NACIONALIZADA');
            activeOps.sort((a, b) => {
                const priority = { 'PENDIENTE_DOCS': 1, 'BORRADOR': 2, 'COTIZACION': 2, 'LISTA_DESPACHO': 3 };
                return (priority[a.estado] || 4) - (priority[b.estado] || 4);
            });

            const displayList = activeOps.slice(0, 3);

            if (displayList.length === 0) {
                container.innerHTML = `
                    <div class="flex flex-col items-center justify-center py-8 opacity-70 text-center gap-2">
                        <div class="w-10 h-10 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[var(--accent)] flex items-center justify-center font-black">OK</div>
                        <p class="text-xs font-bold text-[var(--accent)] uppercase tracking-wider">No tienes pendientes</p>
                        <p class="text-[10px] text-[var(--text-tertiary)] font-medium">Cuando registres evaluaciones, aquí verás los siguientes pasos más urgentes.</p>
                    </div>
                `;
                return;
            }

            container.innerHTML = '';
            displayList.forEach((imp, index) => {
                const safeProducto = escapeHtml(imp.productoDesc || 'Mercancía general');
                const tag = getProductTag(imp.hsCode, imp.productoDesc);

                let stateText = 'Completa datos base de la operación';
                let actionBtn = 'Continuar';
                let actionClick = "window.location.href='evaluacion.jsp'";
                let toneClass = 'bg-[var(--accent-soft)] text-[var(--accent)]';

                if (imp.estado === 'PENDIENTE_DOCS') {
                    stateText = 'Faltan documentos comerciales';
                    actionBtn = 'Subir docs';
                    actionClick = "window.location.href='documentos.jsp'";
                    toneClass = 'bg-rose-50 text-rose-600 dark:bg-rose-950/20 dark:text-rose-400';
                } else if (imp.estado === 'LISTA_DESPACHO') {
                    stateText = 'Lista para pasar a seguimiento';
                    actionBtn = 'Ver avance';
                    actionClick = "window.location.href='seguimiento.jsp'";
                    toneClass = 'bg-emerald-50 text-emerald-600 dark:bg-emerald-950/20 dark:text-emerald-400';
                }

                const ptClass = index === 0 ? 'pt-2' : 'pt-4';
                const pbClass = index === displayList.length - 1 ? 'pb-2' : '';

                const itemDiv = document.createElement('div');
                itemDiv.className = `flex items-center justify-between ${ptClass} ${pbClass} transition-all duration-300 gap-4`;
                itemDiv.innerHTML = `
                    <div class="flex items-center gap-3 min-w-0">
                        <div class="w-10 h-10 rounded-xl ${toneClass} flex items-center justify-center text-[10px] font-black shrink-0 border border-current/10">${tag}</div>
                        <div class="min-w-0">
                            <h5 class="text-sm font-bold text-[var(--text-primary)] truncate">${safeProducto}</h5>
                            <p class="text-[11px] text-[var(--text-tertiary)] font-semibold mt-0.5">${stateText}</p>
                        </div>
                    </div>
                    <button onclick="${actionClick}" class="bg-[var(--surface-1)] hover:bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-primary)] text-[10px] font-black px-4 py-2 rounded-lg uppercase tracking-wider transition-all shrink-0">
                        ${actionBtn}
                    </button>
                `;
                container.appendChild(itemDiv);
            });
        }

        function renderRecentTable(list) {
            const tbody = document.getElementById('recentActivityTableBody');
            if (!tbody) return;
            tbody.innerHTML = '';

            list.sort((a, b) => b.id - a.id);
            const recentList = list.slice(0, 4);

            if (recentList.length === 0) {
                tbody.innerHTML = `<tr><td colspan="3" class="p-6 text-center text-[var(--text-tertiary)] font-medium">No hay actividad registrada.</td></tr>`;
                return;
            }

            recentList.forEach(imp => {
                let stateLabel = 'Listo';
                let stateClass = 'text-emerald-600 bg-emerald-50 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-950/45';

                if (imp.estado === 'BORRADOR' || imp.estado === 'COTIZACION') {
                    stateLabel = 'Borrador';
                    stateClass = 'text-[var(--text-secondary)] bg-[var(--surface-2)] border-[var(--border)]';
                } else if (imp.estado === 'PENDIENTE_DOCS') {
                    stateLabel = 'Expediente';
                    stateClass = 'text-blue-600 bg-blue-50 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-950/45';
                } else if (imp.hsCode && (imp.hsCode.startsWith('8517') || imp.hsCode.startsWith('2106') || imp.hsCode.startsWith('9018'))) {
                    stateLabel = 'Permiso';
                    stateClass = 'text-orange-600 bg-orange-50 border-orange-200 dark:bg-orange-950/20 dark:text-orange-400 dark:border-orange-950/45';
                }

                let entity = '--';
                if (imp.hsCode) {
                    if (imp.hsCode.startsWith('8517')) entity = 'MTC';
                    else if (imp.hsCode.startsWith('2106')) entity = 'DIGESA';
                    else if (imp.hsCode.startsWith('9018')) entity = 'DIGEMID';
                    else if (imp.hsCode.startsWith('1209')) entity = 'SENASA';
                    else if (imp.hsCode.startsWith('4407')) entity = 'SERFOR';
                }

                const safeProducto = escapeHtml(imp.productoDesc || 'Evaluacion');
                const canal = escapeHtml(imp.canalAsignado || '--');
                const canalClass = imp.canalAsignado === 'VERDE' ? 'text-emerald-600' : imp.canalAsignado === 'ROJO' ? 'text-rose-600' : 'text-orange-500';
                const tr = document.createElement('tr');
                tr.className = 'hover:bg-[var(--surface-2)] transition-colors border-b border-[var(--border)]';
                tr.innerHTML = `
                    <td class="p-3">
                        <span class="font-bold text-[var(--text-primary)] block truncate max-w-[150px]">${safeProducto}</span>
                        <span class="text-[8px] text-[var(--text-tertiary)] font-bold uppercase block mt-0.5">OP-${imp.id.toString().padStart(5, '0')}</span>
                    </td>
                    <td class="p-3 ${canalClass} font-black uppercase">${canal}</td>
                    <td class="p-3 text-center">
                        <span class="px-2 py-0.5 rounded-full text-[8px] font-black uppercase border ${stateClass}">${stateLabel}</span>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }

        document.addEventListener('DOMContentLoaded', () => {
            loadKanban();
            loadDashboardStats();

            actualizarHeroCta();
            if (localStorage.getItem('importease_wizard_draft')) {
                document.getElementById('draftAlert').classList.remove('hidden');
            }
        });
    </script>
</body>
</html>
