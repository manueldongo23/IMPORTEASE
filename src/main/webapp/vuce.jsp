<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp");
        return;
    }
    response.sendRedirect("gestor_permisos.jsp");
%>
