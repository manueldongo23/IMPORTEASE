window.ctx = (window.ImportEase && window.ImportEase.ctx) || window.ctx || '';

/* ── FAQ ── */
function ieFaq(btn) {
    const expanded = btn.getAttribute('aria-expanded') === 'true';
    btn.setAttribute('aria-expanded', String(!expanded));
    btn.nextElementSibling.classList.toggle('open', !expanded);
}

/* ── Avatar dropdown ── */
document.addEventListener('click', function(e) {
    const dd  = document.getElementById('ie-user-dd');
    const btn = document.getElementById('ie-avatar-btn');
    if (dd && btn && !btn.contains(e.target) && !dd.contains(e.target)) dd.classList.remove('open');
});

/* ── Borrador ── */
function borrarBorrador() {
    try {
        localStorage.removeItem('importease_wizard_draft');
    } catch (e) {
        console.warn('localStorage is not accessible:', e);
    }
    actualizarHeroCta();
}
function actualizarHeroCta() {
    let hasDraft = false;
    try {
        hasDraft = !!localStorage.getItem('importease_wizard_draft');
    } catch (e) {
        console.warn('localStorage is not accessible:', e);
    }
    const noDraft  = document.getElementById('heroCta-noDraft');
    const draft    = document.getElementById('heroCta-draft');
    if (!noDraft || !draft) return;
    noDraft.style.display = '';
    draft.style.display   = '';
    if (!hasDraft) {
        draft.style.opacity = '0.75';
    } else {
        draft.style.opacity = '1';
    }
}

/* ── Progreso ── */
function updateProgress(state) {
    const pct  = document.getElementById('ie-prog-pct');
    const bar  = document.getElementById('ie-prog-bar');
    const note = document.getElementById('ie-prog-note');
    if (!pct) return;

    const resetBadges = () => {
        ['step1-badge', 'step2-badge', 'step3-badge'].forEach(id => {
            const el = document.getElementById(id);
            if (!el) return;
            el.className = 'ie-step-badge';
            el.innerHTML = '<span class="ie-step-badge-dot"></span>Pendiente';
        });
    };

    const setBadgeDone = (id) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.className = 'ie-step-badge done';
        el.innerHTML = '<span class="ie-step-badge-dot"></span>Completado';
    };

    resetBadges();

    if (!state) {
        pct.textContent = '0%'; bar.style.width = '0%'; note.textContent = 'Aún no has iniciado';
    } else if (state === 'COTIZACION') {
        pct.textContent = '33%'; bar.style.width = '33%'; note.textContent = 'Primera evaluación lista';
        setBadgeDone('step1-badge');
    } else if (state === 'DOCS_PENDIENTES') {
        pct.textContent = '66%'; bar.style.width = '66%'; note.textContent = 'En proceso';
        setBadgeDone('step1-badge'); setBadgeDone('step2-badge');
    } else {
        pct.textContent = '100%'; bar.style.width = '100%'; note.textContent = '¡Importación completa!';
        ['step1-badge','step2-badge','step3-badge'].forEach(setBadgeDone);
    }
}

/* ── Siguiente paso dinámico ── */
function updateNextStep(ops) {
    if (!ops || !ops.length) return;
    const btn = document.getElementById('ie-next-action-btn');
    if (!btn) return;
    const pending = ops.find(o => o.estado === 'DOCS_PENDIENTES');
    const lista   = ops.find(o => o.estado === 'LISTA_DESPACHO');
    if (pending) {
        btn.href = 'documentos.jsp';
        btn.querySelector('.ie-btn-search-lft').innerHTML = '<svg width="17" height="17" fill="none" stroke="#fff" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/></svg>Subir documentos pendientes';
    } else if (lista) {
        btn.href = 'seguimiento.jsp';
        btn.querySelector('.ie-btn-search-lft').innerHTML = '<svg width="17" height="17" fill="none" stroke="#fff" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7.5 14.25v2.25m3-4.5v4.5m3-6.75v6.75m3-9v9M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z"/></svg>Ver seguimiento';
    }
}

/* ── Fetch helper ── */
async function fetchT(url, opts={}, ms=3500) {
    const c = new AbortController();
    const t = setTimeout(() => c.abort(), ms);
    try { return await fetch(url, {...opts, signal: c.signal}); } finally { clearTimeout(t); }
}

function escapeHtml(v) {
    if (!v) return '';
    return String(v).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

/* ── Stats ── */
async function loadStats() {
    try {
        const res = await fetchT(`${window.ctx}/api/dashboard/stats`);
        if (!res.ok) return;
        const s = await res.json();
        const el = id => document.getElementById(id);
        if (el('kpi-operaciones-badge')) el('kpi-operaciones-badge').textContent = s.totalOps || 0;
        if (el('kpi-permisos-badge'))    el('kpi-permisos-badge').textContent = s.restringidos || 0;
        if (el('kpi-tributos'))          el('kpi-tributos').textContent = `S/ ${Number(s.tributosTotal||0).toLocaleString('es-PE',{maximumFractionDigits:0})}`;
        if (s.tipoCambio && el('kpi-tc')) el('kpi-tc').textContent = `S/ ${Number(s.tipoCambio.tipoCambio||3.75).toFixed(3)}`;
    } catch(e) { console.error('stats', e); }
}

/* ── Operaciones ── */
async function loadOps() {
    try {
        const res = await fetchT(`${window.ctx}/api/importacion/listar`);
        const data = await res.json();
        updateNextStep(data);
        if (data && data.length > 0) {
            data.sort((a,b) => b.id - a.id);
            updateProgress(data[0].estado);
        } else {
            updateProgress(null);
        }
    } catch(e) { console.error('ops', e); }
}

/* ── Init ── */
function initDashboard() {
    actualizarHeroCta();
    loadStats();
    loadOps();

    // Bind FAQ accordion click programmatically to avoid inline CSP block
    document.querySelectorAll('.ie-faq-trigger').forEach(trigger => {
        trigger.removeAttribute('onclick');
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const btn = e.currentTarget;
            const expanded = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', String(!expanded));
            
            // Toggle open class on the next element sibling (the faq body)
            const body = btn.nextElementSibling;
            if (body) {
                body.classList.toggle('open', !expanded);
            }
        });
    });

    // Bind avatar dropdown click programmatically to avoid inline CSP block
    const avatarBtn = document.getElementById('ie-avatar-btn');
    if (avatarBtn) {
        avatarBtn.removeAttribute('onclick');
        avatarBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const dd = document.getElementById('ie-user-dd');
            if (dd) dd.classList.toggle('open');
        });
    }

    // Bind header logout button programmatically for CSP compliance
    const logoutBtn = document.getElementById('header-logout-btn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            if (typeof confirmLogout === 'function') {
                confirmLogout();
            } else {
                if (confirm('¿Estás seguro de que deseas cerrar sesión?')) {
                    window.location.href = window.ctx + '/login.jsp';
                }
            }
        });
    }
}

// Safely execute init on DOM ready, handling scenarios where readystate is already interactive/complete
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDashboard);
} else {
    initDashboard();
}
