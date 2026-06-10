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
    String userNombre = (String) session.getAttribute("usuarioNombre");
    if (userNombre == null) userNombre = "Manuel";
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Expediente</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/documentos.css" rel="stylesheet">
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/toast.js"></script>
</head>
<body class="documentos-page flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "documentos");
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Expediente"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Documentos de importacion</span>
            </div>
            <div class="flex items-center gap-6">
                <div class="text-[10px] uppercase tracking-widest font-black text-gray-400 flex gap-6">
                    <span class="flex items-center gap-2"><span class="w-1.5 h-1.5 rounded-full bg-[var(--accent)]"></span> Expedientes activos</span>
                </div>
            </div>
        </header>

        <jsp:include page="/fragments/breadcrumb.jsp" />

        <!-- Main Workspace Contents -->
        <div class="p-12 max-w-7xl mx-auto space-y-10 w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest mb-4">&larr; Volver a Importar paso a paso</a>

            <!-- Loading Spinner -->
            <div id="loadingSpinner" class="hidden flex items-center justify-center p-8">
                <div class="w-8 h-8 border-2 border-[var(--accent)] border-t-transparent rounded-full animate-spin"></div>
                <span class="ml-3 text-xs font-bold text-[var(--text-secondary)]">Cargando...</span>
            </div>

            <!-- Title and Selector -->
            <div class="fade-up flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-[var(--border)] pb-6">
                <div>
                    <span class="pill-heading">Carpeta de trabajo</span>
                    <h2 class="text-4xl font-black text-[var(--text-primary)] tracking-tight mt-3">Documentos</h2>
                    <p class="text-[var(--text-secondary)] text-sm font-semibold mt-1">Prepara solo lo necesario para seguir: factura, transporte y, si aplica, origen.</p>
                </div>
                <div class="w-full md:w-[28rem]">
                    <label class="block text-[9px] uppercase tracking-widest font-black text-gray-400 mb-2">Seleccionar importacion</label>
                    <select id="importacionSelect" class="w-full px-5 py-3.5 bg-white border border-[var(--border)] rounded-xl text-xs text-[var(--text-primary)] font-semibold custom-input appearance-none bg-no-repeat focus:outline-none transition-all cursor-pointer">
                        <option value="" class="text-gray-400">Cargando tus operaciones...</option>
                    </select>
                </div>
            </div>

            <div class="glass-card hero-banner p-6 fade-up" style="animation-delay: 0.15s">
                <div class="flex items-center gap-4">
                    <div id="expedienteStatusIcon" class="w-12 h-12 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[var(--accent)] flex items-center justify-center font-black">OK</div>
                    <div>
                        <p class="text-[10px] font-black uppercase tracking-[0.22em] text-[var(--accent)]">Que falta</p>
                        <h3 id="expedienteStatusTitle" class="text-xl font-black text-[var(--text-primary)] mt-1">Selecciona una operacion</h3>
                        <p id="expedienteStatusText" class="text-sm text-[var(--text-secondary)] font-semibold mt-1">Te diremos si tu carpeta esta lista, si faltan documentos o si primero debes revisar permisos.</p>
                    </div>
                </div>
            </div>
            <!-- Folders Grid -->
            <div class="grid grid-cols-1 md:grid-cols-3 gap-6 fade-up" style="animation-delay: 0.1s">
                <!-- Factura -->
                <div id="card_FACTURA_COMERCIAL" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-[var(--accent-soft)] flex items-center justify-center text-[var(--accent)] border border-[var(--accent-glow)] font-black">
                            FC
                        </div>
                        <span id="badge_FACTURA_COMERCIAL" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Pendiente</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">Factura Comercial</h3>
                    <p id="desc_FACTURA_COMERCIAL" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" data-knowledge-key="factura_comercial" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" data-knowledge-key="factura_comercial" data-knowledge-subtitulo="Ejemplo rapido para novatos" data-knowledge-relacion="Imagina que tu proveedor te envia una factura con producto, cantidades, precio e incoterm. Ese es el ejemplo que debes validar." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" data-knowledge-key="factura_comercial" data-knowledge-etapa="Antes de embarcar" data-knowledge-subtitulo="Momento correcto para pedirla" data-knowledge-relacion="Pide la factura final cuando ya cerraste el valor y antes del embarque para que no haya diferencias." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                        <a href="incoterms-lab.jsp" class="primary-button text-[10px]">Ver quien paga envio</a>
                    </div>
                </div>
                

                <!-- BL / AWB -->
                <div id="card_BILL_OF_LADING" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-500 border border-gray-200 font-black">
                            BL
                        </div>
                        <span id="badge_BILL_OF_LADING" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Pendiente</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">BL / AWB</h3>
                    <p id="desc_BILL_OF_LADING" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" data-knowledge-key="bill_of_lading" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" data-knowledge-key="bill_of_lading" data-knowledge-subtitulo="Ejemplo rapido para novatos" data-knowledge-relacion="Si tu carga va por mar veras un BL. Si viaja por aire veras un AWB. Ambos sirven para respaldar el transporte." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" data-knowledge-key="bill_of_lading" data-knowledge-etapa="Antes de declarar" data-knowledge-subtitulo="Momento correcto para pedirlo" data-knowledge-relacion="Aparece despues del embarque y debe coincidir con tu factura y la mercancia real." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                    </div>
                </div>

                <!-- Certificados -->
                <div id="card_CERTIFICADO_ORIGEN" class="glass-card p-6 folder-card border-[var(--border)] bg-white">
                    <div class="flex justify-between items-start mb-6">
                        <div class="w-12 h-12 rounded-xl bg-gray-100 flex items-center justify-center text-gray-500 border border-gray-200 font-black">
                            CO
                        </div>
                        <span id="badge_CERTIFICADO_ORIGEN" class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-orange-50 text-orange-600 border border-orange-200">Pendiente</span>
                    </div>
                    <h3 class="text-sm font-black text-[var(--text-primary)] mb-1">Certificado de Origen</h3>
                    <p id="desc_CERTIFICADO_ORIGEN" class="text-[10px] text-gray-400 font-semibold">0 documentos</p>
                    <div class="mt-4 flex flex-wrap gap-2">
                        <button type="button" data-knowledge-key="certificado_origen" class="px-3 py-2 rounded-xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] text-[10px] font-black uppercase tracking-wider text-[var(--accent)]">Ver para que sirve</button>
                        <button type="button" data-knowledge-key="certificado_origen" data-knowledge-subtitulo="Ejemplo rapido para novatos" data-knowledge-relacion="Solo te conviene pedirlo cuando un TLC o una preferencia de origen realmente puede ayudarte a pagar menos arancel." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Ver ejemplo</button>
                        <button type="button" data-knowledge-key="certificado_origen" data-knowledge-etapa="Antes de declarar" data-knowledge-subtitulo="Momento correcto para pedirlo" data-knowledge-relacion="Debes pedirlo antes de declarar si piensas usar un beneficio de origen preferencial." class="px-3 py-2 rounded-xl bg-white border border-[var(--border)] text-[10px] font-black uppercase tracking-wider text-[var(--text-primary)]">Cuando lo necesito</button>
                    </div>
                </div>
            </div>

            <!-- Upload Area -->
            <div class="glass-card upload-shell p-10 fade-up" style="animation-delay: 0.2s">
                <div class="flex flex-col md:flex-row items-start md:items-center justify-between gap-6 mb-8 border-b border-[var(--border)]/50 pb-6">
                    <div>
                        <h3 class="text-lg font-black text-[var(--text-primary)]">Agregar documento</h3>
                        <p class="text-xs text-[var(--text-secondary)] mt-1">Sube solo lo necesario para completar la carpeta base.</p>
                    </div>
                    <div class="w-full md:w-80">
                        <label class="block text-[9px] uppercase tracking-widest font-black text-gray-400 mb-2">Que documento vas a subir</label>
                        <select id="tipoDocSelect" class="w-full px-5 py-3 bg-white border border-[var(--border)] rounded-xl text-xs text-[var(--text-primary)] font-semibold custom-input appearance-none bg-no-repeat focus:outline-none transition-all cursor-pointer">
                            <option value="FACTURA_COMERCIAL">Factura Comercial</option>
                            <option value="BILL_OF_LADING">BL / AWB</option>
                            <option value="CERTIFICADO_ORIGEN">Certificado de Origen</option>
                        </select>
                    </div>
                </div>

                <div id="dropZone" class="dropzone-shell border-2 border-dashed border-[var(--border)] rounded-2xl p-14 text-center hover:border-[var(--accent)]/50 hover:bg-[var(--accent-soft)] transition-all cursor-pointer group">
                    <div class="w-16 h-16 mx-auto bg-[var(--accent-soft)] rounded-full flex items-center justify-center text-2xl text-[var(--accent)] group-hover:bg-[var(--accent-glow)] transition-all mb-4">
                        +
                    </div>
                    <h4 class="text-base font-bold text-[var(--text-primary)] mb-1">Arrastra y suelta tu archivo aqui</h4>
                    <p class="text-xs text-[var(--text-secondary)] mb-6">o haz clic para explorar en tu computadora (PDF, JPG, PNG)</p>
                    <button class="primary-button text-[10px]">
                        Seleccionar Archivo
                    </button>
                    <input type="file" class="hidden" id="fileInput">
                </div>
            </div>

            <!-- Recent Files Table -->
            <div class="glass-card records-shell overflow-hidden fade-up" style="animation-delay: 0.3s">
                <div class="p-6 border-b border-[var(--border)] flex justify-between items-center bg-[var(--surface-2)]">
                    <h3 class="text-sm font-black text-[var(--text-primary)] uppercase tracking-wider">Documentos de la operacion</h3>
                    <span id="checkSummary" class="text-[10px] text-[var(--text-secondary)] font-bold bg-[var(--surface-1)] border border-[var(--border)] px-3 py-1 rounded-lg">Selecciona una operacion</span>
                </div>
                <div class="overflow-x-auto">
                    <table class="w-full text-left border-collapse">
                        <thead>
                            <tr class="text-[10px] uppercase tracking-widest text-[var(--text-secondary)] bg-[var(--surface-2)] border-b border-[var(--border)]">
                                <th class="px-8 py-4">Documento</th>
                                <th class="px-8 py-4">Estado</th>
                                <th class="px-8 py-4">Nombre de Archivo</th>
                                <th class="px-8 py-4">Fecha</th>
                                <th class="px-8 py-4 text-right">Acciones</th>
                            </tr>
                        </thead>
                        <tbody id="documentosTableBody" class="divide-y divide-[var(--border)] text-xs font-semibold text-[var(--text-secondary)]">
                            <tr>
                                <td colspan="5" class="px-8 py-12 text-center text-gray-400">Cargando documentos...</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </main>

    <!-- Configuración dinámica — únicas expresiones JSP permitidas en scripts -->
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        window.ImportEase = window.ImportEase || {};
        window.ImportEase.ctx        = '<%= escapeJs(request.getContextPath()) %>';
        window.ImportEase.csrfToken  = '<%= escapeJs(request.getAttribute("csrfToken") != null ? String.valueOf(request.getAttribute("csrfToken")) : "") %>';
        window.ImportEase.csrfHeader = '<%= escapeJs(request.getAttribute("csrfHeader") != null ? String.valueOf(request.getAttribute("csrfHeader")) : "X-CSRF-TOKEN") %>';
        // Alias legacy
        window.ctx       = window.ImportEase.ctx;
        window.csrfToken = window.ImportEase.csrfToken;
    </script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/documentos.js" defer></script>

    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/knowledge-base.js"></script>
</body>
</html>
