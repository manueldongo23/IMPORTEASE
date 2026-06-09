/* auth-login.js - Login Interface Interaction & Submission */

document.addEventListener('DOMContentLoaded', () => {
    // Read context path, CSRF details from global namespace safely
    const ctx = window.ImportEase?.ctx || window.ctx || "";
    const csrfToken = window.ImportEase?.csrfToken || window.csrfToken || "";
    const csrfHeader = window.ImportEase?.csrfHeader || "X-CSRF-TOKEN";

    /* ── Neural Network Canvas Animation ── */
    (() => {
        const canvas = document.getElementById('neuralCanvas');
        if (!canvas) return;
        
        // Prevent duplicate animation runs
        if (canvas.dataset.initialized === 'true') return;
        canvas.dataset.initialized = 'true';

        const ctxCanvas = canvas.getContext('2d');
        let W, H, nodes = [], frame = 0;

        function resize() {
            W = canvas.width  = canvas.offsetWidth;
            H = canvas.height = canvas.offsetHeight;
            initNodes();
        }

        function initNodes() {
            nodes = [];
            const count = Math.floor((W * H) / 9500);
            for (let i = 0; i < count; i++) {
                nodes.push({
                    x: Math.random() * W,
                    y: Math.random() * H,
                    vx: (Math.random() - 0.5) * 0.28,
                    vy: (Math.random() - 0.5) * 0.28,
                    r: Math.random() * 1.5 + 0.6,
                    hue: Math.random() > 0.65 ? 195 : 265, // Cyan & Purple/Violet nodes
                    pulse: Math.random() * Math.PI * 2
                });
            }
        }

        function draw() {
            // Check if element is still in DOM and visible
            if (!document.getElementById('neuralCanvas')) return;
            
            ctxCanvas.clearRect(0, 0, W, H);
            frame++;

            nodes.forEach(n => {
                n.x += n.vx;
                n.y += n.vy;
                if (n.x < 0 || n.x > W) n.vx *= -1;
                if (n.y < 0 || n.y > H) n.vy *= -1;
                n.pulse += 0.012;
            });

            // Connections
            const maxDist = Math.min(W, H) * 0.25;
            for (let i = 0; i < nodes.length; i++) {
                for (let j = i + 1; j < nodes.length; j++) {
                    const a = nodes[i], b = nodes[j];
                    const dx = a.x - b.x, dy = a.y - b.y;
                    const dist = Math.sqrt(dx*dx + dy*dy);
                    if (dist < maxDist) {
                        const alpha = (1 - dist / maxDist) * 0.12;
                        const grad = ctxCanvas.createLinearGradient(a.x, a.y, b.x, b.y);
                        grad.addColorStop(0, `hsla(${a.hue}, 80%, 70%, ${alpha})`);
                        grad.addColorStop(1, `hsla(${b.hue}, 80%, 70%, ${alpha})`);
                        ctxCanvas.beginPath();
                        ctxCanvas.moveTo(a.x, a.y);
                        ctxCanvas.lineTo(b.x, b.y);
                        ctxCanvas.strokeStyle = grad;
                        ctxCanvas.lineWidth = 0.65;
                        ctxCanvas.stroke();
                    }
                }
            }

            // Render Nodes
            nodes.forEach(n => {
                const glow = (Math.sin(n.pulse) * 0.5 + 0.5) * 0.5 + 0.3;
                ctxCanvas.beginPath();
                ctxCanvas.arc(n.x, n.y, n.r, 0, Math.PI * 2);
                ctxCanvas.fillStyle = `hsla(${n.hue}, 85%, 72%, ${glow})`;
                ctxCanvas.fill();

                // Soft radial glow
                ctxCanvas.beginPath();
                ctxCanvas.arc(n.x, n.y, n.r * 4, 0, Math.PI * 2);
                const radGrad = ctxCanvas.createRadialGradient(n.x, n.y, 0, n.x, n.y, n.r * 4);
                radGrad.addColorStop(0, `hsla(${n.hue}, 85%, 72%, ${glow * 0.15})`);
                radGrad.addColorStop(1, `hsla(${n.hue}, 85%, 72%, 0)`);
                ctxCanvas.fillStyle = radGrad;
                ctxCanvas.fill();
            });

            requestAnimationFrame(draw);
        }

        window.addEventListener('resize', resize);
        resize();
        draw();
    })();

    /* ── Toggle Password Visibility ── */
    const togglePwBtn = document.getElementById('togglePw');
    if (togglePwBtn) {
        togglePwBtn.addEventListener('click', function() {
            const inp = document.getElementById('password');
            if (!inp) return;
            const isText = inp.type === 'text';
            inp.type = isText ? 'password' : 'text';
            this.innerHTML = isText
                ? `<svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>`
                : `<svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>`;
        });
    }

    /* ── Captcha Refresh function ── */
    window.refreshCaptcha = function() {
        const captchaImg = document.getElementById('captchaImg');
        const captchaInp = document.getElementById('captcha');
        if (captchaImg) {
            captchaImg.src = (ctx ? ctx + '/' : '') + 'captcha?' + Date.now();
        }
        if (captchaInp) {
            captchaInp.value = '';
        }
    };

    const refreshBtn = document.getElementById('btnRefreshCaptcha');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', window.refreshCaptcha);
    }

    /* ── Toast Notifications ── */
    let toastTimer;
    function showToast(title, msg, success) {
        const el = document.getElementById('toast');
        const iconEl = document.getElementById('toastIcon');
        const tEl = document.getElementById('toastTitle');
        const mEl = document.getElementById('toastMsg');
        
        if (!el || !iconEl || !tEl || !mEl) return;

        iconEl.textContent = success ? '✨' : '⚠️';
        tEl.textContent = title;
        tEl.style.color = success ? '#10b981' : '#ef4444';
        mEl.textContent = msg;
        el.classList.add('show');
        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => el.classList.remove('show'), 4000);
    }

    /* ── Login Form Handler ── */
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnLogin');
            const emailEl = document.getElementById('email');
            const passwordEl = document.getElementById('password');
            const captchaEl = document.getElementById('captcha');

            if (!btn || !emailEl || !passwordEl || !captchaEl) return;

            btn.disabled = true;
            btn.innerHTML = `<div class="auth-spinner"></div> AUTENTICANDO...`;

            try {
                const endpoint = (ctx ? ctx : '') + '/api/usuario/login';
                
                // Safe construction of headers containing optional CSRF
                const reqHeaders = { 'Content-Type': 'application/json' };
                if (csrfToken) {
                    reqHeaders[csrfHeader] = csrfToken;
                }

                const res = await fetch(endpoint, {
                    method: 'POST',
                    headers: reqHeaders,
                    body: JSON.stringify({
                        email: emailEl.value,
                        password: passwordEl.value,
                        captcha: captchaEl.value
                    })
                });

                const data = await res.json();
                if (data.success) {
                    btn.style.background = '#10b981';
                    btn.innerHTML = '✓ ACCESO AUTORIZADO';
                    showToast('Sesión Autorizada', 'Redirigiendo al cockpit de aduanas...', true);
                    setTimeout(() => {
                        window.location.href = 'dashboard.jsp';
                    }, 300);
                } else {
                    showToast('Autenticación Fallida', data.mensaje || 'Credenciales inválidas', false);
                    window.refreshCaptcha();
                    btn.disabled = false;
                    btn.innerHTML = `ACCEDER AL COCKPIT <svg class="arrow-icon" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/></svg>`;
                }
            } catch (err) {
                showToast('Fallo de Red', 'Error de conexión con el servidor corporativo.', false);
                btn.disabled = false;
                btn.innerHTML = `ACCEDER AL COCKPIT <svg class="arrow-icon" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/></svg>`;
            }
        });
    }
});
