import sys

with open('C:\\Users\\Manuel_Dongo\\Music\\Importease_Aduanero_Final\\src\\main\\webapp\\evaluacion.jsp', 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_html = """                <div id="stepGroup-3" class="step-content">
                    <!-- Header Section -->
                    <div class="flex items-center justify-between mb-8">
                        <div>
                            <span class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest block mb-1">PASO 3 DE 4</span>
                            <h2 class="text-3xl font-black text-[#1a1d2e] tracking-tight">¿Cuánto podría costar?</h2>
                            <p class="text-xs text-[#5a6275] font-semibold mt-2">Calculamos un estimado basado en los valores que ingreses. Podrás ajustarlos después.</p>
                        </div>
                        <!-- Right side illustration container -->
                        <div class="hidden md:block w-48 h-24 relative">
                            <svg viewBox="0 0 200 100" fill="none" xmlns="http://www.w3.org/2000/svg" class="w-full h-full drop-shadow-lg">
                                <rect x="20" y="20" width="40" height="60" rx="8" fill="#818CF8" opacity="0.9"/>
                                <rect x="26" y="26" width="28" height="15" rx="3" fill="#C7D2FE"/>
                                <circle cx="30" cy="50" r="3" fill="white"/>
                                <circle cx="40" cy="50" r="3" fill="white"/>
                                <circle cx="50" cy="50" r="3" fill="white"/>
                                <circle cx="30" cy="60" r="3" fill="white"/>
                                <circle cx="40" cy="60" r="3" fill="white"/>
                                <circle cx="50" cy="60" r="3" fill="white"/>
                                <circle cx="30" cy="70" r="3" fill="white"/>
                                <circle cx="40" cy="70" r="3" fill="white"/>
                                <circle cx="50" cy="70" r="3" fill="white"/>
                                <rect x="70" y="30" width="100" height="40" rx="4" fill="white" stroke="#E0D9FF" stroke-width="2"/>
                                <path d="M75 55L90 45L110 50L130 35L145 40L160 30" stroke="#5B50F0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                <rect x="90" y="55" width="4" height="10" fill="#C7D2FE"/>
                                <rect x="110" y="50" width="4" height="15" fill="#A5B4FC"/>
                                <rect x="130" y="40" width="4" height="25" fill="#818CF8"/>
                                <rect x="150" y="30" width="4" height="35" fill="#6366F1"/>
                                <circle cx="165" cy="70" r="15" fill="#A5B4FC"/>
                                <text x="165" y="74" fill="white" font-size="10" font-weight="bold" font-family="sans-serif" text-anchor="middle">S/.</text>
                            </svg>
                        </div>
                    </div>

                    <!-- Didactic Box -->
                    <div class="w-full bg-[#F5F3FF] border border-[#E0D9FF] rounded-2xl p-4 flex flex-col lg:flex-row lg:items-center justify-between gap-4 mb-8">
                        <div class="flex gap-4 items-center">
                            <div class="w-10 h-10 rounded-xl bg-white flex items-center justify-center shrink-0 text-[#5B50F0] shadow-sm">
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                                </svg>
                            </div>
                            <div>
                                <h5 class="text-xs font-bold text-[#1A1D2E]">Cómo leer esta pantalla</h5>
                                <p class="text-[11px] text-[#5A6275] font-semibold leading-normal mt-0.5">
                                    Producto, envío y seguro forman la base de cálculo. Si no conoces algún dato, usa un estimado conservador y ajústalo después.
                                </p>
                            </div>
                        </div>
                        <button type="button" class="text-[#5B50F0] hover:underline text-xs font-bold bg-transparent border-none cursor-pointer flex items-center gap-1 shrink-0">
                            Ver guía rápida
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M9.09 9a3 3 0 015.83 1c0 2-3 3-3 3M12 17h.01"/></svg>
                        </button>
                    </div>

                    <!-- Scenarios Grid -->
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                        <!-- Minimo -->
                        <div class="rounded-2xl border border-[#e8eaf0] bg-white p-5 shadow-sm flex flex-col cursor-pointer hover:border-[#22c55e] transition-all">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-xl bg-[#DCFCE7] text-[#15803D] flex items-center justify-center shrink-0">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z"/></svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#1a1d2e]">Mínimo</p>
                                    <p class="text-xl font-black text-[#1a1d2e] mt-0.5" id="scenarioMin">S/ 0.00</p>
                                </div>
                            </div>
                            <p class="text-[10px] text-[#5a6275] font-semibold mt-auto pt-2">Supone pocas variaciones y control simple.</p>
                        </div>
                        <!-- Esperado (Selected) -->
                        <div class="rounded-2xl border-2 border-[#5B50F0] bg-[#F5F3FF] p-5 shadow-sm flex flex-col cursor-pointer relative">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-xl bg-[#E0D9FF] text-[#5B50F0] flex items-center justify-center shrink-0">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06a1.65 1.65 0 00.33-1.82 1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06a1.65 1.65 0 001.82.33H9a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06a1.65 1.65 0 00-.33 1.82V9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#1a1d2e]">Esperado</p>
                                    <p class="text-xl font-black text-[#1a1d2e] mt-0.5" id="scenarioExpected">S/ 0.00</p>
                                </div>
                            </div>
                            <p class="text-[10px] text-[#5a6275] font-semibold mt-auto pt-2">Es la referencia principal para decidir si sigues.</p>
                        </div>
                        <!-- Conservador -->
                        <div class="rounded-2xl border border-[#e8eaf0] bg-white p-5 shadow-sm flex flex-col cursor-pointer hover:border-[#3b82f6] transition-all">
                            <div class="flex items-center gap-3 mb-2">
                                <div class="w-10 h-10 rounded-xl bg-[#EFF6FF] text-[#3B82F6] flex items-center justify-center shrink-0">
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#1a1d2e]">Conservador</p>
                                    <p class="text-xl font-black text-[#1a1d2e] mt-0.5" id="scenarioMax">S/ 0.00</p>
                                </div>
                            </div>
                            <p class="text-[10px] text-[#5a6275] font-semibold mt-auto pt-2">Reserva extra por ajustes, percepción o revisión.</p>
                        </div>
                    </div>

                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                        <!-- Main Form Column (Col 1) -->
                        <div class="space-y-6">
                            <div class="flex items-center justify-between border-b border-[#e8eaf0] pb-2">
                                <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest">Valores del producto</h4>
                                <div class="flex items-center gap-2">
                                    <span class="text-[10px] text-[#5a6275] font-semibold">Moneda:</span>
                                    <select class="text-[10px] font-bold text-[#1a1d2e] bg-white border border-[#e8eaf0] rounded px-2 py-0.5 outline-none cursor-pointer">
                                        <option>USD</option>
                                    </select>
                                </div>
                            </div>

                            <!-- Fields -->
                            <div class="space-y-4">
                                <div>
                                    <label class="text-[9px] font-bold text-[#5a6275] uppercase block mb-1.5">Cuánto cuesta el producto en total (USD) *</label>
                                    <div class="relative">
                                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                            <span class="text-gray-400">$</span>
                                        </div>
                                        <input type="number" id="logFob" min="0" step="0.01" class="w-full bg-white border border-[#e8eaf0] rounded-xl pl-8 pr-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold" value="35.00">
                                    </div>
                                </div>

                                <div>
                                    <label class="text-[9px] font-bold text-[#5a6275] uppercase block mb-1.5">Cuánto cuesta el envío internacional (USD) *</label>
                                    <div class="relative">
                                        <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                                            <span class="text-gray-400">?</span>
                                        </div>
                                        <input type="number" id="logFlete" min="0" step="0.01" class="w-full bg-white border border-[#e8eaf0] rounded-xl pl-8 pr-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold" value="15.00">
                                    </div>
                                </div>
                                
                                <input type="hidden" id="logSeguro" value="2.50">

                                <!-- Incoterm selection (Mockup uses 3 radios) -->
                                <div>
                                    <div class="flex items-center gap-1 mb-1.5">
                                        <label class="text-[9px] font-bold text-[#5a6275] uppercase block">El precio del proveedor incluye envío internacional</label>
                                        <svg class="w-3 h-3 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"/><path d="M12 16v-4M12 8h.01"/></svg>
                                    </div>
                                    
                                    <div class="flex flex-col gap-2 mt-2" id="incotermSelectorContainer">
                                        <button type="button" data-incoterm="FOB" class="w-full text-left px-4 py-3 rounded-xl border border-[#5B50F0] bg-[#F5F3FF] text-[#5B50F0] font-bold text-[11px] flex items-center gap-3 transition-all outline-none">
                                            <div class="w-3.5 h-3.5 rounded-full border-4 border-[#5B50F0] bg-white flex items-center justify-center shrink-0"></div>
                                            No, solo incluye el producto
                                        </button>
                                        <button type="button" data-incoterm="CIF" class="w-full text-left px-4 py-3 rounded-xl border border-[#e8eaf0] bg-white text-[#5a6275] font-semibold text-[11px] hover:bg-gray-50 flex items-center gap-3 transition-all outline-none">
                                            <div class="w-3.5 h-3.5 rounded-full border border-gray-300 bg-white shrink-0"></div>
                                            Sí, incluye producto y envío
                                        </button>
                                        <button type="button" data-incoterm="FOB_UNKNOWN" class="w-full text-left px-4 py-3 rounded-xl border border-[#e8eaf0] bg-white text-[#5a6275] font-semibold text-[11px] hover:bg-gray-50 flex items-center gap-3 transition-all outline-none">
                                            <div class="w-3.5 h-3.5 rounded-full border border-gray-300 bg-white shrink-0"></div>
                                            No estoy seguro
                                        </button>
                                    </div>
                                    <input type="hidden" id="opIncotermSelector" value="FOB">
                                </div>
                            </div>

                            <!-- Tipo de cambio referencial -->
                            <div class="p-4 rounded-xl border border-[#e8eaf0] bg-white flex justify-between items-center mt-6">
                                <div>
                                    <p class="text-[11px] text-[#1a1d2e] font-semibold">Tipo de cambio referencial</p>
                                    <div class="flex gap-2 items-center mt-0.5">
                                        <p class="text-[10px] text-[#5a6275]">Fuente: BCRP</p>
                                        <span id="tcTraceChip" class="hidden">PENDIENTE VALIDACIÓN</span>
                                    </div>
                                </div>
                                <input type="text" id="logTC" class="w-16 bg-white text-center border border-[#5B50F0] rounded-lg px-2 py-1.5 font-mono text-[#5B50F0] font-bold text-xs focus:outline-none" value="3.725" readonly>
                            </div>
                        </div>

                        <!-- Center Column: Resumen de Costos (Col 2) -->
                        <div class="space-y-6">
                            <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm h-full flex flex-col">
                                <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest border-b border-[#e8eaf0] pb-2 mb-4">Resumen de costos</h4>
                                
                                <div class="space-y-3 text-[11px] font-semibold text-[#1a1d2e] flex-1">
                                    <div class="flex justify-between">
                                        <span>Producto (FOB)</span>
                                        <span class="font-mono" id="cifFobUsd">S/ 35.00</span>
                                    </div>
                                    <div class="flex justify-between">
                                        <span>Envío internacional</span>
                                        <span class="font-mono" id="cifFleteUsd">S/ 15.00</span>
                                    </div>
                                    <div class="flex justify-between">
                                        <span>Seguro</span>
                                        <span class="font-mono" id="cifSeguroUsd">S/ 2.50</span>
                                    </div>
                                    <div class="flex justify-between pt-2 pb-1 text-[#5B50F0] font-bold">
                                        <span>Valor CIF</span>
                                        <span class="font-mono" id="cifCifUsd">S/ 52.50</span>
                                    </div>

                                    <h5 class="text-[10px] font-black text-[#1a1d2e] uppercase tracking-widest mt-6 mb-3 pt-3 border-t border-[#e8eaf0]">Tributos aproximados</h5>
                                    <div class="space-y-3">
                                        <div class="flex justify-between">
                                            <span id="taxLabel-adValorem">Ad Valorem (0%)</span>
                                            <span class="font-mono" id="taxVal-adValorem">S/ 0.00</span>
                                        </div>
                                        <div class="flex justify-between">
                                            <span id="taxLabel-igv">IGV (16%)</span>
                                            <span class="font-mono" id="taxVal-igv">S/ 0.00</span>
                                        </div>
                                        <div class="flex justify-between">
                                            <span id="taxLabel-ipm">IPM (2.4%)</span>
                                            <span class="font-mono" id="taxVal-ipm">S/ 0.00</span>
                                        </div>
                                        <div class="flex justify-between">
                                            <span id="taxLabel-percepcion">Percepción (3.5%)</span>
                                            <span class="font-mono" id="taxVal-percepcion">S/ 0.00</span>
                                        </div>
                                    </div>
                                </div>

                                <div class="pt-4 border-t-2 border-[#22c55e] mt-4 flex justify-between items-center text-sm font-black text-[#22c55e]">
                                    <span>Total estimado</span>
                                    <span class="font-mono text-base" id="cifCifPen">S/ 195.563</span>
                                </div>

                                <!-- Green info box -->
                                <div id="taxBurdenTrafficLight" class="mt-4 p-4 rounded-xl bg-[#F0FDF4] border border-[#BBF7D0] transition-all duration-300">
                                    <div class="flex items-center gap-2 mb-1">
                                        <div class="w-4 h-4 rounded-full bg-[#22c55e] text-white flex items-center justify-center shrink-0">
                                            <svg class="w-2.5 h-2.5" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
                                        </div>
                                        <h5 class="text-[10px] font-bold text-[#15803D]">Carga Tributaria Baja</h5>
                                    </div>
                                    <p class="text-[10px] text-[#166534] leading-relaxed">
                                        Tus tributos estimados representan 0.0% del valor FOB. Esta tasa es sumamente favorable y reduce significativamente los costos de internamiento.
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Right Column: Sidebar (Col 3) -->
                        <div class="space-y-6">
                            <!-- COMPARA ESCENARIOS -->
                            <div class="bg-white border border-[#e8eaf0] p-5 rounded-2xl shadow-sm">
                                <h4 class="text-[10px] font-black text-[#1a1d2e] uppercase tracking-widest mb-2">Compara escenarios</h4>
                                <p class="text-[10px] text-[#5a6275] mb-4">El valor CIF es la base usual para estimar tributos.</p>

                                <div class="flex flex-col gap-2">
                                    <button type="button" data-knowledge="valor_fob" class="w-full py-2.5 rounded-xl border border-[#5B50F0] text-[#5B50F0] text-[11px] font-bold bg-white hover:bg-gray-50 transition-all outline-none cursor-pointer">
                                        Producto sin envío
                                    </button>
                                    <button type="button" data-knowledge="valor_cif" class="w-full py-2.5 rounded-xl border border-[#5B50F0] text-[#5B50F0] text-[11px] font-bold bg-white hover:bg-gray-50 transition-all outline-none cursor-pointer">
                                        Producto con envío
                                    </button>
                                    <button type="button" id="btnOpenIncotermsLab" class="w-full py-2.5 rounded-xl border-none bg-[#5B50F0] text-white text-[11px] font-bold hover:bg-[#4a40df] transition-all outline-none cursor-pointer">
                                        Comparar quién paga
                                    </button>
                                </div>

                                <details class="text-[10px] font-bold text-[#5a6275] mt-4 border-t border-[#e8eaf0] pt-4 cursor-pointer group">
                                    <summary class="hover:text-[#1a1d2e] flex items-center justify-between outline-none">
                                        Opciones avanzadas 
                                        <svg class="w-3 h-3 transition-transform group-open:rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 9l-7 7-7-7"/></svg>
                                    </summary>
                                    <div class="space-y-3 mt-3 font-semibold text-[10px] cursor-default">
                                        <div>
                                            <label class="block mb-1">Tipo de contribuyente</label>
                                            <select id="tribPerfil" class="w-full bg-white border border-[#e8eaf0] rounded-lg px-2 py-1.5 outline-none focus:border-[#5B50F0]">
                                                <option value="ESTANDAR" selected>Recurrente (3.5% Percepción)</option>
                                                <option value="NUEVO">Nuevo (10% Percepción)</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label class="block mb-1">Acuerdo comercial</label>
                                            <select id="tribTlc" class="w-full bg-white border border-[#e8eaf0] rounded-lg px-2 py-1.5 outline-none focus:border-[#5B50F0]">
                                                <option value="NO" selected>Sin TLC</option>
                                                <option value="SI">Con TLC (0% Arancel)</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label class="block mb-1">Documento de origen</label>
                                            <select id="tribCertificado" class="w-full bg-white border border-[#e8eaf0] rounded-lg px-2 py-1.5 outline-none focus:border-[#5B50F0]">
                                                <option value="Pendiente" selected>No Presentado</option>
                                                <option value="Emitido">Validado</option>
                                            </select>
                                        </div>
                                    </div>
                                </details>
                            </div>

                            <!-- TU RUTA ACTUAL -->
                            <div class="border border-[#e8eaf0] bg-white rounded-2xl p-5 shadow-sm hidden md:block">
                                <h4 class="text-[10px] font-black text-[#1a1d2e] uppercase tracking-widest mb-4">Tu ruta actual</h4>
                                
                                <div class="space-y-3 text-[10px]">
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Producto:</span>
                                        <span class="text-[#5a6275] font-semibold block" id="sideProductName3">--</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Procedencia:</span>
                                        <span class="text-[#5a6275] font-semibold block uppercase" id="sideCountryName3">-- / FOB</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Código del producto:</span>
                                        <span class="text-[#5B50F0] font-bold font-mono block" id="sideHsCode3">--</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Costo base estimado:</span>
                                        <span class="text-[#5a6275] font-semibold block" id="sideCifCost3">S/ 0.00</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Impuestos aproximados:</span>
                                        <span class="text-[#22c55e] font-bold block" id="sideTaxes3">S/ 0.00</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Estado regulatorio:</span>
                                        <span class="text-amber-500 font-bold block" id="sideRegStatus3">--</span>
                                    </div>
                                    <div>
                                        <span class="font-black text-[#1a1d2e] block">Estado de avance:</span>
                                        <span class="mt-1 inline-block bg-amber-50 text-amber-600 border border-amber-200 px-2 py-0.5 rounded font-bold text-[9px] uppercase">Revisar permiso</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
"""

start_idx = -1
end_idx = -1
for i, line in enumerate(lines):
    if '<div id="stepGroup-3" class="step-content">' in line:
        start_idx = i
    if '<div id="stepGroup-4" class="step-content">' in line:
        end_idx = i - 1
        break

if start_idx != -1 and end_idx != -1:
    lines = lines[:start_idx] + [new_html] + lines[end_idx:]
    with open('C:\\Users\\Manuel_Dongo\\Music\\Importease_Aduanero_Final\\src\\main\\webapp\\evaluacion.jsp', 'w', encoding='utf-8') as f:
        f.writelines(lines)
    print("Replaced successfully")
else:
    print(f"Could not find indices: start={start_idx}, end={end_idx}")
