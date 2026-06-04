<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    String userRuc = (String) session.getAttribute("usuarioRuc");
    String userNombre = (String) session.getAttribute("usuarioNombre");
    String safeUserRuc = com.importease.proyecto.service.HtmlUtil.escape(userRuc != null ? userRuc : "");
    String safeUserNombre = com.importease.proyecto.service.HtmlUtil.escape(userNombre != null ? userNombre : "");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Importar</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .step-content { display: none; }
        .step-content.active { display: block !important; animation: fadeUp 0.4s var(--ease-out-expo) both; }
        @keyframes fadeUp { from { opacity: 0; transform: translateY(16px); filter: blur(4px); } to { opacity: 1; transform: translateY(0); filter: blur(0); } }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-[var(--surface-0)] font-['Outfit'] text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "wizard");
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Importar paso a paso"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-hidden relative">
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-[var(--surface-1)]/80 backdrop-blur-xl z-20">
            <div class="flex items-center gap-4">
                <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                    <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                    <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Importacion guiada</span>
                </div>
            </div>
            
            <div class="flex items-center gap-3">
                <span id="autosaveStatus" class="autosave-pill text-[10px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-600 px-3 py-1 rounded-full border border-emerald-100">Autoguardado activo</span>
                <p class="hidden lg:block text-[11px] font-bold text-[var(--text-secondary)]">Responde en simple. Nosotros traducimos a codigo, permisos, costos y documentos.</p>
            </div>
        </header>

        <jsp:include page="/fragments/breadcrumb.jsp" />

        <!-- Barra de Progreso Superior (4 Pasos Humanos) -->
        <div class="timeline-shell mx-8 mt-4 px-6 py-4 bg-[var(--surface-1)] border border-[var(--border)] rounded-2xl overflow-x-auto shadow-sm">
            <div class="flex items-center justify-between min-w-[600px]" id="wizardTimeline">
                <div class="step-indicator flex items-center gap-2" id="stepIndicator-1">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-[var(--accent)] text-white shadow-lg shadow-[var(--accent)]/30">1</span>
                    <span class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">1. Producto</span>
                </div>
                <div class="h-0.5 flex-1 bg-[var(--border)] mx-2" id="timelineBar-1"></div>
                
                <div class="step-indicator flex items-center gap-2 opacity-50" id="stepIndicator-2">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-tertiary)]">2</span>
                    <span class="text-[10px] font-black uppercase tracking-widest text-[var(--text-tertiary)]">2. Código y permisos</span>
                </div>
                <div class="h-0.5 flex-1 bg-[var(--border)] mx-2" id="timelineBar-2"></div>
                
                <div class="step-indicator flex items-center gap-2 opacity-50" id="stepIndicator-3">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-tertiary)]">3</span>
                    <span class="text-[10px] font-black uppercase tracking-widest text-[var(--text-tertiary)]">3. Costos</span>
                </div>
                <div class="h-0.5 flex-1 bg-[var(--border)] mx-2" id="timelineBar-3"></div>
                
                <div class="step-indicator flex items-center gap-2 opacity-50" id="stepIndicator-4">
                    <span class="w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-[var(--surface-1)] border border-[var(--border)] text-[var(--text-tertiary)]">4</span>
                    <span class="text-[10px] font-black uppercase tracking-widest text-[var(--text-tertiary)]">4. Revisión final</span>
                </div>
            </div>
        </div>

        <!-- Barra de progreso porcentual -->
        <div class="px-6 mt-2 flex items-center gap-2">
            <div class="flex-1 h-1 bg-[var(--border)] rounded-full overflow-hidden">
                <div id="progressBar" class="h-full bg-[var(--accent)] rounded-full transition-all duration-500" style="width: 25%"></div>
            </div>
            <span id="progressPercent" class="text-[9px] font-black text-[var(--text-tertiary)]">25%</span>
        </div>

        <!-- Contenido del Wizard & Sidebar Resumen Lateral -->
        <div class="wizard-workspace flex-1 flex overflow-hidden p-8 gap-6">
            
            <!-- Panel Central del Formulario -->
            <div class="wizard-main-shell flex-1 bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 overflow-y-auto custom-scrollbar flex flex-col justify-between shadow-sm">
                
                <div class="space-y-6">
                    <!-- PASO 1: Producto y Origen (Humano) -->
                    <div id="stepGroup-1" class="step-content active">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest block">Paso 1 de 4</span>
                                <h4 class="text-2xl font-black text-[var(--text-primary)] tracking-tight mt-1">Cuentanos que quieres traer</h4>
                            </div>
                        </div>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">No escribas como experto. Describe el producto como se lo explicarias a otra persona.</p>
                        
                        <!-- Caja didáctica -->
                        <div class="didactic-box p-5 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <strong class="text-[var(--accent)] uppercase tracking-wider text-[9px] block mb-1">Que hacemos con tu respuesta</strong>
                            Tomamos el nombre, uso, cantidad y origen para sugerir el codigo aduanero, detectar permisos y preparar el costo. Si algo no queda claro, te preguntaremos en simple.
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                            <button type="button" id="routeCardPersonal" onclick="selectImportRoute('PERSONAL')" class="rounded-2xl border border-[var(--accent)] bg-[var(--accent-soft)] px-5 py-5 text-left transition-all shadow-sm">
                                <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--accent)]">Ruta personal</p>
                                <h5 class="text-lg font-black text-[var(--text-primary)] mt-2">Quiero importar para uso propio</h5>
                                <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Ideal si aún estás comparando una compra, quieres entender tributos o validar si necesitas permiso.</p>
                            </button>
                            <button type="button" id="routeCardComercial" onclick="selectImportRoute('COMERCIAL')" class="rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] px-5 py-5 text-left transition-all hover:border-[var(--accent)]">
                                <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--text-tertiary)]">Ruta comercial</p>
                                <h5 class="text-lg font-black text-[var(--text-primary)] mt-2">Quiero importar para vender o abastecer mi negocio</h5>
                                <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Nos enfocaremos en volumen, permisos, expediente y costo total para que la operación nazca ordenada.</p>
                            </button>
                        </div>

                        <div class="p-4 mb-6 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)]">
                            <p class="text-xs font-semibold text-[var(--text-secondary)]" id="routeHint">Ruta personal: vamos a ayudarte a saber si tu compra sigue siendo personal o si ya parece comercial.</p>
                        </div>
                        <!-- Carga rápida de ejemplos -->
                        <div class="p-4 mb-6 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)]">
                            <div class="flex flex-wrap items-center gap-2">
                                <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest mr-2">Prueba rápida con ejemplos:</span>
                                <button onclick="cargarEjemplo('celular')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-1)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] text-[9px] font-bold text-[var(--accent)] transition-all">Celular (MTC)</button>
                                <button onclick="cargarEjemplo('proteina')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-1)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] text-[9px] font-bold text-[var(--accent)] transition-all">Suplemento (DIGESA)</button>
                                <button onclick="cargarEjemplo('rayos')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-1)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] text-[9px] font-bold text-[var(--accent)] transition-all">Ecógrafo (DIGEMID)</button>
                                <button onclick="cargarEjemplo('semilla')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-1)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] text-[9px] font-bold text-[var(--accent)] transition-all">Semilla (SENASA)</button>
                                <button onclick="cargarEjemplo('madera')" class="px-3 py-1.5 rounded-lg bg-[var(--surface-1)] border border-[var(--border)] hover:border-[var(--accent)] hover:bg-[var(--surface-2)] text-[9px] font-bold text-[var(--accent)] transition-all">Madera (SERFOR)</button>
                            </div>
                        </div>

                        <!-- 2-Column Split: Fields & Side Illustration/Help -->
                        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6">
                            <!-- Left: Inputs -->
                            <div class="lg:col-span-3 space-y-4">
                                <div>
                                    <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Que producto quieres importar *</label>
                                    <input type="text" id="prodNombre" oninput="syncOpNombre()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold" placeholder="Ej: Celular Samsung Galaxy A55">
                                </div>
                                
                                <div>
                                    <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Para que sirve o que caracteristicas tiene *</label>
                                    <textarea id="prodTecnica" rows="2" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold" placeholder="Ej: Teléfono inteligente con WiFi, Bluetooth, cámara y pantalla táctil"></textarea>
                                </div>
                                
                                <div class="grid grid-cols-2 gap-4">
                                    <div>
                                        <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">De que pais viene *</label>
                                        <select id="opPaisOrigen" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold">
                                            <option value="CHINA" selected>China</option>
                                            <option value="USA">USA</option>
                                            <option value="ALEMANIA">Alemania</option>
                                            <option value="CHILE">Chile</option>
                                            <option value="ESPAÑA">España</option>
                                        </select>
                                    </div>
                                    
                                    <div>
                                        <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Lo usaras para *</label>
                                        <select id="prodUso" onchange="onProdUsoChange()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold">
                                            <option value="PERSONAL" selected>Personal</option>
                                            <option value="COMERCIAL">Comercial</option>
                                        </select>
                                    </div>
                                </div>

                                <div id="rucFieldContainer" class="rounded-2xl border border-[var(--border)] bg-[var(--surface-0)] px-4 py-3">
                                    <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Referencia de importador</p>
                                    <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Si eliges la ruta comercial, tomaremos tu identificación registrada para dejar trazabilidad desde el inicio.</p>
                                </div>

                                <div class="grid grid-cols-3 gap-4">
                                    <div>
                                        <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Cuántas unidades *</label>
                                        <input type="number" id="prodCantidad" min="1" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold font-mono" value="1" oninput="sugerirCostosFobFleteSeguro()">
                                        <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-2" id="cantidadHint">Si la cantidad es alta, te avisaremos que tu operación puede requerir tratamiento comercial.</p>
                                    </div>
                                    <div>
                                        <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Marca (opcional)</label>
                                        <input type="text" id="prodMarca" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold" placeholder="Ej: Samsung">
                                    </div>
                                    <div>
                                        <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-wider mb-1 block">Modelo (opcional)</label>
                                        <input type="text" id="prodModelo" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-semibold" placeholder="Ej: Galaxy A55">
                                    </div>
                                </div>
                            </div>

                            <!-- Right: Help Card -->
                            <div class="lg:col-span-2 rounded-2xl border border-[var(--border)] p-6 bg-[var(--surface-0)] flex flex-col justify-between gap-4">
                                <div class="text-center py-6 flex flex-col items-center justify-center gap-3">
                                    <div class="text-5xl font-black text-[var(--accent)]">01</div>
                                    <h5 class="text-sm font-bold text-[var(--text-primary)] mt-2">Qué conviene contarnos aquí</h5>
                                    <p class="text-[11px] text-[var(--text-secondary)] font-semibold leading-relaxed max-w-[200px]">
                                        Cuanto más concreto seas con uso, material, conectividad o composición, más preciso será el diagnóstico.
                                    </p>
                                </div>
                                <div class="p-3 bg-[var(--surface-1)] border border-[var(--border)] rounded-xl text-[9px] text-[var(--accent)] font-black uppercase tracking-wider text-center">
                                    Ruta guiada activa
                                </div>
                            </div>
                        </div>

                        <!-- Campos técnicos J2EE ocultos o autogestionados -->
                        <div class="hidden">
                            <input type="text" id="opNombre" value="Evaluación de producto">
                            <select id="opTipo"><option value="PERSONAL" selected>Personal</option><option value="COMERCIAL">Comercial</option></select>
                            <input type="text" id="opRuc" value="<%= safeUserRuc %>">
                            <select id="opIncoterm">
                                <option value="FOB" selected>FOB</option>
                                <option value="CIF">CIF</option>
                                <option value="CFR">CFR</option>
                                <option value="CIP">CIP</option>
                                <option value="DAP">DAP</option>
                                <option value="DDP">DDP</option>
                                <option value="EXW">EXW</option>
                            </select>
                            <input type="text" id="opProveedor" value="Proveedor Internacional">
                        </div>
                    </div>

                    <!-- PASO 2: Código aduanero y permisos (Clasificación HS + Restricciones) -->
                    <div id="stepGroup-2" class="step-content">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest block">Paso 2 de 4</span>
                                <h4 class="text-2xl font-black text-[var(--text-primary)] tracking-tight mt-1">Código y permisos</h4>
                            </div>
                        </div>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">Este codigo es la forma en que aduanas identifica tu producto. Te mostraremos opciones probables y tu eliges la mas parecida.</p>

                        <!-- Caja Didáctica -->
                        <div class="didactic-box p-5 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <div class="flex justify-between items-start gap-4">
                                <div>
                                    <strong class="text-[var(--accent)] uppercase tracking-wider text-[9px] block mb-1">Que significa este codigo</strong>
                                    Es como la etiqueta oficial del producto ante aduanas. De ahi salen permisos, impuestos y documentos que podrian pedirte.
                                </div>
                                <button type="button" onclick="openKnowledgePanel('codigo_hs')" class="px-3 py-1.5 shrink-0 rounded-full bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black uppercase tracking-widest text-[9px] hover:bg-[var(--surface-2)] transition-all">
                                    Explicame el codigo
                                </button>
                            </div>
                        </div>

                        <!-- Selected HS Card -->
                        <div class="mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl p-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
                            <div class="flex items-center gap-3">
                                <svg class="w-6 h-6 text-[var(--accent)] shrink-0" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
                                </svg>
                                <div>
                                    <div class="flex items-center gap-2">
                                        <p class="text-[10px] font-black text-[var(--accent)] uppercase">Codigo sugerido</p>
                                        <span class="px-2 py-0.5 rounded bg-orange-100 border border-orange-200 text-orange-700 text-[8px] font-black uppercase dark:bg-orange-950/20 dark:text-orange-400">Coincidencia alta</span>
                                    </div>
                                    <p class="text-sm text-[var(--text-primary)] font-black mt-0.5" id="selectedHSLabel">Ninguna seleccionada</p>
                                    <div class="flex flex-wrap items-center gap-2 mt-2">
                                        <span id="hsSourceChip" class="source-chip source-chip--pending">PENDIENTE VALIDACIÓN</span>
                                        <span id="hsConfidenceText" class="text-[10px] font-bold text-[var(--text-secondary)]">Fuente por confirmar</span>
                                    </div>
                                    <div id="hsDigitContainer" class="flex gap-2 mt-3 font-mono font-black text-sm"></div>
                                </div>
                            </div>
                            
                            <details class="text-[10px] font-bold text-[var(--accent)]">
                                <summary class="cursor-pointer hover:underline uppercase tracking-wider">¿Por qué este código?</summary>
                                <p class="mt-2 text-[var(--text-secondary)] font-semibold leading-relaxed max-w-xs">
                                    Porque el producto fue descrito como teléfono inteligente con WiFi y Bluetooth.
                                </p>
                            </details>
                        </div>

                        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3 mb-6 rounded-2xl border border-[var(--border)] bg-[var(--surface-0)] px-4 py-4">
                            <div>
                                <p class="text-[10px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Necesitas comparar mas opciones?</p>
                                <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Abre el buscador de codigo de producto y vuelve con una seleccion aplicada a esta ruta.</p>
                            </div>
                            <button type="button" onclick="openHsAssistant()" class="px-4 py-2.5 rounded-xl bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] text-xs font-black uppercase tracking-widest hover:border-[var(--accent)] transition-all">Buscar codigo</button>
                        </div>

                        <!-- HS Options table -->
                        <h5 class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest mb-3">Encontramos posibles códigos para tu producto:</h5>
                        <div class="overflow-x-auto rounded-xl border border-[var(--border)] bg-[var(--surface-1)] mb-6">
                            <table class="w-full text-left text-xs border-collapse">
                                <thead>
                                    <tr class="bg-[var(--surface-2)] border-b border-[var(--border)] font-bold uppercase tracking-wider text-[var(--text-tertiary)]">
                                        <th class="p-3">Codigo</th>
                                        <th class="p-3">Producto parecido</th>
                                        <th class="p-3 text-center">Coincidencia</th>
                                        <th class="p-3">Impuesto base</th>
                                        <th class="p-3 text-center">Usar</th>
                                    </tr>
                                </thead>
                                <tbody id="hsSugerenciasTable" class="divide-y divide-[var(--border)] text-[var(--text-secondary)] font-semibold">
                                    <tr>
                                        <td colspan="5" class="p-6 text-center text-[var(--text-tertiary)]">Analizando el producto para sugerir códigos...</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>

                        <!-- Interactive confirm block -->
                        <div class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] mb-6 space-y-4">
                            <h5 class="text-[10px] font-black text-[var(--text-secondary)] uppercase tracking-widest border-b border-[var(--border)] pb-2 flex items-center gap-2">
                                <svg class="w-4 h-4 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.375m0-10.5h3.375c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125H9.75M9 4.5V3c0-.621-.504-1.125-1.125-1.125H4.125C3.504 1.875 3 2.379 3 3v13.5c0 .621.504 1.125 1.125 1.125h3.375M9 15h2.25m-2.25-6h2.25m-2.25 3h2.25"/>
                                </svg>
                                Preguntas para confirmar
                            </h5>
                            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs font-semibold">
                                <div class="flex flex-col gap-2 bg-[var(--surface-1)] p-3 rounded-xl border border-[var(--border)]">
                                    <span class="text-[var(--text-secondary)]">¿El producto permite llamadas?</span>
                                    <div class="q-btn-group" id="btnGroup-confirmLlamadas">
                                        <button type="button" class="q-btn active">Sí</button>
                                        <button type="button" class="q-btn">No</button>
                                        <button type="button" class="q-btn">No sé</button>
                                    </div>
                                </div>
                                <div class="flex flex-col gap-2 bg-[var(--surface-1)] p-3 rounded-xl border border-[var(--border)]">
                                    <span class="text-[var(--text-secondary)]">¿Tiene WiFi o Bluetooth?</span>
                                    <div class="q-btn-group" id="btnGroup-confirmWifi">
                                        <button type="button" class="q-btn active">Sí</button>
                                        <button type="button" class="q-btn">No</button>
                                        <button type="button" class="q-btn">No sé</button>
                                    </div>
                                </div>
                                <div class="flex flex-col gap-2 bg-[var(--surface-1)] p-3 rounded-xl border border-[var(--border)]">
                                    <span class="text-[var(--text-secondary)]">¿Es portátil?</span>
                                    <div class="q-btn-group" id="btnGroup-confirmPortatil">
                                        <button type="button" class="q-btn active">Sí</button>
                                        <button type="button" class="q-btn">No</button>
                                        <button type="button" class="q-btn">No sé</button>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="flex flex-col sm:flex-row justify-between items-center bg-[var(--surface-1)] border border-[var(--border)] p-4 rounded-xl gap-4">
                            <span class="text-[10px] font-bold text-[var(--text-secondary)]">Ya tienes un codigo de 10 digitos?</span>
                            <div class="flex gap-2 w-full sm:w-auto">
                                <input type="text" id="manualHSCode" class="flex-1 sm:flex-initial bg-[var(--surface-0)] border border-[var(--border)] rounded-lg px-3 py-1.5 text-xs focus:outline-none focus:border-[var(--accent)] outline-none text-[var(--text-primary)] font-mono font-bold" placeholder="Ej: 8517130000">
                                <button onclick="selectManualHS()" class="bg-[var(--accent)] text-white text-xs px-4 py-1.5 rounded-lg font-bold hover:bg-[var(--accent-hover)] transition-all">Usar codigo</button>
                            </div>
                        </div>

                        <!-- Divisor de sección: Permisos -->
                        <hr class="my-6 border-[var(--border)]">
                        <h5 class="text-lg font-black text-[var(--text-primary)] tracking-tight mb-2">Revisemos si necesitas permiso</h5>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">Algunos productos necesitan autorizacion antes de ingresar al pais. Responde estas preguntas sin preocuparte por la entidad.</p>

                        <!-- Caja Didáctica -->
                        <div class="didactic-box p-4 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <strong class="text-[var(--accent)] uppercase tracking-wider text-[9px] block mb-1">Que revisamos aqui</strong>
                            Buscamos señales simples: alimentos, salud, comunicaciones, agricultura, madera o productos usados. Si aparece una alerta, te diremos que preparar y por que.
                            <div class="mt-3 flex flex-wrap gap-2">
                                <button type="button" onclick="openKnowledgePanel('permiso_autorizacion')" class="inline-flex px-3 py-1 rounded-full bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black uppercase tracking-widest text-[9px] hover:bg-[var(--surface-2)] transition-all">
                                    Que es un permiso
                                </button>
                                <button type="button" onclick="openKnowledgePanel('suce')" class="inline-flex px-3 py-1 rounded-full bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black uppercase tracking-widest text-[9px] hover:bg-[var(--surface-2)] transition-all">
                                    Que es un tramite
                                </button>
                            </div>
                        </div>

                        <!-- Cuestionario e Informe Split -->
                        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6 mb-6">
                            
                            <!-- Left: Interactive questions list -->
                            <div class="lg:col-span-3 bg-[var(--surface-0)] border border-[var(--border)] p-5 rounded-2xl space-y-4">
                                <div class="flex flex-col gap-2 border-b border-[var(--border)] pb-3">
                                    <h5 class="text-[10px] font-black text-[var(--text-secondary)] uppercase tracking-widest flex items-center gap-2">
                                        <svg class="w-4 h-4 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h3.75M9 15h3.375m0-10.5h3.375c.621 0 1.125.504 1.125 1.125v11.25c0 .621-.504 1.125-1.125 1.125H9.75M9 4.5V3c0-.621-.504-1.125-1.125-1.125H4.125C3.504 1.875 3 2.379 3 3v13.5c0 .621.504 1.125 1.125 1.125h3.375M9 15h2.25m-2.25-6h2.25m-2.25 3h2.25"/>
                                        </svg>
                                        Preguntas para confirmar
                                    </h5>
                                    <p class="text-[11px] text-[var(--text-secondary)] font-semibold" id="dynamicQuestionMeta">Las preguntas cambian segun el producto para afinar permisos, documentos y siguiente paso.</p>
                                </div>

                                <div id="dynamicQuestionArea" class="space-y-3"></div>

                                <!-- Hidden form select mappings to integrate nicely with index.js -->
                                <div class="hidden">
                                    <select id="qConsumo"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                    <select id="qSalud"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                    <select id="qWifi"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                    <select id="qContacto"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                    <select id="qMadera"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                    <select id="qUsado"><option value="NO" selected>No</option><option value="SI">Sí</option></select>
                                </div>
                            </div>
                            
                            <!-- Right: Revision results card -->
                            <div class="lg:col-span-2 rounded-2xl border border-orange-200 p-5 bg-[#FFF7ED] flex flex-col justify-between gap-4 dark:bg-amber-950/20 dark:border-amber-900/40">
                                <div>
                                    <div class="flex items-center gap-2">
                                        <span class="text-xl shrink-0" id="vuceSemaforoCircle">??</span>
                                        <h5 class="text-xs font-black uppercase text-emerald-600 tracking-wider" id="vuceSemaforoText">LIBRE DE CONTROL</h5>
                                    </div>
                                    
                                    <div class="mt-4 space-y-3 text-xs">
                                        <div>
                                            <p class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest">Resultado de revisión</p>
                                            <p class="text-[11px] text-[var(--text-secondary)] font-semibold leading-relaxed mt-1" id="vuceReglaMensaje">
                                                El sistema no detecta restricciones automáticas. Si respondes "Sí" a alguna de las preguntas, se actualizará el diagnóstico.
                                            </p>
                                        </div>
                                        <div class="pt-3 border-t border-[var(--border)]/80 text-[10px] space-y-1 font-semibold">
                                            <div class="flex justify-between">
                                                <span class="text-[var(--text-tertiary)]">Quien podria revisar:</span>
                                                <span id="vuceSectorVal" class="text-[var(--text-primary)]">N/A</span>
                                            </div>
                                            <div class="flex justify-between">
                                                <span class="text-[var(--text-tertiary)]">Documento posible:</span>
                                                <span id="vucePermisoVal" class="text-[var(--accent)]">N/A</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="p-3 bg-[var(--surface-1)] border border-[var(--border)] rounded-xl text-[9px] text-[var(--text-tertiary)] font-semibold leading-relaxed">
                                    Esta evaluacion es una guia. La validacion final la realiza la entidad competente o tu agente de aduanas.
                                </div>
                            </div>
                        </div>

                        <!-- Formulario de Plantilla TUPA de la Entidad (Dynamic fields) -->
                        <div id="entityTemplateContainer" class="hidden p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-4">
                            <h5 class="text-[10px] font-black text-amber-600 uppercase tracking-widest mb-1 flex items-center gap-2">
                                <svg class="w-4 h-4 text-amber-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
                                </svg>
                                Datos que normalmente pedirian para el permiso (<span id="tupaEntidadLabel">MTC</span>)
                            </h5>
                            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs" id="tupaFieldsArea"></div>
                    </div>
                </div>

                <!-- PASO 3: Costos por escenarios (Tributos) -->
                <div id="stepGroup-3" class="step-content">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest block">Paso 3 de 4</span>
                                <h4 class="text-2xl font-black text-[var(--text-primary)] tracking-tight mt-1">Cuanto podria costar</h4>
                            </div>
                        </div>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">No buscamos exactitud perfecta. Queremos que sepas si la importacion sigue teniendo sentido antes de comprometer dinero.</p>

                        <!-- Caja Didáctica -->
                        <div class="didactic-box p-4 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <strong>Como leer esta pantalla</strong><br>
                            Producto, envio y seguro forman la base de calculo. Si no conoces algun dato, usa un estimado conservador y ajustalo despues.
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                            <div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] p-4">
                                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Mínimo</p>
                                <p class="text-2xl font-black text-[var(--text-primary)] mt-2" id="scenarioMin">S/ 0.00</p>
                                <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2">Supone pocas variaciones y control simple.</p>
                            </div>
                            <div class="rounded-2xl border border-[var(--accent)] bg-[var(--accent-soft)] p-4">
                                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--accent)]">Esperado</p>
                                <p class="text-2xl font-black text-[var(--text-primary)] mt-2" id="scenarioExpected">S/ 0.00</p>
                                <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2">Es la referencia principal para decidir si sigues.</p>
                            </div>
                            <div class="rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] p-4">
                                <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Conservador</p>
                                <p class="text-2xl font-black text-[var(--text-primary)] mt-2" id="scenarioMax">S/ 0.00</p>
                                <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-2">Reserva extra por ajustes, percepción o revisión.</p>
                            </div>
                        </div>
                        <div class="grid grid-cols-1 lg:grid-cols-5 gap-6">
                            <!-- Inputs del Costeo Simple -->
                            <div class="lg:col-span-3 space-y-4 bg-[var(--surface-0)] border border-[var(--border)] p-5 rounded-2xl flex flex-col justify-between">
                                <div class="space-y-4">
                                    <h5 class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest border-b border-[var(--border)] pb-2">Valores del producto</h5>
                                    
                                    <div>
                                        <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Cuanto cuesta el producto en total (USD) *</label>
                                        <input type="number" id="logFob" min="0" step="0.01" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2.5 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-mono font-bold" value="5000.00" oninput="wizardData.manualCostsModified = true; updateCIFCalculations()">
                                    </div>
                                    <div>
                                        <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Cuanto cuesta el envio internacional (USD) *</label>
                                        <input type="number" id="logFlete" min="0" step="0.01" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2.5 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-mono font-bold" value="450.00" oninput="wizardData.manualCostsModified = true; updateCIFCalculations()">
                                    </div>
                                    
                                    <div>
                                        <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">El precio del proveedor incluye envio internacional?</label>
                                        <div class="space-y-2 mt-2" id="incotermSelectorContainer">
                                            <button type="button" onclick="selectIncotermOption('FOB', this)" class="w-full text-left p-4 rounded-xl border border-[var(--accent)] bg-[var(--accent-soft)] text-[var(--accent)] font-bold flex items-center gap-3 transition-all outline-none">
                                                <span class="w-2.5 h-2.5 rounded-full bg-[var(--accent)]"></span>
                                                No, solo incluye el producto
                                            </button>
                                            <button type="button" onclick="selectIncotermOption('CIF', this)" class="w-full text-left p-4 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-semibold hover:bg-[var(--surface-0)] flex items-center gap-3 transition-all outline-none">
                                                <span class="w-2.5 h-2.5 rounded-full border border-gray-300"></span>
                                                Sí, incluye producto y envío
                                            </button>
                                            <button type="button" onclick="selectIncotermOption('FOB_UNKNOWN', this)" class="w-full text-left p-4 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] text-[var(--text-primary)] font-semibold hover:bg-[var(--surface-0)] flex items-center gap-3 transition-all outline-none">
                                                <span class="w-2.5 h-2.5 rounded-full border border-gray-300"></span>
                                                No estoy seguro
                                            </button>
                                        </div>
                                        <input type="hidden" id="opIncotermSelector" value="FOB">
                                    </div>
                                </div>
                                
                                <div class="pt-4 border-t border-[var(--border)] flex justify-between items-center text-[10px] font-bold text-[var(--text-tertiary)] mt-4">
                                    <div class="flex flex-col gap-1">
                                        <span>Tipo de cambio referencial</span>
                                        <span id="tcTraceChip" class="source-chip source-chip--pending">PENDIENTE VALIDACIÓN</span>
                                    </div>
                                    <input type="text" id="logTC" class="w-16 bg-[var(--surface-1)] text-center border border-[var(--border)] rounded-lg px-2 py-1 font-mono text-[var(--accent)] font-bold focus:outline-none" value="3.70" readonly>
                                </div>
                            </div>

                            <!-- Tabla del Valor CIF y Resumen Tributos -->
                            <div class="lg:col-span-2 p-6 bg-[var(--surface-1)] border border-[var(--border)] rounded-2xl shadow-sm flex flex-col justify-between gap-6">
                                <div>
                                    <h5 class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest mb-3">Resumen de costos</h5>
                                    
                                    <div class="divide-y divide-[var(--border)] text-xs font-semibold text-[var(--text-secondary)]">
                                        <div class="py-2.5 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">Producto (FOB)</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="cifFobUsd">$ 0.00</span>
                                        </div>
                                        <div class="py-2.5 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">Envío</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="cifFleteUsd">$ 0.00</span>
                                        </div>
                                        <div class="py-2.5 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">Seguro</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="cifSeguroUsd">$ 0.00</span>
                                        </div>
                                        <div class="py-3 flex justify-between font-black border-t-2 border-[var(--accent)]/20">
                                            <span class="text-[var(--accent)]">Valor CIF</span>
                                            <span class="text-[var(--accent)] font-mono text-sm" id="cifCifUsd">$ 0.00</span>
                                        </div>
                                    </div>
                                    
                                    <!-- Tributos section -->
                                    <h5 class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest mt-4 mb-2">Tributos aproximados</h5>
                                    <div class="divide-y divide-[var(--border)] text-[11px] font-semibold text-[var(--text-secondary)]">
                                        <div class="py-2 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">Ad Valorem (0%)</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="taxVal-adValorem">S/ 0.00</span>
                                        </div>
                                        <div class="py-2 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">IGV (16%)</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="taxVal-igv">S/ 0.00</span>
                                        </div>
                                        <div class="py-2 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">IPM (2.4%)</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="taxVal-ipm">S/ 0.00</span>
                                        </div>
                                        <div class="py-2 flex justify-between">
                                            <span class="text-[var(--text-tertiary)]">Percepción (3.5%)</span>
                                            <span class="text-[var(--text-primary)] font-mono" id="taxVal-percepcion">S/ 0.00</span>
                                        </div>
                                        <div class="py-3 flex justify-between font-black text-emerald-600 border-t border-[var(--border)]">
                                            <span>Total estimado</span>
                                            <span class="font-mono text-sm text-emerald-600" id="cifCifPen">S/ 0.00</span>
                                        </div>
                                        <!-- Semáforo de Carga Tributaria -->
                                        <div id="taxBurdenTrafficLight" class="mt-4 p-4 rounded-xl border transition-all duration-300"></div>
                                    </div>
                                </div>
                                
                                <div class="didactic-box p-4 bg-[var(--surface-0)] border border-[var(--border)] rounded-xl text-[9px] text-[var(--text-tertiary)] font-semibold leading-relaxed">
                                    El valor CIF es la base usual para estimar tributos. Normalmente suma producto + envío + seguro.
                                    <div class="mt-3 flex flex-wrap gap-2">
                                        <button type="button" onclick="openKnowledgePanel('valor_fob')" class="inline-flex px-3 py-1 rounded-full bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black uppercase tracking-widest text-[9px] hover:bg-[var(--surface-2)]">
                                            Producto sin envio
                                        </button>
                                        <button type="button" onclick="openKnowledgePanel('valor_cif')" class="inline-flex px-3 py-1 rounded-full bg-[var(--surface-1)] border border-[var(--border)] text-[var(--accent)] font-black uppercase tracking-widest text-[9px] hover:bg-[var(--surface-2)]">
                                            Producto con envio
                                        </button>
                                        <button type="button" onclick="openIncotermsLab()" class="inline-flex px-3 py-1 rounded-full bg-[var(--accent)] border border-[var(--accent)] text-white font-black uppercase tracking-widest text-[9px] hover:bg-[var(--accent-hover)]">
                                            Comparar quien paga
                                        </button>
                                    </div>
                                </div>

                                <!-- Expander de Opciones Avanzadas -->
                                <details class="text-[9px] font-bold text-[var(--text-tertiary)]">
                                    <summary class="cursor-pointer hover:text-[var(--text-primary)] uppercase tracking-wider">Opciones avanzadas</summary>
                                    <div class="grid grid-cols-3 gap-2 mt-3">
                                        <div>
                                            <label class="text-[var(--text-tertiary)] block mb-0.5">Tipo de contribuyente</label>
                                            <select id="tribPerfil" onchange="updateTaxes()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded p-1 text-[var(--text-primary)]">
                                                <option value="ESTANDAR" selected>Recurrente (3.5% Percepción)</option>
                                                <option value="NUEVO">Nuevo (10% Percepción)</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label class="text-[var(--text-tertiary)] block mb-0.5">Acuerdo comercial</label>
                                            <select id="tribTlc" onchange="updateTaxes()" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded p-1 text-[var(--text-primary)]">
                                                <option value="NO" selected>Sin TLC</option>
                                                <option value="SI">Con TLC (0% Arancel)</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label class="text-[var(--text-tertiary)] block mb-0.5">Documento de origen</label>
                                            <select id="tribCertificado" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded p-1 text-[var(--text-primary)]">
                                                <option value="Pendiente" selected>No Presentado</option>
                                                <option value="Emitido">Validado</option>
                                            </select>
                                        </div>
                                        <div class="hidden">
                                            <input type="number" id="logSeguro" min="0" step="0.01" value="80.00" oninput="wizardData.manualCostsModified = true; updateCIFCalculations()">
                                        </div>
                                    </div>
                                </details>
                            </div>
                        </div>
                    </div>

                    <!-- PASO 4: Revisión final (Documentos + Plan) -->
                    <div id="stepGroup-4" class="step-content">
                        <div class="flex items-center justify-between mb-4">
                            <div>
                                <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest block">Paso 4 de 4</span>
                                <h4 class="text-2xl font-black text-[var(--text-primary)] tracking-tight mt-1">Revisión final</h4>
                            </div>
                        </div>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">Revisa los documentos que necesitas y el plan final antes de guardar.</p>

                        <h5 class="text-sm font-black text-[var(--text-primary)] mt-6 mb-3 flex items-center gap-2">
                            <svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                            </svg>
                            Documentos que debes tener
                        </h5>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">Marca lo que ya tienes. Si falta algo, el sistema te lo deja como pendiente para no avanzar a ciegas.</p>

                        <!-- Caja Didáctica -->
                        <div class="didactic-box p-4 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <strong>Por que importan</strong><br>
                            Estos documentos sustentan el valor, el transporte y el origen de tu mercancia. No son teoria: son el respaldo que suelen pedir para revisar la importacion.
                        </div>

                        <div class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-4 mb-6">
                            <h5 class="text-[10px] font-black text-amber-600 uppercase tracking-widest mb-1 flex justify-between">
                                <span>Estado de tu carpeta de trabajo</span>
                                <span class="text-gray-500 font-mono text-[9px]">Opcional</span>
                            </h5>
                            
                            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-xs font-semibold">
                                <div>
                                    <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Estado de tu expediente *</label>
                                    <select id="vuceEstado" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2.5 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)] font-bold">
                                        <option value="BORRADOR">Borrador</option>
                                        <option value="EXPEDIENTE_GENERADO" selected>Expediente Generado</option>
                                        <option value="LISTO_PARA_VUCE">Listo para VUCE</option>
                                        <option value="ENVIADO_A_VUCE">Enviado a VUCE</option>
                                        <option value="APROBADO">Aprobado</option>
                                    </select>
                                </div>

                                <div>
                                    <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Numero de tramite, si ya lo tienes</label>
                                    <input type="text" id="vuceSuce" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all font-mono text-[var(--text-primary)]" placeholder="Ej: 20260408192839">
                                </div>

                                <div>
                                    <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Numero de resolucion o licencia</label>
                                    <input type="text" id="vuceResolucion" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all font-mono text-[var(--text-primary)]" placeholder="Ej: RD-2026-MTC-0842">
                                </div>

                                <div>
                                    <label class="text-[9px] font-bold text-[var(--text-secondary)] uppercase block mb-1">Observaciones</label>
                                    <input type="text" id="vuceObs" class="w-full bg-[var(--surface-1)] border border-[var(--border)] rounded-xl px-4 py-2 text-xs focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all text-[var(--text-primary)]" placeholder="Ninguna observación registrada">
                                </div>
                            </div>
                        </div>

                        <div class="p-5 rounded-2xl bg-[var(--surface-1)] border border-[var(--border)] mb-6 shadow-sm">
                            <div class="flex items-center gap-3">
                                <span class="w-10 h-10 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[var(--accent)] flex items-center justify-center font-black">OK</span>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--accent)]">Estado del expediente</p>
                                    <h5 class="text-lg font-black text-[var(--text-primary)] mt-1" id="expedienteReadyTitle">Faltan documentos base</h5>
                                    <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1" id="expedienteReadyText">Necesitas al menos factura comercial y documento de transporte para dejar lista la carpeta principal.</p>
                                </div>
                            </div>
                        </div>
                        <h5 class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-widest mb-3">Checklist basico de documentos</h5>
                        <div class="space-y-3" id="checklistDocsArea">
                            <!-- Factura -->
                            <div class="p-4 rounded-xl bg-[var(--surface-0)] border border-[var(--border)] flex items-center justify-between gap-4">
                                <div class="flex items-center gap-3">
                                    <span class="text-xs font-black px-2 py-1 rounded bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-primary)]">FC</span>
                                    <div>
                                        <h6 class="text-xs font-bold text-[var(--text-primary)] uppercase tracking-wider">Factura comercial *</h6>
                                        <p class="text-[9px] text-[var(--text-tertiary)] mt-0.5">Documento que acredita la compraventa internacional y sustenta el valor FOB pactado.</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-2 shrink-0">
                                    <button type="button" onclick="openKnowledgePanel('factura_comercial')" class="px-3 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-[var(--surface-1)] text-[var(--accent)] border border-[var(--border)] hover:bg-[var(--surface-2)]">Que es</button>
                                    <button onclick="toggleDoc('FACTURA_COMERCIAL')" id="btnDoc-FACTURA_COMERCIAL" class="px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 dark:bg-rose-950/20 dark:text-rose-400 dark:border-rose-950">Pendiente</button>
                                    <span id="docFileIndicator-FACTURA_COMERCIAL" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                </div>
                            </div>

                            <!-- BL -->
                            <div class="p-4 rounded-xl bg-[var(--surface-0)] border border-[var(--border)] flex items-center justify-between gap-4">
                                <div class="flex items-center gap-3">
                                    <span class="text-xs font-black px-2 py-1 rounded bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-primary)]">BL</span>
                                    <div>
                                        <h6 class="text-xs font-bold text-[var(--text-primary)] uppercase tracking-wider">Documento de transporte (BL / AWB) *</h6>
                                        <p class="text-[9px] text-[var(--text-tertiary)] mt-0.5">Contrato de transporte oficial emitido por la naviera o aerolínea de carga.</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-2 shrink-0">
                                    <button type="button" onclick="openKnowledgePanel('bill_of_lading')" class="px-3 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-[var(--surface-1)] text-[var(--accent)] border border-[var(--border)] hover:bg-[var(--surface-2)]">Que es</button>
                                    <button onclick="toggleDoc('BILL_OF_LADING')" id="btnDoc-BILL_OF_LADING" class="px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 dark:bg-rose-950/20 dark:text-rose-400 dark:border-rose-950">Pendiente</button>
                                    <span id="docFileIndicator-BILL_OF_LADING" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                </div>
                            </div>

                            <!-- Certificado de Origen -->
                            <div class="p-4 rounded-xl bg-[var(--surface-0)] border border-[var(--border)] flex items-center justify-between gap-4">
                                <div class="flex items-center gap-3">
                                    <span class="text-xs font-black px-2 py-1 rounded bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-primary)]">CO</span>
                                    <div>
                                        <h6 class="text-xs font-bold text-[var(--text-primary)] uppercase tracking-wider">Certificado de Origen (TLC)</h6>
                                        <p class="text-[9px] text-[var(--text-tertiary)] mt-0.5">Documento que acredita que el producto se fabricó en el origen para beneficiarse de TLC.</p>
                                    </div>
                                </div>
                                <div class="flex items-center gap-2 shrink-0">
                                    <button type="button" onclick="openKnowledgePanel('certificado_origen')" class="px-3 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-[var(--surface-1)] text-[var(--accent)] border border-[var(--border)] hover:bg-[var(--surface-2)]">Que es</button>
                                    <button onclick="toggleDoc('CERTIFICADO_ORIGEN')" id="btnDoc-CERTIFICADO_ORIGEN" class="px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 dark:bg-rose-950/20 dark:text-rose-400 dark:border-rose-950">Pendiente</button>
                                    <span id="docFileIndicator-CERTIFICADO_ORIGEN" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                </div>
                            </div>
                        </div>

                        <!-- Plan Final (dentro del Paso 4) -->
                        <h5 class="text-sm font-black text-[var(--text-primary)] mt-10 mb-3 flex items-center gap-2 border-t border-[var(--border)] pt-6">
                            <svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/>
                            </svg>
                            Tu plan final
                        </h5>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mb-6">Te dejamos una conclusion clara: continuar, revisar algo antes o detener la compra hasta validar.</p>

                        <!-- Caja Didáctica -->
                        <div class="didactic-box p-4 mb-6 bg-[var(--accent-soft)] border border-[var(--accent-glow)] rounded-2xl text-[11px] text-[var(--text-secondary)] leading-relaxed">
                            <strong>Listo: ya tienes una guia de accion.</strong><br>
                            Revisa el resumen, guarda la evaluacion y continua desde Seguimiento cuando tengas nuevos documentos o respuestas del proveedor.
                        </div>

                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 text-xs font-semibold">
                            <!-- Datos Finales Consolidados -->
                            <div class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-3">
                                <h5 class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest mb-1">A. Datos de la importación</h5>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Producto Evaluado:</span>
                                    <span class="text-[var(--text-primary)] font-bold" id="resProd">--</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Importador / RUC:</span>
                                    <span class="text-[var(--text-primary)]" id="resRuc"><%= safeUserRuc %> - <%= safeUserNombre %></span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Codigo probable del producto:</span>
                                    <span class="text-[var(--accent)] font-mono font-bold" id="resHS">--</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Precio del producto:</span>
                                    <span class="text-[var(--text-primary)] font-mono" id="resFob">$ 0.00</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Base estimada para impuestos:</span>
                                    <span class="text-[var(--text-primary)] font-mono" id="resCIF">$ 0.00</span>
                                </div>
                            </div>

                            <!-- Resumen VUCE e Impuestos -->
                            <div class="p-5 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-3">
                                <h5 class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest mb-1">B. Impuestos y Permisos</h5>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Impuestos Aproximados (PEN):</span>
                                    <span class="text-emerald-600 font-mono text-sm font-black" id="resImpuestos">S/ 0.00</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Permisos:</span>
                                    <span id="resVuce" class="text-orange-600 font-bold">--</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-[var(--text-tertiary)]">Tramite asociado:</span>
                                    <span id="resSuce" class="text-[var(--text-primary)] font-mono">--</span>
                                </div>
                                <div class="flex justify-between text-[10px] font-black text-[var(--text-tertiary)] uppercase mt-2 pt-2 border-t border-[var(--border)]">
                                    <span>Código de seguridad digital:</span>
                                    <span class="text-[var(--accent)] font-mono" id="resFirma">C3F1B4E8...</span>
                                </div>
                            </div>
                        </div>

                        <div class="mt-6 p-6 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-2">
                            <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--accent)]">Conclusión</p>
                            <h5 class="text-xl font-black text-[var(--text-primary)]" id="finalDecisionTitle">Estamos preparando tu decisión final</h5>
                            <p class="text-sm text-[var(--text-secondary)] font-semibold" id="finalDecisionText">Completa los pasos anteriores para ver la recomendación principal.</p>
                        </div>
                        <!-- PLAN DE ACCION RECOMENDADO -->
                        <div class="mt-6 p-6 rounded-2xl bg-[var(--surface-1)] border border-[var(--border)] space-y-3 shadow-sm">
                            <h5 class="text-xs font-black text-amber-600 uppercase tracking-widest flex items-center gap-2">
                                <svg class="w-4 h-4 text-amber-500" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01"/>
                                </svg>
                                ¿Quéé debes hacer ahora? (Plan de Acción sugerido)
                            </h5>
                            <ol id="finalActionPlan" class="list-decimal pl-5 space-y-2 text-xs font-semibold text-[var(--text-secondary)]">
                                <li><strong>Solicita la ficha técnica al proveedor:</strong> Ayudará a la aduana a clasificar de forma exacta el producto.</li>
                                <li><strong>Confirma especificaciones inalámbricas:</strong> Si el producto tiene WiFi o Bluetooth, necesitarás homologación en el MTC.</li>
                                <li><strong>Prepara el expediente comercial:</strong> Asegúrate de que el proveedor emita la factura comercial exacta.</li>
                                <li><strong>Guarda esta evaluación:</strong> Quedará archivada en tu historial para cualquier consulta futura.</li>
                                <li><strong>Descarga el borrador final:</strong> Obtén el expediente de preimportación referencial en PDF.</li>
                            </ol>
                        </div>

                        <!-- Download Frame -->
                        <div class="mt-6 p-6 rounded-2xl bg-gradient-to-r from-blue-50 to-emerald-50 border border-[var(--border)] flex flex-col md:flex-row items-center justify-between gap-4 dark:from-zinc-900 dark:to-zinc-800">
                            <div class="flex items-center gap-3">
                                <span class="text-3xl shrink-0">??</span>
                                <div>
                                    <h5 class="text-sm font-black text-[var(--accent)] uppercase tracking-wider">Descargar tu Plan de Importación PDF</h5>
                                    <p class="text-[10px] text-[var(--text-secondary)]">Expediente borrador en PDF con trazabilidad digital para que no olvides ningún paso.</p>
                                </div>
                            </div>
                            <button id="btnDownloadPDF" onclick="descargarExpedienteFinal()" disabled class="bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black px-6 py-3 rounded-xl text-xs uppercase tracking-widest transition-all shadow-md disabled:opacity-30 disabled:cursor-not-allowed">
                                Descargar PDF
                            </button>
                        </div>
                    </div>

                <!-- Footer Buttons -->
                <div class="mt-8 pt-6 border-t border-[var(--border)] flex justify-between">
                    <button onclick="prevStep()" id="btnPrevStep" class="bg-[var(--surface-1)] hover:bg-[var(--surface-2)] text-[var(--text-primary)] font-black px-6 py-2.5 rounded-xl text-xs uppercase tracking-widest border border-[var(--border)] transition-all">Atras</button>
                    <button onclick="nextStep()" id="btnNextStep" class="bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black px-6 py-2.5 rounded-xl text-xs uppercase tracking-widest transition-all shadow-md">Continuar</button>
                </div>
            </div>

            <!-- Sidebar resumen fijo derecho -->
            <div class="wizard-summary-shell w-80 bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-6 flex flex-col justify-between overflow-y-auto custom-scrollbar shadow-sm">
                <div class="space-y-6">
                    <h3 class="text-xs font-black text-[var(--text-tertiary)] uppercase tracking-widest border-b border-[var(--border)] pb-3">Tu ruta actual</h3>
                    
                    <div class="space-y-4 text-xs font-semibold">
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Producto:</span>
                            <span class="text-[var(--text-primary)] block mt-0.5 leading-tight" id="sideProd">Aún no completado</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Procedencia:</span>
                            <span class="text-[var(--text-secondary)] block mt-0.5 leading-none" id="sidePais">Aún no especificado</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Codigo del producto:</span>
                            <span class="text-[var(--accent)] font-mono block mt-0.5 font-bold" id="sideHS">Aún no seleccionado</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Costo base estimado:</span>
                            <span class="text-[var(--text-primary)] font-mono block mt-0.5" id="sideCIF">Se calculará en el paso 3</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Impuestos aproximados:</span>
                            <span class="text-emerald-600 font-mono block mt-0.5 text-sm font-bold" id="sideTaxes">Se calculará en el paso 3</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Estado regulatorio:</span>
                            <span class="block mt-0.5 font-bold" id="sideVuce">Pendiente de revisión</span>
                        </div>
                        <div>
                            <span class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-wider block">Estado de avance:</span>
                            <span class="inline-block mt-1" id="sideEstado">En preparación</span>
                        </div>
                    </div>

                    <div class="action-panel p-4 bg-[var(--surface-0)] border border-[var(--border)] rounded-2xl">
                        <p class="text-[9px] font-black uppercase tracking-[0.18em] text-[var(--accent)]">Que hago ahora</p>
                        <h4 id="sideNextActionTitle" class="text-base font-black text-[var(--text-primary)] mt-2">Completa el producto</h4>
                        <p id="sideNextActionText" class="text-[11px] leading-relaxed text-[var(--text-secondary)] font-semibold mt-1">Describe qué importas para activar la ruta guiada.</p>
                        <button type="button" id="sideNextActionButton" class="w-full mt-4 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black py-2.5 rounded-xl text-[10px] uppercase tracking-widest shadow-md transition-all active:scale-98">
                            Ir al producto
                        </button>
                    </div>
                </div>

                <div class="pt-6 border-t border-[var(--border)] space-y-2">
                    <button onclick="guardarOperacionGeneral()" class="w-full bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black py-3 rounded-xl text-[10px] uppercase tracking-widest transition-all shadow-md">
                        Guardar operación
                    </button>
                    <a href="dashboard.jsp" class="w-full block text-center bg-[var(--surface-1)] hover:bg-[var(--surface-2)] text-[var(--text-primary)] font-black py-3 rounded-xl text-[10px] uppercase tracking-widest border border-[var(--border)] transition-all">
                        Cancelar
                    </a>
                </div>
            </div>

        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
        window.userRuc = '<%= safeUserRuc %>';
        window.userNombre = '<%= safeUserNombre %>';
    </script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/knowledge-base.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/index.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        // Init script when fully loaded
        document.addEventListener('DOMContentLoaded', () => {
            toggleModoDidactico();
            checkAndPromptDraft();
        });
        
        // Interactive choice selectors for Sí/No/No sé questions
        function selectQuestionOption(fieldId, value, buttonEl) {
            // Set hidden field value
            const hiddenInput = document.getElementById(fieldId);
            if (hiddenInput) {
                hiddenInput.value = value;
            }
            
            // Sync with active UI styles
            const btnGroup = buttonEl.parentElement;
            const buttons = btnGroup.querySelectorAll('.q-btn');
            buttons.forEach(btn => btn.classList.remove('active'));
            buttonEl.classList.add('active');
            
            // Trigger synchronization logic
            onVuceQuestionChange();
        }
    </script>
</body>
</html>




