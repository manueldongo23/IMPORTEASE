<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Recuperar Contraseña</title>
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

    <div id="toastNotification" class="fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 px-6 py-4.5 rounded-2xl border bg-[var(--surface-1)] shadow-2xl transition-all duration-500 opacity-0 pointer-events-none scale-95 max-w-md w-[calc(100%-3rem)]">
        <span id="toastIcon" class="text-xl"></span>
        <div class="flex-1">
            <p id="toastTitle" class="text-xs font-black uppercase tracking-wider"></p>
            <p id="toastMessage" class="text-[11px] text-[var(--text-secondary)] font-semibold mt-0.5"></p>
        </div>
    </div>

    <div class="glass-card w-full max-w-[440px] p-8 md:p-10 space-y-8 relative z-10 bg-[var(--surface-1)] border border-[var(--border)] shadow-2xl fade-up">
        <div class="space-y-4">
            <a href="login.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">
                ‹ Volver al Cockpit
            </a>
            <h2 class="text-3xl font-black tracking-tight text-[var(--text-primary)]">Recuperar Acceso</h2>
            <p class="text-[var(--text-secondary)] font-semibold text-xs leading-relaxed">Ingresa tu email corporativo registrado. Te enviaremos un enlace seguro para restablecer la contraseña.</p>
        </div>

        <form id="recuperarForm" class="space-y-6">
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Email Corporativo</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5 text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                        </svg>
                    </div>
                    <input type="email" id="email" required
                           class="w-full pl-14 pr-6 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all"
                           placeholder="admin@empresa.com">
                </div>
            </div>

            <button type="submit" id="btnEnviar" class="w-full py-4 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg transition-all transform active:scale-98">
                ENVIAR SOLICITUD
            </button>
        </form>
    </div>

    <div id="gmailModal" class="fixed inset-0 z-50 hidden items-center justify-center p-6 bg-black/40 backdrop-blur-sm animate-fadeUp">
        <div class="bg-[var(--surface-2)] rounded-3xl w-full max-w-lg p-6 md:p-8 relative overflow-hidden shadow-2xl border border-[var(--border)]">
            <div class="flex items-center justify-between border-b border-[var(--border)] pb-4 mb-6">
                <div class="flex items-center gap-3">
                    <div class="w-8 h-8 rounded-lg bg-rose-500 flex items-center justify-center font-black text-white text-base shadow-sm">M</div>
                    <div>
                        <h4 class="text-sm font-black text-[var(--text-primary)] leading-none" id="gmailModalTitle">Gmail Corporativo</h4>
                        <p class="text-[8px] text-emerald-600 font-bold uppercase tracking-wider mt-0.5">Bandeja de entrada</p>
                    </div>
                </div>
                <span class="text-[10px] font-bold text-[var(--text-tertiary)] font-mono">Buzón: Recibidos</span>
            </div>

            <div class="bg-[var(--surface-1)] border border-[var(--border)] rounded-2xl p-6 space-y-6 shadow-sm text-[var(--text-secondary)]">
                <div class="flex justify-between items-start text-xs border-b border-[var(--border)] pb-3">
                    <div>
                        <p class="font-bold text-[var(--text-primary)]">De: <span class="font-medium text-[var(--text-tertiary)]">soporte@importease.com</span></p>
                        <p class="font-bold text-[var(--text-primary)] mt-1">Para: <span class="font-medium text-[var(--text-tertiary)]" id="gmailTargetEmail">admin@empresa.com</span></p>
                    </div>
                    <span class="text-[9px] text-[var(--text-tertiary)] font-medium">Hace 1 segundo</span>
                </div>

                <div class="space-y-4">
                    <h5 class="text-base font-black text-[var(--text-primary)] tracking-tight">ImportEase - Restablecer Contraseña</h5>
                    <p class="text-xs leading-relaxed">
                        Hola, <strong id="gmailTargetName" class="text-[var(--accent)]">Operador</strong>:<br/>
                        Hemos recibido una solicitud para restablecer el acceso a tu cuenta de importador.
                    </p>
                    <div class="text-xs leading-relaxed font-semibold text-amber-700 bg-amber-50 border border-amber-200 p-4 rounded-xl flex gap-3">
                        <svg class="w-5 h-5 shrink-0 text-amber-600 mt-0.5" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M16.5 10.5V6.75a4.5 4.5 0 10-9 0v3.75m-.75 11.25h10.5a2.25 2.25 0 002.25-2.25v-6.75a2.25 2.25 0 00-2.25-2.25H6.75a2.25 2.25 0 00-2.25 2.25v6.75a2.25 2.25 0 002.25 2.25z"/>
                        </svg>
                        <div id="gmailStatusMessage">
                            <strong>Correo enviado:</strong> ya tienes un enlace seguro en tu bandeja para crear una nueva contraseña. Revisa también spam o correo no deseado.
                        </div>
                    </div>
                </div>

                <div class="space-y-2">
                    <p class="text-[10px] font-black uppercase tracking-[0.2em] text-[var(--text-tertiary)]">Estado</p>
                    <button id="gmailResetLink" type="button"
                       class="inline-flex items-center justify-center w-full px-4 py-3 rounded-xl bg-[var(--accent)] text-white text-xs font-black tracking-[0.18em] uppercase shadow-lg hover:bg-[var(--accent-hover)] transition-colors">
                        Entendido
                    </button>
                </div>

                <div class="pt-2 flex gap-3">
                    <button onclick="rejectReset()" class="px-5 py-2.5 rounded-xl border border-[var(--border)] hover:bg-[var(--surface-2)] text-xs font-bold text-[var(--text-primary)] transition-all flex-1">
                        Cerrar
                    </button>
                </div>
            </div>

            <div class="mt-6 text-center">
                <p class="text-[9px] text-[var(--text-tertiary)] font-bold">Por seguridad, el enlace solo se entrega por correo y nunca se muestra en esta pantalla.</p>
            </div>
        </div>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        let currentTargetEmail = '';

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

            setTimeout(() => {
                toast.classList.add('toast-active');
                toast.style.opacity = '1';
                toast.style.transform = 'translate(-50%, 0) scale(1)';
                toast.style.pointerEvents = 'auto';
            }, 50);

            setTimeout(() => {
                toast.style.opacity = '0';
                toast.style.transform = 'translate(-50%, 0) scale(0.95)';
                toast.style.pointerEvents = 'none';
            }, 4000);
        }

        document.getElementById('recuperarForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnEnviar');
            const email = document.getElementById('email').value;

            btn.disabled = true;
            btn.innerHTML = `
                <div class="flex items-center justify-center gap-3">
                    <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    PROCESANDO SOLICITUD...
                </div>
            `;

            try {
                const res = await fetch('<%= request.getContextPath() %>/api/usuario/recuperar', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email })
                });

                const data = await res.json();
                if (data.success) {
                    currentTargetEmail = email;
                    document.getElementById('gmailTargetEmail').textContent = email;
                    document.getElementById('gmailTargetName').textContent = 'Usuario';
                    document.getElementById('gmailModalTitle').textContent = 'ImportEase - Solicitud registrada';
                    document.getElementById('gmailStatusMessage').innerHTML = '<strong>Solicitud registrada:</strong> si el correo existe, recibiras un enlace seguro. Revisa tu bandeja de entrada y spam.';
                    const resetLink = document.getElementById('gmailResetLink');
                    resetLink.textContent = 'Entendido';
                    resetLink.onclick = (ev) => { ev.preventDefault(); rejectReset(); };
                    document.getElementById('gmailModal').classList.remove('hidden');
                    document.getElementById('gmailModal').classList.add('flex');
                    showNotification('Solicitud registrada', data.mensaje || 'Si el correo existe, recibiras un enlace.', true);
                } else if (data.resetUrl) {
                    document.getElementById('gmailStatusMessage').innerHTML = '<strong>Correo no enviado:</strong> pero puedes usar el enlace directo abajo para restablecer tu contraseña.';
                    const resetLink = document.getElementById('gmailResetLink');
                    resetLink.textContent = 'Abrir restablecimiento';
                    resetLink.onclick = (ev) => { ev.preventDefault(); window.location.href = data.resetUrl; };
                    document.getElementById('gmailModalTitle').textContent = 'ImportEase - Enlace directo';
                    document.getElementById('gmailModal').classList.remove('hidden');
                    document.getElementById('gmailModal').classList.add('flex');
                    showNotification('Correo no enviado', 'Usa el enlace directo disponible en el modal.', false);
                } else {
                    showNotification('Solicitud denegada', data.mensaje || 'Error al procesar la solicitud', false);
                }
            } catch(e) {
                showNotification('Fallo de conexión', 'No pudimos contactar con el servidor de ImportEase.', false);
            } finally {
                btn.disabled = false;
                btn.innerHTML = 'ENVIAR SOLICITUD';
            }
        });

        function rejectReset() {
            document.getElementById('gmailModal').classList.add('hidden');
            document.getElementById('gmailModal').classList.remove('flex');
            showNotification('Solicitud completada', 'Si el correo existe, el enlace llegara a la bandeja registrada.', true);
        }
    </script>
</body>
</html>
