<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    String activePage = (String) request.getAttribute("activePage");
    if (activePage == null) activePage = "";
    String userNombreSidebar = (String) session.getAttribute("usuarioNombre");
    if (userNombreSidebar == null || userNombreSidebar.trim().isEmpty()) userNombreSidebar = "Usuario";
    String userEmail = (String) session.getAttribute("usuarioEmail");
    if (userEmail == null) userEmail = "";
    String initials = (String) request.getAttribute("userInitials");
    if (initials == null || initials.isEmpty()) {
        initials = "US";
        String[] parts = userNombreSidebar.trim().split("\\s+");
        if (parts.length >= 2) {
            initials = (parts[0].substring(0,1) + parts[1].substring(0,1)).toUpperCase();
        } else if (parts.length == 1 && parts[0].length() >= 1) {
            initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
    }
%>
<script nonce="<%= request.getAttribute("csp_nonce") %>">
    window.csrfToken = '<%= com.importease.proyecto.service.CsrfUtil.getToken(session) %>';
    (function() {
        if (localStorage.getItem('dark_mode') === 'true') {
            document.documentElement.classList.add('dark-mode');
        }
    })();
</script>

<style nonce="<%= request.getAttribute("csp_nonce") %>">
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
<div id="sidebarBackdrop" class="sidebar-backdrop" onclick="toggleSidebar()"></div>

<aside id="appSidebar" class="app-sidebar w-64 bg-[var(--nav-bg)] border-r border-[var(--nav-border)] flex flex-col h-full z-50 shrink-0 select-none text-[var(--nav-text)] font-['Outfit'] shadow-2xl">
    <!-- Brand -->
    <div class="px-5 py-5 border-b border-[var(--nav-border)] flex items-center justify-between">
        <a href="dashboard.jsp" class="flex items-center gap-3 min-w-0">
            <div class="w-10 h-10 rounded-2xl bg-white/10 border border-white/10 flex items-center justify-center shrink-0">
                <svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 12h18M12 3c4.971 0 9 4.029 9 9s-4.029 9-9 9-9-4.029-9-9 4.029-9 9-9z"/>
                </svg>
            </div>
            <div class="min-w-0">
                <h1 class="font-black text-base tracking-tight text-white">ImportEase</h1>
                <p class="text-[10px] text-[var(--nav-muted)] font-semibold">Plataforma Aduanera</p>
            </div>
        </a>
        <button onclick="toggleSidebar()" class="mobile-toggle flex lg:hidden items-center justify-center w-8 h-8 rounded-xl bg-white/5 hover:bg-white/10 transition-all border border-white/5 text-white">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M6 18L18 6M6 6l12 12"/>
            </svg>
        </button>
    </div>

    <!-- Quick start hint -->
    <div class="px-4 pt-4">
        <div class="rounded-xl bg-[var(--nav-bg-soft)] border border-[var(--nav-border)] px-3.5 py-2.5">
            <p class="text-[9px] uppercase tracking-[0.2em] font-black text-[var(--accent)]">Empieza aqui</p>
            <p class="text-[11px] text-[var(--nav-muted)] font-semibold mt-1.5 leading-relaxed">Describe tu producto en palabras simples. El sistema lo traduce a código, permisos, costos y documentos.</p>
        </div>
    </div>

    <!-- Navigation -->
    <nav class="flex-1 px-3 py-4 overflow-y-auto custom-scrollbar">
        <%-- Main navigation --%>
        <span class="sidebar-section-label">Navegación</span>

        <% boolean isInicio = "dashboard".equals(activePage); %>
        <a href="dashboard.jsp" class="sidebar-nav-item <%= isInicio ? "is-active" : "" %>">
            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"/>
            </svg>
            <span>Inicio</span>
        </a>

        <% boolean isWizard = "wizard".equals(activePage); %>
        <a href="evaluacion.jsp" class="sidebar-nav-item sidebar-nav-item-accent <%= isWizard ? "is-active" : "" %>">
            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M3 16.5v2.25A2.25 2.25 0 005.25 21h13.5A2.25 2.25 0 0021 18.75V16.5M16.5 12L12 16.5m0 0L7.5 12m4.5 4.5V3"/>
            </svg>
            <span>Importar</span>
        </a>

        <% boolean isDocumentos = "documentos".equals(activePage); %>
        <a href="documentos.jsp" class="sidebar-nav-item <%= isDocumentos ? "is-active" : "" %>">
            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12.75V12A2.25 2.25 0 014.5 9.75h15A2.25 2.25 0 0121.75 12v.75m-8.625-3.375a2.25 2.25 0 00-3.375-3.375h-1.5a1.5 1.5 0 00-1.5 1.5v7.925M2.25 12.75a2.25 2.25 0 002.25 2.25h15a2.25 2.25 0 002.25-2.25m-19.5 0v5.625c0 .621.504 1.125 1.125 1.125h17.25c.621 0 1.125-.504 1.125-1.125v-5.625"/>
            </svg>
            <span>Expediente</span>
        </a>

        <% boolean isHistorial = "historial".equals(activePage); %>
        <a href="seguimiento.jsp" class="sidebar-nav-item <%= isHistorial ? "is-active" : "" %>">
            <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" d="M7.5 14.25v2.25m3-4.5v4.5m3-6.75v6.75m3-9v9M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z"/>
            </svg>
            <span>Historial</span>
        </a>

        <div class="sidebar-divider"></div>

        <button class="sidebar-tools-toggle" onclick="this.nextElementSibling.classList.toggle('open'); this.querySelector('.arrow').classList.toggle('rotate-180')">
            <span class="arrow" style="transition:transform 0.2s">▾</span> Herramientas
        </button>
        <div class="sidebar-tools-list">
            <% boolean isHs = "buscador".equals(activePage); %>
            <a href="buscador.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isHs ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/>
                </svg>
                <span>Buscar código</span>
            </a>

            <% boolean isPermisos = "permisos".equals(activePage); %>
            <a href="gestor_permisos.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isPermisos ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/>
                </svg>
                <span>Permisos</span>
            </a>

            <% boolean isCalculadora = "calculadora".equals(activePage); %>
            <a href="calculadora-negocio.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isCalculadora ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m-3-2.818l.879.659c1.171.879 3.07.879 4.242 0 1.172-.879 1.172-2.303 0-3.182C13.536 12.219 12.768 12 12 12c-.725 0-1.45-.22-2.003-.659-1.106-.879-1.106-2.303 0-3.182s2.9-.879 4.006 0l.415.33M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/>
                </svg>
                <span>Calcular impuestos</span>
            </a>

            <% boolean isObservatorio = "observatorio".equals(activePage); %>
            <a href="observatorio-hs.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isObservatorio ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3 13.125C3 12.504 3.504 12 4.125 12h2.25c.621 0 1.125.504 1.125 1.125v6.75C7.5 20.496 6.996 21 6.375 21h-2.25A1.125 1.125 0 013 19.875v-6.75zm5.25 0c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125v6.75c0 .621-.504 1.125-1.125 1.125h-2.25a1.125 1.125 0 01-1.125-1.125v-6.75z"/>
                </svg>
                <span>Tendencias</span>
            </a>

            <% boolean isIncoterms = "incoterms".equals(activePage); %>
            <a href="incoterms-lab.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isIncoterms ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9m-4.5 16.5V21m0-21v3"/>
                </svg>
                <span>Incoterms</span>
            </a>

            <% boolean isComparador = "comparador".equals(activePage); %>
            <a href="comparador-escenarios.jsp" class="sidebar-nav-item sidebar-nav-item-sm <%= isComparador ? "is-active" : "" %>">
                <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6A1.5 1.5 0 016 3.75h15a1.5 1.5 0 011.5 1.5v9a1.5 1.5 0 01-1.5 1.5h-15a1.5 1.5 0 01-1.5-1.5V6z"/>
                </svg>
                <span>Comparar escenarios</span>
            </a>
        </div>
    </nav>

    <!-- Dark mode toggle -->
    <div class="px-4 pb-2">
        <div class="rounded-xl bg-[var(--nav-bg-soft)] border border-[var(--nav-border)] p-3.5">
            <div class="flex items-center justify-between">
                <span class="text-xs font-semibold text-[var(--nav-text)]">Modo oscuro</span>
                <label class="relative inline-flex items-center cursor-pointer select-none">
                    <input type="checkbox" id="sidebarDarkToggle" class="sr-only peer" onchange="toggleDarkMode(this.checked)">
                    <div class="w-9 h-5 bg-white/20 rounded-full peer peer-checked:bg-white/80 after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:w-4 after:h-4 after:bg-white after:rounded-full after:transition-all peer-checked:after:translate-x-4"></div>
                </label>
            </div>
            <p class="text-[9px] text-[var(--nav-muted)] font-semibold leading-relaxed mt-2">Usa primero "Importar". Las herramientas están agrupadas abajo.</p>
        </div>
    </div>

    <!-- User bottom section -->
    <div class="p-3 border-t border-[var(--nav-border)] bg-[var(--nav-bg-soft)]">
        <div class="flex items-center gap-2.5">
            <div class="w-9 h-9 rounded-xl bg-white/10 border border-white/10 flex items-center justify-center font-black text-sm text-[var(--accent)] shrink-0">
                <%= com.importease.proyecto.service.HtmlUtil.escape(initials) %>
            </div>
            <div class="min-w-0 flex-1">
                <p class="text-sm font-bold text-white truncate"><%= com.importease.proyecto.service.HtmlUtil.escape(userNombreSidebar) %></p>
                <p class="text-[9px] text-[var(--nav-muted)] font-semibold">Sesión activa</p>
            </div>
            <div class="flex items-center gap-1 shrink-0">
                <button onclick="openHelpModal()" title="Ayuda" class="w-8 h-8 rounded-xl bg-white/5 hover:bg-white/10 transition-all flex items-center justify-center border border-white/5">
                    <svg class="w-3.5 h-3.5 text-white" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                </button>
                <button onclick="confirmLogout()" title="Cerrar sesión" class="w-8 h-8 rounded-xl bg-[var(--accent)]/10 hover:bg-[var(--accent)]/20 transition-all flex items-center justify-center border border-[var(--accent)]/20">
                    <svg class="w-3.5 h-3.5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 9V5.25A2.25 2.25 0 0013.5 3h-6a2.25 2.25 0 00-2.25 2.25v13.5A2.25 2.25 0 007.5 21h6a2.25 2.25 0 002.25-2.25V15M12 9l-3 3m0 0l3 3m-3-3h12.75" />
                    </svg>
                </button>
            </div>
        </div>
    </div>
</aside>

<jsp:include page="/fragments/helpModal.jsp" />

<script nonce="<%= request.getAttribute("csp_nonce") %>">
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

document.addEventListener('DOMContentLoaded', () => {
    const isDark = localStorage.getItem('dark_mode') === 'true';
    const darkCheckbox = document.getElementById('sidebarDarkToggle');
    if (darkCheckbox) darkCheckbox.checked = isDark;
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

    normalizeLegacyText(document.body);

    const observer = new MutationObserver(() => normalizeLegacyText(document.body));
    observer.observe(document.body, { childList: true, subtree: true });
});

function confirmLogout() {
    if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
        doLogout();
    }
}

async function doLogout() {
    try {
        const ctxPath = '<%= request.getContextPath() %>';
        await fetch(ctxPath + '/api/usuario/logout', {
            method: 'POST',
            headers: { 'X-CSRF-TOKEN': window.csrfToken || '' }
        });
    } catch (e) {}
    window.location.href = '<%= request.getContextPath() %>/login.jsp';
}
</script>
