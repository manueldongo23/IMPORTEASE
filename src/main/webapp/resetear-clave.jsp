<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Restablecer Contraseña</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        (function() {
            if (localStorage.getItem('dark_mode') === 'true') {
                document.documentElement.classList.add('dark-mode');
            }
        })();
    </script>
</head>
<body class="min-h-screen flex items-center justify-center p-6 text-[var(--text-primary)] bg-[var(--surface-0)] font-['Outfit']">

    <!-- TOAST NOTIFICATION PREMIUM -->
    <div id="toastNotification" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 px-6 py-4.5 rounded-2xl border bg-[var(--surface-1)] shadow-2xl transition-all duration-500 opacity-0 pointer-events-none scale-95 max-w-md w-[calc(100%-3rem)]">
        <span id="toastIcon" class="text-xl"></span>
        <div class="flex-1">
            <p id="toastTitle" class="text-xs font-black uppercase tracking-wider"></p>
            <p id="toastMessage" class="text-[11px] text-[var(--text-secondary)] font-semibold mt-0.5"></p>
        </div>
    </div>

    <div class="glass-card w-full max-w-[440px] p-8 md:p-10 space-y-8 relative z-10 bg-[var(--surface-1)] border border-[var(--border)] shadow-2xl fade-up">
        <div class="space-y-4">
            <h2 class="text-3xl font-black tracking-tight text-[var(--text-primary)]">Nueva Contraseña</h2>
            <p class="text-[var(--text-secondary)] font-semibold text-xs leading-relaxed">Ingresa y confirma tu nueva clave de acceso corporativa.</p>
        </div>

        <form id="resetearForm" class="space-y-5">
            <!-- Email (deshabilitado/oculto) -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Email Destino</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)]">
                        <svg class="w-5 h-5 text-[var(--text-tertiary)]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                        </svg>
                    </div>
                    <input type="email" id="email" required readonly
                           class="w-full pl-14 pr-6 py-4 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)] text-sm text-[var(--text-secondary)] font-bold focus:outline-none">
                </div>
            </div>

            <!-- Password -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Nueva Contraseña</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                        </svg>
                    </div>
                    <input type="password" id="password" required 
                           class="w-full pl-14 pr-12 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                           placeholder="••••••••">
                    <button type="button" onclick="togglePasswordVisibility('password', this)" class="absolute inset-y-0 right-5 flex items-center text-[var(--text-tertiary)] hover:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                    </button>
                </div>
            </div>

            <!-- Confirm Password -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Confirmar Contraseña</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                        </svg>
                    </div>
                    <input type="password" id="confirmPassword" required 
                           class="w-full pl-14 pr-12 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                           placeholder="••••••••">
                    <button type="button" onclick="togglePasswordVisibility('confirmPassword', this)" class="absolute inset-y-0 right-5 flex items-center text-[var(--text-tertiary)] hover:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                            <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                        </svg>
                    </button>
                </div>
            </div>
            
            <button type="submit" id="btnGuardar" class="w-full py-4 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg transition-all transform active:scale-98">
                GUARDAR NUEVA CLAVE
            </button>
        </form>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        const params = new URLSearchParams(window.location.search);
        const emailParam = params.get('email') || '';
        const tokenParam = params.get('token') || '';
        const resetLinkValid = Boolean(emailParam && tokenParam);

        // Precargar email deshabilitado
        document.getElementById('email').value = emailParam;

        function showNotification(title, message, isSuccess = false) {
            const toast = document.getElementById('toastNotification');
            const icon = document.getElementById('toastIcon');
            const tTitle = document.getElementById('toastTitle');
            const tMsg = document.getElementById('toastMessage');
            
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
        }

        document.getElementById('resetearForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            if (!resetLinkValid) {
                showNotification('Enlace invalido', 'Abre el enlace que recibiste por correo para continuar con el restablecimiento.', false);
                return;
            }
            const btn = document.getElementById('btnGuardar');
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            if (password !== confirmPassword) {
                showNotification('Validación Fallida', 'Las contraseñas ingresadas no coinciden.', false);
                return;
            }

            // Validar complejidad frontend
            if (password.length < 8) { showNotification('Validación Fallida', 'La contraseña debe tener al menos 8 caracteres', false); return; }
            if (!/[A-Z]/.test(password)) { showNotification('Validación Fallida', 'La contraseña debe contener al menos una mayúscula', false); return; }
            if (!/[a-z]/.test(password)) { showNotification('Validación Fallida', 'La contraseña debe contener al menos una minúscula', false); return; }
            if (!/\d/.test(password)) { showNotification('Validación Fallida', 'La contraseña debe contener al menos un número', false); return; }

            btn.disabled = true;
            btn.innerHTML = `
                <div class="flex items-center justify-center gap-3">
                    <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    GUARDANDO CONTRASEÑA...
                </div>
            `;

            try {
                const res = await fetch('<%= request.getContextPath() %>/api/usuario/resetear', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
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
                    
                    showNotification('Clave Restablecida', '¡Contraseña actualizada con éxito en la base de datos! Redirigiendo...', true);
                    setTimeout(() => {
                        window.location.href = 'login.jsp';
                    }, 1500);
                } else {
                    showNotification('Error del Servidor', data.mensaje || 'Error al restablecer la contraseña.', false);
                    btn.disabled = false;
                    btn.innerHTML = 'GUARDAR NUEVA CLAVE';
                }
            } catch(e) {
                showNotification('Fallo de Red', 'Fallo de conexión al cockpit central', false);
                btn.disabled = false;
                btn.innerHTML = 'GUARDAR NUEVA CLAVE';
            }
        });

        function togglePasswordVisibility(id, btn) {
            const input = document.getElementById(id);
            if (input.type === 'password') {
                input.type = 'text';
                btn.innerHTML = `<svg class="w-5 h-5 text-[var(--accent)]" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>`;
            } else {
                input.type = 'password';
                btn.innerHTML = `<svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>`;
            }
        }
    </script>
</body>
</html>
