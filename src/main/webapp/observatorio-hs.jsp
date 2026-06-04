<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Observatorio HS</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .trend-bar { transition: width .45s var(--ease-out-expo); }
        .table-row:hover { background: var(--surface-2); }
        input:focus, select:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <% request.setAttribute("activePage", "observatorio"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl sticky top-0 z-10 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Observatorio comercial por HS</span>
            </div>
            <a href="buscador.jsp" class="px-4 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black text-[var(--text-secondary)] uppercase tracking-widest hover:text-[var(--accent)] transition-all cursor-pointer">Volver al buscador</a>
        </header>

        <!-- Main Content Workspace -->
        <section class="p-8 xl:p-12 max-w-7xl mx-auto w-full space-y-8 flex-1">
            <div class="grid grid-cols-1 xl:grid-cols-[1fr_22rem] gap-8">
                <div class="glass-card p-8">
                    <div class="flex flex-col md:flex-row md:items-end md:justify-between gap-6">
                        <div>
                            <span class="pill-heading">Módulo de Inteligencia</span>
                            <h1 class="text-4xl font-black tracking-tight mt-3">Observatorio HS</h1>
                            <p class="text-sm text-[var(--text-secondary)] font-semibold mt-2 max-w-3xl">Analiza proveedores, tendencia y oportunidad comercial usando UN Comtrade cuando haya credenciales. Si no hay API key, el sistema lo etiqueta con claridad.</p>
                        </div>
                        <div class="flex gap-3 shrink-0">
                            <input id="hsInput" class="min-w-0 w-52 px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-mono font-black text-sm focus:outline-none focus:border-[var(--accent)] text-[var(--text-primary)] transition-all" placeholder="8517130000">
                            <button id="btnAnalizar" class="primary-button text-xs font-black uppercase tracking-widest py-3 px-6">Analizar</button>
                        </div>
                    </div>
                </div>

                <aside class="glass-card p-6 space-y-4">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Fuente y confianza</p>
                    <span id="sourceChip" class="source-chip source-chip--pending">Cargando</span>
                    <div class="space-y-2 text-xs font-semibold text-[var(--text-secondary)]">
                        <p id="sourceMeta">Preparando consulta...</p>
                        <p id="updatedAt">Actualizado: -</p>
                    </div>
                </aside>
            </div>

            <div id="credentialsNotice" class="hidden rounded-2xl border border-[var(--warning)] bg-[var(--warning-soft)] px-5 py-4 text-sm font-semibold text-[var(--warning)]">
                UN Comtrade requiere `UN_COMTRADE_KEY` para consulta en vivo. Esta vista usa cache referencial etiquetado y no lo presenta como dato oficial.
            </div>

            <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
                <div class="glass-card p-6">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Mercado estimado</p>
                    <p id="marketValue" class="text-3xl font-black mt-3">$0</p>
                    <p id="marketYear" class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Periodo: -</p>
                </div>
                <div class="glass-card p-6">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Oportunidad</p>
                    <p id="scoreValue" class="text-3xl font-black mt-3">0</p>
                    <p id="scoreLevel" class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Nivel: -</p>
                </div>
                <div class="glass-card p-6">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Proveedor lider</p>
                    <p id="topCountry" class="text-2xl font-black mt-3 truncate">-</p>
                    <p id="topShare" class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Participacion: -</p>
                </div>
                <div class="glass-card p-6">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Riesgo concentracion</p>
                    <p id="riskValue" class="text-2xl font-black mt-3">-</p>
                    <p id="trendValue" class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Tendencia: -</p>
                </div>
            </div>

            <div class="grid grid-cols-1 xl:grid-cols-[1fr_24rem] gap-8">
                <section class="glass-card p-8">
                    <div class="flex items-center justify-between gap-4 border-b border-[var(--border)] pb-4">
                        <div>
                            <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Top paises proveedores</p>
                            <h2 class="text-2xl font-black mt-1">Origenes para Peru</h2>
                        </div>
                        <span id="hsBadge" class="px-3 py-1 rounded-full bg-[var(--accent-soft)] text-[var(--accent)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-widest">HS</span>
                    </div>
                    <div class="overflow-x-auto mt-5">
                        <table class="w-full text-left">
                            <thead>
                                <tr class="text-[10px] uppercase tracking-[0.2em] text-[var(--text-tertiary)]">
                                    <th class="py-3">Pais</th>
                                    <th class="py-3">Valor USD</th>
                                    <th class="py-3">Participacion</th>
                                </tr>
                            </thead>
                            <tbody id="countryRows" class="text-sm font-semibold text-[var(--text-secondary)]"></tbody>
                        </table>
                    </div>
                </section>

                <section class="glass-card p-8">
                    <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Tendencia anual</p>
                    <div id="trendRows" class="space-y-4 mt-6"></div>
                </section>
            </div>

            <section class="glass-card p-8">
                <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Lectura comercial</p>
                <h2 id="recommendationTitle" class="text-2xl font-black mt-1">Selecciona un HS para analizar</h2>
                <p id="recommendationText" class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-4xl">El observatorio conectara tu busqueda HS con datos comerciales y dejara claro si vienen de API oficial, cache o credenciales pendientes.</p>
                <ul id="notesList" class="mt-5 grid grid-cols-1 md:grid-cols-3 gap-3 text-xs text-[var(--text-secondary)] font-semibold"></ul>
            </section>
        </section>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';

        function sourceLabel(type) {
            const labels = {
                OFICIAL_API: 'OFICIAL/API',
                OFICIAL_WEB: 'OFICIAL/WEB',
                TERCERO_API: 'TERCERO',
                BD_LOCAL: 'BD LOCAL',
                ESTIMADO: 'ESTIMADO',
                SIMULADO: 'SIMULADO',
                FALLBACK: 'FALLBACK',
                CACHE: 'CACHE',
                MANUAL_VERIFICADO: 'MANUAL VERIFICADO',
                PENDIENTE_CREDENCIALES: 'PENDIENTE CREDENCIALES',
                PENDIENTE_VALIDACION: 'PENDIENTE VALIDACION'
            };
            return labels[(type || '').toUpperCase()] || String(type || 'PENDIENTE').replace(/_/g, ' ');
        }

        function chipClass(type) {
            const value = (type || '').toUpperCase();
            if (value.includes('OFICIAL')) return 'source-chip source-chip--official';
            if (value === 'CACHE') return 'source-chip source-chip--cache';
            if (value.includes('MANUAL') || value === 'ESTIMADO') return 'source-chip source-chip--manual';
            if (value === 'FALLBACK' || value === 'SIMULADO' || value.includes('PENDIENTE')) return 'source-chip source-chip--pending';
            return 'source-chip source-chip--bd';
        }

        function money(value) {
            const n = Number(value || 0);
            if (n >= 1000000) return '$' + (n / 1000000).toFixed(1) + 'M';
            if (n >= 1000) return '$' + (n / 1000).toFixed(1) + 'K';
            return '$' + Math.round(n).toLocaleString('es-PE');
        }

        let observatorioAbortController = null;

        async function cargarObservatorio() {
            const input = document.getElementById('hsInput');
            const codigo = (input.value || '').replace(/[^0-9]/g, '') || '8517130000';
            input.value = codigo;

            if (observatorioAbortController) {
                observatorioAbortController.abort();
            }
            observatorioAbortController = new AbortController();

            document.getElementById('btnAnalizar').disabled = true;
            document.getElementById('btnAnalizar').textContent = 'Analizando';
            try {
                const res = await fetch(window.ctx + '/api/observatorio/hs?codigo=' + encodeURIComponent(codigo), {
                    signal: observatorioAbortController.signal
                });
                const payload = await res.json();
                if (!payload.success) throw new Error(payload.message || 'No se pudo analizar');
                render(payload.data || {});
            } catch (e) {
                if (e.name === 'AbortError') return;
                document.getElementById('recommendationTitle').textContent = 'No se pudo cargar observatorio';
                document.getElementById('recommendationText').textContent = e.message || 'Revisa sesion y conexion.';
            } finally {
                document.getElementById('btnAnalizar').disabled = false;
                document.getElementById('btnAnalizar').textContent = 'Analizar';
            }
        }

        function render(data) {
            const sourceType = data.sourceType || 'CACHE';
            const chip = document.getElementById('sourceChip');
            chip.className = chipClass(sourceType);
            chip.textContent = sourceLabel(sourceType);
            document.getElementById('sourceMeta').textContent = 'Fuente: ' + (data.source || 'UN_COMTRADE_API') + ' | Confianza ' + Math.round((Number(data.confidence) || 0) * 100) + '%';
            document.getElementById('updatedAt').textContent = 'Actualizado: ' + (data.updatedAt || '-');
            document.getElementById('credentialsNotice').classList.toggle('hidden', !data.faltanCredenciales);
            document.getElementById('hsBadge').textContent = 'HS ' + (data.hs6 || data.hsCode || '-');

            const mercado = data.mercado || {};
            const oportunidad = data.oportunidad || {};
            const top = data.topOrigenes || [];
            document.getElementById('marketValue').textContent = money(mercado.valorUsd);
            document.getElementById('marketYear').textContent = 'Periodo: ' + (mercado.anio || '-');
            document.getElementById('scoreValue').textContent = oportunidad.score || 0;
            document.getElementById('scoreLevel').textContent = 'Nivel: ' + (oportunidad.nivel || '-');
            document.getElementById('topCountry').textContent = top[0]?.pais || '-';
            document.getElementById('topShare').textContent = 'Participacion: ' + (top[0]?.participacion ?? '-') + '%';
            document.getElementById('riskValue').textContent = oportunidad.riesgoConcentracion || '-';
            document.getElementById('trendValue').textContent = 'Tendencia: ' + (oportunidad.tendencia || '-');

            const rows = document.getElementById('countryRows');
            rows.innerHTML = '';
            top.forEach(item => {
                const tr = document.createElement('tr');
                tr.className = 'table-row border-b border-[var(--border)] transition-colors';
                tr.innerHTML = '<td class="py-3 pr-4">' + escapeHtml(item.pais) + '</td>' +
                    '<td class="py-3 pr-4 font-mono font-bold text-[var(--text-primary)]">' + money(item.valorUsd) + '</td>' +
                    '<td class="py-3 font-semibold text-[var(--text-secondary)]">' + Number(item.participacion || 0).toFixed(1) + '%</td>';
                rows.appendChild(tr);
            });

            const trend = data.tendencia || [];
            const max = Math.max(...trend.map(i => Number(i.valorUsd || 0)), 1);
            const trendRows = document.getElementById('trendRows');
            trendRows.innerHTML = '';
            trend.forEach(item => {
                const pct = Math.max(4, Number(item.valorUsd || 0) * 100 / max);
                const div = document.createElement('div');
                div.innerHTML = '<div class="flex justify-between text-xs font-black text-[var(--text-secondary)] mb-2"><span>' + item.anio + '</span><span class="font-mono">' + money(item.valorUsd) + '</span></div>' +
                    '<div class="h-3 rounded-full bg-[var(--surface-2)] border border-[var(--border)] overflow-hidden"><div class="trend-bar h-full rounded-full bg-[var(--accent)]" style="width:' + pct + '%"></div></div>';
                trendRows.appendChild(div);
            });

            document.getElementById('recommendationTitle').textContent = 'Oportunidad ' + (oportunidad.nivel || 'por evaluar') + ' para HS ' + (data.hs6 || '-');
            document.getElementById('recommendationText').textContent = oportunidad.justificacion || 'Valida precio, permisos y proveedor antes de comprar.';
            const notes = document.getElementById('notesList');
            notes.innerHTML = '';
            (data.notes || []).forEach(note => {
                const li = document.createElement('li');
                li.className = 'rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-[var(--text-secondary)] font-semibold';
                li.textContent = note;
                notes.appendChild(li);
            });
        }

        function escapeHtml(value) {
            return String(value || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
        }

        document.getElementById('btnAnalizar').addEventListener('click', cargarObservatorio);
        document.getElementById('hsInput').addEventListener('keydown', e => { if (e.key === 'Enter') cargarObservatorio(); });
        const param = new URLSearchParams(location.search).get('codigo');
        document.getElementById('hsInput').value = param || '8517130000';
        cargarObservatorio();
    </script>
</body>
</html>
