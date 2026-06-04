<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    String userNombre = (String) session.getAttribute("usuarioNombre");
    if (userNombre == null) userNombre = "Manuel";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Expediente</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <style>
        .custom-input:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
        .folder-card { transition: all 0.4s cubic-bezier(0.22, 1, 0.36, 1); }
        .folder-card:hover { 
            transform: translateY(-4px); 
            box-shadow: 0 20px 25px -5px rgba(59, 130, 246, 0.05), 0 10px 10px -5px rgba(59, 130, 246, 0.02); 
            border-color: var(--accent) !important;
        }
        .drag-active { 
            border-color: var(--accent) !important; 
            background-color: var(--accent-soft) !important; 
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "documentos");
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Expediente"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Documentos de importacion</span>
            </div>
            <div class="flex items-center gap-6">
                <div class="text-[10px] uppercase tracking-widest font-black text-gray-400 flex gap-6">
                    <span class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-[var(--accent)]"></span> Expedientes activos</span>
                </div>
            </div>
        </header>

        <jsp:include page="/fragments/breadcrumb.jsp" />

        <!-- Main Workspace Contents -->
        <div class="p-12 max-w-7xl mx-auto space-y-10 w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest mb-4">&larr; Volver a Importar paso a paso</a>

            <!-- Loading Spinner -->
            <div id="loadingSpinner" class="hidden flex items-center justify-center p-8">
                <div class="w-8 h-8 border-2 border-[var(--accent)] border-t-transparent rounded-full animate-spin"></div>
                <span class="ml-3 text-xs font-bold text-[var(--text-secondary)]">Cargando...</span>
            </div>

            <!-- Title and Selector -->
            <div class="fade-up flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-[var(--border)] pb-6">
                <div>
                    <span class="pill-heading">Carpeta de trabajo</span>
                    <h2 class="text-4xl font-black text-[var(--text-primary)] tracking-tight mt-3">Documentos</h2>
                    <p class="text-[var(--text-secondary)] text-sm font-semibold mt-1">Prepara solo lo necesario para seguir: factura, transporte y, si aplica, origen.</p>
                </div>
                <div class="w-full md:w-[28rem]">
                    <label class="block text-[9px] uppercase tracking-widest font-black text-gray-400 mb-2">Seleccionar importacion</label>
                    <select id="importacionSelect" onchange="cambiarOperacion()" class="w-full px-5 py-3.5 bg-white border border-[var(--border)] rounded-xl text-xs text-[var(--text-primary)] font-semibold custom-input appearance-none bg-no-repeat focus:outline-none transition-all cursor-pointer">
                        <option value="" class="text-gray-400">Cargando tus operaciones...</option>
                    </select>
                </div>
            </div>

            <div class="glass-card hero-banner p-6 fade-up" style="animation-delay: 0.15s">
                <div class="flex items-center gap-4">
                    <div id="expedienteStatusIcon" class="w-12 h-12 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[var(--accent)] flex items-center justify-center font-black">OK</div>
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--accent)]">Que falta</p>
                        <h3 id="expedienteStatusTitle" class="text-xl font-black text-[var(--text-primary)] mt-1">Selecciona una operacion</h3>
                        <p id="expedienteStatusText" class="text-sm text-[var(--text-secondary)] font-semibold mt-1">Te diremos si tu carpeta esta lista, si faltan documentos o si primero debes revisar permisos.</p>
                    </div>
                </div>
            </div>
            <!-- Folders Grid -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 fade-up" style="animation-delay: 0.1s">
                <!-- Factura -->
                <div id="card_FACTURA_COMERCIAL" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-[var(--accent-soft)] flex items-center justify-center text-[var(--accent)] border border-[var(--accent-glow)] font-black">
                            FC
                        </div>
                        <span id="badge_FACTURA_COMERCIAL" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Falta</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">Factura Comercial</h3>
                    <p id="desc_FACTURA_COMERCIAL" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" onclick="openKnowledgePanel('factura_comercial')" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" onclick="openKnowledgePanel('factura_comercial', { subtitulo: 'Ejemplo rapido para novatos', relacionConTuCaso: 'Imagina que tu proveedor te envia una factura con producto, cantidades, precio e incoterm. Ese es el ejemplo que debes validar.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" onclick="openKnowledgePanel('factura_comercial', { etapa: 'Antes de embarcar', subtitulo: 'Momento correcto para pedirla', relacionConTuCaso: 'Pide la factura final cuando ya cerraste el valor y antes del embarque para que no haya diferencias.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                        <a href="incoterms-lab.jsp" class="primary-button text-[10px]">Ver quien paga envio</a>
                    </div>
                </div>
                

                <!-- BL / AWB -->
                <div id="card_BILL_OF_LADING" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-500 border border-gray-200 font-black">
                            BL
                        </div>
                        <span id="badge_BILL_OF_LADING" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Falta</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">BL / AWB</h3>
                    <p id="desc_BILL_OF_LADING" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" onclick="openKnowledgePanel('bill_of_lading')" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" onclick="openKnowledgePanel('bill_of_lading', { subtitulo: 'Ejemplo rapido para novatos', relacionConTuCaso: 'Si tu carga va por mar veras un BL. Si viaja por aire veras un AWB. Ambos sirven para respaldar el transporte.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" onclick="openKnowledgePanel('bill_of_lading', { etapa: 'Antes de declarar', subtitulo: 'Momento correcto para pedirlo', relacionConTuCaso: 'Aparece despues del embarque y debe coincidir con tu factura y la mercancia real.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                    </div>
                </div>

                <!-- Certificados -->
                <div id="card_CERTIFICADO_ORIGEN" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-500 border border-gray-200 font-black">
                            CO
                        </div>
                        <span id="badge_CERTIFICADO_ORIGEN" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Falta</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">Certificado de Origen</h3>
                    <p id="desc_CERTIFICADO_ORIGEN" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" onclick="openKnowledgePanel('certificado_origen')" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" onclick="openKnowledgePanel('certificado_origen', { subtitulo: 'Ejemplo rapido para novatos', relacionConTuCaso: 'Solo te conviene pedirlo cuando un TLC o una preferencia de origen realmente puede ayudarte a pagar menos arancel.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" onclick="openKnowledgePanel('certificado_origen', { etapa: 'Antes de declarar', subtitulo: 'Momento correcto para pedirlo', relacionConTuCaso: 'Debes pedirlo antes de declarar si piensas usar un beneficio de origen preferencial.' })" class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                    </div>
                </div>
            </div>

            <!-- Upload Area -->
            <div class="glass-card upload-shell p-10 fade-up" style="animation-delay: 0.2s">
                <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 mb-8 border-b border-[var(--border)]/50 pb-6">
                    <div>
                        <h3 class="text-lg font-black text-[var(--text-primary)]">Agregar documento</h3>
                        <p class="text-xs text-[var(--text-secondary)] mt-1">Sube solo lo necesario para completar la carpeta base.</p>
                    </div>
                    <div class="w-full md:w-80">
                        <label class="block text-[9px] uppercase tracking-widest font-black text-gray-400 mb-2">Que documento vas a subir</label>
                        <select id="tipoDocSelect" class="w-full px-5 py-3 bg-white border border-[var(--border)] rounded-xl text-xs text-[var(--text-primary)] font-semibold custom-input appearance-none bg-no-repeat focus:outline-none transition-all cursor-pointer">
                            <option value="FACTURA_COMERCIAL">Factura Comercial</option>
                            <option value="BILL_OF_LADING">BL / AWB</option>
                            <option value="CERTIFICADO_ORIGEN">Certificado de Origen</option>
                        </select>
                    </div>
                </div>

                <div id="dropZone" class="dropzone-shell border-2 border-dashed border-[var(--border)] rounded-2xl p-14 text-center hover:border-[var(--accent)]/50 hover:bg-[var(--accent-soft)] transition-all cursor-pointer group">
                    <div class="w-16 h-16 mx-auto bg-[var(--accent-soft)] rounded-full flex items-center justify-center text-2xl text-[var(--accent)] group-hover:bg-[var(--accent-glow)] transition-all mb-4">
                        +
                    </div>
                    <h4 class="text-base font-bold text-[var(--text-primary)] mb-1">Arrastra y suelta tu archivo aqui</h4>
                    <p class="text-xs text-[var(--text-secondary)] mb-6">o haz clic para explorar en tu computadora (PDF, JPG, PNG)</p>
                    <button class="primary-button text-[10px]">
                        Seleccionar Archivo
                    </button>
                    <input type="file" class="hidden" id="fileInput">
                </div>
            </div>

            <!-- Recent Files Table -->
            <div class="glass-card records-shell overflow-hidden fade-up" style="animation-delay: 0.3s">
                <div class="p-6 border-b border-[var(--border)] flex justify-between items-center bg-[var(--surface-2)]">
                    <h3 class="text-sm font-black text-[var(--text-primary)] uppercase tracking-wider">Documentos de la operacion</h3>
                    <span id="checkSummary" class="text-[10px] text-[var(--text-secondary)] font-bold bg-[var(--surface-1)] border border-[var(--border)] px-3 py-1 rounded-lg">Selecciona una operacion</span>
                </div>
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse">
                        <thead>
                            <tr class="text-[10px] uppercase tracking-widest text-[var(--text-secondary)] bg-[var(--surface-2)] border-b border-[var(--border)]">
                                <th class="px-8 py-4">Documento</th>
                                <th class="px-8 py-4">Estado</th>
                                <th class="px-8 py-4">Nombre de Archivo</th>
                                <th class="px-8 py-4">Fecha</th>
                                <th class="px-8 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="documentosTableBody" class="divide-y divide-[var(--border)] text-xs font-semibold text-[var(--text-secondary)]">
                            <tr>
                                <td colspan="5" class="px-8 py-12 text-center text-gray-400">Cargando documentos...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        let operacionActualEstado = '';
        let uploadInProgress = false;
        const DOCS_SELECTED_OPERATION_KEY = 'importease_docs_selected_operation';

        // Carga al iniciar
        window.addEventListener('DOMContentLoaded', () => {
            const select = document.getElementById('importacionSelect');
            if (select) {
                select.removeAttribute('onchange');
                select.addEventListener('change', cambiarOperacion);
            }
            prepararTarjetasDocumentos();
            cargarOperaciones();
        });

        function nombreTipoDocumento(tipo) {
            const labels = {
                FACTURA_COMERCIAL: 'Factura Comercial',
                BILL_OF_LADING: 'BL / AWB',
                CERTIFICADO_ORIGEN: 'Certificado de Origen'
            };
            return labels[tipo] || tipo;
        }

        // 1. Cargar operaciones en el selector
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
                        const desc = rawDesc.length > 50 ? rawDesc.substring(0, 50) + "..." : rawDesc;
                        opt.textContent = `Operacion #${op.id} - ${desc} (CIF: $${Number(op.valorCif || 0).toFixed(2)})`;
                        select.appendChild(opt);
                    });

                    const savedOperation = localStorage.getItem(DOCS_SELECTED_OPERATION_KEY);
                    if (savedOperation && Array.from(select.options).some(opt => opt.value === savedOperation)) {
                        select.value = savedOperation;
                    }

                    // Cargar documentos de la seleccion activa
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
                .finally(function () {
                    document.getElementById('loadingSpinner').classList.add('hidden');
                });
        }

        // 2. Disparar cuando cambia la operacion activa
        function cambiarOperacion() {
            const select = document.getElementById('importacionSelect');
            const opId = select.value;
            if (!opId) return;
            operacionActualEstado = select.options[select.selectedIndex]?.dataset.estado || 'BORRADOR';
            localStorage.setItem(DOCS_SELECTED_OPERATION_KEY, opId);
            setDropZoneState('Arrastra tu ' + nombreTipoDocumento(document.getElementById('tipoDocSelect').value), 'o haz clic para explorar en tu computadora (PDF, JPG, PNG)');

            cargarDocumentos(opId);
        }

        // 3. Cargar listado de documentos para la operacion
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

        function setDocumentosLoading() {
            const tbody = document.getElementById('documentosTableBody');
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

        function actualizarSemaforoExpediente(docs) {
            const title = document.getElementById('expedienteStatusTitle');
            const text = document.getElementById('expedienteStatusText');
            const icon = document.getElementById('expedienteStatusIcon');
            if (!title || !text || !icon) return;

            const hasFactura = docs.some(d => d.tipo_documento === 'FACTURA_COMERCIAL' && d.ruta_archivo);
            const hasTransporte = docs.some(d => d.tipo_documento === 'BILL_OF_LADING' && d.ruta_archivo);

            if (hasFactura && hasTransporte) {
                title.textContent = 'Listo para seguir';
                text.textContent = 'Tu carpeta base ya tiene lo minimo para continuar con la operacion y completar validaciones puntuales.';
                icon.className = 'w-12 h-12 rounded-2xl bg-[var(--success-soft)] border border-[var(--success)] text-[var(--success)] flex items-center justify-center font-black';
                icon.textContent = 'OK';
                return;
            }

            if (operacionActualEstado === 'BORRADOR' || operacionActualEstado === 'COTIZACION') {
                title.textContent = 'Primero revisa permisos';
                text.textContent = 'Todavia estas en una etapa temprana. Confirma el codigo y los permisos antes de exigir documentos finales.';
                icon.className = 'w-12 h-12 rounded-2xl bg-[var(--warning-soft)] border border-[var(--warning)] text-[var(--warning)] flex items-center justify-center font-black';
                icon.textContent = '?';
                return;
            }

            title.textContent = 'Faltan documentos';
            text.textContent = 'Aun necesitas factura comercial y documento de transporte para dejar la carpeta base en orden.';
            icon.className = 'w-12 h-12 rounded-2xl bg-[var(--danger-soft)] border border-[var(--danger)] text-[var(--danger)] flex items-center justify-center font-black';
            icon.textContent = '!!';
        }

        // 4. Actualizar estado de las carpetas superiores
        function actualizarCarpetasGrid(docs) {
            const tipos = ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'];
            
            tipos.forEach(tipo => {
                const doc = docs.find(d => d.tipo_documento === tipo);
                const card = document.getElementById(`card_${tipo}`);
                const badge = document.getElementById(`badge_${tipo}`);
                const desc = document.getElementById(`desc_${tipo}`);

                if (card && badge && desc) {
                    card.dataset.tipo = tipo;
                    card.setAttribute('role', 'button');
                    card.setAttribute('tabindex', '0');
                    card.setAttribute('aria-label', 'Preparar subida de ' + nombreTipoDocumento(tipo));
                    if (doc && doc.ruta_archivo) {
                        // Documento cargado
                        card.className = "glass-card p-6 folder-card border-[var(--success)] bg-white hover:border-[var(--success)] shadow-sm cursor-pointer transition-all";
                        badge.className = "px-2 py-0.5 rounded text-[8px] font-black uppercase bg-[var(--success-soft)] text-[var(--success)] border border-[var(--success)]";
                        badge.textContent = "Cargado";
                        // Sacar el nombre de archivo de la ruta
                        const filename = doc.ruta_archivo.split('/').pop().split('_').slice(1).join('_') || "Cargado";
                        desc.textContent = filename.length > 20 ? filename.substring(0, 17) + "..." : filename;
                    } else {
                        // Documento faltante
                        card.className = "glass-card p-6 folder-card border-[var(--border)] bg-white cursor-pointer transition-all";
                        badge.className = "px-2 py-0.5 rounded text-[8px] font-black uppercase bg-[var(--warning-soft)] text-[var(--warning)] border border-[var(--warning)]";
                        badge.textContent = "Falta";
                        desc.textContent = "0 documentos";
                    }
                }
            });
            seleccionarTipoDocumento(document.getElementById('tipoDocSelect').value, false, false);
        }

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

            setDropZoneState('Arrastra tu ' + nombreTipoDocumento(tipo), 'o haz clic para explorar en tu computadora (PDF, JPG, PNG)');

            if (scrollToUploader) {
                document.getElementById('dropZone')?.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
            if (notify) {
                showNotification('Listo', 'Sube ' + nombreTipoDocumento(tipo) + ' para esta operacion.', true);
            }
        }

        function escapeHtml(text) {
            return String(text ?? "")
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/\"/g, "&quot;")
                .replace(/'/g, "&#39;");
        }

        function documentosBase(opId) {
            return [
                { id: `FACTURA_COMERCIAL-${opId}`, tipo_documento: 'FACTURA_COMERCIAL', ruta_archivo: '', fecha_subida: '', es_obligatorio: true },
                { id: `BILL_OF_LADING-${opId}`, tipo_documento: 'BILL_OF_LADING', ruta_archivo: '', fecha_subida: '', es_obligatorio: true },
                { id: `CERTIFICADO_ORIGEN-${opId}`, tipo_documento: 'CERTIFICADO_ORIGEN', ruta_archivo: '', fecha_subida: '', es_obligatorio: false }
            ];
        }

        // 5. Renderizar tabla con archivos reales y estados
        function renderizarTablaDocumentos(docs, opId) {
            const tbody = document.getElementById('documentosTableBody');
            tbody.innerHTML = '';

            if (docs.length === 0) {
                docs = documentosBase(opId);
            }

            let cargados = 0;
            let obligatorios = 0;

            docs.forEach(doc => {
                const safeDocId = Number.parseInt(doc.id, 10);
                const hasNumericId = Number.isFinite(safeDocId);

                if (doc.es_obligatorio) obligatorios++;
                const isLoaded = doc.ruta_archivo != null && doc.ruta_archivo !== '';
                if (isLoaded) cargados++;

                const row = document.createElement('tr');
                row.className = 'hover:bg-[var(--surface-2)]/30 transition-colors border-b border-[var(--border)]';

                // Nombre y tipo formateados didácticamente
                let docLabel = escapeHtml((doc.tipo_documento || '').toString().replace(/_/g, ' '));
                let cleanFileName = "Sin archivo cargado";
                
                if (isLoaded) {
                    cleanFileName = doc.ruta_archivo.split('/').pop().split('_').slice(1).join('_');
                    cleanFileName = cleanFileName.length > 30 ? cleanFileName.substring(0, 27) + "..." : cleanFileName;
                }
                cleanFileName = escapeHtml(cleanFileName);
                const fechaSubida = (isLoaded && doc.fecha_subida)
                    ? escapeHtml(String(doc.fecha_subida).substring(0, 16))
                    : '-';

                // Generar HTML de la fila
                row.innerHTML = `
                    <td class="px-8 py-5 flex items-center gap-3">
                        <div class="w-9 h-9 rounded-xl ${isLoaded ? 'bg-[var(--accent-soft)] text-[var(--accent)] border border-[var(--accent-glow)]' : 'bg-[var(--surface-2)] text-[var(--text-tertiary)] border border-[var(--border)]'} flex items-center justify-center shrink-0 font-black">
                            DOC
                        </div>
                        <div>
                            <p class="font-bold text-[var(--text-primary)] uppercase text-[11px] tracking-wider">${docLabel}</p>
                            <p class="text-[8px] text-[var(--text-tertiary)] font-bold uppercase mt-0.5">${doc.es_obligatorio ? 'OBLIGATORIO' : 'OPCIONAL'}</p>
                        </div>
                    </td>
                    <td class="px-8 py-5">
                        <span class="px-2 py-0.5 rounded text-[8px] font-black uppercase border ${isLoaded ? 'bg-[var(--success-soft)] text-[var(--success)] border-[var(--success)]' : 'bg-[var(--warning-soft)] text-[var(--warning)] border-[var(--warning)]'}">
                            ${isLoaded ? 'CARGADO' : 'PENDIENTE'}
                        </span>
                    </td>
                    <td class="px-8 py-5 text-xs ${isLoaded ? 'text-[var(--accent)] font-bold' : 'text-[var(--text-tertiary)]'}">${cleanFileName}</td>
                    <td class="px-8 py-5 text-xs text-[var(--text-secondary)]">${fechaSubida}</td>
                    <td class="px-8 py-5 text-right">
                        <div class="flex items-center justify-end gap-2">
                            ${isLoaded && hasNumericId ? `
                                <a href="api/documentos/descargar?id=${safeDocId}" title="Descargar" class="p-2 hover:bg-[var(--accent-soft)] rounded-lg text-[var(--text-secondary)] hover:text-[var(--accent)] transition-colors border border-[var(--border)] bg-white">
                                    Bajar
                                </a>
                                <button type="button" data-delete-doc="${safeDocId}" title="Eliminar" class="p-2 hover:bg-[var(--danger-soft)] rounded-lg text-[var(--text-secondary)] hover:text-[var(--danger)] transition-colors border border-[var(--border)] bg-white">
                                    X
                                </button>
                            ` : `
                                <span class="text-[10px] text-[var(--text-secondary)] font-semibold">Listo para cargar</span>
                            `}
                        </div>
                    </td>
                `;
                tbody.appendChild(row);
            });

            document.getElementById('checkSummary').textContent = `${cargados} de ${docs.length} documentos cargados (${obligatorios} obligatorios)`;
            tbody.querySelectorAll('[data-delete-doc]').forEach(btn => {
                btn.addEventListener('click', () => eliminarDocumento(btn.dataset.deleteDoc));
            });
        }

        // 6. Eliminar archivo real
        function eliminarDocumento(docId) {
            if (!confirm('Seguro que deseas eliminar este documento?')) return;

            fetch(`api/documentos/eliminar?id=${encodeURIComponent(docId)}`, { method: 'POST' })
                .then(res => res.json())
                .then(data => {
                    if (data.error) {
                        showNotification('Error', data.error, false);
                    } else {
                        showNotification('Éxito', 'Documento eliminado correctamente.', true);
                        cambiarOperacion(); // Refrescar
                    }
                })
                .catch(err => showNotification('Error', 'Error al eliminar archivo: ' + err.message, false));
        }

        // 7. Configurar subida de archivo real por AJAX
        const dropZone = document.getElementById('dropZone');
        const fileInput = document.getElementById('fileInput');

        ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
            dropZone.addEventListener(eventName, preventDefaults, false);
        });

        function preventDefaults(e) {
            e.preventDefault();
            e.stopPropagation();
        }

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

        function handleDrop(e) {
            if (uploadInProgress) return;
            let dt = e.dataTransfer;
            let files = dt.files;
            if (files.length > 0) {
                subirArchivo(files[0]);
            }
        }

        function setDropZoneState(title, subtitle, busy = false) {
            const h4 = dropZone.querySelector('h4');
            const p = dropZone.querySelector('p');
            if (h4) h4.textContent = title;
            if (p) p.textContent = subtitle;
            dropZone.classList.toggle('is-busy', busy);
        }

        dropZone.addEventListener('click', () => {
            if (uploadInProgress) return;
            fileInput.click();
        });

        fileInput.addEventListener('change', function() {
            if (this.files.length > 0) {
                subirArchivo(this.files[0]);
            }
        });

        // 8. Hacer llamada POST AJAX real con multipart/form-data
        function subirArchivo(file) {
            const opId = document.getElementById('importacionSelect').value;
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

            // Validar tamano maximo (5MB)
            if (file.size > 1024 * 1024 * 5) {
                showNotification('Atención', 'El archivo excede el tamano maximo permitido de 5 MB.', false);
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            uploadInProgress = true;
            dropZone.classList.add('drag-active');
            setDropZoneState('Subiendo ' + nombreTipoDocumento(tipoDoc) + '...', 'Estamos actualizando la carpeta sin recargar la pantalla.', true);

            fetch(`api/documentos/subir?importacionId=${encodeURIComponent(opId)}&tipoDocumento=${encodeURIComponent(tipoDoc)}`, {
                method: 'POST',
                body: formData
            })
            .then(res => {
                dropZone.classList.remove('drag-active');
                uploadInProgress = false;
                setDropZoneState('Arrastra tu ' + nombreTipoDocumento(tipoDoc), 'o haz clic para explorar en tu computadora (PDF, JPG, PNG)');
                
                if (res.status === 400) {
                    return res.json().then(d => { throw new Error(d.error || 'Archivo invalido') });
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
                setDropZoneState('Arrastra tu ' + nombreTipoDocumento(tipoDoc), 'o haz clic para explorar en tu computadora (PDF, JPG, PNG)');
                showNotification('Error', 'Error al subir: ' + err.message, false);
            });
        }
    </script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/knowledge-base.js"></script>
</body>
</html>




