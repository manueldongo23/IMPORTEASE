<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
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
    <title>ImportEase - Calcular impuestos</title>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/knowledge-base.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <style>
        .dam-label { font-size: 8px; font-weight: 800; color: var(--text-tertiary); text-transform: uppercase; letter-spacing: 0.1em; }
        .dam-value { font-size: 11px; font-weight: 700; color: var(--text-primary); }
        .canal-verde { background: var(--success); box-shadow: 0 0 15px var(--success-soft); }
        .canal-naranja { background: var(--warning); box-shadow: 0 0 15px var(--warning-soft); }
        .canal-rojo { background: var(--danger); box-shadow: 0 0 15px var(--danger-soft); }
        input:focus, select:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "calculadora"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="flex items-center gap-4">
                <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                    <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                    <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest">Calculo simple de impuestos</span>
                </div>
            </div>
            <div class="flex items-center gap-2">
                <button onclick="openKnowledgePanel('dam', { actionLabel: 'Ver ejemplo de declaracion', actionCallback: verDAM })" class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] hover:text-[var(--accent-hover)] transition-all bg-[var(--accent-soft)] border border-[var(--accent-glow)] px-4 py-2 rounded-xl cursor-pointer">
                    Entender declaracion
                </button>
                <button onclick="verDAM()" class="text-[10px] font-black uppercase tracking-widest text-[var(--text-primary)] hover:bg-[var(--surface-2)] transition-all bg-white border border-[var(--border)] px-4 py-2 rounded-xl cursor-pointer">
                    Ver ejemplo
                </button>
            </div>
        </header>

        <div class="p-10 max-w-screen-2xl mx-auto w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest mb-4">&larr; Volver a Importar paso a paso</a>
            <!-- Banner de Advertencia Referencial -->
            <div class="bg-[var(--warning-soft)] border border-[var(--warning)]/20 rounded-2xl p-4 flex items-center gap-4 backdrop-blur-md mb-8">
                <span class="text-2xl">⚠️</span>
                <div class="text-xs text-[var(--warning)] font-semibold">
                    <strong class="text-[var(--warning)] font-bold uppercase tracking-widest text-[10px] block mb-1">Calculo referencial</strong>
                    Usa esta pantalla para estimar. Antes de comprar o embarcar, valida los montos finales con SUNAT, la entidad del permiso o tu agente de aduanas.
                </div>
            </div>

            <div class="grid grid-cols-12 gap-10">
                <!-- INPUTS -->
                <div class="col-span-12 lg:col-span-5 space-y-6">
                    <div class="glass-card p-8 space-y-6">
                        <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                            <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)]">1. Producto y envio</h3>
                            <div class="flex items-center gap-2">
                                 <span class="text-[10px] font-bold text-[var(--text-tertiary)]">¿Primera vez?</span>
                                 <input type="checkbox" id="checkPrimeraVez" onchange="calcular()" class="accent-[var(--accent)] cursor-pointer">
                             </div>
                        </div>
                        
                        <div class="grid grid-cols-2 gap-6 text-xs font-semibold text-[var(--text-secondary)]">
                            <div class="space-y-2">
                                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest">Valor del producto (USD)</label>
                                <input type="number" id="valFob" value="10000" oninput="calcular()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 font-bold focus:border-[var(--accent)] outline-none text-[var(--text-primary)] transition-all">
                            </div>
                            <div class="space-y-2">
                                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest">Cantidad</label>
                                <input type="number" id="valCant" value="500" oninput="calcular()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 font-bold focus:border-[var(--accent)] outline-none text-[var(--text-primary)] transition-all">
                            </div>
                            <div class="space-y-2">
                                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest">Envio internacional</label>
                                <input type="number" id="valFlete" value="1500" oninput="calcular()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 font-bold focus:border-[var(--accent)] outline-none text-[var(--text-primary)] transition-all">
                            </div>
                            <div class="space-y-2">
                                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest">Seguro</label>
                                <input type="number" id="valSeguro" value="120" oninput="calcular()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 font-bold focus:border-[var(--accent)] outline-none text-[var(--text-primary)] transition-all">
                            </div>
                        </div>

                        <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)] pt-4 border-t border-[var(--border)]">2. Precio de venta</h3>
                        <div class="space-y-4">
                            <div class="p-5 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl">
                                <label class="text-[9px] font-black text-[var(--accent)] uppercase tracking-widest mb-1 block">Precio de venta por unidad (USD)</label>
                                <input type="number" id="valPrecioVenta" value="65" oninput="calcular()" class="w-full bg-transparent border-none text-3xl font-black text-[var(--text-primary)] focus:ring-0 focus:outline-none p-0">
                            </div>
                            <p class="text-[10px] text-[var(--text-tertiary)] font-semibold italic">Usa un precio realista que cubra gastos locales y margen.</p>
                        </div>
                    </div>

                    <button id="btnGuardar" onclick="guardar()" class="primary-button w-full py-5 text-xs font-black uppercase tracking-widest transition-all">
                        Guardar estimacion
                    </button>
                </div>

                <!-- OUTPUTS / RESULTS -->
                <div class="col-span-12 lg:col-span-7 space-y-6">
                    <div class="grid grid-cols-2 gap-6">
                        <div class="glass-card p-6 flex flex-col justify-between">
                            <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest">ROI Proyectado</p>
                            <p id="resROI" class="text-4xl font-black mt-3">0%</p>
                        </div>
                        <div class="glass-card p-6 flex flex-col justify-between">
                            <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest">Ganancia Neta Total</p>
                            <p id="resGanancia" class="text-3xl font-black text-[var(--text-primary)] mt-3">$ 0.00</p>
                        </div>
                    </div>

                    <div class="glass-card p-8 space-y-6">
                        <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                            <div>
                                <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)]">Detalle de impuestos</h3>
                                <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Estimado sobre la base de producto, envio y seguro</p>
                            </div>
                            <div id="canalBadge" class="px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest text-white">Revision pendiente</div>
                        </div>

                        <div class="space-y-4 font-semibold text-xs text-[var(--text-secondary)] font-sans">
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Base para impuestos (producto + envio + seguro)</span><span id="resCif" class="font-bold text-[var(--text-primary)]">$ 0.00</span></div>
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Impuesto base (<span id="txtAV">0</span>%)</span><span id="resAV" class="font-bold text-[var(--text-primary)]">$ 0.00</span></div>
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">IGV (16%)</span><span id="resIGV" class="font-bold text-[var(--text-primary)]">$ 0.00</span></div>
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">IPM (2%)</span><span id="resIPM" class="font-bold text-[var(--text-primary)]">$ 0.00</span></div>
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Monto retenido (<span id="txtPerP">3.5</span>%)</span><span id="resPer" class="font-bold text-[var(--warning)]">$ 0.00</span></div>
                            
                            <div class="border-t border-[var(--border)] pt-4 flex justify-between items-end font-sans">
                                <span class="text-xs font-black uppercase text-[var(--accent)]">Total de impuestos estimados</span>
                                <span id="resTotalImp" class="text-2xl font-black text-[var(--text-primary)] font-mono">$ 0.00</span>
                            </div>
                        </div>

                        <div class="h-44">
                            <canvas id="costChart"></canvas>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </main>

    <!-- Ejemplo de declaracion referencial -->
    <div id="modalDAM" class="fixed inset-0 z-50 hidden items-center justify-center p-6">
        <div class="absolute inset-0 bg-black/60 backdrop-blur-sm" onclick="cerrarDAM()"></div>
        <div class="bg-white rounded-3xl w-full max-w-4xl p-10 relative overflow-y-auto max-h-[90vh] shadow-2xl border border-[var(--border)]">
            <div class="flex justify-between items-start mb-8 border-b-2 border-[var(--nav-bg)] pb-4">
                <div class="flex items-center gap-4">
                    <div style="font-family: 'Outfit', sans-serif; font-size: 24px; font-weight: 900; letter-spacing: -1px; color: var(--nav-bg)">SUNAT</div>
                    <div>
                        <h2 class="text-xl font-black text-[var(--text-primary)] tracking-tight">Ejemplo de declaracion</h2>
                        <p class="text-[8px] font-black text-[var(--text-tertiary)] uppercase tracking-tighter">Vista referencial para entender el formato</p>
                    </div>
                </div>
                <div class="text-right">
                    <p class="dam-label">Nro. Declaración</p>
                    <p class="text-lg font-black text-[var(--text-primary)] font-mono">118-2026-10-<span id="damSerial">000000</span></p>
                </div>
            </div>

            <div class="grid grid-cols-3 gap-0 border-2 border-[var(--nav-bg)] text-xs font-semibold text-[var(--text-secondary)]">
                <div class="p-4 border-r-2 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">1.1 Importador</p><p class="dam-value"><%= com.importease.proyecto.service.HtmlUtil.escape((String) session.getAttribute("usuarioNombre")) %></p></div>
                <div class="p-4 border-r-2 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">1.2 RUC</p><p class="dam-value">20601234567</p></div>
                <div class="p-4 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">1.3 Aduana</p><p class="dam-value">118 - MARÍTIMA DEL CALLAO</p></div>
                
                <div class="p-4 border-r-2 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">2.1 Documento transporte</p><p class="dam-value">2026-4521</p></div>
                <div class="p-4 border-r-2 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">2.2 Vía Transp.</p><p class="dam-value">1 - MARÍTIMA</p></div>
                <div class="p-4 border-b-2 border-[var(--nav-bg)]"><p class="dam-label">2.3 País Origen</p><p id="damOrigen" class="dam-value">CHINA</p></div>
                
                <div class="col-span-3 p-3 bg-[var(--surface-2)] border-b-2 border-[var(--nav-bg)]"><p class="dam-label text-center">Descripción de Mercancía</p></div>
                <div class="col-span-3 p-4 border-b-2 border-[var(--nav-bg)]">
                    <p id="damDesc" class="font-bold text-[var(--text-primary)] text-sm italic"></p>
                    <p id="damHs" class="text-xs font-mono text-[var(--text-tertiary)] mt-2">Partida: 0000.00.00.00</p>
                </div>

                <div class="p-4 border-r-2 border-[var(--nav-bg)]"><p class="dam-label">Valor del producto</p><p id="damFob" class="dam-value">$ 0.00</p></div>
                <div class="p-4 border-r-2 border-[var(--nav-bg)]"><p class="dam-label">Flete / Seguro</p><p id="damFlete" class="dam-value">$ 0.00</p></div>
                <div class="p-4 bg-[var(--nav-bg)] text-white"><p class="dam-label text-white/50">Base total</p><p id="damCif" class="text-base font-black">$ 0.00</p></div>
            </div>

            <div class="mt-8 flex justify-between items-center pt-4 border-t border-[var(--border)]">
                <p class="text-[9px] text-[var(--text-tertiary)] font-semibold">Ejemplo creado por ImportEase para orientarte. No es una declaracion oficial.</p>
                <button onclick="cerrarDAM()" class="primary-button py-2.5 px-6 text-xs uppercase">Cerrar ejemplo</button>
            </div>
        </div>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        let chart = null;
        const params = new URLSearchParams(window.location.search);

        function init() {
            if (params.get('fob')) document.getElementById('valFob').value = params.get('fob');
            if (params.get('cant')) document.getElementById('valCant').value = params.get('cant');
            if (params.get('flete')) document.getElementById('valFlete').value = params.get('flete');
            
            window.hsCode = params.get('hsCode') || '000000';
            window.hsDesc = decodeURIComponent(params.get('desc') || 'Producto Genérico');
            window.hsAv = parseFloat(params.get('av')) || 6;
            
            document.getElementById('txtAV').innerText = window.hsAv;
            document.getElementById('damSerial').innerText = Math.floor(Math.random() * 999999).toString().padStart(6, '0');

            calcular();
        }

        function calcular() {
            const fob = parseFloat(document.getElementById('valFob').value) || 0;
            const flete = parseFloat(document.getElementById('valFlete').value) || 0;
            const seguro = parseFloat(document.getElementById('valSeguro').value) || 0;
            const cant = parseInt(document.getElementById('valCant').value) || 1;
            const precioVenta = parseFloat(document.getElementById('valPrecioVenta').value) || 0;
            const primeraVez = document.getElementById('checkPrimeraVez').checked;

            const cif = fob + flete + seguro;
            const av = cif * (window.hsAv / 100);
            const igv = (cif + av) * 0.16;
            const ipm = (cif + av) * 0.02;
            const perP = primeraVez ? 10 : 3.5;
            const percepcion = (cif + av + igv + ipm) * (perP / 100);
            const totalImp = av + igv + ipm + percepcion;

            const costoTotal = cif + totalImp + 300; // 300 de gastos locales fijos para el cálculo
            const costoUnitario = costoTotal / cant;
            const gananciaUnit = precioVenta - costoUnitario;
            const gananciaTotal = gananciaUnit * cant;
            const roi = (gananciaTotal / costoTotal) * 100;

            // Update DOM
            document.getElementById('resCif').innerText = '$ ' + cif.toFixed(2);
            document.getElementById('resAV').innerText = '$ ' + av.toFixed(2);
            document.getElementById('resIGV').innerText = '$ ' + igv.toFixed(2);
            document.getElementById('resIPM').innerText = '$ ' + ipm.toFixed(2);
            document.getElementById('resPer').innerText = '$ ' + percepcion.toFixed(2);
            document.getElementById('txtPerP').innerText = perP;
            document.getElementById('resTotalImp').innerText = '$ ' + totalImp.toFixed(2);
            document.getElementById('resROI').innerText = roi.toFixed(1) + '%';
            document.getElementById('resROI').className = `text-4xl font-black mt-3 ${roi > 20 ? 'text-[var(--success)]' : 'text-[var(--danger)]'}`;
            document.getElementById('resGanancia').innerText = '$ ' + gananciaTotal.toLocaleString('en-US', {minimumFractionDigits: 2, maximumFractionDigits: 2});

            // Canal Logic
            const canal = document.getElementById('canalBadge');
            if (window.hsAv > 6 || fob > 15000) { 
                canal.innerText = "Revision alta"; 
                canal.className = "px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest text-white canal-rojo"; 
            } else if (fob > 5000) { 
                canal.innerText = "Revision media"; 
                canal.className = "px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest text-white canal-naranja"; 
            } else { 
                canal.innerText = "Revision baja"; 
                canal.className = "px-4 py-1.5 rounded-full text-[9px] font-black uppercase tracking-widest text-white canal-verde"; 
            }

            updateChart(fob, totalImp, flete + seguro);
            updateDAM(fob, flete, seguro, cif);
        }

        function updateChart(fob, taxes, logistics) {
            const ctx = document.getElementById('costChart').getContext('2d');
            if (chart) chart.destroy();
            chart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['Costos'],
                    datasets: [
                        { label: 'Producto', data: [fob], backgroundColor: '#0f1729' },
                        { label: 'Impuestos', data: [taxes], backgroundColor: '#3b82f6' },
                        { label: 'Envio y seguro', data: [logistics], backgroundColor: '#94a3b8' }
                    ]
                },
                options: {
                    indexAxis: 'y',
                    plugins: { legend: { display: false } },
                    scales: { x: { stacked: true, display: false }, y: { stacked: true, display: false } },
                    maintainAspectRatio: false
                }
            });
        }

        function updateDAM(fob, flete, seguro, cif) {
            document.getElementById('damDesc').innerText = window.hsDesc;
            document.getElementById('damHs').innerText = 'Codigo de producto: ' + window.hsCode;
            document.getElementById('damFob').innerText = '$ ' + fob.toFixed(2);
            document.getElementById('damFlete').innerText = '$ ' + (flete + seguro).toFixed(2);
            document.getElementById('damCif').innerText = '$ ' + cif.toFixed(2);
        }

        async function guardar() {
            const btn = document.getElementById('btnGuardar');
            btn.innerText = 'Procesando...';
            btn.disabled = true;

            const data = {
                hsCode: window.hsCode,
                fob: document.getElementById('valFob').value,
                flete: document.getElementById('valFlete').value,
                seguro: document.getElementById('valSeguro').value,
                tipo: 'COMERCIAL'
            };

            try {
                const res = await fetch(`<%= request.getContextPath() %>/api/importacion/cotizar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(data)
                });
                if(res.ok) {
                    window.location.href = 'dashboard.jsp';
                } else {
                    showNotification('Error', 'Error al guardar cotización', false);
                    btn.innerText = 'Confirmar Operación Comercial';
                    btn.disabled = false;
                }
            } catch(e) {
                showNotification('Error', 'Error de conexión', false);
                btn.disabled = false;
            }
        }

        function verDAM() { document.getElementById('modalDAM').classList.remove('hidden'); document.getElementById('modalDAM').classList.add('flex'); }
        function cerrarDAM() { document.getElementById('modalDAM').classList.add('hidden'); }

        window.onload = init;
    </script>
</body>
</html>
