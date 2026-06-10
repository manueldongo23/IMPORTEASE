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
    <title>ImportEase - Asistente de Acompañamiento de Importación</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link rel="icon" type="image/svg+xml" href="favicon.svg">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/auth.css" rel="stylesheet">
</head>
<body class="auth-page auth-login">
<%@ include file="/WEB-INF/fragments/consent-banner.jsp" %>

    <!-- Toast notification -->
    <div id="toast" class="auth-toast">
        <span class="auth-toast-icon" id="toastIcon"></span>
        <div>
            <p class="auth-toast-title" id="toastTitle"></p>
            <p class="auth-toast-msg" id="toastMsg"></p>
        </div>
    </div>

    <div class="auth-panel-container">
        <!-- LEFT PANEL -->
        <div class="auth-panel-left">
            <!-- Live neural connection animation -->
            <canvas id="neuralCanvas"></canvas>
            <div class="auth-panel-left-overlay"></div>

            <!-- Floating Stats Widgets (Mapped directly to screenshot locations via IDs) -->
            <div class="auth-stat-widget" id="stat-despachos">
                <div class="auth-stat-icon green">
                    <svg role="img" aria-label="Checkmark" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/>
                    </svg>
                </div>
                <div class="auth-stat-info">
                    <div class="auth-stat-label">Despachos</div>
                    <div class="auth-stat-value">98.7%</div>
                    <div class="auth-stat-subtext">Efectividad</div>
                </div>
            </div>

            <div class="auth-stat-widget" id="stat-embarques">
                <div class="auth-stat-icon blue">
                    <svg role="img" aria-label="Grid" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A2.25 2.25 0 016 3.75h2.25A2.25 2.25 0 0110.5 6v2.25a2.25 2.25 0 01-2.25 2.25H6a2.25 2.25 0 01-2.25-2.25V6zM3.75 15.75A2.25 2.25 0 016 13.5h2.25a2.25 2.25 0 012.25 2.25V18a2.25 2.25 0 01-2.25 2.25H6A2.25 2.25 0 013.75 18v-2.25zM13.5 6a2.25 2.25 0 012.25-2.25H18A2.25 2.25 0 0120.25 6v2.25A2.25 2.25 0 0118 10.5h-2.25a2.25 2.25 0 01-2.25-2.25V6zM13.5 15.75a2.25 2.25 0 012.25-2.25H18a2.25 2.25 0 012.25 2.25V18A2.25 2.25 0 0118 20.25h-2.25A2.25 2.25 0 0113.5 18v-2.25z"/>
                    </svg>
                </div>
                <div class="auth-stat-info">
                    <div class="auth-stat-label">Embarques</div>
                    <div class="auth-stat-value">+12,450</div>
                </div>
            </div>

            <div class="auth-stat-widget" id="stat-cumplimiento">
                <div class="auth-stat-icon purple">
                    <svg role="img" aria-label="Shield" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                    </svg>
                </div>
                <div class="auth-stat-info">
                    <div class="auth-stat-label">Cumplimiento</div>
                    <div class="auth-stat-value">100%</div>
                    <div class="auth-stat-subtext">Regulatorio</div>
                </div>
            </div>

            <div class="auth-stat-widget" id="stat-tiempo">
                <div class="auth-stat-icon cyan">
                    <svg role="img" aria-label="Clock" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z"/>
                    </svg>
                </div>
                <div class="auth-stat-info">
                    <div class="auth-stat-label">Tiempo promedio</div>
                    <div class="auth-stat-value">24h</div>
                    <div class="auth-stat-subtext">Liberación</div>
                </div>
            </div>

            <div class="auth-panel-left-content">
                <!-- Brand header -->
                <div class="auth-brand">
                    <div class="auth-brand-logo">
                        <span class="auth-brand-logo-text">e</span>
                    </div>
                    <span class="auth-brand-name">ImportEase <span>Enterprise</span></span>
                </div>

                <!-- Hero headline section -->
                <div class="auth-hero-block">
                    <h1 class="auth-hero-title">
                        Logistics<br>
                        <span class="accent">Intelligence.</span>
                    </h1>
                    <p class="auth-hero-desc">
                        Plataforma de acompañamiento que guía a usuarios sin experiencia paso a paso en su primera importación: desde elegir el producto hasta recibirlo.
                    </p>

                    <!-- Features list -->
                    <div class="auth-features-strip">
                        <div class="auth-feature-item">
                            <div class="auth-feature-header auto">
                                <svg role="img" aria-label="Automatización" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12a7.5 7.5 0 0015 0m-15 0a7.5 7.5 0 1115 0m-15 0H3m16.5 0H21m-1.5 0H12m-8.457 3.077l1.41-1.41m12.102-12.102l1.41-1.41m0 14.152l-1.41-1.41M5.93 5.93l-1.41 1.41M12 3.75V3m0 16.5v.75m0-1.5V12"/>
                                </svg>
                                Automatización
                            </div>
                            <div class="auth-feature-desc">Flujos inteligentes</div>
                        </div>
                        <div class="auth-feature-item">
                            <div class="auth-feature-header vis">
                                <svg role="img" aria-label="Visibilidad" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                </svg>
                                Visibilidad
                            </div>
                            <div class="auth-feature-desc">Datos en tiempo real</div>
                        </div>
                        <div class="auth-feature-item">
                            <div class="auth-feature-header cum">
                                <svg role="img" aria-label="Cumplimiento" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12c0 1.268-.63 2.39-1.593 3.068a3.745 3.745 0 01-1.043 3.296 3.745 3.745 0 01-3.296 1.043A3.745 3.745 0 0112 21c-1.268 0-2.39-.63-3.068-1.593a3.746 3.746 0 01-3.296-1.043 3.745 3.745 0 01-1.043-3.296A3.745 3.745 0 013 12c0-1.268.63-2.39 1.593-3.068a3.745 3.745 0 011.043-3.296 3.746 3.746 0 013.296-1.043A3.746 3.746 0 0112 3c1.268 0 2.39.63 3.068 1.593a3.746 3.746 0 013.296 1.043 3.746 3.746 0 011.043 3.296A3.745 3.745 0 0121 12z"/>
                                </svg>
                                Cumplimiento
                            </div>
                            <div class="auth-feature-desc">Normativa asegurada</div>
                        </div>
                    </div>

                    <!-- Bottom security badge -->
                    <div class="auth-bottom-capsule">
                        <div class="auth-capsule-item">
                            <svg role="img" aria-label="Entorno seguro" class="auth-capsule-icon blue" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                            </svg>
                            Entorno corporativo seguro
                        </div>
                        <span class="divider">|</span>
                        <div class="auth-capsule-item">
                            <span class="auth-capsule-dot-green"></span>
                            Alta disponibilidad
                        </div>
                        <span class="divider">|</span>
                        <div class="auth-capsule-item">
                            <svg role="img" aria-label="Datos protegidos" class="auth-capsule-icon purple" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                            </svg>
                            Datos protegidos
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- RIGHT PANEL -->
        <div class="auth-panel-right">
            <div class="auth-card fade-up">
                <!-- Systems operational status badge -->
                <div class="auth-status-badge">
                    <div class="auth-status-dot"></div>
                    <span class="auth-status-text">Sistema Operacional</span>
                </div>

                <h2 class="auth-card-title">Bienvenido a ImportEase</h2>
                <p class="auth-card-subtitle">Ingresa tus credenciales de importador para continuar.</p>

                <form id="loginForm">
                    <!-- Email -->
                    <div class="auth-field">
                        <div class="auth-field-header">
                            <label class="auth-field-label" for="email">Email Corporativo</label>
                        </div>
                        <div class="auth-input-wrap">
                            <span class="auth-input-icon">
                                <svg role="img" aria-label="Email" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                                </svg>
                            </span>
                            <input type="email" id="email" class="auth-input" placeholder="ejemplo@empresa.com" required autocomplete="email">
                        </div>
                    </div>

                    <!-- Password -->
                    <div class="auth-field">
                        <div class="auth-field-header">
                            <label class="auth-field-label" for="password">Clave de Acceso</label>
                            <a href="recuperar.jsp" class="auth-field-link">Recuperar</a>
                        </div>
                        <div class="auth-input-wrap">
                            <span class="auth-input-icon">
                                <svg role="img" aria-label="Contraseña" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                                </svg>
                            </span>
                            <input type="password" id="password" class="auth-input" placeholder="••••••••" required autocomplete="current-password">
                            <button type="button" class="auth-btn-toggle-pw" id="togglePw" aria-label="Mostrar contraseña">
                                <svg role="img" aria-label="Mostrar contraseña" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                </svg>
                            </button>
                        </div>
                    </div>

                    <!-- Captcha (Verificación Humana) -->
                    <div class="auth-captcha-box">
                        <div class="auth-captcha-header">
                            <span class="auth-captcha-label">Verificación Humana</span>
                            <button type="button" class="auth-captcha-refresh" id="btnRefreshCaptcha">Actualizar</button>
                        </div>
                        <div class="auth-captcha-row">
                            <div class="auth-captcha-img-wrap">
                                <img id="captchaImg" src="captcha" alt="Código de verificación captcha">
                            </div>
                            <input type="text" id="captcha" class="auth-captcha-input" placeholder="INPUT ..." maxlength="5" required autocomplete="off" aria-label="Código de verificación captcha">
                        </div>
                    </div>

                    <!-- Secure Warning Capsule -->
                    <div class="auth-secure-capsule">
                        <svg role="img" aria-label="Escudo" class="shield-icon" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                        </svg>
                        <div class="auth-capsule-text">
                            <div class="auth-capsule-title">Acceso seguro y cifrado</div>
                            <div class="auth-capsule-desc">Tus datos están protegidos con encriptación de nivel empresarial.</div>
                        </div>
                        <svg role="img" aria-label="Candado" class="lock-icon" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                        </svg>
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" id="btnLogin" class="auth-button">
                        INGRESAR A IMPORTEASE
                        <svg role="img" aria-label="Flecha" class="arrow-icon" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/>
                        </svg>
                    </button>
                </form>

                <!-- Card Footer -->
                <div class="auth-card-footer">
                    <svg role="img" aria-label="Acceso seguro" class="auth-footer-shield" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                    </svg>
                    <span>Acceso seguro</span>
                    <span class="divider">•</span>
                    <span>Entorno corporativo</span>
                </div>
            </div>

            <!-- Subtle register link styled underneath card -->
            <div class="auth-sub-footer-link" id="auth-register-footer">
                ¿Sin cuenta? <a href="registro.jsp">Registrar Empresa</a>
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
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/auth-login.js" defer></script>
</body>
</html>
