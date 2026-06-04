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
    <title>ImportEase - Radar Logistico</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <!-- Leaflet.js library for premium maps -->
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=" crossorigin=""/>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=" crossorigin=""></script>
    <style>
        .timeline-dot { box-shadow: 0 0 0 6px var(--accent-glow); }
        input:focus, select:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
        /* Custom Leaflet style overrides to feel premium */
        .leaflet-bar {
            border: 1px solid var(--border) !important;
            box-shadow: var(--shadow-sm) !important;
            border-radius: var(--radius-md) !important;
            overflow: hidden;
        }
        .leaflet-bar a {
            background-color: white !important;
            color: var(--text-secondary) !important;
            border-bottom: 1px solid var(--border) !important;
            transition: all var(--duration-fast);
        }
        .leaflet-bar a:hover {
            background-color: var(--surface-2) !important;
            color: var(--accent) !important;
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <% request.setAttribute("activePage", "tracking"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl sticky top-0 z-10 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Radar logístico / web tracking</span>
            </div>
            <a href="seguimiento.jsp" class="px-4 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black text-[var(--text-secondary)] uppercase tracking-widest hover:text-[var(--accent)] transition-all cursor-pointer">Seguimiento</a>
        </header>

        <!-- Main Workspace Contents -->
        <section class="p-8 xl:p-12 max-w-7xl mx-auto w-full space-y-8 flex-1">
            <div class="grid grid-cols-1 xl:grid-cols-[1fr_24rem] gap-8">
                <div class="glass-card p-8">
                    <span class="pill-heading">Módulo de Radar</span>
                    <h1 class="text-4xl font-black tracking-tight mt-3">Radar Logístico</h1>
                    <p class="text-sm text-[var(--text-secondary)] font-semibold mt-2 max-w-3xl">Registra DHL, FedEx, UPS, Maersk, BL o contenedor. Cuando falten tokens, ImportEase lo muestra como credenciales pendientes, no como tracking real.</p>

                    <form id="trackingForm" class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4 mt-8">
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Proveedor</span>
                            <select id="proveedor" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-bold focus:outline-none transition-all cursor-pointer">
                                <option>DHL</option>
                                <option>FEDEX</option>
                                <option>UPS</option>
                                <option>MAERSK</option>
                                <option>OTRO</option>
                            </select>
                        </label>
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Tracking number</span>
                            <input id="trackingNumber" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-mono font-bold focus:outline-none transition-all" placeholder="1234567890">
                        </label>
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">BL number</span>
                            <input id="blNumber" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-mono font-bold focus:outline-none transition-all" placeholder="BL opcional">
                        </label>
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Contenedor</span>
                            <input id="containerNumber" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-mono font-bold focus:outline-none transition-all" placeholder="MSKU1234567">
                        </label>
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Operacion ID</span>
                            <input id="operacionId" type="number" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-mono font-bold focus:outline-none transition-all" placeholder="Opcional">
                        </label>
                        <label class="space-y-2">
                            <span class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">ETA</span>
                            <input id="eta" type="date" class="w-full px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-bold focus:outline-none transition-all cursor-pointer">
                        </label>
                        <div class="md:col-span-2 xl:col-span-3 flex flex-wrap gap-3 pt-2">
                            <button type="submit" id="btnRegistrar" class="primary-button px-6 py-3 text-xs font-black uppercase tracking-widest">Registrar tracking</button>
                            <button type="button" id="btnSincronizar" class="px-6 py-3 rounded-xl bg-white border border-[var(--border)] text-xs font-black uppercase tracking-widest text-[var(--text-secondary)] hover:bg-[var(--surface-2)] transition-all cursor-pointer" disabled>Sincronizar API</button>
                        </div>
                    </form>
                </div>

                <aside class="glass-card p-6 space-y-5">
                    <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Estado actual</p>
                    <p id="estadoActual" class="text-3xl font-black">Sin envio</p>
                    <span id="trackingChip" class="source-chip source-chip--pending">PENDIENTE</span>
                    <div class="space-y-2 text-xs font-semibold text-[var(--text-secondary)]">
                        <p id="apiEndpoint">API: -</p>
                        <p id="trackingConfidence">Confianza: -</p>
                    </div>
                </aside>
            </div>

            <div id="trackingNotice" class="hidden rounded-2xl border border-[var(--warning)] bg-[var(--warning-soft)] px-5 py-4 text-sm font-semibold text-[var(--warning)]"></div>

            <div class="grid grid-cols-1 xl:grid-cols-[1fr_24rem] gap-8">
                <section class="glass-card p-8">
                    <div class="flex items-center justify-between gap-4 border-b border-[var(--border)] pb-4">
                        <div>
                            <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Timeline</p>
                            <h2 class="text-2xl font-black mt-1">Eventos logisticos</h2>
                        </div>
                        <span id="trackingIdBadge" class="px-3 py-1 rounded-full bg-[var(--accent-soft)] text-[var(--accent)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-widest">Sin ID</span>
                    </div>
                    <!-- Leaflet interactive map container -->
                    <div id="map-container" class="mt-6 mb-8 rounded-2xl overflow-hidden border border-[var(--border)] bg-[var(--surface-2)] shadow-sm relative h-80 z-0">
                        <div id="map" class="h-full w-full"></div>
                        <div id="map-overlay-info" class="absolute bottom-4 left-4 z-[1000] bg-white/95 backdrop-blur-md px-4 py-2.5 rounded-xl border border-[var(--border)] shadow-md text-xs font-semibold text-[var(--text-secondary)] pointer-events-none hidden max-w-xs">
                            <p id="map-route-title" class="font-black text-[var(--text-primary)] mb-0.5"></p>
                            <p id="map-route-desc"></p>
                        </div>
                    </div>
                    <div id="timeline" class="mt-8 space-y-6"></div>
                </section>

                <aside class="glass-card p-6">
                    <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em]">Mis tracking</p>
                    <div id="trackingList" class="space-y-3 mt-5"></div>
                </aside>
            </div>
        </section>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
        let currentTrackingId = null;

        function sourceLabel(type) {
            const labels = {
                OFICIAL_API: 'OFICIAL/API',
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
            if (value.includes('MANUAL')) return 'source-chip source-chip--manual';
            return 'source-chip source-chip--pending';
        }

        async function listar() {
            try {
                const res = await fetch(window.ctx + '/api/tracking/listar');
                const payload = await res.json();
                renderLista(payload.data || []);
            } catch (e) {
                renderLista([]);
            }
        }

        async function registrar(e) {
            e.preventDefault();
            const body = {
                proveedor: val('proveedor'),
                trackingNumber: val('trackingNumber'),
                blNumber: val('blNumber'),
                containerNumber: val('containerNumber'),
                operacionId: val('operacionId'),
                eta: val('eta')
            };
            document.getElementById('btnRegistrar').disabled = true;
            try {
                const res = await fetch(window.ctx + '/api/tracking/registrar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'X-CSRF-TOKEN': window.csrfToken || '' },
                    body: JSON.stringify(body)
                });
                const payload = await res.json();
                if (!payload.success) throw new Error(payload.message || 'No se pudo registrar');
                renderDetalle(payload.data || {});
                listar();
            } catch (err) {
                mostrarNotice(err.message || 'Error registrando tracking');
            } finally {
                document.getElementById('btnRegistrar').disabled = false;
            }
        }

        async function sincronizar() {
            if (!currentTrackingId || currentTrackingId < 0) {
                mostrarNotice('Este registro es temporal. Aplica la migracion SQL para sincronizarlo desde BD.');
                return;
            }
            document.getElementById('btnSincronizar').disabled = true;
            try {
                const res = await fetch(window.ctx + '/api/tracking/sincronizar?id=' + currentTrackingId, {
                    method: 'POST',
                    headers: { 'X-CSRF-TOKEN': window.csrfToken || '' }
                });
                const payload = await res.json();
                if (!payload.success) throw new Error(payload.message || 'No se pudo sincronizar');
                renderDetalle(payload.data || {});
                if ((payload.data || {}).sourceType === 'PENDIENTE_CREDENCIALES') {
                    mostrarNotice('Adaptador listo. Falta configurar credenciales del proveedor para consultar la API oficial.');
                }
            } catch (err) {
                mostrarNotice(err.message || 'Error sincronizando tracking');
            } finally {
                document.getElementById('btnSincronizar').disabled = false;
            }
        }

        function renderDetalle(data) {
            const envio = data.envio || {};
            currentTrackingId = Number(envio.id || 0);
            document.getElementById('estadoActual').textContent = envio.estadoActual || 'Registrado';
            const sourceType = envio.sourceType || data.sourceType || 'MANUAL_VERIFICADO';
            const chip = document.getElementById('trackingChip');
            chip.className = chipClass(sourceType);
            chip.textContent = sourceLabel(sourceType);
            document.getElementById('apiEndpoint').textContent = 'API: ' + (data.apiEndpoint || '-');
            document.getElementById('trackingConfidence').textContent = 'Confianza: ' + Math.round(Number(envio.confidence || 0) * 100) + '%';
            document.getElementById('trackingIdBadge').textContent = currentTrackingId ? 'ID ' + currentTrackingId : 'Sin ID';
            document.getElementById('btnSincronizar').disabled = !currentTrackingId;
            if (envio.persistido === false) mostrarNotice(envio.mensajePersistencia || 'Registro temporal.');
            renderTimeline(data.eventos || []);
            updateMapRoute(data);
        }

        function renderTimeline(eventos) {
            const box = document.getElementById('timeline');
            box.innerHTML = '';
            if (!eventos.length) {
                box.innerHTML = '<p class="text-sm font-semibold text-[var(--text-secondary)]">Aun no hay eventos para este envio.</p>';
                return;
            }
            eventos.forEach(ev => {
                const sourceType = ev.sourceType || 'PENDIENTE_VALIDACION';
                const row = document.createElement('div');
                row.className = 'relative pl-9';
                row.innerHTML = '<span class="timeline-dot absolute left-0 top-1.5 w-3 h-3 rounded-full bg-[var(--accent)]"></span>' +
                    '<div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-5 text-[var(--text-secondary)] font-semibold">' +
                    '<div class="flex flex-wrap items-center justify-between gap-3"><p class="font-black text-[var(--text-primary)]">' + escapeHtml(ev.estado || '-') + '</p><span class="' + chipClass(sourceType) + '">' + sourceLabel(sourceType) + '</span></div>' +
                    '<p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">' + escapeHtml(ev.descripcion || '') + '</p>' +
                    '<p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)] mt-3">' + escapeHtml(ev.fuente || '-') + ' | ' + escapeHtml(ev.fechaEvento || ev.fecha_evento || '-') + '</p>' +
                    '</div>';
                box.appendChild(row);
            });
        }

        function renderLista(items) {
            const box = document.getElementById('trackingList');
            box.innerHTML = '';
            if (!items.length) {
                box.innerHTML = '<p class="text-xs text-[var(--text-secondary)] font-semibold">Todavia no tienes tracking persistidos.</p>';
                return;
            }
            items.forEach(item => {
                const btn = document.createElement('button');
                btn.className = 'w-full text-left rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] p-4 hover:border-[var(--accent)] transition-all cursor-pointer';
                btn.innerHTML = '<p class="text-xs font-black text-[var(--text-primary)]">' + escapeHtml(item.proveedor || '-') + ' · ' + escapeHtml(item.estadoActual || '-') + '</p>' +
                    '<p class="text-[10px] text-[var(--text-secondary)] font-semibold mt-1 truncate">' + escapeHtml(item.trackingNumber || item.blNumber || item.containerNumber || 'Sin referencia') + '</p>';
                btn.onclick = () => cargarDetalle(item.id);
                box.appendChild(btn);
            });
        }

        async function cargarDetalle(id) {
            const res = await fetch(window.ctx + '/api/tracking/detalle?id=' + encodeURIComponent(id));
            const payload = await res.json();
            if (payload.success) renderDetalle(payload.data || {});
        }

        function mostrarNotice(text) {
            const box = document.getElementById('trackingNotice');
            box.textContent = text;
            box.classList.remove('hidden');
        }

        function val(id) { return (document.getElementById(id).value || '').trim(); }
        function escapeHtml(value) {
            return String(value || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
        }

        document.getElementById('trackingForm').addEventListener('submit', registrar);
        document.getElementById('btnSincronizar').addEventListener('click', sincronizar);
        // Leaflet Map Logic
        let map = null;
        let routeLine = null;
        let markers = [];

        function initMap() {
            if (map) return;
            map = L.map('map', {
                center: [15.0, -20.0],
                zoom: 2,
                zoomControl: true,
                attributionControl: false
            });

            // Premium elegant light cartodb basemap tiles matching our design tokens
            L.tileLayer('https://{s}.basemaps.cartocdn.com/light_all/{z}/{x}/{y}{r}.png', {
                maxZoom: 18
            }).addTo(map);
        }

        function updateMapRoute(data) {
            initMap();

            // Clean previous layer lines and markers
            if (routeLine) {
                map.removeLayer(routeLine);
                routeLine = null;
            }
            markers.forEach(m => map.removeLayer(m));
            markers = [];

            const envio = data.envio || {};
            const proveedor = (envio.proveedor || 'OTRO').toUpperCase();
            const estado = (envio.estadoActual || 'REGISTRADO').toUpperCase();

            // Geographic coordinates (real locations)
            const shanghai = [31.2304, 121.4737];
            const yokohama = [35.4437, 139.6380];
            const miami = [25.7617, -80.1918];
            const callao = [-12.0564, -77.1186];
            const almacen = [-12.0464, -77.0428];

            let pathCoords = [];
            let routeName = "";
            let pointsInfo = [];

            if (proveedor === 'MAERSK') {
                pathCoords = [shanghai, yokohama, callao, almacen];
                routeName = "Ruta Marítima: Shanghai → Yokohama → Callao → Almacén";
                pointsInfo = [
                    { name: "Puerto de Shanghai", desc: "Origen de carga marítima", coords: shanghai, icon: "🏭" },
                    { name: "Puerto de Yokohama", desc: "Escala de transbordo", coords: yokohama, icon: "⚓" },
                    { name: "Puerto del Callao", desc: "Ingreso / Aduana Marítima 118", coords: callao, icon: "🏛️" },
                    { name: "Almacén Autorizado", desc: "Destino final de importador", coords: almacen, icon: "🏢" }
                ];
            } else if (proveedor === 'DHL' || proveedor === 'FEDEX' || proveedor === 'UPS') {
                pathCoords = [miami, callao, almacen];
                routeName = "Ruta Aérea: Miami (MIA) → Callao (Aduana 235) → Almacén";
                pointsInfo = [
                    { name: "Aeropuerto de Miami", desc: "Origen de despacho exprés", coords: miami, icon: "✈️" },
                    { name: "Aeropuerto Jorge Chávez", desc: "Aduana Aérea y Postal 235", coords: callao, icon: "🏛️" },
                    { name: "Almacén Autorizado", desc: "Destino final de importador", coords: almacen, icon: "🏢" }
                ];
            } else {
                pathCoords = [shanghai, callao, almacen];
                routeName = "Ruta Estándar: Shanghai → Callao → Almacén";
                pointsInfo = [
                    { name: "Puerto de Shanghai", desc: "Origen", coords: shanghai, icon: "🏭" },
                    { name: "Puerto del Callao", desc: "Desaduanamiento", coords: callao, icon: "🏛️" },
                    { name: "Almacén Autorizado", desc: "Destino final", coords: almacen, icon: "🏢" }
                ];
            }

            // Draw dotted line route path
            routeLine = L.polyline(pathCoords, {
                color: '#3b82f6', // var(--accent)
                weight: 4,
                dashArray: '8, 8',
                opacity: 0.85
            }).addTo(map);

            // Populate custom div markers
            pointsInfo.forEach((p, idx) => {
                const isCurrent = checkIsCurrentNode(idx, pointsInfo.length, estado);
                
                const customIcon = L.divIcon({
                    html: `<div class="flex items-center justify-center w-8 h-8 rounded-full border-2 ${isCurrent ? 'bg-blue-600 text-white border-white scale-125 shadow-lg ring-4 ring-blue-200 animate-pulse' : 'bg-white text-gray-600 border-gray-300'} font-semibold text-xs transition-all">
                             ${p.icon}
                           </div>`,
                    className: 'custom-map-icon',
                    iconSize: [32, 32],
                    iconAnchor: [16, 16]
                });

                const marker = L.marker(p.coords, { icon: customIcon }).addTo(map);
                marker.bindPopup(`<strong>${p.name}</strong><br><span class="text-xs text-gray-500 font-semibold">${p.desc}</span>`);
                markers.push(marker);

                if (isCurrent) {
                    marker.openPopup();
                }
            });

            // Show detail info overlay card
            const overlay = document.getElementById('map-overlay-info');
            if (overlay) {
                document.getElementById('map-route-title').textContent = routeName;
                document.getElementById('map-route-desc').innerHTML = `Estado operativo: <span class="font-black text-[#3b82f6] uppercase tracking-wide">${estado}</span>`;
                overlay.classList.remove('hidden');
            }

            // Fit map camera beautifully to route coordinates
            setTimeout(() => {
                map.invalidateSize();
                map.flyToBounds(routeLine.getBounds(), {
                    padding: [50, 50],
                    maxZoom: 6,
                    duration: 1.2
                });
            }, 250);
        }

        function checkIsCurrentNode(idx, total, estado) {
            // Evaluates current status and positions cargo marker accordingly
            if (estado === 'REGISTRADO' && idx === 0) return true;
            if ((estado === 'EN_TRANSITO' || estado === 'ADAPTADOR_CONFIGURADO' || estado === 'PENDIENTE_VALIDACION') && total > 3 && idx === 1) return true;
            if ((estado === 'EN_TRANSITO' || estado === 'ADAPTADOR_CONFIGURADO' || estado === 'PENDIENTE_VALIDACION') && total === 3 && idx === 0) return true;
            if ((estado === 'ARRIBADO' || estado === 'EN_ADUANA' || estado === 'PENDIENTE_CREDENCIALES') && idx === total - 2) return true;
            if ((estado === 'NACIONALIZADO' || estado === 'ENTREGADO') && idx === total - 1) return true;
            return false;
        }

        listar();
        renderTimeline([]);
    </script>
</body>
</html>
