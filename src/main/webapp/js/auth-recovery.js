/* auth-recovery.js - Password Recovery Interface Interaction & Submission */

document.addEventListener('DOMContentLoaded', () => {
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
        let W, H;

        function resize() {
            W = canvas.width = canvas.offsetWidth;
            H = canvas.height = canvas.offsetHeight;
        }
        resize();
        window.addEventListener('resize', resize);

        const NODE_COUNT = 50;
        const CONNECTION_DIST = 120;
        const SPEED = 0.25;
        const nodes = [];

        function randomNode() {
            return {
                x: Math.random() * W,
                y: Math.random() * H,
                vx: (Math.random() - 0.5) * SPEED,
                vy: (Math.random() - 0.5) * SPEED
            };
        }
        for (let i = 0; i < NODE_COUNT; i++) nodes.push(randomNode());

        function draw() {
            if (!document.getElementById('neuralCanvas')) return;
            
            ctxCanvas.clearRect(0, 0, W, H);
            for (let i = 0; i < nodes.length; i++) {
                const n = nodes[i];
                n.x += n.vx;
                n.y += n.vy;
                if (n.x < 0 || n.x > W) n.vx *= -1;
                if (n.y < 0 || n.y > H) n.vy *= -1;

                ctxCanvas.beginPath();
                ctxCanvas.arc(n.x, n.y, 1.5, 0, Math.PI * 2);
                ctxCanvas.fillStyle = 'rgba(96,165,250,0.4)';
                ctxCanvas.fill();

                for (let j = i + 1; j < nodes.length; j++) {
                    const m = nodes[j];
                    const dx = n.x - m.x;
                    const dy = n.y - m.y;
                    const dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist < CONNECTION_DIST) {
                        ctxCanvas.beginPath();
                        ctxCanvas.moveTo(n.x, n.y);
                        ctxCanvas.lineTo(m.x, m.y);
                        ctxCanvas.strokeStyle = 'rgba(96,165,250,' + (0.07 * (1 - dist / CONNECTION_DIST)) + ')';
                        ctxCanvas.stroke();
                    }
                }
            }
            requestAnimationFrame(draw);
        }
        draw();
    })();

    /* ── Toast Notifications ── */
    function showToast(title, message, isSuccess) {
        const toast = document.getElementById('toastNotification');
        const icon = document.getElementById('toastIcon');
        const tTitle = document.getElementById('toastTitle');
        const tMsg = document.getElementById('toastMessage');

        if (!toast || !icon || !tTitle || !tMsg) return;

        icon.textContent = isSuccess ? '✨' : '⚠️';
        tTitle.textContent = title;
        tTitle.style.color = isSuccess ? '#10b981' : '#ef4444';
        tMsg.textContent = message;
        toast.classList.add('active');
        setTimeout(() => toast.classList.remove('active'), 4000);
    }

    /* ── Success Modal controls ── */
    window.openModal = function(title, bodyText, infoText, isDirectLink, directUrl) {
        const modal = document.getElementById('successModal');
        const mTitle = document.getElementById('modalTitle');
        const mBody = document.getElementById('modalBodyText');
        const mInfo = document.getElementById('modalInfoText');
        const primaryBtn = document.getElementById('modalPrimaryBtn');

        if (!modal || !mTitle || !mBody || !mInfo || !primaryBtn) return;

        mTitle.textContent = title;
        mBody.innerHTML = bodyText;
        mInfo.textContent = infoText;

        if (isDirectLink && directUrl) {
            primaryBtn.textContent = 'Abrir restablecimiento';
            primaryBtn.onclick = () => { window.location.href = directUrl; };
        } else {
            primaryBtn.textContent = 'Entendido';
            primaryBtn.onclick = window.closeModal;
        }
        modal.classList.add('open');
    };

    window.closeModal = function() {
        const modal = document.getElementById('successModal');
        if (modal) modal.classList.remove('open');
        showToast('Solicitud completada', 'Si el correo existe, recibirás el enlace en tu bandeja.', true);
    };

    const closeBtn = document.getElementById('btnRecuperarCloseModal');
    if (closeBtn) {
        closeBtn.addEventListener('click', window.closeModal);
    }

    /* ── Form Handler ── */
    const recuperarForm = document.getElementById('recuperarForm');
    if (recuperarForm) {
        recuperarForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnEnviar');
            const emailInp = document.getElementById('email');

            if (!btn || !emailInp) return;

            const email = emailInp.value;
            btn.disabled = true;
            btn.innerHTML = `Enviando... <span class="auth-spinner" style="display:inline-block; border-width:1.5px; width:0.8rem; height:0.8rem; margin-left:0.5rem;"></span>`;

            try {
                const endpoint = (ctx ? ctx : '') + '/api/usuario/recuperar';
                
                // Safe construction of headers containing optional CSRF
                const reqHeaders = { 'Content-Type': 'application/json' };
                if (csrfToken) {
                    reqHeaders[csrfHeader] = csrfToken;
                }

                const res = await fetch(endpoint, {
                    method: 'POST',
                    headers: reqHeaders,
                    body: JSON.stringify({ email })
                });
                const data = await res.json();
                if (data.success) {
                    window.openModal(
                        'ImportEase — Solicitud registrada',
                        'Hemos recibido tu solicitud. Si el correo <strong>' + email + '</strong> está registrado, recibirás un enlace seguro.',
                        'Revisa también tu carpeta de spam o correo no deseado. El enlace expira en 15 minutos.',
                        false, null
                    );
                    showToast('Solicitud registrada', data.mensaje || 'Si el correo existe, llegará un enlace.', true);
                } else if (data.resetUrl) {
                    window.openModal(
                        'ImportEase — Enlace directo',
                        'El correo no pudo enviarse, pero puedes restablecer tu contraseña directamente desde el botón de abajo.',
                        'Este enlace es de un solo uso y expira en 15 minutos.',
                        true, data.resetUrl
                    );
                    showToast('Correo no enviado', 'Usa el enlace directo disponible.', false);
                } else {
                    showToast('Solicitud denegada', data.mensaje || 'Error al procesar la solicitud.', false);
                }
            } catch(e) {
                showToast('Fallo de conexión', 'No pudimos contactar con el servidor corporativo.', false);
            } finally {
                btn.disabled = false;
                btn.innerHTML = `Enviar enlace de recuperación <div class="auth-circle-arrow"><svg fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5L21 12m0 0l-7.5 7.5M21 12H3"/></svg></div>`;
            }
        });
    }
});
