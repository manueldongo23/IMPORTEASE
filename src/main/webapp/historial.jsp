<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="java.util.List" %>
<%@ page import="com.importease.proyecto.model.Importacion" %>
<%@ page import="com.importease.proyecto.repository.ImportacionDAO" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%@ page import="java.sql.Connection" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    String userNombre = (String) session.getAttribute("usuarioNombre");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Seguimiento</title>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js" integrity="sha512-GNBJfMnHMHgGFAMxWPcVRFjTHLHv31GKGL2Y2J+Nz3biHBkNEEbgOCBe/RgizvJNaT0CkM+MJhUvRiLl5p+IA==" crossorigin="anonymous" referrerpolicy="no-referrer"></script>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <style>
        .dam-template { display: none; background: white; color: black; font-family: 'Outfit', sans-serif; }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "historial"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Seguimiento operativo</span>
            </div>
            
            <button onclick="window.location.href='evaluacion.jsp'" class="primary-button text-xs font-black uppercase tracking-widest">
                + Nueva ruta
            </button>
        </header>

        <!-- Main Content Area -->
        <div class="p-12 max-w-7xl mx-auto space-y-8 w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">&larr; Volver a Importar paso a paso</a>

            <!-- Page Title Header -->
            <div class="flex justify-between items-end fade-up">
                <div>
                    <span class="pill-heading">Panel de operaciones</span>
                    <h2 class="text-4xl font-black mb-2 tracking-tight text-[var(--text-primary)] mt-3">Seguimiento</h2>
                    <p class="text-[var(--text-secondary)] text-sm font-semibold">Cada operacion muestra su estado actual, el siguiente paso y si ya esta lista para seguir.</p>
                </div>
            </div>

            <!-- Filter tabs matching design tokens -->
            <div class="flex flex-wrap gap-2 fade-up" style="animation-delay: 0.05s">
                <button onclick="filterTable('ALL')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-[var(--accent)] text-white transition-all filter-pill active cursor-pointer" id="pill-ALL">Todos</button>
                <button onclick="filterTable('Borrador')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer" id="pill-Borrador">Borrador</button>
                <button onclick="filterTable('Requiere permiso')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer" id="pill-Requiere">Requiere permiso</button>
                <button onclick="filterTable('Faltan documentos')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer" id="pill-Faltan">Faltan documentos</button>
                <button onclick="filterTable('Listas')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer" id="pill-Listas">Listas</button>
                <button onclick="filterTable('Cerradas')" class="px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer" id="pill-Cerradas">Cerradas</button>
            </div>

            <!-- Table Card in glassmorphism -->
            <div class="glass-card p-0 overflow-hidden fade-up" style="animation-delay: 0.1s">
                <table class="w-full text-left border-collapse">
                    <thead class="bg-[var(--surface-2)] text-[var(--text-secondary)] text-[10px] uppercase font-black tracking-widest border-b border-[var(--border)]">
                        <tr>
                            <th class="px-8 py-5">Operacion</th>
                            <th class="px-8 py-5">Código</th>
                            <th class="px-8 py-5">Entidad</th>
                            <th class="px-8 py-5">Estado</th>
                            <th class="px-8 py-5 text-center">Documentos</th>
                            <th class="px-8 py-5">Siguiente paso</th>
                            <th class="px-8 py-5 text-center">Acciones</th>
                        </tr>
                    </thead>
                    <tbody class="divide-y divide-[var(--border)] text-[var(--text-secondary)] font-semibold text-xs" id="evaluationsTableBody">
                        <%
                            Integer usuarioId = (Integer) session.getAttribute("usuarioId");
                            com.importease.proyecto.service.ImportacionService importService = new com.importease.proyecto.service.ImportacionService();
                            List<Importacion> lista = importService.listarPorUsuario(usuarioId);

                            if (lista != null && !lista.isEmpty()) {
                                for (Importacion imp : lista) {
                                    String code = imp.getHsCode();
                                    
                                    String entity = "--";
                                    if (code != null) {
                                        if (code.startsWith("8517")) entity = "MTC";
                                        else if (code.startsWith("2106") || code.startsWith("1901")) entity = "DIGESA";
                                        else if (code.startsWith("3004") || code.startsWith("3304") || code.startsWith("9018")) entity = "DIGEMID";
                                        else if (code.startsWith("0602") || code.startsWith("1209")) entity = "SENASA";
                                        else if (code.startsWith("4407")) entity = "SERFOR";
                                    }
                                    
                                    String rawState = imp.getEstado();
                                    if (rawState == null) rawState = "BORRADOR";
                                    
                                    String stateLabel = "Borrador";
                                    String stateClass = "text-[var(--text-secondary)] bg-[var(--surface-2)] border-[var(--border)]";
                                    String docFraction = "0/6";
                                    String nextAction = "Completar información";
                                    boolean isListable = false;
                                    
                                    if (rawState.equals("BORRADOR") || rawState.equals("COTIZACION")) {
                                        stateLabel = "Borrador";
                                        stateClass = "text-[var(--text-secondary)] bg-[var(--surface-2)] border-[var(--border)]";
                                        docFraction = "0/6";
                                        nextAction = "Completar información";
                                    } else if (!entity.equals("--") && !rawState.equals("LISTA_DESPACHO") && !rawState.equals("NACIONALIZADA")) {
                                        stateLabel = "Requiere permiso";
                                        stateClass = "text-[var(--warning)] bg-[var(--warning-soft)] border-[var(--warning)]";
                                        docFraction = "2/6";
                                        nextAction = "Preparar expediente";
                                    } else if (rawState.equals("PENDIENTE_DOCS")) {
                                        stateLabel = "En revisión";
                                        stateClass = "text-[var(--accent)] bg-[var(--accent-soft)] border-[var(--accent)]";
                                        docFraction = "1/6";
                                        nextAction = "Confirmar composición";
                                    } else {
                                        stateLabel = "Lista";
                                        stateClass = "text-[var(--success)] bg-[var(--success-soft)] border-[var(--success)]";
                                        docFraction = "6/6";
                                        nextAction = "Descargar reporte";
                                        isListable = true;
                                    }
                        %>
                        <tr class="hover:bg-[var(--surface-2)]/30 transition-all table-row-item border-b border-[var(--border)]" data-state-label="<%= stateLabel %>" data-raw-state="<%= rawState %>">
                            <td class="px-8 py-5">
                                <p class="text-sm font-black text-[var(--text-primary)]"><%= HtmlUtil.escape(imp.getProductoDesc() != null ? imp.getProductoDesc() : "Mercancía General") %></p>
                                <p class="text-[9px] text-[var(--text-tertiary)] font-bold mt-1 uppercase tracking-widest">OP-<%= String.format("%05d", imp.getId()) %></p>
                            </td>
                            <td class="px-8 py-5 font-mono text-[11px] text-[var(--text-secondary)] font-bold">
                                <%= HtmlUtil.escape(code != null ? code : "Sin Partida") %>
                            </td>
                            <td class="px-8 py-5 font-black text-[var(--text-tertiary)] uppercase">
                                <%= entity %>
                            </td>
                            <td class="px-8 py-5">
                                <span class="px-2.5 py-0.5 rounded-full text-[9px] font-black uppercase border <%= stateClass %>"><%= stateLabel %></span>
                            </td>
                            <td class="px-8 py-5 text-center text-[var(--text-primary)] font-bold">
                                <%= docFraction %>
                            </td>
                            <td class="px-8 py-5 text-[var(--text-secondary)] font-semibold">
                                <%= nextAction %>
                            </td>
                            <td class="px-8 py-5 text-center">
                                <% if (isListable) { %>
                                <button data-imp-id="<%= imp.getId() %>" onclick='downloadDAMById(this.dataset.impId)' 
                                        class="bg-white hover:bg-[var(--surface-2)] border border-[var(--border)] text-[var(--text-primary)] text-[10px] font-black px-4 py-2 rounded-xl uppercase tracking-wider transition-all shadow-sm cursor-pointer">
                                    Ver
                                </button>
                                <% } else { %>
                                <button onclick="window.location.href='evaluacion.jsp'" class="bg-white hover:bg-[var(--accent-soft)] border border-[var(--border)] text-[var(--accent)] text-[10px] font-black px-4 py-2 rounded-xl uppercase tracking-wider transition-all shadow-sm cursor-pointer">
                                    Continuar
                                </button>
                                <% } %>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="7" class="px-8 py-24 text-center">
                                <div class="flex flex-col items-center justify-center opacity-40 gap-4">
                                    <span class="text-5xl">📁</span>
                                    <p class="text-base font-bold italic">No se han registrado evaluaciones en este periodo.</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </main>

    <!-- Hidden Template for PDF (Refined Premium Navy + Blue Brand Styling) -->
    <div id="damTemplate" class="dam-template p-10">
        <div style="border: 2px solid #e2e8f0; padding: 40px; background-color: #ffffff; border-radius: 20px; box-shadow: 0 10px 30px rgba(15, 23, 42, 0.05);">
            <div style="display: flex; justify-content: space-between; border-bottom: 2px solid #f1f5f9; padding-bottom: 25px; margin-bottom: 30px;">
                <div>
                    <h1 style="font-size: 28px; font-weight: 900; color: #0f1729; margin: 0; letter-spacing: -1px; font-family: 'Outfit', sans-serif;">IMPORTEASE</h1>
                    <p style="font-size: 10px; margin: 6px 0; font-weight: 800; color: #3b82f6; text-transform: uppercase; letter-spacing: 0.1em;">Declaración Simplificada & Reporte de Importación</p>
                </div>
                <div style="text-align: right;">
                    <h2 style="font-size: 15px; margin: 0; color: #0f1729; font-weight: 800;">Expediente N° <span id="pdfDamNum" style="font-family: monospace; font-weight: 900;"></span></h2>
                    <p style="font-size: 8px; color: #94a3b8; margin: 6px 0; font-weight: 700; letter-spacing: 0.05em;">REPORTE DE VIABILIDAD ADUANERA PREMIUM</p>
                </div>
            </div>
            
            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 30px; font-size: 11px;">
                <div style="border: 1px solid #e2e8f0; padding: 15px; background: #f8f9fc; border-radius: 12px;">
                    <strong style="font-size: 8px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em;">Importador Evaluado:</strong>
                    <p id="pdfImportador" style="font-size: 13px; font-weight: 800; color: #0f1729; margin: 5px 0 0 0;"></p>
                </div>
                <div style="border: 1px solid #e2e8f0; padding: 15px; background: #f8f9fc; border-radius: 12px;">
                    <strong style="font-size: 8px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em;">Aduana Referencial:</strong>
                    <p style="font-size: 13px; font-weight: 800; color: #0f1729; margin: 5px 0 0 0;">MARÍTIMA DEL CALLAO (118)</p>
                </div>
            </div>

            <div style="border: 1px solid #e2e8f0; padding: 18px; margin-bottom: 30px; background: #ffffff; border-radius: 12px; font-size: 11px; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.02);">
                <strong style="font-size: 8px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em;">Información de la Mercancía</strong>
                <p id="pdfDesc" style="font-size: 14px; font-weight: 800; color: #0f1729; margin: 8px 0; text-transform: uppercase; letter-spacing: -0.2px;"></p>
                <div style="display: flex; gap: 60px; margin-top: 12px; border-top: 1px dashed #e2e8f0; padding-top: 10px;">
                    <div><span style="font-size: 8px; color: #94a3b8; font-weight: 700;">PARTIDA ARANCELARIA (HS):</span> <span id="pdfHS" style="font-family: monospace; font-weight: 800; color: #0f1729; font-size: 12px;"></span></div>
                    <div><span style="font-size: 8px; color: #94a3b8; font-weight: 700;">PAÍS DE ORIGEN:</span> <span id="pdfPais" style="font-weight: 800; color: #0f1729; font-size: 12px;"></span></div>
                </div>
            </div>

            <table style="width: 100%; border-collapse: collapse; margin-bottom: 30px; font-size: 11px; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;">
                <thead>
                    <tr style="background: #f1f3f9; color: #475569;">
                        <th style="border: 1px solid #e2e8f0; font-size: 8px; padding: 12px 15px; text-align: left; letter-spacing: 0.05em; font-weight: 800;">VALOR FOB (USD)</th>
                        <th style="border: 1px solid #e2e8f0; font-size: 8px; padding: 12px 15px; text-align: left; letter-spacing: 0.05em; font-weight: 800;">FLETE (USD)</th>
                        <th style="border: 1px solid #e2e8f0; font-size: 8px; padding: 12px 15px; text-align: left; letter-spacing: 0.05em; font-weight: 800;">SEGURO (USD)</th>
                        <th style="border: 1px solid #e2e8f0; font-size: 8px; padding: 12px 15px; text-align: left; letter-spacing: 0.05em; font-weight: 800; background: #e8ecf4;">VALOR CIF TOTAL (USD)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr style="background: white;">
                        <td id="pdfFob" style="border: 1px solid #e2e8f0; padding: 15px; font-size: 14px; font-weight: 700; color: #0f1729; font-family: monospace;"></td>
                        <td id="pdfFlete" style="border: 1px solid #e2e8f0; padding: 15px; font-size: 14px; font-weight: 700; color: #0f1729; font-family: monospace;"></td>
                        <td id="pdfSeguro" style="border: 1px solid #e2e8f0; padding: 15px; font-size: 14px; font-weight: 700; color: #0f1729; font-family: monospace;"></td>
                        <td id="pdfCif" style="border: 1px solid #e2e8f0; padding: 15px; font-size: 16px; font-weight: 900; color: #3b82f6; background: #f8f9fc; font-family: monospace;"></td>
                    </tr>
                </tbody>
            </table>

            <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 20px; font-size: 11px;">
                <div style="border: 1px solid #e2e8f0; padding: 20px; background: #ffffff; border-radius: 16px; box-shadow: 0 4px 12px rgba(15, 23, 42, 0.01);">
                    <strong style="font-size: 9px; text-transform: uppercase; color: #3b82f6; border-bottom: 2px solid #f1f5f9; display: block; padding-bottom: 8px; margin-bottom: 12px; letter-spacing: 0.05em;">Liquidación de Tributos Aproximados (PEN)</strong>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px; color: #475569;"><span>Ad-Valorem</span> <span id="pdfAv" style="font-weight: 700; color: #0f1729; font-family: monospace;"></span></div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px; color: #475569;"><span>IGV / IPM</span> <span id="pdfIgv" style="font-weight: 700; color: #0f1729; font-family: monospace;"></span></div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px; color: #475569;"><span>ISC</span> <span id="pdfIsc" style="font-weight: 700; color: #0f1729; font-family: monospace;"></span></div>
                    <div style="display: flex; justify-content: space-between; margin-bottom: 6px; color: #475569;"><span>Percepción SUNAT</span> <span id="pdfPer" style="font-weight: 700; color: #0f1729; font-family: monospace;"></span></div>
                    <div style="display: flex; justify-content: space-between; font-size: 14px; font-weight: 900; color: #3b82f6; margin-top: 15px; padding-top: 12px; border-top: 2px solid #f1f5f9;">
                        <span>TOTAL TRIBUTOS REFERENCIALES</span>
                        <span id="pdfTotal" style="font-family: monospace;"></span>
                    </div>
                </div>
                <div style="border: 1px solid #e2e8f0; padding: 20px; text-align: center; background: #f8f9fc; border-radius: 16px; display: flex; flex-direction: column; justify-content: center; align-items: center;">
                    <strong style="font-size: 8px; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.05em; font-weight: 700;">Riesgo estimado / canal probable</strong>
                    <div id="pdfCanal" style="font-size: 28px; font-weight: 900; margin-top: 10px; font-family: 'Outfit', sans-serif;"></div>
                </div>
            </div>

            <div style="margin-top: 40px; border-top: 1px solid #f1f5f9; padding-top: 20px; display: flex; justify-content: space-between; font-size: 8px; color: #94a3b8; font-weight: 700; text-transform: uppercase; letter-spacing: 0.05em;">
                <div>Evaluación digital para entrenamiento logístico</div>
                <div>Generado por ImportEase Asistente v4.5</div>
            </div>
        </div>
    </div>

    <!-- Scripting for Table Filter & PDF Generation -->
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        const importaciones = {};
        <% if (lista != null) { for (Importacion impJs : lista) { %>
        importaciones['<%= impJs.getId() %>'] = {
            numeroDam: 'OP-<%= String.format("%05d", impJs.getId()) %>',
            productoDesc: '<%= HtmlUtil.escape(impJs.getProductoDesc() != null ? impJs.getProductoDesc() : "Mercancía General") %>',
            hsCode: '<%= HtmlUtil.escape(impJs.getHsCode()) %>',
            paisOrigen: '<%= impJs.getPaisOrigen() != null ? HtmlUtil.escape(impJs.getPaisOrigen()) : "N/A" %>',
            fob: <%= impJs.getValorFob() %>,
            flete: <%= impJs.getFlete() %>,
            seguro: <%= impJs.getSeguro() %>,
            cif: <%= impJs.getValorCif() %>,
            adValoremAplicado: <%= impJs.getMontoAdValorem() %>,
            igvAplicado: <%= impJs.getMontoIgb() + impJs.getMontoIpm() %>,
            iscAplicado: <%= impJs.getMontoIsc() %>,
            percepcionAplicada: <%= impJs.getMontoPercepcion() %>,
            totalImpuestos: <%= impJs.getTotalImpuestos() %>,
            canalAsignado: '<%= impJs.getCanalAsignado() != null ? HtmlUtil.escape(impJs.getCanalAsignado()) : "PENDIENTE" %>'
        };
        <% } } %>

        function filterTable(filter) {
            // Adjust active pills
            const pills = document.querySelectorAll('.filter-pill');
            pills.forEach(p => p.className = "px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-white border border-[var(--border)] text-[var(--text-secondary)] hover:border-[var(--accent)] transition-all filter-pill cursor-pointer");
            
            let activeId = "pill-ALL";
            if (filter === 'Borrador') activeId = "pill-Borrador";
            else if (filter === 'Requiere permiso') activeId = "pill-Requiere";
            else if (filter === 'Faltan documentos') activeId = "pill-Faltan";
            else if (filter === 'Listas') activeId = "pill-Listas";
            else if (filter === 'Cerradas') activeId = "pill-Cerradas";
            
            const activePill = document.getElementById(activeId);
            if (activePill) {
                activePill.className = "px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-wider bg-[var(--accent)] text-white transition-all filter-pill active cursor-pointer";
            }
            
            // Loop through rows and filter
            const rows = document.querySelectorAll('.table-row-item');
            rows.forEach(row => {
                const state = row.getAttribute('data-state-label');
                const rawState = row.getAttribute('data-raw-state');
                
                let show = false;
                if (filter === 'ALL') {
                    show = true;
                } else if (filter === 'Borrador' && state === 'Borrador') {
                    show = true;
                } else if (filter === 'Requiere permiso' && state === 'Requiere permiso') {
                    show = true;
                } else if (filter === 'Faltan documentos' && rawState === 'PENDIENTE_DOCS') {
                    show = true;
                } else if (filter === 'Listas' && state === 'Lista') {
                    show = true;
                } else if (filter === 'Cerradas' && rawState === 'NACIONALIZADA') {
                    show = true;
                }
                
                row.style.display = show ? '' : 'none';
            });
        }

        function downloadDAMById(id) {
            const imp = importaciones[id];
            if (!imp) return;
            downloadDAM(imp);
        }

        function downloadDAM(imp) {
            document.getElementById('pdfDamNum').innerText = imp.numeroDam;
            document.getElementById('pdfImportador').innerText = '<%= HtmlUtil.escape(userNombre) %>'.toUpperCase();
            document.getElementById('pdfDesc').innerText = imp.productoDesc;
            document.getElementById('pdfHS').innerText = imp.hsCode;
            document.getElementById('pdfPais').innerText = imp.paisOrigen;
            document.getElementById('pdfFob').innerText = imp.fob.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfFlete').innerText = imp.flete.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfSeguro').innerText = imp.seguro.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfCif').innerText = imp.cif.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfAv').innerText = imp.adValoremAplicado.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfIgv').innerText = imp.igvAplicado.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfIsc').innerText = imp.iscAplicado.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfPer').innerText = imp.percepcionAplicada.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfTotal').innerText = imp.totalImpuestos.toLocaleString('en-US', {minimumFractionDigits: 2});
            document.getElementById('pdfCanal').innerText = imp.canalAsignado;
            document.getElementById('pdfCanal').style.color = imp.canalAsignado === 'VERDE' ? '#059669' : (imp.canalAsignado === 'ROJO' ? '#dc2626' : '#d97706');

            const element = document.getElementById('damTemplate');
            element.style.display = 'block';
            
            const opt = {
                margin: 5,
                filename: 'Expediente_' + imp.numeroDam + '.pdf',
                image: { type: 'jpeg', quality: 1 },
                html2canvas: { scale: 3, letterRendering: true, useCORS: true },
                jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' }
            };

            html2pdf().set(opt).from(element).save().then(() => {
                element.style.display = 'none';
            });
        }
    </script>
</body>
</html>
