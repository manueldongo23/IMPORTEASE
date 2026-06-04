<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Página no encontrada</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
</head>
<body class="flex items-center justify-center min-h-screen bg-[var(--nav-bg)] text-[var(--text-inverse)]">
    <div class="text-center space-y-8 p-12 max-w-lg fade-up">
        <!-- SVG Shield Lock Icon -->
        <div class="w-24 h-24 mx-auto rounded-[2rem] bg-white/5 border border-white/10 flex items-center justify-center text-[var(--accent)]">
            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor" class="w-10 h-10">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 9v3.75m0-10.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.75c0 5.592 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.57-.598-3.75h-.152c-3.196 0-6.1-1.249-8.25-3.286zm0 13.036h.008v.008H12v-.008z" />
            </svg>
        </div>
        <div>
            <h1 class="text-4xl font-black tracking-tight mb-3 text-[var(--text-inverse)]">Acceso Restringido</h1>
            <p class="text-[var(--nav-muted)] text-sm font-semibold">La página que buscas no está disponible, requiere permisos especiales de aduanas o su sesión ha expirado.</p>
        </div>
        <div class="flex gap-4 justify-center">
            <a href="dashboard.jsp" class="px-8 py-3 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white font-black text-xs uppercase tracking-widest rounded-xl transition-all shadow-lg shadow-[var(--accent-glow)]">
                Ir al Dashboard
            </a>
            <a href="login.jsp" class="px-8 py-3 bg-white/5 hover:bg-white/10 text-white font-black text-xs uppercase tracking-widest rounded-xl transition-all border border-white/10">
                Iniciar Sesión
            </a>
        </div>
        <p class="text-[10px] text-[var(--nav-muted)] font-bold uppercase tracking-widest">ImportEase Enterprise Trade Platform</p>
    </div>
</body>
</html>
