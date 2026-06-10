<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%@ page import="com.importease.proyecto.service.ConexionDB, java.sql.Connection, com.importease.proyecto.service.LoggerUtil" %>
<%!
    private String escapeJs(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("</", "<\\/");
    }
%>
<%
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) {
        activePage = "";
    }
    // For sidebar: load user notification count
    Integer usuarioId = (Integer) session.getAttribute("usuarioId");
    int notifCount = 0;
    String userNombreSidebar = "";
    String initials = "US";
    if (usuarioId != null) {
        try (Connection con = ConexionDB.obtenerConexionSecundaria();
             java.sql.PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM solicitudes_permiso WHERE usuario_id = ? AND estado = 'PERMISO_REQUERIDO'")) {
            ps.setInt(1, usuarioId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) notifCount = rs.getInt(1);
            }
        } catch (Exception e) {
            LoggerUtil.warn("No se pudieron cargar notificaciones del sidebar: " + e.getMessage());
        }

        userNombreSidebar = (String) session.getAttribute("usuarioNombre");
        if (userNombreSidebar == null || userNombreSidebar.trim().isEmpty()) {
            userNombreSidebar = "Usuario";
        }
        String[] namePartsSidebar = userNombreSidebar.trim().split("\\s+");
        if (namePartsSidebar.length >= 2) {
            initials = (namePartsSidebar[0].substring(0,1) + namePartsSidebar[1].substring(0,1)).toUpperCase();
        } else if (namePartsSidebar.length == 1 && namePartsSidebar[0].length() >= 1) {
            initials = namePartsSidebar[0].substring(0, Math.min(2, namePartsSidebar[0].length())).toUpperCase();
        }
    } else {
        userNombreSidebar = "Usuario";
    }
%>
<script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
    window.csrfToken = '<%= escapeJs(com.importease.proyecto.service.CsrfUtil.getToken(session)) %>';
    (function() {
        if (localStorage.getItem('dark_mode') === 'true') {
            document.documentElement.classList.add('dark-mode');
        }
    })();
</script>

<link rel="stylesheet" href="css/sidebar.css">
<script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>" src="js/ux-enhancements.js" defer></script>
<style nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
.sidebar-tools-toggle {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.5rem 0.75rem;
    border-radius: 0.5rem;
    color: var(--nav-muted);
    font-size: 0.65rem;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.1em;
    transition: all 0.2s;
    background: transparent;
    border: 1px solid transparent;
    width: 100%;
    text-align: left;
}
.sidebar-tools-toggle:hover { color: var(--nav-text); background: var(--nav-active); }
.sidebar-tools-list { display: none; margin-top: 0.25rem; }
.sidebar-tools-list.open { display: block; }
.arrow { display: inline-block; transition: transform 0.2s ease; }
.rotate-180 { transform: rotate(180deg); }
.sidebar-nav-item-accent {
    background: var(--accent);
    color: #fff;
}
.sidebar-nav-item-accent:hover {
    filter: brightness(1.1);
    color: #fff;
}
.sidebar-nav-item-accent.is-active {
    box-shadow: 0 0 0 2px var(--accent);
}
.sidebar-nav-item-sm {
    padding: 0.35rem 0.5rem 0.35rem 1.5rem;
    font-size: 0.75rem;
}
.sidebar-nav-item-sm svg {
    width: 0.85rem;
    height: 0.85rem;
}
</style>

<!-- Mobile sidebar backdrop -->
<div id="sidebarBackdrop" class="sidebar-backdrop"></div>

