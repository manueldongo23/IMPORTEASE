<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%
    Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
    String errorMessage = "Lo sentimos, ha ocurrido un error inesperado.";
    String title = "Algo salió mal";
    
    if (statusCode != null) {
        if (statusCode == 404) {
            title = "Página no encontrada";
            errorMessage = "La ruta que buscas no existe o ha sido movida.";
        } else if (statusCode == 403) {
            title = "Acceso Denegado";
            errorMessage = "No tienes permisos suficientes para ver esta página.";
        } else if (statusCode == 500) {
            title = "Error del Servidor";
            errorMessage = "Nuestro sistema experimentó un fallo temporal. Intenta nuevamente.";
        }
    } else {
        statusCode = 500;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - <%= statusCode %> <%= title %></title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/tailwind-output.css" rel="stylesheet">
    <link href="<%= request.getContextPath() %>/css/main.css" rel="stylesheet">
</head>
<body class="flex items-center justify-center min-h-screen bg-[var(--surface-1)] bg-grid font-sans text-[var(--text-primary)]">
    <div class="glass-card p-12 max-w-lg w-full text-center fade-up relative overflow-hidden group">
        <!-- Glow effect -->
        <div class="absolute -top-24 -right-24 w-48 h-48 bg-[var(--accent)] rounded-full blur-[80px] opacity-20 group-hover:opacity-40 transition-opacity duration-700 pointer-events-none"></div>
        <div class="absolute -bottom-24 -left-24 w-48 h-48 bg-[var(--danger)] rounded-full blur-[80px] opacity-10 group-hover:opacity-30 transition-opacity duration-700 pointer-events-none"></div>

        <h1 class="text-9xl font-black text-[var(--text-primary)] tracking-tighter mb-2"><%= statusCode %></h1>
        <p class="pill-heading inline-block mb-6"><%= title %></p>
        
        <p class="text-[var(--text-secondary)] font-semibold text-sm mb-8 leading-relaxed max-w-sm mx-auto">
            <%= errorMessage %>
        </p>

        <div class="flex flex-col gap-3">
            <a href="<%= request.getContextPath() %>/dashboard.jsp" class="primary-button text-xs py-3.5 hover-lift">
                Volver al Dashboard Central
            </a>
            <button onclick="window.history.back()" class="soft-button text-xs py-3 hover-lift">
                Regresar a la página anterior
            </button>
        </div>
    </div>
</body>
</html>
