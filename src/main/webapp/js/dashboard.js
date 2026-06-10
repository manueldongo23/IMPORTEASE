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
        hasDraft = !!localStorage.getItem('importease_wizard_draft') || !!localStorage.getItem('importWizard');
    } catch (e) {
        console.warn('localStorage is not accessible:', e);
    }
    const noDraft  = document.getElementById('heroCta-noDraft');
    const draft    = document.getElementById('heroCta-draft');
    if (!noDraft || !draft) return;
    noDraft.style.display = '';
    draft.style.display   = '';
    if (!hasDraft) {
        draft.style.opacity = '0.5';
        draft.style.pointerEvents = 'none';
    } else {
        draft.style.opacity = '1';
        draft.style.pointerEvents = 'auto';
    }
}

/* ── Progreso y 10 Pasos ── */
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

    let progPctVal = '0%';
    let progBarWidth = '0%';
    let progNoteText = 'Aún no has iniciado';

    if (!state) {
        progPctVal = '0%'; progBarWidth = '0%'; progNoteText = 'Aún no has iniciado';
    } else if (state === 'COTIZACION') {
        progPctVal = '33%'; progBarWidth = '33%'; progNoteText = 'Primera evaluación lista';
        setBadgeDone('step1-badge');
    } else if (state === 'DOCS_PENDIENTES') {
        progPctVal = '66%'; progBarWidth = '66%'; progNoteText = 'En proceso';
        setBadgeDone('step1-badge'); setBadgeDone('step2-badge');
    } else {
        progPctVal = '100%'; progBarWidth = '100%'; progNoteText = '¡Importación completa!';
        ['step1-badge','step2-badge','step3-badge'].forEach(setBadgeDone);
    }

    pct.textContent = progPctVal;
    if (bar) bar.style.width = progBarWidth;
    if (note) note.textContent = progNoteText;

    // Update KPI Card 1: Progreso de Importación
    const kpiProgVal = document.getElementById('kpi-progreso-val');
    const kpiProgBadge = document.getElementById('kpi-progreso-badge');
    if (kpiProgVal && kpiProgBadge) {
        kpiProgVal.textContent = progPctVal;
        if (!state) {
            kpiProgBadge.className = 'badge bg-secondary';
            kpiProgBadge.style.background = '#e2e8f0';
            kpiProgBadge.style.color = '#475569';
            kpiProgBadge.textContent = 'Sin iniciar';
        } else if (state === 'COTIZACION') {
            kpiProgBadge.className = 'badge bg-info';
            kpiProgBadge.style.background = '#e0f2fe';
            kpiProgBadge.style.color = '#0369a1';
            kpiProgBadge.textContent = 'Cotizado';
        } else if (state === 'DOCS_PENDIENTES') {
            kpiProgBadge.className = 'badge bg-warning text-dark';
            kpiProgBadge.style.background = '#fef3c7';
            kpiProgBadge.style.color = '#92400e';
            kpiProgBadge.textContent = 'Docs Pendientes';
        } else if (state === 'LISTA_DESPACHO') {
            kpiProgBadge.className = 'badge bg-primary';
            kpiProgBadge.style.background = '#dbeafe';
            kpiProgBadge.style.color = '#1e40af';
            kpiProgBadge.textContent = 'Listo Despacho';
        } else if (state === 'TRANSITO') {
            kpiProgBadge.className = 'badge bg-primary';
            kpiProgBadge.style.background = '#e0e7ff';
            kpiProgBadge.style.color = '#3730a3';
            kpiProgBadge.textContent = 'En Tránsito';
        } else if (state === 'NACIONALIZADA') {
            kpiProgBadge.className = 'badge bg-success';
            kpiProgBadge.style.background = '#d3f9d8';
            kpiProgBadge.style.color = '#2b8a3e';
            kpiProgBadge.textContent = 'Nacionalizada';
        }
    }

    // Update 10-Step Timeline
    let activeStepIndex = 0;
    let timelineProgress = 0;
    if (state === 'COTIZACION') {
        activeStepIndex = 5; // step 5: Costos
        timelineProgress = 45;
    } else if (state === 'DOCS_PENDIENTES') {
        activeStepIndex = 6; // step 6: Documentos
        timelineProgress = 55;
    } else if (state === 'LISTA_DESPACHO') {
        activeStepIndex = 8; // step 8: Preparación
        timelineProgress = 75;
    } else if (state === 'TRANSITO') {
        activeStepIndex = 9; // step 9: Seguimiento
        timelineProgress = 88;
    } else if (state === 'NACIONALIZADA') {
        activeStepIndex = 10; // step 10: Cierre
        timelineProgress = 100;
    }

    const tProgressBar = document.getElementById('timeline-progress-bar');
    if (tProgressBar) {
        tProgressBar.style.width = `${timelineProgress}%`;
    }

    for (let i = 1; i <= 10; i++) {
        const circle = document.getElementById(`t-circle-${i}`);
        if (!circle) continue;

        if (activeStepIndex === 0) {
            circle.className = 'ie-timeline-circle pending';
            circle.innerHTML = `${i}`;
        } else if (i < activeStepIndex) {
            circle.className = 'ie-timeline-circle completed';
            circle.innerHTML = `<svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/></svg>`;
        } else if (i === activeStepIndex) {
            if (state === 'NACIONALIZADA') {
                circle.className = 'ie-timeline-circle completed';
                circle.innerHTML = `<svg width="12" height="12" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5"/></svg>`;
            } else {
                circle.className = 'ie-timeline-circle active';
                circle.innerHTML = `${i}`;
            }
        } else {
            circle.className = 'ie-timeline-circle pending';
            circle.innerHTML = `${i}`;
        }
    }

    const timelineStatus = document.getElementById('timeline-global-status');
    if (timelineStatus) {
        if (!state) {
            timelineStatus.className = 'ie-step-badge';
            timelineStatus.style.background = '#f1f3f9';
            timelineStatus.style.color = '#64748b';
            timelineStatus.innerHTML = '<span class="ie-step-badge-dot"></span>Sin Importaciones';
        } else {
            timelineStatus.className = 'ie-step-badge done';
            timelineStatus.style.background = '#e6fcf5';
            timelineStatus.style.color = '#0ca678';
            timelineStatus.innerHTML = '<span class="ie-step-badge-dot"></span>Operación Activa';
        }
    }
}

