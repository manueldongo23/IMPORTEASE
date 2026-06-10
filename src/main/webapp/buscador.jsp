<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
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
    String userNombre = (String) session.getAttribute("usuarioNombre");
    if (userNombre == null) userNombre = "Manuel";
    String userRuc = (String) session.getAttribute("usuarioRuc");
    boolean esEmpresa = userRuc != null && userRuc.startsWith("20");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Codigo de producto</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
<link href="css/hs-buscador.css" rel="stylesheet">
</head>
<body class="flex h-screen overflow-hidden bg-[var(--surface-0)] font-['Outfit'] text-[var(--text-primary)]">
<%@ include file="/WEB-INF/fragments/consent-banner.jsp" %>
    <% request.setAttribute("activePage", "buscador"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-y-auto hs-buscador">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-[var(--surface-1)]/80 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Buscador de codigo de producto</span>
            </div>
            <div class="flex items-center gap-6">
                <div class="text-[10px] uppercase tracking-widest font-black text-[var(--text-tertiary)] flex gap-6">
                    <span class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Fuentes activas</span>
                    <span class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-emerald-500"></span> Reglas de permisos</span>
                </div>
            </div>
        </header>

    <div class="p-8 lg:p-12 max-w-7xl mx-auto space-y-10 w-full flex-1">
        <!-- Hero Section -->
        <div class="hs-hero-container fade-up">
            <div>
                <span class="text-[10px] font-black text-indigo-600 uppercase tracking-widest bg-indigo-50 px-3 py-1.5 rounded-full border border-indigo-200/50">Herramienta de apoyo</span>
                <h2 class="text-4.5xl font-black text-[var(--text-primary)] tracking-tight mt-3">
                    Encuentra el código arancelario <br>
                    <span class="text-indigo-600">de tu producto</span>
                </h2>
                <p class="text-[var(--text-secondary)] text-sm font-semibold mt-2.5 max-w-md leading-relaxed">
                    Este código te ayuda a calcular impuestos, permisos y documentos necesarios para la importación.
                </p>
                <div class="mt-6">
                    <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-indigo-600 hover:text-indigo-700 hover:underline uppercase tracking-widest">
                        &larr; Volver a importar paso a paso
                    </a>
                </div>
            </div>
            <div class="hs-hero-illustration-box flex justify-end">
                <img src="css/buscador_hero_illustration.png" alt="Illustration" class="w-full max-w-[340px]">
            </div>
        </div>

        <!-- Search Bar and Suggestions -->
        <div class="hs-search-card fade-up space-y-5" style="animation-delay:0.1s; z-index: 40">
            <div class="flex gap-4">
                <div class="relative flex-1 group">
                    <div class="hs-search-input-wrapper">
                        <div class="absolute left-5 inset-y-0 flex items-center pointer-events-none text-slate-500">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                        </div>
                        <input type="text" id="hsSearch" autocomplete="off" list="hsSearchOptions"
                               class="hs-search-input w-full pl-14 pr-6 py-4 bg-transparent border-none outline-none text-white text-base font-bold"
                               placeholder="<%= esEmpresa ? "Ej: aceite de pie de buey, laptop, 8517130000..." : "Ej: perfume, celular, 8517130000..." %>">
                        <datalist id="hsSearchOptions"></datalist>
                    </div>
                    
                    <!-- Autocomplete dropdown -->
                    <div id="autocompleteBox" class="hidden absolute top-full left-0 right-0 mt-2 bg-white rounded-2xl border border-slate-200 z-50 shadow-xl overflow-hidden animate-fadeUp">
                        <div id="acList" class="divide-y divide-slate-100 max-h-80 overflow-y-auto custom-scrollbar"></div>
                    </div>
                </div>
                <button id="btnBuscar" class="hs-search-btn px-8 py-4 flex items-center gap-2 shrink-0">
                    <span>Buscar código</span>
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                </button>
            </div>
            
            <p class="hs-search-subtext">Puedes buscar por nombre del producto o código arancelario</p>
            
            <!-- Helper tag recommendations -->
            <div class="flex flex-wrap items-center gap-2.5 text-xs font-semibold">
                <span class="text-slate-500 font-bold">Búsquedas populares:</span>
                <button type="button" class="hs-popular-tag" data-tag="aceite">Aceites y Lubricantes</button>
                <button type="button" class="hs-popular-tag" data-tag="laptop">Equipos Informáticos</button>
                <button type="button" class="hs-popular-tag" data-tag="prendas">Prendas de Vestir</button>
                <button type="button" class="hs-popular-tag" data-tag="calzado">Calzado</button>
                <button type="button" class="hs-popular-tag" data-tag="celular">Electrónicos</button>
            </div>
        </div>

        <!-- Loading Spinner -->
        <div id="loadingSkeleton" class="hidden flex items-center justify-center p-12">
            <div class="w-8 h-8 border-2 border-indigo-600 border-t-transparent rounded-full animate-spin"></div>
            <span class="ml-3 text-xs font-bold text-slate-500">Buscando coincidencia y permisos...</span>
        </div>

        <!-- Results Section -->
        <div id="mainResults" class="hidden space-y-8 fade-up" style="animation-delay:0.2s">
            <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                
                <!-- COLUMNA IZQUIERDA -->
                <div class="space-y-8">
                    
                    <!-- CARD 1: CÓDIGO ENCONTRADO -->
                    <div class="bg-white border border-slate-200 rounded-3xl p-8 space-y-6 shadow-sm">
                        <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-slate-100 pb-5 gap-4">
                            <div class="flex items-center gap-2.5">
                                <div class="w-5 h-5 rounded-full bg-emerald-500 flex items-center justify-center text-white shrink-0">
                                    <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/></svg>
                                </div>
                                <h3 class="font-black uppercase tracking-wider text-xs text-slate-800">Código encontrado</h3>
                            </div>
                            
                            <div class="flex items-center gap-2">
                                <span class="text-[10px] text-slate-500 font-semibold">Última verificación: Hoy, 10:35 a.m.</span>
                                <span class="px-2 py-0.5 rounded-full text-[9px] font-bold bg-emerald-50 text-emerald-600 border border-emerald-200/50">Fuente confirmada</span>
                            </div>
                        </div>
                        
                        <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6">
                            <!-- 5 Digit Blocks -->
                            <div>
                                <p class="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-3">Código Nacional</p>
                                <div class="hs-digit-container">
                                    <div class="hs-digit-segment">
                                        <div id="digitCapitulo" class="hs-digit-box">--</div>
                                        <span class="hs-digit-label mt-1.5">Capítulo</span>
                                    </div>
                                    <div class="text-slate-300 font-bold text-lg leading-none select-none">.</div>
                                    <div class="hs-digit-segment">
                                        <div id="digitPartida" class="hs-digit-box">--</div>
                                        <span class="hs-digit-label mt-1.5">Partida</span>
                                    </div>
                                    <div class="text-slate-300 font-bold text-lg leading-none select-none">.</div>
                                    <div class="hs-digit-segment">
                                        <div id="digitSubpartida" class="hs-digit-box">--</div>
                                        <span class="hs-digit-label mt-1.5">Subpartida</span>
                                    </div>
                                    <div class="text-slate-300 font-bold text-lg leading-none select-none">.</div>
                                    <div class="hs-digit-segment">
                                        <div id="digitItem" class="hs-digit-box">--</div>
                                        <span class="hs-digit-label mt-1.5">Item</span>
                                    </div>
                                    <div class="text-slate-300 font-bold text-lg leading-none select-none">.</div>
                                    <div class="hs-digit-segment">
                                        <div id="digitSubitem" class="hs-digit-box">--</div>
                                        <span class="hs-digit-label mt-1.5">Subitem</span>
                                    </div>
                                </div>
                            </div>
                            
                            <!-- Status and Copy -->
                            <div class="space-y-4 flex-1">
                                <div class="flex items-start gap-2.5">
                                    <div class="w-5 h-5 rounded-full bg-indigo-50 border border-indigo-100 flex items-center justify-center text-indigo-500 shrink-0 mt-0.5">
                                        <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                                    </div>
                                    <div>
                                        <p class="text-[10px] text-slate-500 font-semibold leading-relaxed">Identificación usada para permisos e impuestos</p>
                                        <div class="flex items-center gap-1.5 mt-1.5">
                                            <span id="hsSearchSourceChip" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-amber-50 text-amber-600 border border-amber-200">PENDIENTE VALIDACIÓN</span>
                                            <span id="hsSearchConfidenceText" class="text-[9px] text-slate-400 font-bold">Fuente por confirmar</span>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="flex flex-wrap gap-2">
                                    <button id="btnUsarEnImportacion" class="hidden px-4 py-2.5 rounded-xl bg-indigo-600 text-white text-[10px] font-black uppercase tracking-widest hover:bg-indigo-700 transition-all shadow-md">Usar este código</button>
                                    <button id="btnVerMercado" class="hidden px-4 py-2.5 rounded-xl bg-white text-[10px] font-black text-indigo-600 uppercase tracking-widest border border-slate-200 hover:bg-slate-50 transition-all">Ver mercado</button>
                                    <button id="btnCopiarCodigo" class="px-4 py-2.5 rounded-xl bg-white text-[10px] font-black text-slate-600 hover:text-indigo-600 uppercase tracking-widest border border-slate-200 hover:bg-slate-50 transition-all flex items-center gap-1.5">
                                        <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8 5H6a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2v-1M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 002 2h2a2 2 0 002-2M8 5a2 2 0 012-2h2a2 2 0 012 2m0 0h2a2 2 0 012 2v3m2 4H10m0 0l3-3m-3 3l3 3"/></svg>
                                        <span>Copiar código</span>
                                    </button>
                                </div>
                            </div>
                        </div>
                        
                        <div class="p-5 rounded-2xl bg-slate-50 border border-slate-100 relative overflow-hidden">
                            <div class="absolute top-0 left-0 w-1.5 h-full bg-indigo-600"></div>
                            <p class="text-[9px] font-black text-slate-400 uppercase tracking-widest mb-1.5">Descripción del producto</p>
                            <h4 id="hsDescripcion" class="text-base text-slate-800 font-extrabold transition-all duration-300">
                                -
                            </h4>
                            <p class="text-[9px] font-black text-slate-400 uppercase tracking-[0.2em] mt-3">Descripción referencial para revisar</p>
                        </div>
                    </div>
                    
                    <!-- CARD 2: INFORMACIÓN ÚTIL PARA DECIDIR -->
                    <div id="productInfoPanel" class="bg-white border border-slate-200 rounded-3xl p-8 space-y-6 shadow-sm">
                        <div class="flex items-center gap-2.5 border-b border-slate-100 pb-4">
                            <div class="w-5 h-5 rounded-full bg-blue-50 border border-blue-100 flex items-center justify-center text-blue-500 shrink-0">
                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 111.063.852l-.708 2.836a.75.75 0 001.063.852l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z"/></svg>
                            </div>
                            <h3 class="font-black uppercase tracking-wider text-xs text-slate-800">Información útil para decidir</h3>
                        </div>
                        
                        <div class="flex items-center justify-between gap-6">
                            <!-- Specs Table -->
                            <div class="flex-1">
                                <table class="hs-info-table">
                                    <tbody id="productInfoContent">
                                        <!-- Llenado dinámico -->
                                    </tbody>
                                </table>
                            </div>
                            
                            <!-- Checklist Illustration SVG -->
                            <div class="hs-checklist-illustration">
                                <svg viewBox="0 0 100 100" class="w-full h-full">
                                    <rect x="25" y="15" width="50" height="70" rx="6" fill="#F8FAFC" stroke="#CBD5E1" stroke-width="1.5"/>
                                    <rect x="38" y="9" width="24" height="8" rx="2" fill="#64748B"/>
                                    <circle cx="38" cy="35" r="4.5" fill="#10B981"/>
                                    <path d="M35.5 35l1.5 1.5 3-3" stroke="white" stroke-width="1.25" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                                    <line x1="48" y1="35" x2="68" y2="35" stroke="#94A3B8" stroke-width="3.5" stroke-linecap="round"/>
                                    <circle cx="38" cy="52" r="4.5" fill="#10B981"/>
                                    <path d="M35.5 52l1.5 1.5 3-3" stroke="white" stroke-width="1.25" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                                    <line x1="48" y1="52" x2="68" y2="52" stroke="#94A3B8" stroke-width="3.5" stroke-linecap="round"/>
                                    <circle cx="38" cy="69" r="4.5" fill="#10B981"/>
                                    <path d="M35.5 69l1.5 1.5 3-3" stroke="white" stroke-width="1.25" stroke-linecap="round" stroke-linejoin="round" fill="none"/>
                                    <line x1="48" y1="69" x2="62" y2="69" stroke="#94A3B8" stroke-width="3.5" stroke-linecap="round"/>
                                </svg>
                            </div>
                        </div>
                        <div id="productTip" class="p-4 rounded-xl bg-indigo-50 border border-indigo-100 text-xs text-indigo-900 font-semibold hidden"></div>
                    </div>
                    
                </div>
                
                <!-- COLUMNA DERECHA -->
                <div class="space-y-8">
                    
                    <!-- CARD 3: IMPUESTOS APROXIMADOS -->
                    <div class="bg-white border border-slate-200 rounded-3xl p-8 space-y-6 shadow-sm">
                        <div class="flex items-center gap-2.5 border-b border-slate-100 pb-4">
                            <div class="w-5 h-5 rounded-full bg-violet-50 border border-violet-100 flex items-center justify-center text-violet-500 shrink-0">
                                <svg class="w-3 h-3" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.25 18L9 11.25l4.306 4.307a11.95 11.95 0 015.814-5.519l2.74-1.22m0 0l-5.94-2.28m5.94 2.28l-2.28 5.941"/></svg>
                            </div>
                            <div>
                                <h3 class="font-black uppercase tracking-wider text-xs text-slate-800">Impuestos aproximados</h3>
                                <p class="text-[9px] text-slate-400 font-semibold">Montos referenciales para estimar costo</p>
                            </div>
                        </div>
                        
                        <div class="space-y-5">
                            <div>
                                <div class="flex justify-between items-end text-xs font-bold text-slate-700">
                                    <span class="text-[10px] text-slate-400 font-black uppercase tracking-wider">Impuesto Base</span>
                                    <span id="tribAV" class="text-base font-extrabold text-slate-900">0%</span>
                                </div>
                                <div class="hs-tax-progress-bar">
                                    <div id="barAV" class="hs-tax-progress-fill av" style="width:0%"></div>
                                </div>
                            </div>
                            
                            <div>
                                <div class="flex justify-between items-end text-xs font-bold text-slate-700">
                                    <span class="text-[10px] text-slate-400 font-black uppercase tracking-wider">IGV / IPM</span>
                                    <span id="tribIGV" class="text-base font-extrabold text-slate-900">18%</span>
                                </div>
                                <div class="hs-tax-progress-bar">
                                    <div id="barIGV" class="hs-tax-progress-fill igv" style="width:0%"></div>
                                </div>
                            </div>
                            
                            <div>
                                <div class="flex justify-between items-end text-xs font-bold text-slate-700">
                                    <span class="text-[10px] text-slate-400 font-black uppercase tracking-wider">ISC (Consumo)</span>
                                    <span id="tribISC" class="text-base font-extrabold text-slate-900">0%</span>
                                </div>
                                <div class="hs-tax-progress-bar">
                                    <div id="barISC" class="hs-tax-progress-fill isc" style="width:0%"></div>
                                </div>
                            </div>
                        </div>
                        
                        <div class="border-t border-slate-100 pt-4 text-center">
                            <a href="evaluacion.jsp" class="text-xs font-extrabold text-indigo-600 hover:text-indigo-700 flex items-center justify-center gap-1.5">
                                <span>Ver cálculo detallado</span>
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/></svg>
                            </a>
                        </div>
                    </div>
                    
                    <!-- CARD 4: AYUDA RÁPIDA -->
                    <div class="bg-white border border-slate-200 rounded-3xl p-8 space-y-5 shadow-sm">
                        <div class="flex items-center gap-2.5">
                            <div class="w-5 h-5 rounded-full bg-yellow-50 border border-yellow-100 flex items-center justify-center text-yellow-500 shrink-0">
                                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 18v-3H9.75a.75.75 0 01-.53-1.28l4.5-4.5a.75.75 0 011.28.53V12h2.25a.75.75 0 01.53 1.28l-4.5 4.5a.75.75 0 01-1.28-.53zM12 2.25a9.75 9.75 0 109.75 9.75A9.76 9.76 0 0012 2.25z"/></svg>
                            </div>
                            <div>
                                <h3 class="font-black uppercase tracking-wider text-xs text-slate-800">Ayuda rápida</h3>
                                <p class="text-[9px] text-slate-400 font-semibold">Asistente didáctico para <%= com.importease.proyecto.service.HtmlUtil.escape(userNombre) %></p>
                            </div>
                        </div>
                        
                        <p id="mentorConsejo" class="text-xs text-slate-600 font-semibold leading-relaxed">
                            Describe tu producto. Te ayudaremos a interpretar códigos arancelarios, calcular impuestos y entender permisos paso a paso sin términos complejos.
                        </p>
                        
                        <button id="btnMentorLink" class="w-full py-2.5 px-4 rounded-xl border border-slate-200 hover:bg-slate-50 text-[10px] font-black text-indigo-600 uppercase tracking-widest flex items-center justify-center gap-2 transition-all">
                            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 21a9.004 9.004 0 008.716-6.747M12 21a9.004 9.004 0 01-8.716-6.747M12 21c2.485 0 4.5-4.03 4.5-9S14.485 3 12 3m0 18c-2.485 0-4.5-4.03-4.5-9S9.515 3 12 3m0 0a8.997 8.997 0 017.843 4.582M12 3a8.997 8.997 0 00-7.843 4.582m15.686 0A11.953 11.953 0 0112 10.5c-2.905 0-5.64-.506-8.157-1.418m16.314 0C19.645 11.754 16 14.25 12 14.25c-4 0-7.645-2.496-8.157-5.168"/></svg>
                            <span>Ver acuerdos comerciales</span>
                        </button>
                    </div>
                    
                </div>
                
            </div>
            
            <!-- HORIZONTAL ALERTA VUCE BANNER -->
            <div id="vuceBadge" class="hs-alert-banner warning">
                <div class="flex items-center gap-3">
                    <div class="w-6 h-6 rounded-full bg-[#D97706] text-white flex items-center justify-center text-xs shrink-0 font-black">!</div>
                    <p class="text-xs font-black">
                        <span id="vuceBadgeTitle">REVISA PERMISO CON DIGESA.</span>
                        <span id="vuceBadgeDesc" class="font-semibold text-[11px] opacity-90 ml-1">Antes de comprar o embarcar, confirma si necesitas autorización.</span>
                    </p>
                </div>
                <button id="vuceBadgeLink" class="hs-alert-banner-btn flex items-center gap-1.5">
                    <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8.25M15.75 21v-8.25M8.25 21v-8.25M3 9l9-6 9 6m-1.5 12V10.33l-7.5-5-7.5 5V21M3 21h18"/></svg>
                    <span>Ver entidad</span>
                </button>
            </div>
        </div>
    </div>
    </main>

    <!-- AduanaBot Floating Chat widget (Proposal 10) -->
    <div id="aduanabot-trigger" class="fixed bottom-6 right-6 w-14 h-14 bg-gradient-to-tr from-blue-600 to-blue-500 text-white rounded-full flex items-center justify-center shadow-lg hover:scale-105 active:scale-95 cursor-pointer z-[2000] border-2 border-white/20 transition-all">
        <svg class="w-6 h-6 animate-pulse" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M8.684 10.742h.008v.008h-.008v-.008zM12 10.742h.008v.008H12v-.008zM15.316 10.742h.008v.008h-.008v-.008zM12 21a9.003 9.003 0 008.316-12.56C19.789 6.18 16.182 3 12 3S4.211 6.18 3.684 8.44A9.003 9.003 0 0012 21z"/></svg>
    </div>

    <div id="aduanabot-panel" class="hidden fixed bottom-24 right-6 w-96 max-w-[90vw] h-[520px] max-h-[75vh] bg-white/95 backdrop-blur-xl border border-[var(--border)] rounded-3xl shadow-2xl z-[2000] flex flex-col overflow-hidden transition-all duration-300">
        <!-- Chat Header -->
        <div class="px-6 py-4 bg-gradient-to-r from-blue-600 to-blue-500 text-white flex items-center justify-between shrink-0">
            <div class="flex items-center gap-3">
                <span class="text-2xl">🤖</span>
                <div>
                    <h4 class="font-black text-sm tracking-tight">AduanaBot</h4>
                    <p class="text-[9px] text-blue-100 font-bold uppercase tracking-wider">Te explica en simple</p>
                </div>
            </div>
            <button id="aduanabot-close" class="text-white/80 hover:text-white hover:scale-115 transition-transform text-lg font-black">&times;</button>
        </div>

        <!-- Chat Messages -->
        <div id="aduanabot-messages" class="flex-1 p-5 overflow-y-auto custom-scrollbar space-y-4 text-xs font-semibold bg-gray-50/50">
            <div class="flex gap-2">
                <div class="w-7 h-7 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center text-xs shrink-0 font-bold">🤖</div>
                <div class="bg-white px-4 py-3 rounded-2xl border border-[var(--border)] text-[var(--text-secondary)] shadow-sm max-w-[80%] leading-relaxed animate-scaleIn">
                    Hola. Escribeme que producto deseas importar en lenguaje natural, por ejemplo "laptops Lenovo para oficina" o "cosmeticos de uva". Te ayudare a encontrar un codigo probable, impuestos y permisos posibles.
                </div>
            </div>
        </div>

        <!-- Chat Input Form -->
        <form id="aduanabot-form" class="p-4 border-t border-[var(--border)] bg-white flex gap-2 items-center shrink-0">
            <input id="aduanabot-input" type="text" placeholder="Escribe tu producto..." class="flex-1 px-4 py-2.5 rounded-xl border border-[var(--border)] bg-gray-50 text-xs font-semibold focus:outline-none focus:border-blue-500 focus:bg-white transition-all" autocomplete="off">
            <button type="submit" class="w-9 h-9 bg-blue-600 hover:bg-blue-700 text-white rounded-xl flex items-center justify-center shadow-md hover:scale-105 active:scale-95 transition-all shrink-0">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M6 12L3.269 3.126A59.768 59.768 0 0121.485 12 59.77 59.77 0 013.27 20.876L5.999 12zm0 0h7.5"/></svg>
            </button>
        </form>
    </div>

    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
  window.ImportEase = window.ImportEase || {};
  window.ImportEase.ctx = '<%= escapeJs(request.getContextPath()) %>';
  window.ImportEase.csrfToken = '<%= escapeJs(request.getAttribute("csrfToken") != null ? String.valueOf(request.getAttribute("csrfToken")) : "") %>';
  window.ImportEase.csrfHeader = '<%= escapeJs(request.getAttribute("csrfHeader") != null ? String.valueOf(request.getAttribute("csrfHeader")) : "X-CSRF-TOKEN") %>';
  // Compatibilidad legacy
  window.ctx = window.ImportEase.ctx;
  window.csrfToken = window.ImportEase.csrfToken;
</script>
<script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/hs-buscador.js"></script>
</body>
</html>
