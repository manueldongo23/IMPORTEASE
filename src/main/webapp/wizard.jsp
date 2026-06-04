<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
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
    <title>ImportEase - Wizard Guiado</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .w-step { transition: all 0.3s cubic-bezier(0.4,0,0.2,1); }
        .w-step.active { transform: scale(1.05); box-shadow: 0 0 0 3px var(--accent); }
        .w-step.completed { background: var(--accent) !important; color: white !important; border-color: transparent !important; }
        .w-step.blocked { background: #ef4444 !important; color: white !important; }
        .w-help { border-left: 3px solid var(--accent); background: color-mix(in srgb, var(--accent) 6%, transparent); }
        .severity-chip { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 10px; font-weight: 800; text-transform: uppercase; letter-spacing: 0.05em; }
        .severity-chip.OK { background: #dcfce7; color: #166534; }
        .severity-chip.MEDIO { background: #fef9c3; color: #854d0e; }
        .severity-chip.ALTO { background: #fed7aa; color: #9a3412; }
        .severity-chip.CRITICO { background: #fecaca; color: #991b1b; }
        .severity-chip.PENDIENTE { background: #e5e7eb; color: #374151; }
        .source-chip { display: inline-block; padding: 1px 8px; border-radius: 4px; font-size: 9px; font-weight: 700; text-transform: uppercase; }
        .source-chip.oficial { background: #dbeafe; color: #1e40af; }
        .source-chip.referencial { background: #fef3c7; color: #92400e; }
        .source-chip.simulado { background: #fce4ec; color: #b71c1c; }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "aduanas"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar">
        <header class="h-16 border-b border-[var(--border)] px-8 flex items-center justify-between bg-white/70 backdrop-blur-xl sticky top-0 z-10">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Wizard Guiado</span>
            </div>
            <div class="flex items-center gap-3">
                <a href="expediente-aduanero.jsp" class="soft-button text-xs">Volver al expediente</a>
            </div>
        </header>

        <section class="p-8 xl:p-10 max-w-7xl mx-auto space-y-7">
            <div class="glass-card hero-banner p-8">
                <p class="pill-heading">Flujo guiado de importacion</p>
                <h1 id="wizardTitle" class="text-4xl font-black tracking-tight mt-3">Prepara tu importacion paso a paso</h1>
                <p id="wizardSubtitle" class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-3xl">
                    Sigue los 8 pasos para completar tu expediente aduanero.
                </p>

                <div id="stepRail" class="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-8 gap-2 mt-7"></div>

                <div class="flex items-center gap-4 mt-4">
                    <select id="operacionSelect" class="custom-input px-4 py-2.5 rounded-2xl bg-white border border-[var(--border)] text-xs font-black text-[var(--text-secondary)] min-w-[18rem]">
                        <option value="">Selecciona un expediente</option>
                    </select>
                    <button id="btnRefresh" class="primary-button text-xs">Actualizar</button>
                </div>
            </div>

            <section class="grid grid-cols-1 xl:grid-cols-[1fr_22rem] gap-6">
                <div class="space-y-6">
                    <div id="stepContent" class="glass-card section-shell p-8">
                        <div class="flex items-center justify-center min-h-[12rem] text-[var(--text-secondary)]">
                            Selecciona un expediente para comenzar
                        </div>
                    </div>

                    <div id="helpBox" class="w-help p-6 rounded-2xl hidden">
                        <p id="helpTitle" class="font-black text-sm text-[var(--accent)]">Ayuda contextual</p>
                        <p id="helpDesc" class="text-sm mt-1"></p>
                    </div>

                    <div id="coherenciaPanel" class="hidden space-y-3"></div>
                </div>

                <aside class="space-y-6">
                    <div id="healthPanel" class="glass-card section-shell p-6">
                        <p class="pill-heading">Estado del expediente</p>
                        <div id="healthContent" class="mt-4 space-y-3 text-sm">
                            <p class="text-[var(--text-secondary)]">Cargando...</p>
                        </div>
                    </div>

                    <div id="nextActionPanel" class="glass-card section-shell p-6 bg-[var(--accent-soft)]">
                        <p class="pill-heading">Siguiente accion</p>
                        <div id="nextActionContent" class="mt-4 text-sm">
                            <p class="text-[var(--text-secondary)]">Selecciona un expediente</p>
                        </div>
                    </div>
                </aside>
            </section>
        </section>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
    const STEP_NAMES = [
        "Intencion", "Datos basicos", "Clasificacion", "Transporte",
        "Documentos", "Coherencia", "DTA / PRE-DAM", "Revision final"
    ];
    const STEP_HELP = [
        { q: "Tu mercaderia se queda en Peru, solo pasa o volvera a salir?", doc: "RUC, poder de representacion", riesgo: "Sin regimen no puedes continuar" },
        { q: "Registra FOB, pais de origen, incoterm y descripcion del producto", doc: "Factura proforma, packing list", riesgo: "Sin datos basicos no se calculan tributos" },
        { q: "El HS Code es el DNI aduanero del producto. Si no lo sabes, describe marca, uso y material", doc: "Clasificacion arancelaria oficial", riesgo: "HS incorrecto = tributos erroneos" },
        { q: "Registra el medio de transporte, BL/AWB y fechas de embarque/llegada", doc: "Bill of Lading / Air Waybill", riesgo: "Sin manifiesto no puedes pasar a PRE-DAM" },
        { q: "Verifica que tengas todos los documentos obligatorios", doc: "Factura, BL, Seguro, Certificado de Origen", riesgo: "Documentos incompletos bloquean el despacho" },
        { q: "El sistema verifica coherencia entre regimen, HS, pais e incoterm", doc: "Ninguno (validacion automatica)", riesgo: "Inconsistencias pueden generar observaciones SUNAT" },
        { q: "Este calculo DTA y PRE-DAM es referencial. No reemplaza la validacion oficial de SUNAT", doc: "DAM, DTA, garantia", riesgo: "Sin PRE-DAM no puedes numerar" },
        { q: "Confirma todos los datos antes de enviar el expediente", doc: "Resumen completo del expediente", riesgo: "Errores no corregidos = observaciones" }
    ];

    let currentExpedienteId = null;

    document.getElementById('operacionSelect').addEventListener('change', function() {
        const val = this.value;
        if (val) {
            currentExpedienteId = parseInt(val);
            loadAll();
        }
    });

    document.getElementById('btnRefresh').addEventListener('click', function() {
        if (currentExpedienteId) loadAll();
    });

    async function loadOperaciones() {
        try {
            const r = await fetch('/api/operaciones', { credentials: 'same-origin' });
            if (!r.ok) return;
            const data = await r.json();
            const sel = document.getElementById('operacionSelect');
            sel.innerHTML = '<option value="">Selecciona un expediente</option>';
            const ops = data.lista || data.operaciones || data;
            (Array.isArray(ops) ? ops : []).forEach(op => {
                const opt = document.createElement('option');
                opt.value = op.id;
                const hs = op.hsCode || op.hs_code || '';
                const desc = op.productoDesc || op.producto_desc || '';
                opt.textContent = '#' + op.id + ' - ' + desc + ' (' + hs + ')';
                sel.appendChild(opt);
            });
        } catch (e) {
            console.error('Error cargando operaciones:', e);
        }
    }

    async function loadAll() {
        if (!currentExpedienteId) return;
        await Promise.all([
            loadPasoActual(),
            loadHealth(),
            loadNextAction(),
            loadCoherencia()
        ]);
    }

    async function loadPasoActual() {
        try {
            const r = await fetch('/api/wizard/pasoActual?expedienteId=' + currentExpedienteId, { credentials: 'same-origin' });
            if (r.status === 403) {
                document.getElementById('stepContent').innerHTML = '<div class="text-red-500 font-bold">No autorizado para este expediente</div>';
                return;
            }
            if (!r.ok) return;
            const data = await r.json();
            renderPasoActual(data);
        } catch (e) {
            console.error('Error paso actual:', e);
        }
    }

    function renderPasoActual(data) {
        const step = data.step || 1;
        const nombre = data.nombre || '';
        const desc = data.descripcion || '';
        const estado = data.estado || 'PENDIENTE';
        const blockMsg = data.mensajeBloqueo || '';
        const pct = data.porcentajeGlobal || 0;

        document.getElementById('wizardTitle').textContent = 'Paso ' + step + ': ' + nombre;
        document.getElementById('wizardSubtitle').textContent = desc;

        const rail = document.getElementById('stepRail');
        rail.innerHTML = '';
        for (let i = 0; i < 8; i++) {
            const btn = document.createElement('button');
            btn.className = 'w-step text-[10px] font-black px-3 py-2 rounded-xl border border-[var(--border)] bg-white text-[var(--text-secondary)] text-center leading-tight';
            btn.textContent = (i + 1) + '. ' + STEP_NAMES[i];
            if (i + 1 === step) btn.classList.add('active', 'border-[var(--accent)]', 'text-[var(--accent)]');
            if (i + 1 < step) btn.classList.add('completed');
            if (estado === 'BLOQUEADO' && i + 1 === step) btn.classList.add('blocked');
            rail.appendChild(btn);
        }

        const content = document.getElementById('stepContent');
        let html = '<div class="flex items-start justify-between"><div><p class="text-xs font-black text-[var(--accent)] uppercase tracking-widest">Paso ' + step + ' de 8</p>';
        html += '<h2 class="text-2xl font-black mt-1">' + nombre + '</h2></div>';
        html += '<span class="severity-chip ' + (estado === 'COMPLETO' ? 'OK' : estado === 'BLOQUEADO' ? 'CRITICO' : 'PENDIENTE') + '">' + estado + '</span></div>';
        html += '<p class="text-sm text-[var(--text-secondary)] mt-3">' + desc + '</p>';
        html += '<p class="text-xs font-bold mt-4">Progreso global: ' + pct + '%</p>';

        if (estado === 'BLOQUEADO' && blockMsg) {
            html += '<div class="mt-4 p-4 bg-red-50 border border-red-200 rounded-2xl"><p class="text-xs font-bold text-red-700">Bloqueado: ' + blockMsg + '</p></div>';
        }

        html += '<div class="flex gap-3 mt-6">';
        if (step > 1) {
            html += '<button onclick="retrocederPaso()" class="soft-button text-xs">Anterior</button>';
        }
        if (estado !== 'BLOQUEADO') {
            html += '<button onclick="avanzarPaso()" class="primary-button text-xs">' + (step < 8 ? 'Avanzar al siguiente paso' : 'Finalizar') + '</button>';
        }
        html += '<button onclick="bloquearPaso()" class="px-4 py-2 rounded-xl bg-red-50 border border-red-200 text-red-600 text-xs font-black hover:bg-red-100">Bloquear</button>';
        html += '</div>';
        content.innerHTML = html;

        const helpIdx = Math.min(step - 1, STEP_HELP.length - 1);
        const help = STEP_HELP[helpIdx];
        const helpBox = document.getElementById('helpBox');
        helpBox.classList.remove('hidden');
        document.getElementById('helpTitle').textContent = 'Que significa';
        document.getElementById('helpDesc').innerHTML =
            '<strong>' + help.q + '</strong><br><br>' +
            '<strong>Que documento necesito:</strong> ' + help.doc + '<br>' +
            '<strong>Que pasa si no lo hago:</strong> ' + help.riesgo;
    }

    async function loadHealth() {
        try {
            const r = await fetch('/api/wizard/salud?expedienteId=' + currentExpedienteId, { credentials: 'same-origin' });
            if (!r.ok) return;
            const data = await r.json();
            renderHealth(data);
        } catch (e) {
            console.error('Error health:', e);
        }
    }

    function renderHealth(data) {
        const c = document.getElementById('healthContent');
        const items = [
            { label: 'Completitud', value: data.porcentajeCompletitud || '0%' },
            { label: 'Riesgo documental', value: data.riesgoDocumental || 'N/A', cls: data.riesgoDocumental },
            { label: 'Riesgo normativo', value: data.riesgoNormativo || 'N/A', cls: data.riesgoNormativo },
            { label: 'Riesgo plazo', value: data.riesgoPlazo || 'N/A', cls: data.riesgoPlazo },
            { label: 'Manifiesto', value: data.estadoManifiesto || 'N/A', cls: data.estadoManifiesto },
            { label: 'Permisos', value: data.estadoPermisos || 'N/A', cls: data.estadoPermisos },
            { label: 'PRE-DAM', value: data.estadoPreDam || 'N/A', cls: data.estadoPreDam },
            { label: 'DAM (SUNAT)', value: data.estadoDamSunat || 'N/A', cls: data.estadoDamSunat || 'PENDIENTE' }
        ];
        let html = '';
        items.forEach(item => {
            html += '<div class="flex justify-between items-center"><span class="text-[var(--text-secondary)]">' + item.label + '</span>';
            if (item.cls) {
                html += '<span class="severity-chip ' + item.cls + '">' + item.value + '</span>';
            } else {
                html += '<span class="font-bold text-xs">' + item.value + '</span>';
            }
            html += '</div>';
        });
        if (data.documentosFaltantes && data.documentosFaltantes.length > 0) {
            html += '<div class="mt-3 pt-3 border-t border-[var(--border)]"><p class="text-xs font-bold text-red-500">Faltan:</p>';
            data.documentosFaltantes.forEach(d => {
                html += '<p class="text-xs text-[var(--text-secondary)]">- ' + d + '</p>';
            });
            html += '</div>';
        }
        c.innerHTML = html;
    }

    async function loadNextAction() {
        try {
            const r = await fetch('/api/wizard/siguienteAccion?expedienteId=' + currentExpedienteId, { credentials: 'same-origin' });
            if (!r.ok) return;
            const data = await r.json();
            renderNextAction(data);
        } catch (e) {
            console.error('Error next action:', e);
        }
    }

    function renderNextAction(data) {
        const c = document.getElementById('nextActionContent');
        let html = '<div class="flex items-center gap-2"><span class="severity-chip ' + (data.prioridad || 'MEDIO') + '">' + (data.prioridad || 'MEDIO') + '</span>';
        html += '<span class="font-black text-xs">Paso ' + (data.paso || '?') + '</span></div>';
        html += '<p class="font-bold text-sm mt-2">' + (data.accion || 'Sin accion pendiente') + '</p>';
        html += '<p class="text-xs text-[var(--text-secondary)] mt-1">' + (data.motivo || '') + '</p>';
        if (data.bloqueo) {
            html += '<div class="mt-3 p-3 bg-red-50 border border-red-200 rounded-xl"><p class="text-xs font-bold text-red-600">Bloqueo: ' + data.bloqueo + '</p></div>';
        }
        if (data.camposFaltantes && data.camposFaltantes.length > 0) {
            html += '<div class="mt-2"><p class="text-xs font-bold">Campos faltantes:</p><ul class="list-disc list-inside text-xs text-[var(--text-secondary)]">';
            data.camposFaltantes.forEach(cf => {
                html += '<li>' + cf + '</li>';
            });
            html += '</ul></div>';
        }
        c.innerHTML = html;
    }

    async function loadCoherencia() {
        try {
            const r = await fetch('/api/wizard/coherencia?expedienteId=' + currentExpedienteId, { credentials: 'same-origin' });
            if (!r.ok) return;
            const data = await r.json();
            renderCoherencia(data);
        } catch (e) {
            console.error('Error coherencia:', e);
        }
    }

    function renderCoherencia(issues) {
        const panel = document.getElementById('coherenciaPanel');
        if (!issues || issues.length === 0) {
            panel.classList.add('hidden');
            return;
        }
        panel.classList.remove('hidden');
        let html = '<div class="glass-card section-shell p-6"><p class="pill-heading">Problemas de coherencia</p>';
        issues.forEach(issue => {
            const cls = issue.gravedad === 'ALTA' || issue.gravedad === 'ERROR' ? 'CRITICO' : issue.gravedad === 'MEDIA' ? 'ALTO' : 'MEDIO';
            html += '<div class="mt-3 p-3 rounded-xl border border-[var(--border)]"><div class="flex justify-between">';
            html += '<p class="font-bold text-xs">' + (issue.campo || '') + '</p>';
            html += '<span class="severity-chip ' + cls + '">' + (issue.gravedad || 'INFO') + '</span></div>';
            html += '<p class="text-xs mt-1">' + (issue.descripcion || '') + '</p>';
            if (issue.sugerencia) {
                html += '<p class="text-xs text-[var(--accent)] mt-1">Sugerencia: ' + issue.sugerencia + '</p>';
            }
            html += '</div>';
        });
        html += '</div>';
        panel.innerHTML = html;
    }

    async function avanzarPaso() {
        if (!currentExpedienteId) return;
        try {
            const r = await fetch('/api/wizard/avanzar?expedienteId=' + currentExpedienteId, {
                method: 'POST', credentials: 'same-origin', headers: { 'X-CSRF-TOKEN': window.csrfToken || '' }
            });
            if (r.status === 403) { alert('No autorizado para este expediente'); return; }
            const data = await r.json();
            renderPasoActual(data);
            await Promise.all([loadHealth(), loadNextAction(), loadCoherencia()]);
        } catch (e) {
            console.error('Error avanzar:', e);
        }
    }

    async function retrocederPaso() {
        if (!currentExpedienteId) return;
        try {
            const r = await fetch('/api/wizard/retroceder?expedienteId=' + currentExpedienteId, {
                method: 'POST', credentials: 'same-origin', headers: { 'X-CSRF-TOKEN': window.csrfToken || '' }
            });
            if (r.status === 403) { alert('No autorizado para este expediente'); return; }
            const data = await r.json();
            renderPasoActual(data);
            await Promise.all([loadHealth(), loadNextAction(), loadCoherencia()]);
        } catch (e) {
            console.error('Error retroceder:', e);
        }
    }

    async function bloquearPaso() {
        if (!currentExpedienteId) return;
        const motivo = prompt('Motivo del bloqueo:');
        if (motivo === null) return;
        try {
            const r = await fetch('/api/wizard/bloquear?expedienteId=' + currentExpedienteId + '&motivo=' + encodeURIComponent(motivo || 'Bloqueado por el usuario'), {
                method: 'POST', credentials: 'same-origin', headers: { 'X-CSRF-TOKEN': window.csrfToken || '' }
            });
            if (r.status === 403) { alert('No autorizado para este expediente'); return; }
            await loadAll();
        } catch (e) {
            console.error('Error bloquear:', e);
        }
    }

    loadOperaciones();
    </script>
</body>
</html>
