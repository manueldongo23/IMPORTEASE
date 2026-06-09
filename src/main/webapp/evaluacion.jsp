<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.*" %>
<%!
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("</", "<\\/");
    }
%>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
    String userRuc = (String) session.getAttribute("usuarioRuc");
    String userNombre = (String) session.getAttribute("usuarioNombre");
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
    <link href="css/evaluacion.css" rel="stylesheet">
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

        <!-- ── TOPBAR NUEVA ── -->
        <div class="ev-topbar">
            <div class="ev-topbar-left">
                <a href="dashboard.jsp" class="ev-topbar-logo">
                    <div class="ev-topbar-logo-icon">
                        <svg width="18" height="18" fill="none" stroke="#fff" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 12h18M12 3c4.971 0 9 4.029 9 9s-4.029 9-9 9-9-4.029-9-9 4.029-9 9-9z"/></svg>
                    </div>
                    <span class="ev-topbar-logo-name">ImportEase</span>
                </a>
                <div class="ev-breadcrumb">
                    <span class="ev-breadcrumb-sep">›</span>
                    <a href="dashboard.jsp">Inicio</a>
                    <span class="ev-breadcrumb-sep">›</span>
                    <span class="ev-breadcrumb-current">Importación guiada</span>
                </div>
            </div>
            <div class="ev-topbar-center">
                <span class="ev-autosave-dot"></span>
                <span id="autosaveStatus">Guardado automáticamente</span>
            </div>
            <p class="ev-topbar-hint">Responde en simple. Nosotros lo traducimos a código, permisos, costos y documentos.</p>
        </div>

        <!-- ── STEPPER 4 PASOS ── -->
        <div class="ev-stepper">
            <div class="ev-step-item active" id="stepIndicator-1">
                <div class="ev-step-num font-bold">1</div>
                <div class="ev-step-text">
                    <span class="ev-step-title">1. Producto</span>
                    <span class="ev-step-desc">Cuéntanos qué quieres traer</span>
                </div>
            </div>
            <div class="ev-step-sep" id="timelineBar-1"></div>
            <div class="ev-step-item pending" id="stepIndicator-2">
                <div class="ev-step-num font-bold">2</div>
                <div class="ev-step-text">
                    <span class="ev-step-title">2. Código y permisos</span>
                    <span class="ev-step-desc">Detectamos el código y permisos</span>
                </div>
            </div>
            <div class="ev-step-sep" id="timelineBar-2"></div>
            <div class="ev-step-item pending" id="stepIndicator-3">
                <div class="ev-step-num font-bold">3</div>
                <div class="ev-step-text">
                    <span class="ev-step-title">3. Costos</span>
                    <span class="ev-step-desc">Calculamos el costo estimado</span>
                </div>
            </div>
            <div class="ev-step-sep" id="timelineBar-3"></div>
            <div class="ev-step-item pending" id="stepIndicator-4">
                <div class="ev-step-num font-bold">4</div>
                <div class="ev-step-text">
                    <span class="ev-step-title">4. Revisión final</span>
                    <span class="ev-step-desc">Confirmas y generamos tu ruta</span>
                </div>
            </div>
        </div>
        <!-- Barra porcentual oculta (compat. JS) -->
        <div class="hidden"><div id="progressBar"></div><span id="progressPercent">25%</span></div>

        <!-- ── WORKSPACE ── -->
        <div class="ev-workspace">
            <!-- MAIN SCROLL -->
            <div class="ev-main custom-scrollbar">
                <div class="space-y-6">
                    <!-- PASO 1: Producto y Origen (Humano) -->
                    <div id="stepGroup-1" class="step-content active">
                        <!-- Header with Title on Left and info card on Right -->
                        <div class="flex flex-col lg:flex-row justify-between items-start gap-6 mb-8">
                            <div class="flex-1">
                                <h2 class="text-3xl font-black text-[#1a1d2e] tracking-tight">Cuéntanos qué quieres traer</h2>
                                <p class="text-xs text-[#5a6275] font-semibold mt-2">No necesitas escribir como experto. Describe el producto como se lo explicarías a otra persona.</p>
                            </div>
                            <!-- Purple info card -->
                            <div class="w-full lg:w-96 bg-[#F5F3FF] border border-[#E0D9FF] rounded-2xl p-4 flex gap-3 items-start shrink-0">
                                <div class="w-8 h-8 rounded-lg bg-[#E0D9FF] flex items-center justify-center shrink-0 text-[#5B50F0]">
                                    <!-- Lightbulb icon -->
                                    <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v1m0-12a4 4 0 00-4 4v2.5a.5.5 0 01-.2.4l-.8.6a1 1 0 00-.4.8v1.2a1 1 0 001 1h8.8a1 1 0 001-1v-1.2a1 1 0 00-.4-.8l-.8-.6a.5.5 0 01-.2-.4V10a4 4 0 00-4-4z"/>
                                    </svg>
                                </div>
                                <div>
                                    <h5 class="text-xs font-bold text-[#1A1D2E]">¿Qué haremos con tu respuesta?</h5>
                                    <p class="text-[11px] text-[#5A6275] font-semibold leading-normal mt-1">
                                        Tomamos el nombre, uso, cantidad y origen para sugerir el código aduanero, detectar permisos y preparar el costo.
                                    </p>
                                </div>
                            </div>
                        </div>

                        <!-- Main Split Grid: Left Form, Right widgets -->
                        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <!-- Left: form fields -->
                            <div class="lg:col-span-2 space-y-6">
                                <!-- Question: ¿Para qué vas a importar? -->
                                <div>
                                                  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <!-- Option card: Personal -->
                                        <div id="routeCardPersonal" class="route-choice-card selected">
                                            <div class="route-icon-wrap">
                                                <!-- Profile icon -->
                                                <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/>
                                                </svg>
                                            </div>
                                            <div class="pr-6">
                                                <h4 class="text-xs font-bold text-[#1a1d2e] leading-snug">Quiero importar para uso propio</h4>
                                                <p class="text-[11px] text-[#6b7280] font-semibold leading-relaxed mt-1">Para mi uso personal o de mi familia. No es para la venta.</p>
                                            </div>
                                            <!-- Radio circle indicator -->
                                            <div class="route-radio">
                                                <div class="route-radio-dot"></div>
                                            </div>
                                        </div>
                                        <!-- Option card: Commercial -->
                                        <div id="routeCardComercial" class="route-choice-card">
                                            <div class="route-icon-wrap">
                                                <!-- Store icon -->
                                                <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                                                </svg>
                                            </div>
                                            <div class="pr-6">
                                                <h4 class="text-xs font-bold text-[#1a1d2e] leading-snug">Quiero importar para vender o abastecer mi negocio</h4>
                                                <p class="text-[11px] text-[#6b7280] font-semibold leading-relaxed mt-1">Para vender o abastecer mi empresa o emprendimiento.</p>
                                            </div>
                                            <!-- Radio circle indicator -->
                                            <div class="route-radio">
                                                <div class="route-radio-dot"></div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
 
                                <!-- Form Fields Grid -->
                                <div class="space-y-4">
                                    <!-- Row 1: Product Name & Description -->
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151] flex items-center gap-1">
                                                ¿Qué producto quieres importar?
                                                <span class="tooltip-icon" title="Describe brevemente el producto en español">?</span>
                                            </label>
                                            <input type="text" id="prodNombre" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white" placeholder="Ej. Celular Samsung Galaxy A55">
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151] flex items-center gap-1">
                                                ¿Para qué sirve o cómo es?
                                                <span class="tooltip-icon" title="Describe las funciones principales o características físicas">?</span>
                                            </label>
                                            <input type="text" id="prodTecnica" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white" placeholder="Ej. Teléfono inteligente con WiFi, Bluetooth, cámara y pantalla táctil">
                                        </div>
                                    </div>
 
                                    <!-- Row 2: Country of Origin & Intended Use -->
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151] flex items-center gap-1">
                                                ¿De qué país viene?
                                                <span class="tooltip-icon" title="País donde se fabrica o desde donde se envía la mercancía">?</span>
                                            </label>
                                            <select id="opPaisOrigen" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white cursor-pointer">
                                                <option value="" disabled>Selecciona el país de origen</option>
                                                <option value="CHINA" selected>China</option>
                                                <option value="USA">USA</option>
                                                <option value="ALEMANIA">Alemania</option>
                                                <option value="CHILE">Chile</option>
                                                <option value="ESPAÑA">España</option>
                                            </select>
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151] flex items-center gap-1">
                                                ¿Lo usarás para?
                                                <span class="tooltip-icon" title="Destino comercial o personal de la importación">?</span>
                                            </label>
                                            <select id="prodUso" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white cursor-pointer">
                                                <option value="" disabled>Selecciona una opción</option>
                                                <option value="PERSONAL" selected>Uso propio</option>
                                                <option value="COMERCIAL">Vender o abastecer negocio</option>
                                            </select>
                                        </div>
                                    </div>
 
                                    <!-- Row 3: Units, Brand, Model -->
                                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151] flex items-center gap-1">
                                                ¿Cuántas unidades traerás?
                                                <span class="tooltip-icon" title="Cantidad física de unidades a importar">?</span>
                                            </label>
                                            <input type="number" id="prodCantidad" min="1" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white" placeholder="Ej. 1" value="1">
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151]">Marca (opcional)</label>
                                            <input type="text" id="prodMarca" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white" placeholder="Ej. Samsung">
                                        </div>
                                        <div class="flex flex-col gap-1.5">
                                            <label class="text-[11px] font-bold text-[#374151]">Modelo (opcional)</label>
                                            <input type="text" id="prodModelo" class="w-full border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-semibold bg-white" placeholder="Ej. Galaxy A55">
                                        </div>
                                    </div>
                                </div>

                                <!-- Examples Section -->
                                <div class="pt-2">
                                    <h5 class="text-[11px] font-bold text-[#374151] mb-2 flex items-center gap-1.5">
                                        <!-- Lightning icon -->
                                        <svg class="w-3.5 h-3.5 text-[#5B50F0]" fill="currentColor" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clip-rule="evenodd"/>
                                        </svg>
                                        Ejemplos rápidos
                                    </h5>
                                    <div class="flex flex-wrap gap-2" id="examplesPillContainer">
                                        <!-- Cellphone example -->
                                        <button type="button" data-example="celular" class="example-pill px-3 py-1.5 rounded-full border border-[#c4b5fd] bg-[#f5f3ff] text-xs font-bold text-[#5B50F0] flex items-center gap-1.5 hover:scale-95 transition-all">
                                            <!-- Mobile phone icon -->
                                            <svg class="w-3.5 h-3.5 text-[#5B50F0]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z"/>
                                            </svg>
                                            Celular
                                        </button>
                                        <!-- Supplement example -->
                                        <button type="button" data-example="proteina" class="example-pill px-3 py-1.5 rounded-full border border-[#a7f3d0] bg-[#ecfdf5] text-xs font-bold text-[#059669] flex items-center gap-1.5 hover:scale-95 transition-all">
                                            <!-- Supplement jar/leaf icon -->
                                            <svg class="w-3.5 h-3.5 text-[#059669]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
                                            </svg>
                                            Suplemento
                                        </button>
                                        <!-- Electronic example -->
                                        <button type="button" data-example="rayos" class="example-pill px-3 py-1.5 rounded-full border border-[#bfdbfe] bg-[#eff6ff] text-xs font-bold text-[#2563eb] flex items-center gap-1.5 hover:scale-95 transition-all">
                                            <!-- Monitor icon -->
                                            <svg class="w-3.5 h-3.5 text-[#2563eb]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"/>
                                            </svg>
                                            Electrónico
                                        </button>
                                        <!-- Seed example -->
                                        <button type="button" data-example="semilla" class="example-pill px-3 py-1.5 rounded-full border border-[#bbf7d0] bg-[#f0fdf4] text-xs font-bold text-[#16a34a] flex items-center gap-1.5 hover:scale-95 transition-all">
                                            <!-- Leaf icon -->
                                            <svg class="w-3.5 h-3.5 text-[#16a34a]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
                                            Semilla
                                        </button>
                                        <!-- Wood example -->
                                        <button type="button" data-example="madera" class="example-pill px-3 py-1.5 rounded-full border border-[#fed7aa] bg-[#fff7ed] text-xs font-bold text-[#ea580c] flex items-center gap-1.5 hover:scale-95 transition-all">
                                            <!-- Log icon -->
                                            <svg class="w-3.5 h-3.5 text-[#ea580c]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M4 10h16M4 14h16M4 6h16M4 18h16"/>
                                            </svg>
                                            Madera
                                        </button>
                                    </div>
                                </div>
                            </div>

                            <!-- Right widgets -->
                            <div class="space-y-6">
                                <!-- Card: Qué conviene contarnos -->
                                <div class="border border-[#e8eaf0] bg-white rounded-2xl p-6 flex flex-col items-center text-center relative overflow-hidden shadow-sm">
                                    <div class="absolute -top-10 -left-10 w-28 h-28 bg-[#EFF6FF] rounded-full z-0 opacity-60"></div>
                                    <!-- Sparkly 01 -->
                                    <div class="relative z-10 flex items-start gap-1">
                                        <span class="text-7xl font-extrabold text-[#DCE6FC] tracking-tighter leading-none select-none">01</span>
                                        <!-- Sparkle svg indicator -->
                                        <svg class="w-5 h-5 text-[#5B50F0] mt-1 shrink-0" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M12 2L14.5 9.5L22 12L14.5 14.5L12 22L9.5 14.5L2 12L9.5 9.5L12 2Z"/>
                                        </svg>
                                    </div>
                                    
                                    <h4 class="text-sm font-bold text-[#1A1D2E] mt-3 relative z-10">Qué conviene contarnos aquí</h4>
                                    <div class="w-full space-y-3 mt-4 text-left relative z-10">
                                        <div class="flex items-center gap-2.5 text-xs text-[#374151] font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-[#5B50F0] text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3.5" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
                                                </svg>
                                            </span>
                                            Qué es el producto
                                        </div>
                                        <div class="flex items-center gap-2.5 text-xs text-[#374151] font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-[#5B50F0] text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3.5" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
                                                </svg>
                                            </span>
                                            Para qué lo usarás
                                        </div>
                                        <div class="flex items-center gap-2.5 text-xs text-[#374151] font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-[#5B50F0] text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3.5" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/>
                                                </svg>
                                            </span>
                                            De dónde viene y cuántas unidades traerás
                                        </div>
                                    </div>
                                </div>

                                <!-- Route status card: Green -->
                                <div id="activeRoutePanel" class="border border-[#A7F3D0] bg-[#F0FDF4] rounded-2xl p-5 flex justify-between items-center shadow-sm">
                                    <div>
                                        <span class="text-[10px] font-black text-[#059669] uppercase tracking-wider block">Ruta activa</span>
                                        <h4 id="activeRouteTitle" class="text-sm font-black text-[#1A1D2E] mt-1.5">Uso propio</h4>
                                        <p id="activeRouteDesc" class="text-[11px] text-[#5A6275] font-semibold mt-1">Estamos configurando esta importación para uso personal.</p>
                                    </div>
                                    <!-- Profile circle icon -->
                                    <div class="w-12 h-12 rounded-full bg-[#D1FAE5] text-[#059669] flex items-center justify-center shrink-0">
                                        <svg id="activeRouteIcon" class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/>
                                        </svg>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <!-- Campos técnicos J2EE ocultos o autogestionados -->
                        <div class="hidden">
                            <input type="text" id="opNombre" value="Evaluación de producto">
                            <input type="hidden" id="qWifi" value="NO">
                            <input type="hidden" id="qConsumo" value="NO">
                            <input type="hidden" id="qSalud" value="NO">
                            <input type="hidden" id="qContacto" value="NO">
                            <input type="hidden" id="qUsado" value="NO">
                            <input type="hidden" id="qMadera" value="NO">
                            <select id="opTipo"><option value="PERSONAL" selected>Personal</option><option value="COMERCIAL">Comercial</option></select>
                            <input type="text" id="opRuc" value="<%= userRuc != null ? userRuc : "" %>">
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

                    <!-- PASO 2: Código y permisos -->
                    <div id="stepGroup-2" class="step-content">
                        <!-- Header / Title -->
                        <div class="flex items-center gap-4 mb-6">
                            <div class="w-12 h-12 rounded-full border border-gray-200 bg-gray-50 flex items-center justify-center shrink-0">
                                <svg class="w-6 h-6 text-[#5B50F0]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/>
                                </svg>
                            </div>
                            <div>
                                <h2 class="text-2xl font-black text-[#1a1d2e] tracking-tight leading-tight">Encontramos un código probable para tu producto</h2>
                                <p class="text-xs text-[#5a6275] font-semibold mt-1">Te mostramos la opción más parecida y otras alternativas para que elijas con calma.</p>
                            </div>
                        </div>

                        <!-- Main Split Grid: Left Form/Table, Right restriction card -->
                        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
                            <!-- Left: Suggested Code Card and Alternatives -->
                            <div class="lg:col-span-2 space-y-6">
                                <!-- Info alert bar (purple info card) - spans full width of left column -->
                                <div class="w-full bg-[#F5F3FF] border border-[#E0D9FF] rounded-2xl p-4 flex flex-col lg:flex-row lg:items-center justify-between gap-4">
                                    <div class="flex gap-3 items-start">
                                        <div class="w-8 h-8 rounded-lg bg-[#E0D9FF] flex items-center justify-center shrink-0 text-[#5B50F0] mt-0.5">
                                            <!-- Info circle icon -->
                                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v1m0-12a4 4 0 00-4 4v2.5a.5.5 0 01-.2.4l-.8.6a1 1 0 00-.4.8v1.2a1 1 0 001 1h8.8a1 1 0 001-1v-1.2a1 1 0 00-.4-.8l-.8-.6a.5.5 0 01-.2-.4V10a4 4 0 00-4-4z"/>
                                            </svg>
                                        </div>
                                        <div>
                                            <h5 class="text-xs font-bold text-[#1A1D2E]">¿Qué es este código?</h5>
                                            <p class="text-[11px] text-[#5A6275] font-semibold leading-normal mt-1">
                                                Es la forma oficial en que aduanas identifica tu producto. De este código dependen los impuestos, permisos y documentos.
                                            </p>
                                        </div>
                                    </div>
                                    <button type="button" id="explainHSBtn" class="bg-white border border-[#E0D9FF] hover:bg-gray-50 text-[#5B50F0] font-black px-4 py-2.5 rounded-xl text-[10px] uppercase tracking-wider whitespace-nowrap transition-all shadow-sm cursor-pointer shrink-0">
                                        Explícamelo fácil ›
                                    </button>
                                </div>

                                <!-- Suggested Code Card -->
                                <div class="border border-[#e8eaf0] bg-white rounded-2xl p-6 shadow-sm relative flex flex-col lg:flex-row justify-between items-start lg:items-center gap-6">
                                    <div class="flex-1 space-y-3">
                                        <div>
                                            <span class="text-[10px] font-black text-[#5B50F0] uppercase tracking-wider block">Código sugerido</span>
                                            <div id="selectedHSCodeBig" class="text-3.5xl font-black text-[#5B50F0] tracking-tight mt-1">--</div>
                                            <div class="flex items-center gap-2 mt-1.5 flex-wrap">
                                                <h4 id="selectedHSLabel" class="text-base font-bold text-[#1a1d2e]">--</h4>
                                                <span id="hsMatchBadge" class="px-2 py-0.5 rounded bg-[#DCFCE7] border border-[#BBF7D0] text-[#15803D] text-[10px] font-bold uppercase">Coincidencia alta</span>
                                            </div>
                                            
                                            <div class="flex items-center gap-3 mt-2 text-[11px] text-[#5A6275] font-semibold">
                                                <span>Fuente oficial</span>
                                                <span class="text-gray-300">|</span>
                                                <span id="hsConfidenceText">Confianza <span class="text-[#1a1d2e] font-bold">98%</span></span>
                                            </div>
                                        </div>

                                        <!-- Digit breakdown boxes -->
                                        <div id="hsDigitContainer" class="flex gap-2">
                                            <!-- Rendered dynamically by applyHSCodeUI() -->
                                        </div>
                                        
                                        <p id="selectedHSExplanation" class="text-[11px] text-[#5a6275] font-semibold border-t border-dashed border-[#e8eaf0] pt-3 mt-3">Lo sugerimos porque tu producto coincide con artículos de perfumería para uso personal o comercial.</p>
                                    </div>
                                    
                                    <!-- Right Column: Actions -->
                                    <div class="w-full lg:w-auto flex flex-col items-center justify-center gap-3 shrink-0 lg:border-l lg:pl-6 border-t lg:border-t-0 pt-5 lg:pt-0 border-[#e8eaf0]">
                                        <button type="button" id="btnConfirmHS" class="w-full lg:w-auto bg-[#5B50F0] hover:bg-[#4a40df] text-white font-extrabold px-6 py-3.5 rounded-xl text-xs uppercase tracking-widest transition-all shadow-md flex items-center justify-center gap-2 border-none cursor-pointer">
                                            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/>
                                            </svg>
                                            Usar este código
                                        </button>
                                        <button type="button" id="btnWhySuggested" class="text-[#5B50F0] hover:underline text-xs font-bold bg-transparent border-none cursor-pointer mt-1">
                                            ¿Por qué lo sugerimos?
                                        </button>
                                    </div>
                                </div>

                                <!-- Other Options Table -->
                                <div class="border border-[#e8eaf0] bg-white rounded-2xl p-6 shadow-sm">
                                    <h4 class="text-xs font-bold text-[#1A1D2E] mb-4">Otras opciones parecidas</h4>
                                    
                                    <div class="overflow-x-auto">
                                        <table class="w-full text-left border-collapse">
                                            <thead>
                                                <tr class="border-b border-[#e8eaf0] text-[9px] uppercase tracking-wider text-[#5a6275] font-bold">
                                                    <th class="pb-3">Código</th>
                                                    <th class="pb-3">Descripción</th>
                                                    <th class="pb-3 text-center">Coincidencia</th>
                                                    <th class="pb-3">Permiso</th>
                                                    <th class="pb-3 text-center">Acción</th>
                                                </tr>
                                            </thead>
                                            <tbody id="hsSugerenciasTable">
                                                <!-- Rendered dynamically by index.js -->
                                            </tbody>
                                        </table>
                                    </div>
                                    
                                    <!-- Manual code input wrapper -->
                                    <div class="mt-6 pt-6 border-t border-[#e8eaf0] flex flex-col md:flex-row justify-between items-center gap-4">
                                        <div>
                                            <h5 class="text-[11px] font-bold text-[#1A1D2E]">¿No es ninguno de la lista?</h5>
                                            <p class="text-[10px] text-[#5a6275] font-semibold">Si conoces el código arancelario exacto, puedes ingresarlo manualmente aquí.</p>
                                        </div>
                                        <div class="flex items-center gap-2 w-full md:w-auto shrink-0">
                                            <input type="text" id="manualHSCode" class="border border-[#e8eaf0] rounded-xl px-4 py-2.5 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-mono font-bold w-full md:w-40 bg-white" placeholder="Ej. 3303.00.00.00">
                                            <button type="button" id="btnSelectManualHS" class="bg-[#EEF2F6] hover:bg-[#5B50F0] hover:text-white text-[#5B50F0] font-black px-4 py-2.5 rounded-xl text-xs transition-all border-none cursor-pointer shrink-0">
                                                Elegir
                                            </button>
                                        </div>
                                    </div>
                                </div>

                                <!-- Dynamic VUCE Confirmation Questions -->
                                <div>
                                    <div class="flex items-center gap-2">
                                        <div class="w-6 h-6 rounded-full bg-[#E0F2FE] text-[#0369A1] flex items-center justify-center font-bold text-xs shrink-0 font-sans">?</div>
                                        <h4 class="text-xs font-bold text-[#1A1D2E]">Preguntas rápidas para confirmar</h4>
                                    </div>
                                    <p id="dynamicQuestionMeta" class="text-[11px] text-[#5a6275] font-semibold mt-1">Estas respuestas nos ayudan a afinar el código y evitar confusiones.</p>
                                    
                                    <!-- 4 horizontal cards on desktop -->
                                    <div id="dynamicQuestionArea" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mt-4">
                                        <!-- Rendered dynamically by index.js -->
                                    </div>
                                </div>
                            </div>

                            <!-- Right: widgets / information blocks -->
                            <div class="space-y-6">
                                <!-- Card: Qué estamos revisando -->
                                <div class="ev-card-revisando rounded-2xl p-6 relative overflow-hidden shadow-sm flex flex-col justify-between min-h-[240px] text-white">
                                    <!-- Background decorative circle -->
                                    <div class="absolute -top-10 -right-10 w-32 h-32 bg-white/5 rounded-full pointer-events-none"></div>
                                    
                                    <div class="flex justify-between items-start mb-4">
                                        <!-- Header: "02 Qué estamos revisando ahora" -->
                                        <div class="relative z-10 flex flex-col items-start text-left">
                                            <div class="flex items-start gap-1">
                                                <span class="text-5xl font-extrabold text-[#EDE9FE]/20 tracking-tighter leading-none select-none">02</span>
                                                <!-- Sparkle svg -->
                                                <svg class="w-4 h-4 text-white mt-1 shrink-0" viewBox="0 0 24 24" fill="currentColor">
                                                    <path d="M12 2L14.5 9.5L22 12L14.5 14.5L12 22L9.5 14.5L2 12L9.5 9.5L12 2Z"/>
                                                </svg>
                                            </div>
                                            <h4 class="text-sm font-bold text-white mt-2">Qué estamos revisando ahora</h4>
                                        </div>
                                        <!-- Magnifying glass on cardboard box SVG Illustration -->
                                        <div class="relative z-10 w-16 h-16 shrink-0 flex items-center justify-center">
                                            <svg class="w-14 h-14 opacity-90" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M32 10L10 18V44L32 52L54 44V18L32 10Z" fill="#C7D2FE" fill-opacity="0.3"/>
                                                <path d="M32 10L10 18L32 26L54 18L32 10Z" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
                                                <path d="M10 18V44L32 52M54 18V44L32 52" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
                                                <path d="M32 26V52" stroke="white" stroke-width="1.5" stroke-linejoin="round"/>
                                                <!-- Magnifying glass -->
                                                <circle cx="42" cy="38" r="8" fill="#5B50F0" stroke="white" stroke-width="2"/>
                                                <path d="M47.5 43.5L56 52" stroke="white" stroke-width="2.5" stroke-linecap="round"/>
                                            </svg>
                                        </div>
                                    </div>
                                    
                                    <!-- Checklist layout -->
                                    <div class="w-full space-y-3 relative z-10">
                                        <div class="flex items-center gap-2.5 text-xs text-white/90 font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-white/20 text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="4" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                                            </span>
                                            Qué código se parece más a tu producto
                                        </div>
                                        <div class="flex items-center gap-2.5 text-xs text-white/90 font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-white/20 text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="4" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                                            </span>
                                            Si necesitas permiso antes de importar
                                        </div>
                                        <div class="flex items-center gap-2.5 text-xs text-white/90 font-semibold">
                                            <span class="w-5 h-5 rounded-full bg-white/20 text-white flex items-center justify-center shrink-0">
                                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="4" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                                            </span>
                                            Qué documentos podrías preparar
                                        </div>
                                    </div>
                                </div>
 
                                <!-- Dynamic Restriction Card -->
                                <div id="sideRestriccionContainer" class="border border-amber-200 bg-amber-50 rounded-2xl p-5 shadow-sm space-y-4">
                                    <div class="flex justify-between items-center">
                                        <div class="flex items-center gap-2">
                                            <div class="w-8 h-8 rounded-lg bg-amber-100/80 text-amber-600 flex items-center justify-center shrink-0">
                                                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                                                </svg>
                                            </div>
                                            <h5 id="sideRestriccionTitle" class="text-xs font-bold text-[#1A1D2E]">Posible permiso sanitario</h5>
                                        </div>
                                        <span id="sideRestriccionBadge" class="px-2 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-bold uppercase">Permiso posible</span>
                                    </div>
                                    
                                    <p id="sideRestriccionDesc" class="text-[11px] text-[#5A6275] font-semibold leading-relaxed">
                                        Los perfumes y cosméticos pueden requerir revisión sanitaria según su composición y uso.
                                    </p>
                                    
                                    <div class="grid grid-cols-2 gap-y-2 text-[11px] font-semibold text-[#5a6275]">
                                        <span>Entidad probable:</span>
                                        <span id="sideRestriccionEntidad" class="font-bold text-[#1A1D2E] text-right">DIGEMID</span>
                                        <span>Tipo de revisión:</span>
                                        <span id="sideRestriccionTipo" class="font-bold text-[#1A1D2E] text-right">Cosméticos / cuidado personal</span>
                                        <span>Estado:</span>
                                        <span id="sideRestriccionEstado" class="font-bold text-amber-600 text-right">Revisar antes de continuar</span>
                                    </div>
                                    
                                    <button type="button" id="btnVerTupaDetalle" class="w-full text-center py-2.5 rounded-xl border border-amber-300 hover:bg-amber-100/50 text-amber-800 text-[11px] font-bold transition-all bg-transparent cursor-pointer">
                                        Ver qué podrían pedir ›
                                    </button>
                                </div>
 
                                <!-- Card: Qué sigue después -->
                                <div class="border border-[#A7F3D0] bg-[#F0FDF4] rounded-2xl p-5 shadow-sm relative overflow-hidden">
                                    <div class="flex items-start justify-between gap-4">
                                        <div class="space-y-3 flex-1">
                                            <div class="flex items-center gap-2.5">
                                                <div class="w-8 h-8 rounded-full bg-[#D1FAE5] text-[#059669] flex items-center justify-center shrink-0">
                                                    <svg class="w-4.5 h-4.5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" d="M3 6a3 3 0 013-3h10a1 1 0 01.8 1.6L14.25 9l2.55 3.4A1 1 0 0116 14H6a1 1 0 00-1 1v3"/>
                                                    </svg>
                                                </div>
                                                <h5 class="text-xs font-bold text-[#1A1D2E]">Qué sigue después</h5>
                                            </div>
                                            <p class="text-[11px] text-[#5A6275] font-semibold leading-relaxed">
                                                Cuando confirmes el código, te mostraremos permisos, documentos y costo estimado.
                                            </p>
                                            
                                            <div class="pt-1">
                                                <span class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#D1FAE5] text-[#047857] text-[10px] font-bold">
                                                    <svg class="w-3.5 h-3.5 text-[#059669]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                        <path stroke-linecap="round" stroke-linejoin="round" d="M15 10.5a3 3 0 11-6 0 3 3 0 016 0z"/>
                                                        <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 10.5c0 7.142-7.5 11.25-7.5 11.25s-7.5-4.108-7.5-11.25a7.5 7.5 0 1115 0z"/>
                                                    </svg>
                                                    Ruta guiada activa
                                                </span>
                                            </div>
                                        </div>
                                        
                                        <!-- Document and Path Illustration -->
                                        <div class="w-16 h-20 shrink-0 flex items-center justify-center relative mt-2">
                                            <svg class="w-14 h-16" viewBox="0 0 60 80" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <!-- Dotted path -->
                                                <path d="M10 70C25 70 20 20 40 10" stroke="#059669" stroke-width="2" stroke-dasharray="3 3" stroke-linecap="round"/>
                                                <circle cx="40" cy="10" r="4" fill="#059669"/>
                                                <!-- Document sheet -->
                                                <g>
                                                    <rect x="15" y="15" width="35" height="48" rx="4" fill="white" stroke="#A7F3D0" stroke-width="1.5"/>
                                                    <!-- Lines on document -->
                                                    <line x1="21" y1="23" x2="31" y2="23" stroke="#D1FAE5" stroke-width="2.5" stroke-linecap="round"/>
                                                    <line x1="21" y1="29" x2="44" y2="29" stroke="#E5E7EB" stroke-width="1.5" stroke-linecap="round"/>
                                                    <line x1="21" y1="34" x2="40" y2="34" stroke="#E5E7EB" stroke-width="1.5" stroke-linecap="round"/>
                                                    <line x1="21" y1="39" x2="44" y2="39" stroke="#E5E7EB" stroke-width="1.5" stroke-linecap="round"/>
                                                    <line x1="21" y1="44" x2="35" y2="44" stroke="#E5E7EB" stroke-width="1.5" stroke-linecap="round"/>
                                                    <!-- Circular stamp or seal -->
                                                    <circle cx="40" cy="52" r="5" fill="#3B82F6" fill-opacity="0.2"/>
                                                    <circle cx="40" cy="52" r="3" fill="#3B82F6"/>
                                                </g>
                                            </svg>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                <!-- PASO 3: Costos por escenarios (Tributos) -->
                <div id="stepGroup-3" class="step-content">

                    <!-- Header Section -->
                    <div class="flex items-start justify-between mb-8">
                        <div>
                            <span class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest block mb-1">PASO 3 DE 4</span>
                            <h2 class="text-3xl font-black text-[#1a1d2e] tracking-tight">&#191;Cu&#225;nto podr&#237;a <span class="text-[#5B50F0]">costar</span> esta importaci&#243;n?</h2>
                            <p class="text-xs text-[#5a6275] font-semibold mt-2">Te mostramos un estimado detallado para que tomes la mejor decisi&#243;n antes de importar.</p>
                        </div>
                        <div class="hidden md:block w-48 shrink-0">
                            <img src="css/steps_icons.png" class="w-full h-auto object-contain" alt="Ilustración Costos">
                        </div>
                    </div>

                    <!-- Scenarios Grid -->
                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
                        <!-- Card Mínimo -->
                        <div class="rounded-2xl border border-[#e8eaf0] bg-white p-5 flex items-center justify-between shadow-sm hover:border-[#22c55e] transition-all cursor-pointer">
                            <div class="flex items-center gap-4">
                                <div class="w-12 h-12 rounded-full bg-[#E8F8EE] text-[#22C55E] flex items-center justify-center shrink-0">
                                    <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253" />
                                    </svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#9ca3af]">MÍNIMO</p>
                                    <p class="text-lg font-black text-[#1a1d2e] mt-0.5" id="scenarioMin">S/ 0.00</p>
                                    <p class="text-[10px] text-[#9ca3af] font-semibold">Escenario optimista</p>
                                </div>
                            </div>
                            <div class="w-5 h-5 rounded-full bg-[#E8F8EE] text-[#22C55E] flex items-center justify-center text-[10px] font-bold">i</div>
                        </div>

                        <!-- Card Esperado -->
                        <div class="rounded-2xl border-2 border-[#5B50F0] bg-[#F5F3FF] p-5 flex items-center justify-between shadow-sm cursor-pointer">
                            <div class="flex items-center gap-4">
                                <div class="w-12 h-12 rounded-full bg-[#E0D9FF] text-[#5B50F0] flex items-center justify-center shrink-0">
                                    <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                        <circle cx="12" cy="12" r="10" />
                                        <circle cx="12" cy="12" r="6" />
                                        <circle cx="12" cy="12" r="2" />
                                    </svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#5B50F0]">ESPERADO</p>
                                    <p class="text-lg font-black text-[#1a1d2e] mt-0.5" id="scenarioExpected">S/ 0.00</p>
                                    <p class="text-[10px] text-[#5B50F0] font-semibold">Referencia principal</p>
                                </div>
                            </div>
                            <div class="w-5 h-5 rounded-full bg-[#E0D9FF] text-[#5B50F0] flex items-center justify-center text-[10px] font-bold">i</div>
                        </div>

                        <!-- Card Conservador -->
                        <div class="rounded-2xl border border-[#e8eaf0] bg-white p-5 flex items-center justify-between shadow-sm hover:border-[#3b82f6] transition-all cursor-pointer">
                            <div class="flex items-center gap-4">
                                <div class="w-12 h-12 rounded-full bg-[#EFF6FF] text-[#3B82F6] flex items-center justify-center shrink-0">
                                    <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                                    </svg>
                                </div>
                                <div>
                                    <p class="text-[10px] font-black uppercase tracking-widest text-[#9ca3af]">CONSERVADOR</p>
                                    <p class="text-lg font-black text-[#1a1d2e] mt-0.5" id="scenarioMax">S/ 0.00</p>
                                    <p class="text-[10px] text-[#9ca3af] font-semibold">Con márgenes de seguridad</p>
                                </div>
                            </div>
                            <div class="w-5 h-5 rounded-full bg-[#EFF6FF] text-[#3B82F6] flex items-center justify-center text-[10px] font-bold">i</div>
                        </div>
                    </div>

                    <!-- Main 2-column Grid -->
                    <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">

                        <!-- Left Column: Form + Compare -->
                        <div class="lg:col-span-2 space-y-6">
                            <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm">
                                <div class="flex items-center justify-between border-b border-[#e8eaf0] pb-2 mb-5">
                                    <div class="flex items-center gap-2">
                                        <div class="w-6 h-6 bg-[#F5F3FF] text-[#5B50F0] rounded flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z" />
                                            </svg>
                                        </div>
                                        <h4 class="text-xs font-bold text-[#1a1d2e] font-sans">Valores del producto</h4>
                                    </div>
                                </div>
                                <div class="space-y-6">
                                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                                        <div>
                                            <label class="text-[11px] font-bold text-[#374151] mb-1.5 flex items-center gap-1">
                                                Costo del producto (FOB)
                                                <span class="tooltip-icon" title="Costo del producto en fábrica o puerto de origen">?</span>
                                            </label>
                                            <div class="relative flex items-center">
                                                <span class="absolute left-3 text-xs font-bold text-gray-400">USD</span>
                                                <input type="number" id="logFob" min="0" step="0.01" class="w-full bg-white border border-[#e8eaf0] rounded-xl pl-12 pr-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold" value="35.00">
                                            </div>
                                        </div>
                                        <div>
                                            <label class="text-[11px] font-bold text-[#374151] mb-1.5 flex items-center gap-1">
                                                Envío internacional
                                                <span class="tooltip-icon" title="Costo del flete de transporte internacional">?</span>
                                            </label>
                                            <div class="relative flex items-center">
                                                <span class="absolute left-3 text-xs font-bold text-gray-400">USD</span>
                                                <input type="number" id="logFlete" min="0" step="0.01" class="w-full bg-white border border-[#e8eaf0] rounded-xl pl-12 pr-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold" value="15.00">
                                            </div>
                                        </div>
                                        <div>
                                            <label class="text-[11px] font-bold text-[#374151] mb-1.5 flex items-center gap-1">
                                                Seguro internacional
                                                <span class="tooltip-icon" title="Costo del seguro de transporte de la mercancía">?</span>
                                            </label>
                                            <div class="relative flex items-center">
                                                <span class="absolute left-3 text-xs font-bold text-gray-400">USD</span>
                                                <input type="number" id="logSeguro" min="0" step="0.01" class="w-full bg-white border border-[#e8eaf0] rounded-xl pl-12 pr-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold" value="2.50">
                                            </div>
                                        </div>
                                    </div>
                                    <div>
                                        <label class="text-[11px] font-bold text-[#374151] mb-2 block">¿El proveedor incluye el envío internacional?</label>
                                        <div class="grid grid-cols-1 md:grid-cols-3 gap-4" id="incotermSelectorContainer">
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
                                    <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
                                        <div>
                                            <label class="text-[11px] font-bold text-[#374151] mb-1.5 block">Tipo de cambio referencial (HOY 10:31 a.m.)</label>
                                            <input type="text" id="logTC" class="w-full bg-[#F9FAFB] border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs font-mono font-bold text-gray-700 outline-none" value="3.725" readonly>
                                        </div>
                                        <div>
                                            <label class="text-[11px] font-bold text-[#374151] mb-1.5 block">Moneda base</label>
                                            <div class="flex items-center gap-2 border border-[#e8eaf0] bg-[#F9FAFB] rounded-xl px-4 py-3 text-xs text-[#1a1d2e] font-bold select-none h-[42px] mt-0">
                                                <svg class="w-5 h-4.5 rounded" viewBox="0 0 3 2">
                                                    <rect width="1" height="2" fill="#d91414"/>
                                                    <rect x="1" width="1" height="2" fill="#fff"/>
                                                    <rect x="2" width="1" height="2" fill="#d91414"/>
                                                </svg>
                                                <span>PEN (Soles)</span>
                                            </div>
                                        </div>
                                    </div>
                                    <div class="p-4 rounded-xl border border-[#E0D9FF] bg-[#F5F3FF] flex items-start gap-3">
                                        <div class="w-5 h-5 rounded-full bg-[#E0D9FF] text-[#5B50F0] flex items-center justify-center shrink-0 text-xs font-bold font-sans">i</div>
                                        <p class="text-[11px] text-[#5A6275] font-semibold leading-relaxed">Este cálculo es referencial. Los valores pueden variar según la fecha de importación y la empresa de logística.</p>
                                    </div>
                                </div>
                            </div>

                            <!-- Comparar escenarios -->
                            <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm flex flex-col md:flex-row items-center justify-between gap-4">
                                <div class="flex items-center gap-3">
                                    <div class="w-12 h-12 rounded-xl bg-[#F5F3FF] border border-[#E0D9FF] flex items-center justify-center text-[#5B50F0] shrink-0 shadow-sm">
                                        <svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 3v17M3 10h18M6 10v6a3 3 0 003 3h6a3 3 0 003-3v-6" />
                                        </svg>
                                    </div>
                                    <div>
                                        <h4 class="text-xs font-bold text-[#1A1D2E]">&#191;Quieres comparar con otro proveedor?</h4>
                                        <p class="text-[11px] text-[#5a6275] font-semibold mt-0.5">Compara escenarios y elige la mejor opci&#243;n para tu importaci&#243;n.</p>
                                    </div>
                                </div>
                                <div class="flex flex-wrap gap-2.5 shrink-0">
                                    <button type="button" data-knowledge="valor_fob" class="px-4 py-2.5 rounded-xl border border-[#5B50F0] bg-white text-[#5B50F0] text-xs font-bold hover:bg-[#F5F3FF] transition-all outline-none cursor-pointer">Producto sin env&#237;o</button>
                                    <button type="button" data-knowledge="valor_cif" class="px-4 py-2.5 rounded-xl border border-[#5B50F0] bg-white text-[#5B50F0] text-xs font-bold hover:bg-[#F5F3FF] transition-all outline-none cursor-pointer">Producto con env&#237;o</button>
                                    <button type="button" id="btnOpenIncotermsLab" class="px-4 py-2.5 rounded-xl border-none bg-[#5B50F0] text-white text-xs font-bold hover:bg-[#4a40df] transition-all outline-none cursor-pointer">Comparar qui&#233;n paga</button>
                                </div>
                            </div>
                        </div>

                        <!-- Right Column: Cost Summary + Traffic Light -->
                        <div class="space-y-6">
                            <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm">
                                <div class="flex items-center justify-between border-b border-[#e8eaf0] pb-2 mb-4">
                                    <div class="flex items-center gap-2">
                                        <div class="w-6 h-6 bg-[#F5F3FF] text-[#5B50F0] rounded flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5" />
                                            </svg>
                                        </div>
                                        <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest">Resumen de costos</h4>
                                    </div>
                                    <button type="button" id="btnOpenTaxesTable" class="text-[11px] font-bold text-[#5B50F0] hover:underline bg-transparent border-none cursor-pointer">Ver detalle</button>
                                </div>
                                <div class="space-y-3.5 text-xs font-semibold text-gray-600">
                                    <div class="flex justify-between items-center">
                                        <span>Producto (FOB)</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="cifFobUsd">$ 35.00</span>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <span>Env&#237;o internacional</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="cifFleteUsd">$ 15.00</span>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <span>Seguro internacional</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="cifSeguroUsd">$ 2.50</span>
                                    </div>
                                    <div class="flex justify-between items-center pt-3 pb-2 border-t border-dashed border-[#e8eaf0]">
                                        <span class="text-[#5B50F0] font-black text-[10px] uppercase">Valor CIF</span>
                                        <span class="font-mono font-black text-sm text-[#5B50F0]" id="cifCifUsd">S/ 195.57</span>
                                    </div>
                                </div>

                                <h5 class="text-[10px] font-black text-[#1a1d2e] uppercase tracking-widest mt-6 mb-3.5 pt-3 border-t border-[#e8eaf0]">Tributos aproximados</h5>
                                <div class="space-y-3 text-xs font-semibold text-gray-600">
                                    <div class="flex justify-between items-center">
                                        <span id="taxLabel-adValorem">Ad Valorem (0%)</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="taxVal-adValorem">S/ 0.00</span>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <span id="taxLabel-igv">IGV (18%)</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="taxVal-igv">S/ 35.20</span>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <span id="taxLabel-ipm">IPM (2.4%)</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="taxVal-ipm">S/ 4.69</span>
                                    </div>
                                    <div class="flex justify-between items-center">
                                        <span id="taxLabel-percepcion">Percepci&#243;n (3.5%)</span>
                                        <span class="font-mono text-[#1a1d2e] font-bold" id="taxVal-percepcion">S/ 6.35</span>
                                    </div>
                                </div>

                                <div class="pt-4 border-t-2 border-[#22c55e] mt-5 flex justify-between items-center">
                                    <span class="text-sm font-black text-[#22c55e] uppercase">Total estimado</span>
                                    <span class="font-mono text-base font-black text-[#22c55e]" id="cifCifPen">S/ 241.81</span>
                                </div>
                            </div>

                            <!-- Carga Tributaria Baja Widget -->
                            <div id="taxBurdenTrafficLight" class="p-5 rounded-2xl bg-[#E8F8EE] border border-[#B3EBD0] transition-all duration-300">
                                <div class="flex items-center gap-2.5 mb-1.5">
                                    <div class="w-5 h-5 rounded-full bg-[#22c55e] text-white flex items-center justify-center shrink-0">
                                        <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3.5" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                        </svg>
                                    </div>
                                    <h5 class="text-xs font-black text-[#15803D] uppercase tracking-wide">Carga Tributaria Baja</h5>
                                </div>
                                <p class="text-[11px] text-[#166534] font-semibold leading-relaxed">
                                    Tus tributos representan el <span class="font-bold">18.1%</span> del valor CIF. Excelente opción
                                </p>
                                <div class="w-full bg-white/40 h-2.5 rounded-full mt-3 overflow-hidden">
                                    <div class="bg-[#22c55e] h-full rounded-full transition-all duration-500" style="width: 18.1%;"></div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                    <!-- PASO 4: Revisión final (Documentos + Plan) -->
                    <div id="stepGroup-4" class="step-content">
                        <!-- Header Section -->
                        <div class="flex flex-col md:flex-row md:items-start justify-between mb-6 gap-4">
                            <div>
                                <span class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest block mb-1">PASO 4 DE 4</span>
                                <h2 class="text-3xl font-black text-[#1a1d2e] tracking-tight">Revisi&#243;n final</h2>
                                <p class="text-xs text-[#5a6275] font-semibold mt-2">Revisa los documentos que necesitas y el plan final antes de guardar.</p>
                            </div>
                        </div>

                        <!-- 2-Column Grid Layout -->
                        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6 mt-6">
                            
                            <!-- Left Column: inputs, state card, checklist -->
                            <div class="lg:col-span-2 space-y-6">
                                
                                <!-- Card 1: ESTADO DE TU CARPETA DE TRABAJO -->
                                <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm">
                                    <div class="flex items-center justify-between border-b border-[#e8eaf0] pb-2 mb-5">
                                        <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest flex items-center gap-2">
                                            <svg class="w-4 h-4 text-[#5B50F0]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.69-6.44l-2.12-2.12a1.5 1.5 0 00-1.061-.44H4.5A2.25 2.25 0 002.25 6v12a2.25 2.25 0 002.25 2.25h15A2.25 2.25 0 0021.75 18V9" />
                                            </svg>
                                            ESTADO DE TU CARPETA DE TRABAJO
                                        </h4>
                                        <span class="bg-[#F3F4F6] text-[#6B7280] font-bold text-[9px] px-2 py-0.5 rounded uppercase tracking-wider">OPCIONAL</span>
                                    </div>
                                    <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs font-semibold">
                                        <div>
                                            <label class="text-[9px] font-bold text-[#5a6275] uppercase block mb-1.5">Estado de tu expediente *</label>
                                            <select id="vuceEstado" class="w-full bg-white border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all text-[#1a1d2e] font-bold cursor-pointer">
                                                <option value="BORRADOR">Borrador</option>
                                                <option value="EXPEDIENTE_GENERADO" selected>Expediente Generado</option>
                                                <option value="LISTO_PARA_VUCE">Listo para VUCE</option>
                                                <option value="ENVIADO_A_VUCE">Enviado a VUCE</option>
                                                <option value="APROBADO">Aprobado</option>
                                            </select>
                                        </div>
                                        <div>
                                            <label class="text-[9px] font-bold text-[#5a6275] uppercase block mb-1.5">N&#176; de tr&#225;mite, si ya lo tienes</label>
                                            <input type="text" id="vuceSuce" class="w-full bg-white border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all font-mono text-[#1a1d2e] font-bold" placeholder="Ej: 28266488192839">
                                        </div>
                                        <div>
                                            <label class="text-[9px] font-bold text-[#5a6275] uppercase block mb-1.5">N&#176; de resoluci&#243;n o licencia</label>
                                            <input type="text" id="vuceResolucion" class="w-full bg-white border border-[#e8eaf0] rounded-xl px-4 py-3 text-xs focus:outline-none focus:border-[#5B50F0] transition-all font-mono text-[#1a1d2e] font-bold" placeholder="Ej: RD-2826-MTC-8842">
                                        </div>
                                        <!-- Hidden VUCE Obs field to avoid JS breakage -->
                                        <input type="hidden" id="vuceObs" value="">
                                    </div>
                                </div>

                                <!-- Card 2: ESTADO DEL EXPEDIENTE (Lavender/Lila alert card) -->
                                <div class="bg-gradient-to-r from-[#F5F3FF] to-[#FAF9FF] border border-[#E0D9FF] rounded-2xl p-6 shadow-sm relative overflow-hidden">
                                    <div class="absolute -right-4 -bottom-4 opacity-10">
                                        <svg class="w-32 h-32 text-[#5B50F0]" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z"/></svg>
                                    </div>
                                    <div class="flex flex-col sm:flex-row items-center justify-between gap-4 relative z-10">
                                        <div class="flex items-center gap-4">
                                            <div class="w-12 h-12 rounded-full bg-white border-2 border-[#5B50F0] flex items-center justify-center font-black text-[#5B50F0] shadow-sm shrink-0">
                                                OK
                                            </div>
                                            <div>
                                                <p class="text-[9px] font-black uppercase tracking-[0.2em] text-[#5B50F0]">ESTADO DEL EXPEDIENTE</p>
                                                <h5 class="text-base font-black text-[#1a1d2e] mt-1" id="expedienteReadyTitle">Primero revisa el permiso</h5>
                                                <p class="text-xs text-[#5a6275] font-semibold mt-1" id="expedienteReadyText">El producto podr&#237;a necesitar autorizaci&#243;n. Prepara los datos de la entidad antes de cerrar la carpeta.</p>
                                            </div>
                                        </div>
                                        <div class="shrink-0">
                                            <svg class="w-16 h-16 text-[#5B50F0] drop-shadow-md" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <rect x="8" y="14" width="48" height="36" rx="4" fill="#C7D2FE" opacity="0.5"/>
                                                <path d="M16 10H32L38 18H52V50H12V10H16Z" fill="#818CF8" opacity="0.9"/>
                                                <rect x="20" y="24" width="24" height="20" rx="2" fill="white"/>
                                                <path d="M32 30L37 35L30 40" stroke="#5B50F0" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                <circle cx="32" cy="34" r="6" fill="#5B50F0" opacity="0.2"/>
                                            </svg>
                                        </div>
                                    </div>
                                </div>

                                <!-- Card 3: CHECKLIST BÁSICO DE DOCUMENTOS -->
                                <div class="bg-white border border-[#e8eaf0] p-6 rounded-2xl shadow-sm">
                                    <div class="flex items-center gap-2 mb-4 pb-2 border-b border-[#e8eaf0]">
                                        <div class="w-6 h-6 bg-[#EFF6FF] text-[#5B50F0] rounded flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                                            </svg>
                                        </div>
                                        <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest">CHECKLIST B&#193;SICO DE DOCUMENTOS</h4>
                                    </div>
                                    <div class="space-y-3" id="checklistDocsArea">
                                        <!-- Factura -->
                                        <div class="p-4 rounded-xl bg-white border border-[#e8eaf0] flex items-center justify-between gap-4 hover:bg-gray-50 transition-all">
                                            <div class="flex items-center gap-3">
                                                <span class="text-xs font-black w-8 h-8 rounded-lg bg-[#F5F3FF] border border-[#E0D9FF] text-[#5B50F0] font-mono flex items-center justify-center shrink-0">FC</span>
                                                <div>
                                                    <h6 class="text-xs font-bold text-[#1a1d2e] uppercase tracking-wider font-sans">FACTURA COMERCIAL *</h6>
                                                    <p class="text-[9px] text-[#5a6275] font-semibold mt-0.5 font-sans">Documento que acredita la compraventa internacional y sustenta el valor FOB pactado.</p>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-3 shrink-0">
                                                <button type="button" data-knowledge="factura_comercial" class="px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-white text-[#5B50F0] border border-[#e8eaf0] hover:bg-[#F5F3FF] transition-all cursor-pointer">¿QUÉ ES?</button>
                                                <button type="button" data-doc="FACTURA_COMERCIAL" id="btnDoc-FACTURA_COMERCIAL" class="px-4 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer">PENDIENTE</button>
                                                <span id="docFileIndicator-FACTURA_COMERCIAL" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                                <input type="checkbox" data-check-doc="FACTURA_COMERCIAL" class="w-4 h-4 border border-gray-300 rounded focus:ring-0 cursor-pointer">
                                            </div>
                                        </div>

                                        <!-- BL -->
                                        <div class="p-4 rounded-xl bg-white border border-[#e8eaf0] flex items-center justify-between gap-4 hover:bg-gray-50 transition-all">
                                            <div class="flex items-center gap-3">
                                                <span class="text-xs font-black w-8 h-8 rounded-lg bg-[#F5F3FF] border border-[#E0D9FF] text-[#5B50F0] font-mono flex items-center justify-center shrink-0">BL</span>
                                                <div>
                                                    <h6 class="text-xs font-bold text-[#1a1d2e] uppercase tracking-wider font-sans">DOCUMENTO DE TRANSPORTE (BL / AWB) *</h6>
                                                    <p class="text-[9px] text-[#5a6275] font-semibold mt-0.5 font-sans">Contrato de transporte oficial emitido por la naviera o aerol&#237;nea de carga.</p>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-3 shrink-0">
                                                <button type="button" data-knowledge="bill_of_lading" class="px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-white text-[#5B50F0] border border-[#e8eaf0] hover:bg-[#F5F3FF] transition-all cursor-pointer">¿QUÉ ES?</button>
                                                <button type="button" data-doc="BILL_OF_LADING" id="btnDoc-BILL_OF_LADING" class="px-4 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer">PENDIENTE</button>
                                                <span id="docFileIndicator-BILL_OF_LADING" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                                <input type="checkbox" data-check-doc="BILL_OF_LADING" class="w-4 h-4 border border-gray-300 rounded focus:ring-0 cursor-pointer">
                                            </div>
                                        </div>

                                        <!-- Certificado de Origen -->
                                        <div class="p-4 rounded-xl bg-white border border-[#e8eaf0] flex items-center justify-between gap-4 hover:bg-gray-50 transition-all">
                                            <div class="flex items-center gap-3">
                                                <span class="text-xs font-black w-8 h-8 rounded-lg bg-[#F5F3FF] border border-[#E0D9FF] text-[#5B50F0] font-mono flex items-center justify-center shrink-0">CO</span>
                                                <div>
                                                    <h6 class="text-xs font-bold text-[#1a1d2e] uppercase tracking-wider font-sans">CERTIFICADO DE ORIGEN (TLC)</h6>
                                                    <p class="text-[9px] text-[#5a6275] font-semibold mt-0.5 font-sans">Documento que acredita que el producto se fabric&#243; en el origen para beneficiarse de TLC.</p>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-3 shrink-0">
                                                <button type="button" data-knowledge="certificado_origen" class="px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-white text-[#5B50F0] border border-[#e8eaf0] hover:bg-[#F5F3FF] transition-all cursor-pointer">¿QUÉ ES?</button>
                                                <button type="button" data-doc="CERTIFICADO_ORIGEN" id="btnDoc-CERTIFICADO_ORIGEN" class="px-4 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer">PENDIENTE</button>
                                                <span id="docFileIndicator-CERTIFICADO_ORIGEN" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                                <input type="checkbox" data-check-doc="CERTIFICADO_ORIGEN" class="w-4 h-4 border border-gray-300 rounded focus:ring-0 cursor-pointer">
                                            </div>
                                        </div>

                                        <!-- Packing List -->
                                        <div class="p-4 rounded-xl bg-white border border-[#e8eaf0] flex items-center justify-between gap-4 hover:bg-gray-50 transition-all">
                                            <div class="flex items-center gap-3">
                                                <span class="text-xs font-black w-8 h-8 rounded-lg bg-[#F5F3FF] border border-[#E0D9FF] text-[#5B50F0] font-mono flex items-center justify-center shrink-0">PL</span>
                                                <div>
                                                    <h6 class="text-xs font-bold text-[#1a1d2e] uppercase tracking-wider font-sans">LISTA DE EMPAQUE (PACKING LIST)</h6>
                                                    <p class="text-[9px] text-[#5a6275] font-semibold mt-0.5 font-sans">Detalle del contenido de cada bulto: cantidades, pesos y medidas.</p>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-3 shrink-0">
                                                <button type="button" data-knowledge="packing_list" class="px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-white text-[#5B50F0] border border-[#e8eaf0] hover:bg-[#F5F3FF] transition-all cursor-pointer">¿QUÉ ES?</button>
                                                <button type="button" data-doc="PACKING_LIST" id="btnDoc-PACKING_LIST" class="px-4 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer">PENDIENTE</button>
                                                <span id="docFileIndicator-PACKING_LIST" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                                <input type="checkbox" data-check-doc="PACKING_LIST" class="w-4 h-4 border border-gray-300 rounded focus:ring-0 cursor-pointer">
                                            </div>
                                        </div>

                                        <!-- Otros Documentos -->
                                        <div class="p-4 rounded-xl bg-white border border-[#e8eaf0] flex items-center justify-between gap-4 hover:bg-gray-50 transition-all">
                                            <div class="flex items-center gap-3">
                                                <span class="text-xs font-black w-8 h-8 rounded-lg bg-[#F5F3FF] border border-[#E0D9FF] text-[#5B50F0] font-mono flex items-center justify-center shrink-0">OT</span>
                                                <div>
                                                    <h6 class="text-xs font-bold text-[#1a1d2e] uppercase tracking-wider font-sans">OTROS DOCUMENTOS</h6>
                                                    <p class="text-[9px] text-[#5a6275] font-semibold mt-0.5 font-sans">Certificados, permisos especiales, fichas t&#233;cnicas, cat&#225;logos u otros documentos relevantes.</p>
                                                </div>
                                            </div>
                                            <div class="flex items-center gap-3 shrink-0">
                                                <button type="button" data-knowledge="otros_documentos" class="px-3 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-white text-[#5B50F0] border border-[#e8eaf0] hover:bg-[#F5F3FF] transition-all cursor-pointer">¿QUÉ ES?</button>
                                                <button type="button" data-doc="OTROS_DOCUMENTOS" id="btnDoc-OTROS_DOCUMENTOS" class="px-4 py-1.5 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer">PENDIENTE</button>
                                                <span id="docFileIndicator-OTROS_DOCUMENTOS" class="hidden ml-2 text-[9px] text-amber-400 font-bold" title="Recuerda subir el archivo real del documento">⚠ Sin archivo</span>
                                                <input type="checkbox" data-check-doc="OTROS_DOCUMENTOS" class="w-4 h-4 border border-gray-300 rounded focus:ring-0 cursor-pointer">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Right Column: didactic card, cost summary, plan summary -->
                            <div class="space-y-6">
                                
                                <!-- Card 1: ¿Por qué son importantes? -->
                                <div class="bg-[#F5F3FF] border border-[#E0D9FF] rounded-2xl p-5 shadow-sm relative overflow-hidden">
                                    <div class="flex items-start gap-4">
                                        <div class="w-10 h-10 rounded-full bg-white border border-[#E0D9FF] flex items-center justify-center text-[#5B50F0] shrink-0 shadow-sm">
                                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"/>
                                            </svg>
                                        </div>
                                        <div>
                                            <h4 class="text-xs font-bold text-[#1a1d2e] font-sans">&#191;Por qu&#233; son importantes?</h4>
                                            <p class="text-[11px] text-[#5a6275] font-semibold leading-relaxed mt-1">Estos documentos sustentan el valor, el transporte y el origen de tu mercanc&#237;a. No son te&#243;ricos o el respaldo que suelen pedir para revisar la importaci&#243;n.</p>
                                        </div>
                                    </div>
                                </div>

                                <!-- Card 2: RESUMEN RÁPIDO -->
                                <div class="bg-white border border-[#e8eaf0] p-5 rounded-2xl shadow-sm">
                                    <div class="flex items-center gap-2 mb-4 pb-2 border-b border-[#e8eaf0]">
                                        <div class="w-6 h-6 bg-[#EFF6FF] text-[#5B50F0] rounded flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                                            </svg>
                                        </div>
                                        <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest font-sans">RESUMEN R&#193;PIDO</h4>
                                    </div>
                                    <div class="space-y-3 text-xs font-semibold text-[#5a6275]">
                                        <div class="flex justify-between items-center">
                                            <span>Costo base estimado</span>
                                            <span class="font-mono text-[#1a1d2e] font-bold" id="resCIF">S/ 0.00</span>
                                        </div>
                                        <div class="flex justify-between items-center">
                                            <span>Impuestos aproximados</span>
                                            <span class="font-mono text-emerald-600 font-bold" id="resImpuestos">S/ 0.00</span>
                                        </div>
                                        <div class="flex justify-between items-center pt-3 pb-1 border-t border-dashed border-[#e8eaf0]">
                                            <span class="text-[#5B50F0] font-black uppercase text-[10px]">Costo total estimado</span>
                                            <span class="font-mono text-base font-black text-[#5B50F0]" id="resTotalPen">S/ 0.00</span>
                                        </div>
                                    </div>
                                    
                                    <!-- Dynamic traffic light panel for Step 4 -->
                                    <div id="step4TrafficLight" class="mt-4 p-4 rounded-xl bg-[#F0FDF4] border border-[#BBF7D0] transition-all duration-300">
                                        <div class="flex items-center gap-2 mb-1">
                                            <div id="step4TrafficLightCircle" class="w-4 h-4 rounded-full bg-[#22c55e] text-white flex items-center justify-center shrink-0">
                                                <svg class="w-2.5 h-2.5" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                                            </div>
                                            <h5 id="step4TrafficLightTitle" class="text-[10px] font-bold text-[#15803D]">Carga Tributaria Baja</h5>
                                        </div>
                                        <p id="step4TrafficLightText" class="text-[10px] text-[#166534] leading-relaxed">Tu c&#225;lculo representa 0.00 del valor FOB.<br>Esto de por s&#237; es sumamente favorable.</p>
                                    </div>
                                </div>

                                <!-- Card 3: TU PLAN FINAL -->
                                <div class="bg-white border border-[#e8eaf0] p-5 rounded-2xl shadow-sm">
                                    <div class="flex items-center gap-2 mb-4 pb-2 border-b border-[#e8eaf0]">
                                        <div class="w-6 h-6 bg-[#EFF6FF] text-[#5B50F0] rounded flex items-center justify-center shrink-0">
                                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                                            </svg>
                                        </div>
                                        <h4 class="text-[10px] font-black text-[#5B50F0] uppercase tracking-widest">TU PLAN FINAL</h4>
                                    </div>
                                    <p class="text-[10px] text-[#5a6275] font-semibold mb-4 leading-relaxed">Te dejamos una conclusi&#243;n clara para que avances con seguridad.</p>
                                    <div class="flex items-start gap-3 mt-4">
                                        <div class="w-6 h-6 rounded-full bg-[#E8F8F0] border border-[#B3EBD0] text-[#15803D] flex items-center justify-center shrink-0 mt-0.5">
                                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24">
                                                <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
                                            </svg>
                                        </div>
                                        <div>
                                            <h5 class="text-xs font-black text-[#1a1d2e]" id="finalDecisionTitle">Listo para confirmar</h5>
                                            <p class="text-[10px] text-[#5a6275] font-semibold mt-1 leading-relaxed" id="finalDecisionText">Revisa el resumen, guarda la evaluaci&#243;n y contin&#250;a desde Seguimiento cuando tengas nuevos documentos o respuestas del proveedor.</p>
                                        </div>
                                    </div>
                                </div>

                            </div>
                        </div>
                    </div>

                <!-- Footer Buttons -->
                <div class="mt-8 pt-6 border-t border-[var(--border)] flex flex-col sm:flex-row justify-between items-center gap-4">
                    <!-- Left: Shield Banner (Step 1 only) or Atrás button (Step > 1) -->
                    <div class="flex items-center w-full sm:w-auto">
                        <!-- Shield Banner for Step 1 -->
                        <div id="step1ShieldBanner" class="flex items-center gap-3">
                            <div class="w-9 h-9 rounded-full bg-[#EFF6FF] text-[#5B50F0] flex items-center justify-center shrink-0">
                                <!-- Shield icon -->
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                                </svg>
                            </div>
                            <div>
                                <h5 class="text-xs font-bold text-[#1A1D2E]">Te guiamos paso a paso.</h5>
                                <p class="text-[11px] text-[#5a6275] font-semibold mt-0.5">Si algo parece comercial o requiere permiso, te lo avisaremos.</p>
                            </div>
                        </div>
                        
                        <!-- Atrás Button (hidden on Step 1) -->
                        <button id="btnPrevStep" class="hidden bg-white hover:bg-[#F3F4F6] text-[#374151] font-bold px-6 py-2.5 rounded-xl text-xs uppercase tracking-widest border border-[#e8eaf0] transition-all">
                            Atras
                        </button>
                    </div>

                    <!-- Right: Guardar y salir & Continuar -->
                    <div class="flex items-center gap-6 w-full sm:w-auto justify-end">
                        <button type="button" id="btnSaveAndExit" class="text-xs font-bold text-[#5B50F0] hover:underline bg-transparent border-none cursor-pointer">
                            Guardar y salir
                        </button>
                        <button id="btnNextStep" class="bg-[#5B50F0] hover:bg-[#4a40df] text-white font-extrabold px-6 py-3 rounded-xl text-xs uppercase tracking-widest transition-all shadow-md flex items-center gap-1.5 border-none cursor-pointer">
                            Continuar
                            <!-- Right arrow -->
                            <svg class="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/>
                            </svg>
                        </button>
                        <button id="btnSaveOperationGeneral" class="hidden bg-[#5B50F0] hover:bg-[#4a40df] text-white font-extrabold px-6 py-3 rounded-xl text-xs uppercase tracking-widest transition-all shadow-md flex items-center gap-2 border-none cursor-pointer">
                            <!-- Lock icon -->
                            <svg class="w-4 h-4 text-white" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z" />
                            </svg>
                            Finalizar y guardar expediente
                            <!-- Right arrow -->
                            <svg class="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/>
                            </svg>
                        </button>
                    </div>
                </div>
            </div>

            <!-- Sidebar resumen fijo derecho - OCULTO, IDs manejados por columna del paso 3 -->
            <div class="hidden" aria-hidden="true">
                <span id="sideProd"></span>
                <span id="sidePais"></span>
                <span id="sideHS"></span>
                <span id="sideCIF"></span>
                <span id="sideTaxes"></span>
                <span id="sideVuce"></span>
                <span id="sideEstado"></span>
                <span id="sideNextActionTitle"></span>
                <span id="sideNextActionText"></span>
                <button id="sideNextActionButton"></button>
                
                <!-- Unused Step 4 elements to prevent JS errors -->
                <span id="resProd"></span>
                <span id="resRuc"></span>
                <span id="resHS"></span>
                <span id="resFob"></span>
                <span id="resVuce"></span>
                <span id="resSuce"></span>
                <span id="resFirma"></span>
                <ol id="finalActionPlan"></ol>
                <button id="btnDownloadPDF"></button>
            </div>

        </div>
    </main>

    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        window.ctx = '<%= escapeJs(request.getContextPath()) %>';
        window.userRuc = '<%= escapeJs(userRuc != null ? userRuc : "") %>';
        window.userNombre = '<%= escapeJs(userNombre != null ? userNombre : "") %>';
        window.userId = <%= session.getAttribute("usuarioId") %>;
    </script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/knowledge-base.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/state.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/vuce-assessment.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/costs.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/route.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/checklist.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/storage.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/stepper.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/dom-bindings.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/evaluacion/index.js"></script>

</body>
</html>





