/* auth-reset.js - Reset Password Interface Interaction & Submission */

document.addEventListener('DOMContentLoaded', () => {
    const ctx = window.ImportEase?.ctx || window.ctx || "";
    const csrfToken = window.ImportEase?.csrfToken || window.csrfToken || "";
    const csrfHeader = window.ImportEase?.csrfHeader || "X-CSRF-TOKEN";

    // Extract query parameters safely (do not expose or log token)
    const params = new URLSearchParams(window.location.search);
    const emailParam = params.get('email') || '';
    const tokenParam = params.get('token') || '';
    const resetLinkValid = Boolean(emailParam && tokenParam);

    // Prepopulate disabled email field
    const emailInp = document.getElementById('email');
    if (emailInp) {
        emailInp.value = emailParam;
    }

    /* ── Show Notifications ── */
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

    /* ── Bind Event Listeners for Password Visibility (No inline onclick) ── */
    document.querySelectorAll('.toggle-pw-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (!input) return;
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            this.innerHTML = isPassword
                ? `<svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>`
                : `<svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>`;
        });
    });

    /* ── Reset Form Handler ── */
    const resetForm = document.getElementById('resetearForm');
    if (resetForm) {
        resetForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!resetLinkValid) {
                window.showNotification('Enlace inválido', 'Abre el enlace que recibiste por correo para continuar con el restablecimiento.', false);
                return;
            }
            const btn = document.getElementById('btnGuardar');
            const passwordEl = document.getElementById('password');
            const confirmPasswordEl = document.getElementById('confirmPassword');

            if (!btn || !passwordEl || !confirmPasswordEl) return;

            const password = passwordEl.value;
            const confirmPassword = confirmPasswordEl.value;

            if (password !== confirmPassword) {
                window.showNotification('Validación Fallida', 'Las contraseñas ingresadas no coinciden.', false);
                return;
            }

            // Complexity validations
            if (password.length < 8) { window.showNotification('Validación Fallida', 'La contraseña debe tener al menos 8 caracteres', false); return; }
            if (!/[A-Z]/.test(password)) { window.showNotification('Validación Fallida', 'La contraseña debe contener al menos una mayúscula', false); return; }
            if (!/[a-z]/.test(password)) { window.showNotification('Validación Fallida', 'La contraseña debe contener al menos una minúscula', false); return; }
            if (!/\d/.test(password)) { window.showNotification('Validación Fallida', 'La contraseña debe contener al menos un número', false); return; }

            btn.disabled = true;
            btn.innerHTML = `
                <div class="flex items-center justify-center gap-3">
                    <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    GUARDANDO CONTRASEÑA...
                </div>
            `;

            try {
                const endpoint = (ctx ? ctx : '') + '/api/usuario/resetear';
                
                // Safe construction of headers containing optional CSRF
                const reqHeaders = { 'Content-Type': 'application/json' };
                if (csrfToken) {
                    reqHeaders[csrfHeader] = csrfToken;
                }

                const res = await fetch(endpoint, {
                    method: 'POST',
                    headers: reqHeaders,
                    body: JSON.stringify({
                        email: emailParam,
                        token: tokenParam,
                        password: password
                    })
                });

                const data = await res.json();
                if (data.success) {
                    btn.innerHTML = 'CONTRASEÑA ACTUALIZADA ✓';
                    btn.className = 'w-full py-4 bg-emerald-500 text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg transition-all text-center';
                    
                    window.showNotification('Clave Restablecida', '¡Contraseña actualizada con éxito en la base de datos! Redirigiendo...', true);
                    setTimeout(() => {
                        window.location.href = 'login.jsp';
                    }, 1500);
                } else {
                    window.showNotification('Error del Servidor', data.mensaje || 'Error al restablecer la contraseña.', false);
                    btn.disabled = false;
                    btn.innerHTML = 'GUARDAR NUEVA CLAVE';
                }
            } catch(err) {
                window.showNotification('Fallo de Red', 'Fallo de conexión al cockpit central', false);
                btn.disabled = false;
                btn.innerHTML = 'GUARDAR NUEVA CLAVE';
            }
        });
    }
});
