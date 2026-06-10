/* ====================================================================
   permisos.js - Lógica interactiva para la gestión de permisos
   ==================================================================== */

(function () {
    "use strict";

    // Variables de contexto dinámicas
    const CTX = window.ImportEase?.ctx || window.ctx || '';
    const CSRF = window.ImportEase?.csrfToken || window.csrfToken || '';
    const CSRF_HEADER = window.ImportEase?.csrfHeader || 'X-CSRF-TOKEN';

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return str.toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    let currentOperacion = null;
    let currentSolicitudes = [];
    let activeSolicitudId = null;

    // Hacer funciones disponibles globalmente para compatibilidad temporal
    window.selectAndEvaluateOp = selectAndEvaluateOp;
    window.cargarOperaciones = cargarOperaciones;
    window.onOperacionChange = onOperacionChange;
    window.evaluarOperacion = evaluarOperacion;
    window.renderResultado = renderResultado;
    window.cargarCuestionario = cargarCuestionario;
    window.submitCuestionario = submitCuestionario;
    window.autorrellenarExpediente = autorrellenarExpediente;
    window.descargarPDF = descargarPDF;
    window.cargarChecklist = cargarChecklist;
    window.registrarSuce = registrarSuce;
    window.cargarHistorial = cargarHistorial;
    window.filterPermits = filterPermits;

    // Triggered when dynamic row buttons are clicked
    function selectAndEvaluateOp(opId) {
        const select = document.getElementById('selectOperacion');
        if (select) {
            select.value = opId;
            onOperacionChange();
            
            // Auto Scroll to evaluation section
            select.scrollIntoView({ behavior: 'smooth', block: 'start' });
            
            // Auto trigger evaluation
            setTimeout(() => {
                evaluarOperacion();
            }, 300);
        }
    }

    async function cargarOperaciones() {
        try {
            const resp = await fetch(CTX + '/api/importacion/listar');
            if (!resp.ok) throw new Error('Error cargando operaciones');
            const ops = await resp.json();
            const select = document.getElementById('selectOperacion');
            if (select) {
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
            }

            const historial = document.getElementById('historialSection');
            if (historial) {
                historial.classList.remove('hidden');
                setTimeout(() => historial.querySelector('h2, h3, button')?.focus(), 100);
            }
            cargarHistorial();
        } catch(e) {
            console.error('Error:', e);
        }
    }

    function onOperacionChange() {
        const select = document.getElementById('selectOperacion');
        const btn = document.getElementById('btnEvaluar');
        const details = document.getElementById('opDetails');
        
        if (select && select.value) {
            if (btn) btn.disabled = false;
            if (!select.selectedOptions[0]?.dataset?.json) return;
            const op = JSON.parse(select.selectedOptions[0].dataset.json);
            currentOperacion = op;
            
            const prod = document.getElementById('opProducto');
            const code = document.getElementById('opHsCode');
            const pais = document.getElementById('opPais');
            const cif = document.getElementById('opCif');
            
            if (prod) prod.textContent = op.productoDesc || '—';
            if (code) code.textContent = op.hsCode || '—';
            if (pais) pais.textContent = op.paisOrigen || '—';
            if (cif) cif.textContent = op.cif ? `$ ${parseFloat(op.cif).toFixed(2)}` : '—';
            
            if (details) {
                details.classList.remove('hidden');
                setTimeout(() => details.querySelector('h2, h3, button')?.focus(), 100);
            }
        } else {
            if (btn) btn.disabled = true;
            currentOperacion = null;
            if (details) details.classList.add('hidden');
        }
        
        ['resultadoSection','cuestionarioSection','expedienteSection','checklistSection','suceSection'].forEach(id => {
            const el = document.getElementById(id);
            if (el) el.classList.add('hidden');
        });
    }

    async function evaluarOperacion() {
        if (!currentOperacion) return;
        
        const loading = document.getElementById('loadingState');
        const resultado = document.getElementById('resultadoSection');
        if (loading) loading.classList.remove('hidden');
        if (resultado) resultado.classList.add('hidden');

        try {
            const resp = await fetch(CTX + `/api/permisos/evaluar?operacionId=${currentOperacion.id}`);
            if (!resp.ok) throw new Error('Error evaluando');
            const data = await resp.json();
            
            if (loading) loading.classList.add('hidden');
            renderResultado(data);
            if (resultado) {
                resultado.classList.remove('hidden');
                setTimeout(() => resultado.querySelector('h2, h3, button')?.focus(), 100);
            }

            currentSolicitudes = data.solicitudes || [];
            if (currentSolicitudes.length > 0) {
                activeSolicitudId = currentSolicitudes[0].id;
            }

            if (!data.libre) {
                if (data.entidades && data.entidades.length > 0) {
                    cargarCuestionario(data.entidades[0].codigoEntidad);
                    const quest = document.getElementById('cuestionarioSection');
                    if (quest) {
                        quest.classList.remove('hidden');
                        setTimeout(() => quest.querySelector('h2, h3, button')?.focus(), 100);
                    }
                }
                
                ['expedienteSection', 'checklistSection', 'suceSection'].forEach(id => {
                    const el = document.getElementById(id);
                    if (el) {
                        el.classList.remove('hidden');
                        setTimeout(() => el.querySelector('h2, h3, button')?.focus(), 100);
                    }
                });

                if (data.restricciones && data.restricciones.length > 0) {
                    cargarChecklist(data.restricciones[0].codigoEntidad, data.restricciones[0].tipoPermiso);
                }
            }
        } catch(e) {
            if (loading) loading.classList.add('hidden');
            console.error('Error:', e);
            if (window.showNotification) {
                window.showNotification('Error', 'Error al evaluar la operación', false);
            }
        }
    }

    function translateRiesgo(riesgo) {
        if (!riesgo) return 'Pendiente de evaluar';
        const r = riesgo.toUpperCase().trim();
        if (r === 'ALTO') return 'Alto';
        if (r === 'MEDIO') return 'Medio';
        if (r === 'BAJO') return 'Bajo';
        if (r === 'CRITICO' || r === 'CRÍTICO') return 'Crítico';
        return riesgo;
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

        if (!box || !icon || !title || !desc || !container) return;

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
            title.textContent = 'Puede requerir permiso — revisa antes';
            desc.textContent = 'El producto tiene puntos que conviene confirmar antes de asumir que ya requiere permiso formal.';
            container.innerHTML = '';
            restricciones.forEach((regla) => {
                const card = document.createElement('div');
                card.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] hover:border-[var(--accent)] transition-all font-semibold text-xs';
                card.innerHTML = `
                    <div class="flex items-start gap-4">
                        <div class="w-10 h-10 rounded-xl bg-white border border-[var(--border)] flex items-center justify-center shrink-0 mt-0.5">
                            <span class="text-xs font-black text-[var(--accent)]">${escapeHtml(regla.codigoEntidad)}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap mb-1">
                                <h4 class="text-xs font-bold text-[var(--text-primary)]">${escapeHtml(regla.tipoPermiso || 'Validacion previa')}</h4>
                                <span class="text-[9px] font-black px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 border border-amber-200">${escapeHtml(translateRiesgo(regla.nivelRiesgo || 'Revisar'))}</span>
                            </div>
                            <p class="text-[11px] text-gray-500 leading-relaxed mt-1">${escapeHtml(regla.mensajeUsuario || 'Confirma la ficha tecnica o el uso real antes de preparar el expediente.')}</p>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        } else {
            box.className = 'rounded-xl p-5 mb-6 border border-orange-200 bg-orange-50';
            icon.className = 'w-14 h-14 rounded-2xl flex items-center justify-center shrink-0 bg-orange-100';
            icon.innerHTML = '!!';
            title.textContent = 'Requiere permiso';
            desc.textContent = `Detectamos ${restricciones.length} restriccion(es) y conviene preparar expediente antes de embarcar.`;

            container.innerHTML = '';
            restricciones.forEach((regla) => {
                const card = document.createElement('div');
                card.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] hover:border-[var(--accent)] transition-all font-semibold text-xs';
                card.innerHTML = `
                    <div class="flex items-start gap-4">
                        <div class="w-10 h-10 rounded-xl bg-white border border-[var(--border)] flex items-center justify-center shrink-0 mt-0.5">
                            <span class="text-xs font-black text-[var(--accent)]">${escapeHtml(regla.codigoEntidad)}</span>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center gap-2 flex-wrap mb-1">
                                <h4 class="text-xs font-bold text-[var(--text-primary)]">${escapeHtml(regla.tipoPermiso)}</h4>
                                <span class="text-[9px] font-black px-2 py-0.5 rounded-full bg-orange-50 text-orange-600 border border-orange-200">${escapeHtml(translateRiesgo(regla.nivelRiesgo))}</span>
                            </div>
                            <p class="text-[11px] text-gray-500 leading-relaxed mt-1">${escapeHtml(regla.mensajeUsuario || '')}</p>
                        </div>
                    </div>
                `;
                container.appendChild(card);
            });
        }
    }

    async function cargarCuestionario(codigoEntidad) {
        try {
            const entLabel = document.getElementById('cuestionarioEntidad');
            if (entLabel) entLabel.textContent = codigoEntidad;
            
            const resp = await fetch(CTX + `/api/permisos/preguntas?entidad=${codigoEntidad}`);
            if (!resp.ok) throw new Error('Error cargando preguntas');
            const preguntas = await resp.json();
            
            const container = document.getElementById('preguntasContainer');
            if (container) {
                container.innerHTML = '';
                preguntas.forEach((p, i) => {
                    const div = document.createElement('div');
                    div.className = 'bg-[var(--surface-2)] rounded-xl p-4 border border-[var(--border)] font-semibold text-xs';
                    
                    let inputHtml = `
                        <div class="flex gap-4 mt-2">
                            <label class="flex items-center gap-2 cursor-pointer">
                                <input type="radio" name="pregunta_${escapeHtml(p.id)}" value="true" class="accent-[var(--accent)]">
                                <span class="text-sm text-gray-600">Sí</span>
                            </label>
                            <label class="flex items-center gap-2 cursor-pointer">
                                <input type="radio" name="pregunta_${escapeHtml(p.id)}" value="false" class="accent-[var(--accent)]">
                                <span class="text-sm text-gray-600">No</span>
                            </label>
                        </div>`;
                    
                    div.innerHTML = `
                        <div class="flex items-start gap-3">
                            <span class="w-6 h-6 rounded-lg bg-white border border-[var(--border)] flex items-center justify-center text-xs font-bold text-[var(--accent)] shrink-0 mt-0.5">${i + 1}</span>
                            <div class="flex-1">
                                <p class="text-xs text-[var(--text-primary)] font-bold">${escapeHtml(p.pregunta)}</p>
                                ${p.obligatoria ? '<span class="text-[9px] text-red-500 font-bold">* Obligatoria</span>' : ''}
                                ${inputHtml}
                            </div>
                        </div>
                    `;
                    container.appendChild(div);
                });
            }

            const quest = document.getElementById('cuestionarioSection');
            if (quest) {
                quest.classList.remove('hidden');
                setTimeout(() => quest.querySelector('h2, h3, button')?.focus(), 100);
            }
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
            if (window.showNotification) {
                window.showNotification('Atención', 'Por favor, complete las preguntas del cuestionario.', false);
            }
            return;
        }

        try {
            const resp = await fetch(CTX + '/api/permisos/guardar-respuestas', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [CSRF_HEADER]: CSRF
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

            if (window.showNotification) {
                window.showNotification('Éxito', '¡Respuestas guardadas correctamente para auditoría aduanera!', true);
            }
            const expSec = document.getElementById('expedienteSection');
            if (expSec) expSec.scrollIntoView({ behavior: 'smooth', block: 'start' });
        } catch (e) {
            if (window.showNotification) {
                window.showNotification('Error', e.message, false);
            }
        }
    }

    async function autorrellenarExpediente() {
        if (!activeSolicitudId) return;
        
        const btn = document.getElementById('btnAutorrellenar');
        if (btn) btn.disabled = true;

        try {
            const resp = await fetch(CTX + `/api/permisos/autorrellenar?solicitudId=${activeSolicitudId}`, {
                method: 'POST',
                headers: {
                    [CSRF_HEADER]: CSRF
                }
            });
            if (!resp.ok) throw new Error('Error autorellenando');
            const data = await resp.json();
            
            const grid = document.getElementById('datosGrid');
            const datosSection = document.getElementById('datosExpediente');
            if (grid) {
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
                        <span class="text-[9px] text-gray-400 uppercase tracking-widest font-semibold block mb-1">${escapeHtml(labels[d.campo] || d.campo)}</span>
                        <span class="text-xs text-[var(--text-primary)] font-bold">${escapeHtml(d.valor || '—')}</span>
                    `;
                    grid.appendChild(cell);
                });
            }
            
            if (datosSection) {
                datosSection.classList.remove('hidden');
                setTimeout(() => datosSection.querySelector('h2, h3, button')?.focus(), 100);
            }
            const pdfBtn = document.getElementById('btnPdf');
            if (pdfBtn) pdfBtn.disabled = false;
            if (btn) btn.disabled = false;
            
            if (window.showNotification) {
                window.showNotification('Éxito', '¡Expediente digital autocompletado con éxito!', true);
            }
        } catch(e) {
            if (btn) btn.disabled = false;
            if (window.showNotification) {
                window.showNotification('Error', 'Error autorrellenando expediente', false);
            }
        }
    }

    async function descargarPDF() {
        if (!activeSolicitudId) return;
        window.location.href = CTX + `/api/permisos/pdf?id=${activeSolicitudId}`;
        if (window.showNotification) {
            window.showNotification('Éxito', 'Descargando expediente digital PDF', true);
        }
    }

    async function cargarChecklist(entidad, tipoPermiso) {
        const container = document.getElementById('checklistContainer');
        if (!container) return;
        
        container.innerHTML = `
            <div class="mb-4 p-3 rounded-xl bg-amber-50 border border-amber-200 text-[11px] text-amber-800 font-semibold flex items-start gap-2">
                <span class="text-sm shrink-0">⚠️</span>
                <span>Checklist referencial. Valida los requisitos finales en VUCE o con la entidad correspondiente antes de continuar.</span>
            </div>
        `;
        
        const docs = [
            { id: 'FC', nombre: 'Factura Comercial Proforma', desc: 'Sustenta la compra y composición' },
            { id: 'FT', nombre: 'Ficha Técnica del Fabricante', desc: 'Detalla componentes, rango y frecuencia' },
            { id: 'MU', nombre: 'Manual de Uso o Operación', desc: 'Indica funcionalidad y potencia' }
        ];

        const progress = document.getElementById('checklistProgress');
        if (progress) progress.innerText = "0 de 3 completados";

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
        
        const suceInput = document.getElementById('inputSuce');
        const suce = suceInput ? suceInput.value : '';
        
        if (!suce) {
            if (window.showNotification) {
                window.showNotification('Atención', 'Ingrese el número SUCE de validación', false);
            }
            return;
        }

        try {
            await fetch(CTX + `/api/importacion/cambiarEstado?id=${opId}&nuevoEstado=LISTA_DESPACHO`, {
                method: 'POST',
                headers: {
                    [CSRF_HEADER]: CSRF
                }
            });
            if (window.showNotification) {
                window.showNotification('Éxito', '¡Número SUCE registrado y expediente vinculado correctamente!', true);
            }
            setTimeout(() => window.location.reload(), 1500);
        } catch(e) {
            if (window.showNotification) {
                window.showNotification('Error', 'Error vinculando SUCE', false);
            }
        }
    }

    async function cargarHistorial() {
        const container = document.getElementById('historialContainer');
        if (!container) return;
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
                            <h4 class="text-sm font-bold text-[var(--text-primary)]">Tramite #${escapeHtml(sol.id)}</h4>
                            <p class="text-[9px] text-[var(--accent)] font-bold uppercase mt-0.5">Operación: OP-${escapeHtml(sol.operacionId.toString().padStart(5, '0'))}</p>
                        </div>
                        <span class="px-2 py-0.5 rounded-full text-[8px] font-black uppercase border border-teal-200 bg-teal-50 text-[var(--accent)]">${escapeHtml(sol.estado)}</span>
                    </div>
                    <div class="grid grid-cols-2 gap-2 text-[10px] border-t border-[var(--border)]/50 pt-2 font-medium">
                        <div><span class="text-gray-400">Entidad:</span> <strong class="text-[var(--text-primary)]">${escapeHtml(sol.codigoEntidad)}</strong></div>
                        <div><span class="text-gray-400">Permiso:</span> <strong class="text-[var(--text-primary)]">${escapeHtml(sol.tipoPermiso)}</strong></div>
                        <div><span class="text-gray-400">SUCE:</span> <strong class="text-purple-600 font-mono">${escapeHtml(sol.suce || 'En borrador')}</strong></div>
                        <div><span class="text-gray-400">Resolución:</span> <strong class="text-[var(--text-primary)]">${escapeHtml(sol.resolucion || 'Pendiente')}</strong></div>
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
            
            let show = false;
            if (filter === 'Pendientes' && state === 'Pendiente') {
                show = true;
            } else if (filter === 'En trámite' && state === 'En revisión') {
                show = true;
            } else if (filter === 'Aprobados' && state === 'Aprobado') {
                show = true;
            } else if (filter === 'Rechazados' && state === 'En preparación') {
                show = true;
            }
            
            row.style.display = show ? '' : 'none';
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        cargarOperaciones();

        // 1. Escuchar cambios en el selector de operaciones
        const selectOperacion = document.getElementById('selectOperacion');
        if (selectOperacion) {
            selectOperacion.addEventListener('change', onOperacionChange);
        }

        // 2. Escuchar clicks en los botones de acciones estáticos
        const btnEvaluar = document.getElementById('btnEvaluar');
        if (btnEvaluar) {
            btnEvaluar.addEventListener('click', evaluarOperacion);
        }

        const btnSubmitCuestionario = document.getElementById('btnSubmitCuestionario');
        if (btnSubmitCuestionario) {
            btnSubmitCuestionario.addEventListener('click', submitCuestionario);
        }

        const btnAutorrellenar = document.getElementById('btnAutorrellenar');
        if (btnAutorrellenar) {
            btnAutorrellenar.addEventListener('click', autorrellenarExpediente);
        }

        const btnPdf = document.getElementById('btnPdf');
        if (btnPdf) {
            btnPdf.addEventListener('click', descargarPDF);
        }

        const btnRegistrarSuce = document.getElementById('btnRegistrarSuce');
        if (btnRegistrarSuce) {
            btnRegistrarSuce.addEventListener('click', registrarSuce);
        }

        // 3. Delegación de eventos en la tabla para los botones de las filas
        const permitsTableBody = document.getElementById('permitsTableBody');
        if (permitsTableBody) {
            permitsTableBody.addEventListener('click', (event) => {
                const btn = event.target.closest('button[data-action="select-eval"]');
                if (btn) {
                    const opId = btn.getAttribute('data-op-id');
                    if (opId) {
                        selectAndEvaluateOp(opId);
                    }
                }
            });
        }

        // 4. Delegación de eventos para los filtros (pills)
        const filtersContainer = document.getElementById('filtersContainer');
        if (filtersContainer) {
            filtersContainer.addEventListener('click', (event) => {
                const btn = event.target.closest('button[data-filter]');
                if (btn) {
                    const filter = btn.getAttribute('data-filter');
                    if (filter) {
                        filterPermits(filter);
                    }
                }
            });
        }

        // Auto default filter to Pending
        setTimeout(() => {
            filterPermits('Pendientes');
        }, 300);
    });

})();
