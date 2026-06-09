/**
 * UX Enhancements - Globals for ImportEase
 * Includes: Top Progress Bar (NProgress style), Anti-double click, Session Timeout
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Inyectar Top Progress Bar en el DOM
    const progressBar = document.createElement('div');
    progressBar.id = 'global-progress-bar';
    progressBar.className = 'fixed top-0 left-0 h-1 bg-[var(--accent)] z-[9999] transition-all duration-300 ease-out w-0 shadow-[0_0_10px_var(--accent)]';
    document.body.appendChild(progressBar);

    let progressTimer = null;
    let activeRequests = 0;

    const startProgress = () => {
        if (activeRequests === 0) {
            progressBar.style.opacity = '1';
            progressBar.style.width = '20%';
            let width = 20;
            progressTimer = setInterval(() => {
                width += (100 - width) * 0.1;
                progressBar.style.width = width + '%';
            }, 200);
        }
        activeRequests++;
    };

    const stopProgress = () => {
        activeRequests--;
        if (activeRequests <= 0) {
            activeRequests = 0;
            clearInterval(progressTimer);
            progressBar.style.width = '100%';
            setTimeout(() => {
                progressBar.style.opacity = '0';
                setTimeout(() => { progressBar.style.width = '0%'; }, 300);
            }, 300);
        }
    };

    // Interceptar llamadas Fetch para el Progress Bar
    const originalFetch = window.fetch;
    window.fetch = async (...args) => {
        startProgress();
        try {
            const response = await originalFetch(...args);
            return response;
        } finally {
            stopProgress();
        }
    };

    // Interceptar form submits para Anti-Doble Clic
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => {
            const submitBtn = form.querySelector('button[type="submit"], input[type="submit"]');
            if (submitBtn) {
                if (submitBtn.disabled) {
                    e.preventDefault();
                    return;
                }
                submitBtn.disabled = true;
                const originalText = submitBtn.innerHTML;
                submitBtn.dataset.originalText = originalText;
                submitBtn.innerHTML = `<span class="flex items-center gap-2"><div class="w-3 h-3 border-2 border-white border-t-transparent rounded-full animate-spin"></div> Procesando...</span>`;
                submitBtn.classList.add('opacity-75', 'cursor-not-allowed');
                
                // Timeout seguridad (por si no recarga la pag)
                setTimeout(() => {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = originalText;
                    submitBtn.classList.remove('opacity-75', 'cursor-not-allowed');
                }, 10000);
            }
        });
    });

    // Sesión Timeout (web.xml define 30 minutos = 1800000 ms)
    // Mostramos aviso a los 28 min = 1680000 ms
    let sessionTimeoutWarning;
    const resetSessionTimer = () => {
        clearTimeout(sessionTimeoutWarning);
        sessionTimeoutWarning = setTimeout(() => {
            if (window.showToast) {
                window.showToast("Tu sesión caducará en 2 minutos por inactividad. Mueve el ratón o haz clic para mantenerla viva.", "warning");
            }
        }, 1680000);
    };
    
    // Escuchar actividad básica para "mantener viva" la sesión en UI
    // (Opcional: hacer un fetch a /api/ping si el backend lo requiere estricto)
    ['mousemove', 'keydown', 'click', 'scroll'].forEach(event => {
        window.addEventListener(event, () => {
            resetSessionTimer();
        }, { passive: true });
    });
    resetSessionTimer();
});
