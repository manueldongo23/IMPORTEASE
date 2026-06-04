<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    String userNombreTop = (String) session.getAttribute("usuarioNombre");
    if (userNombreTop == null || userNombreTop.trim().isEmpty()) userNombreTop = "Usuario";
    String userEmailTop = (String) session.getAttribute("usuarioEmail");
    if (userEmailTop == null) userEmailTop = "";
    String initialsTop = "US";
    String[] partsTop = userNombreTop.trim().split("\\s+");
    if (partsTop.length >= 2) {
        initialsTop = (partsTop[0].substring(0,1) + partsTop[1].substring(0,1)).toUpperCase();
    } else if (partsTop.length == 1 && partsTop[0].length() >= 1) {
        initialsTop = partsTop[0].substring(0, Math.min(2, partsTop[0].length())).toUpperCase();
    }
    request.setAttribute("userInitials", initialsTop);
%>
<header class="topbar-shell">
    <div class="flex items-center gap-3">
        <!-- Mobile hamburger -->
        <button onclick="toggleSidebar()" class="mobile-toggle" aria-label="Toggle sidebar">
            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25h16.5"/>
            </svg>
        </button>

        <div class="topbar-status">
            <span class="topbar-status__dot"></span>
            <span>Asistente activo: listo</span>
        </div>
    </div>

    <div class="flex items-center gap-4">
        <!-- Search -->
        <div class="search-bar hidden md:block">
            <svg class="search-bar__icon" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
            </svg>
            <input type="search" class="search-bar__input" placeholder="Buscar productos, códigos..." aria-label="Buscar">
        </div>

        <!-- Status links -->
        <div class="topbar-links hidden sm:flex">
            <span><span class="topbar-links__dot"></span> SUNAT</span>
            <span><span class="topbar-links__dot"></span> VUCE</span>
            <span><span class="topbar-links__dot"></span> SBS</span>
        </div>

        <!-- User dropdown -->
        <div class="user-dropdown">
            <button type="button" class="user-dropdown__trigger" onclick="toggleUserDropdown()" aria-haspopup="true" aria-expanded="false">
                <span class="user-dropdown__avatar"><%= com.importease.proyecto.service.HtmlUtil.escape(initialsTop) %></span>
                <span class="hidden sm:inline"><%= com.importease.proyecto.service.HtmlUtil.escape(userNombreTop) %></span>
                <svg class="w-3.5 h-3.5 text-[var(--text-tertiary)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 8.25l-7.5 7.5-7.5-7.5"/>
                </svg>
            </button>
            <div id="userDropdownMenu" class="user-dropdown__menu" role="menu">
                <div class="user-dropdown__header">
                    <p class="user-dropdown__name"><%= com.importease.proyecto.service.HtmlUtil.escape(userNombreTop) %></p>
                    <p class="user-dropdown__email"><%= com.importease.proyecto.service.HtmlUtil.escape(userEmailTop) %></p>
                </div>
                <a href="dashboard.jsp" class="user-dropdown__item" role="menuitem">
                    <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"/>
                    </svg>
                    Panel principal
                </a>
                <a href="perfil.jsp" class="user-dropdown__item" role="menuitem">
                    <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z"/>
                    </svg>
                    Mi perfil
                </a>
                <div class="user-dropdown__divider"></div>
                <button type="button" class="user-dropdown__item" onclick="doLogout()" role="menuitem">
                    <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75"/>
                    </svg>
                    Cerrar sesión
                </button>
            </div>
        </div>
    </div>
</header>

<script nonce="<%= request.getAttribute("csp_nonce") %>">
    function toggleUserDropdown() {
        const menu = document.getElementById('userDropdownMenu');
        if (!menu) return;
        const isOpen = menu.classList.contains('is-open');
        menu.classList.toggle('is-open');
        const trigger = menu.previousElementSibling;
        if (trigger) trigger.setAttribute('aria-expanded', !isOpen);
    }

    // Close user dropdown on outside click
    document.addEventListener('click', function(event) {
        const dropdown = document.querySelector('.user-dropdown');
        const menu = document.getElementById('userDropdownMenu');
        if (dropdown && menu && !dropdown.contains(event.target)) {
            menu.classList.remove('is-open');
            const trigger = menu.previousElementSibling;
            if (trigger) trigger.setAttribute('aria-expanded', 'false');
        }
    });

    // Close dropdown on escape
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            const menu = document.getElementById('userDropdownMenu');
            if (menu && menu.classList.contains('is-open')) {
                menu.classList.remove('is-open');
                const trigger = menu.previousElementSibling;
                if (trigger) trigger.setAttribute('aria-expanded', 'false');
            }
        }
    });
</script>