/* ── Siguiente paso dinámico ── */
function updateNextStep(ops) {
    if (!ops || !ops.length) return;
    const btn = document.getElementById('ie-next-action-btn');
    if (!btn) return;
    const pending = ops.find(o => o.estado === 'DOCS_PENDIENTES');
    const lista   = ops.find(o => o.estado === 'LISTA_DESPACHO');
    const transito = ops.find(o => o.estado === 'TRANSITO');
    
    if (pending) {
        btn.href = 'documentos.jsp';
        btn.querySelector('.ie-btn-search-lft').innerHTML = '<svg width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/></svg>Subir documentos pendientes';
    } else if (lista || transito) {
        btn.href = 'seguimiento.jsp';
        btn.querySelector('.ie-btn-search-lft').innerHTML = '<svg width="17" height="17" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M7.5 14.25v2.25m3-4.5v4.5m3-6.75v6.75m3-9v9M6 20.25h12A2.25 2.25 0 0020.25 18V6A2.25 2.25 0 0018 3.75H6A2.25 2.25 0 003.75 6v12A2.25 2.25 0 006 20.25z"/></svg>Ver seguimiento';
    } else {
        btn.href = 'buscador.jsp';
        btn.querySelector('.ie-btn-search-lft').innerHTML = '<svg width="17" height="17" fill="none" stroke="currentColor" stroke-width="2.2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="m21 21-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z"/></svg>Buscar mi código';
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
            const latestOp = data[0];
            updateProgress(latestOp.estado);

            // Fetch Documents for Card 2
            if (latestOp.estado === 'DOCS_PENDIENTES') {
                try {
                    const docRes = await fetchT(`${window.ctx}/api/documentos/listar?importacionId=${latestOp.id}`);
                    if (docRes.ok) {
                        const docs = await docRes.json();
                        const uploadedCount = docs.filter(d => d.ruta_archivo).length;
                        const pendingCount = Math.max(0, 3 - uploadedCount);
                        document.getElementById('kpi-docs-val').textContent = pendingCount;
                        const badge = document.getElementById('kpi-docs-badge');
                        if (badge) {
                            badge.className = pendingCount > 0 ? 'badge bg-warning text-dark' : 'badge bg-success';
                            badge.style.background = pendingCount > 0 ? '#fef3c7' : '#d3f9d8';
                            badge.style.color = pendingCount > 0 ? '#92400e' : '#2b8a3e';
                            badge.textContent = pendingCount > 0 ? `Subidos: ${uploadedCount}/3` : 'Completo';
                        }
                    }
                } catch (docErr) {
                    console.error('docs stats', docErr);
                }
            } else if (latestOp.estado === 'LISTA_DESPACHO' || latestOp.estado === 'TRANSITO' || latestOp.estado === 'NACIONALIZADA') {
                document.getElementById('kpi-docs-val').textContent = '0';
                const badge = document.getElementById('kpi-docs-badge');
                if (badge) {
                    badge.className = 'badge bg-success';
                    badge.style.background = '#d3f9d8';
                    badge.style.color = '#2b8a3e';
                    badge.textContent = 'Completo';
                }
            } else {
                document.getElementById('kpi-docs-val').textContent = '3';
                const badge = document.getElementById('kpi-docs-badge');
                if (badge) {
                    badge.className = 'badge bg-secondary';
                    badge.style.background = '#e2e8f0';
                    badge.style.color = '#475569';
                    badge.textContent = 'No iniciado';
                }
            }

            // Fetch Risk and Difficulty for Card 4
            if (latestOp.hsCode) {
                try {
                    const cifVal = latestOp.valorCifBD || latestOp.valorCif || latestOp.cif || 0;
                    const riskRes = await fetchT(`${window.ctx}/api/riesgo/evaluar?hsCode=${latestOp.hsCode}&cif=${cifVal}&usuarioId=${latestOp.usuarioId}`);
                    if (riskRes.ok) {
                        const r = await riskRes.json();
                        let riesgoNivel = 'Bajo';
                        let badgeClass = 'badge bg-success';
                        let badgeText = 'Fácil';
                        let iconBgColor = '#E6FCF5';
                        let iconTextColor = '#0CA678';
                        let badgeBg = '#d3f9d8';
                        let badgeColor = '#2b8a3e';

                        if (r.canal === 'ROJO') {
                            riesgoNivel = 'Alto';
                            badgeClass = 'badge bg-danger';
                            badgeText = 'Difícil';
                            iconBgColor = '#FFE3E3';
                            iconTextColor = '#FA5252';
                            badgeBg = '#ffe3e3';
                            badgeColor = '#c92a2a';
                        } else if (r.canal === 'NARANJA') {
                            riesgoNivel = 'Medio';
                            badgeClass = 'badge bg-warning text-dark';
                            badgeText = 'Medio';
                            iconBgColor = '#FFF9DB';
                            iconTextColor = '#F59F00';
                            badgeBg = '#fff9db';
                            badgeColor = '#e67700';
                        }

                        document.getElementById('kpi-riesgo-val').textContent = riesgoNivel;
                        const rBadge = document.getElementById('kpi-riesgo-badge');
                        if (rBadge) {
                            rBadge.className = badgeClass;
                            rBadge.style.background = badgeBg;
                            rBadge.style.color = badgeColor;
                            rBadge.textContent = badgeText;
                        }
                        
                        const iconBg = document.getElementById('kpi-riesgo-icon-bg');
                        if (iconBg) {
                            iconBg.style.background = iconBgColor;
                            iconBg.style.color = iconTextColor;
                        }
                    }
                } catch (riskErr) {
                    console.error('risk evaluation err', riskErr);
                }
            }

            // Fetch Taxes for Card 3
            if (latestOp.totalImpuestosBD || latestOp.totalImpuestos !== undefined) {
                const taxes = latestOp.totalImpuestosBD || latestOp.totalImpuestos || 0;
                document.getElementById('kpi-tributos').textContent = `S/ ${Number(taxes).toLocaleString('es-PE', { maximumFractionDigits: 0 })}`;
            }

        } else {
            updateProgress(null);
            document.getElementById('kpi-docs-val').textContent = '3';
            const badge = document.getElementById('kpi-docs-badge');
            if (badge) {
                badge.className = 'badge bg-secondary';
                badge.style.background = '#e2e8f0';
                badge.style.color = '#475569';
                badge.textContent = 'No iniciado';
            }
            
            document.getElementById('kpi-riesgo-val').textContent = 'Bajo';
            const rBadge = document.getElementById('kpi-riesgo-badge');
            if (rBadge) {
                rBadge.className = 'badge bg-info';
                rBadge.style.background = '#e0f2fe';
                rBadge.style.color = '#0369a1';
                rBadge.textContent = 'Fácil';
            }

            document.getElementById('kpi-tributos').textContent = 'S/ 0';
        }
    } catch(e) { console.error('ops', e); }
}

