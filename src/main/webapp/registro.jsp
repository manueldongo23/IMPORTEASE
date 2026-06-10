<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Enterprise Registration</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;500;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link rel="icon" type="image/svg+xml" href="favicon.svg">
    <link href="css/main.css" rel="stylesheet">
    <link href="css/auth.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        (function() { if (localStorage.getItem('dark_mode') === 'true') document.documentElement.classList.add('dark-mode'); })();
    </script>
</head>
<body class="auth-page auth-register">
<%@ include file="/WEB-INF/fragments/consent-banner.jsp" %>

    <!-- Toast notification overlay -->
    <div id="toastNotification" class="auth-toast">
        <span id="toastIcon" class="auth-toast-icon"></span>
        <div>
            <p id="toastTitle" class="auth-toast-title"></p>
            <p id="toastMessage" class="auth-toast-msg"></p>
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
                    <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
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
                    <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                    <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                    <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12a7.5 7.5 0 0015 0m-15 0a7.5 7.5 0 1115 0m-15 0H3m16.5 0H21m-1.5 0H12m-8.457 3.077l1.41-1.41m12.102-12.102l1.41-1.41m0 14.152l-1.41-1.41M5.93 5.93l-1.41 1.41M12 3.75V3m0 16.5v.75m0-1.5V12"/>
                                </svg>
                                Automatización
                            </div>
                            <div class="auth-feature-desc">Flujos inteligentes</div>
                        </div>
                        <div class="auth-feature-item">
                            <div class="auth-feature-header vis">
                                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                                    <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                </svg>
                                Visibilidad
                            </div>
                            <div class="auth-feature-desc">Datos en tiempo real</div>
                        </div>
                        <div class="auth-feature-item">
                            <div class="auth-feature-header cum">
                                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                            <svg class="auth-capsule-icon blue" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                            <svg class="auth-capsule-icon purple" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
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
                    <span class="auth-status-text">Registro Corporativo</span>
                </div>

                <h2 class="auth-card-title">Alta de Empresa</h2>
                <p class="auth-card-subtitle">Configura tu perfil de importador y accede a la red logística.</p>

                <form id="registroForm">
                    <!-- Grid for RUC & Razón Social -->
                    <div class="auth-grid auth-grid-2">
                        <!-- RUC -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="ruc">RUC (Registro Único)</label>
                                <span id="rucValidoBadge" class="auth-validation-badge">VÁLIDO</span>
                            </div>
                            <div class="auth-ruc-row">
                                <div class="auth-input-wrap" style="flex: 1;">
                                    <span class="auth-input-icon">
                                        <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 9a3 3 0 11-6 0 3 3 0 016 0zm6 3a9 9 0 11-18 0 9 9 0 0118 0z"/>
                                        </svg>
                                    </span>
                                    <input type="text" id="ruc" class="auth-input" placeholder="20123456789" required maxlength="11" autocomplete="off">
                                    <div id="rucCheck" class="auth-check-indicator">
                                        <svg fill="currentColor" viewBox="0 0 20 20">
                                            <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                                        </svg>
                                    </div>
                                </div>
                                <button type="button" id="btnValidarRuc" class="auth-btn-validate">Validar</button>
                            </div>
                            <div class="auth-validation-subtext" style="margin-top: 0.35rem; display: flex; align-items: center; gap: 0.25rem;">
                                <span id="rucIndicatorIcon" class="hidden text-emerald-500">
                                    <svg width="12" height="12" fill="currentColor" viewBox="0 0 20 20">
                                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                                    </svg>
                                </span>
                                <span id="rucValidationText">RUC pendiente de validación ante SUNAT.</span>
                            </div>
                        </div>

                        <!-- Razón Social -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="razonSocial">Razón Social</label>
                            </div>
                            <div class="auth-input-wrap">
                                <span class="auth-input-icon">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/>
                                    </svg>
                                </span>
                                <input type="text" id="razonSocial" class="auth-input readonly-state" placeholder="Automático al validar RUC" required readonly>
                                <div id="razonCheck" class="auth-check-indicator">
                                    <svg fill="currentColor" viewBox="0 0 20 20">
                                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                                    </svg>
                                </div>
                            </div>
                            <div class="auth-validation-subtext" style="margin-top: 0.35rem; display: flex; align-items: center; gap: 0.25rem;">
                                <span id="razonIndicatorIcon" class="hidden text-emerald-500">
                                    <svg width="12" height="12" fill="currentColor" viewBox="0 0 20 20">
                                        <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/>
                                    </svg>
                                </span>
                                <span id="razonValidationText">Razón social de la empresa.</span>
                            </div>
                        </div>
                    </div>

                    <!-- Diagnostic & Configuration -->
                    <div style="margin: 1.5rem 0 0.75rem 0;">
                        <span class="auth-section-header">Diagnóstico y Configuración de Perfil</span>
                    </div>
                    <div class="auth-grid auth-grid-2">
                        <!-- Nivel de Experiencia -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="nivelExperiencia">Nivel de Experiencia en Importación</label>
                            </div>
                            <div class="auth-input-wrap">
                                <span class="auth-input-icon">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25"/>
                                    </svg>
                                </span>
                                <select id="nivelExperiencia" class="auth-input cursor-pointer" required style="background: #ffffff; color: #1a1d2e; border: 1.5px solid #e2e8f0; border-radius: 12px; padding: 10px 14px 10px 42px; width: 100%; font-size: 0.8rem; font-weight: 600; outline: none; -webkit-appearance: none;">
                                    <option value="NUNCA" selected>Nunca he importado (Se activarán guías y consejos)</option>
                                    <option value="POCO">He importado 1-2 veces</option>
                                    <option value="FRECUENTE">Importo con frecuencia (Se simplificará la interfaz)</option>
                                </select>
                            </div>
                        </div>

                        <!-- Rol de Usuario -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="perfil">Rol en la Plataforma</label>
                            </div>
                            <div class="auth-input-wrap">
                                <span class="auth-input-icon">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"/>
                                    </svg>
                                </span>
                                <select id="perfil" class="auth-input cursor-pointer" required style="background: #ffffff; color: #1a1d2e; border: 1.5px solid #e2e8f0; border-radius: 12px; padding: 10px 14px 10px 42px; width: 100%; font-size: 0.8rem; font-weight: 600; outline: none; -webkit-appearance: none;">
                                    <option value="OPERADOR" selected>Operador (Crea y gestiona operaciones)</option>
                                    <option value="CONSULTOR">Consultor (Permisos de solo lectura)</option>
                                    <option value="ADMIN">Administrador (Gestión total de la cuenta)</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- Credentials section header -->
                    <div style="margin: 1.5rem 0 0.75rem 0;">
                        <span class="auth-section-header">Credenciales de Acceso</span>
                    </div>

                    <!-- Grid for Email & Password -->
                    <div class="auth-grid auth-grid-2">
                        <!-- Email -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="email">Email de Administrador</label>
                            </div>
                            <div class="auth-input-wrap">
                                <span class="auth-input-icon">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                                    </svg>
                                </span>
                                <input type="email" id="email" class="auth-input" placeholder="admin@empresa.com.pe" required autocomplete="email">
                            </div>
                        </div>

                        <!-- Password -->
                        <div class="auth-field">
                            <div class="auth-field-header">
                                <label class="auth-field-label" for="password">Clave de Acceso</label>
                            </div>
                            <div class="auth-input-wrap">
                                <span class="auth-input-icon">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 00-2 2zm10-10V7a4 4 0 00-8 0v4h8z"/>
                                    </svg>
                                </span>
                                <input type="password" id="password" class="auth-input" placeholder="••••••••" required>
                                <button type="button" class="auth-btn-toggle-pw" id="btnTogglePassword" aria-label="Mostrar contraseña">
                                    <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                                        <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                                    </svg>
                                </button>
                            </div>
                        </div>
                    </div>

                    <!-- Buen Contribuyente banner card -->
                    <div class="auth-contribuyente-card">
                        <div class="auth-contribuyente-icon">
                            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z"/>
                            </svg>
                        </div>
                        <div class="auth-contribuyente-info">
                            <span class="auth-contribuyente-title">Certificación de Buen Contribuyente</span>
                            <span class="auth-contribuyente-desc">Habilita beneficios arancelarios y prioridad en el despacho nacional ante SUNAT.</span>
                        </div>
                        <input type="checkbox" id="buenContribuyente" class="auth-contribuyente-checkbox">
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" id="btnRegister" class="auth-button">
                        GENERAR TOKEN DE ACCESO
                        <svg class="arrow-icon" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/>
                        </svg>
                    </button>
                </form>

                <!-- Card Footer -->
                <div class="auth-card-footer">
                    <svg class="auth-footer-shield" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                    </svg>
                    <span>Acceso seguro</span>
                    <span class="divider">•</span>
                    <span>Entorno corporativo</span>
                </div>
            </div>

            <!-- Subtle login link styled underneath card -->
            <div class="auth-sub-footer-link" id="auth-register-footer">
                ¿Ya tienes cuenta? <a href="login.jsp">Identifícate</a>
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
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/toast.js"></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/common.js" defer></script>
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/auth-register.js" defer></script>
</body>
</html>
