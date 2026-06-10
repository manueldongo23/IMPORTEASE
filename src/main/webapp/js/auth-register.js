/* auth-register.js - Enterprise Registration Interface Interaction & Submission */

document.addEventListener('DOMContentLoaded', () => {
    const ctx = window.ImportEase?.ctx || window.ctx || "";
    const csrfToken = window.ImportEase?.csrfToken || window.csrfToken || "";
    const csrfHeader = window.ImportEase?.csrfHeader || "X-CSRF-TOKEN";

    initNeuralCanvas('neuralCanvas', { nodeCount: 50 });

    /* ── Validar RUC SUNAT ── */
    async function validarRuc() {
        const ruc = document.getElementById('ruc').value;
        if (ruc.length !== 11) { showNotification('Validación', 'RUC debe contener 11 dígitos', false); return; }
        if (!/^\d{11}$/.test(ruc)) { showNotification('Validación', 'RUC solo debe contener números', false); return; }
        
        const btn = document.getElementById('btnValidarRuc');
        const original = btn.innerText;
        btn.innerText = "Validando...";
        btn.disabled = true;
        
        try {
            const endpoint = (ctx ? ctx : '') + '/api/usuario/validarRuc?ruc=' + ruc;
            const res = await fetch(endpoint);
            const data = await res.json();
            
            const chipBadge = document.getElementById('rucValidoBadge');
            const rucText = document.getElementById('rucValidationText');
            const rucInput = document.getElementById('ruc');
            const rucCheck = document.getElementById('rucCheck');
            const rucIcon = document.getElementById('rucIndicatorIcon');
            
            const razonInput = document.getElementById('razonSocial');
            const razonText = document.getElementById('razonValidationText');
            const razonCheck = document.getElementById('razonCheck');
            const razonIcon = document.getElementById('razonIndicatorIcon');
            
            const buenContribuyenteCheck = document.getElementById('buenContribuyente');
            
            if (data.error) {
                showNotification('RUC Inválido', 'El RUC ingresado no es válido. Verifica el número e intenta de nuevo.', false);
                btn.innerText = original;
                btn.disabled = false;
                
                // Reset RUC states
                rucInput.classList.remove('validated');
                if (rucCheck) rucCheck.style.display = 'none';
                if (rucIcon) rucIcon.classList.add('hidden');
                if (chipBadge) chipBadge.style.display = 'none';
                if (rucText) {
                    rucText.textContent = "El RUC no pasó la validación de estructura.";
                    rucText.className = "text-[10px] font-bold text-rose-500";
                }
                
                // Reset Razon Social states
                razonInput.value = "";
                razonInput.classList.remove('validated');
                if (razonCheck) razonCheck.style.display = 'none';
                if (razonIcon) razonIcon.classList.add('hidden');
                if (razonText) {
                    razonText.textContent = "Razón social de la empresa.";
                    razonText.className = "text-[10px] font-bold text-slate-400";
                }
                return;
            }

            if (data.razonSocial) {
                razonInput.value = data.razonSocial;
                buenContribuyenteCheck.checked = data.buenContribuyente || false;

                const fuenteRuc = data.fuenteRuc || '';
                const esApiReal = fuenteRuc === 'TERCERO_API';
                const esSimulacion = fuenteRuc.includes('SIMULACION');
                const esLocal = (fuenteRuc.includes('LOCAL') || fuenteRuc.includes('VALIDACION_LOCAL')) && !esSimulacion;

                // Turn inputs green and show indicators
                rucInput.classList.add('validated');
                if (rucCheck) rucCheck.style.display = 'block';
                if (rucIcon) rucIcon.classList.remove('hidden');
                if (chipBadge) chipBadge.style.display = 'inline-flex';
                
                razonInput.classList.add('validated');
                if (razonCheck) razonCheck.style.display = 'block';
                if (razonIcon) razonIcon.classList.remove('hidden');

                if (data.rucValidado && esApiReal) {
                    btn.innerText = "✓ Validado";
                    btn.className = "px-5 rounded-2xl bg-emerald-50 border border-emerald-200 text-[10px] font-black text-emerald-600 uppercase tracking-widest";
                    if (rucText) {
                        rucText.textContent = "RUC válido y activo en SUNAT.";
                        rucText.className = "text-[10px] font-bold text-emerald-500";
                    }
                    if (razonText) {
                        razonText.textContent = "Razón social encontrada y verificada.";
                        razonText.className = "text-[10px] font-bold text-emerald-500";
                    }
                    razonInput.readOnly = true;
                    razonInput.className = "auth-input readonly-state validated";
                } else if (data.rucValidado && esSimulacion) {
                    btn.innerText = "✓ Simulado";
                    btn.className = "px-5 rounded-2xl bg-amber-50 border border-amber-200 text-[10px] font-black text-amber-700 uppercase tracking-widest";
                    if (rucText) {
                        rucText.textContent = "RUC válido y activo (simulado). Confianza: " + Math.round((data.rucConfianza || 0.5) * 100) + "%";
                        rucText.className = "text-[10px] font-bold text-emerald-500";
                    }
                    if (razonText) {
                        razonText.textContent = "Razón social simulada. Corrige si es necesario.";
                        razonText.className = "text-[10px] font-bold text-emerald-500";
                    }
                    razonInput.readOnly = false;
                    razonInput.className = "auth-input validated";
                    razonInput.focus();
                } else if (data.rucValidado && esLocal) {
                    btn.innerText = "✓ Estructura OK";
                    btn.className = "px-5 rounded-2xl bg-blue-50 border border-blue-200 text-[10px] font-black text-blue-600 uppercase tracking-widest";
                    if (rucText) {
                        rucText.textContent = "RUC válido (estructura verificada). Confianza: " + Math.round((data.rucConfianza || 0.78) * 100) + "%";
                        rucText.className = "text-[10px] font-bold text-emerald-500";
                    }
                    if (razonText) {
                        razonText.textContent = "Ingresa la razón social manualmente.";
                        razonText.className = "text-[10px] font-bold text-blue-500";
                    }
                    razonInput.readOnly = false;
                    razonInput.value = "";
                    razonInput.placeholder = "Ingresa la razón social de la empresa";
                    razonInput.className = "auth-input validated";
                    razonInput.focus();
                } else {
                    btn.innerText = "Pendiente";
                    btn.className = "px-5 rounded-2xl bg-orange-50 border border-orange-200 text-[10px] font-black text-orange-700 uppercase tracking-widest";
                    if (rucText) {
                        rucText.textContent = "RUC no verificado externamente.";
                        rucText.className = "text-[10px] font-bold text-orange-500";
                    }
                    if (razonText) {
                        razonText.textContent = "Ingresa la razón social manualmente.";
                        razonText.className = "text-[10px] font-bold text-orange-500";
                    }
                    razonInput.readOnly = false;
                    razonInput.className = "auth-input validated";
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

    const btnValidar = document.getElementById('btnValidarRuc');
    if (btnValidar) {
        btnValidar.addEventListener('click', validarRuc);
    }

    /* ── Toggle Password Visibility ── */
    const btnToggle = document.getElementById('btnTogglePassword');
    if (btnToggle) {
        btnToggle.addEventListener('click', () => {
            const input = document.getElementById('password');
            if (!input) return;
            const isPassword = input.type === 'password';
            input.type = isPassword ? 'text' : 'password';
            btnToggle.innerHTML = isPassword
                ? `<svg class="w-5 h-5 text-[#7c3aed]" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>`
                : `<svg class="w-5 h-5 text-slate-400" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>`;
        });
    }

    /* ── Form Handler ── */
    const form = document.getElementById('registroForm');
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnRegister');
            const rucInp = document.getElementById('ruc');
            const razonInp = document.getElementById('razonSocial');
            const emailInp = document.getElementById('email');
            const passwordInp = document.getElementById('password');
            const buenContribuyenteInp = document.getElementById('buenContribuyente');
            const nivelExperienciaInp = document.getElementById('nivelExperiencia');
            const perfilInp = document.getElementById('perfil');

            if (!btn || !rucInp || !razonInp || !emailInp || !passwordInp || !buenContribuyenteInp) return;

            btn.disabled = true;
            const originalHtml = btn.innerHTML;
            btn.innerHTML = `
                <div class="flex items-center justify-center gap-3">
                    <div class="auth-spinner"></div>
                    PROCESANDO REGISTRO...
                </div>
            `;

            const payload = {
                ruc: rucInp.value,
                razonSocial: razonInp.value,
                email: emailInp.value,
                passwordHash: passwordInp.value,
                buenContribuyente: buenContribuyenteInp.checked,
                nivelExperiencia: nivelExperienciaInp ? nivelExperienciaInp.value : 'NUNCA',
                perfil: perfilInp ? perfilInp.value : 'OPERADOR'
            };

            // Contraseña complejidad
            const pw = payload.passwordHash;
            if (pw.length < 8) { showError('La contraseña debe tener al menos 8 caracteres'); return; }
            if (!/[A-Z]/.test(pw)) { showError('La contraseña debe contener al menos una mayúscula'); return; }
            if (!/[a-z]/.test(pw)) { showError('La contraseña debe contener al menos una minúscula'); return; }
            if (!/\d/.test(pw)) { showError('La contraseña debe contener al menos un número'); return; }

            function showError(msg) {
                showNotification('Validación', msg, false);
                btn.disabled = false;
                btn.innerHTML = originalHtml;
            }

            try {
                const endpoint = (ctx ? ctx : '') + '/api/usuario/registro';
                
                // Safe construction of headers containing optional CSRF
                const reqHeaders = { 'Content-Type': 'application/json' };
                if (csrfToken) {
                    reqHeaders[csrfHeader] = csrfToken;
                }

                const res = await fetch(endpoint, {
                    method: 'POST',
                    headers: reqHeaders,
                    body: JSON.stringify(payload)
                });
                
                const data = await res.json();
                if (data.success) {
                    btn.innerHTML = 'CUENTA CREADA EXITOSAMENTE';
                    btn.className = 'w-full max-w-md py-4 bg-emerald-500 text-white text-xs font-black tracking-[0.2em] rounded-2xl shadow-lg text-center';
                    setTimeout(() => {
                        window.location.href = 'login.jsp';
                    }, 1500);
                } else {
                    showNotification('Error', 'Error en el despliegue: ' + (data.mensaje || 'Validación fallida'), false);
                    btn.disabled = false;
                    btn.innerHTML = originalHtml;
                }
            } catch(e) {
                btn.disabled = false;
                btn.innerHTML = originalHtml;
                showNotification('Error', 'Error al procesar el registro corporativo.', false);
            }
        });
    }
});
