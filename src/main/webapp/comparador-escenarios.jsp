<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    String userNombre = (String) session.getAttribute("usuarioNombre");
    if (userNombre == null) userNombre = "Manuel";
    int usuarioId = (int) session.getAttribute("usuarioId");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Comparar escenarios</title>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .scenario-card { transition: all 0.3s var(--ease-spring); }
        .scenario-card:hover { transform: translateY(-4px); border-color: var(--accent) !important; box-shadow: 0 10px 25px -5px var(--accent-glow) !important; }
        input[type="range"] {
            -webkit-appearance: none;
            width: 100%;
            height: 6px;
            background: var(--surface-2);
            border-radius: 5px;
            outline: none;
            border: none;
        }
        input[type="range"]::-webkit-slider-thumb {
            -webkit-appearance: none;
            width: 18px;
            height: 18px;
            border-radius: 50%;
            background: var(--accent);
            cursor: pointer;
            transition: transform 0.1s;
        }
        input[type="range"]::-webkit-slider-thumb:hover {
            transform: scale(1.2);
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <% request.setAttribute("activePage", "comparador"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl sticky top-0 z-10 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Comparador de decisiones</span>
            </div>
            <div class="text-[10px] uppercase tracking-widest font-black text-[var(--text-tertiary)] flex gap-4">
                <span>Tipo de cambio listo</span>
                <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 my-auto"></span>
            </div>
        </header>

        <!-- Main Workspace Content -->
        <section class="p-8 xl:p-12 max-w-7xl mx-auto w-full space-y-8 flex-1">
            <div class="fade-up">
                <span class="pill-heading">Compara antes de comprar</span>
                <h1 class="text-4xl font-black tracking-tight mt-3">Compara costos antes de decidir</h1>
                <p class="text-sm text-[var(--text-secondary)] font-semibold mt-2 max-w-3xl">Cambia producto, valor, envio y seguro para ver cuanto podria variar tu costo final. La idea es ayudarte a elegir mejor, no obligarte a conocer terminos aduaneros.</p>
            </div>

            <!-- Panel de Entrada de Datos -->
            <div class="glass-card p-8 grid grid-cols-1 lg:grid-cols-4 gap-8 items-center fade-up" style="animation-delay:0.1s">
                <div class="space-y-2">
                    <label class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)] block">Producto o codigo</label>
                    <select id="hsCodeSelect" onchange="actualizarComparacion()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs font-bold focus:outline-none cursor-pointer">
                        <!-- Se cargan dinamicamente -->
                        <option value="8517130000">8517.13.00.00 - Smartphones (impuesto base 0% / MTC)</option>
                        <option value="8471300000" selected>8471.30.00.00 - Laptops portatiles (impuesto base 0%)</option>
                        <option value="3304990000">3304.99.00.00 - Cosmeticos / cuidado de piel (impuesto base 6% / DIGEMID)</option>
                        <option value="6109100000">6109.10.00.00 - Polos de algodon (impuesto base 11%)</option>
                        <option value="2204210000">2204.21.00.00 - Vino de uvas frescas (impuesto base 6% + ISC 25% / DIGESA)</option>
                        <option value="8708999090">8708.99.90.90 - Repuestos automotrices (impuesto base 6%)</option>
                    </select>
                </div>
                <div class="space-y-2">
                    <div class="flex justify-between">
                        <label class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Valor del producto: <span id="fobValText" class="font-bold text-[var(--accent)] font-mono">$ 10,000</span></label>
                    </div>
                    <input type="range" id="fobSlider" min="500" max="100000" step="500" value="10000" oninput="onSliderInput('fob')" onchange="actualizarComparacion()">
                </div>
                <div class="space-y-2">
                    <div class="flex justify-between">
                        <label class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Envio internacional: <span id="fleteValText" class="font-bold text-[var(--accent)] font-mono">$ 1,200</span></label>
                    </div>
                    <input type="range" id="fleteSlider" min="50" max="15000" step="50" value="1200" oninput="onSliderInput('flete')" onchange="actualizarComparacion()">
                </div>
                <div class="space-y-2">
                    <div class="flex justify-between">
                        <label class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--text-tertiary)]">Seguro: <span id="seguroValText" class="font-bold text-[var(--accent)] font-mono">$ 150</span></label>
                    </div>
                    <input type="range" id="seguroSlider" min="10" max="3000" step="10" value="150" oninput="onSliderInput('seguro')" onchange="actualizarComparacion()">
                </div>
            </div>

            <!-- CONTENEDOR 1: beneficio de origen vs pago regular -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                <div class="lg:col-span-2 space-y-6">
                    <div class="glass-card p-8 space-y-6">
                        <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                            <div>
                                <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)]">Escenario A: comprar con o sin beneficio de origen</h3>
                                <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Compara si un documento de origen puede ayudarte a pagar menos.</p>
                            </div>
                            <span class="px-3 py-1 rounded-full bg-blue-50 border border-blue-200 text-blue-700 text-[10px] font-black uppercase tracking-widest">Con beneficio vs regular</span>
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <!-- Con beneficio -->
                            <div class="p-6 rounded-2xl border border-emerald-500/20 bg-emerald-50/30 flex flex-col justify-between scenario-card dark:bg-emerald-950/10">
                                <div>
                                    <div class="flex items-center gap-2">
                                        <span class="text-xl">*</span>
                                        <h4 class="text-xs font-black uppercase text-emerald-600 tracking-wider">Con documento de origen</h4>
                                    </div>
                                    <p class="text-sm text-[var(--text-secondary)] font-semibold mt-3">Puede reducir el impuesto base si el producto y el pais califican.</p>
                                </div>
                                <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2">
                                    <div class="flex justify-between text-xs font-semibold"><span class="text-[var(--text-tertiary)]">Impuesto base:</span><span class="font-bold text-[var(--text-primary)] font-mono">USD 0.00 (0%)</span></div>
                                    <div class="flex justify-between text-xs font-semibold"><span class="text-[var(--text-tertiary)]">Total de impuestos:</span><span id="tlcTaxes" class="font-bold text-emerald-600 font-mono">S/ 0.00</span></div>
                                </div>
                            </div>

                            <!-- Sin beneficio -->
                            <div class="p-6 rounded-2xl border border-rose-500/20 bg-rose-50/30 flex flex-col justify-between scenario-card dark:bg-rose-950/10">
                                <div>
                                    <div class="flex items-center gap-2">
                                        <span class="text-xl">!</span>
                                        <h4 class="text-xs font-black uppercase text-rose-600 tracking-wider">Sin beneficio de origen</h4>
                                    </div>
                                    <p class="text-sm text-[var(--text-secondary)] font-semibold mt-3">Pagas la tasa regular calculada para ese producto.</p>
                                </div>
                                <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2">
                                    <div class="flex justify-between text-xs font-semibold"><span class="text-[var(--text-tertiary)]">Impuesto base regular:</span><span id="regularTasa" class="font-bold text-[var(--text-primary)] font-mono">USD 0.00 (6%)</span></div>
                                    <div class="flex justify-between text-xs font-semibold"><span class="text-[var(--text-tertiary)]">Total de impuestos:</span><span id="noTlcTaxes" class="font-bold text-rose-600 font-mono">S/ 0.00</span></div>
                                </div>
                            </div>
                        </div>

                        <!-- Fila informativa de Ahorro -->
                        <div class="p-5 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl flex items-center justify-between text-xs font-bold text-[var(--accent)]">
                            <span>Ahorro estimado:</span>
                            <span id="tlcAhorro" class="text-lg font-black font-mono">S/ 0.00 (0%)</span>
                        </div>
                    </div>
                </div>

                <div class="glass-card p-6 flex flex-col justify-between">
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Consejo de ahorro</p>
                        <h4 class="text-lg font-black text-[var(--text-primary)] mt-1">Como pagar menos sin improvisar</h4>
                        <p class="text-xs text-[var(--text-secondary)] leading-relaxed font-semibold mt-4" id="mentorTlcText">
                            Si compras desde un pais con beneficio aplicable, pide al proveedor el documento de origen antes de pagar. Sin ese sustento, normalmente se calcula con la tasa regular.
                        </p>
                    </div>
                    <a href="observatorio-hs.jsp" class="primary-button w-full py-4 text-center text-xs font-black uppercase tracking-widest block">Ver tendencias por producto</a>
                </div>
            </div>

            <!-- CONTENEDOR 2: primera importacion vs importador recurrente -->
            <div class="glass-card p-8 space-y-6 fade-up" style="animation-delay:0.2s">
                <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                    <div>
                        <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)]">Escenario B: primera importacion o importador recurrente</h3>
                        <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Compara como puede variar el monto retenido segun tu historial.</p>
                    </div>
                    <span class="px-3 py-1 rounded-full bg-purple-50 border border-purple-200 text-purple-700 text-[10px] font-black uppercase tracking-widest">Monto retenido</span>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <!-- Buen Contribuyente (0%) -->
                    <div class="p-6 rounded-2xl border border-emerald-500/20 bg-emerald-50/20 flex flex-col justify-between scenario-card dark:bg-emerald-950/10">
                        <div>
                            <span class="px-2 py-1 rounded bg-emerald-100 text-emerald-700 text-[8px] font-black uppercase">Exento (0%)</span>
                            <h4 class="text-sm font-black text-[var(--text-primary)] mt-3">Con historial favorable</h4>
                            <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2 leading-relaxed">Algunos contribuyentes pueden no tener este cobro adicional. Usalo solo como referencia.</p>
                        </div>
                        <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2 text-xs font-semibold">
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Monto retenido:</span><span class="font-bold text-emerald-600 font-mono">S/ 0.00</span></div>
                            <div class="flex justify-between border-t border-[var(--border)] pt-2 font-bold"><span class="text-[var(--text-primary)]">Total de impuestos:</span><span id="perfBuenTaxes" class="font-mono text-[var(--text-primary)]">S/ 0.00</span></div>
                        </div>
                    </div>

                    <!-- Estándar Recurrente (3.5%) -->
                    <div class="p-6 rounded-2xl border border-blue-500/20 bg-blue-50/20 flex flex-col justify-between scenario-card dark:bg-blue-950/10">
                        <div>
                            <span class="px-2 py-1 rounded bg-blue-100 text-blue-700 text-[8px] font-black uppercase">Estándar (3.5%)</span>
                            <h4 class="text-sm font-black text-[var(--text-primary)] mt-3">Importador frecuente</h4>
                            <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2 leading-relaxed">Si ya importas con cierta frecuencia, el monto adicional suele ser menor que en una primera operacion.</p>
                        </div>
                        <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2 text-xs font-semibold">
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Monto retenido:</span><span id="perfEstPer" class="font-bold text-[var(--text-primary)] font-mono">S/ 0.00</span></div>
                            <div class="flex justify-between border-t border-[var(--border)] pt-2 font-bold"><span class="text-[var(--text-primary)]">Total de impuestos:</span><span id="perfEstTaxes" class="font-mono text-[var(--text-primary)]">S/ 0.00</span></div>
                        </div>
                    </div>

                    <!-- Nuevo Importador (10%) -->
                    <div class="p-6 rounded-2xl border border-rose-500/20 bg-rose-50/20 flex flex-col justify-between scenario-card dark:bg-rose-950/10">
                        <div>
                            <span class="px-2 py-1 rounded bg-rose-100 text-rose-700 text-[8px] font-black uppercase">Crítico (10%)</span>
                            <h4 class="text-sm font-black text-[var(--text-primary)] mt-3">Primera importacion</h4>
                            <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2 leading-relaxed">Si es tu primera operacion, considera un monto adicional de referencia para no quedarte corto de caja.</p>
                        </div>
                        <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2 text-xs font-semibold">
                            <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Monto retenido:</span><span id="perfNuePer" class="font-bold text-rose-600 font-mono">S/ 0.00</span></div>
                            <div class="flex justify-between border-t border-[var(--border)] pt-2 font-bold"><span class="text-[var(--text-primary)]">Total de impuestos:</span><span id="perfNueTaxes" class="font-mono text-rose-600 font-bold">S/ 0.00</span></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- CONTENEDOR 3: forma de retirar la carga -->
            <div class="grid grid-cols-1 lg:grid-cols-12 gap-8 fade-up" style="animation-delay:0.3s">
                <div class="lg:col-span-8 glass-card p-8 space-y-6">
                    <div class="flex justify-between items-center border-b border-[var(--border)] pb-4">
                        <div>
                            <h3 class="text-xs font-black uppercase tracking-widest text-[var(--text-secondary)]">Escenario C: retirar rapido o dejar en almacen</h3>
                            <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Compara preparar todo antes de la llegada contra resolverlo despues.</p>
                        </div>
                        <span class="px-3 py-1 rounded-full bg-emerald-50 border border-emerald-200 text-emerald-700 text-[10px] font-black uppercase tracking-widest">Tiempo y almacen</span>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <!-- Preparado antes -->
                        <div class="p-6 rounded-2xl border border-emerald-500/20 bg-emerald-50/10 flex flex-col justify-between scenario-card dark:bg-emerald-950/10">
                            <div>
                                <div class="flex items-center gap-2">
                                    <span class="text-lg">*</span>
                                    <h4 class="text-xs font-black uppercase text-emerald-600 tracking-wider">Preparado antes de llegar</h4>
                                </div>
                                <ul class="text-xs text-[var(--text-secondary)] font-semibold mt-4 space-y-2 list-disc pl-4">
                                    <li>Preparas documentos antes de que la carga llegue.</li>
                                    <li><strong>Menos espera:</strong> Puedes evitar dias innecesarios en almacen.</li>
                                    <li><strong>Mas orden:</strong> Llegas con costos y permisos revisados.</li>
                                    <li><strong>Ahorro:</strong> Menos cargos por almacenaje temporal.</li>
                                </ul>
                            </div>
                            <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2 text-xs font-semibold">
                                <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Cargo adicional:</span><span class="font-bold text-[var(--text-primary)] font-mono">$ 0.00</span></div>
                                <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Costo Total Estimado:</span><span id="desAnticipadoTotal" class="font-bold text-emerald-600 font-mono">S/ 0.00</span></div>
                            </div>
                        </div>

                        <!-- Resolver despues -->
                        <div class="p-6 rounded-2xl border border-rose-500/20 bg-rose-50/10 flex flex-col justify-between scenario-card dark:bg-rose-950/10">
                            <div>
                                <div class="flex items-center gap-2">
                                    <span class="text-lg">!</span>
                                    <h4 class="text-xs font-black uppercase text-rose-600 tracking-wider">Resolver despues de llegar</h4>
                                </div>
                                <ul class="text-xs text-[var(--text-secondary)] font-semibold mt-4 space-y-2 list-disc pl-4">
                                    <li>La carga llega y luego empiezas a cerrar pendientes.</li>
                                    <li><strong>Mas costo:</strong> Pueden aparecer cargos de manipuleo y almacen.</li>
                                    <li><strong>Mas presion:</strong> Hay plazos para retirar o regularizar.</li>
                                    <li><strong>Mas revision:</strong> Puede tomar mas tiempo si faltan datos.</li>
                                </ul>
                            </div>
                            <div class="mt-6 border-t border-[var(--border)] pt-4 space-y-2 text-xs font-semibold">
                                <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Cargo de almacen:</span><span class="font-bold text-rose-600 font-mono">$ 150.00</span></div>
                                <div class="flex justify-between"><span class="text-[var(--text-tertiary)]">Costo Total Estimado:</span><span id="desDiferidoTotal" class="font-bold text-rose-600 font-mono">S/ 0.00</span></div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="lg:col-span-4 bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 space-y-6 shadow-sm flex flex-col justify-between">
                    <div>
                        <p class="text-[10px] font-black text-[var(--accent)] uppercase tracking-[0.22em] mb-4">Comparacion visual</p>
                        <div class="h-56 relative">
                            <canvas id="scenarioChart"></canvas>
                        </div>
                    </div>
                    <div class="p-4 bg-[var(--surface-0)] border border-[var(--border)] rounded-2xl">
                        <p class="text-[9px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Resultado simple</p>
                        <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-1" id="despachoAhorroText">
                            Preparar la importacion antes de que llegue puede reducir cargos de almacen y esperas.
                        </p>
                    </div>
                </div>
            </div>
        </section>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
        window.usuarioId = <%= usuarioId %>;
        let chart = null;

        function onSliderInput(type) {
            const val = document.getElementById(type + 'Slider').value;
            document.getElementById(type + 'ValText').innerText = '$ ' + Number(val).toLocaleString('en-US');
        }

        async function actualizarComparacion() {
            const hsCode = document.getElementById('hsCodeSelect').value;
            const fob = document.getElementById('fobSlider').value;
            const flete = document.getElementById('fleteSlider').value;
            const seguro = document.getElementById('seguroSlider').value;

            try {
                const url = `${window.ctx}/api/costo/comparar?hsCode=${hsCode}&fob=${fob}&flete=${flete}&seguro=${seguro}&paisOrigen=CN&incoterm=FOB&usuarioId=${window.usuarioId}`;
                const res = await fetch(url);
                const data = await res.json();

                if (data.error) {
                    console.error("Error comparador:", data.error);
                    return;
                }

                // Render Escenario A: beneficio de origen
                const tlc = data.tlcChina.CON_TLC_CHINA;
                const noTlc = data.tlcChina.SIN_TLC_CHINA;
                const avTasa = noTlc.arancel > 0 ? (noTlc.arancel / noTlc.cif * 100).toFixed(0) : '6';

                document.getElementById('tlcTaxes').innerText = 'S/ ' + tlc.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2, maximumFractionDigits: 2});
                document.getElementById('regularTasa').innerText = `USD ${noTlc.arancel.toFixed(2)} (${avTasa}%)`;
                document.getElementById('noTlcTaxes').innerText = 'S/ ' + noTlc.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2, maximumFractionDigits: 2});

                const ahorroTlc = noTlc.totalSoles - tlc.totalSoles;
                const pctAhorroTlc = noTlc.totalSoles > 0 ? (ahorroTlc / noTlc.totalSoles * 100) : 0;
                document.getElementById('tlcAhorro').innerText = `S/ ${ahorroTlc.toLocaleString('es-PE', {minimumFractionDigits: 2})} (${pctAhorroTlc.toFixed(1)}%)`;

                // Render Escenario B: historial del importador
                const perfBuen = data.perfiles.BUEN_CONTRIBUYENTE;
                const perfEst = data.perfiles.ESTANDAR;
                const perfNue = data.perfiles.PRIMERA_IMPORTACION;

                document.getElementById('perfBuenTaxes').innerText = 'S/ ' + perfBuen.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2});
                
                document.getElementById('perfEstPer').innerText = 'S/ ' + (perfEst.percepcion * data.tipoCambio).toLocaleString('es-PE', {minimumFractionDigits: 2});
                document.getElementById('perfEstTaxes').innerText = 'S/ ' + perfEst.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2});

                document.getElementById('perfNuePer').innerText = 'S/ ' + (perfNue.percepcion * data.tipoCambio).toLocaleString('es-PE', {minimumFractionDigits: 2});
                document.getElementById('perfNueTaxes').innerText = 'S/ ' + perfNue.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2});

                // Render Escenario C: forma de retiro
                const desAnt = data.despacho.ANTICIPADO;
                const desDif = data.despacho.DIFERIDO;

                document.getElementById('desAnticipadoTotal').innerText = 'S/ ' + desAnt.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2});
                document.getElementById('desDiferidoTotal').innerText = 'S/ ' + desDif.totalSoles.toLocaleString('es-PE', {minimumFractionDigits: 2});

                const ahorroDespacho = desDif.totalSoles - desAnt.totalSoles;
                document.getElementById('despachoAhorroText').innerHTML = `<strong>Ahorro estimado:</strong> Preparar la importacion antes de que llegue puede ahorrar <strong>S/ ${ahorroDespacho.toLocaleString('es-PE', {minimumFractionDigits: 0})}</strong> al reducir almacenaje y esperas.`;

                // Actualizar Gráfico
                updateChart(
                    [tlc.totalSoles, noTlc.totalSoles],
                    [perfBuen.totalSoles, perfEst.totalSoles, perfNue.totalSoles],
                    [desAnt.totalSoles, desDif.totalSoles]
                );
            } catch (e) {
                console.error("Error al actualizar simulación de escenarios:", e);
            }
        }

        function updateChart(tlcData, perfData, desData) {
            const ctx = document.getElementById('scenarioChart').getContext('2d');
            if (chart) chart.destroy();

            chart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: ['Con origen', 'Regular', 'Historial ok', 'Frecuente', 'Primera vez', 'Antes', 'Despues'],
                    datasets: [{
                        label: 'Total estimado (S/)',
                        data: [...tlcData, ...perfData, ...desData],
                        backgroundColor: [
                            '#10b981', '#ef4444', 
                            '#10b981', '#3b82f6', '#f59e0b',
                            '#10b981', '#ef4444'
                        ],
                        borderWidth: 0,
                        borderRadius: 8
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            grid: { display: false },
                            ticks: { font: { size: 9, family: 'Outfit' } }
                        },
                        x: {
                            grid: { display: false },
                            ticks: { font: { size: 9, family: 'Outfit' } }
                        }
                    }
                }
            });
        }

        window.onload = () => {
            onSliderInput('fob');
            onSliderInput('flete');
            onSliderInput('seguro');
            actualizarComparacion();
        };
    </script>
</body>
</html>
