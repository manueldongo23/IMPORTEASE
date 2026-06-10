<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<%@ page import="java.util.*" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.importease.proyecto.dto.SeguimientoDTO" %>
<%@ page import="com.importease.proyecto.service.SeguimientoServicio" %>
<%@ page import="com.importease.proyecto.service.HtmlUtil" %>
<%@ page import="com.importease.proyecto.model.Importacion" %>
<%@ page import="com.importease.proyecto.repository.ImportacionRepositorio" %>
<%@ page import="com.importease.proyecto.service.ConexionDB" %>
<%
    // Seguridad: usuario autenticado
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    Integer usuarioId = (Integer) session.getAttribute("usuarioId");
    String operacionIdParam = request.getParameter("operacionId");

    // Toda la logica en SeguimientoServicio - JSP solo renderiza
    SeguimientoDTO seg = new SeguimientoServicio().construir(usuarioId, operacionIdParam);



    // Todas las importaciones del usuario (para selector de operacion)
    List<Importacion> todas = new ArrayList<>();
    try (java.sql.Connection con = ConexionDB.obtenerConexion()) {
        todas = new ImportacionRepositorio().listarPorUsuario(con, usuarioId);
    } catch (Exception ignored) {}
%>

<%!
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("</", "<\\/");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Seguimiento</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;700&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/knowledge-base.js"></script>
    <style>
        .animate-pulse-slow { animation: pulse 2.5s cubic-bezier(0.4,0,0.6,1) infinite; }
        @keyframes pulse { 0%,100%{opacity:1} 50%{opacity:.85} }
        .seg-main { padding: 32px 40px; max-width: 1200px; margin: 0 auto; display: flex; flex-direction: column; gap: 24px; width: 100%; }
        .seg-header { display: flex; align-items: center; justify-content: space-between; }
        .seg-title { font-size: 34px; font-weight: 900; color: #1A1D2E; letter-spacing: -0.5px; line-height: 1.1; }
        .seg-subtitle { font-size: 15px; color: #64748B; margin-top: 4px; font-weight: 500; }
        .seg-btn-new { display: inline-flex; align-items: center; gap: 8px; padding: 12px 22px; border-radius: 12px; font-size: 14px; font-weight: 700; color: #fff; background: #5B50F0; border: none; cursor: pointer; text-decoration: none; transition: background 0.2s; box-shadow: 0 4px 14px rgba(91,80,240,0.3); }
        .seg-btn-new:hover { background: #4A3FD0; }
        .seg-card { background: #fff; border: 1px solid #E8EAED; border-radius: 24px; box-shadow: 0 4px 24px rgba(0,0,0,0.05); padding: 32px; }
        /* Hero Card */
        .hero-card { display: flex; align-items: center; gap: 36px; }
        .hero-icon-wrap { position: relative; width: 130px; height: 130px; background: #F0EFFE; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
        .hero-sparkle1 { position: absolute; top: 14px; right: 8px; width: 18px; height: 18px; color: #A78BFA; }
        .hero-sparkle2 { position: absolute; top: 36px; right: 0; width: 12px; height: 12px; color: #C4B5FD; }
        .hero-check { position: absolute; bottom: 4px; left: -4px; width: 34px; height: 34px; background: #10B981; border-radius: 50%; border: 3px solid #fff; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 8px rgba(16,185,129,0.4); }
        .hero-info { flex: 1; min-width: 0; }
        .hero-h2 { font-size: 26px; font-weight: 900; color: #1A1D2E; letter-spacing: -0.3px; margin-bottom: 20px; }
        .hero-fields { display: flex; align-items: flex-start; gap: 40px; margin-bottom: 20px; }
        .hero-field label { display: block; font-size: 13px; color: #64748B; font-weight: 500; margin-bottom: 3px; }
        .hero-field span { font-size: 15px; font-weight: 700; color: #1A1D2E; }
        .hero-field .estado-badge { display: inline-flex; align-items: center; padding: 4px 10px; border-radius: 6px; font-size: 12px; font-weight: 700; background: #D1FAE5; color: #047857; }
        .progress-label { font-size: 13px; font-weight: 700; color: #059669; margin-bottom: 6px; }
        .progress-track { width: 100%; max-width: 460px; height: 8px; background: #F1F5F9; border-radius: 99px; overflow: hidden; margin-bottom: 8px; }
        .progress-fill { height: 100%; background: #10B981; border-radius: 99px; }
        .progress-note { font-size: 13px; color: #64748B; font-weight: 500; }
        .hero-actions { display: flex; flex-direction: column; gap: 10px; width: 230px; flex-shrink: 0; }
        .btn-continuar { display: flex; align-items: center; justify-content: space-between; padding: 13px 20px; border-radius: 12px; background: #5B50F0; color: #fff; font-weight: 700; font-size: 14px; border: none; cursor: pointer; text-decoration: none; transition: background 0.2s; box-shadow: 0 4px 14px rgba(91,80,240,0.3); }
        .btn-continuar:hover { background: #4A3FD0; }
        .btn-outline { display: flex; align-items: center; gap: 10px; padding: 11px 16px; border-radius: 12px; background: #fff; border: 1.5px solid #E2E8F0; color: #1A1D2E; font-weight: 600; font-size: 13px; cursor: pointer; text-decoration: none; transition: border-color 0.2s, box-shadow 0.2s; }
        .btn-outline:hover { border-color: #5B50F0; box-shadow: 0 2px 8px rgba(91,80,240,0.1); }
        .btn-outline svg { color: #94A3B8; flex-shrink: 0; }
        /* Stepper */
        .stepper-wrap { position: relative; width: 100%; display: flex; justify-content: space-between; align-items: flex-start; }
        .step-line-bg { position: absolute; top: 15px; left: 10%; right: 10%; height: 2px; background: #E2E8F0; z-index: 0; }
        .step-line-fg { position: absolute; top: 15px; left: 10%; height: 2px; background: #10B981; z-index: 1; transition: width 0.4s; }
        .step-col { position: relative; z-index: 2; display: flex; flex-direction: column; align-items: center; width: 120px; }
        .step-circle-done { width: 32px; height: 32px; border-radius: 50%; background: #10B981; display: flex; align-items: center; justify-content: center; color: #fff; outline: 10px solid #fff; }
        .step-circle-active { width: 32px; height: 32px; border-radius: 50%; background: #5B50F0; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 900; font-size: 14px; outline: 10px solid #fff; }
        .step-circle-pending { width: 32px; height: 32px; border-radius: 50%; background: #fff; border: 2px solid #E2E8F0; display: flex; align-items: center; justify-content: center; color: #94A3B8; font-weight: 700; font-size: 14px; outline: 10px solid #fff; }
        .step-name { margin-top: 16px; font-size: 14px; font-weight: 700; color: #1A1D2E; text-align: center; }
        .step-status-done { font-size: 12px; font-weight: 700; color: #10B981; margin-top: 2px; }
        .step-status-active { font-size: 12px; font-weight: 700; color: #5B50F0; margin-top: 2px; }
        .step-status-pending { font-size: 12px; font-weight: 700; color: #94A3B8; margin-top: 2px; }
        .step-footer { margin-top: 20px; font-size: 14px; color: #64748B; font-weight: 500; text-align: center; }
        /* Alert */
        .alert-warn { background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 20px; padding: 18px 20px; display: flex; align-items: flex-start; gap: 14px; }
        .alert-icon { width: 38px; height: 38px; border-radius: 50%; background: #FEF3C7; border: 1px solid #FDE68A; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
        .alert-title { font-size: 14px; font-weight: 900; color: #92400E; margin-bottom: 4px; }
        .alert-body { font-size: 13px; color: #B45309; font-weight: 500; }
        /* Bottom grid */
        .bottom-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
        .bottom-card-title { display: flex; align-items: center; gap: 14px; margin-bottom: 20px; }
        .bottom-icon-circle { width: 46px; height: 46px; border-radius: 50%; background: #5B50F0; display: flex; align-items: center; justify-content: center; flex-shrink: 0; box-shadow: 0 4px 12px rgba(91,80,240,0.3); }
        .bottom-h3 { font-size: 19px; font-weight: 900; color: #1A1D2E; }
        .bottom-h3-sub { font-size: 13px; color: #64748B; font-weight: 500; margin-top: 2px; }
        .action-row { display: flex; align-items: center; justify-content: space-between; padding: 14px 18px; border-radius: 14px; border: 1.5px solid #E2E8F0; background: #fff; cursor: pointer; text-decoration: none; transition: border-color 0.2s, box-shadow 0.2s; margin-bottom: 10px; }
        .action-row:last-child { margin-bottom: 0; }
        .action-row:hover { border-color: #5B50F0; box-shadow: 0 4px 12px rgba(91,80,240,0.08); }
        .action-row-left { display: flex; align-items: center; gap: 14px; }
        .action-row-icon { width: 34px; height: 34px; border-radius: 50%; background: #F0EFFE; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
        .action-row-label { font-size: 14px; font-weight: 700; color: #1A1D2E; }
        .action-row-arrow { color: #94A3B8; }
        /* Details */
        .detail-row { display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid #F1F5F9; }
        .detail-row:last-child { border-bottom: none; }
        .detail-left { display: flex; align-items: center; gap: 10px; font-size: 14px; color: #64748B; font-weight: 500; }
        .detail-icon-wrap { width: 26px; height: 26px; border-radius: 50%; background: #F8FAFC; border: 1px solid #E2E8F0; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
        .detail-value { font-size: 14px; font-weight: 700; color: #1A1D2E; }
        .seg-gear-icon { color: #5B50F0; width: 28px; height: 28px; flex-shrink: 0; }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-[#F8FAFC] font-['Outfit'] text-[#1E293B]">
    <jsp:include page="/fragments/toast.jsp" />

    <jsp:include page="/fragments/sidebar.jsp" />

    <main style="flex:1;overflow-y:auto;display:flex;flex-direction:column;background:#F8FAFC;">
        <div class="seg-main">
            
            <!-- Header -->
            <div class="seg-header">
                <div>
                    <h1 class="seg-title">Seguimiento</h1>
                    <p class="seg-subtitle">Te guiamos paso a paso para completar tu importación sin complicarte.</p>
                </div>
                <button onclick="window.location.href='evaluacion.jsp'" class="seg-btn-new">
                    <svg width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/></svg>
                    NUEVA IMPORTACIÓN
                </button>
            </div>

            <% if (seg.isSinImportacion()) { %>
            <!-- Empty State -->
            <div class="seg-card" style="display:flex;flex-direction:column;align-items:center;text-align:center;max-width:560px;margin:40px auto;gap:20px;padding:56px 40px;">
                <div style="width:72px;height:72px;background:#EEF0FB;border-radius:50%;display:flex;align-items:center;justify-content:center;">
                    <svg width="36" height="36" fill="none" stroke="#5B50F0" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"/></svg>
                </div>
                <div>
                    <h2 style="font-size:20px;font-weight:900;color:#1A1D2E;margin-bottom:8px;">Aún no tienes una importación en seguimiento</h2>
                    <p style="font-size:14px;color:#64748B;font-weight:500;">Para comenzar a rastrear y gestionar el estado de tu carga paso a paso, inicia un nuevo proceso con nuestro asistente inteligente.</p>
                </div>
                <button onclick="window.location.href='evaluacion.jsp'" class="seg-btn-new">Crear mi primera importación</button>
            </div>
            <% } else { // has importacion %>

            <!-- Hero Card -->
            <div class="seg-card hero-card">
                <!-- Icon -->
                <div class="hero-icon-wrap">
                    <svg width="68" height="68" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M32 6L54 18L32 31L10 18L32 6Z" fill="#8B7CFF"/>
                        <path d="M10 18L32 31V56L10 43V18Z" fill="#6652FF"/>
                        <path d="M54 18L32 31V56L54 43V18Z" fill="#503CE6"/>
                        <path d="M22 12.5L44 24.5" stroke="#B0A6FF" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    <svg class="hero-sparkle1" fill="#A78BFA" viewBox="0 0 24 24"><path d="M12 0l2 8 8 2-8 2-2 8-2-8-8-2 8-2z"/></svg>
                    <svg class="hero-sparkle2" fill="#C4B5FD" viewBox="0 0 24 24"><path d="M12 0l2 8 8 2-8 2-2 8-2-8-8-2 8-2z"/></svg>
                    <div class="hero-check">
                        <svg width="16" height="16" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg>
                    </div>
                </div>

                <!-- Info -->
                <div class="hero-info">
                    <h2 class="hero-h2">
                        <%= HtmlUtil.escape(seg.getHeroBannerTitulo()) %>
                    </h2>
                    <div class="hero-fields">
                        <div class="hero-field">
                            <label>Producto</label>
                            <span title="<%= HtmlUtil.escape(seg.getProducto()) %>"><%= HtmlUtil.escape(seg.getDisplayProducto()) %></span>
                        </div>
                        <div class="hero-field">
                            <label>Código</label>
                            <span><%= HtmlUtil.escape(seg.getCodigoArancelario()) %></span>
                        </div>
                        <div class="hero-field">
                            <label>Estado</label>
                            <span class="estado-badge"><%= HtmlUtil.escape(seg.getEstadoLabel()) %></span>
                        </div>
                    </div>
                    <div class="progress-label"><%= seg.getPorcentajeAvance() %>% completado</div>
                    <div class="progress-track">
                        <div class="progress-fill" style="width:<%= seg.getPorcentajeAvance() %>%;"></div>
                    </div>
                    <div class="progress-note">Ya completaste casi todo. Solo revisa tus documentos y continúa al cierre.</div>
                </div>

                <!-- Actions -->
                <div class="hero-actions">
                    <a href="<%= HtmlUtil.escape(seg.getSiguientePasoUrl()) %>" class="btn-continuar">
                        <span>Continuar</span>
                        <svg width="16" height="16" fill="none" stroke="#fff" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                    </a>
                    <a href="documentos.jsp" class="btn-outline">
                        <svg width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                        Revisar documentos
                    </a>
                    <button onclick="openStageHelp('<%= HtmlUtil.escape(seg.getStageVisual()) %>', '<%= HtmlUtil.escape(seg.getProducto()) %>', '<%= HtmlUtil.escape(seg.getEntidadRevisora()) %>')" class="btn-outline" style="border:none;cursor:pointer;">
                        <svg width="18" height="18" fill="none" stroke="#94A3B8" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 14l9-5-9-5-9 5 9 5z"/><path stroke-linecap="round" stroke-linejoin="round" d="M12 14l6.16-3.422a12.083 12.083 0 01.665 6.479A11.952 11.952 0 0012 20.055a11.952 11.952 0 00-6.824-2.998 12.078 12.078 0 01.665-6.479L12 14z"/><path stroke-linecap="round" stroke-linejoin="round" d="M12 14v7"/></svg>
                        Entender este paso
                    </button>
                </div>
            </div>

            <!-- Stepper Card -->
            <div class="seg-card" style="display:flex;flex-direction:column;align-items:center;padding-top:40px;padding-bottom:32px;">
                <div class="stepper-wrap">
                    <div class="step-line-bg"></div>
                    <div class="step-line-fg" style="width:<%= seg.getStepperVisual() == 1 ? "0%" : (seg.getStepperVisual() == 2 ? "33%" : (seg.getStepperVisual() == 3 ? "66%" : "100%")) %>;"></div>

                    <!-- Step 1 -->
                    <div class="step-col">
                        <% if (seg.getStepperVisual() > 1) { %>
                        <div class="step-circle-done"><svg width="14" height="14" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg></div>
                        <% } else { %>
                        <div class="step-circle-active">1</div>
                        <% } %>
                        <span class="step-name">1. Producto</span>
                        <span class="<%= seg.getStepperVisual() > 1 ? "step-status-done" : "step-status-active" %>"><%= seg.getStepperVisual() > 1 ? "Completado" : "Paso actual" %></span>
                    </div>

                    <!-- Step 2 -->
                    <div class="step-col">
                        <% if (seg.getStepperVisual() > 2) { %>
                        <div class="step-circle-done"><svg width="14" height="14" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg></div>
                        <% } else if (seg.getStepperVisual() == 2) { %>
                        <div class="step-circle-active">2</div>
                        <% } else { %>
                        <div class="step-circle-pending">2</div>
                        <% } %>
                        <span class="step-name">2. Permisos</span>
                        <span class="<%= seg.getStepperVisual() > 2 ? "step-status-done" : (seg.getStepperVisual() == 2 ? "step-status-active" : "step-status-pending") %>"><%= seg.getStepperVisual() > 2 ? "Completado" : (seg.getStepperVisual() == 2 ? "Paso actual" : "Pendiente") %></span>
                    </div>

                    <!-- Step 3 -->
                    <div class="step-col">
                        <% if (seg.getStepperVisual() > 3 || seg.isCompleto()) { %>
                        <div class="step-circle-done"><svg width="14" height="14" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg></div>
                        <% } else if (seg.getStepperVisual() == 3) { %>
                        <div class="step-circle-active">3</div>
                        <% } else { %>
                        <div class="step-circle-pending">3</div>
                        <% } %>
                        <span class="step-name">3. Documentos</span>
                        <span class="<%= (seg.getStepperVisual() > 3 || seg.isCompleto()) ? "step-status-done" : (seg.getStepperVisual() == 3 ? "step-status-active" : "step-status-pending") %>"><%= (seg.getStepperVisual() > 3 || seg.isCompleto()) ? "Completado" : (seg.getStepperVisual() == 3 ? "Paso actual" : "Pendiente") %></span>
                    </div>

                    <!-- Step 4 -->
                    <div class="step-col">
                        <% if (seg.isCompleto()) { %>
                        <div class="step-circle-done"><svg width="14" height="14" fill="none" stroke="#fff" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M5 13l4 4L19 7"/></svg></div>
                        <% } else if (seg.getStepperVisual() == 4) { %>
                        <div class="step-circle-active">4</div>
                        <% } else { %>
                        <div class="step-circle-pending">4</div>
                        <% } %>
                        <span class="step-name">4. Revisión final</span>
                        <span class="<%= seg.isCompleto() ? "step-status-done" : (seg.getStepperVisual() == 4 ? "step-status-active" : "step-status-pending") %>"><%= seg.isCompleto() ? "Cerrado" : (seg.getStepperVisual() == 4 ? "Paso actual" : "Pendiente") %></span>
                    </div>
                </div>
                <div class="step-footer">
                    <%= HtmlUtil.escape(seg.getSiguientePasoDesc()) %>
                </div>
            </div>

            <!-- Alert -->
            <% if (seg.tieneAlertas()) { %>
            <div class="alert-warn">
                <div class="alert-icon">
                    <svg width="20" height="20" fill="none" stroke="#B45309" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                </div>
                <div>
                    <div class="alert-title">Requiere tu atención inmediata</div>
                    <div class="alert-body">
                        <% for (String alerta : seg.getAlertas()) { %>
                        <div>&bull; <%= HtmlUtil.escape(alerta) %></div>
                        <% } %>
                    </div>
                </div>
            </div>
            <% } %>

            <!-- Bottom Grid -->
            <div class="bottom-grid">
                <!-- Left: Qué debes hacer -->
                <div class="seg-card" style="display:flex;flex-direction:column;">
                    <div class="bottom-card-title">
                        <div class="bottom-icon-circle">
                            <svg width="22" height="22" fill="none" stroke="#fff" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/></svg>
                        </div>
                        <div>
                            <div class="bottom-h3">¿Qué debes hacer ahora?</div>
                            <div class="bottom-h3-sub"><%= HtmlUtil.escape(seg.getSiguientePasoDesc()) %></div>
                        </div>
                    </div>
                    <div style="display:flex;flex-direction:column;gap:0;">
                        <a href="<%= HtmlUtil.escape(seg.getSiguientePasoUrl()) %>" class="action-row">
                            <div class="action-row-left">
                                <div class="action-row-icon">
                                    <% if ("PERMISOS".equals(seg.getStageVisual())) { %>
                                    <svg width="16" height="16" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"/></svg>
                                    <% } else if ("EXPEDIENTE".equals(seg.getStageVisual())) { %>
                                    <svg width="16" height="16" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5m-13.5-9L12 3m0 0l4.5 4.5M12 3v13.5"/></svg>
                                    <% } else { %>
                                    <svg width="16" height="16" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
                                    <% } %>
                                </div>
                                <span class="action-row-label"><%= HtmlUtil.escape(seg.getSiguientePasoTitulo()) %></span>
                            </div>
                            <svg class="action-row-arrow" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                        </a>
                        <a href="documentos.jsp" class="action-row">
                            <div class="action-row-left">
                                <div class="action-row-icon">
                                    <svg width="16" height="16" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4 6h16M4 12h16M4 18h7"/></svg>
                                </div>
                                <span class="action-row-label">
                                    <% if (!seg.isHasFactura() || !seg.isHasBl() || !seg.isHasCert()) { %>Ver qué documentos me faltan<% } else { %>Revisar mis documentos<% } %>
                                </span>
                            </div>
                            <svg class="action-row-arrow" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                        </a>
                        <button onclick="if(typeof openHelpModal==='function')openHelpModal()" class="action-row" style="width:100%;text-align:left;background:#fff;border-top:none;">
                            <div class="action-row-left">
                                <div class="action-row-icon">
                                    <svg width="16" height="16" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M18.364 5.636a9 9 0 010 12.728M15.536 8.464a5 5 0 010 7.072M6.343 17.657a9 9 0 010-12.728M9.172 15.536a5 5 0 010-7.072"/></svg>
                                </div>
                                <span class="action-row-label">Hablar con soporte</span>
                            </div>
                            <svg class="action-row-arrow" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                        </button>
                    </div>
                </div>

                <!-- Right: Detalles técnicos -->
                <div class="seg-card" style="display:flex;flex-direction:column;">
                    <div class="bottom-card-title" style="margin-bottom:16px;">
                        <svg class="seg-gear-icon" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z"/><circle cx="12" cy="12" r="3"/></svg>
                        <div class="bottom-h3">Detalles técnicos</div>
                    </div>
                    <div>
                        <div class="detail-row">
                            <div class="detail-left">
                                <div class="detail-icon-wrap"><svg width="14" height="14" fill="none" stroke="#64748B" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14"/></svg></div>
                                <span>Operación</span>
                            </div>
                            <span class="detail-value">#<%= seg.getNumeroOperacion() %></span>
                        </div>
                        <div class="detail-row">
                            <div class="detail-left">
                                <div class="detail-icon-wrap"><svg width="14" height="14" fill="none" stroke="#64748B" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg></div>
                                <span>Costo base</span>
                            </div>
                            <span class="detail-value"><%= HtmlUtil.escape(seg.getCostoBase()) %></span>
                        </div>
                        <div class="detail-row">
                            <div class="detail-left">
                                <div class="detail-icon-wrap"><svg width="14" height="14" fill="none" stroke="#64748B" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg></div>
                                <span>Revisor</span>
                            </div>
                            <span class="detail-value"><%= HtmlUtil.escape(seg.getEntidadRevisora()) %></span>
                        </div>
                        <div class="detail-row">
                            <div class="detail-left">
                                <div class="detail-icon-wrap"><svg width="14" height="14" fill="none" stroke="#64748B" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg></div>
                                <span>Última actualización</span>
                            </div>
                            <span class="detail-value"><%= HtmlUtil.escape(seg.getUltimaActualizacion()) %></span>
                        </div>
                    </div>
                </div>
            </div>

                <!-- Otras operaciones section -->
                <% if (todas != null && todas.size() > 1) { %>
                    <section class="bg-white border border-[#E2E8F0] p-6 rounded-[24px] shadow-sm">
                        <div class="border-b border-[#F1F5F9] pb-4 mb-4">
                            <h3 class="text-base font-black text-[#1A1D2E]">Otras operaciones en curso</h3>
                            <p class="text-xs text-[#64748B] font-semibold">Selecciona otra de tus rutas registradas para hacer seguimiento.</p>
                        </div>
                        <div class="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
                            <%
                                int numeroOperacion = seg.getNumeroOperacion();
                                for (Importacion op : todas) {
                                    if (op.getId() == numeroOperacion) {
                                        continue;
                                    }
                                    String opHs = op.getHsCode();
                                    String opProduct = op.getProductoDesc() != null ? op.getProductoDesc() : "Producto sin descripción";
                                    String opState = op.getEstado() != null ? op.getEstado() : "BORRADOR";
                                    
                                    int opProgress = 24;
                                    String opLabel = "Preparación";
                                    String opToneClass = "bg-blue-50 text-blue-700 border-blue-100";
                                    
                                    if ("NACIONALIZADA".equals(opState)) {
                                        opProgress = 100;
                                        opLabel = "Cerrada";
                                        opToneClass = "bg-emerald-50 text-emerald-700 border-emerald-100";
                                    } else if (opHs == null || opHs.trim().isEmpty()) {
                                        opProgress = 24;
                                        opLabel = "Preparación";
                                        opToneClass = "bg-blue-50 text-blue-700 border-blue-100";
                                    } else if ("PENDIENTE_DOCS".equals(opState)) {
                                        opProgress = 76;
                                        opLabel = "Documentos";
                                        opToneClass = "bg-amber-50 text-amber-700 border-amber-100";
                                    } else if ("BORRADOR".equals(opState) || "COTIZACION".equals(opState)) {
                                        opProgress = 64;
                                        opLabel = "Documentos";
                                        opToneClass = "bg-cyan-50 text-cyan-700 border-cyan-100";
                                    } else {
                                        opProgress = 92;
                                        opLabel = "Revisión final";
                                        opToneClass = "bg-emerald-50 text-emerald-700 border-emerald-100";
                                    }
                            %>
                            <article class="p-4 rounded-xl border border-[#E2E8F0] hover:border-[#5B50F0]/30 bg-[#F8FAFC] flex flex-col justify-between gap-3 transition-all">
                                <div class="space-y-1">
                                    <div class="flex items-center justify-between">
                                        <span class="text-[9px] font-black uppercase text-[#94A3B8]">Operación #<%= op.getId() %></span>
                                        <span class="px-2 py-0.5 rounded-full text-[9px] font-bold border <%= opToneClass %>"><%= opLabel %></span>
                                    </div>
                                    <h4 class="text-xs font-black text-[#1A1D2E] truncate"><%= HtmlUtil.escape(opProduct) %></h4>
                                </div>
                                <div class="space-y-2">
                                    <div class="flex justify-between items-center text-[10px] font-black text-[#64748B]">
                                        <span>Avance</span>
                                        <span><%= opProgress %>%</span>
                                    </div>
                                    <div class="w-full h-1.5 bg-[#E2E8F0] rounded-full overflow-hidden">
                                        <div class="h-full bg-[#5B50F0]" style="width: <%= opProgress %>%"></div>
                                    </div>
                                    <a href="seguimiento.jsp?operacionId=<%= op.getId() %>" class="w-full py-2 rounded-lg bg-white border border-[#E2E8F0] hover:bg-[#EEF0FB] hover:border-[#5B50F0]/20 flex items-center justify-center text-[10px] font-black uppercase tracking-wider text-[#5B50F0] transition-all">
                                        Ver seguimiento
                                    </a>
                                </div>
                            </article>
                            <%
                                }
                            %>
                        </div>
                    </section>
                <% } %>
            <% } %>
        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        function openStageHelp(stage, producto, entity) {
            if (stage === 'EXPEDIENTE') {
                if (typeof openKnowledgePanel === 'function') {
                    openKnowledgePanel('factura_comercial', {
                        relacionConTuCaso: 'Tu importación "' + producto + '" ya tiene datos base. Ahora toca ordenar factura, transporte y origen para avanzar sin observaciones.'
                    });
                }
                return;
            }
            if (stage === 'PERMISOS') {
                if (typeof openKnowledgePanel === 'function') {
                    openKnowledgePanel('permiso_autorizacion', {
                        relacionConTuCaso: 'Tu importación "' + producto + '" activa una revisión con ' + entity + '. Lo importante es saber si solo debes revisar o si necesitas autorización antes de embarcar.'
                    });
                }
                return;
            }
            if (typeof openKnowledgePanel === 'function') {
                openKnowledgePanel('declaracion_aduanera', {
                    relacionConTuCaso: 'Tu importación "' + producto + '" todavía está armando su ruta. Esta ficha explica cómo se conecta la declaración con el resto del proceso.'
                });
            }
        }
    </script>
</body>
</html>
