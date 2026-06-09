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
    String userRuc    = (String) session.getAttribute("usuarioRuc");
    String userNombre = (String) session.getAttribute("usuarioNombre");
    String userNombreDisplay = (userNombre != null && !userNombre.trim().isEmpty()) ? userNombre : "Usuario";
    String initials2 = "US";
    String[] nameParts = userNombreDisplay.trim().split("\\s+");
    if (nameParts.length >= 2) {
        initials2 = (nameParts[0].substring(0,1) + nameParts[1].substring(0,1)).toUpperCase();
    } else if (nameParts.length == 1 && nameParts[0].length() >= 1) {
        initials2 = nameParts[0].substring(0, Math.min(2, nameParts[0].length())).toUpperCase();
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Inicio</title>
    <meta name="description" content="Importa paso a paso con ImportEase. Fácil, segura y sin errores.">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;600;700;800;900&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">

<link href="css/utilidades.css" rel="stylesheet">
<link href="css/componentes.css" rel="stylesheet">
<link href="css/dashboard.css" rel="stylesheet">
</head>

<body class="flex h-screen overflow-hidden" style="font-family:'Outfit',sans-serif;background:#f5f6fa;">
    <% request.setAttribute("activePage", "dashboard"); %>
<%
    List<Map<String,String>> crumbs = new ArrayList<>();
    crumbs.add(java.util.Map.of("url","dashboard.jsp","label","Inicio"));
    crumbs.add(java.util.Map.of("label","Panel"));
    request.setAttribute("breadcrumb", crumbs);
%>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 flex flex-col overflow-hidden">

        <!-- ══════════ TOPBAR ══════════ -->
        <div class="ie-topbar" style="display:flex; align-items:center; justify-content:flex-end; gap:16px; padding:16px 28px; background:#ffffff; border-bottom:1px solid #e5e8f5; flex-shrink:0;">
            <!-- Botón empezar -->
            <a href="evaluacion.jsp?step=1" class="ie-btn-start" id="db-start-btn" style="display:inline-flex; align-items:center; gap:8px; background:#5B50F0; color:#ffffff; font-size:0.85rem; font-weight:700; padding:10px 18px; border-radius:12px; text-decoration:none; transition:background 0.2s;">
                <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/>
                </svg>
                Empezar importación
            </a>
            <!-- Notificaciones -->
            <a href="seguimiento.jsp" class="ie-btn-notif" title="Notificaciones" id="db-notif-btn" style="display:flex; align-items:center; justify-content:center; width:40px; height:40px; border-radius:50%; color:#64748b; background:transparent; border:none; transition:background-color 0.2s;">
                <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0"/>
                </svg>
            </a>
            <!-- Avatar -->
            <div class="ie-avatar-wrap">
                <div class="ie-avatar" id="ie-avatar-btn" style="display:flex; align-items:center; gap:8px; cursor:pointer; user-select:none; padding:4px; border-radius:50%;">
                    <div class="ie-avatar-circle" style="width:40px; height:40px; border-radius:50%; background:#EEF0FB; color:#5B50F0; font-size:0.85rem; font-weight:800; display:flex; align-items:center; justify-content:center;"><%= com.importease.proyecto.service.HtmlUtil.escape(initials2) %></div>
                    <svg class="ie-avatar-caret" width="14" height="14" fill="none" stroke="#64748b" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5"/></svg>
                </div>
                <div class="ie-user-dd" id="ie-user-dd">
                    <div class="ie-user-dd-header">
                        <p><%= com.importease.proyecto.service.HtmlUtil.escape(userNombreDisplay) %></p>
                        <p>Sesión activa</p>
                    </div>
                    <a href="evaluacion.jsp?step=1">
                        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/></svg>
                        Nueva importación
                    </a>
                    <a href="seguimiento.jsp">
                        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7.5 14.25v2.25m3-4.5v4.5m3-6.75v6.75m3-9v9M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z"/></svg>
                        Mis importaciones
                    </a>
                    <div class="dd-sep"></div>
                    <button id="header-logout-btn">
                        <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75"/></svg>
                        Cerrar sesión
                    </button>
                </div>
            </div>
        </div>

        <!-- ══════════ SCROLL ══════════ -->
        <div class="ie-scroll custom-scrollbar">

            <!-- ── HERO ── -->
            <div class="ie-hero ie-anim-1">
                <!-- Texto izquierda -->
                <div style="flex: 1; min-w-0;">
                    <div class="ie-hero-badge">👋 Bienvenido a ImportEase</div>
                    <h1>Importa <span class="purple">paso a paso,</span><br>sin complicarte</h1>
                    <p class="ie-hero-desc">Te guiamos en cada etapa para que tu primera importación sea fácil, segura y sin errores.</p>
                    <div class="ie-hero-btns" style="display:flex; gap:12px; align-items:center; flex-wrap:wrap; margin-bottom:0;">
                        <a href="evaluacion.jsp?step=1" class="ie-btn-start" id="heroCta-noDraft" style="background:#5B50F0; padding:12px 24px; border-radius:12px; font-weight:700; font-size:0.9rem; text-decoration:none; display:inline-flex; align-items:center; gap:6px;">
                            Empezar mi importación
                            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/></svg>
                        </a>
                        <a href="evaluacion.jsp?step=1" class="ie-hero-btn-outline" id="heroCta-draft" style="background:#ffffff; border:1.5px solid #5B50F0; color:#5B50F0; padding:12px 24px; border-radius:12px; font-weight:700; font-size:0.9rem; text-decoration:none; display:inline-flex; align-items:center; gap:6px;">
                            Continuar proceso
                            <svg width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5.25 5.653c0-.856.917-1.398 1.667-.986l11.54 6.348a1.125 1.125 0 010 1.971l-11.54 6.347a1.125 1.125 0 01-1.667-.985V5.653z"/></svg>
                        </a>
                    </div>
                </div>

                <!-- Ilustración barco + globo -->
                <div class="ie-hero-ship">
                    <img src="css/hero_ship.png" alt="Barco de importación con globo terráqueo">
                </div>

                <!-- Tarjeta de progreso -->
                <div class="ie-progress-card">
                    <p class="ie-progress-card-label">Tu progreso</p>
                    <div class="ie-progress-pct" id="ie-prog-pct">0%</div>
                    <div class="ie-progress-bar-wrap">
                        <div class="ie-progress-bar-fill" id="ie-prog-bar" style="width:0%"></div>
                    </div>
                    <span class="ie-progress-note" id="ie-prog-note">Aún no has iniciado</span>
                </div>
            </div>

            <!-- ── GRID INFERIOR ── -->
            <div class="ie-grid-bottom">

                <!-- COLUMNA IZQUIERDA: pasos + banner -->
                <div class="ie-anim-2">
                    <!-- Título sección -->
                    <div class="ie-steps-section-title" style="display:flex; align-items:center; gap:8px; font-size:0.8rem; font-weight:700; color:#5B50F0; text-transform:uppercase; letter-spacing:0.07em; margin-bottom:12px;">
                        <svg width="16" height="16" fill="currentColor" viewBox="0 0 24 24" style="color: #5B50F0;">
                            <path d="M12 2l2.4 7.2 7.2 2.4-7.2 2.4-2.4 7.2-2.4-7.2-7.2-2.4 7.2-2.4z"/>
                        </svg>
                        Así importas con ImportEase
                    </div>

                    <!-- Tarjeta de 3 pasos -->
                    <div class="ie-steps-card">
                        <div class="ie-steps-row">

                            <!-- PASO 1 -->
                            <a href="evaluacion.jsp?step=1" class="ie-step" id="step-card-1">
                                <div class="ie-step-num">1</div>
                                <div class="ie-step-icon-wrap">
                                    <!-- Ícono: laptop con descripción de producto -->
                                    <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" width="80" height="80">
                                        <circle cx="40" cy="40" r="36" fill="#EEF0FB"/>
                                        <!-- Monitor -->
                                        <rect x="18" y="22" width="44" height="28" rx="4" fill="#fff" stroke="#C4BFEE" stroke-width="1.5"/>
                                        <rect x="20" y="24" width="40" height="24" rx="3" fill="#F0EEFF"/>
                                        <!-- Líneas de texto -->
                                        <rect x="25" y="29" width="22" height="3" rx="1.5" fill="#5B50F0"/>
                                        <rect x="25" y="35" width="16" height="2.5" rx="1.25" fill="#A78BFA"/>
                                        <rect x="25" y="40" width="18" height="2.5" rx="1.25" fill="#C4B5FD"/>
                                        <!-- Bolsa de compras -->
                                        <rect x="46" y="27" width="11" height="13" rx="2" fill="#fff" stroke="#5B50F0" stroke-width="1.2"/>
                                        <path d="M49 30 Q51 27 54 30" stroke="#5B50F0" stroke-width="1.2" fill="none" stroke-linecap="round"/>
                                        <rect x="48" y="32" width="7" height="1.5" rx=".75" fill="#A78BFA"/>
                                        <!-- Base monitor -->
                                        <rect x="35" y="50" width="10" height="3" rx="1" fill="#C4BFEE"/>
                                        <rect x="29" y="52" width="22" height="2.5" rx="1.25" fill="#C4BFEE"/>
                                    </svg>
                                </div>
                                <h4>Describe tu producto</h4>
                                <p>Cuéntanos qué importarás.<br>Entre más detalles, mejor.</p>
                                <span class="ie-step-badge" id="step1-badge"><span class="ie-step-badge-dot"></span>Pendiente</span>
                            </a>

                            <!-- Flecha 1 -->
                            <div class="ie-step-arrow" style="width:32px; height:32px; border-radius:50%; background:#ffffff; border:1px solid #e5e8f5; display:flex; align-items:center; justify-content:center; flex-shrink:0; margin:0 4px;">
                                <svg width="16" height="16" fill="none" stroke="#94a3b8" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"/></svg>
                            </div>

                            <!-- PASO 2 -->
                            <a href="buscador.jsp" class="ie-step" id="step-card-2">
                                <div class="ie-step-num">2</div>
                                <div class="ie-step-icon-wrap">
                                    <!-- Ícono: lupa con código de barras -->
                                    <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" width="80" height="80">
                                        <circle cx="40" cy="40" r="36" fill="#EEF0FB"/>
                                        <!-- Lupa -->
                                        <circle cx="36" cy="36" r="14" stroke="#1a1d2e" stroke-width="2.5" fill="#fff"/>
                                        <line x1="46" y1="46" x2="56" y2="56" stroke="#1a1d2e" stroke-width="3" stroke-linecap="round"/>
                                        <!-- Barras de código dentro de la lupa -->
                                        <rect x="28" y="30" width="2.5" height="12" rx="1" fill="#5B50F0"/>
                                        <rect x="32" y="30" width="1.5" height="12" rx=".75" fill="#5B50F0"/>
                                        <rect x="35" y="30" width="3" height="12" rx="1" fill="#5B50F0"/>
                                        <rect x="40" y="30" width="1.5" height="12" rx=".75" fill="#5B50F0"/>
                                        <rect x="43" y="30" width="2" height="12" rx="1" fill="#5B50F0"/>
                                        <!-- Badge check azul -->
                                        <circle cx="54" cy="28" r="9" fill="#3B82F6"/>
                                        <path d="M50 28l3 3 6-6" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </div>
                                <h4>Revisa código y permisos</h4>
                                <p>Encontramos tu HS Code y validamos si necesitas permisos.</p>
                                <span class="ie-step-badge" id="step2-badge"><span class="ie-step-badge-dot"></span>Pendiente</span>
                            </a>

                            <!-- Flecha 2 -->
                            <div class="ie-step-arrow" style="width:32px; height:32px; border-radius:50%; background:#ffffff; border:1px solid #e5e8f5; display:flex; align-items:center; justify-content:center; flex-shrink:0; margin:0 4px;">
                                <svg width="16" height="16" fill="none" stroke="#94a3b8" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75"/></svg>
                            </div>

                            <!-- PASO 3 -->
                            <a href="calculadora-negocio.jsp" class="ie-step" id="step-card-3">
                                <div class="ie-step-num">3</div>
                                <div class="ie-step-icon-wrap">
                                    <!-- Ícono: clipboard + calculadora -->
                                    <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" width="80" height="80">
                                        <circle cx="40" cy="40" r="36" fill="#EEF0FB"/>
                                        <!-- Clipboard -->
                                        <rect x="20" y="22" width="28" height="36" rx="4" fill="#fff" stroke="#C4BFEE" stroke-width="1.5"/>
                                        <!-- Clip en la parte superior -->
                                        <rect x="28" y="18" width="12" height="7" rx="3" fill="#5B50F0"/>
                                        <!-- Checkboxes -->
                                        <rect x="25" y="30" width="5" height="5" rx="1.5" fill="#EEF0FB" stroke="#5B50F0" stroke-width="1.2"/>
                                        <path d="M26 32.5l1.5 1.5 2.5-2.5" stroke="#5B50F0" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"/>
                                        <rect x="33" y="31" width="12" height="2" rx="1" fill="#C4B5FD"/>
                                        <rect x="25" y="38" width="5" height="5" rx="1.5" fill="#EEF0FB" stroke="#5B50F0" stroke-width="1.2"/>
                                        <path d="M26 40.5l1.5 1.5 2.5-2.5" stroke="#5B50F0" stroke-width="1" stroke-linecap="round" stroke-linejoin="round"/>
                                        <rect x="33" y="39" width="10" height="2" rx="1" fill="#C4B5FD"/>
                                        <rect x="25" y="46" width="5" height="5" rx="1.5" fill="#EEF0FB" stroke="#C4BFEE" stroke-width="1.2"/>
                                        <rect x="33" y="47" width="8" height="2" rx="1" fill="#E0D9FF"/>
                                        <!-- Calculadora pequeña -->
                                        <rect x="48" y="36" width="16" height="20" rx="3" fill="#5B50F0"/>
                                        <rect x="50" y="38" width="12" height="5" rx="1.5" fill="#fff" opacity=".3"/>
                                        <rect x="51" y="45" width="3" height="3" rx="1" fill="#fff" opacity=".6"/>
                                        <rect x="56" y="45" width="3" height="3" rx="1" fill="#fff" opacity=".6"/>
                                        <rect x="51" y="50" width="3" height="3" rx="1" fill="#fff" opacity=".6"/>
                                        <rect x="56" y="50" width="3" height="3" rx="1" fill="#fff" opacity=".6"/>
                                    </svg>
                                </div>
                                <h4>Calcula costos y prepara documentos</h4>
                                <p>Estimamos tus costos y te decimos qué documentos necesitas.</p>
                                <span class="ie-step-badge" id="step3-badge"><span class="ie-step-badge-dot"></span>Pendiente</span>
                            </a>
                        </div>
                    </div>

                    <!-- Banner info con persona -->
                    <a href="seguimiento.jsp" class="ie-info-banner">
                        <div class="ie-info-icon">
                            <svg width="22" height="22" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.853l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z"/>
                            </svg>
                        </div>
                        <div class="ie-info-content">
                            <h5>Vamos contigo en cada paso</h5>
                            <p>Nuestro sistema revisa la información, te alerta si algo falta y te guía hasta completar tu importación.</p>
                        </div>
                        <div class="ie-info-check">
                            <svg width="15" height="15" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/></svg>
                        </div>
                        <img src="css/person_laptop.png" alt="Persona con laptop" class="ie-info-person">
                    </a>
                </div>

                <!-- COLUMNA DERECHA: siguiente paso + ayuda -->
                <div class="ie-aside ie-anim-3">

                    <!-- Tarjeta siguiente paso -->
                    <div class="ie-next-card">
                        <div class="ie-next-head">
                            <div class="ie-next-icon">
                                <svg width="22" height="22" fill="none" stroke="#fff" stroke-width="2.2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
                                </svg>
                            </div>
                            <div style="flex:1;">
                                <h3>Tu siguiente paso 👣</h3>
                                <p>Para comenzar, busquemos el código (HS Code) de tu producto. Con esto sabremos requisitos y permisos.</p>
                            </div>
                        </div>
                        <a href="buscador.jsp" class="ie-btn-search" id="ie-next-action-btn">
                            <span class="ie-btn-search-lft">
                                <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
                                </svg>
                                Buscar mi código
                            </span>
                            <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                        </a>
                    </div>

                    <!-- Tarjeta Ayuda rápida -->
                    <div class="ie-help-card">
                        <div class="ie-help-title">
                            <h4>Ayuda rápida</h4>
                            <span class="ie-help-q-btn">
                                <svg width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z"/></svg>
                            </span>
                        </div>

                        <!-- FAQ 1 -->
                        <div class="ie-faq">
                            <button class="ie-faq-trigger" aria-expanded="false" type="button">
                                <div class="ie-faq-left">
                                    <svg class="ie-faq-ico" style="color:#5B50F0;" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9.568 3H5.25A2.25 2.25 0 003 5.25v4.318c0 .597.237 1.17.659 1.591l9.581 9.581a2.25 2.25 0 003.182 0l4.318-4.318a2.25 2.25 0 000-3.182L11.16 3.659A2.25 2.25 0 009.568 3zM6 6h.008v.008H6V6z"/></svg>
                                    <span class="ie-faq-label">¿Qué es un HS Code?</span>
                                </div>
                                <svg class="ie-faq-chevron" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5"/></svg>
                            </button>
                            <div class="ie-faq-body">
                                Número de 6-10 dígitos que identifica tu producto en aduana. Con él se determinan aranceles, permisos y documentos.<br>
                                <a href="buscador.jsp" style="color:#5B50F0; font-weight:600; text-decoration:none; margin-top:4px; display:inline-block;">Buscar mi código →</a>
                            </div>
                        </div>

                        <!-- FAQ 2 -->
                        <div class="ie-faq">
                            <button class="ie-faq-trigger" aria-expanded="false" type="button">
                                <div class="ie-faq-left">
                                    <svg class="ie-faq-ico" style="color:#5B50F0;" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/></svg>
                                    <span class="ie-faq-label">¿Necesito permisos?</span>
                                </div>
                                <svg class="ie-faq-chevron" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5"/></svg>
                            </button>
                            <div class="ie-faq-body">
                                Depende del producto. Alimentos, medicamentos y electrónicos pueden requerir autorización de DIGESA, DIGEMID, MTC o SENASA.<br>
                                <a href="gestor_permisos.jsp" style="color:#5B50F0; font-weight:600; text-decoration:none; margin-top:4px; display:inline-block;">Ver gestor de permisos →</a>
                            </div>
                        </div>

                        <!-- FAQ 3 -->
                        <div class="ie-faq">
                            <button class="ie-faq-trigger" aria-expanded="false" type="button">
                                <div class="ie-faq-left">
                                    <svg class="ie-faq-ico" style="color:#5B50F0;" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/></svg>
                                    <span class="ie-faq-label">¿Qué documentos me pedirán?</span>
                                </div>
                                <svg class="ie-faq-chevron" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="m19.5 8.25-7.5 7.5-7.5-7.5"/></svg>
                            </button>
                            <div class="ie-faq-body">
                                Factura comercial, Packing list, Bill of Lading/AWB, Certificado de origen y DUA. El sistema te indica cuáles aplican.<br>
                                <a href="documentos.jsp" style="color:#5B50F0; font-weight:600; text-decoration:none; margin-top:4px; display:inline-block;">Ver expediente →</a>
                            </div>
                        </div>

                    </div>

                </div><!-- fin aside -->
            </div><!-- fin ie-grid-bottom -->

        </div><!-- fin ie-scroll -->
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
    <script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/dashboard.js" defer></script>

</body>
</html>