<aside id="appSidebar" class="app-sidebar w-64 bg-[var(--nav-bg)] border-r border-[var(--nav-border)] flex flex-col h-full z-50 shrink-0 select-none text-[var(--nav-text)] font-['Outfit'] shadow-2xl">
    <!-- Brand -->
    <div class="px-5 py-5 border-b border-[var(--nav-border)] flex items-center justify-between">
        <a href="dashboard.jsp" class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-xl bg-[#EEF0FB] flex items-center justify-center shrink-0">
                <svg role="img" aria-label="Logo" class="w-6 h-6 text-[#5B50F0]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
                    <polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline>
                    <line x1="12" y1="22.08" x2="12" y2="12"></line>
                </svg>
            </div>
            <div class="min-w-0">
                <div class="font-black text-base tracking-tight text-[#1a1d2e]">ImportEase</div>
                <p class="text-[10px] text-[var(--nav-muted)] font-semibold">Plataforma Aduanera</p>
            </div>
        </a>
        <button class="mobile-toggle flex lg:hidden items-center justify-center w-8 h-8 rounded-xl bg-[#f3f4fd] hover:bg-[#eef0fb] transition-all border border-[#e5e8f5] text-[#5B50F0]" aria-label="Cerrar menú">
            <svg role="img" aria-label="Cerrar" class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-3 py-4 overflow-y-auto custom-scrollbar flex flex-col gap-1" role="navigation" aria-label="Navegación principal">
        <% boolean isInicio = "dashboard".equals(activePage); %>
        <a href="dashboard.jsp" class="sidebar-nav-item <%= isInicio ? "is-active" : "" %>">
            <svg role="img" aria-label="Inicio" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"/>
            </svg>
            <span>Inicio</span>
        </a>

        <% boolean isMiImportacion = "wizard".equals(activePage) || "historial".equals(activePage) || "seguimiento".equals(activePage); %>
        <a href="seguimiento.jsp" class="sidebar-nav-item <%= isMiImportacion ? "is-active" : "" %>">
            <svg role="img" aria-label="Mi importación" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M20.25 7.5l-.625 10.632a2.25 2.25 0 01-2.247 2.118H6.622a2.25 2.25 0 01-2.247-2.118L3.75 7.5M10 11.25h4M3.375 7.5h17.25c.621 0 1.125-.504 1.125-1.125v-1.5c0-.621-.504-1.125-1.125-1.125H3.375c-.621 0-1.125.504-1.125 1.125v1.5c0 .621.504 1.125 1.125 1.125z"/>
            </svg>
            <span>Mi importación</span>
        </a>

        <% boolean isBuscar = "buscador".equals(activePage) || "observatorio".equals(activePage); %>
        <a href="buscador.jsp" class="sidebar-nav-item <%= isBuscar ? "is-active" : "" %>">
            <svg role="img" aria-label="Buscar" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
            </svg>
            <span>Buscar producto / código</span>
        </a>

        <% boolean isDocPerm = "documentos".equals(activePage) || "permisos".equals(activePage); %>
        <a href="documentos.jsp" class="sidebar-nav-item <%= isDocPerm ? "is-active" : "" %>">
            <svg role="img" aria-label="Documentos y permisos" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/>
            </svg>
            <span>Documentos y permisos</span>
        </a>

        <% boolean isCostos = "calculadora".equals(activePage) || "comparador".equals(activePage) || "incoterms".equals(activePage); %>
        <a href="calculadora-negocio.jsp" class="sidebar-nav-item <%= isCostos ? "is-active" : "" %>">
            <svg role="img" aria-label="Costos" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
            </svg>
            <span>Costos</span>
        </a>

        <div class="sidebar-divider"></div>

        <button class="sidebar-tools-toggle" type="button">
            <span class="arrow">▾</span> Herramientas
        </button>
        <div class="sidebar-tools-list">
            <% boolean isHs = "buscador".equals(activePage); %>
            <a href="buscador.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isHs ? "is-active" : "" %>">
                <svg role="img" aria-label="Buscar código" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
                </svg>
                <span>Buscar código</span>
            </a>

            <% boolean isPermisos = "permisos".equals(activePage); %>
            <a href="gestor_permisos.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isPermisos ? "is-active" : "" %>">
                <svg role="img" aria-label="Permisos" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                </svg>
                <span>Permisos</span>
            </a>

            <% boolean isCalculadora = "calculadora".equals(activePage); %>
            <a href="calculadora-negocio.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isCalculadora ? "is-active" : "" %>">
                <svg role="img" aria-label="Calcular impuestos" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <span>Calcular impuestos</span>
            </a>

            <% boolean isObservatorio = "observatorio".equals(activePage); %>
            <a href="observatorio-hs.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isObservatorio ? "is-active" : "" %>">
                <svg role="img" aria-label="Tendencias" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zm5.25 0c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v6.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125v-6.75z"/>
                </svg>
                <span>Tendencias</span>
            </a>

            <% boolean isIncoterms = "incoterms".equals(activePage); %>
            <a href="incoterms-lab.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isIncoterms ? "is-active" : "" %>">
                <svg role="img" aria-label="Incoterms" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9m-4.5 16.5V21m0-21v3"/>
                </svg>
                <span>Incoterms</span>
            </a>

            <% boolean isComparador = "comparador".equals(activePage); %>
            <a href="comparador-escenarios.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isComparador ? "is-active" : "" %>">
                <svg role="img" aria-label="Comparar escenarios" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A1.5 1.5 0 016 3.75h15a1.5 1.5 0 011.5 1.5v9a1.5 1.5 0 01-1.5 1.5h-15a1.5 1.5 0 01-1.5-1.5V6z"/>
                </svg>
                <span>Comparar escenarios</span>
            </a>
        </div>

        <div class="sidebar-divider"></div>

        <button type="button" class="sidebar-nav-item" id="sidebar-help-nav-item">
            <svg role="img" aria-label="Ayuda" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z"/>
            </svg>
            <span>Ayuda</span>
        </button>
    </nav>

    <!-- Bottom support widget -->
    <div class="sidebar-support-card">
        <div class="sidebar-support-avatar-wrap">
            <div class="sidebar-support-avatar">
                <svg role="img" aria-label="Soporte" viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M4 18a8 8 0 0 1 16 0" />
                    <circle cx="12" cy="10" r="4" />
                    <path d="M8 10a4 4 0 0 1 8 0" />
                    <rect x="6" y="9" width="2" height="3" rx="1" fill="currentColor" />
                    <rect x="16" y="9" width="2" height="3" rx="1" fill="currentColor" />
                    <path d="M16 11c0 2-2 2-2 2" />
                </svg>
            </div>
        </div>
        <div class="sidebar-support-content">
            <span class="sidebar-support-title">¿Necesitas ayuda?</span>
            <span class="sidebar-support-subtitle">Estamos aquí para acompañarte</span>
            <a href="#" class="sidebar-support-link" id="sidebar-support-contact-btn" role="button">
                Contactar soporte &rarr;
            </a>
        </div>
    </div>
</aside>

<jsp:include page="/fragments/helpModal.jsp" />

<script nonce="<%= escapeJs(String.valueOf(request.getAttribute("csp_nonce"))) %>">
const legacyTextFixes = [
    ['Ã¡', 'a'], ['Ã©', 'e'], ['Ã­', 'i'], ['Ã³', 'o'], ['Ãº', 'u'],
    ['Ã', 'A'], ['Ã‰', 'E'], ['Ã', 'I'], ['Ã“', 'O'], ['Ãš', 'U'],
    ['Ã±', 'n'], ['Ã‘', 'N'], ['Â¿', '¿'], ['Â¡', '¡'], ['â†’', '→'],
    ['â†', '←'], ['âœ•', '×'], ['ðŸ’¡', ''], ['ðŸ‘‰', ''], ['ðŸ“±', ''],
    ['ðŸ¥›', ''], ['ðŸ”¬', ''], ['ðŸŒ±', ''], ['ðŸªµ', ''], ['ðŸ“¦', ''],
    ['ðŸ“‹', ''], ['ðŸŸ¢', ''], ['ðŸ“„', ''], ['ðŸš¢', ''], ['ðŸ›¡ï¸', ''],
    ['âš ï¸', ''], ['ðŸ“¤', ''], ['ðŸ“¥', ''], ['ðŸ—‘ï¸', ''], ['ðŸ”„', ''],
    ['ðŸŒ', ''], ['ðŸ’¾', ''], ['ðŸ“', ''], ['ðŸ”', ''],
    ['NÂ°', 'N°'], ['AÃºn', 'Aun'], ['Se calcularÃ¡', 'Se calculara'],
    ['revisiÃ³n', 'revision'], ['preparaciÃ³n', 'preparacion'], ['SÃ­', 'Si'],
    ['No sÃ©', 'No se'], ['OperaciÃ³n', 'Operacion'], ['InformaciÃ³n', 'Informacion'],
    ['CÃ³digo', 'Codigo'], ['TrÃ¡mite', 'Tramite'], ['ResoluciÃ³n', 'Resolucion'],
    ['ObservaciÃ³n', 'Observacion'], ['tamaÃ±o', 'tamano'], ['Ã©xito', 'exito'],
    ['invÃ¡lido', 'invalido'], ['autorizaciÃ³n', 'autorizacion'], ['aquÃ­', 'aqui']
];

function normalizeLegacyText(root) {
    if (!root) return;

    const normalizeString = (value) => {
        let output = value;
        legacyTextFixes.forEach(([from, to]) => {
            output = output.split(from).join(to);
        });
        return output;
    };

    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
    const textNodes = [];
    while (walker.nextNode()) {
        textNodes.push(walker.currentNode);
    }
    textNodes.forEach(node => {
        const fixed = normalizeString(node.nodeValue || '');
        if (fixed !== node.nodeValue) node.nodeValue = fixed;
    });

    root.querySelectorAll?.('*').forEach(el => {
        ['placeholder', 'title', 'value'].forEach(attr => {
            const current = el.getAttribute && el.getAttribute(attr);
            if (current) {
                const fixed = normalizeString(current);
                if (fixed !== current) el.setAttribute(attr, fixed);
            }
        });
    });
}

function toggleDarkMode(isActive) {
    localStorage.setItem('dark_mode', isActive ? 'true' : 'false');
    document.documentElement.classList.toggle('dark-mode', isActive);
    window.dispatchEvent(new CustomEvent('darkModeChanged', { detail: { active: isActive } }));
}

function toggleSidebar() {
    const sidebar = document.getElementById('appSidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    if (!sidebar) return;

    const isExpanded = sidebar.classList.contains('sidebar-expanded');
    if (isExpanded) {
        sidebar.classList.remove('sidebar-expanded');
        sidebar.classList.add('sidebar-collapsed');
        if (backdrop) backdrop.classList.remove('is-visible');
        document.body.style.overflow = '';
    } else {
        sidebar.classList.add('sidebar-expanded');
        sidebar.classList.remove('sidebar-collapsed');
        if (backdrop) backdrop.classList.add('is-visible');
        document.body.style.overflow = 'hidden';
    }
}

function closeSidebar() {
    const sidebar = document.getElementById('appSidebar');
    const backdrop = document.getElementById('sidebarBackdrop');
    if (!sidebar) return;
    sidebar.classList.remove('sidebar-expanded');
    sidebar.classList.add('sidebar-collapsed');
    if (backdrop) backdrop.classList.remove('is-visible');
    document.body.style.overflow = '';
}

function initSidebar() {
    const isDark = localStorage.getItem('dark_mode') === 'true';
    const darkCheckbox = document.getElementById('sidebarDarkToggle');
    if (darkCheckbox) {
        darkCheckbox.checked = isDark;
        darkCheckbox.addEventListener('change', function() {
            toggleDarkMode(this.checked);
        });
    }
    document.documentElement.classList.toggle('dark-mode', isDark);

    // Close sidebar on escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') closeSidebar();
    });

    // Close sidebar on window resize above breakpoint
    let resizeTimer;
    window.addEventListener('resize', () => {
        clearTimeout(resizeTimer);
        resizeTimer = setTimeout(() => {
            if (window.innerWidth > 900) closeSidebar();
        }, 200);
    });

    // Programmatic event listeners for CSP compliance
    const backdrop = document.getElementById('sidebarBackdrop');
    if (backdrop) {
        backdrop.addEventListener('click', (e) => {
            e.preventDefault();
            toggleSidebar();
        });
    }

    const mobileToggle = document.querySelector('.mobile-toggle');
    if (mobileToggle) {
        mobileToggle.addEventListener('click', (e) => {
            e.preventDefault();
            toggleSidebar();
        });
    }

    const toolsToggle = document.querySelector('.sidebar-tools-toggle');
    if (toolsToggle) {
        toolsToggle.addEventListener('click', function(e) {
            e.preventDefault();
            this.nextElementSibling.classList.toggle('open');
            const arrow = this.querySelector('.arrow');
            if (arrow) arrow.classList.toggle('rotate-180');
        });
    }

    const helpBtn = document.getElementById('sidebar-help-nav-item');
    if (helpBtn) {
        helpBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof openHelpModal === 'function') {
                openHelpModal();
            }
        });
    }

    const supportBtn = document.getElementById('sidebar-support-contact-btn');
    if (supportBtn) {
        supportBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof openHelpModal === 'function') {
                openHelpModal();
            }
        });
    }

    normalizeLegacyText(document.body);

    const observer = new MutationObserver(() => normalizeLegacyText(document.body));
    observer.observe(document.body, { childList: true, subtree: true });
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initSidebar);
} else {
    initSidebar();
}

function confirmLogout() {
    if (confirm('Deseas cerrar sesion?')) {
        doLogout();
    }
}

async function doLogout() {
    const ctxPath = '<%= escapeJs(request.getContextPath()) %>';
    try {
        await fetch(ctxPath + '/api/logout', {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': window.csrfToken || '' },
            credentials: 'same-origin'
        });
    } catch (e) {}
    window.location.replace(ctxPath + '/login.jsp');
}
</script>
