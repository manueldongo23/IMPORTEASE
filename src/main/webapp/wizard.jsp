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
    <title>ImportEase - Wizard Guiado</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/utilidades.css" rel="stylesheet">
    <link href="css/componentes.css" rel="stylesheet">
    <link href="css/importacion-guiada.css" rel="stylesheet">
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)]">
    <% request.setAttribute("activePage", "aduanas"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar">
        <header class="h-16 border-b border-[var(--border)] px-8 flex items-center justify-between bg-white/70 backdrop-blur-xl sticky top-0 z-10">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-widest">Wizard Guiado</span>
            </div>
            <div class="flex items-center gap-3">
                <a href="expediente-aduanero.jsp" class="soft-button text-xs">Volver al expediente</a>
            </div>
        </header>

        <section class="p-8 xl:p-10 max-w-7xl mx-auto space-y-7">
            <div class="glass-card hero-banner p-8">
                <p class="pill-heading">Flujo guiado de importacion</p>
                <h1 id="wizardTitle" class="text-4xl font-black tracking-tight mt-3">Prepara tu importacion paso a paso</h1>
                <p id="wizardSubtitle" class="text-sm text-[var(--text-secondary)] font-semibold mt-3 max-w-3xl">
                    Sigue los 8 pasos para completar tu expediente aduanero.
                </p>

                <div id="stepRail" class="grid grid-cols-2 md:grid-cols-4 xl:grid-cols-8 gap-2 mt-7"></div>

                <div class="flex items-center gap-4 mt-4">
                    <select id="operacionSelect" class="custom-input px-4 py-2.5 rounded-2xl bg-white border border-[var(--border)] text-xs font-black text-[var(--text-secondary)] min-w-[18rem]">
                        <option value="">Selecciona un expediente</option>
                    </select>
                    <button id="btnRefresh" class="primary-button text-xs">Actualizar</button>
                </div>
            </div>

            <section class="grid grid-cols-1 xl:grid-cols-[1fr_22rem] gap-6">
                <div class="space-y-6">
                    <div id="stepContent" class="glass-card section-shell p-8">
                        <div class="flex items-center justify-center min-h-[12rem] text-[var(--text-secondary)]">
                            Selecciona un expediente para comenzar
                        </div>
                    </div>

                    <div id="helpBox" class="w-help p-6 rounded-2xl hidden">
                        <p id="helpTitle" class="font-black text-sm text-[var(--accent)]">Ayuda contextual</p>
                        <p id="helpDesc" class="text-sm mt-1"></p>
                    </div>

                    <div id="coherenciaPanel" class="hidden space-y-3"></div>
                </div>

                <aside class="space-y-6">
                    <div id="healthPanel" class="glass-card section-shell p-6">
                        <p class="pill-heading">Estado del expediente</p>
                        <div id="healthContent" class="mt-4 space-y-3 text-sm">
                            <p class="text-[var(--text-secondary)]">Cargando...</p>
                        </div>
                    </div>

                    <div id="nextActionPanel" class="glass-card section-shell p-6 bg-[var(--accent-soft)]">
                        <p class="pill-heading">Siguiente accion</p>
                        <div id="nextActionContent" class="mt-4 text-sm">
                            <p class="text-[var(--text-secondary)]">Selecciona un expediente</p>
                        </div>
                    </div>
                </aside>
            </section>
        </section>
    </main>

    <!-- Configuración dinámica — únicas expresiones JSP permitidas en scripts -->
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        window.ImportEase = window.ImportEase || {};
        window.ImportEase.ctx        = '<%= escapeJs(request.getContextPath()) %>';
        window.ImportEase.csrfToken  = '<%= escapeJs(request.getAttribute("csrfToken") != null ? String.valueOf(request.getAttribute("csrfToken")) : "") %>';
        window.ImportEase.csrfHeader = '<%= escapeJs(request.getAttribute("csrfHeader") != null ? String.valueOf(request.getAttribute("csrfHeader")) : "X-CSRF-TOKEN") %>';
        // Compatibilidad legacy
        window.ctx       = window.ImportEase.ctx;
        window.csrfToken = window.ImportEase.csrfToken;
    </script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/importacion-guiada.js"></script>
</body>
</html>
