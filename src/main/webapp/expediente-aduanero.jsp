<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%!
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("</", "<\\/");
    }
%>
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
    <link href="css/expediente.css" rel="stylesheet">
    
</head>
<body class="expediente-page flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
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
        <section class="expediente-summary" aria-label="Resumen de la importación">
  <div class="expediente-summary-item" id="resumenEstadoVisual"><span class="summary-icon">📦</span> <strong>Estado de tu importación:</strong> <span class="value"></span></div>
  <div class="expediente-summary-item" id="resumenAvanceVisual"><span class="summary-icon">📈</span> <strong>Avance:</strong> <span class="value"></span></div>
  <div class="expediente-summary-item" id="resumenSiguienteVisual"><span class="summary-icon">➜</span> <strong>Siguiente paso:</strong> <span class="value"></span></div>
  <div class="expediente-summary-item" id="resumenPredamVisual"><span class="summary-icon">🧾</span> <strong>PRE‑DAM:</strong> <span class="value"></span></div>
  <div class="expediente-summary-item" id="resumenRiesgoVisual"><span class="summary-icon">⚠️</span> <strong>Riesgo:</strong> <span class="value"></span></div>
</section>

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
                            <span id="panelPlazos" class="text-red-500 font-bold">0 alertas crÃ­ticas</span>
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
                        <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)] mb-1">PrÃ³xima acciÃ³n recomendada</p>
                        <p id="panelSiguiente" class="expediente-next-action font-bold text-sm text-[var(--text-primary)]" role="region" aria-live="polite" aria-label="Siguiente acción recomendada">Selecciona una operación</p>
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
                                    â„¹ï¸  Ficha
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
                                    â„¹ï¸  Ficha
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
                    <details class="expediente-technical-group"><summary>Timeline</summary>
  <div id="timelineBox" class="timeline-line space-y-5 mt-6"></div>
</details>
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
                        <div id="dtaWatermark" class="absolute inset-0 bg-white/40 backdrop-blur-[2px] pointer-events-none rounded-3xl flex items-center justify-center p-6 border border-amber-200/50">
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
                <details class="expediente-technical-group"><summary>Legal</summary>
  <div id="legalBox" class="grid gap-4"></div>
</details>
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
                    <details class="expediente-technical-group"><summary>Fuentes</summary>
  <div id="fuentesBox" class="grid gap-4"></div>
</details>:grid-cols-2 xl:grid-cols-3 gap-4 mt-6">
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
                            <label class="text-[10px] uppercase tracking-wider font-bold text-gray-500">Fecha de exportaciÃ³n precedente</label>
                            <input id="rFechaExportacion" type="date" class="custom-input rounded-2xl px-4 py-3 font-bold">
                        </div>
                        <div class="flex flex-col gap-1 col-span-2">
                            <label class="text-[10px] uppercase tracking-wider font-bold text-gray-500">Fecha prevista de importaciÃ³n</label>
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
                <span class="text-xl">âš–ï¸</span>
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
            <p class="text-[10px] center text-gray-400 font-semibold leading-relaxed">
                Esta ayuda es educativa y referencial. Antes de presentar una declaracion oficial, valida el caso con un especialista o la entidad correspondiente.
            </p>
        </div>
    </div>

    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        window.ImportEase = window.ImportEase || {};
        window.ImportEase.ctx        = '<%= escapeJs(request.getContextPath()) %>';
        window.ImportEase.csrfToken  = '<%= escapeJs(request.getAttribute("csrfToken") != null ? String.valueOf(request.getAttribute("csrfToken")) : "") %>';
        window.ImportEase.csrfHeader = '<%= escapeJs(request.getAttribute("csrfHeader") != null ? String.valueOf(request.getAttribute("csrfHeader")) : "X-CSRF-TOKEN") %>';
        // Compatibilidad legacy
        window.ctx        = window.ImportEase.ctx;
        window.csrfToken  = window.ImportEase.csrfToken;
    </script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/expediente.js" defer></script>
</body>
</html>