/* ── Init ── */
function initDashboard() {
    actualizarHeroCta();
    loadStats();
    loadOps();

    // Bind experience select & checkbox changes
    const expSelect = document.getElementById('user-experience-select');
    const tipsCheckbox = document.getElementById('user-tips-checkbox');
    if (expSelect) {
        expSelect.addEventListener('change', () => savePreferences());
    }
    if (tipsCheckbox) {
        tipsCheckbox.addEventListener('change', () => savePreferences());
    }

    async function savePreferences() {
        if (!expSelect || !tipsCheckbox) return;
        const csrfToken  = (window.ImportEase && window.ImportEase.csrfToken) || window.csrfToken || '';
        const csrfHeader = (window.ImportEase && window.ImportEase.csrfHeader) || 'X-CSRF-TOKEN';
        
        const payload = {
            nivelExperiencia: expSelect.value,
            preferencias: JSON.stringify({ ocultarConsejos: !tipsCheckbox.checked })
        };
        try {
            const res = await fetch(`${window.ctx}/api/usuario/preferencias`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                window.location.reload();
            } else {
                console.error('Error al guardar preferencias');
            }
        } catch (e) {
            console.error('Error saving prefs', e);
        }
    }

    // Bind FAQ accordion click programmatically to avoid inline CSP block
    document.querySelectorAll('.ie-faq-trigger').forEach(trigger => {
        trigger.removeAttribute('onclick');
        trigger.addEventListener('click', function(e) {
            e.preventDefault();
            const btn = e.currentTarget;
            const expanded = btn.getAttribute('aria-expanded') === 'true';
            btn.setAttribute('aria-expanded', String(!expanded));
            
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
        logoutBtn.addEventListener('click', async (e) => {
            e.preventDefault();
            if (confirm('Deseas cerrar sesion?')) {
                if (typeof window.doLogout === 'function') {
                    window.doLogout();
                    return;
                }
                try {
                    const csrfToken = (window.ImportEase && window.ImportEase.csrfToken) || window.csrfToken || '';
                    const csrfHeader = (window.ImportEase && window.ImportEase.csrfHeader) || 'X-CSRF-TOKEN';
                    await fetch(`${window.ctx}/api/logout`, {
                        method: 'POST',
                        headers: { [csrfHeader]: csrfToken },
                        credentials: 'same-origin'
                    });
                } catch (err) {
                    console.warn('logout', err);
                }
                window.location.replace(`${window.ctx}/login.jsp`);
            }
        });
    }
}

// Safely execute init on DOM ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initDashboard);
} else {
    initDashboard();
}
