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
    <title>ImportEase - Quien paga el envio</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .risk-verde { background: var(--success-soft); color: var(--success); border-color: var(--success); }
        .risk-amarillo { background: var(--warning-soft); color: var(--warning); border-color: var(--warning); }
        .risk-rojo { background: var(--danger-soft); color: var(--danger); border-color: var(--danger); }
        .incoterm-pill.is-active { background: var(--accent); color: white; border-color: transparent; }
        input:focus, select:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <% request.setAttribute("activePage", "incoterms"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl sticky top-0 z-10 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Quien paga el envio</span>
            </div>
            <button id="btnBackWizard" class="px-4 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black text-[var(--text-secondary)] uppercase tracking-widest hover:text-[var(--accent)] transition-all cursor-pointer">Volver a importar</button>
        </header>

        <!-- Main Workspace Contents -->
        <section class="p-8 xl:p-12 max-w-7xl mx-auto w-full space-y-8 flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest mb-4">&larr; Volver a Importar paso a paso</a>
            <div class="grid grid-cols-1 xl:grid-cols-[1fr_24rem] gap-8">
                <section class="glass-card p-8">
                    <span class="pill-heading">Aprende decidiendo</span>
                    <h1 class="text-4xl font-black tracking-tight mt-3">Tu proveedor ofrece hacerse cargo de parte del envio. Revisa si te conviene.</h1>
                    <p class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-3xl">Compara quien paga envio, seguro y entrega. El sistema conserva los codigos tecnicos, pero te explica la decision en lenguaje simple.</p>

                    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-3 mt-8">
                        <button class="case-btn rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-left transition-all hover:border-[var(--accent)] hover:-translate-y-0.5 cursor-pointer" data-case="cif">
                            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Caso 1</p>
                            <p class="font-black mt-1 text-[var(--text-primary)]">Proveedor incluye envio a Callao</p>
                        </button>
                        <button class="case-btn rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-left transition-all hover:border-[var(--accent)] hover:-translate-y-0.5 cursor-pointer" data-case="courier">
                            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Caso 2</p>
                            <p class="font-black mt-1 text-[var(--text-primary)]">Traer por courier</p>
                        </button>
                        <button class="case-btn rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-left transition-all hover:border-[var(--accent)] hover:-translate-y-0.5 cursor-pointer" data-case="sorpresas">
                            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Caso 3</p>
                            <p class="font-black mt-1 text-[var(--text-primary)]">Evitar sorpresas</p>
                        </button>
                        <button class="case-btn rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-left transition-all hover:border-[var(--accent)] hover:-translate-y-0.5 cursor-pointer" data-case="nose">
                            <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Caso 4</p>
                            <p class="font-black mt-1 text-[var(--text-primary)]">No estoy seguro</p>
                        </button>
                    </div>
                </section>

                <aside class="glass-card p-6 space-y-4">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Referencia</p>
                    <span class="source-chip source-chip--bd">ICC 2020 REFERENCIAL</span>
                    <p class="text-xs text-[var(--text-secondary)] font-semibold">Usamos nombres tecnicos como referencia, pero la pantalla se centra en la pregunta practica: quien paga y que debes pedir.</p>
                    <a href="https://iccwbo.org/business-solutions/incoterms-rules/" target="_blank" class="inline-flex px-4 py-2 rounded-xl bg-[var(--surface-2)] border border-[var(--border)] text-[10px] font-black uppercase tracking-widest text-[var(--accent)] transition-all hover:bg-[var(--accent-glow)]">Ver ICC</a>
                </aside>
            </div>

            <div class="grid grid-cols-1 xl:grid-cols-[24rem_1fr] gap-8">
                <section class="glass-card p-6 space-y-5">
                    <div>
                        <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Datos de tu caso</p>
                        <h2 class="text-2xl font-black mt-1">Simulador</h2>
                    </div>

                    <div class="grid grid-cols-3 gap-2" id="incotermPills"></div>

                    <label class="block space-y-2">
                        <span class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Precio del producto USD</span>
                        <input id="fobInput" type="number" min="0" step="0.01" value="5000" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-mono font-bold text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-all">
                    </label>
                    <label class="block space-y-2">
                        <span class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Envio internacional USD</span>
                        <input id="fleteInput" type="number" min="0" step="0.01" value="450" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-mono font-bold text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-all">
                    </label>
                    <label class="block space-y-2">
                        <span class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Seguro USD</span>
                        <input id="seguroInput" type="number" min="0" step="0.01" value="80" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-mono font-bold text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-all">
                    </label>
                    <div class="grid grid-cols-2 gap-3">
                        <label class="block space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Tipo</span>
                            <select id="tipoInput" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-bold text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-all cursor-pointer">
                                <option value="COMERCIAL">Comercial</option>
                                <option value="PERSONAL">Personal</option>
                            </select>
                        </label>
                        <label class="block space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Producto</span>
                            <select id="restringidoInput" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] font-bold text-[var(--text-primary)] focus:outline-none focus:border-[var(--accent)] transition-all cursor-pointer">
                                <option value="false">Sin permiso evidente</option>
                                <option value="true">Podria requerir permiso</option>
                            </select>
                        </label>
                    </div>
                    <button id="btnSimular" class="primary-button w-full text-xs font-black uppercase tracking-widest py-3">Comparar responsabilidad</button>
                </section>

                <section class="space-y-6">
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
                        <div class="glass-card p-6">
                            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Opcion elegida</p>
                            <p id="resIncoterm" class="text-4xl font-black mt-2 text-[var(--text-primary)]">FOB</p>
                            <p id="resNombre" class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Free On Board</p>
                        </div>
                        <div class="glass-card p-6">
                            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Base para impuestos</p>
                            <p id="resCif" class="text-4xl font-black mt-2 text-[var(--text-primary)]">$0</p>
                            <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Producto + envio + seguro</p>
                        </div>
                        <div class="glass-card p-6">
                            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Nivel de cuidado</p>
                            <span id="riskChip" class="inline-flex mt-3 px-4 py-1.5 rounded-full border text-xs font-black uppercase tracking-widest risk-verde">VERDE</span>
                            <p id="riskText" class="text-xs text-[var(--text-secondary)] font-semibold mt-3">Listo para revisar.</p>
                        </div>
                    </div>

                    <div class="glass-card p-8">
                        <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Responsabilidades</p>
                        <h2 class="text-2xl font-black mt-1">Quien paga y que debes pedir</h2>
                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mt-6">
                            <div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-5">
                                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Envio</p>
                                <p id="resFlete" class="font-black mt-2 text-[var(--text-primary)]">-</p>
                            </div>
                            <div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-5">
                                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Seguro</p>
                                <p id="resSeguro" class="font-black mt-2 text-[var(--text-primary)]">-</p>
                            </div>
                            <div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-5">
                                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Recomendación</p>
                                <p id="resReco" class="font-black mt-2 text-[var(--text-primary)]">-</p>
                            </div>
                        </div>
                        <p id="resLectura" class="text-sm text-[var(--text-secondary)] font-semibold mt-6"></p>
                        <div id="checklist" class="grid grid-cols-1 md:grid-cols-2 gap-3 mt-5"></div>
                        <div class="mt-6 flex flex-wrap gap-3">
                            <button id="btnUsarWizard" class="primary-button px-6 text-xs font-black uppercase tracking-widest py-3.5">Usar esta opcion en mi importacion</button>
                            <a href="evaluacion.jsp" class="px-6 py-3.5 rounded-xl bg-white border border-[var(--border)] text-xs font-black uppercase tracking-widest text-[var(--text-secondary)] hover:bg-[var(--surface-2)] transition-all cursor-pointer text-center">Solo volver</a>
                        </div>
                    </div>

                    <div class="glass-card p-8">
                        <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Comparador rapido</p>
                        <div id="incotermList" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-3 mt-5"></div>
                    </div>
                </section>
            </div>
        </section>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
        const RETURN_KEY = 'importease_wizard_incoterm_decision';
        let selectedIncoterm = 'FOB';
        let lastResult = null;
        let catalog = [];

        const params = new URLSearchParams(location.search);
        document.getElementById('fobInput').value = params.get('fob') || '5000';
        document.getElementById('fleteInput').value = params.get('flete') || '450';
        document.getElementById('seguroInput').value = params.get('seguro') || '80';
        document.getElementById('tipoInput').value = params.get('tipo') || 'COMERCIAL';
        selectedIncoterm = (params.get('incoterm') || 'FOB').toUpperCase();

        function money(value) {
            return '$ ' + Number(value || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }

        function body() {
            return {
                incoterm: selectedIncoterm,
                fob: Number(document.getElementById('fobInput').value || 0),
                flete: Number(document.getElementById('fleteInput').value || 0),
                seguro: Number(document.getElementById('seguroInput').value || 0),
                tipoImportacion: document.getElementById('tipoInput').value,
                productoRestringido: document.getElementById('restringidoInput').value === 'true'
            };
        }

        async function cargarCatalogo() {
            const res = await fetch(window.ctx + '/api/incoterms/listar');
            const payload = await res.json();
            catalog = payload.data || [];
            renderPills();
            renderCatalog();
            simular();
        }

        function renderPills() {
            const box = document.getElementById('incotermPills');
            box.innerHTML = '';
            ['FOB','CIF','CFR','CIP','DAP','DDP'].forEach(code => {
                const btn = document.createElement('button');
                btn.className = 'incoterm-pill rounded-xl border px-3 py-2 text-xs font-black transition-all ' + (selectedIncoterm === code ? 'is-active cursor-pointer' : 'bg-white border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] cursor-pointer');
                btn.textContent = code;
                btn.onclick = () => { selectedIncoterm = code; renderPills(); simular(); };
                box.appendChild(btn);
            });
        }

        function renderCatalog() {
            const box = document.getElementById('incotermList');
            box.innerHTML = '';
            catalog.filter(i => ['FOB','CIF','CFR','CIP','DAP','DDP'].includes(i.codigo)).forEach(item => {
                const div = document.createElement('button');
                div.className = 'text-left rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 hover:border-[var(--accent)] transition-all cursor-pointer';
                div.innerHTML = '<p class="font-black text-[var(--text-primary)]">' + item.codigo + ' - ' + escapeHtml(item.nombre) + '</p>' +
                    '<p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">' + escapeHtml(item.descripcion) + '</p>';
                div.onclick = () => { selectedIncoterm = item.codigo; renderPills(); simular(); };
                box.appendChild(div);
            });
        }

        async function simular() {
            const res = await fetch(window.ctx + '/api/incoterms/simular', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': window.csrfToken || '' },
                body: JSON.stringify(body())
            });
            const payload = await res.json();
            if (payload.data && payload.data.csrfToken) window.csrfToken = payload.data.csrfToken;
            if (!payload.success) return;
            renderResult(payload.data || {});
        }

        function renderResult(data) {
            lastResult = data;
            selectedIncoterm = data.incoterm || selectedIncoterm;
            document.getElementById('resIncoterm').textContent = data.incoterm || '-';
            document.getElementById('resNombre').textContent = data.nombre || '-';
            document.getElementById('resCif').textContent = money(data.valorCifAduana);
            document.getElementById('resFlete').textContent = data.quienPagaFlete || '-';
            document.getElementById('resSeguro').textContent = data.quienPagaSeguro || '-';
            document.getElementById('resReco').textContent = (data.recomendacion?.incoterm || '-') + ': ' + (data.recomendacion?.motivo || '');
            document.getElementById('resLectura').textContent = data.lectura || '';
            const risk = (data.riesgo?.nivel || 'VERDE').toLowerCase();
            const chip = document.getElementById('riskChip');
            chip.className = 'inline-flex mt-3 px-4 py-1.5 rounded-full border text-xs font-black uppercase tracking-widest risk-' + risk;
            chip.textContent = (data.riesgo?.nivel || 'VERDE');
            document.getElementById('riskText').textContent = data.riesgo?.mensaje || '';

            const list = document.getElementById('checklist');
            list.innerHTML = '';
            (data.checklist || []).forEach(item => {
                const div = document.createElement('div');
                div.className = 'rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 text-xs text-[var(--text-secondary)] font-semibold';
                div.textContent = item;
                list.appendChild(div);
            });
            renderPills();
        }

        async function usarEnWizard() {
            if (!lastResult) await simular();
            const decision = Object.assign({}, lastResult || {}, body());
            localStorage.setItem(RETURN_KEY, JSON.stringify(decision));
            try {
                const res = await fetch(window.ctx + '/api/incoterms/guardar-decision', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': window.csrfToken || '' },
                    body: JSON.stringify(decision)
                });
                const payload = await res.json();
                if (payload.data && payload.data.csrfToken) window.csrfToken = payload.data.csrfToken;
            } catch (e) {}
            window.location.href = 'evaluacion.jsp?step=4';
        }

        function applyCase(type) {
            if (type === 'cif') selectedIncoterm = 'CIF';
            if (type === 'courier') { selectedIncoterm = 'CIP'; document.getElementById('tipoInput').value = 'PERSONAL'; }
            if (type === 'sorpresas') selectedIncoterm = 'FOB';
            if (type === 'nose') selectedIncoterm = 'NO_SE';
            renderPills();
            simular();
        }

        function escapeHtml(value) {
            return String(value || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
        }

        document.getElementById('btnSimular').addEventListener('click', simular);
        document.getElementById('btnUsarWizard').addEventListener('click', usarEnWizard);
        document.getElementById('btnBackWizard').addEventListener('click', () => location.href = 'evaluacion.jsp?step=4');
        document.querySelectorAll('.case-btn').forEach(btn => btn.addEventListener('click', () => applyCase(btn.dataset.case)));
        ['fobInput','fleteInput','seguroInput','tipoInput','restringidoInput'].forEach(id => {
            document.getElementById(id).addEventListener('change', simular);
        });
        cargarCatalogo();
    </script>
</body>
</html>
