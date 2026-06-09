/**
 * expediente.js — Lógica de Expediente Aduanero
 *
 * Lee configuración desde window.ImportEase (definido en el JSP):
 *   window.ImportEase.ctx         → context path
 *   window.ImportEase.csrfToken   → token CSRF
 *   window.ImportEase.csrfHeader  → nombre del header CSRF
 *
 * Alias legacy soportados: window.ctx, window.csrfToken
 *
 * Endpoints documentados (no modificar contratos):
 *   GET  /api/importacion/listar
 *   POST /api/aduanas/evaluar-regimen
 *   POST /api/aduanas/evaluar-modalidad
 *   GET  /api/aduanas/expediente?operacionId=
 *   POST /api/aduanas/generar-expediente
 *   POST /api/aduanas/registrar-manifiesto
 *   POST /api/aduanas/generar-predam
 *   POST /api/aduanas/evaluar-reimportacion
 *   POST /api/aduanas/evaluar-transbordo
 *   GET  /api/fuentes/estado
 *   GET  /api/fuentes/arancel?hs=
 *   GET  /api/fuentes/vuce?hs=
 *   GET  /api/fuentes/comtrade?hs=
 *   POST /api/fuentes/tracking/consultar
 *   GET  /api/mentor/ficha?clave=
 */

