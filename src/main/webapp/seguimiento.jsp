<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Seguimiento</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/knowledge-base.js"></script>
</head>
<body class="flex h-screen overflow-hidden bg-[var(--surface-0)] font-['Outfit'] text-[var(--text-primary)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "historial");
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Seguimiento"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-[var(--surface-1)]/75 backdrop-blur-xl sticky top-0 z-10 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Seguimiento simple</span>
            </div>
            <button onclick="window.location.href='evaluacion.jsp'" class="px-5 py-2.5 rounded-xl text-xs font-black uppercase tracking-widest text-white bg-[var(--accent)] hover:bg-[var(--accent-hover)] shadow-md transition-all active:scale-98">
                Nueva importacion
            </button>
        </header>

        <jsp:include page="/fragments/breadcrumb.jsp" />

        <div class="p-8 lg:p-10 max-w-7xl mx-auto space-y-8 w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">&larr; Volver a Importar paso a paso</a>
            <section class="grid lg:grid-cols-[1.5fr_1fr] gap-6">
                <!-- Hero section -->
                <div class="glass-card bg-[var(--surface-1)] border border-[var(--border)] p-8 rounded-3xl shadow-sm flex flex-col justify-between relative overflow-hidden">
                    <div class="absolute inset-0 bg-[radial-gradient(circle_at_30%_30%,rgba(59,130,246,0.015),transparent_50%)] pointer-events-none"></div>
                    <div class="relative z-10">
                        <p class="text-[10px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">Panel de avance</p>
                        <h1 class="text-3xl font-black tracking-tight text-[var(--text-primary)] mt-3">Sigue cada importacion sin perderte</h1>
                        <p class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-2xl">
                            Aqui veras que esta listo, que falta y cual es el siguiente paso recomendado para cada importacion.
                        </p>
                    </div>
                    <div class="mt-8 flex flex-wrap gap-3 relative z-10">
                        <button onclick="window.location.href='evaluacion.jsp'" class="bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white text-xs font-black px-5 py-3 rounded-xl uppercase tracking-widest shadow-md transition-all active:scale-98">
                            Empezar nueva importacion
                        </button>
                        <button onclick="window.location.href='expediente-aduanero.jsp'" class="bg-[var(--surface-1)] hover:bg-[var(--surface-2)] text-[var(--text-primary)] border border-[var(--border)] text-xs font-black px-5 py-3 rounded-xl uppercase tracking-widest transition-all">
                            Ver documentos
                        </button>
                    </div>
                </div>

                <!-- KPI Panel -->
                <div class="glass-card bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-6 shadow-sm flex flex-col justify-between">
                    <p class="text-[10px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">Resumen rápido</p>
                    <div class="mt-5 space-y-4 flex-1 flex flex-col justify-center">
                        <div class="p-4 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)]">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--text-tertiary)]">Operaciones activas</p>
                            <p id="kpiActivas" class="text-3xl font-black text-[var(--text-primary)] mt-1.5 leading-none">0</p>
                        </div>
                        <div class="grid grid-cols-2 gap-4">
                            <div class="p-4 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)]">
                                <p class="text-[10px] font-black uppercase tracking-widest text-emerald-600">Listas</p>
                                <p id="kpiListas" class="text-2xl font-black text-emerald-600 mt-1.5 leading-none">0</p>
                            </div>
                            <div class="p-4 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)]">
                                <p class="text-[10px] font-black uppercase tracking-widest text-orange-600">Atención</p>
                                <p id="kpiAtencion" class="text-2xl font-black text-orange-600 mt-1.5 leading-none">0</p>
                            </div>
                        </div>
                    </div>
                </div>
            </section>

            <!-- Map board -->
            <section class="glass-card bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-6 shadow-sm">
                <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">Mapa vivo</p>
                        <h2 class="text-2xl font-black text-[var(--text-primary)] mt-1.5">Etapas de tus importaciones</h2>
                    </div>
                    <span id="seguimientoUpdatedAt" class="autosave-pill text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-600 px-3 py-1 rounded-full border border-emerald-100">Actualizando...</span>
                </div>
                <div id="stageBoard" class="stage-board mt-5 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                    <button type="button" class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] text-left opacity-40 animate-pulse h-28" aria-label="Cargando etapas"></button>
                    <button type="button" class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] text-left opacity-40 animate-pulse h-28" aria-label="Cargando etapas"></button>
                    <button type="button" class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] text-left opacity-40 animate-pulse h-28" aria-label="Cargando etapas"></button>
                    <button type="button" class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] text-left opacity-40 animate-pulse h-28" aria-label="Cargando etapas"></button>
                </div>
            </section>

            <!-- Operations list -->
            <section class="glass-card bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-6 shadow-sm">
                <div class="flex flex-col md:flex-row md:items-center justify-between border-b border-[var(--border)] pb-6 gap-4">
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.24em] text-[var(--accent)]">Tus operaciones</p>
                        <h2 class="text-2xl font-black text-[var(--text-primary)] mt-1.5">Estado actual y siguiente paso</h2>
                    </div>
                    <div class="flex flex-wrap gap-2">
                        <button class="filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--accent)] text-white shadow-sm" data-filter="ALL">Todas</button>
                        <button class="filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--surface-2)]" data-filter="PREPARACION">Preparacion</button>
                        <button class="filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--surface-2)]" data-filter="PERMISOS">Permisos</button>
                        <button class="filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--surface-2)]" data-filter="EXPEDIENTE">Documentos</button>
                        <button class="filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--surface-2)]" data-filter="LISTA">Lista</button>
                    </div>
                </div>

                <div id="seguimientoList" class="mt-6 space-y-4">
                    <div class="rounded-3xl border border-dashed border-[var(--border)] bg-[var(--surface-0)] p-8 text-center text-[var(--text-tertiary)] font-semibold">
                        Cargando seguimiento...
                    </div>
                </div>
            </section>
        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        const listContainer = document.getElementById('seguimientoList');
        const kpiActivas = document.getElementById('kpiActivas');
        const kpiListas = document.getElementById('kpiListas');
        const kpiAtencion = document.getElementById('kpiAtencion');
        const stageBoard = document.getElementById('stageBoard');
        const seguimientoUpdatedAt = document.getElementById('seguimientoUpdatedAt');
        let allRows = [];
        let activeFilter = 'ALL';

        const STAGE_CONFIG = [
            { key: 'PREPARACION', title: 'Preparacion', text: 'Falta producto, uso o codigo.' },
            { key: 'PERMISOS', title: 'Permisos', text: 'Hay una entidad por revisar.' },
            { key: 'EXPEDIENTE', title: 'Documentos', text: 'Falta ordenar archivos.' },
            { key: 'LISTA', title: 'Lista', text: 'Puede avanzar al cierre.' }
        ];

        function inferEntity(hsCode) {
            if (!hsCode) return 'Por confirmar';
            if (hsCode.startsWith('8517')) return 'MTC';
            if (hsCode.startsWith('2106') || hsCode.startsWith('1901')) return 'DIGESA';
            if (hsCode.startsWith('3004') || hsCode.startsWith('3304') || hsCode.startsWith('9018')) return 'DIGEMID';
            if (hsCode.startsWith('0602') || hsCode.startsWith('1209')) return 'SENASA';
            if (hsCode.startsWith('4407')) return 'SERFOR';
            return 'Sin alerta directa';
        }

        function classifyOperation(op) {
            const rawState = (op.estado || 'BORRADOR').toUpperCase();
            const hs = (op.hsCode || '').trim();
            const entity = inferEntity(hs);

            if (rawState === 'NACIONALIZADA') {
                return { stage: 'LISTA', label: 'Cerrada', note: 'La importacion ya fue cerrada o nacionalizada.', action: 'Ver revision', href: 'expediente-aduanero.jsp', progress: 100, tone: 'emerald' };
            }
            if (!hs) {
                return { stage: 'PREPARACION', label: 'En preparacion', note: 'Todavia falta completar producto, uso o codigo.', action: 'Continuar importacion', href: 'evaluacion.jsp', progress: 24, tone: 'cyan' };
            }
            if (entity !== 'Sin alerta directa' && rawState !== 'LISTA_DESPACHO') {
                return { stage: 'PERMISOS', label: 'Revisar permisos', note: 'El producto podria necesitar revision con ' + entity + '.', action: 'Revisar permisos', href: 'gestor_permisos.jsp', progress: 52, tone: 'orange' };
            }
            if (rawState === 'PENDIENTE_DOCS') {
                return { stage: 'EXPEDIENTE', label: 'Faltan documentos', note: 'Ya tienes datos base, pero falta ordenar documentos.', action: 'Subir documentos', href: 'documentos.jsp', progress: 76, tone: 'amber' };
            }
            if (rawState === 'BORRADOR' || rawState === 'COTIZACION') {
                return { stage: 'EXPEDIENTE', label: 'Completar documentos', note: 'Ya hay producto y codigo. Ahora ordena documentos y datos finales.', action: 'Subir documentos', href: 'documentos.jsp', progress: 64, tone: 'cyan' };
            }
            return { stage: 'LISTA', label: 'Lista para avanzar', note: 'La importacion ya puede pasar a revision final.', action: 'Ver revision', href: 'expediente-aduanero.jsp', progress: 92, tone: 'emerald' };
        }

        function toneClasses(tone) {
            if (tone === 'emerald') return { chip: 'bg-emerald-50 text-emerald-600 border-emerald-200 dark:bg-emerald-950/20 dark:text-emerald-400 dark:border-emerald-950/30', bar: 'from-emerald-400 to-emerald-500' };
            if (tone === 'amber' || tone === 'orange') return { chip: 'bg-amber-50 text-amber-600 border-amber-200 dark:bg-amber-950/20 dark:text-amber-400 dark:border-amber-950/30', bar: 'from-amber-300 to-orange-400' };
            return { chip: 'bg-blue-50 text-blue-600 border-blue-200 dark:bg-blue-950/20 dark:text-blue-400 dark:border-blue-950/30', bar: 'from-blue-400 to-indigo-500' };
        }

        function escapeHtml(value) {
            return String(value ?? '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#39;');
        }

        function renderStageBoard(rows) {
            if (!stageBoard) return;
            const total = rows.length || 1;
            stageBoard.innerHTML = STAGE_CONFIG.map(stage => {
                const count = rows.filter(item => item.meta.stage === stage.key).length;
                const percent = Math.round((count / total) * 100);
                const activeClass = activeFilter === stage.key ? ' border-[var(--accent)] ring-2 ring-[var(--accent-glow)]' : '';
                return `
                    <button type="button" class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] text-left flex flex-col justify-between transition-all hover:border-[var(--accent)] ${activeClass}" data-stage-filter="${stage.key}">
                        <div class="w-full">
                            <span class="text-[9px] font-black uppercase tracking-[0.2em] text-[var(--accent)]">${escapeHtml(stage.title)}</span>
                            <strong class="block text-2xl font-black text-[var(--text-primary)] mt-1">${count}</strong>
                            <span class="block text-[10px] text-[var(--text-secondary)] font-semibold mt-1 leading-normal">${escapeHtml(stage.text)}</span>
                        </div>
                        <span class="block w-full h-1 bg-[var(--border)] rounded-full overflow-hidden mt-3"><i class="block h-full bg-[var(--accent)]" style="width:${percent}%"></i></span>
                    </button>
                `;
            }).join('');

            stageBoard.querySelectorAll('[data-stage-filter]').forEach(btn => {
                btn.addEventListener('click', () => setActiveFilter(btn.dataset.stageFilter));
            });
        }

        function openDamHelp(importId, producto) {
            openKnowledgePanel('dam', {
                subtitulo: 'Explicacion simple del documento aduanero.',
                relacionConTuCaso: 'Estas revisando "' + producto + '". Antes de abrir el documento, entiende primero que campos importan y que todavia debes confirmar.',
                actionLabel: 'Ver ejemplo',
                actionHref: 'api/importacion/dam/descargar?id=' + importId
            });
        }

        function openStageHelp(stage, producto, entity) {
            if (stage === 'EXPEDIENTE') {
                openKnowledgePanel('factura_comercial', {
                    relacionConTuCaso: 'Tu importacion "' + producto + '" ya tiene datos base. Ahora toca ordenar factura, transporte y origen para avanzar sin observaciones.'
                });
                return;
            }
            if (stage === 'PERMISOS') {
                openKnowledgePanel('permiso_autorizacion', {
                    relacionConTuCaso: 'Tu importacion "' + producto + '" activa una revision con ' + entity + '. Lo importante es saber si solo debes revisar o si necesitas autorizacion antes de embarcar.'
                });
                return;
            }
            openKnowledgePanel('declaracion_aduanera', {
                relacionConTuCaso: 'Tu importacion "' + producto + '" todavia esta armando su ruta. Esta ficha explica como se conecta la declaracion con el resto del proceso.'
            });
        }

        function renderRows(rows) {
            renderStageBoard(rows);
            const visibleRows = activeFilter === 'ALL' ? rows : rows.filter(item => item.meta.stage === activeFilter);
            if (!visibleRows.length) {
                listContainer.innerHTML = '<div class="rounded-3xl border border-dashed border-[var(--border)] bg-[var(--surface-0)] p-8 text-center text-[var(--text-tertiary)] font-semibold"><p>No hay operaciones en esta vista todavía.</p><button onclick="window.location.href=\'evaluacion.jsp\'" class="inline-flex mt-4 px-5 py-3 rounded-2xl bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black text-xs uppercase tracking-widest shadow-md transition-all active:scale-98">Crear nueva ruta</button></div>';
                return;
            }

            listContainer.innerHTML = visibleRows.map(item => {
                const tone = toneClasses(item.meta.tone);
                const productText = escapeHtml(item.producto);
                const noteText = escapeHtml(item.meta.note);
                const hsText = escapeHtml(item.hs);
                const entityText = escapeHtml(item.entity);
                const cifText = escapeHtml(item.cif);
                const labelText = escapeHtml(item.meta.label);
                const actionText = escapeHtml(item.meta.action);
                return `
                    <article class="rounded-3xl border border-[var(--border)] bg-[var(--surface-1)] p-6 shadow-sm relative overflow-hidden transition-all hover:border-[var(--accent)]/40 hover:shadow-md">
                        <div class="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
                            <div class="min-w-0 flex-1">
                                <div class="flex items-center gap-3 flex-wrap">
                                    <span class="px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest border ${tone.chip}">${labelText}</span>
                                    <span class="text-[9px] font-black uppercase tracking-[0.24em] text-[var(--text-tertiary)]">Operación #${item.id}</span>
                                </div>
                                <h3 class="text-xl font-black text-[var(--text-primary)] mt-3">${productText}</h3>
                                <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">${noteText}</p>
                                <div class="mt-4 grid grid-cols-1 sm:grid-cols-3 gap-3 text-xs font-semibold text-[var(--text-secondary)]">
                                    <div class="rounded-xl bg-[var(--surface-0)] border border-[var(--border)] px-4 py-2.5">
                                        <span class="block text-[9px] font-black uppercase tracking-widest text-[var(--accent)]">Codigo</span>
                                        <span class="block mt-1 font-mono text-[var(--text-primary)] font-bold">${hsText}</span>
                                    </div>
                                    <div class="rounded-xl bg-[var(--surface-0)] border border-[var(--border)] px-4 py-2.5">
                                        <span class="block text-[9px] font-black uppercase tracking-widest text-[var(--accent)]">Quien revisa</span>
                                        <span class="block mt-1 text-[var(--text-primary)] font-bold">${entityText}</span>
                                    </div>
                                    <div class="rounded-xl bg-[var(--surface-0)] border border-[var(--border)] px-4 py-2.5">
                                        <span class="block text-[9px] font-black uppercase tracking-widest text-[var(--accent)]">Costo base</span>
                                        <span class="block mt-1 font-mono text-[var(--text-primary)] font-bold">$${cifText}</span>
                                    </div>
                                </div>
                            </div>
                            <div class="lg:w-72 shrink-0">
                                <div class="rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] p-5">
                                    <div class="flex items-center justify-between text-[10px] font-black uppercase tracking-widest text-[var(--text-tertiary)]">
                                        <span>Avance</span>
                                        <span class="text-[var(--text-primary)]">${item.meta.progress}%</span>
                                    </div>
                                    <div class="mt-2.5 h-2 rounded-full bg-[var(--surface-2)] border border-[var(--border)] overflow-hidden">
                                        <div class="h-full rounded-full bg-gradient-to-r ${tone.bar}" style="width:${item.meta.progress}%"></div>
                                    </div>
                                    <a href="${item.meta.href}${item.meta.href.includes('?') ? '&' : '?'}operacionId=${item.id}" class="mt-5 flex items-center justify-center px-4 py-3 rounded-xl bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black text-xs uppercase tracking-widest shadow-md transition-all active:scale-98">
                                        ${actionText}
                                    </a>
                                    <div class="mt-3 grid grid-cols-1 gap-2">
                                        <button type="button" data-action="dam" data-id="${item.id}" data-product="${productText}" class="px-4 py-2 rounded-xl bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black text-[10px] uppercase tracking-widest hover:bg-[var(--surface-2)] transition-all">
                                            Entender documento
                                        </button>
                                        <button type="button" data-action="stage" data-stage="${item.meta.stage}" data-product="${productText}" data-entity="${entityText}" class="px-4 py-2 rounded-xl bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-primary)] font-black text-[10px] uppercase tracking-widest hover:bg-[var(--surface-2)] transition-all">
                                            Que me falta
                                        </button>
                                        <a href="expediente-aduanero.jsp?operacionId=${item.id}" class="px-4 py-2 rounded-xl bg-[var(--surface-1)] border border-[var(--border)] text-center text-[var(--text-primary)] font-black text-[10px] uppercase tracking-widest hover:bg-[var(--surface-2)] transition-all">
                                            Ver revision
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </article>
                `;
            }).join('');

            listContainer.querySelectorAll('[data-action="dam"]').forEach((btn) => {
                btn.addEventListener('click', () => {
                    openDamHelp(Number(btn.dataset.id), btn.dataset.product || 'esta operacion');
                });
            });
            listContainer.querySelectorAll('[data-action="stage"]').forEach((btn) => {
                btn.addEventListener('click', () => {
                    openStageHelp(btn.dataset.stage, btn.dataset.product || 'esta operacion', btn.dataset.entity || 'la entidad');
                });
            });
        }

        function updateKpis(rows) {
            kpiActivas.textContent = rows.length;
            kpiListas.textContent = rows.filter(item => item.meta.stage === 'LISTA').length;
            kpiAtencion.textContent = rows.filter(item => item.meta.stage !== 'LISTA').length;
        }

        function setActiveFilter(filter) {
            activeFilter = filter || 'ALL';
            document.querySelectorAll('.filter-btn').forEach(item => {
                const isActive = item.dataset.filter === activeFilter;
                item.className = isActive
                    ? 'filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--accent)] text-white shadow-sm'
                    : 'filter-btn px-4 py-2 rounded-full text-[10px] font-black uppercase tracking-wider bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-secondary)] hover:bg-[var(--surface-2)]';
            });
            renderRows(allRows);
        }

        function loadSeguimiento() {
            fetch('api/importacion/listar')
                .then(res => {
                    if (!res.ok) throw new Error('No se pudo cargar el seguimiento.');
                    return res.json();
                })
                .then(data => {
                    if (!Array.isArray(data)) throw new Error('Respuesta inesperada.');
                    allRows = (data || []).map(op => {
                        const meta = classifyOperation(op);
                        return {
                            id: op.id,
                            producto: op.productoDesc || 'Producto sin descripción',
                            hs: op.hsCode || 'Pendiente',
                            entity: inferEntity(op.hsCode || ''),
                            cif: Number(op.valorCif || 0).toFixed(2),
                            meta
                        };
                    });
                    updateKpis(allRows);
                    if (seguimientoUpdatedAt) {
                        seguimientoUpdatedAt.textContent = 'Actualizado ' + new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit' }).format(new Date());
                    }
                    renderRows(allRows);
                })
                .catch(() => {
                    renderStageBoard([]);
                    if (seguimientoUpdatedAt) seguimientoUpdatedAt.textContent = 'Sin conexión';
                    listContainer.innerHTML = '<div class="rounded-3xl border border-red-200 bg-red-50/20 p-8 text-center text-[var(--text-secondary)] font-semibold"><p class="text-lg font-black text-[var(--text-primary)]">No pudimos cargar el seguimiento.</p><p class="text-xs mt-2 text-[var(--text-secondary)]">Vuelve a intentar en unos segundos o retoma tu importación desde el panel principal.</p><button onclick="window.location.href=\'dashboard.jsp\'" class="inline-flex mt-5 px-5 py-3 rounded-2xl bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black text-xs uppercase tracking-widest shadow-md">Ir al inicio</button></div>';
                });
        }

        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                setActiveFilter(btn.dataset.filter);
            });
        });

        document.addEventListener('DOMContentLoaded', loadSeguimiento);
    </script>
</body>
</html>

