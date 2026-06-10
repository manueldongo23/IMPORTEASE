<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%!
    private String escapeJs(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("<", "\\u003C").replace(">", "\\u003E").replace("&", "\\u0026");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Restablecer Contraseña</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/auth.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        (function() {
            if (localStorage.getItem('dark_mode') === 'true') {
                document.documentElement.classList.add('dark-mode');
            }
        })();
    </script>
</head>
<body class="min-h-screen flex items-center justify-center p-6 text-[var(--text-primary)] bg-[var(--surface-0)] font-['Outfit']">
<%@ include file="/WEB-INF/fragments/consent-banner.jsp" %>

    <!-- TOAST NOTIFICATION PREMIUM -->
    <div id="toastNotification" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 px-6 py-4.5 rounded-2xl border bg-[var(--surface-1)] shadow-2xl transition-all duration-500 opacity-0 pointer-events-none scale-95 max-w-md w-[calc(100%-3rem)]">
        <span id="toastIcon" class="text-xl"></span>
        <div class="flex-1">
            <p id="toastTitle" class="text-xs font-black uppercase tracking-wider"></p>
            <p id="toastMessage" class="text-[11px] text-[var(--text-secondary)] font-semibold mt-0.5"></p>
        </div>
    </div>

    <div class="glass-card w-full max-w-[440px] p-8 md:p-10 space-y-8 relative z-10 bg-[var(--surface-1)] border border-[var(--border)] shadow-2xl fade-up">
        <div class="space-y-4">
            <h2 class="text-3xl font-black tracking-tight text-[var(--text-primary)]">Nueva Contraseña</h2>
            <p class="text-[var(--text-secondary)] font-semibold text-xs leading-relaxed">Ingresa y confirma tu nueva clave de acceso corporativa.</p>
        </div>

        <form id="resetearForm" class="space-y-5">
            <!-- Email (deshabilitado/oculto) -->
            <div class="space-y-2">
                <label for="email" class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Email Destino</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)]">
                        <svg role="img" aria-label="Email" class="w-5 h-5 text-[var(--text-tertiary)]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                        </svg>
                    </div>
                    <input type="email" id="email" required readonly
                           class="w-full pl-14 pr-6 py-4 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)] text-sm text-[var(--text-secondary)] font-bold focus:outline-none">
                </div>
            </div>

            <!-- Password -->
            <div class="space-y-2">
                <label for="password" class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Nueva Contraseña</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg role="img" aria-label="Nueva contraseña" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                        </svg>
                    </div>
                    <input type="password" id="password" required 
                           class="w-full pl-14 pr-12 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                           placeholder="••••••••">
                    <button type="button" class="absolute inset-y-0 right-5 flex items-center text-[var(--text-tertiary)] hover:text-[var(--accent)] transition-colors toggle-pw-btn" data-target="password" aria-label="Mostrar contraseña">
                        <svg role="img" aria-label="Mostrar contraseña" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- Confirm Password -->
            <div class="space-y-2">
                <label for="confirmPassword" class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Confirmar Contraseña</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg role="img" aria-label="Confirmar contraseña" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                        </svg>
                    </div>
                    <input type="password" id="confirmPassword" required 
                           class="w-full pl-14 pr-12 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                           placeholder="••••••••">
                    <button type="button" class="absolute inset-y-0 right-5 flex items-center text-[var(--text-tertiary)] hover:text-[var(--accent)] transition-colors toggle-pw-btn" data-target="confirmPassword" aria-label="Mostrar contraseña">
                        <svg role="img" aria-label="Mostrar contraseña" class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                    </button>
                </div>
            </div>
            
            <button type="submit" id="btnGuardar" class="w-full py-4 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg transition-all transform active:scale-98">
                GUARDAR NUEVA CLAVE
            </button>
        </form>
    </div>

    <!-- CONFIGURATION & EXTERNAL LOGIC SCRIPTS -->
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        window.ImportEase = window.ImportEase || {};
        window.ImportEase.ctx = '<%= escapeJs(request.getContextPath()) %>';
        window.ImportEase.csrfToken = '<%= escapeJs(String.valueOf(request.getAttribute("csrfToken"))) %>';
        window.ImportEase.csrfHeader = '<%= escapeJs(String.valueOf(request.getAttribute("csrfHeader"))) %>';
        window.ctx = window.ImportEase.ctx;
        window.csrfToken = window.ImportEase.csrfToken;
    </script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/common.js" defer></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/auth-reset.js" defer></script>
</body>
</html>
