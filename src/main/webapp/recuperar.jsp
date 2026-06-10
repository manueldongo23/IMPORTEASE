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
    <title>ImportEase - Recuperar Contraseña</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link rel="icon" type="image/svg+xml" href="favicon.svg">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/auth.css" rel="stylesheet">
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
        (function() { if (localStorage.getItem('dark_mode') === 'true') document.documentElement.classList.add('dark-mode'); })();
    </script>
</head>
<body class="auth-page auth-recovery">
<%@ include file="/WEB-INF/fragments/consent-banner.jsp" %>

    <!-- Toast notification overlay -->
    <div id="toastNotification" class="auth-toast">
        <span id="toastIcon" class="auth-toast-icon"></span>
        <div>
            <p id="toastTitle" class="auth-toast-title"></p>
            <p id="toastMessage" class="auth-toast-msg"></p>
        </div>
    </div>

    <!-- Success Modal Overlay -->
    <div id="successModal" class="auth-modal-overlay">
        <div class="auth-modal-box">
            <div class="auth-modal-header">
                <div class="auth-modal-header-left">
                    <div class="auth-modal-badge">I</div>
                    <h4 id="modalTitle">ImportEase — Solicitud registrada</h4>
                </div>
                <span class="chip">Recibidos</span>
            </div>

            <div class="auth-modal-email-preview">
                <div class="auth-modal-email-row">
                    <div><strong>De:</strong> <span>soporte@importease.com</span></div>
                    <span class="time">Ahora mismo</span>
                </div>
                <div class="auth-modal-body">
                    <h5>Restablece tu contraseña de ImportEase</h5>
                    <p id="modalBodyText">Hemos recibido tu solicitud. Si el correo existe en nuestra base, recibirás un enlace seguro en los próximos minutos.</p>
                    <div class="info-box" id="modalInfoBox">
                        <svg role="img" aria-label="Advertencia" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
                        </svg>
                        <span id="modalInfoText">Revisa también tu carpeta de spam o correo no deseado. El enlace expira en 15 minutos.</span>
                    </div>
                </div>
            </div>

            <div class="auth-modal-actions">
                <button id="modalPrimaryBtn">Entendido</button>
                <button type="button" class="secondary" id="btnRecuperarCloseModal">Cerrar</button>
            </div>

            <p class="auth-modal-footer-note">Por seguridad, el enlace solo se entrega por correo electrónico.</p>
        </div>
    </div>

    <div class="auth-panel-container">
        <!-- LEFT PANEL -->
        <div class="auth-panel-left">
            <canvas id="neuralCanvas"></canvas>
            <div class="auth-panel-left-overlay"></div>

            <div class="auth-panel-left-content">
                <!-- Logo -->
                <div class="auth-brand">
                    <div class="auth-brand-logo">
                        <span class="auth-brand-logo-text">e</span>
                    </div>
                    <span class="auth-brand-name">ImportEase <span>Enterprise</span></span>
                </div>

                <div class="auth-hero-block">
                    <!-- Priority pill -->
                    <div class="priority-badge">
                        <svg role="img" aria-label="Seguridad" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                        </svg>
                        Tu seguridad, nuestra prioridad
                    </div>

                    <h1 class="auth-hero-title">Recupera el acceso a<br/>tu cuenta de forma<br/><span class="accent">segura</span></h1>
                    <div class="auth-hero-bar"></div>
                    <p class="auth-hero-desc">Te ayudamos a restablecer tu contraseña de manera rápida y protegida para que puedas volver a lo que importa.</p>

                    <!-- Vertical Features Stack -->
                    <div class="auth-features-stack">
                        <div class="auth-stack-item">
                            <div class="auth-stack-icon-wrap">
                                <svg role="img" aria-label="Enlace seguro" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                                </svg>
                            </div>
                            <div class="auth-stack-text-wrap">
                                <div class="auth-stack-title">Enlace seguro</div>
                                <div class="auth-stack-desc">Generamos un enlace único y encriptado.</div>
                            </div>
                        </div>

                        <div class="auth-stack-item">
                            <div class="auth-stack-icon-wrap">
                                <svg role="img" aria-label="Tiempo" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                </svg>
                            </div>
                            <div class="auth-stack-text-wrap">
                                <div class="auth-stack-title">Válido por 15 min</div>
                                <div class="auth-stack-desc">El enlace expira por tu seguridad.</div>
                            </div>
                        </div>

                        <div class="auth-stack-item">
                            <div class="auth-stack-icon-wrap">
                                <svg role="img" aria-label="Protección corporativa" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 21V18a1.5 1.5 0 00-1.5-1.5H6A1.5 1.5 0 004.5 18v3M10.5 3.5a1.5 1.5 0 013 0V4H18.25m-12.5 0H6m6-3V21m3.656-11.844a4.5 4.5 0 00-7.312 0"/>
                                </svg>
                            </div>
                            <div class="auth-stack-text-wrap">
                                <div class="auth-stack-title">Protección corporativa</div>
                                <div class="auth-stack-desc">Cumplimos con los más altos estándares de seguridad.</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- RIGHT PANEL -->
        <div class="auth-panel-right">
            <!-- Back to top-right link -->
            <a href="login.jsp" class="auth-top-back-link">
                <svg role="img" aria-label="Volver" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5L3 12m0 0l7.5-7.5M3 12h18"/>
                </svg>
                Volver al inicio
            </a>

            <div class="auth-card">
                <!-- Mail badge icon -->
                <div class="auth-mail-badge-container">
                    <div class="auth-mail-dashed-circle">
                        <svg role="img" aria-label="Correo" class="auth-mail-icon" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                        </svg>
                        <div class="auth-mail-shield-badge">
                            <svg role="img" aria-label="Escudo" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                            </svg>
                        </div>
                    </div>
                </div>

                <h2 class="auth-card-title center">Restablece tu contraseña</h2>
                <div class="title-accent-line"></div>
                <p class="auth-card-subtitle center">Ingresa el correo asociado a tu cuenta y te enviaremos un enlace seguro para recuperar el acceso.</p>

                <form id="recuperarForm" class="auth-field">
                    <label class="auth-field-label" for="email">Correo electrónico</label>
                    <div class="auth-input-wrap">
                        <div class="auth-input-icon">
                            <svg role="img" aria-label="Correo" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                            </svg>
                        </div>
                        <input type="email" id="email" class="auth-input" required placeholder="manuelandrepo@gmail.com" autocomplete="email">
                        <div class="auth-checkmark-icon">
                            <svg role="img" aria-label="Verificado" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z"/>
                            </svg>
                        </div>
                    </div>

                    <button type="submit" id="btnEnviar" class="auth-button">
                        Enviar enlace de recuperación
                        <div class="auth-circle-arrow">
                            <svg role="img" aria-label="Enviar" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/>
                            </svg>
                        </div>
                    </button>
                </form>

                <!-- Divider with lock -->
                <div class="auth-card-divider">
                    <div class="line"></div>
                    <svg role="img" aria-label="Candado" class="auth-card-divider-lock" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                    </svg>
                    <div class="line"></div>
                </div>

                <p class="auth-footer-text">¿Ya recordaste tu contraseña? <a href="login.jsp">Inicia sesión</a></p>
            </div>
        </div>
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
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/auth-recovery.js" defer></script>
</body>
</html>