(function () {
  'use strict';

  /* ── Configuración ───────────────────────────── */
  var cfg = (window.ImportEase || {});
  var CTX = cfg.ctx || window.ctx || '';

  /* ── Estado global ───────────────────────────── */
  var operaciones = [];
  var expediente = null;
  var fuentesEstado = null;
  var selectedDestino = 'PERU';
  var qs = new URLSearchParams(location.search);

  /* ── Step rail ───────────────────────────────── */
  var steps = ['Producto','Ruta','Forma','Transporte','Valor','Docs','Impuestos','Riesgo','Declaracion','Reglas'];
  var stepRailEl = document.getElementById('stepRail');
  if (stepRailEl) {
    stepRailEl.innerHTML = steps.map(function(s, i) {
      return '<div class="flex items-center gap-2 rounded-2xl border border-[#E6E2D8] bg-white px-3 py-3">' +
             '<span class="step-dot is-ready w-7 h-7 rounded-full border flex items-center justify-center text-[10px] font-black">' + (i + 1) + '</span>' +
             '<span class="text-[10px] font-black uppercase tracking-widest text-gray-500">' + s + '</span>' +
             '</div>';
    }).join('');
  }

  /* ── Utilidades ──────────────────────────────── */
  function activeOperacionId() {
    return Number((document.getElementById('operacionSelect') || {}).value || 0);
  }

  function money(value) {
    return '$ ' + Number(value || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  function pct(value) {
    return Math.round(Number(value || 0) * 100) + '%';
  }

  function esc(v) {
    return String(v == null ? '' : v).replace(/[&<>"']/g, function(c) {
      return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
    });
  }

  function activeOperacion() {
    return operaciones.find(function(item) { return Number(item.id) === activeOperacionId(); }) || {};
  }

  /* ── API helper ──────────────────────────────── */
  async function api(path, options) {
    options = options || {};
    var csrfToken = (window.ImportEase && window.ImportEase.csrfToken) || window.csrfToken || '';
    var csrfHeader = (window.ImportEase && window.ImportEase.csrfHeader) || 'X-CSRF-TOKEN';
    var headers = Object.assign({ 'Content-Type': 'application/json' }, options.headers || {});
    headers[csrfHeader] = csrfToken;
    var res = await fetch(CTX + path, Object.assign({}, options, { headers: headers }));
    var payload = await res.json();
    if (payload.data && payload.data.csrfToken) {
      window.csrfToken = payload.data.csrfToken;
      if (window.ImportEase) window.ImportEase.csrfToken = payload.data.csrfToken;
    }
    if (!res.ok || payload.success === false) throw new Error(payload.message || 'Error de API');
    return payload.data;
  }

  /* ── Carga de operaciones ────────────────────── */
  async function cargarOperaciones() {
    var res = await fetch(CTX + '/api/importacion/listar');
    operaciones = await res.json();
    var select = document.getElementById('operacionSelect');
    if (!Array.isArray(operaciones) || operaciones.length === 0) {
      select.innerHTML = '<option value="">No tienes operaciones aun</option>';
      renderEmpty();
      return;
    }
    select.innerHTML = operaciones.map(function(op) {
      return '<option value="' + op.id + '">#' + op.id + ' - ' + esc(op.productoDesc || op.hsCode || 'Operacion') + '</option>';
    }).join('');
    var requested = qs.get('operacionId') || qs.get('id');
    if (requested && operaciones.some(function(op) { return String(op.id) === String(requested); })) {
      select.value = requested;
    }
    await evaluarYRender();
  }

  /* ── Evaluar y renderizar ────────────────────── */
  async function evaluarYRender() {
    var operacionId = activeOperacionId();
    if (!operacionId) return;

    // Reset state and show watermark
    expediente = null;
    var watermark = document.getElementById('dtaWatermark');
    if (watermark) watermark.classList.remove('hidden');

    try {
      var op = operaciones.find(function(item) { return Number(item.id) === operacionId; }) || {};
      var regimen = await api('/api/aduanas/evaluar-regimen', { method: 'POST', body: JSON.stringify({ destino: selectedDestino }) });
      var restringida = await isRestricted(op.hsCode);
      var modalidad = await api('/api/aduanas/evaluar-modalidad', { method: 'POST', body: JSON.stringify({ regimenCodigo: regimen.regimenCodigo, fob: op.valorFob || op.fob || 0, restringida: restringida }) });
      renderRegimen(regimen, modalidad);
      await cargarExpediente(false);
    } catch (e) {
      notify('No pudimos consultar la información en este momento.', 'error');
    }
  }

  /* ── Cargar expediente ───────────────────────── */
  async function cargarExpediente(silent) {
    var operacionId = activeOperacionId();
    if (!operacionId) return;
    try {
      expediente = await api('/api/aduanas/expediente?operacionId=' + operacionId);
      renderExpediente();
    } catch (e) {
      if (!silent) notify('Genera el expediente para activar timeline y alertas.', 'warning');
    }
  }

  /* ── Generar expediente ──────────────────────── */
  async function generarExpediente() {
    var operacionId = activeOperacionId();
    if (!operacionId) return notify('Selecciona una operacion.', 'warning');
    expediente = await api('/api/aduanas/generar-expediente', { method: 'POST', body: JSON.stringify({ operacionId: operacionId, destino: selectedDestino }) });
    renderExpediente();
    notify('Expediente aduanero generado.', 'success');
  }

  /* ── Render: Regimen ─────────────────────────── */
  function renderRegimen(regimen, modalidad) {
    document.getElementById('regimenNombre').textContent = regimen.regimenNombre || '-';
    document.getElementById('regimenMotivo').textContent = regimen.motivo || '-';
    document.getElementById('modalidadNombre').textContent = modalidad.modalidadNombre || '-';
    document.getElementById('modalidadMotivo').textContent = modalidad.motivo || '-';
    document.getElementById('modalidadPlazo').textContent = modalidad.plazoTexto || '-';
    document.getElementById('panelSiguiente').textContent = regimen.siguientePaso || 'Completar expediente';

    var rChip = document.getElementById('regimenChip');
    rChip.textContent = regimen.sourceType || 'Fuente pendiente de confirmar';
    rChip.className = 'source-chip mt-4 ' + chipClass(regimen.sourceType);

    var mChip = document.getElementById('modalidadChip');
    mChip.textContent = modalidad.sourceType || 'Fuente pendiente de confirmar';
    mChip.className = 'source-chip mt-3 ' + chipClass(modalidad.sourceType);
    mChip.classList.remove('hidden');
  }

  /* ── Render: Expediente completo ─────────────── */
  function renderExpediente() {
    if (!expediente || expediente.errorCode) return;
    var panel = expediente.panel || {};

    document.getElementById('panelCompletitud').textContent = panel.completitud || 0;
    document.getElementById('panelDocs').textContent = (panel.docsCompletos || 0) + '/' + (panel.docsTotal || 0) + ' completos';

    var plazosEl = document.getElementById('panelPlazos');
    if (panel.alertasCriticas > 0) {
      plazosEl.textContent = panel.alertasCriticas + ' alertas críticas';
      plazosEl.className = 'text-red-500 font-bold';
    } else {
      plazosEl.textContent = '0 alertas críticas';
      plazosEl.className = 'text-green-600 font-bold';
    }

    document.getElementById('panelFuentes').textContent = (panel.fuentesManuales || 0) + ' manual, ' + (panel.fuentesSimuladas || 0) + ' referencial';

    var predamEl = document.getElementById('panelPredam');
    predamEl.textContent = panel.estadoPredam || 'Pendiente';
    if ((panel.estadoPredam || '').toLowerCase().includes('bloqueada')) {
      predamEl.className = 'text-red-500 font-bold';
    } else {
      predamEl.className = 'text-orange-500 font-bold';
    }

    document.getElementById('panelSiguiente').textContent = panel.siguientePaso || 'Completar expediente';

    renderRegimen(expediente.regimen || {}, expediente.modalidad || {});
    renderAlertas(expediente.alertas || []);
    renderTimeline(expediente.timeline || []);
    renderDocs(expediente.documentos || []);
    renderDta(expediente.dta || {}, expediente.predam || {});
    renderLegal(expediente.baseLegal || []);
    renderPlazos(expediente.plazos || []);
    cargarFuentesReales();
    updateResumen();
  }

  /* ── updateResumen — tarjeta resumen visual ────── */
  function updateResumen() {
    if (!expediente) return;
    var panel = expediente.panel || {};

    function setVal(id, text) {
      var el = document.getElementById(id);
      if (!el) return;
      var span = el.querySelector('.value');
      if (span) span.textContent = text || '';
    }

    // Estado de la importación
    var op = activeOperacion();
    var estado = op.estado || panel.estado || 'En proceso';
    setVal('resumenEstadoVisual', estado);

    // Avance (completitud)
    var avance = (panel.completitud != null) ? panel.completitud + '%' : '—';
    setVal('resumenAvanceVisual', avance);

    // Siguiente paso
    var siguiente = panel.siguientePaso || 'Completar expediente';
    setVal('resumenSiguienteVisual', siguiente);

    // PRE-DAM
    var predam = panel.estadoPredam || 'Pendiente';
    setVal('resumenPredamVisual', predam);

    // Riesgo — nunca mostrar null / N/A / undefined
    var riesgo = panel.nivelRiesgo || panel.canalRiesgo || panel.riesgo;
    if (!riesgo || riesgo === 'N/A' || riesgo === 'null' || riesgo === 'undefined') {
      riesgo = 'Pendiente de evaluar';
    }
    setVal('resumenRiesgoVisual', riesgo);
  }



  /* ── Render: Alertas ─────────────────────────── */
  function renderAlertas(alertas) {
    var box = document.getElementById('alertasBox');
    if (!alertas.length) {
      box.innerHTML = '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-semibold text-gray-500">Sin alertas criticas. Genera expediente para calcular controles.</div>';
      return;
    }
    box.innerHTML = alertas.map(function(a) {
      var borderClass = 'border-[var(--border)]';
      var textClass = 'text-[var(--accent)]';
      var sev = String(a.severidad || 'INFO').toUpperCase();
      if (sev === 'ALTA' || sev === 'CRITICA' || sev === 'CRÍTICA') {
        borderClass = 'border-red-500 bg-red-50/20';
        textClass = 'text-red-600 font-bold';
      } else if (sev === 'MEDIA' || sev === 'ADVERTENCIA') {
        borderClass = 'border-orange-500 bg-orange-50/20';
        textClass = 'text-orange-600 font-bold';
      }
      return '<div class="rounded-2xl border ' + borderClass + ' bg-white p-4">' +
             '<p class="text-[10px] font-black uppercase tracking-widest ' + textClass + '">' + esc(a.severidad || 'INFO') + '</p>' +
             '<p class="font-black mt-1">' + esc(a.mensaje) + '</p>' +
             '<p class="text-xs text-gray-500 font-semibold mt-2">' + esc(a.accion) + '</p>' +
             '<span class="source-chip ' + chipClass(a.sourceType) + ' mt-3">' + esc(a.sourceType || 'REFERENCIAL') + '</span>' +
             '</div>';
    }).join('');
  }

  /* ── Render: Timeline ────────────────────────── */
  function renderTimeline(rows) {
    var box = document.getElementById('timelineBox');
    box.innerHTML = rows.map(function(ev) {
      return '<div class="timeline-item">' +
             '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
             '<div class="flex flex-wrap items-center justify-between gap-3">' +
             '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(ev.codigo) + '</p>' +
             '<span class="font-mono text-xs font-black text-gray-500">' + esc(ev.fecha || '') + '</span>' +
             '</div>' +
             '<h3 class="text-lg font-black mt-2">' + esc(ev.nombre) + '</h3>' +
             '<p class="text-sm text-gray-500 font-semibold mt-2">' + esc(ev.efectoLegal) + '</p>' +
             '<p class="text-xs text-gray-400 font-semibold mt-2">' + esc(ev.observacion) + '</p>' +
             '</div></div>';
    }).join('');
  }

  /* ── Render: Plazos ──────────────────────────── */
  function renderPlazos(plazos) {
    var box = document.getElementById('plazosBox');
    if (!plazos || !plazos.length) {
      box.innerHTML = '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-semibold text-gray-500">No hay plazos críticos para este régimen.</div>';
      return;
    }
    box.innerHTML = plazos.map(function(p) {
      var colorClass = 'text-gray-600';
      var borderClass = 'border-[var(--border)]';
      var chipStyle = 'source-chip--official';
      if (p.status === 'EXPIRED')  { colorClass = 'text-red-600';    borderClass = 'border-red-500';    chipStyle = 'source-chip--error bg-red-100 text-red-600 border-red-300'; }
      else if (p.status === 'CRITICAL') { colorClass = 'text-orange-600'; borderClass = 'border-orange-500'; chipStyle = 'bg-orange-100 text-orange-600 border-orange-300'; }
      else if (p.status === 'WARNING')  { colorClass = 'text-yellow-600'; borderClass = 'border-yellow-500'; chipStyle = 'bg-yellow-100 text-yellow-600 border-yellow-300'; }

      return '<div class="rounded-2xl border ' + borderClass + ' bg-white p-4">' +
             '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(p.label) + '</p>' +
             '<p class="text-sm font-bold ' + colorClass + ' mt-2">Base: ' + esc(p.baseEvent) + ' (' + esc(p.baseDate).replace('T',' ') + ')</p>' +
             '<p class="text-sm font-bold ' + colorClass + ' mt-1">Vence: ' + esc(p.deadline).replace('T',' ') + '</p>' +
             '<div class="flex items-center gap-2 mt-3">' +
             '<span class="source-chip text-[9px] px-2 py-1 ' + chipStyle + '">' + esc(p.status) + ' (' + esc(p.daysRemaining) + ' d)</span>' +
             '<span class="source-chip text-[9px] px-2 py-1 ' + chipClass(p.sourceType) + '">' + esc(p.source) + '</span>' +
             '</div>' +
             '<p class="text-xs text-gray-500 font-semibold mt-3">' + esc(p.message) + '</p>' +
             '</div>';
    }).join('');
  }

  /* ── Render: Documentos ──────────────────────── */
  function renderDocs(docs) {
    var box = document.getElementById('docsBox');
    box.innerHTML = docs.map(function(d) {
      return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
             '<div class="flex items-start justify-between gap-3">' +
             '<h3 class="font-black">' + esc(d.nombre) + '</h3>' +
             '<span class="source-chip ' + (d.requerido ? 'source-chip--pending' : 'source-chip--bd') + '">' + (d.requerido ? 'OBLIGATORIO' : 'SEGUN CASO') + '</span>' +
             '</div>' +
             '<p class="text-sm text-gray-500 font-semibold mt-3">' + esc(d.descripcion) + '</p>' +
             '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] mt-4">' + esc(d.estado) + '</p>' +
             '</div>';
    }).join('');
  }

  /* ── Render: DTA y Pre-DAM ───────────────────── */
  function renderDta(dta, predam) {
    var dtaBox = document.getElementById('dtaBox');
    var rows = [
      ['Base CIF USD', money(dta.baseCifUsd)],
      ['Base CIF PEN', 'S/ ' + Number(dta.baseCifPen || 0).toLocaleString('es-PE', {minimumFractionDigits:2})],
      ['Ad Valorem', money(dta.adValorem)],
      ['IGV', money(dta.igv)],
      ['IPM', money(dta.ipm)],
      ['Percepcion', money(dta.percepcion)],
      ['Total estimado', money(dta.total)],
      ['Origen del dato', dta.sourceType || 'REFERENCIAL'],
      ['Envio y seguro <button class="text-[9px] font-black text-[var(--accent)] hover:underline ml-1" onclick="cargarFichaAsesoria(\'INCOTERMS_VALORACION\')">Ver ayuda</button>', esc(dta.incotermResponsabilidad || 'Incoterm pendiente de definir')]
    ];
    dtaBox.innerHTML = rows.map(function(r) {
      return '<div class="rounded-2xl border border-[var(--border)] bg-white p-4">' +
             '<p class="text-[10px] font-black uppercase tracking-widest text-gray-400">' + r[0] + '</p>' +
             '<p class="text-xl font-black mt-1">' + r[1] + '</p>' +
             '</div>';
    }).join('');

    var watermark = document.getElementById('dtaWatermark');
    if (watermark) {
      if (dta && dta.dtaDisponible && dta.dtaCalculado) {
        watermark.classList.add('hidden');
      } else {
        watermark.classList.remove('hidden');
      }
    }

    document.getElementById('predamBox').innerHTML =
      '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
      '<p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Numero</p>' +
      '<p class="text-2xl font-black font-mono mt-2">' + esc(predam.numeroDam || 'Pendiente') + '</p>' +
      '</div>' +
      '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
      '<p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Nivel de revision</p>' +
      '<p class="text-2xl font-black mt-2">Riesgo estimado: ' + esc(predam.canalProbable || '-') + '</p>' +
      '<span class="source-chip mt-3 ' + chipClass(predam.sourceType) + '">' + esc(predam.sourceType || 'REFERENCIAL') + '</span>' +
      '<p class="text-xs text-[var(--text-secondary)] font-semibold mt-3 text-red-500">' + esc(predam.legalWarning || 'Esta vista previa es referencial. No reemplaza la declaracion oficial ni tiene valor legal.') + '</p>' +
      '</div>';
  }

  /* ── Render: Base legal ──────────────────────── */
  function renderLegal(items) {
    var box = document.getElementById('legalBox');
    box.innerHTML = items.map(function(item) {
      return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
             '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(item.procedimiento) + '</p>' +
             '<h3 class="font-black mt-2">' + esc(item.validacion) + '</h3>' +
             '<p class="text-sm text-gray-500 font-semibold mt-2">' + esc(item.baseLegal) + '</p>' +
             '<div class="mt-4 flex flex-wrap gap-2">' +
             '<span class="source-chip source-chip--bd">' + esc(item.estado) + '</span>' +
             '<span class="source-chip source-chip--estimated">REFERENCIAL</span>' +
             '</div>' +
             '<p class="text-xs text-gray-400 font-semibold mt-3">' + esc(item.accion) + '</p>' +
             '</div>';
    }).join('');
  }

  /* ── Cargar fuentes reales ───────────────────── */
  async function cargarFuentesReales() {
    var op = activeOperacion();
    var hs = op.hsCode || op.codigoHs || (expediente && expediente.predam && expediente.predam.hsCode) || '';
    try {
      var results = await Promise.all([
        api('/api/fuentes/estado'),
        api('/api/fuentes/arancel?hs=' + encodeURIComponent(hs)),
        api('/api/fuentes/vuce?hs=' + encodeURIComponent(hs)),
        hs ? api('/api/fuentes/comtrade?hs=' + encodeURIComponent(hs)) : Promise.resolve(null)
      ]);
      fuentesEstado = { estado: results[0], arancel: results[1], vuce: results[2], comtrade: results[3] };
      renderFuentes();
    } catch (e) {
      document.getElementById('fuentesBox').innerHTML =
        '<div class="rounded-3xl border border-[var(--border)] bg-white p-5 text-sm font-bold text-gray-500">No pudimos consultar la información en este momento.</div>';
    }
  }

  /* ── Chip helper ─────────────────────────────── */
  function chipClass(sourceType) {
    var type = String(sourceType || '').toUpperCase();
    if (type.includes('OFFICIAL_API') || type.includes('OFFICIAL_PROCEDURE') || type.includes('OFICIAL')) return 'source-chip--official';
    if (type.includes('CACHE'))     return 'source-chip--cache';
    if (type.includes('FALLBACK'))  return 'source-chip--fallback';
    if (type.includes('SIMULATED') || type.includes('SIMULADO')) return 'source-chip--simulated';
    if (type.includes('PENDING') || type.includes('PENDIENTE'))  return 'source-chip--pending';
    if (type.includes('MANUAL'))    return 'source-chip--manual';
    if (type.includes('SYSTEM_RULE')) return 'source-chip--system';
    if (type.includes('REFERENTIAL')) return 'source-chip--estimated';
    if (type.includes('UNKNOWN'))   return 'source-chip--error bg-red-100 text-red-600 border-red-300';
    return 'source-chip--bd';
  }

  /* ── Fuente card helper ──────────────────────── */
  function fuenteCard(title, value, sourceType, meta, url) {
    return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
           '<p class="text-[10px] font-black uppercase tracking-widest text-gray-400">' + esc(title) + '</p>' +
           '<h3 class="text-xl font-black mt-2">' + esc(value || '-') + '</h3>' +
           '<p class="text-xs text-gray-500 font-semibold mt-3">' + esc(meta || '') + '</p>' +
           '<div class="mt-4 flex flex-wrap gap-2"><span class="source-chip ' + chipClass(sourceType) + '">' + esc(sourceType || 'BD_LOCAL') + '</span>' +
           (url ? '<a class="source-chip source-chip--cache" target="_blank" rel="noopener" href="' + esc(url) + '">Ver fuente</a>' : '') +
           '</div></div>';
  }

  /* ── Render: Fuentes ─────────────────────────── */
  function renderFuentes() {
    if (!fuentesEstado) return;
    var estado   = fuentesEstado.estado  || {};
    var tc       = estado.tipoCambio     || {};
    var arancel  = fuentesEstado.arancel || {};
    var vuce     = fuentesEstado.vuce    || {};
    var comtrade = fuentesEstado.comtrade || {};
    var resumen  = estado.resumen  || {};
    var mercado  = comtrade.mercado || {};

    document.getElementById('fuentesBox').innerHTML = [
      fuenteCard('Tipo de cambio', 'S/ ' + (tc.tipoCambio || tc.venta || '--'), tc.sourceType || 'CACHE',
        'Dato usado para convertir dolares a soles. Confianza: ' + pct(tc.confidence || 0),
        tc.fuenteUrl || 'https://estadisticas.bcrp.gob.pe/estadisticas/series/api/'),
      fuenteCard('Codigo de producto', arancel.hsCode || activeOperacion().hsCode || 'Pendiente', arancel.sourceType || 'BD_LOCAL',
        (arancel.descripcion || 'Validar codigo del producto') + ' | Confianza: ' + pct(arancel.confidence || 0),
        arancel.fuenteUrl),
      fuenteCard('Permisos', ((vuce.tramites || [])[0] ? (vuce.tramites[0].entidad || 'Validar entidad') : 'Validar entidad'), vuce.sourceType || 'BD_LOCAL',
        vuce.nota || 'Referencia local. No equivale a permiso emitido.',
        vuce.fuenteUrl),
      fuenteCard('Tendencia de mercado', mercado.valorUsd ? ('$ ' + Number(mercado.valorUsd).toLocaleString('es-PE')) : 'Dato pendiente', comtrade.sourceType || 'CACHE',
        'Referencia para comparar volumen comercial. Confianza: ' + pct(comtrade.confidence || 0),
        comtrade.fuenteUrl || 'https://comtradeplus.un.org/'),
      fuenteCard('Seguimiento logistico', 'Opcional', 'PENDIENTE_CREDENCIALES',
        'Solo se valida automaticamente si agregas datos de proveedor logistico.',
        'https://developer.dhl.com/'),
      fuenteCard('Resumen de validaciones', (resumen.okCache || 0) + ' disponibles', 'BD_LOCAL',
        'Pendientes: ' + ((resumen.errores || 0) + (resumen.fallbacks || 0)) + ' | Referenciales: ' + (resumen.simulados || 0),
        null)
    ].join('');

    var eventos = estado.ultimosEventos || [];
    document.getElementById('fuenteEventosBox').innerHTML = eventos.length
      ? eventos.map(function(ev) {
          return '<div class="rounded-2xl border border-[var(--border)] bg-white p-4">' +
                 '<div class="flex items-center justify-between gap-2">' +
                 '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(ev.fuente) + '</p>' +
                 '<span class="source-chip ' + chipClass(ev.sourceType) + '">' + esc(ev.resultado) + '</span>' +
                 '</div>' +
                 '<p class="text-sm font-black mt-2">' + esc(ev.tipoEvento || ev.entidadAfectada || 'Validacion') + '</p>' +
                 '<p class="text-xs text-gray-500 font-semibold mt-2">' + esc(ev.url || ev.mensajeError || 'Validacion registrada con fecha.') + '</p>' +
                 '</div>';
        }).join('')
      : '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-bold text-gray-500">Aun no hay validaciones registradas.</div>';
  }

  /* ── Consultar tracking ──────────────────────── */
  async function consultarTrackingFuente() {
    var operacionId = activeOperacionId();
    var trackingNumber = document.getElementById('trackingNumber').value.trim();
    var blOrContainer  = document.getElementById('trackingBl').value.trim();
    if (!trackingNumber && !blOrContainer) return notify('Ingresa tracking, BL o contenedor.', 'warning');
    var data = await api('/api/fuentes/tracking/consultar', {
      method: 'POST',
      body: JSON.stringify({
        operacionId: operacionId,
        proveedor: document.getElementById('trackingProveedor').value,
        trackingNumber: trackingNumber,
        blNumber: blOrContainer,
        containerNumber: blOrContainer
      })
    });
    var envio  = data.envio  || {};
    var eventos = data.eventos || [];
    document.getElementById('trackingBox').innerHTML =
      resultCard(envio.estadoActual || 'Tracking registrado',
                 'Fuente: ' + (envio.source || 'TRACKING_API') + ' | Endpoint: ' + (data.apiEndpoint || 'pendiente'),
                 'Estado fuente: ' + (envio.sourceType || data.sourceType || 'PENDIENTE_CREDENCIALES')) +
      (eventos.length
        ? '<div class="mt-3 space-y-2">' + eventos.map(function(ev) {
            return '<div class="rounded-2xl border border-[var(--border)] bg-white p-3 text-xs font-semibold text-gray-500">' + esc(ev.estado) + ': ' + esc(ev.descripcion) + '</div>';
          }).join('') + '</div>'
        : '');
    await cargarFuentesReales();
  }

  /* ── Estado vacío ────────────────────────────── */
  function renderEmpty() {
    document.getElementById('panelCompletitud').textContent = '0';
    document.getElementById('panelSiguiente').textContent = 'Crea una operacion primero';
    document.getElementById('alertasBox').innerHTML =
      '<a href="evaluacion.jsp" class="primary-button text-xs w-full justify-center">Crear importacion</a>';
  }

  /* ── Guardar manifiesto ──────────────────────── */
  async function guardarManifiesto() {
    var operacionId = activeOperacionId();
    if (!operacionId) return;
    await api('/api/aduanas/registrar-manifiesto', {
      method: 'POST',
      body: JSON.stringify({
        operacionId:       operacionId,
        numeroManifiesto:  document.getElementById('mNumero').value,
        numeroDocumento:   document.getElementById('mBl').value,
        numeroContenedor:  document.getElementById('mContenedor').value,
        precintoOrigen:    document.getElementById('mPrecinto').value,
        bultos:            Number(document.getElementById('mBultos').value || 1),
        pesoBruto:         Number(document.getElementById('mPeso').value || 0),
        puertoOrigen:      document.getElementById('mPuertoOrigen').value,
        puertoArribo:      document.getElementById('mPuertoArribo').value,
        depositoTemporal:  document.getElementById('mDeposito').value
      })
    });
    notify('Datos de transporte guardados.', 'success');
  }

  /* ── Generar Pre-DAM ─────────────────────────── */
  async function generarPredam() {
    var operacionId = activeOperacionId();
    if (!operacionId) return;
    var data = await api('/api/aduanas/generar-predam', { method: 'POST', body: JSON.stringify({ operacionId: operacionId, destino: selectedDestino }) });
    if (data.success === false && data.errorCode === 'PREDAM_VALIDATION_FAILED') {
      var missingHtml = (data.missingFields || []).map(function(f) { return '<li>• ' + esc(f) + '</li>'; }).join('');
      var html = '<div class="text-left">' +
                 '<p class="font-bold text-red-600 mb-2">' + esc(data.title || 'Aún no puedes generar la PRE-DAM') + '</p>' +
                 '<p class="text-sm mb-2">' + esc(data.message) + '</p>' +
                 '<ul class="text-xs text-red-500 font-semibold">' + missingHtml + '</ul>' +
                 '<p class="text-xs text-gray-500 mt-2">Completa estos datos antes de volver a intentar.</p>' +
                 '</div>';
      notifyHtml(html, 'error');
      return;
    }
    notify(data.mensaje || 'Vista previa creada.', 'success');
    await cargarExpediente(true);
  }

  /* ── Evaluar reimportación ───────────────────── */
  async function evaluarReimportacion() {
    var fechaExport = document.getElementById('rFechaExportacion').value;
    var fechaImport = document.getElementById('rFechaImportacion').value;
    if (!fechaExport || !fechaImport) {
      return notify('Por favor, seleccione ambas fechas para la evaluación.', 'warning');
    }
    var d1 = new Date(fechaExport);
    var d2 = new Date(fechaImport);
    if (d2 < d1) {
      return notify('La fecha prevista de importación debe ser posterior a la fecha de exportación.', 'warning');
    }
    var diffMonths = (d2.getFullYear() - d1.getFullYear()) * 12 + (d2.getMonth() - d1.getMonth());
    var data = await api('/api/aduanas/evaluar-reimportacion', {
      method: 'POST',
      body: JSON.stringify({
        exportadaDesdePeru:      document.getElementById('rExportada').checked,
        exportacionRegularizada: document.getElementById('rRegularizada').checked,
        transformada:            document.getElementById('rTransformada').checked,
        beneficioExportacion:    document.getElementById('rBeneficio').checked,
        mesesDesdeEmbarque:      diffMonths
      })
    });
    document.getElementById('reimportacionBox').innerHTML = resultCard(
      data.procede ? 'Podria proceder' : 'Aun no procede',
      data.diagnostico,
      'Nivel de revision probable: ' + data.canalProbable + ' (referencial)'
    );
  }

  /* ── Evaluar transbordo ──────────────────────── */
  async function evaluarTransbordo() {
    var data = await api('/api/aduanas/evaluar-transbordo', {
      method: 'POST',
      body: JSON.stringify({
        modalidad:              document.getElementById('tModalidad').value,
        diferenciaPesoMayor2:   document.getElementById('tPeso').checked,
        precintoViolado:        document.getElementById('tPrecinto').checked,
        solicitudesPendientes:  document.getElementById('tPendientes').checked
      })
    });
    document.getElementById('transbordoBox').innerHTML = resultCard(
      data.regularizable ? 'Regularizable' : 'Con observaciones',
      data.mensaje,
      (data.alertas || []).join(' | ')
    );
  }

  /* ── Result card helper ──────────────────────── */
  function resultCard(title, text, detail) {
    return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
           '<h3 class="text-xl font-black">' + esc(title) + '</h3>' +
           '<p class="text-sm text-gray-500 font-semibold mt-2">' + esc(text) + '</p>' +
           '<p class="text-xs text-[var(--accent)] font-black uppercase tracking-widest mt-4">' + esc(detail) + '</p>' +
           '</div>';
  }

  /* ── Restricted HS check ─────────────────────── */
  async function isRestricted(hs) {
    hs = String(hs || '');
    try {
      var arancelData = await api('/api/fuentes/arancel?hs=' + encodeURIComponent(hs));
      if (arancelData && typeof arancelData.restringida === 'boolean') {
        return arancelData.restringida;
      }
    } catch (e) {
      // Ignore API errors and treat as not restricted
    }
    return false;
  }

  /* ── Notificaciones ──────────────────────────── */
  function notify(message, type) {
    var old = document.querySelector('.importease-toast');
    if (old) old.remove();
    var div = document.createElement('div');
    div.className = 'importease-toast importease-toast--' + (type || 'success');
    div.textContent = message;
    document.body.appendChild(div);
    setTimeout(function() { div.remove(); }, 4000);
  }

  function notifyHtml(html, type) {
    var old = document.querySelector('.importease-toast');
    if (old) old.remove();
    var div = document.createElement('div');
    div.className = 'importease-toast importease-toast--' + (type || 'success');
    div.innerHTML = html;
    document.body.appendChild(div);
    setTimeout(function() { div.remove(); }, 6000);
  }

  /* ── Mentor drawer ───────────────────────────── */
  var mentorDrawer    = document.getElementById('mentorDrawer');
  var btnMentorToggle = document.getElementById('btnMentorToggle');
  var btnMentorClose  = document.getElementById('btnMentorClose');

  function toggleMentorDrawer(show) {
    if (!mentorDrawer) return;
    if (show) {
      mentorDrawer.classList.remove('translate-x-full');
    } else {
      mentorDrawer.classList.add('translate-x-full');
    }
  }

  async function cargarFichaAsesoria(clave) {
    try {
      var data = await api('/api/mentor/ficha?clave=' + encodeURIComponent(clave));
      document.getElementById('mentorTitulo').textContent     = data.titulo     || '-';
      document.getElementById('mentorArticulo').textContent   = data.articulo   || '-';
      document.getElementById('mentorExplicacion').textContent = data.explicacion || '-';
      var consejoBox = document.getElementById('mentorConsejoBox');
      if (data.consejo) {
        document.getElementById('mentorConsejo').textContent = data.consejo;
        consejoBox.classList.remove('hidden');
      } else {
        consejoBox.classList.add('hidden');
      }
      toggleMentorDrawer(true);
    } catch (e) {
      notify('No pudimos consultar la información en este momento.', 'error');
    }
  }

  /* Exponemos cargarFichaAsesoria globalmente (usada en onclick inline del HTML) */
  window.cargarFichaAsesoria = cargarFichaAsesoria;

  /* ── Event listeners ─────────────────────────── */
  document.querySelectorAll('.regimen-choice').forEach(function(btn) {
    btn.addEventListener('click', function() {
      selectedDestino = btn.dataset.destino;
      document.querySelectorAll('.regimen-choice').forEach(function(b) { b.classList.remove('is-active'); });
      btn.classList.add('is-active');
      evaluarYRender();
    });
  });

  document.querySelectorAll('.aduana-tab').forEach(function(btn) {
    btn.addEventListener('click', function() {
      document.querySelectorAll('.aduana-tab').forEach(function(b) { b.classList.remove('is-active'); });
      btn.classList.add('is-active');
      document.querySelectorAll('.tab-panel').forEach(function(panel) { panel.classList.add('hidden'); });
      var target = document.getElementById('tab-' + btn.dataset.tab);
      if (target) target.classList.remove('hidden');
    });
  });

  document.getElementById('operacionSelect').addEventListener('change', evaluarYRender);
  document.getElementById('btnGenerar').addEventListener('click', generarExpediente);
  document.getElementById('btnGuardarManifiesto').addEventListener('click', guardarManifiesto);
  document.getElementById('btnPredam').addEventListener('click', generarPredam);
  document.getElementById('btnReimportacion').addEventListener('click', evaluarReimportacion);
  document.getElementById('btnTransbordo').addEventListener('click', evaluarTransbordo);
  document.getElementById('btnFuentesRefresh').addEventListener('click', cargarFuentesReales);
  document.getElementById('btnTrackingConsultar').addEventListener('click', consultarTrackingFuente);
  document.getElementById('btnExplainRegimen').addEventListener('click', function() {
    notify('El sistema toma respuestas simples y propone una ruta referencial. Valida el caso real antes de presentar documentos.', 'warning');
  });

  if (btnMentorToggle) {
    btnMentorToggle.addEventListener('click', function() { cargarFichaAsesoria('GENERAL'); });
  }
  if (btnMentorClose) {
    btnMentorClose.addEventListener('click', function() { toggleMentorDrawer(false); });
  }

  /* ── Fechas por defecto de reimportación ─────── */
  function initReimportacionDates() {
    var today      = new Date();
    var exportDate = new Date();
    exportDate.setMonth(today.getMonth() - 8);
    document.getElementById('rFechaImportacion').value = today.toISOString().split('T')[0];
    document.getElementById('rFechaExportacion').value = exportDate.toISOString().split('T')[0];
  }

  /* ── Inicio ──────────────────────────────────── */
  initReimportacionDates();
  cargarOperaciones();

})();
