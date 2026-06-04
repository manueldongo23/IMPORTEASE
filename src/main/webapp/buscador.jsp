<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
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
    <style>
        .tribute-bar { transition: width 1s var(--ease-spring); }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-[var(--surface-0)] font-['Outfit'] text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "buscador"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
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
            <div class="fade-up">
                <span class="text-[10px] font-black text-[var(--accent)] uppercase tracking-widest bg-[var(--accent-soft)] px-3 py-1 rounded-full border border-[var(--accent-glow)]">Herramienta de apoyo</span>
                <h2 class="text-4xl font-black text-[var(--text-primary)] tracking-tight mt-3">Codigo de producto</h2>
                <p class="text-[var(--text-secondary)] text-sm font-semibold mt-1">Busca como aduanas reconoce tu producto. Si vienes desde Importar, puedes aplicar el codigo y volver sin perder avance.</p>
            </div>

            <div id="wizardReturnBanner" class="fade-up rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] px-5 py-4 shadow-sm border-l-4 border-l-[var(--accent)]">
                <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">&larr; Volver a Importar paso a paso</a>
            </div>
            
            <!-- Search Bar and Suggestions -->
            <div class="relative fade-up space-y-3" style="animation-delay:0.1s; z-index: 40">
                <div class="flex gap-4">
                    <div class="relative flex-1 group">
                        <div class="absolute inset-y-0 left-6 flex items-center pointer-events-none z-10 text-gray-400 group-focus-within:text-[var(--accent)] transition-colors">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
                        </div>
                        <input type="text" id="hsSearch" autocomplete="off" list="hsSearchOptions"
                               class="w-full pl-16 pr-10 py-5 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-lg font-bold placeholder:text-gray-400 focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all shadow-sm" 
                               placeholder="<%= esEmpresa ? "Ej: aceites, laptops, 8517130000..." : "Ej: perfume, celular, 8517130000..." %>"
                               oninput="onInput(this.value)"
                               onkeydown="onKeyDown(event)">
                        <datalist id="hsSearchOptions"></datalist>
                        
                        <!-- Autocomplete dropdown -->
                        <div id="autocompleteBox" class="hidden absolute top-full left-0 right-0 mt-2 bg-[var(--surface-1)] rounded-2xl border border-[var(--border)] z-50 shadow-xl overflow-hidden animate-fadeUp">
                            <div id="acList" class="divide-y divide-[var(--border)] max-h-80 overflow-y-auto custom-scrollbar"></div>
                        </div>
                    </div>
                    <button onclick="buscar()" id="btnBuscar" class="px-8 py-5 text-sm font-black rounded-2xl bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white transition-all duration-300 hover:scale-[1.01] active:scale-[0.99] shadow-lg flex items-center gap-2 shrink-0">
                        <span>Buscar codigo</span>
                    </button>
                </div>
                
                <!-- Helper tag recommendations depending on user profile -->
                <div class="flex flex-wrap items-center gap-2 text-xs text-[var(--text-secondary)] font-semibold px-2">
                    <span>Sugerencias populares:</span>
                    <% if (esEmpresa) { %>
                        <button onclick="document.getElementById('hsSearch').value='laptop'; onInput('laptop');" class="text-[var(--accent)] font-bold hover:underline">Equipos Informáticos</button> ·
                        <button onclick="document.getElementById('hsSearch').value='aceite'; onInput('aceite');" class="text-[var(--accent)] font-bold hover:underline">Aceites y Lubricantes</button> ·
                        <button onclick="document.getElementById('hsSearch').value='prendas'; onInput('prendas');" class="text-[var(--accent)] font-bold hover:underline">Prendas de Vestir</button>
                    <% } else { %>
                        <button onclick="document.getElementById('hsSearch').value='perfume'; onInput('perfume');" class="text-[var(--accent)] font-bold hover:underline">Perfumes</button> ·
                        <button onclick="document.getElementById('hsSearch').value='celular'; onInput('celular');" class="text-[var(--accent)] font-bold hover:underline">Smartphones</button> ·
                        <button onclick="document.getElementById('hsSearch').value='leche'; onInput('leche');" class="text-[var(--accent)] font-bold hover:underline">Productos Lácteos</button>
                    <% } %>
                </div>
            </div>

            <!-- Results Grid -->
            <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
                
                <!-- Loading Spinner -->
                <div id="loadingSkeleton" class="hidden lg:col-span-2 flex items-center justify-center p-12">
                    <div class="w-8 h-8 border-2 border-[var(--accent)] border-t-transparent rounded-full animate-spin"></div>
                    <span class="ml-3 text-xs font-bold text-[var(--text-secondary)]">Buscando...</span>
                </div>

                <!-- Main Info -->
                <div id="mainResults" class="lg:col-span-2 bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 space-y-8 shadow-sm fade-up" style="animation-delay:0.2s">
                    <div class="flex flex-col sm:flex-row sm:items-center justify-between border-b border-[var(--border)] pb-6 gap-4">
                        <div class="flex items-center gap-3">
                            <div class="w-10 h-10 rounded-xl bg-[var(--accent-soft)] flex items-center justify-center text-[var(--accent)] border border-[var(--accent-glow)] shrink-0">
                                <svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
                                </svg>
                            </div>
                            <div>
                                <h3 class="font-black uppercase tracking-wider text-xs text-[var(--accent)]">Codigo encontrado</h3>
                                <p class="text-[10px] text-[var(--text-tertiary)] font-semibold mt-0.5">Identificacion usada para permisos e impuestos</p>
                                <div class="flex flex-wrap items-center gap-2 mt-2">
                                    <span id="hsSearchSourceChip" class="source-chip source-chip--pending">PENDIENTE VALIDACIÓN</span>
                                    <span id="hsSearchConfidenceText" class="text-[10px] text-[var(--text-tertiary)] font-bold">Fuente por confirmar</span>
                                </div>
                            </div>
                        </div>
                        <div class="flex flex-wrap items-center gap-2">
                            <button id="btnUsarEnImportacion" onclick="usarCodigoEnImportacion()" class="hidden px-4 py-2.5 rounded-xl bg-[var(--accent)] text-white text-[10px] font-black uppercase tracking-widest hover:bg-[var(--accent-hover)] transition-all shadow-md">
                                Usar en Importar
                            </button>
                            <button id="btnVerMercado" onclick="verMercadoHs()" class="hidden px-4 py-2.5 rounded-xl bg-[var(--surface-1)] text-[10px] font-black text-[var(--accent)] uppercase tracking-widest border border-[var(--border)] hover:bg-[var(--surface-2)] transition-all">
                                Ver mercado
                            </button>
                            <button onclick="copiarCodigo()" class="px-4 py-2.5 rounded-xl bg-[var(--surface-0)] text-[10px] font-black text-[var(--text-secondary)] hover:text-[var(--accent)] uppercase tracking-widest border border-[var(--border)] hover:bg-[var(--surface-2)] transition-all flex items-center gap-2">
                                Copiar codigo
                            </button>
                        </div>
                    </div>

                    <!-- Digit Blocks -->
                    <div class="flex flex-col items-center justify-center space-y-2 py-6 bg-[var(--surface-0)] rounded-2xl border border-[var(--border)]">
                        <div class="hs-digit-container justify-center font-mono font-black text-2xl flex items-center gap-1">
                            <div id="digit1" class="hs-digit-block bg-[var(--surface-1)] px-3 py-2 rounded-lg border border-[var(--border)] text-[var(--text-primary)]">——</div>
                            <div class="digit-dot text-[var(--text-tertiary)] font-bold text-xl">.</div>
                            <div id="digit2" class="hs-digit-block bg-[var(--surface-1)] px-3 py-2 rounded-lg border border-[var(--border)] text-[var(--text-primary)]">——</div>
                            <div class="digit-dot text-[var(--text-tertiary)] font-bold text-xl">.</div>
                            <div id="digit3" class="hs-digit-block bg-[var(--surface-1)] px-3 py-2 rounded-lg border border-[var(--border)] text-[var(--text-primary)]">——</div>
                            <div class="digit-dot text-[var(--text-tertiary)] font-bold text-xl">.</div>
                            <div id="digit4" class="hs-digit-block bg-[var(--surface-1)] px-4 py-2 rounded-lg border border-[var(--border)] text-[var(--text-primary)]">————</div>
                        </div>
                        <div class="flex flex-wrap justify-center gap-6 text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-widest pt-2 text-center">
                            <span>Capítulo</span>
                            <span>Grupo</span>
                            <span>Detalle</span>
                            <span>Codigo nacional</span>
                        </div>
                    </div>

                    <!-- Description -->
                    <div class="p-6 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] relative overflow-hidden">
                        <div class="absolute top-0 left-0 w-1.5 h-full bg-[var(--accent)]"></div>
                        <div id="hsDescripcion" class="text-base text-[var(--text-secondary)] font-semibold leading-relaxed italic transition-all duration-500">
                            Describe tu producto o ingresa un codigo para ver la ficha referencial...
                        </div>
                        <p class="text-[9px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] mt-6">Descripcion referencial para revisar</p>
                    </div>

                    <!-- Product Info Panel -->
                    <div id="productInfoPanel" class="hidden p-6 rounded-2xl bg-[var(--surface-0)] border border-[var(--border)] space-y-4">
                        <div class="flex items-center gap-3 mb-2">
                            <div class="w-8 h-8 rounded-lg bg-[var(--accent-soft)] flex items-center justify-center text-[var(--accent)] border border-[var(--accent-glow)] shrink-0">
                                <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 111.063.852l-.708 2.836a.75.75 0 001.063.852l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z"/></svg>
                            </div>
                            <h4 class="text-xs font-black uppercase tracking-[0.2em] text-[var(--accent)]">Informacion util para decidir</h4>
                        </div>
                        <div id="productInfoContent" class="space-y-3 font-semibold text-xs text-[var(--text-secondary)] leading-relaxed"></div>
                        <div id="productTip" class="mt-4 p-4 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-xs text-[var(--accent-deep)] font-semibold hidden"></div>
                    </div>

                    <!-- VUCE Badge -->
                    <div id="vuceBadge" class="hidden p-6 rounded-2xl flex items-center gap-5 transition-all duration-500 border border-[var(--border)] bg-[var(--surface-0)]">
                        <div id="vuceBadgeIconBox" class="w-12 h-12 rounded-xl flex items-center justify-center text-xl shrink-0 border border-current/10"></div>
                        <div class="flex-1">
                            <p id="vuceBadgeTitle" class="text-xs font-black uppercase tracking-widest text-[var(--text-primary)]"></p>
                            <p id="vuceBadgeDesc" class="text-[11px] text-[var(--text-secondary)] mt-0.5 font-medium leading-relaxed"></p>
                        </div>
                        <a id="vuceBadgeLink" href="#" target="_blank" class="px-5 py-2.5 rounded-xl bg-[var(--surface-1)] text-[10px] font-black uppercase text-[var(--accent)] hover:bg-[var(--surface-2)] transition-all border border-[var(--border)] shrink-0">
                            Ver portal oficial
                        </a>
                    </div>
                </div>

                <!-- Side Panels -->
                <div class="space-y-8 fade-up" style="animation-delay: 0.3s">
                    <!-- Tributos -->
                    <div class="bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 space-y-6 shadow-sm">
                        <div class="flex items-center gap-3 border-b border-[var(--border)] pb-4">
                            <div class="w-9 h-9 rounded-xl bg-[var(--accent-soft)] flex items-center justify-center text-[var(--accent)] border border-[var(--accent-glow)] shrink-0">
                                <svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.22.022m0 0L15.5 12.5m-6.5 1.682l-.22-.022m0 0L5.5 11.5M10.125 18H15m-7.5-6H15m-7.5-6H12"/></svg>
                            </div>
                            <div>
                                <h3 class="font-black uppercase tracking-wider text-xs text-[var(--text-secondary)]">Impuestos aproximados</h3>
                                <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Montos referenciales para estimar costo</p>
                            </div>
                        </div>
                        
                        <div class="space-y-6 font-semibold text-xs text-[var(--text-secondary)]">
                            <div>
                                <div class="flex justify-between mb-2 items-end">
                                    <span class="text-[10px] text-[var(--text-tertiary)] font-black uppercase tracking-widest">Impuesto base</span>
                                    <span id="tribAV" class="text-xl font-black text-[var(--text-primary)] leading-none">0%</span>
                                </div>
                                <div class="h-2 bg-[var(--surface-0)] rounded-full overflow-hidden border border-[var(--border)]">
                                    <div id="barAV" class="tribute-bar h-full bg-[var(--accent)] rounded-full" style="width:0%"></div>
                                </div>
                            </div>
                            <div>
                                <div class="flex justify-between mb-2 items-end">
                                    <span class="text-[10px] text-[var(--text-tertiary)] font-black uppercase tracking-widest">IGV / IPM</span>
                                    <span id="tribIGV" class="text-xl font-black text-[var(--text-primary)] leading-none">18%</span>
                                </div>
                                <div class="h-2 bg-[var(--surface-0)] rounded-full overflow-hidden border border-[var(--border)]">
                                    <div id="barIGV" class="tribute-bar h-full bg-[var(--accent)]/80 rounded-full" style="width:0%"></div>
                                </div>
                            </div>
                            <div>
                                <div class="flex justify-between mb-2 items-end">
                                    <span class="text-[10px] text-[var(--text-tertiary)] font-black uppercase tracking-widest">ISC (Consumo)</span>
                                    <span id="tribISC" class="text-xl font-black text-[var(--text-primary)] leading-none">0%</span>
                                </div>
                                <div class="h-2 bg-[var(--surface-0)] rounded-full overflow-hidden border border-[var(--border)]">
                                    <div id="barISC" class="tribute-bar h-full bg-rose-500 rounded-full" style="width:0%"></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Mentor IA Advice -->
                    <div class="bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 space-y-4 relative overflow-hidden shadow-sm bg-[radial-gradient(circle_at_30%_30%,rgba(59,130,246,0.015),transparent_50%)]">
                        <div class="flex items-center gap-3">
                            <span class="text-xl shrink-0">💡</span>
                            <div>
                                <h3 class="text-xs font-black uppercase tracking-wider text-[var(--accent)]">Ayuda rapida</h3>
                                <p class="text-[9px] text-[var(--text-tertiary)] font-semibold mt-0.5">Asistente didáctico para <%= com.importease.proyecto.service.HtmlUtil.escape(userNombre) %></p>
                            </div>
                        </div>
                        <p id="mentorConsejo" class="text-xs text-[var(--text-secondary)] leading-relaxed font-semibold transition-all duration-500">
                            Escribe el producto como lo conoces. Te ayudaremos a entender codigo, permisos e impuestos sin jerga.
                        </p>
                        <a id="mentorLink" href="https://www.sunat.gob.pe/orientacionaduanera/tlc/" target="_blank"
                           class="hidden inline-block text-center py-2.5 px-4 rounded-xl bg-[var(--surface-1)] border border-[var(--border)] text-[9px] font-black text-[var(--accent)] uppercase tracking-widest hover:bg-[var(--surface-2)] transition-all w-full">
                            Ver acuerdos comerciales
                        </a>
                    </div>
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

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
    </script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/buscador.js"></script>
</body>
</html>
