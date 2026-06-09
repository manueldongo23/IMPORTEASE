/* importacion-guiada.js – extracted from wizard.jsp */

function escapeHtml(str) {
    if (str === null || str === undefined) return '';
    return str.toString()
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

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
    html += '<h2 class="text-2xl font-black mt-1">' + escapeHtml(nombre) + '</h2></div>';
    html += '<span class="severity-chip ' + (estado === 'COMPLETO' ? 'OK' : estado === 'BLOQUEADO' ? 'CRITICO' : 'PENDIENTE') + '">' + escapeHtml(estado) + '</span></div>';
    html += '<p class="text-sm text-[var(--text-secondary)] mt-3">' + escapeHtml(desc) + '</p>';
    html += '<p class="text-xs font-bold mt-4">Progreso global: ' + pct + '%</p>';

    if (estado === 'BLOQUEADO' && blockMsg) {
        html += '<div class="mt-4 p-4 bg-red-50 border border-red-200 rounded-2xl"><p class="text-xs font-bold text-red-700">Bloqueado: ' + escapeHtml(blockMsg) + '</p></div>';
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
    document.getElementById('helpTitle').textContent = '¿Qué significa?';
    document.getElementById('helpDesc').innerHTML =
        '<strong>' + help.q + '</strong><br><br>' +
        '<strong>¿Qué documento necesito?:</strong> ' + help.doc + '<br>' +
        '<strong>¿Qué pasa si no lo hago?:</strong> ' + help.riesgo;
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
        html += '<div class="flex justify-between items-center"><span class="text-[var(--text-secondary)]">' + escapeHtml(item.label) + '</span>';
        if (item.cls) {
            html += '<span class="severity-chip ' + escapeHtml(item.cls) + '">' + escapeHtml(item.value) + '</span>';
        } else {
            html += '<span class="font-bold text-xs">' + escapeHtml(item.value) + '</span>';
        }
        html += '</div>';
    });
    if (data.documentosFaltantes && data.documentosFaltantes.length > 0) {
        html += '<div class="mt-3 pt-3 border-t border-[var(--border)]"><p class="text-xs font-bold text-red-500">Faltan:</p>';
        data.documentosFaltantes.forEach(d => {
            html += '<p class="text-xs text-[var(--text-secondary)]">- ' + escapeHtml(d) + '</p>';
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
    let html = '<div class="flex items-center gap-2"><span class="severity-chip ' + escapeHtml(data.prioridad || 'MEDIO') + '">' + escapeHtml(data.prioridad || 'MEDIO') + '</span>';
    html += '<span class="font-black text-xs">Paso ' + escapeHtml(data.paso || '?') + '</span></div>';
    html += '<p class="font-bold text-sm mt-2">' + escapeHtml(data.accion || 'Sin accion pendiente') + '</p>';
    html += '<p class="text-xs text-[var(--text-secondary)] mt-1">' + escapeHtml(data.motivo || '') + '</p>';
    if (data.bloqueo) {
        html += '<div class="mt-3 p-3 bg-red-50 border border-red-200 rounded-xl"><p class="text-xs font-bold text-red-600">Bloqueo: ' + escapeHtml(data.bloqueo) + '</p></div>';
    }
    if (data.camposFaltantes && data.camposFaltantes.length > 0) {
        html += '<div class="mt-2"><p class="text-xs font-bold">Campos faltantes:</p><ul class="list-disc list-inside text-xs text-[var(--text-secondary)]">';
        data.camposFaltantes.forEach(cf => {
            html += '<li>' + escapeHtml(cf) + '</li>';
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
        html += '<div class="mt-3 p-3 rounded-xl border border-[var(--border)]"><div class="flex justify-between"><p class="font-bold text-xs">' + escapeHtml(issue.campo || '') + '</p>';
        html += '<span class="severity-chip ' + cls + '">' + escapeHtml(issue.gravedad || 'INFO') + '</span></div>';
        html += '<p class="text-xs mt-1">' + escapeHtml(issue.descripcion || '') + '</p>';
        if (issue.sugerencia) {
            html += '<p class="text-xs text-[var(--accent)] mt-1">Sugerencia: ' + escapeHtml(issue.sugerencia) + '</p>';
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
