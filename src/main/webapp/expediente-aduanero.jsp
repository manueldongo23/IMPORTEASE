<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Revision final</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <style>
        .aduana-tab.is-active { background: var(--accent); color: white !important; border-color: transparent; }
        .regimen-choice.is-active { border-color: var(--accent); background: var(--accent-soft); }
        .step-dot.is-ready { background: var(--accent); color: white; border-color: transparent; }
        .timeline-line { position: relative; }
        .timeline-line::before { content:""; position:absolute; left:1rem; top:1.75rem; bottom:1rem; width:2px; background: color-mix(in srgb, var(--accent) 22%, var(--border)); }
        .timeline-item { position: relative; padding-left: 3rem; }
        .timeline-item::before { content:""; position:absolute; left:0.62rem; top:0.4rem; width:0.8rem; height:0.8rem; border-radius:999px; background: var(--accent); box-shadow:0 0 0 6px color-mix(in srgb, var(--accent) 13%, transparent); }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "aduanas"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar">
        <header class="h-16 border-b border-[var(--border)] px-8 flex items-center justify-between bg-white/70 backdrop-blur-xl sticky top-0 z-10">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Revision final guiada</span>
            </div>
            <div class="flex items-center gap-3">
                <button id="btnMentorToggle" class="soft-button text-xs flex items-center gap-2">
                    <span>?</span> Ayuda simple
                </button>
                <select id="operacionSelect" class="custom-input px-4 py-2.5 rounded-2xl bg-white border border-[var(--border)] text-xs font-black text-[var(--text-secondary)] min-w-[18rem]">
                    <option value="">Cargando operaciones...</option>
                </select>
                <button id="btnGenerar" class="primary-button text-xs">Preparar revision</button>
            </div>
        </header>

        <section class="p-8 xl:p-10 max-w-7xl mx-auto space-y-7">
            <section class="grid grid-cols-1 xl:grid-cols-[1fr_22rem] gap-6">
                <div class="glass-card hero-banner p-8">
                    <p class="pill-heading">Revision antes de avanzar</p>
                    <h1 class="text-4xl font-black tracking-tight mt-3">Confirma que tu importacion esta ordenada</h1>
                    <p class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-3xl">
                        El sistema revisa destino del producto, documentos, costos, plazos y riesgos. Cuando aparezca un termino tecnico, lo mostramos como referencia y te decimos que accion tomar.
                    </p>
                    <div id="stepRail" class="grid grid-cols-2 md:grid-cols-5 xl:grid-cols-10 gap-2 mt-7"></div>
                </div>

                <aside class="glass-card section-shell p-6 bg-[var(--accent-soft)]">
                    <p class="pill-heading">Estado de la carpeta</p>
                    <h2 class="text-3xl font-black mt-4 text-[var(--accent)]"><span id="panelCompletitud">0</span>% <span class="text-sm text-gray-500 font-bold uppercase tracking-widest">Completitud</span></h2>
                    
                    <div class="grid grid-cols-2 gap-3 mt-5 text-xs font-semibold text-[var(--text-secondary)]">
                        <div class="bg-white p-3 rounded-xl border border-[var(--border)]">
                            <p class="font-black text-[var(--text-primary)] mb-1">Documentos</p>
                            <span id="panelDocs">0/0 completos</span>
                        </div>
                        <div class="bg-white p-3 rounded-xl border border-[var(--border)]">
                            <p class="font-black text-[var(--text-primary)] mb-1">Plazos</p>
                            <span id="panelPlazos" class="text-red-500 font-bold">0 alertas críticas</span>
                        </div>
                        <div class="bg-white p-3 rounded-xl border border-[var(--border)]">
                            <p class="font-black text-[var(--text-primary)] mb-1">Validaciones</p>
                            <span id="panelFuentes">0 por revisar</span>
                        </div>
                        <div class="bg-white p-3 rounded-xl border border-[var(--border)]">
                            <p class="font-black text-[var(--text-primary)] mb-1">Declaracion</p>
                            <span id="panelPredam" class="text-red-500 font-bold">Bloqueada</span>
                        </div>
                    </div>
                    
                    <div class="mt-5 p-4 rounded-2xl bg-white border border-[var(--border)]">
                        <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] mb-1">Próxima acción recomendada</p>
                        <p id="panelSiguiente" class="font-bold text-sm text-[var(--text-primary)]">Selecciona una operación</p>
                    </div>
                </aside>
            </section>

            <section class="glass-card section-shell p-5">
                <div id="tabs" class="flex flex-wrap gap-2">
                    <button class="aduana-tab is-active px-4 py-2 rounded-full border border-[var(--border)] text-[11px] font-black uppercase tracking-widest" data-tab="guia">Guia</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="manifiesto">Transporte</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="timeline">Pasos</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="docs">Documentos</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="dta">Impuestos y declaracion</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="legal">Reglas aplicables</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="fuentes">Validaciones</button>
                    <button class="aduana-tab px-4 py-2 rounded-full border border-[var(--border)] bg-white text-gray-500 text-[11px] font-black uppercase tracking-widest" data-tab="especiales">Casos especiales</button>
                </div>
            </section>

            <section id="tab-guia" class="tab-panel grid grid-cols-1 xl:grid-cols-[1fr_24rem] gap-6">
                <div class="glass-card section-shell p-7">
                    <div class="section-heading">
                        <div>
                            <p class="pill-heading">Pregunta clave</p>
                            <h2 class="text-2xl font-black mt-3">Que pasara con el producto?</h2>
                        </div>
                        <button id="btnExplainRegimen" class="soft-button text-xs">Explicame la decision</button>
                    </div>
                    <div id="regimenChoices" class="grid md:grid-cols-2 xl:grid-cols-5 gap-3 mt-6">
                        <button class="regimen-choice is-active rounded-2xl border bg-white p-4 text-left" data-destino="PERU">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Se queda en Peru</p>
                            <p class="font-black mt-2 text-[var(--text-primary)]">Se vendera o usara aqui</p>
                        </button>
                        <button class="regimen-choice rounded-2xl border border-[var(--border)] bg-white p-4 text-left" data-destino="TRANSITO">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Solo pasa por Peru</p>
                            <p class="font-black mt-2 text-[var(--text-primary)]">Solo va de paso</p>
                        </button>
                        <button class="regimen-choice rounded-2xl border border-[var(--border)] bg-white p-4 text-left" data-destino="TEMPORAL">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Entra temporalmente</p>
                            <p class="font-black mt-2 text-[var(--text-primary)]">Entrara por un tiempo</p>
                        </button>
                        <button class="regimen-choice rounded-2xl border border-[var(--border)] bg-white p-4 text-left" data-destino="REIMPORTACION">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Retorna al pais</p>
                            <p class="font-black mt-2 text-[var(--text-primary)]">Vuelve al Peru</p>
                        </button>
                        <button class="regimen-choice rounded-2xl border border-[var(--border)] bg-white p-4 text-left" data-destino="TRANSBORDO">
                            <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Sale en otro transporte</p>
                            <p class="font-black mt-2 text-[var(--text-primary)]">Cambia de transporte</p>
                        </button>
                    </div>

                    <div class="grid md:grid-cols-2 gap-5 mt-6">
                        <div class="rounded-3xl border border-[var(--border)] bg-white p-6">
                            <div class="flex justify-between items-center">
                                <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Ruta sugerida</p>
                                <button type="button" class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] hover:underline flex items-center gap-1" onclick="cargarFichaAsesoria('REIMPORTACION')">
                                    ℹ️ Ficha
                                </button>
                            </div>
                            <h3 id="regimenNombre" class="text-2xl font-black mt-2 text-[var(--text-primary)]">-</h3>
                            <p id="regimenMotivo" class="text-sm text-[var(--text-secondary)] font-semibold mt-3">Selecciona una operacion para evaluar.</p>
                            <span id="regimenChip" class="source-chip mt-4">Referencia interna</span>
                        </div>
                        <div class="rounded-3xl border border-[var(--border)] bg-white p-6">
                            <div class="flex justify-between items-center">
                                <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Forma recomendada</p>
                                <button type="button" class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] hover:underline flex items-center gap-1" onclick="cargarFichaAsesoria('DESTINACION_DIFERIDA_15D')">
                                    ℹ️ Ficha
                                </button>
                            </div>
                            <h3 id="modalidadNombre" class="text-2xl font-black mt-2 text-[var(--text-primary)]">-</h3>
                            <p id="modalidadMotivo" class="text-sm text-[var(--text-secondary)] font-semibold mt-3">Pendiente.</p>
                            <p id="modalidadPlazo" class="text-xs font-black text-[var(--accent)] uppercase tracking-widest mt-4">-</p>
                            <span id="modalidadChip" class="source-chip mt-3 hidden"></span>
                        </div>
                    </div>
                </div>

                <aside class="glass-card section-shell p-6">
                    <p class="pill-heading">Alertas</p>
                    <div id="alertasBox" class="space-y-3 mt-5"></div>
                </aside>
            </section>

            <section id="tab-manifiesto" class="tab-panel hidden glass-card section-shell p-7">
                <div class="section-heading">
                    <div>
                        <p class="pill-heading">Datos de transporte</p>
                        <h2 class="text-2xl font-black mt-3">Registra como viene la carga</h2>
                    </div>
                    <button id="btnGuardarManifiesto" class="primary-button text-xs">Guardar datos</button>
                </div>
                <div class="grid md:grid-cols-3 gap-4 mt-6">
                    <input id="mNumero" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Nro manifiesto">
                    <input id="mBl" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="BL / documento transporte">
                    <input id="mContenedor" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Contenedor">
                    <input id="mPrecinto" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Precinto origen">
                    <input id="mBultos" type="number" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Bultos">
                    <input id="mPeso" type="number" step="0.001" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Peso bruto">
                    <input id="mPuertoOrigen" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Puerto origen">
                    <input id="mPuertoArribo" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Puerto arribo" value="Callao">
                    <input id="mDeposito" class="custom-input rounded-2xl px-4 py-3 font-bold" placeholder="Deposito temporal">
                </div>
                <p class="text-xs text-[var(--text-secondary)] font-semibold mt-5">Estos datos son para ordenar tu carpeta. No se envian como documento oficial.</p>
            </section>

            <section id="tab-timeline" class="tab-panel hidden grid grid-cols-1 xl:grid-cols-[1fr_20rem] gap-6">
                <div class="glass-card section-shell p-7">
                    <p class="pill-heading">Pasos de la importacion</p>
                    <div id="timelineBox" class="timeline-line space-y-5 mt-6"></div>
                </div>
                <aside class="glass-card section-shell p-6">
                    <p class="pill-heading">Fechas que cuidar</p>
                    <div id="plazosBox" class="space-y-3 mt-5"></div>
                </aside>
            </section>

            <section id="tab-docs" class="tab-panel hidden glass-card section-shell p-7">
                <div class="section-heading">
                    <div>
                        <p class="pill-heading">Documentos segun tu caso</p>
                        <h2 class="text-2xl font-black mt-3">Documentos que cambian segun tu caso</h2>
                    </div>
                    <a href="documentos.jsp" class="soft-button text-xs">Abrir carga de documentos</a>
                </div>
                <div id="docsBox" class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 mt-6"></div>
            </section>

            <section id="tab-dta" class="tab-panel hidden grid grid-cols-1 xl:grid-cols-2 gap-6">
                <div class="glass-card section-shell p-7 relative overflow-hidden">
                    <div class="flex items-center justify-between">
                        <p class="pill-heading">Impuestos estimados</p>
                        <button class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] hover:underline flex items-center gap-1" onclick="cargarFichaAsesoria('EXIGIBILIDAD_DTA')">
                            Ver ayuda
                        </button>
                    </div>
                    <div class="relative mt-6">
                        <div id="dtaBox" class="grid md:grid-cols-2 gap-4"></div>
                        <!-- Glassmorphic Warning Watermark/Banner overlay -->
                        <div class="absolute inset-0 bg-white/40 backdrop-blur-[2px] pointer-events-none rounded-3xl flex items-center justify-center p-6 border border-amber-200/50">
                            <div class="bg-amber-50/90 backdrop-blur-md px-6 py-4 rounded-2xl border border-amber-200 shadow-xl max-w-sm text-center">
                                <p class="text-[10px] font-black uppercase tracking-widest text-amber-700 flex items-center justify-center gap-1">
                                    CALCULO REFERENCIAL
                                </p>
                                <p class="text-xs text-amber-900 font-bold mt-2">
                                    Los montos son aproximados. Sirven para prepararte, no reemplazan la validacion oficial.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="glass-card section-shell p-7">
                    <div class="flex items-center justify-between">
                        <p class="pill-heading">Vista previa de declaracion</p>
                        <button class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] hover:underline flex items-center gap-1" onclick="cargarFichaAsesoria('ABANDONO_LEGAL')">
                            Ver plazos
                        </button>
                    </div>
                    <div id="predamBox" class="space-y-4 mt-6"></div>
                    <button id="btnPredam" class="primary-button text-xs mt-5">Crear vista previa</button>
                </div>
            </section>

            <section id="tab-legal" class="tab-panel hidden glass-card section-shell p-7">
                <p class="pill-heading">Reglas que aplican a tu caso</p>
                <div id="legalBox" class="grid md:grid-cols-2 gap-4 mt-6"></div>
            </section>

            <section id="tab-fuentes" class="tab-panel hidden grid grid-cols-1 xl:grid-cols-[1fr_22rem] gap-6">
                <div class="glass-card section-shell p-7">
                    <div class="section-heading">
                        <div>
                            <p class="pill-heading">Validaciones disponibles</p>
                            <h2 class="text-2xl font-black mt-3">De donde salen los datos</h2>
                        </div>
                        <button id="btnFuentesRefresh" class="soft-button text-xs">Actualizar validaciones</button>
                    </div>
                    <div id="fuentesBox" class="grid md:grid-cols-2 xl:grid-cols-3 gap-4 mt-6">
                        <div class="rounded-3xl border border-[var(--border)] bg-white p-5 text-sm font-bold text-[var(--text-secondary)]">Cargando validaciones...</div>
                    </div>
                    <div class="mt-6">
                        <h3 class="text-sm font-black uppercase tracking-widest text-[var(--text-tertiary)]">Ultimas validaciones</h3>
                        <div id="fuenteEventosBox" class="grid md:grid-cols-2 gap-3 mt-4"></div>
                    </div>
                </div>
                <aside class="glass-card section-shell p-6">
                    <p class="pill-heading">Seguimiento opcional</p>
                    <h3 class="text-xl font-black mt-3">Consulta sin prometer datos falsos</h3>
                    <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Si no hay credenciales DHL/FedEx/UPS/Maersk, el sistema lo marcara como pendiente de credenciales.</p>
                    <div class="space-y-3 mt-5">
                        <select id="trackingProveedor" class="custom-input rounded-2xl px-4 py-3 font-bold w-full">
                            <option value="DHL">DHL</option>
                            <option value="FEDEX">FedEx</option>
                            <option value="UPS">UPS</option>
                            <option value="MAERSK">Maersk</option>
                            <option value="OTRO">Otro/manual</option>
                        </select>
                        <input id="trackingNumber" class="custom-input rounded-2xl px-4 py-3 font-bold w-full" placeholder="Numero de seguimiento">
                        <input id="trackingBl" class="custom-input rounded-2xl px-4 py-3 font-bold w-full" placeholder="BL / contenedor opcional">
                        <button id="btnTrackingConsultar" class="primary-button text-xs w-full">Consultar seguimiento</button>
                    </div>
                    <div id="trackingBox" class="mt-5"></div>
                </aside>
            </section>

            <section id="tab-especiales" class="tab-panel hidden grid grid-cols-1 xl:grid-cols-2 gap-6">
                <div class="glass-card section-shell p-7">
                    <p class="pill-heading">Producto que vuelve al Peru</p>
                    <div class="grid md:grid-cols-2 gap-3 mt-5">
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="rExportada" type="checkbox" class="accent-[var(--accent)]" checked> Exportada desde Peru</label>
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="rRegularizada" type="checkbox" class="accent-[var(--accent)]" checked> Exportacion regularizada</label>
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="rTransformada" type="checkbox" class="accent-[var(--accent)]"> Transformada/reparada</label>
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="rBeneficio" type="checkbox" class="accent-[var(--accent)]"> Tuvo beneficio</label>
                        
                        <div class="flex flex-col gap-1 col-span-2">
                            <label class="text-[10px] uppercase tracking-wider font-bold text-gray-500">Fecha de exportación precedente</label>
                            <input id="rFechaExportacion" type="date" class="custom-input rounded-2xl px-4 py-3 font-bold">
                        </div>
                        <div class="flex flex-col gap-1 col-span-2">
                            <label class="text-[10px] uppercase tracking-wider font-bold text-gray-500">Fecha prevista de importación</label>
                            <input id="rFechaImportacion" type="date" class="custom-input rounded-2xl px-4 py-3 font-bold">
                        </div>
                    </div>
                    <button id="btnReimportacion" class="primary-button text-xs mt-5">Evaluar retorno</button>
                    <div id="reimportacionBox" class="mt-5"></div>
                </div>
                <div class="glass-card section-shell p-7">
                    <p class="pill-heading">Transbordo y regularizacion</p>
                    <select id="tModalidad" class="custom-input rounded-2xl px-4 py-3 font-bold mt-5 w-full">
                        <option value="M1_DIRECTO">Directo de un transporte a otro</option>
                        <option value="M2_TIERRA">Con descarga en tierra</option>
                        <option value="M3_DEPOSITO">Con ingreso a almacen temporal</option>
                    </select>
                    <div class="grid md:grid-cols-2 gap-3 mt-4">
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="tPeso" type="checkbox" class="accent-[var(--accent)]"> Diferencia peso mayor 2%</label>
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="tPrecinto" type="checkbox" class="accent-[var(--accent)]"> Precinto violado</label>
                        <label class="flex gap-2 items-center font-bold text-[var(--text-secondary)]"><input id="tPendientes" type="checkbox" class="accent-[var(--accent)]"> Solicitudes pendientes</label>
                    </div>
                    <button id="btnTransbordo" class="primary-button text-xs mt-5">Evaluar cambio de transporte</button>
                    <div id="transbordoBox" class="mt-5"></div>
                </div>
            </section>
        </section>
    </main>

    <!-- Panel de ayuda simple -->
    <div id="mentorDrawer" class="fixed inset-y-0 right-0 w-[26rem] bg-white/90 backdrop-blur-xl border-l border-[var(--border)] shadow-2xl z-50 transform translate-x-full transition-transform duration-300 ease-in-out flex flex-col">
        <div class="p-6 border-b border-[var(--border)] flex items-center justify-between bg-white/95">
            <div class="flex items-center gap-2">
                <span class="text-xl">⚖️</span>
                <div>
                    <h3 class="font-black text-sm text-[var(--text-primary)]">Ayuda para entender</h3>
                    <p class="text-[10px] text-gray-500 font-bold uppercase tracking-widest">Referencia legal simplificada</p>
                </div>
            </div>
            <button id="btnMentorClose" class="p-2 hover:bg-gray-100 rounded-full text-gray-500 transition-colors">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
        </div>
        <div class="flex-1 overflow-y-auto p-6 space-y-6 custom-scrollbar">
            <div class="bg-[var(--accent-soft)] p-5 rounded-3xl border border-[var(--accent-glow)] space-y-2">
                <p id="mentorArticulo" class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">-</p>
                <h4 id="mentorTitulo" class="text-xl font-black text-[var(--text-primary)]">Seleccione un tema de consulta</h4>
            </div>
            <div class="space-y-4">
                <div class="space-y-2">
                    <p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Explicacion</p>
                    <p id="mentorExplicacion" class="text-sm font-semibold text-[var(--text-secondary)] leading-relaxed">
                        Haz clic en las ayudas para entender por que el sistema recomienda una ruta o un documento.
                    </p>
                </div>
                <div id="mentorConsejoBox" class="bg-amber-50 border border-amber-200 p-4 rounded-2xl hidden">
                    <p class="text-[10px] font-black uppercase tracking-widest text-amber-700">Recomendacion</p>
                    <p id="mentorConsejo" class="text-xs font-bold text-amber-900 mt-1"></p>
                </div>
            </div>
        </div>
        <div class="p-6 border-t border-[var(--border)] bg-gray-50/80">
            <p class="text-[10px] text-center text-gray-400 font-semibold leading-relaxed">
                Esta ayuda es educativa y referencial. Antes de presentar una declaracion oficial, valida el caso con un especialista o la entidad correspondiente.
            </p>
        </div>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        window.ctx = '<%= request.getContextPath() %>';
        let operaciones = [];
        let expediente = null;
        let fuentesEstado = null;
        let selectedDestino = 'PERU';
        const qs = new URLSearchParams(location.search);

        const steps = ['Producto','Ruta','Forma','Transporte','Valor','Docs','Impuestos','Riesgo','Declaracion','Reglas'];
        const stepRail = document.getElementById('stepRail');
        stepRail.innerHTML = steps.map((s, i) => '<div class="flex items-center gap-2 rounded-2xl border border-[#E6E2D8] bg-white px-3 py-3"><span class="step-dot is-ready w-7 h-7 rounded-full border flex items-center justify-center text-[10px] font-black">' + (i + 1) + '</span><span class="text-[10px] font-black uppercase tracking-widest text-gray-500">' + s + '</span></div>').join('');

        function activeOperacionId() { return Number(document.getElementById('operacionSelect').value || 0); }
        function money(value) { return '$ ' + Number(value || 0).toLocaleString('es-PE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }); }
        function pct(value) { return Math.round(Number(value || 0) * 100) + '%'; }
        function esc(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
        function activeOperacion() { return operaciones.find(item => Number(item.id) === activeOperacionId()) || {}; }

        async function api(path, options = {}) {
            const headers = Object.assign({ 'Content-Type': 'application/json', 'X-CSRF-TOKEN': window.csrfToken || '' }, options.headers || {});
            const res = await fetch(window.ctx + path, Object.assign({}, options, { headers }));
            const payload = await res.json();
            if (payload.data && payload.data.csrfToken) window.csrfToken = payload.data.csrfToken;
            if (!res.ok || payload.success === false) throw new Error(payload.message || 'Error de API');
            return payload.data;
        }

        async function cargarOperaciones() {
            const res = await fetch(window.ctx + '/api/importacion/listar');
            operaciones = await res.json();
            const select = document.getElementById('operacionSelect');
            if (!Array.isArray(operaciones) || operaciones.length === 0) {
                select.innerHTML = '<option value="">No tienes operaciones aun</option>';
                renderEmpty();
                return;
            }
            select.innerHTML = operaciones.map(op => '<option value="' + op.id + '">#' + op.id + ' - ' + esc(op.productoDesc || op.hsCode || 'Operacion') + '</option>').join('');
            const requested = qs.get('operacionId') || qs.get('id');
            if (requested && operaciones.some(op => String(op.id) === String(requested))) select.value = requested;
            await evaluarYRender();
        }

        async function evaluarYRender() {
            const operacionId = activeOperacionId();
            if (!operacionId) return;
            try {
                const op = operaciones.find(item => Number(item.id) === operacionId) || {};
                const regimen = await api('/api/aduanas/evaluar-regimen', { method: 'POST', body: JSON.stringify({ destino: selectedDestino }) });
                const modalidad = await api('/api/aduanas/evaluar-modalidad', { method: 'POST', body: JSON.stringify({ regimenCodigo: regimen.regimenCodigo, fob: op.valorFob || op.fob || 0, restringida: isRestricted(op.hsCode) }) });
                renderRegimen(regimen, modalidad);
                await cargarExpediente(false);
            } catch (e) {
                notify('No se pudo evaluar el expediente.', 'error');
            }
        }

        async function cargarExpediente(silent) {
            const operacionId = activeOperacionId();
            if (!operacionId) return;
            try {
                expediente = await api('/api/aduanas/expediente?operacionId=' + operacionId);
                renderExpediente();
            } catch (e) {
                if (!silent) notify('Genera el expediente para activar timeline y alertas.', 'warning');
            }
        }

        async function generarExpediente() {
            const operacionId = activeOperacionId();
            if (!operacionId) return notify('Selecciona una operacion.', 'warning');
            expediente = await api('/api/aduanas/generar-expediente', { method: 'POST', body: JSON.stringify({ operacionId, destino: selectedDestino }) });
            renderExpediente();
            notify('Expediente aduanero generado.', 'success');
        }

        function renderRegimen(regimen, modalidad) {
            document.getElementById('regimenNombre').textContent = regimen.regimenNombre || '-';
            document.getElementById('regimenMotivo').textContent = regimen.motivo || '-';
            document.getElementById('modalidadNombre').textContent = modalidad.modalidadNombre || '-';
            document.getElementById('modalidadMotivo').textContent = modalidad.motivo || '-';
            document.getElementById('modalidadPlazo').textContent = modalidad.plazoTexto || '-';
            document.getElementById('panelSiguiente').textContent = regimen.siguientePaso || 'Completar expediente';
            
            const rChip = document.getElementById('regimenChip');
            rChip.textContent = regimen.sourceType || 'FUENTE NO IDENTIFICADA';
            rChip.className = 'source-chip mt-4 ' + chipClass(regimen.sourceType);
            
            const mChip = document.getElementById('modalidadChip');
            mChip.textContent = modalidad.sourceType || 'FUENTE NO IDENTIFICADA';
            mChip.className = 'source-chip mt-3 ' + chipClass(modalidad.sourceType);
            mChip.classList.remove('hidden');
        }

        function renderExpediente() {
            if (!expediente || expediente.errorCode) return;
            const panel = expediente.panel || {};
            
            document.getElementById('panelCompletitud').textContent = panel.completitud || 0;
            document.getElementById('panelDocs').textContent = (panel.docsCompletos || 0) + '/' + (panel.docsTotal || 0) + ' completos';
            
            const plazosEl = document.getElementById('panelPlazos');
            if (panel.alertasCriticas > 0) {
                plazosEl.textContent = panel.alertasCriticas + ' alertas críticas';
                plazosEl.className = 'text-red-500 font-bold';
            } else {
                plazosEl.textContent = '0 alertas críticas';
                plazosEl.className = 'text-green-600 font-bold';
            }
            
            document.getElementById('panelFuentes').textContent = (panel.fuentesManuales || 0) + ' manual, ' + (panel.fuentesSimuladas || 0) + ' referencial';
            
            const predamEl = document.getElementById('panelPredam');
            predamEl.textContent = panel.estadoPredam || 'Pendiente';
            if ((panel.estadoPredam || '').toLowerCase().includes('bloqueada')) {
                predamEl.className = 'text-red-500 font-bold';
            } else {
                predamEl.className = 'text-orange-500 font-bold';
            }
            
            document.getElementById('panelSiguiente').textContent = panel.siguientePaso || 'Completar expediente';

            renderRegimen(expediente.regimen || {}, expediente.modalidad || {});
            renderAlertas(expediente.alertas || []);
            renderTimeline(expediente.timeline || []);
            renderDocs(expediente.documentos || []);
            renderDta(expediente.dta || {}, expediente.predam || {});
            renderLegal(expediente.baseLegal || []);
            renderPlazos(expediente.plazos || []);
            cargarFuentesReales();
        }

        function renderAlertas(alertas) {
            const box = document.getElementById('alertasBox');
            if (!alertas.length) {
                box.innerHTML = '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-semibold text-gray-500">Sin alertas criticas. Genera expediente para calcular controles.</div>';
                return;
            }
            box.innerHTML = alertas.map(a => '<div class="rounded-2xl border border-[var(--border)] bg-white p-4"><p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(a.severidad || 'INFO') + '</p><p class="font-black mt-1">' + esc(a.mensaje) + '</p><p class="text-xs text-gray-500 font-semibold mt-2">' + esc(a.accion) + '</p><span class="source-chip source-chip--estimated mt-3">ESTIMADO</span></div>').join('');
        }

        function renderTimeline(rows) {
            const box = document.getElementById('timelineBox');
            box.innerHTML = rows.map(ev => '<div class="timeline-item"><div class="rounded-3xl border border-[var(--border)] bg-white p-5"><div class="flex flex-wrap items-center justify-between gap-3"><p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(ev.codigo) + '</p><span class="font-mono text-xs font-black text-gray-500">' + esc(ev.fecha || '') + '</span></div><h3 class="text-lg font-black mt-2">' + esc(ev.nombre) + '</h3><p class="text-sm text-gray-500 font-semibold mt-2">' + esc(ev.efectoLegal) + '</p><p class="text-xs text-gray-400 font-semibold mt-2">' + esc(ev.observacion) + '</p></div></div>').join('');
        }

        function renderPlazos(plazos) {
            const box = document.getElementById('plazosBox');
            if (!plazos || !plazos.length) {
                box.innerHTML = '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-semibold text-gray-500">No hay plazos críticos para este régimen.</div>';
                return;
            }
            
            box.innerHTML = plazos.map(p => {
                let colorClass = 'text-gray-600';
                let borderClass = 'border-[var(--border)]';
                let chipStyle = 'source-chip--official';
                if (p.status === 'EXPIRED') { colorClass = 'text-red-600'; borderClass = 'border-red-500'; chipStyle = 'source-chip--error bg-red-100 text-red-600 border-red-300'; }
                else if (p.status === 'CRITICAL') { colorClass = 'text-orange-600'; borderClass = 'border-orange-500'; chipStyle = 'bg-orange-100 text-orange-600 border-orange-300'; }
                else if (p.status === 'WARNING') { colorClass = 'text-yellow-600'; borderClass = 'border-yellow-500'; chipStyle = 'bg-yellow-100 text-yellow-600 border-yellow-300'; }

                return '<div class="rounded-2xl border ' + borderClass + ' bg-white p-4">' +
                       '<p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(p.label) + '</p>' +
                       '<p class="text-sm font-bold ' + colorClass + ' mt-2">Base: ' + esc(p.baseEvent) + ' (' + esc(p.baseDate).replace('T',' ') + ')</p>' +
                       '<p class="text-sm font-bold ' + colorClass + ' mt-1">Vence: ' + esc(p.deadline).replace('T',' ') + '</p>' +
                       '<div class="flex items-center gap-2 mt-3">' +
                       '<span class="source-chip text-[9px] px-2 py-1 ' + chipStyle + '">' + esc(p.status) + ' (' + esc(p.daysRemaining) + ' d)</span>' +
                       '<span class="source-chip text-[9px] px-2 py-1 ' + chipClass(p.sourceType) + '">' + esc(p.source) + '</span>' +
                       '</div>' +
                       '<p class="text-xs text-gray-500 font-semibold mt-3">' + esc(p.message) + '</p>' +
                       '</div>';
            }).join('');
        }

        function renderDocs(docs) {
            const box = document.getElementById('docsBox');
            box.innerHTML = docs.map(d => '<div class="rounded-3xl border border-[var(--border)] bg-white p-5"><div class="flex items-start justify-between gap-3"><h3 class="font-black">' + esc(d.nombre) + '</h3><span class="source-chip ' + (d.requerido ? 'source-chip--pending' : 'source-chip--bd') + '">' + (d.requerido ? 'OBLIGATORIO' : 'SEGUN CASO') + '</span></div><p class="text-sm text-gray-500 font-semibold mt-3">' + esc(d.descripcion) + '</p><p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] mt-4">' + esc(d.estado) + '</p></div>').join('');
        }

        function renderDta(dta, predam) {
            const dtaBox = document.getElementById('dtaBox');
            const rows = [
                ['Base CIF USD', money(dta.baseCifUsd)],
                ['Base CIF PEN', 'S/ ' + Number(dta.baseCifPen || 0).toLocaleString('es-PE', {minimumFractionDigits:2})],
                ['Ad Valorem', money(dta.adValorem)],
                ['IGV', money(dta.igv)],
                ['IPM', money(dta.ipm)],
                ['Percepcion', money(dta.percepcion)],
                ['Total estimado', money(dta.total)],
                ['Origen del dato', dta.sourceType || 'REFERENCIAL'],
                ['Envio y seguro <button class="text-[9px] font-black text-[var(--accent)] hover:underline ml-1" onclick="cargarFichaAsesoria(\'INCOTERMS_VALORACION\')">Ver ayuda</button>', 'Revisar quien paga']
            ];
            dtaBox.innerHTML = rows.map(r => '<div class="rounded-2xl border border-[var(--border)] bg-white p-4"><p class="text-[10px] font-black uppercase tracking-widest text-gray-400">' + r[0] + '</p><p class="text-xl font-black mt-1">' + r[1] + '</p></div>').join('');
            document.getElementById('predamBox').innerHTML =
                '<div class="rounded-3xl border border-[var(--border)] bg-white p-5"><p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Numero</p><p class="text-2xl font-black font-mono mt-2">' + esc(predam.numeroDam || 'Pendiente') + '</p></div>' +
                '<div class="rounded-3xl border border-[var(--border)] bg-white p-5"><p class="text-[10px] font-black uppercase tracking-widest text-gray-400">Nivel de revision</p><p class="text-2xl font-black mt-2">Riesgo estimado: ' + esc(predam.canalProbable || '-') + '</p><span class="source-chip mt-3 ' + chipClass(predam.sourceType) + '">' + esc(predam.sourceType || 'REFERENCIAL') + '</span><p class="text-xs text-[var(--text-secondary)] font-semibold mt-3 text-red-500">' + esc(predam.legalWarning || 'Esta vista previa es referencial. No reemplaza la declaracion oficial ni tiene valor legal.') + '</p></div>';
        }

        function renderLegal(items) {
            const box = document.getElementById('legalBox');
            box.innerHTML = items.map(item => '<div class="rounded-3xl border border-[var(--border)] bg-white p-5"><p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(item.procedimiento) + '</p><h3 class="font-black mt-2">' + esc(item.validacion) + '</h3><p class="text-sm text-gray-500 font-semibold mt-2">' + esc(item.baseLegal) + '</p><div class="mt-4 flex flex-wrap gap-2"><span class="source-chip source-chip--bd">' + esc(item.estado) + '</span><span class="source-chip source-chip--estimated">REFERENCIAL</span></div><p class="text-xs text-gray-400 font-semibold mt-3">' + esc(item.accion) + '</p></div>').join('');
        }

        async function cargarFuentesReales() {
            const op = activeOperacion();
            const hs = op.hsCode || op.codigoHs || (expediente?.predam?.hsCode) || '';
            try {
                const [estado, arancel, vuce, comtrade] = await Promise.all([
                    api('/api/fuentes/estado'),
                    api('/api/fuentes/arancel?hs=' + encodeURIComponent(hs)),
                    api('/api/fuentes/vuce?hs=' + encodeURIComponent(hs)),
                    hs ? api('/api/fuentes/comtrade?hs=' + encodeURIComponent(hs)) : Promise.resolve(null)
                ]);
                fuentesEstado = { estado, arancel, vuce, comtrade };
                renderFuentes();
            } catch (e) {
                document.getElementById('fuentesBox').innerHTML = '<div class="rounded-3xl border border-[var(--border)] bg-white p-5 text-sm font-bold text-gray-500">No se pudieron cargar las validaciones. Puedes seguir revisando la carpeta y volver a intentar.</div>';
            }
        }

        function chipClass(sourceType) {
            const type = String(sourceType || '').toUpperCase();
            if (type.includes('OFFICIAL_API') || type.includes('OFFICIAL_PROCEDURE') || type.includes('OFICIAL')) return 'source-chip--official';
            if (type.includes('CACHE')) return 'source-chip--cache';
            if (type.includes('FALLBACK')) return 'source-chip--fallback';
            if (type.includes('SIMULATED') || type.includes('SIMULADO')) return 'source-chip--simulated';
            if (type.includes('PENDING') || type.includes('PENDIENTE')) return 'source-chip--pending';
            if (type.includes('MANUAL')) return 'source-chip--manual';
            if (type.includes('SYSTEM_RULE')) return 'source-chip--system';
            if (type.includes('REFERENTIAL')) return 'source-chip--estimated';
            if (type.includes('UNKNOWN')) return 'source-chip--error bg-red-100 text-red-600 border-red-300';
            return 'source-chip--bd';
        }

        function fuenteCard(title, value, sourceType, meta, url) {
            return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5">' +
                '<p class="text-[10px] font-black uppercase tracking-widest text-gray-400">' + esc(title) + '</p>' +
                '<h3 class="text-xl font-black mt-2">' + esc(value || '-') + '</h3>' +
                '<p class="text-xs text-gray-500 font-semibold mt-3">' + esc(meta || '') + '</p>' +
                '<div class="mt-4 flex flex-wrap gap-2"><span class="source-chip ' + chipClass(sourceType) + '">' + esc(sourceType || 'BD_LOCAL') + '</span>' +
                (url ? '<a class="source-chip source-chip--cache" target="_blank" rel="noopener" href="' + esc(url) + '">Ver fuente</a>' : '') +
                '</div></div>';
        }

        function renderFuentes() {
            if (!fuentesEstado) return;
            const estado = fuentesEstado.estado || {};
            const tc = estado.tipoCambio || {};
            const arancel = fuentesEstado.arancel || {};
            const vuce = fuentesEstado.vuce || {};
            const comtrade = fuentesEstado.comtrade || {};
            const resumen = estado.resumen || {};
            const mercado = comtrade.mercado || {};
            document.getElementById('fuentesBox').innerHTML = [
                fuenteCard('Tipo de cambio', 'S/ ' + (tc.tipoCambio || tc.venta || '--'), tc.sourceType || 'CACHE', 'Dato usado para convertir dolares a soles. Confianza: ' + pct(tc.confidence || 0), tc.fuenteUrl || 'https://estadisticas.bcrp.gob.pe/estadisticas/series/api/'),
                fuenteCard('Codigo de producto', arancel.hsCode || activeOperacion().hsCode || 'Pendiente', arancel.sourceType || 'BD_LOCAL', (arancel.descripcion || 'Validar codigo del producto') + ' | Confianza: ' + pct(arancel.confidence || 0), arancel.fuenteUrl),
                fuenteCard('Permisos', ((vuce.tramites || [])[0]?.entidad || 'Validar entidad'), vuce.sourceType || 'BD_LOCAL', vuce.nota || 'Referencia local. No equivale a permiso emitido.', vuce.fuenteUrl),
                fuenteCard('Tendencia de mercado', mercado.valorUsd ? ('$ ' + Number(mercado.valorUsd).toLocaleString('es-PE')) : 'Dato pendiente', comtrade.sourceType || 'CACHE', 'Referencia para comparar volumen comercial. Confianza: ' + pct(comtrade.confidence || 0), comtrade.fuenteUrl || 'https://comtradeplus.un.org/'),
                fuenteCard('Seguimiento logistico', 'Opcional', 'PENDIENTE_CREDENCIALES', 'Solo se valida automaticamente si agregas datos de proveedor logistico.', 'https://developer.dhl.com/'),
                fuenteCard('Resumen de validaciones', (resumen.okCache || 0) + ' disponibles', 'BD_LOCAL', 'Pendientes: ' + ((resumen.errores || 0) + (resumen.fallbacks || 0)) + ' | Referenciales: ' + (resumen.simulados || 0), null)
            ].join('');
            const eventos = estado.ultimosEventos || [];
            document.getElementById('fuenteEventosBox').innerHTML = eventos.length
                ? eventos.map(ev => '<div class="rounded-2xl border border-[var(--border)] bg-white p-4"><div class="flex items-center justify-between gap-2"><p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">' + esc(ev.fuente) + '</p><span class="source-chip ' + chipClass(ev.sourceType) + '">' + esc(ev.resultado) + '</span></div><p class="text-sm font-black mt-2">' + esc(ev.tipoEvento || ev.entidadAfectada || 'Validacion') + '</p><p class="text-xs text-gray-500 font-semibold mt-2">' + esc(ev.url || ev.mensajeError || 'Validacion registrada con fecha.') + '</p></div>').join('')
                : '<div class="rounded-2xl border border-[var(--border)] bg-white p-4 text-sm font-bold text-gray-500">Aun no hay validaciones registradas.</div>';
        }

        async function consultarTrackingFuente() {
            const operacionId = activeOperacionId();
            const trackingNumber = document.getElementById('trackingNumber').value.trim();
            const blOrContainer = document.getElementById('trackingBl').value.trim();
            if (!trackingNumber && !blOrContainer) return notify('Ingresa tracking, BL o contenedor.', 'warning');
            const data = await api('/api/fuentes/tracking/consultar', {
                method: 'POST',
                body: JSON.stringify({
                    operacionId,
                    proveedor: document.getElementById('trackingProveedor').value,
                    trackingNumber,
                    blNumber: blOrContainer,
                    containerNumber: blOrContainer
                })
            });
            const envio = data.envio || {};
            const eventos = data.eventos || [];
            document.getElementById('trackingBox').innerHTML = resultCard(envio.estadoActual || 'Tracking registrado', 'Fuente: ' + (envio.source || 'TRACKING_API') + ' | Endpoint: ' + (data.apiEndpoint || 'pendiente'), 'Estado fuente: ' + (envio.sourceType || data.sourceType || 'PENDIENTE_CREDENCIALES')) +
                (eventos.length ? '<div class="mt-3 space-y-2">' + eventos.map(ev => '<div class="rounded-2xl border border-[var(--border)] bg-white p-3 text-xs font-semibold text-gray-500">' + esc(ev.estado) + ': ' + esc(ev.descripcion) + '</div>').join('') + '</div>' : '');
            await cargarFuentesReales();
        }

        function renderEmpty() {
            document.getElementById('panelCompletitud').textContent = '0';
            document.getElementById('panelSiguiente').textContent = 'Crea una operacion primero';
            document.getElementById('alertasBox').innerHTML = '<a href="evaluacion.jsp" class="primary-button text-xs w-full justify-center">Crear importacion</a>';
        }

        async function guardarManifiesto() {
            const operacionId = activeOperacionId();
            if (!operacionId) return;
            await api('/api/aduanas/registrar-manifiesto', {
                method: 'POST',
                body: JSON.stringify({
                    operacionId,
                    numeroManifiesto: document.getElementById('mNumero').value,
                    numeroDocumento: document.getElementById('mBl').value,
                    numeroContenedor: document.getElementById('mContenedor').value,
                    precintoOrigen: document.getElementById('mPrecinto').value,
                    bultos: Number(document.getElementById('mBultos').value || 1),
                    pesoBruto: Number(document.getElementById('mPeso').value || 0),
                    puertoOrigen: document.getElementById('mPuertoOrigen').value,
                    puertoArribo: document.getElementById('mPuertoArribo').value,
                    depositoTemporal: document.getElementById('mDeposito').value
                })
            });
            notify('Datos de transporte guardados.', 'success');
        }

        async function generarPredam() {
            const operacionId = activeOperacionId();
            if (!operacionId) return;
            const data = await api('/api/aduanas/generar-predam', { method: 'POST', body: JSON.stringify({ operacionId, destino: selectedDestino }) });
            if (data.success === false && data.errorCode === 'PREDAM_VALIDATION_FAILED') {
                const missingHtml = (data.missingFields || []).map(f => '<li>• ' + esc(f) + '</li>').join('');
                const html = '<div class="text-left"><p class="font-bold text-red-600 mb-2">' + esc(data.title) + '</p><p class="text-sm mb-2">' + esc(data.message) + '</p><ul class="text-xs text-red-500 font-semibold">' + missingHtml + '</ul><p class="text-xs text-gray-500 mt-2">Completa estos datos antes de volver a intentar.</p></div>';
                notifyHtml(html, 'error');
                return;
            }
            notify(data.mensaje || 'Vista previa creada.', 'success');
            await cargarExpediente(true);
        }

        async function evaluarReimportacion() {
            const fechaExport = document.getElementById('rFechaExportacion').value;
            const fechaImport = document.getElementById('rFechaImportacion').value;
            if (!fechaExport || !fechaImport) {
                return notify('Por favor, seleccione ambas fechas para la evaluación.', 'warning');
            }
            const d1 = new Date(fechaExport);
            const d2 = new Date(fechaImport);
            if (d2 < d1) {
                return notify('La fecha prevista de importación debe ser posterior a la fecha de exportación.', 'warning');
            }
            const diffMonths = (d2.getFullYear() - d1.getFullYear()) * 12 + (d2.getMonth() - d1.getMonth());
            
            const data = await api('/api/aduanas/evaluar-reimportacion', {
                method: 'POST',
                body: JSON.stringify({
                    exportadaDesdePeru: document.getElementById('rExportada').checked,
                    exportacionRegularizada: document.getElementById('rRegularizada').checked,
                    transformada: document.getElementById('rTransformada').checked,
                    beneficioExportacion: document.getElementById('rBeneficio').checked,
                    mesesDesdeEmbarque: diffMonths
                })
            });
            document.getElementById('reimportacionBox').innerHTML = resultCard(data.procede ? 'Podria proceder' : 'Aun no procede', data.diagnostico, 'Nivel de revision probable: ' + data.canalProbable + ' (referencial)');
        }

        async function evaluarTransbordo() {
            const data = await api('/api/aduanas/evaluar-transbordo', {
                method: 'POST',
                body: JSON.stringify({
                    modalidad: document.getElementById('tModalidad').value,
                    diferenciaPesoMayor2: document.getElementById('tPeso').checked,
                    precintoViolado: document.getElementById('tPrecinto').checked,
                    solicitudesPendientes: document.getElementById('tPendientes').checked
                })
            });
            document.getElementById('transbordoBox').innerHTML = resultCard(data.regularizable ? 'Regularizable' : 'Con observaciones', data.mensaje, (data.alertas || []).join(' | '));
        }

        function resultCard(title, text, detail) {
            return '<div class="rounded-3xl border border-[var(--border)] bg-white p-5"><h3 class="text-xl font-black">' + esc(title) + '</h3><p class="text-sm text-gray-500 font-semibold mt-2">' + esc(text) + '</p><p class="text-xs text-[var(--accent)] font-black uppercase tracking-widest mt-4">' + esc(detail) + '</p></div>';
        }

        function isRestricted(hs) {
            hs = String(hs || '');
            return hs.startsWith('8517') || hs.startsWith('2106') || hs.startsWith('3004') || hs.startsWith('1209');
        }

        function notify(message, type) {
            const old = document.querySelector('.importease-toast');
            if (old) old.remove();
            const div = document.createElement('div');
            div.className = 'importease-toast importease-toast--' + (type || 'success');
            div.textContent = message;
            document.body.appendChild(div);
            setTimeout(() => div.remove(), 4000);
        }

        function notifyHtml(html, type) {
            const old = document.querySelector('.importease-toast');
            if (old) old.remove();
            const div = document.createElement('div');
            div.className = 'importease-toast importease-toast--' + (type || 'success');
            div.innerHTML = html;
            document.body.appendChild(div);
            setTimeout(() => div.remove(), 6000);
        }

        document.querySelectorAll('.regimen-choice').forEach(btn => {
            btn.addEventListener('click', () => {
                selectedDestino = btn.dataset.destino;
                document.querySelectorAll('.regimen-choice').forEach(b => b.classList.remove('is-active'));
                btn.classList.add('is-active');
                evaluarYRender();
            });
        });

        document.querySelectorAll('.aduana-tab').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('.aduana-tab').forEach(b => b.classList.remove('is-active'));
                btn.classList.add('is-active');
                document.querySelectorAll('.tab-panel').forEach(panel => panel.classList.add('hidden'));
                document.getElementById('tab-' + btn.dataset.tab).classList.remove('hidden');
            });
        });

        document.getElementById('operacionSelect').addEventListener('change', evaluarYRender);
        document.getElementById('btnGenerar').addEventListener('click', generarExpediente);
        document.getElementById('btnGuardarManifiesto').addEventListener('click', guardarManifiesto);
        document.getElementById('btnPredam').addEventListener('click', generarPredam);
        document.getElementById('btnReimportacion').addEventListener('click', evaluarReimportacion);
        document.getElementById('btnTransbordo').addEventListener('click', evaluarTransbordo);
        document.getElementById('btnFuentesRefresh').addEventListener('click', cargarFuentesReales);
        document.getElementById('btnTrackingConsultar').addEventListener('click', consultarTrackingFuente);
        document.getElementById('btnExplainRegimen').addEventListener('click', () => notify('El sistema toma respuestas simples y propone una ruta referencial. Valida el caso real antes de presentar documentos.', 'warning'));
        
        // Mentor Drawer Handlers
        const mentorDrawer = document.getElementById('mentorDrawer');
        const btnMentorToggle = document.getElementById('btnMentorToggle');
        const btnMentorClose = document.getElementById('btnMentorClose');

        function toggleMentorDrawer(show) {
            if (show) {
                mentorDrawer.classList.remove('translate-x-full');
            } else {
                mentorDrawer.classList.add('translate-x-full');
            }
        }

        btnMentorToggle.addEventListener('click', () => {
            cargarFichaAsesoria('GENERAL');
        });
        btnMentorClose.addEventListener('click', () => toggleMentorDrawer(false));

        async function cargarFichaAsesoria(clave) {
            try {
                const data = await api('/api/mentor/ficha?clave=' + encodeURIComponent(clave));
                document.getElementById('mentorTitulo').textContent = data.titulo || '-';
                document.getElementById('mentorArticulo').textContent = data.articulo || '-';
                document.getElementById('mentorExplicacion').textContent = data.explicacion || '-';
                const consejoBox = document.getElementById('mentorConsejoBox');
                if (data.consejo) {
                    document.getElementById('mentorConsejo').textContent = data.consejo;
                    consejoBox.classList.remove('hidden');
                } else {
                    consejoBox.classList.add('hidden');
                }
                toggleMentorDrawer(true);
            } catch (e) {
                notify('No se pudo cargar la asesoría técnica.', 'error');
            }
        }

        function initReimportacionDates() {
            const today = new Date();
            const exportDate = new Date();
            exportDate.setMonth(today.getMonth() - 8);
            
            document.getElementById('rFechaImportacion').value = today.toISOString().split('T')[0];
            document.getElementById('rFechaExportacion').value = exportDate.toISOString().split('T')[0];
        }

        initReimportacionDates();
        cargarOperaciones();
    </script>
</body>
</html>
