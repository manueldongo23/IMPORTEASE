<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Enterprise Registration</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        (function() {
            if (localStorage.getItem('dark_mode') === 'true') {
                document.documentElement.classList.add('dark-mode');
            }
        })();
    </script>
</head>
<body class="min-h-screen flex items-center justify-center p-6 md:p-12 text-[var(--text-primary)] bg-[var(--surface-0)] font-['Outfit']">

    <jsp:include page="/fragments/toast.jsp" />

    <div class="glass-card w-full max-w-4xl p-8 md:p-12 space-y-8 relative z-10 fade-up bg-[var(--surface-1)] border border-[var(--border)] shadow-2xl">
        <div class="flex flex-col md:flex-row md:items-center justify-between gap-6 border-b border-[var(--border)] pb-6">
            <div class="space-y-2">
                <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[var(--accent-soft)] border border-[var(--accent-glow)] mb-1">
                    <span class="w-2 h-2 rounded-full bg-[var(--accent)]"></span>
                    <span class="text-[9px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Registro Corporativo V2.0</span>
                </div>
                <h1 class="text-3xl font-black text-[var(--text-primary)] tracking-tight">Alta de Empresa</h1>
                <p class="text-[var(--text-secondary)] text-sm font-semibold">Configura tu perfil de importador y accede a la red logística.</p>
            </div>
            <div class="hidden lg:block text-right">
                <p class="text-[9px] text-[var(--text-tertiary)] font-black uppercase tracking-widest mb-1">Estado del Padrón</p>
                <div class="flex items-center gap-2 justify-end text-emerald-600">
                    <span class="text-[10px] font-black uppercase tracking-widest">Sincronizado SUNAT</span>
                    <span class="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse"></span>
                </div>
            </div>
        </div>

        <form id="registroForm" class="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
            <!-- RUC -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">RUC (Registro Único)</label>
                <div class="flex gap-3">
                    <div class="relative flex-1 group">
                        <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M2.25 21h19.5m-18-18v18m10.5-18v18m6-13.5V21M6.75 6.75h.75m-.75 3h.75m-.75 3h.75m3-6h.75m-.75 3h.75m-.75 3h.75M6.75 21h10.5V3.75c0-.621-.504-1.125-1.125-1.125h-8.25C6.879 2.625 6 3.504 6 4.5V21z"/>
                            </svg>
                        </div>
                        <input type="text" id="ruc" required maxlength="11" 
                               class="w-full pl-14 pr-4 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                               placeholder="20123456789">
                    </div>
                    <button type="button" onclick="validarRuc(event)" 
                            class="px-5 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] hover:bg-[var(--surface-2)] text-[9px] font-black uppercase tracking-widest text-[var(--accent)] transition-all active:scale-98">
                        Validar
                    </button>
                </div>
                <div class="flex flex-wrap items-center gap-2">
                    <span id="rucSourceChip" class="source-chip source-chip--pending">PENDIENTE VALIDACIÓN</span>
                    <span id="rucValidationText" class="text-[10px] font-bold text-[var(--text-secondary)]">La razón social no se tratará como oficial hasta validar la fuente.</span>
                </div>
            </div>

            <!-- Razón Social -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Razón Social</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 21v-8.25M15.75 21v-8.25M8.25 21v-8.25M3 9l9-6 9 6m-1.5 12V10.332A48.36 48.36 0 0012 9.75c-2.551 0-5.056.2-7.5.582V21M3 21h18M12 6.75h.008v.008H12V6.75z"/>
                        </svg>
                    </div>
                    <input type="text" id="razonSocial" required 
                           class="w-full pl-14 pr-6 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-2)] text-sm font-bold text-[var(--text-secondary)] placeholder-[var(--text-tertiary)] focus:outline-none" 
                           placeholder="Automático al validar RUC" readonly>
                </div>
            </div>

            <!-- Email -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Email de Administrador</label>
                <div class="relative group">
                    <div class="absolute inset-y-0 left-5 flex items-center pointer-events-none text-[var(--text-tertiary)] group-focus-within:text-[var(--accent)] transition-colors">
                        <svg class="w-5 h-5" fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                        </svg>
                    </div>
                    <input type="email" id="email" required 
                           class="w-full pl-14 pr-6 py-4 rounded-2xl border border-[var(--border)] bg-[var(--surface-1)] text-sm font-bold text-[var(--text-primary)] placeholder-[var(--text-tertiary)] focus:outline-none focus:border-[var(--accent)] focus:ring-4 focus:ring-[var(--accent-glow)] transition-all" 
                           placeholder="admin@empresa.com.pe">
                </div>
            </div>

            <!-- Password -->
            <div class="space-y-2">
                <label class="text-[10px] font-black text-[var(--text-tertiary)] uppercase tracking-[0.2em] ml-1">Clave de Acceso</label>
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
            
            <div class="md:col-span-2 flex items-center gap-5 p-5 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)] group hover:bg-[var(--accent-soft)]/20 transition-all cursor-pointer">
                <div class="flex items-center h-6">
                    <input type="checkbox" id="buenContribuyente" 
                           class="w-5 h-5 rounded-md text-[var(--accent)] bg-[var(--surface-1)] border-[var(--border)] focus:ring-0 cursor-pointer">
                </div>
                <label for="buenContribuyente" class="flex-1 cursor-pointer">
                    <p class="text-xs font-black text-[var(--text-primary)] uppercase tracking-wider group-hover:text-[var(--accent)] transition-colors">Certificación de Buen Contribuyente</p>
                    <p class="text-[11px] text-[var(--text-secondary)] font-semibold mt-0.5">Habilita beneficios arancelarios y prioridad en el despacho nacional ante SUNAT.</p>
                </label>
            </div>
            
            <div class="md:col-span-2 pt-4 flex flex-col items-center gap-5">
                <button type="submit" id="btnRegister" class="w-full max-w-md py-4 bg-[var(--accent)] hover:bg-[var(--accent-hover)] text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg transition-all transform active:scale-98">
                    GENERAR TOKEN DE ACCESO
                </button>
                <p class="text-[var(--text-secondary)] text-[10px] font-black uppercase tracking-widest">
                    ¿Ya tienes cuenta? 
                    <a href="login.jsp" class="text-[var(--accent)] font-black hover:underline ml-1">Identifícate</a>
                </p>
            </div>
        </form>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        async function validarRuc(event) {
            const ruc = document.getElementById('ruc').value;
            if (ruc.length !== 11) { showNotification('Validación', 'RUC debe contener 11 dígitos', false); return; }
            if (!/^\d{11}$/.test(ruc)) { showNotification('Validación', 'RUC solo debe contener números', false); return; }
            
            const btn = event.currentTarget;
            const original = btn.innerText;
            btn.innerText = "Validando...";
            btn.disabled = true;
            
            try {
                const res = await fetch('<%= request.getContextPath() %>/api/usuario/validarRuc?ruc=' + ruc);
                const data = await res.json();
                const chip = document.getElementById('rucSourceChip');
                const text = document.getElementById('rucValidationText');
                const razonInput = document.getElementById('razonSocial');

                if (data.error) {
                    // RUC completamente inválido (no pasó ni dígito verificador)
                    showNotification('RUC Inválido', 'El RUC ingresado no es válido. Verifica el número e intenta de nuevo.', false);
                    btn.innerText = original;
                    btn.disabled = false;
                    chip.className = "source-chip source-chip--pending";
                    chip.textContent = "INVÁLIDO";
                    chip.style.color = "var(--red-600, #dc2626)";
                    text.textContent = "El RUC no pasó la validación. Verifica que sea un número de 11 dígitos correcto.";
                    return;
                }

                if (data.razonSocial) {
                    razonInput.value = data.razonSocial;
                    document.getElementById('buenContribuyente').checked = data.buenContribuyente || false;

                    // Determinar si es validación por API externa o local
                    const fuenteRuc = data.fuenteRuc || '';
                    const esApiReal = fuenteRuc === 'TERCERO_API';
                    const esSimulacion = fuenteRuc.includes('SIMULACION');
                    const esLocal = (fuenteRuc.includes('LOCAL') || fuenteRuc.includes('VALIDACION_LOCAL')) && !esSimulacion;
                    const esCache = fuenteRuc.includes('CACHE');

                    if (data.rucValidado && esApiReal) {
                        // ✅ Validado por API externa (máxima confianza)
                        btn.innerText = "✓ Validado";
                        btn.className = "px-5 rounded-2xl bg-emerald-50 border border-emerald-200 text-[9px] font-black text-emerald-600 uppercase tracking-widest";
                        chip.className = "source-chip source-chip--third";
                        chip.textContent = esCache ? "CACHÉ LOCAL" : (fuenteRuc || "TERCERO");
                        text.textContent = "RUC validado por fuente " + (esCache ? "caché" : "externa") + ". Confianza: " + Math.round((data.rucConfianza || 0.82) * 100) + "%";
                        razonInput.readOnly = true;
                        razonInput.className = razonInput.className.replace('bg-[var(--surface-2)]', 'bg-[var(--surface-1)]');
                    } else if (data.rucValidado && esSimulacion) {
                        // 🟡 Simulado (sin token API, datos de prueba editables)
                        btn.innerText = "✓ Simulado";
                        btn.className = "px-5 rounded-2xl bg-yellow-50 border border-yellow-200 text-[9px] font-black text-yellow-700 uppercase tracking-widest";
                        chip.className = "source-chip source-chip--pending";
                        chip.style.background = "rgba(234,179,8,0.1)";
                        chip.style.color = "#a16207";
                        chip.style.borderColor = "#fde047";
                        chip.textContent = "SIMULADO";
                        text.textContent = "RUC válido (simulado). Confianza: " + Math.round((data.rucConfianza || 0.5) * 100) + "%. Corrige la razón social si es necesario.";
                        razonInput.readOnly = false;
                        razonInput.placeholder = "Corrige la razón social si es necesario";
                        razonInput.className = razonInput.className.replace('bg-[var(--surface-2)]', 'bg-[var(--surface-1)]');
                        razonInput.focus();
                    } else if (data.rucValidado && esLocal) {
                        // 🔵 Validado localmente (dígito verificador OK, pero sin razón social oficial)
                        btn.innerText = "✓ Estructura OK";
                        btn.className = "px-5 rounded-2xl bg-blue-50 border border-blue-200 text-[9px] font-black text-blue-600 uppercase tracking-widest";
                        chip.className = "source-chip source-chip--pending";
                        chip.style.background = "rgba(59,130,246,0.1)";
                        chip.style.color = "#2563eb";
                        chip.style.borderColor = "#93c5fd";
                        chip.textContent = "VALIDACIÓN LOCAL";
                        text.textContent = "RUC válido (dígito verificador OK). Confianza: " + Math.round((data.rucConfianza || 0.78) * 100) + "%. Ingresa la razón social manualmente.";
                        // Permitir editar razón social ya que no viene de API oficial
                        razonInput.readOnly = false;
                        razonInput.value = "";
                        razonInput.placeholder = "Ingresa la razón social de la empresa";
                        razonInput.className = razonInput.className.replace('bg-[var(--surface-2)]', 'bg-[var(--surface-1)]');
                        razonInput.focus();
                    } else {
                        // 🟠 No validado (pendiente)
                        btn.innerText = "Pendiente";
                        btn.className = "px-5 rounded-2xl bg-orange-50 border border-orange-200 text-[9px] font-black text-orange-700 uppercase tracking-widest";
                        chip.className = "source-chip source-chip--pending";
                        chip.textContent = "PENDIENTE VALIDACIÓN";
                        text.textContent = "No se pudo validar con fuente externa. Puedes continuar, pero quedará marcado como no validado.";
                        razonInput.readOnly = false;
                        razonInput.placeholder = "Ingresa la razón social manualmente";
                    }
                } else {
                    showNotification('RUC no válido', 'El número de RUC no es válido o no está registrado.', false);
                    btn.innerText = original;
                    btn.disabled = false;
                }
            } catch(e) {
                showNotification('Error', 'Error de conexión al validar RUC. Intenta de nuevo.', false);
                btn.innerText = original;
                btn.disabled = false;
            }
        }

        document.getElementById('registroForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnRegister');
            btn.disabled = true;
            btn.innerHTML = `
                <div class="flex items-center justify-center gap-3">
                    <div class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    PROCESANDO REGISTRO...
                </div>
            `;

            const payload = {
                ruc: document.getElementById('ruc').value,
                razonSocial: document.getElementById('razonSocial').value,
                email: document.getElementById('email').value,
                passwordHash: document.getElementById('password').value,
                buenContribuyente: document.getElementById('buenContribuyente').checked,
                perfil: 'IMPORTADOR_ESTANDAR'
            };

            // Validación de contraseña en el cockpit
            const pw = payload.passwordHash;
            if (pw.length < 8) { showError('La contraseña debe tener al menos 8 caracteres'); return; }
            if (!/[A-Z]/.test(pw)) { showError('La contraseña debe contener al menos una mayúscula'); return; }
            if (!/[a-z]/.test(pw)) { showError('La contraseña debe contener al menos una minúscula'); return; }
            if (!/\d/.test(pw)) { showError('La contraseña debe contener al menos un número'); return; }

            function showError(msg) {
                showNotification('Validación', msg, false);
                btn.disabled = false;
                btn.innerHTML = 'GENERAR TOKEN DE ACCESO';
            }

            try {
                const res = await fetch('<%= request.getContextPath() %>/api/usuario/registro', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });
                
                const data = await res.json();
                if (data.success) {
                    btn.innerHTML = 'CUENTA CREADA EXITOSAMENTE';
                    btn.className = 'w-full max-w-md py-4 bg-emerald-500 text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg text-center';
                    setTimeout(() => window.location.href = 'login.jsp', 1500);
                } else {
                    showNotification('Error', 'Error en el despliegue: ' + (data.mensaje || 'Validación fallida'), false);
                    btn.disabled = false;
                    btn.innerHTML = 'GENERAR TOKEN DE ACCESO';
                }
            } catch(e) {
                btn.disabled = false;
                btn.innerHTML = 'GENERAR TOKEN DE ACCESO';
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
