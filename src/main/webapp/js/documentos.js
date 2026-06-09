/**
 * documentos.js — Lógica de Documentos de Importación
 *
 * Lee configuración desde window.ImportEase (definido en el JSP):
 *   window.ImportEase.ctx         → context path
 *   window.ImportEase.csrfToken   → token CSRF
 *   window.ImportEase.csrfHeader  → nombre del header CSRF
 *
 * Alias legacy soportados: window.ctx, window.csrfToken
 *
 * Endpoints documentados (no modificar contratos):
 *   GET  api/importacion/listar
 *   GET  api/documentos/listar?importacionId={id}
 *   POST api/documentos/subir?importacionId={id}&tipoDocumento={tipo}
 *   POST api/documentos/eliminar?id={docId}
 *   GET  api/documentos/descargar?id={docId}
 *
 * CSRF: añadido en POST de subir y eliminar.
 * Si el backend no valida CSRF, el header es ignorado sin romper nada.
 */

(function () {
    'use strict';

    /* ── Configuración desde window.ImportEase ─────── */
    const csrfToken  = window.ImportEase?.csrfToken  || window.csrfToken  || '';
    const csrfHeader = window.ImportEase?.csrfHeader || 'X-CSRF-TOKEN';

    if (!csrfToken) {
        console.warn('[documentos.js] CSRF token no disponible. Los POST de subir/eliminar pueden fallar si el backend lo requiere.');
    }

    /* ── Estado global de la pantalla ─────────────── */
    let operacionActualEstado = '';
    let uploadInProgress = false;
    const DOCS_SELECTED_OPERATION_KEY = 'importease_docs_selected_operation';

    /* ── Inicialización al cargar el DOM ───────────── */
    window.addEventListener('DOMContentLoaded', () => {
        const select = document.getElementById('importacionSelect');
        if (select) {
            select.removeAttribute('onchange');
            select.addEventListener('change', cambiarOperacion);
        }
        prepararTarjetasDocumentos();
        cargarOperaciones();
    });

    /* ─────────────────────────────────────────────────
       Helpers de texto
    ───────────────────────────────────────────────── */

    function nombreTipoDocumento(tipo) {
        const labels = {
            FACTURA_COMERCIAL: 'Factura Comercial',
            BILL_OF_LADING:    'BL / AWB',
            CERTIFICADO_ORIGEN: 'Certificado de Origen'
        };
        return labels[tipo] || tipo;
    }

    function escapeHtml(text) {
        return String(text ?? '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    /* ─────────────────────────────────────────────────
       1. Cargar operaciones en el selector
    ───────────────────────────────────────────────── */
    function cargarOperaciones() {
        document.getElementById('loadingSpinner').classList.remove('hidden');
        fetch('api/importacion/listar')
            .then(res => {
                if (!res.ok) throw new Error('No se pudieron cargar tus operaciones.');
                return res.json();
            })
            .then(data => {
                if (!Array.isArray(data)) throw new Error(data.error || 'Respuesta inesperada del servidor.');
                const select = document.getElementById('importacionSelect');
                select.innerHTML = '';

                if (data.length === 0) {
                    select.innerHTML = '<option value="">No tienes operaciones creadas</option>';
                    localStorage.removeItem(DOCS_SELECTED_OPERATION_KEY);
                    document.getElementById('documentosTableBody').innerHTML = `
                        <tr>
                            <td colspan="5" class="px-8 py-12 text-center text-gray-400">
                                Todavia no hay operaciones guardadas. Crea una ruta en Importar y vuelve aqui para cargar documentos.
                            </td>
                        </tr>`;
                    actualizarCarpetasGrid([]);
                    actualizarSemaforoExpediente([]);
                    return;
                }

                data.forEach(op => {
                    const opt = document.createElement('option');
                    opt.value = op.id;
                    opt.dataset.estado = op.estado || 'BORRADOR';
                    opt.className = 'text-[var(--text-primary)] font-semibold';
                    const rawDesc = op.productoDesc || 'Producto sin descripcion';
                    const desc = rawDesc.length > 50 ? rawDesc.substring(0, 50) + '...' : rawDesc;
                    opt.textContent = `Operacion #${op.id} - ${desc} (CIF: $${Number(op.valorCif || 0).toFixed(2)})`;
                    select.appendChild(opt);
                });

                const savedOperation = localStorage.getItem(DOCS_SELECTED_OPERATION_KEY);
                if (savedOperation && Array.from(select.options).some(opt => opt.value === savedOperation)) {
                    select.value = savedOperation;
                }

                cambiarOperacion();
            })
            .catch(err => {
                console.error('Error cargando operaciones:', err);
                showNotification('Error', 'No pudimos cargar tus operaciones. Reintenta en unos segundos.', false);
                document.getElementById('importacionSelect').innerHTML = '<option value="">No se pudo cargar operaciones</option>';
                document.getElementById('documentosTableBody').innerHTML = `
                    <tr>
                        <td colspan="5" class="px-8 py-12 text-center text-gray-400">
                            No pudimos conectar con tus operaciones. Reintenta en unos segundos o guarda una ruta nueva desde Importar.
                        </td>
                    </tr>`;
                actualizarCarpetasGrid([]);
                actualizarSemaforoExpediente([]);
            })
            .finally(() => {
                document.getElementById('loadingSpinner').classList.add('hidden');
            });
    }

    /* ─────────────────────────────────────────────────
       2. Disparar cuando cambia la operación activa
    ───────────────────────────────────────────────── */
    function cambiarOperacion() {
        const select = document.getElementById('importacionSelect');
        const opId = select.value;
        if (!opId) return;
        operacionActualEstado = select.options[select.selectedIndex]?.dataset.estado || 'BORRADOR';
        localStorage.setItem(DOCS_SELECTED_OPERATION_KEY, opId);
        setDropZoneState(
            'Arrastra tu ' + nombreTipoDocumento(document.getElementById('tipoDocSelect').value),
            'o haz clic para explorar en tu computadora (PDF, JPG, PNG)'
        );
        cargarDocumentos(opId);
    }

    /* ─────────────────────────────────────────────────
       3. Cargar listado de documentos para la operación
    ───────────────────────────────────────────────── */
    function cargarDocumentos(opId) {
        setDocumentosLoading();
        fetch(`api/documentos/listar?importacionId=${encodeURIComponent(opId)}`)
            .then(res => {
                if (res.status === 403) {
                    showNotification('Error', 'Acceso no autorizado a esta operacion.', false);
                    return [];
                }
                if (!res.ok) throw new Error('No se pudieron cargar los documentos.');
                return res.json();
            })
            .then(docs => {
                if (!Array.isArray(docs)) docs = [];
                actualizarCarpetasGrid(docs);
                renderizarTablaDocumentos(docs, opId);
                actualizarSemaforoExpediente(docs);
            })
            .catch(err => {
                console.error('Error cargando documentos:', err);
                showNotification('Error', 'No pudimos actualizar los documentos.', false);
                actualizarCarpetasGrid([]);
                renderizarTablaDocumentos([], opId);
                actualizarSemaforoExpediente([]);
            });
    }

    /* ─────────────────────────────────────────────────
       Helpers de UI
    ───────────────────────────────────────────────── */

    function setDocumentosLoading() {
        const tbody   = document.getElementById('documentosTableBody');
        const summary = document.getElementById('checkSummary');
        if (summary) summary.textContent = 'Actualizando documentos...';
        if (!tbody) return;
        tbody.innerHTML = `
            <tr>
                <td colspan="5" class="px-8 py-10">
                    <div class="doc-loading-row"></div>
                    <div class="doc-loading-row short mt-3"></div>
                </td>
            </tr>`;
    }

    /* ─────────────────────────────────────────────────
       4. Semáforo de estado del expediente
    ───────────────────────────────────────────────── */
    function actualizarSemaforoExpediente(docs) {
        const title = document.getElementById('expedienteStatusTitle');
        const text  = document.getElementById('expedienteStatusText');
        const icon  = document.getElementById('expedienteStatusIcon');
        if (!title || !text || !icon) return;

        const hasFactura    = docs.some(d => d.tipo_documento === 'FACTURA_COMERCIAL' && d.ruta_archivo);
        const hasTransporte = docs.some(d => d.tipo_documento === 'BILL_OF_LADING' && d.ruta_archivo);

        if (hasFactura && hasTransporte) {
            title.textContent = 'Listo para seguir';
            text.textContent  = 'Tu carpeta base ya tiene lo minimo para continuar con la operacion y completar validaciones puntuales.';
            icon.className    = 'w-12 h-12 rounded-2xl bg-[var(--success-soft)] border border-[var(--success)] text-[var(--success)] flex items-center justify-center font-black';
            icon.textContent  = 'OK';
            return;
        }

        if (operacionActualEstado === 'BORRADOR' || operacionActualEstado === 'COTIZACION') {
            title.textContent = 'Aún en preparación — confirma permisos primero';
            text.textContent  = 'Todavia estas en una etapa temprana. Confirma el codigo y los permisos antes de exigir documentos finales.';
            icon.className    = 'w-12 h-12 rounded-2xl bg-[var(--warning-soft)] border border-[var(--warning)] text-[var(--warning)] flex items-center justify-center font-black';
            icon.textContent  = '?';
            return;
        }

        title.textContent = 'Faltan documentos';
        text.textContent  = 'Aun necesitas factura comercial y documento de transporte para dejar la carpeta base en orden.';
        icon.className    = 'w-12 h-12 rounded-2xl bg-[var(--danger-soft)] border border-[var(--danger)] text-[var(--danger)] flex items-center justify-center font-black';
        icon.textContent  = '!!';
    }

    /* ─────────────────────────────────────────────────
       5. Actualizar estado de las carpetas superiores
    ───────────────────────────────────────────────── */
    function actualizarCarpetasGrid(docs) {
        const tipos = ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'];

        tipos.forEach(tipo => {
            const doc   = docs.find(d => d.tipo_documento === tipo);
            const card  = document.getElementById(`card_${tipo}`);
            const badge = document.getElementById(`badge_${tipo}`);
            const desc  = document.getElementById(`desc_${tipo}`);

            if (card && badge && desc) {
                card.dataset.tipo = tipo;
                card.setAttribute('role', 'button');
                card.setAttribute('tabindex', '0');
                card.setAttribute('aria-label', 'Preparar subida de ' + nombreTipoDocumento(tipo));

                if (doc && doc.ruta_archivo) {
                    card.className  = 'glass-card p-6 folder-card border-[var(--success)] bg-white hover:border-[var(--success)] shadow-sm cursor-pointer transition-all';
                    badge.className = 'px-2 py-0.5 rounded text-[8px] font-black uppercase bg-[var(--success-soft)] text-[var(--success)] border border-[var(--success)]';
                    badge.textContent = 'Cargado ✓';
                    const filename = doc.ruta_archivo.split('/').pop().split('_').slice(1).join('_') || 'Cargado';
                    desc.textContent = filename.length > 20 ? filename.substring(0, 17) + '...' : filename;
                } else {
                    card.className  = 'glass-card p-6 folder-card border-[var(--border)] bg-white cursor-pointer transition-all';
                    badge.className = 'px-2 py-0.5 rounded text-[8px] font-black uppercase bg-[var(--warning-soft)] text-[var(--warning)] border border-[var(--warning)]';
                    badge.textContent = 'Pendiente';
                    desc.textContent  = 'Aún no has subido nada';
                }
            }
        });

        seleccionarTipoDocumento(document.getElementById('tipoDocSelect').value, false, false);
    }

    /* ─────────────────────────────────────────────────
       Preparar tarjetas con listeners
    ───────────────────────────────────────────────── */
    function prepararTarjetasDocumentos() {
        document.querySelectorAll('.folder-card').forEach(card => {
            card.addEventListener('click', () => seleccionarTipoDocumento(card.dataset.tipo || card.id.replace('card_', ''), true, true));
            card.addEventListener('keydown', event => {
                if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    seleccionarTipoDocumento(card.dataset.tipo || card.id.replace('card_', ''), true, true);
                }
            });
        });

        const tipoSelect = document.getElementById('tipoDocSelect');
        if (tipoSelect) {
            tipoSelect.addEventListener('change', () => seleccionarTipoDocumento(tipoSelect.value, false, false));
        }
    }

    function seleccionarTipoDocumento(tipo, scrollToUploader = true, notify = true) {
        const tipoSelect = document.getElementById('tipoDocSelect');
        if (tipoSelect && tipo) tipoSelect.value = tipo;

        document.querySelectorAll('.folder-card').forEach(card => {
            card.classList.toggle('is-selected', card.dataset.tipo === tipo || card.id === `card_${tipo}`);
        });

        setDropZoneState(
            'Arrastra tu ' + nombreTipoDocumento(tipo),
            'o haz clic para explorar en tu computadora (PDF, JPG, PNG)'
        );

        if (scrollToUploader) {
            document.getElementById('dropZone')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        if (notify) {
            showNotification('Listo', 'Sube ' + nombreTipoDocumento(tipo) + ' para esta operacion.', true);
        }
    }

    /* ─────────────────────────────────────────────────
       Datos base cuando no hay documentos cargados
    ───────────────────────────────────────────────── */
    function documentosBase(opId) {
        return [
            { id: `FACTURA_COMERCIAL-${opId}`, tipo_documento: 'FACTURA_COMERCIAL', ruta_archivo: '', fecha_subida: '', es_obligatorio: true },
            { id: `BILL_OF_LADING-${opId}`,    tipo_documento: 'BILL_OF_LADING',    ruta_archivo: '', fecha_subida: '', es_obligatorio: true },
            { id: `CERTIFICADO_ORIGEN-${opId}`, tipo_documento: 'CERTIFICADO_ORIGEN', ruta_archivo: '', fecha_subida: '', es_obligatorio: false }
        ];
    }

    /* ─────────────────────────────────────────────────
       6. Renderizar tabla con archivos reales y estados
    ───────────────────────────────────────────────── */
    function renderizarTablaDocumentos(docs, opId) {
        const tbody = document.getElementById('documentosTableBody');
        tbody.innerHTML = '';

        if (docs.length === 0) {
            docs = documentosBase(opId);
        }

        let cargados     = 0;
        let obligatorios = 0;

        docs.forEach(doc => {
            const safeDocId    = Number.parseInt(doc.id, 10);
            const hasNumericId = Number.isFinite(safeDocId);

            if (doc.es_obligatorio) obligatorios++;
            const isLoaded = doc.ruta_archivo != null && doc.ruta_archivo !== '';
            if (isLoaded) cargados++;

            const row = document.createElement('tr');
            row.className = 'hover:bg-[var(--surface-2)]/30 transition-colors border-b border-[var(--border)]';

            let docLabel     = escapeHtml((doc.tipo_documento || '').toString().replace(/_/g, ' '));
            let cleanFileName = 'Sin archivo cargado';

            if (isLoaded) {
                cleanFileName = doc.ruta_archivo.split('/').pop().split('_').slice(1).join('_');
                cleanFileName = cleanFileName.length > 30 ? cleanFileName.substring(0, 27) + '...' : cleanFileName;
            }
            cleanFileName = escapeHtml(cleanFileName);

            const fechaSubida = (isLoaded && doc.fecha_subida)
                ? escapeHtml(String(doc.fecha_subida).substring(0, 16))
                : '-';

            /* Etiqueta de obligatoriedad (texto para principiantes) */
            const obligLabel = doc.es_obligatorio ? 'Obligatorio' : 'Opcional';

            /* Badge de estado */
            const estadoBadgeClass = isLoaded
                ? 'bg-[var(--success-soft)] text-[var(--success)] border-[var(--success)]'
                : 'bg-[var(--warning-soft)] text-[var(--warning)] border-[var(--warning)]';
            const estadoTexto = isLoaded ? 'Cargado ✓' : 'Pendiente';

            row.innerHTML = `
                <td class="px-8 py-5 flex items-center gap-3">
                    <div class="w-9 h-9 rounded-xl ${isLoaded ? 'bg-[var(--accent-soft)] text-[var(--accent)] border border-[var(--accent-glow)]' : 'bg-[var(--surface-2)] text-[var(--text-tertiary)] border border-[var(--border)]'} flex items-center justify-center shrink-0 font-black">
                        DOC
                    </div>
                    <div>
                        <p class="font-bold text-[var(--text-primary)] uppercase text-[11px] tracking-wider">${docLabel}</p>
                        <p class="text-[8px] text-[var(--text-tertiary)] font-bold uppercase mt-0.5">${obligLabel}</p>
                    </div>
                </td>
                <td class="px-8 py-5">
                    <span class="px-2 py-0.5 rounded text-[8px] font-black uppercase border ${estadoBadgeClass}">
                        ${estadoTexto}
                    </span>
                </td>
                <td class="px-8 py-5 text-xs ${isLoaded ? 'text-[var(--accent)] font-bold' : 'text-[var(--text-tertiary)]'}">${cleanFileName}</td>
                <td class="px-8 py-5 text-xs text-[var(--text-secondary)]">${fechaSubida}</td>
                <td class="px-8 py-5 text-right">
                    <div class="flex items-center justify-end gap-2">
                        ${isLoaded && hasNumericId ? `
                            <a href="api/documentos/descargar?id=${safeDocId}" title="Descargar" class="px-3 py-1.5 hover:bg-[var(--accent-soft)] rounded-lg text-[10px] font-black text-[var(--accent)] hover:text-[var(--accent)] transition-colors border border-[var(--border)] bg-white uppercase tracking-wider">
                                Descargar
                            </a>
                            <button type="button" data-delete-doc="${safeDocId}" title="Quitar documento" class="px-3 py-1.5 hover:bg-[var(--danger-soft)] rounded-lg text-[10px] font-black text-[var(--text-secondary)] hover:text-[var(--danger)] transition-colors border border-[var(--border)] bg-white uppercase tracking-wider">
                                Quitar
                            </button>
                        ` : `
                            <span class="text-[10px] text-[var(--text-secondary)] font-semibold">Subir ahora</span>
                        `}
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });

        document.getElementById('checkSummary').textContent =
            `${cargados} de ${docs.length} documentos cargados (${obligatorios} obligatorios)`;

        tbody.querySelectorAll('[data-delete-doc]').forEach(btn => {
            btn.addEventListener('click', () => eliminarDocumento(btn.dataset.deleteDoc));
        });
    }

    /* ─────────────────────────────────────────────────
       7. Eliminar archivo real
         PENDIENTE: reemplazar confirm() nativo por modal inline
         cuando se disponga de un componente de diálogo reutilizable.
    ───────────────────────────────────────────────── */
    function eliminarDocumento(docId) {
        // PENDIENTE: reemplazar confirm() por modal inline accesible
        if (!confirm('¿Seguro que deseas quitar este documento?')) return;

        fetch(`api/documentos/eliminar?id=${encodeURIComponent(docId)}`, {
            method: 'POST',
            headers: { [csrfHeader]: csrfToken }
        })
            .then(res => res.json())
            .then(data => {
                if (data.error) {
                    showNotification('Error', data.error, false);
                } else {
                    showNotification('Éxito', 'Documento quitado correctamente.', true);
                    cambiarOperacion(); // Refrescar
                }
            })
            .catch(err => showNotification('Error', 'Error al quitar el archivo: ' + err.message, false));
    }

    /* ─────────────────────────────────────────────────
       8. Configurar subida de archivo por AJAX
    ───────────────────────────────────────────────── */
    const dropZone  = document.getElementById('dropZone');
    const fileInput = document.getElementById('fileInput');

    if (dropZone && fileInput) {
        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, preventDefaults, false);
        });

        ['dragenter', 'dragover'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => {
                dropZone.classList.add('drag-active');
            }, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, () => {
                dropZone.classList.remove('drag-active');
            }, false);
        });

        dropZone.addEventListener('drop', handleDrop, false);

        dropZone.addEventListener('click', () => {
            if (uploadInProgress) return;
            fileInput.click();
        });

        fileInput.addEventListener('change', function () {
            if (this.files.length > 0) {
                subirArchivo(this.files[0]);
            }
        });
    }

    function preventDefaults(e) {
        e.preventDefault();
        e.stopPropagation();
    }

    function handleDrop(e) {
        if (uploadInProgress) return;
        const files = e.dataTransfer.files;
        if (files.length > 0) {
            subirArchivo(files[0]);
        }
    }

    function setDropZoneState(title, subtitle, busy = false) {
        if (!dropZone) return;
        const h4 = dropZone.querySelector('h4');
        const p  = dropZone.querySelector('p');
        if (h4) h4.textContent = title;
        if (p)  p.textContent  = subtitle;
        dropZone.classList.toggle('is-busy', busy);
    }

    /* ─────────────────────────────────────────────────
       9. Subir archivo real (multipart/form-data + CSRF)
         Nota: NO se añade Content-Type en headers.
         El navegador lo calcula automáticamente con el boundary correcto.
         Solo se añade el header CSRF separado del body.
    ───────────────────────────────────────────────── */
    function subirArchivo(file) {
        const opId    = document.getElementById('importacionSelect').value;
        const tipoDoc = document.getElementById('tipoDocSelect').value;

        if (!opId) {
            showNotification('Atención', 'Por favor, selecciona una operacion primero.', false);
            return;
        }

        // Validar extensión
        const ext = file.name.split('.').pop().toLowerCase();
        if (ext !== 'pdf' && ext !== 'jpg' && ext !== 'jpeg' && ext !== 'png') {
            showNotification('Atención', 'Extension no permitida. Solo PDF, JPG y PNG.', false);
            return;
        }

        // Validar tamaño máximo (5 MB)
        if (file.size > 1024 * 1024 * 5) {
            showNotification('Atención', 'El archivo excede el tamaño máximo permitido de 5 MB.', false);
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        uploadInProgress = true;
        dropZone.classList.add('drag-active');
        setDropZoneState(
            'Subiendo ' + nombreTipoDocumento(tipoDoc) + '...',
            'Estamos actualizando la carpeta sin recargar la pantalla.',
            true
        );

        fetch(
            `api/documentos/subir?importacionId=${encodeURIComponent(opId)}&tipoDocumento=${encodeURIComponent(tipoDoc)}`,
            {
                method: 'POST',
                // Solo header CSRF — NO Content-Type (el navegador lo calcula con boundary)
                headers: { [csrfHeader]: csrfToken },
                body: formData
            }
        )
        .then(res => {
            dropZone.classList.remove('drag-active');
            uploadInProgress = false;
            setDropZoneState(
                'Arrastra tu ' + nombreTipoDocumento(tipoDoc),
                'o haz clic para explorar en tu computadora (PDF, JPG, PNG)'
            );

            if (res.status === 400) {
                return res.json().then(d => { throw new Error(d.error || 'Archivo invalido'); });
            }
            if (res.status === 403) {
                throw new Error('No tienes autorizacion para cargar archivos en esta operacion.');
            }
            return res.json();
        })
        .then(data => {
            if (data.error) {
                showNotification('Error', 'Error: ' + data.error, false);
            } else {
                showNotification('Éxito', 'Archivo "' + data.nombre + '" cargado con exito.', true);
                fileInput.value = '';
                cambiarOperacion(); // Refrescar vista
            }
        })
        .catch(err => {
            dropZone.classList.remove('drag-active');
            uploadInProgress = false;
            setDropZoneState(
                'Arrastra tu ' + nombreTipoDocumento(tipoDoc),
                'o haz clic para explorar en tu computadora (PDF, JPG, PNG)'
            );
            showNotification('Error', 'Error al subir: ' + err.message, false);
        });
    }

})();
