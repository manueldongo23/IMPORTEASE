// common.js – shared utilities, toast handling, AJAX wrapper

/**
 * Simple wrapper for fetch with CSRF token and JSON handling.
 */
async function apiFetch(url, options = {}) {
  const ctx = window.ImportEase && window.ImportEase.ctx ? window.ImportEase.ctx : (window.ctx || '');
  const headers = {
    'Content-Type': 'application/json',
    'X-CSRF-TOKEN': window.csrfToken,
    ...options.headers,
  };
  const response = await fetch(ctx + url, { ...options, headers });
  if (!response.ok) {
    const err = await response.text();
    console.error('API error', response.status, err);
    throw new Error(err || 'Server error');
  }
  return response.json();
}

/**
 * Show a simple toast message (info, success, warning, error).
 */
function showToast(message, type = 'info') {
  const toastContainer = document.getElementById('toastContainer');
  if (!toastContainer) return;
  const toast = document.createElement('div');
  toast.className = `alert alert-${type} shadow`;
  toast.textContent = message;
  toastContainer.appendChild(toast);
  setTimeout(() => toast.remove(), 5000);
}

function initNeuralCanvas(canvasId, opts) {
    opts = opts || {};
    const canvas = document.getElementById(canvasId);
    if (!canvas || canvas.dataset.initialized === 'true') return;
    canvas.dataset.initialized = 'true';
    const ctx = canvas.getContext('2d');
    const NODE_COUNT = opts.nodeCount || 50;
    const CONNECTION_DIST = opts.connectionDist || 120;
    const SPEED = opts.speed || 0.25;
    const COLOR = opts.color || 'rgba(96,165,250,0.4)';
    const LINE_COLOR_BASE = opts.lineColor || 'rgba(96,165,250,';
    let W, H;
    function resize() { W = canvas.width = canvas.offsetWidth; H = canvas.height = canvas.offsetHeight; }
    resize();
    window.addEventListener('resize', resize);
    const nodes = Array.from({ length: NODE_COUNT }, () => ({
        x: Math.random() * (W || 600), y: Math.random() * (H || 400),
        vx: (Math.random() - 0.5) * SPEED, vy: (Math.random() - 0.5) * SPEED
    }));
    function draw() {
        if (!document.getElementById(canvasId)) return;
        const w = canvas.width, h = canvas.height;
        ctx.clearRect(0, 0, w, h);
        for (let i = 0; i < nodes.length; i++) {
            const n = nodes[i];
            n.x += n.vx; n.y += n.vy;
            if (n.x < 0 || n.x > w) n.vx *= -1;
            if (n.y < 0 || n.y > h) n.vy *= -1;
            ctx.beginPath();
            ctx.arc(n.x, n.y, 1.5, 0, Math.PI * 2);
            ctx.fillStyle = COLOR;
            ctx.fill();
            for (let j = i + 1; j < nodes.length; j++) {
                const m = nodes[j];
                const dx = n.x - m.x, dy = n.y - m.y, dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < CONNECTION_DIST) {
                    ctx.beginPath();
                    ctx.moveTo(n.x, n.y);
                    ctx.lineTo(m.x, m.y);
                    ctx.strokeStyle = LINE_COLOR_BASE + (0.07 * (1 - dist / CONNECTION_DIST)) + ')';
                    ctx.stroke();
                }
            }
        }
        requestAnimationFrame(draw);
    }
    draw();
}
window.showNotification = function(title, message, isSuccess = false) {
    const toast = document.getElementById('toastNotification');
    const icon = document.getElementById('toastIcon');
    const tTitle = document.getElementById('toastTitle');
    const tMsg = document.getElementById('toastMessage');
    
    if (!toast || !icon || !tTitle || !tMsg) return;

    icon.innerText = isSuccess ? '✨' : '⚠️';
    tTitle.innerText = title;
    tTitle.className = `text-xs font-black uppercase tracking-wider ${isSuccess ? 'text-emerald-600' : 'text-rose-500'}`;
    tMsg.innerText = message;
    
    toast.className = `fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 px-6 py-4.5 rounded-2xl border bg-[var(--surface-1)] shadow-2xl transition-all duration-500 opacity-0 pointer-events-none scale-95 max-w-md w-[calc(100%-3rem)] ${isSuccess ? 'border-emerald-100 dark:border-emerald-950' : 'border-rose-100 dark:border-rose-950'}`;
    
    // Trigger animation
    setTimeout(() => {
        toast.classList.add('toast-active');
        toast.style.opacity = '1';
        toast.style.transform = 'translate(-50%, 0) scale(1)';
        toast.style.pointerEvents = 'auto';
    }, 50);
    
    // Hide after 4 seconds
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translate(-50%, 0) scale(0.95)';
        toast.style.pointerEvents = 'none';
    }, 4000);
};

const API = {
    LOGIN: '/api/usuario/login',
    LOGOUT: '/api/usuario/logout',
    REGISTER: '/api/usuario/registro',
    RECOVERY: '/api/usuario/recuperar',
    RESET: '/api/usuario/resetear',
    PERFIL: '/api/usuario/perfil',
    EVALUAR_PERMISOS: '/api/permisos/evaluar',
    PREGUNTAS_PERMISOS: '/api/permisos/preguntas',
    DOCUMENTOS_PERMISOS: '/api/permisos/documentos',
    SOLICITUDES_PERMISO: '/api/permisos/solicitudes',
    DASHBOARD_STATS: '/api/dashboard/stats',
    DASHBOARD_OPERACIONES: '/api/dashboard/operaciones',
};
