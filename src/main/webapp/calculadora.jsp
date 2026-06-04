<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    // Verificar sesión activa
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    
    // Redirigir a la calculadora comercial (negocio) por defecto, propagando todos los parámetros de consulta
    String queryString = request.getQueryString();
    String redirectUrl = "calculadora-negocio.jsp";
    if (queryString != null && !queryString.isEmpty()) {
        redirectUrl += "?" + queryString;
    }
    response.sendRedirect(redirectUrl);
%>
