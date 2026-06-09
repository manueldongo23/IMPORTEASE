// ==========================================================================
// IMPORTEASE - STORAGE & DATA PERSISTENCE MODULE (storage.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    W.saveWizardDraft = async function(redirectAfter = false) {
        W.captureFields();
        try {
            W.wizardData.currentStep = W.currentStep;
            W.wizardData.savedAt = new Date().toISOString();
            W.wizardData.usuarioId = window.userId;
            W.wizardData.vuceAnswers = {
                qConsumo: W.getQuestionAnswer('qConsumo'),
                qSalud: W.getQuestionAnswer('qSalud'),
                qWifi: W.getQuestionAnswer('qWifi'),
                qContacto: W.getQuestionAnswer('qContacto'),
                qMadera: W.getQuestionAnswer('qMadera'),
                qUsado: W.getQuestionAnswer('qUsado')
            };
            W.wizardData.dynamicAnswers = Object.assign({}, W.wizardData.dynamicAnswers || {});
            
            const rawDraft = JSON.stringify(W.wizardData);
            localStorage.setItem(W.WIZARD_DRAFT_KEY, rawDraft);
            W.updateAutosaveStatus(W.wizardData.savedAt);

            const payload = {
                pasoActual: W.currentStep,
                jsonBorrador: rawDraft
            };

            const fetchPromise = fetch(`${window.ctx}/api/importacion/guardarBorrador`, {
                method: 'POST',
                headers: W.csrfHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(payload)
            });

            if (redirectAfter) {
                try {
                    await fetchPromise;
                } catch (err) {
                    console.error("Error saving draft to DB on exit", err);
                }
                window.location.href = 'dashboard.jsp';
            }
        } catch (e) {
            console.error("Error saving draft", e);
            if (redirectAfter) {
                window.location.href = 'dashboard.jsp';
            }
        }
    };

    W.updateAutosaveStatus = function(savedAt) {
        const el = document.getElementById('autosaveStatus');
        if (!el) return;

        if (!savedAt) {
            el.textContent = 'Autoguardado activo';
            el.dataset.state = 'idle';
            return;
        }

        const date = new Date(savedAt);
        el.textContent = Number.isNaN(date.getTime())
            ? 'Cambios guardados'
            : 'Guardado ' + W.AUTOSAVE_TIME_FORMATTER.format(date);
        el.dataset.state = 'saved';
    };

    W.loadWizardDraft = function() {
        try {
            let raw = localStorage.getItem(W.WIZARD_DRAFT_KEY);
            let fromEmergency = false;
            if (!raw) {
                raw = sessionStorage.getItem('importease_emergency_draft');
                if (raw) {
                    fromEmergency = true;
                }
            }
            if (!raw) return null;
            
            const draft = JSON.parse(raw);
            if (!draft || typeof draft !== 'object') return null;
            
            draft.currentStep = Number(draft.currentStep) || 1;
            draft.logFob = parseFloat(draft.logFob) || 0;
            draft.logFlete = parseFloat(draft.logFlete) || 0;
            draft.logSeguro = parseFloat(draft.logSeguro) || 0;
            
            if (!draft.dynamicAnswers || typeof draft.dynamicAnswers !== 'object') {
                draft.dynamicAnswers = {};
            }
            if (!draft.vuceQuestions || typeof draft.vuceQuestions !== 'object') {
                draft.vuceQuestions = { consumo: false, salud: false, wifi: false, contacto: false, madera: false, usado: false };
            }
            if (draft.selectedHS && typeof draft.selectedHS !== 'object') {
                draft.selectedHS = null;
            }
            
            if (fromEmergency) {
                localStorage.setItem(W.WIZARD_DRAFT_KEY, JSON.stringify(draft));
                sessionStorage.removeItem('importease_emergency_draft');
            }
            
            return draft;
        } catch (e) {
            localStorage.removeItem(W.WIZARD_DRAFT_KEY);
            return null;
        }
    };

    W.checkAndPromptDraft = async function() {
        let dbDraft = null;
        try {
            const res = await fetch(`${window.ctx}/api/importacion/obtenerBorrador`);
            if (res.ok) {
                const data = await res.json();
                if (data && data.jsonBorrador) {
                    dbDraft = JSON.parse(data.jsonBorrador);
                    dbDraft.fechaActualizacion = data.fechaActualizacion;
                }
            }
        } catch (e) {
            console.error("Error fetching draft from DB:", e);
        }

        const localDraft = W.loadWizardDraft();

        if (!dbDraft && !localDraft) {
            W.initWizardNormally();
            return;
        }

        let selectedDraft = null;
        if (dbDraft && localDraft) {
            const dbTime = new Date(dbDraft.savedAt || dbDraft.fechaActualizacion || 0).getTime();
            const localTime = new Date(localDraft.savedAt || 0).getTime();
            
            const localUserMatch = localDraft.usuarioId === window.userId;
            
            if (localUserMatch && localTime > dbTime) {
                selectedDraft = localDraft;
            } else {
                selectedDraft = dbDraft;
            }
        } else if (dbDraft) {
            selectedDraft = dbDraft;
        } else {
            if (localDraft.usuarioId === window.userId) {
                selectedDraft = localDraft;
            }
        }

        if (!selectedDraft) {
            W.initWizardNormally();
            return;
        }

        const hasReturnKeys = localStorage.getItem(W.WIZARD_HS_RETURN_KEY) || 
                              localStorage.getItem(W.WIZARD_INCOTERM_RETURN_KEY) || 
                              new URLSearchParams(window.location.search).has('step');

        if (hasReturnKeys) {
            W.applyWizardDraft(selectedDraft);
            W.initWizardNormally();
            return;
        }

        W.showDraftPromptModal(selectedDraft);
    };

    W.showDraftPromptModal = function(draft) {
        const modalId = 'importease-draft-modal';
        const existing = document.getElementById(modalId);
        if (existing) existing.remove();

        const overlay = document.createElement('div');
        overlay.id = modalId;
        overlay.className = 'fixed inset-0 bg-black/60 backdrop-blur-md flex items-center justify-center z-50 transition-all duration-300';
        overlay.style.animation = 'fadeIn 0.3s ease-out forwards';

        const prodName = draft.prodNombre || draft.opNombre || 'Producto sin nombre';
        const dateStr = draft.savedAt ? new Date(draft.savedAt).toLocaleString('es-PE', { dateStyle: 'short', timeStyle: 'short' }) : 'Fecha de cotización';
        const stepNum = draft.currentStep || 1;

        overlay.innerHTML = `
            <div class="bg-[var(--surface-1)] border border-[var(--border)] rounded-3xl p-8 max-w-md w-full mx-4 shadow-2xl transition-all duration-300 transform scale-95" style="animation: scaleUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) forwards; background-color: var(--surface-1);">
                <div class="flex items-center gap-4 mb-6">
                    <div class="w-12 h-12 rounded-2xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center shrink-0">
                        <svg class="w-6 h-6 text-purple-600 animate-pulse" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
                        </svg>
                    </div>
                    <div>
                        <h4 class="text-lg font-black text-[var(--text-primary)] tracking-tight">¿Continuamos donde te quedaste?</h4>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mt-1">Encontramos un borrador guardado automáticamente.</p>
                    </div>
                </div>

                <div class="p-5 mb-6 rounded-2xl bg-[var(--surface-2)] border border-[var(--border)] space-y-3">
                    <div class="flex justify-between items-center text-xs font-semibold">
                        <span class="text-[var(--text-tertiary)]">Operación:</span>
                        <span class="text-[var(--text-primary)] font-black text-right truncate max-w-[200px]">${W.escapeHtml(prodName)}</span>
                    </div>
                    <div class="flex justify-between items-center text-xs font-semibold">
                        <span class="text-[var(--text-tertiary)]">Último paso guardado:</span>
                        <span class="px-2 py-0.5 rounded bg-purple-100 text-purple-700 text-[10px] font-black uppercase">Paso ${stepNum} de 4</span>
                    </div>
                    <div class="flex justify-between items-center text-xs font-semibold">
                        <span class="text-[var(--text-tertiary)]">Guardado el:</span>
                        <span class="text-[var(--text-secondary)] font-mono">${dateStr}</span>
                    </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                    <button type="button" id="btnDraftNew" class="px-4 py-3 rounded-xl border border-[var(--border)] bg-[var(--surface-1)] hover:bg-[var(--surface-2)] text-[var(--text-primary)] text-xs font-black uppercase tracking-wider transition-all duration-200 outline-none">
                        Iniciar nuevo
                    </button>
                    <button type="button" id="btnDraftRestore" class="px-4 py-3 rounded-xl bg-purple-600 hover:bg-purple-700 text-white text-xs font-black uppercase tracking-wider shadow-lg shadow-purple-500/20 hover:shadow-purple-500/30 transition-all duration-200 outline-none">
                        Recuperar
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(overlay);
        document.body.classList.add('modal-open');

        const style = document.createElement('style');
        style.id = 'importease-draft-styles';
        style.innerHTML = `
            @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
            @keyframes scaleUp { from { opacity: 0; transform: scale(0.95); } to { opacity: 1; transform: scale(1); } }
            body.modal-open > *:not(#importease-draft-modal) {
                pointer-events: none !important;
                user-select: none !important;
            }
        `;
        document.head.appendChild(style);

        document.getElementById('btnDraftRestore').addEventListener('click', () => {
            document.body.classList.remove('modal-open');
            W.applyWizardDraft(draft);
            overlay.remove();
            style.remove();
            W.initWizardNormally();
            W.mostrarNotificacion("Borrador restaurado con éxito");
        });

        document.getElementById('btnDraftNew').addEventListener('click', () => {
            document.body.classList.remove('modal-open');
            try {
                localStorage.removeItem(W.WIZARD_DRAFT_KEY);
            } catch(e) {}
            overlay.remove();
            style.remove();
            W.initWizardNormally();
            W.mostrarNotificacion("Iniciando cotización en blanco");
        });
    };

    W.applyWizardDraft = function(draft) {
        if (!draft || typeof draft !== 'object') return;
        W.wizardData = Object.assign({}, W.wizardData, draft);

        W.setVal('prodNombre', W.wizardData.prodNombre);
        W.setVal('prodTecnica', W.wizardData.prodTecnica);
        W.setVal('opPaisOrigen', W.wizardData.opPaisOrigen);
        W.setVal('prodUso', W.wizardData.opTipo === 'COMERCIAL' ? 'COMERCIAL' : 'PERSONAL');
        W.setVal('prodCantidad', W.wizardData.prodCantidad);
        W.setVal('prodMarca', W.wizardData.prodMarca);
        W.setVal('prodModelo', W.wizardData.prodModelo);
        W.setVal('opNombre', W.wizardData.opNombre);
        W.setVal('opTipo', W.wizardData.opTipo);
        W.setVal('opRuc', W.wizardData.opRuc);
        W.setVal('opIncoterm', W.wizardData.opIncoterm);
        W.setVal('opProveedor', W.wizardData.opProveedor);
        W.setVal('logFob', W.wizardData.logFob);
        W.setVal('logFlete', W.wizardData.logFlete);
        W.setVal('logSeguro', W.wizardData.logSeguro);
        W.setVal('logTC', W.wizardData.logTC);
        W.setVal('tribPerfil', W.wizardData.tribPerfil);
        W.setVal('tribTlc', W.wizardData.tribTlc);
        W.setVal('tribCertificado', W.wizardData.tribCertificado);
        W.setVal('vuceEstado', W.wizardData.vuceEstado);
        W.setVal('vuceSuce', W.wizardData.vuceSuce);
        W.setVal('vuceResolucion', W.wizardData.vuceResolucion);
        W.setVal('vuceObs', W.wizardData.vuceObs);
        W.wizardData.selectedHSOrigin = draft.selectedHSOrigin || W.wizardData.selectedHSOrigin || 'AUTO';
        W.wizardData.dynamicAnswers = Object.assign({}, draft.dynamicAnswers || W.wizardData.dynamicAnswers || {});
        const answers = W.wizardData.vuceAnswers || {};
        W.setVal('qConsumo', answers.qConsumo || (W.wizardData.vuceQuestions.consumo ? 'SI' : 'NO'));
        W.setVal('qSalud', answers.qSalud || (W.wizardData.vuceQuestions.salud ? 'SI' : 'NO'));
        W.setVal('qWifi', answers.qWifi || (W.wizardData.vuceQuestions.wifi ? 'SI' : 'NO'));
        W.setVal('qContacto', answers.qContacto || (W.wizardData.vuceQuestions.contacto ? 'SI' : 'NO'));
        W.setVal('qMadera', answers.qMadera || (W.wizardData.vuceQuestions.madera ? 'SI' : 'NO'));
        W.setVal('qUsado', answers.qUsado || (W.wizardData.vuceQuestions.usado ? 'SI' : 'NO'));

        if (W.wizardData.selectedHS) {
            W.applyHSCodeUI(W.wizardData.selectedHS);
        }

        W.onProdUsoChange();
        W.onVuceQuestionChange();
        W.renderDynamicQuestions();
        W.updateDocButtonsUI();
        W.updateImportRouteUI();
        W.updateCIFCalculations();
        W.evalVuceQuestions();
        W.updateSidebar();

        const restoredStep = Number(draft.currentStep);
        if (Number.isFinite(restoredStep) && restoredStep >= 1 && restoredStep <= 4) {
            W.goToStep(restoredStep);
        } else {
            W.updateAutosaveStatus(W.wizardData.savedAt);
        }
    };

    W.restoreHsReturnSelection = function() {
        try {
            const raw = localStorage.getItem(W.WIZARD_HS_RETURN_KEY);
            if (!raw) return;
            const hs = JSON.parse(raw);
            if (hs && hs.codigo) {
                W.wizardData.selectedHS = hs;
                W.wizardData.selectedHSOrigin = 'MANUAL';
                W.applyHSCodeUI(hs);
                W.updateCIFCalculations();
                W.evalVuceQuestions();
                W.mostrarNotificacion("Codigo recuperado desde el buscador");
                localStorage.removeItem(W.WIZARD_HS_RETURN_KEY);
                W.goToStep(3);
            }
        } catch (e) {}
    };

    W.restoreIncotermReturnSelection = function() {
        try {
            const raw = localStorage.getItem(W.WIZARD_INCOTERM_RETURN_KEY);
            if (!raw) return false;
            const decision = JSON.parse(raw);
            if (!decision || !decision.incoterm) return false;

            const incoterm = String(decision.incoterm || 'FOB').toUpperCase();
            W.wizardData.opIncoterm = incoterm;
            W.wizardData.logFob = Number(decision.fob || decision.valorProducto || W.wizardData.logFob || 0);
            W.wizardData.logFlete = Number(decision.flete || W.wizardData.logFlete || 0);
            W.wizardData.logSeguro = Number(decision.seguro || W.wizardData.logSeguro || 0);
            W.wizardData.manualCostsModified = true;

            W.setVal('opIncoterm', incoterm);
            W.setVal('opIncotermSelector', incoterm === 'CIF' ? 'CIF' : 'FOB');
            W.setVal('logFob', W.wizardData.logFob);
            W.setVal('logFlete', W.wizardData.logFlete);
            W.setVal('logSeguro', W.wizardData.logSeguro);
            W.syncIncotermButtons();
            W.updateCIFCalculations();
            W.saveWizardDraft();
            localStorage.removeItem(W.WIZARD_INCOTERM_RETURN_KEY);
            W.mostrarNotificacion(`Incoterm ${incoterm} aplicado al costeo`);
            W.goToStep(4);
            return true;
        } catch (e) {
            return false;
        }
    };

    W.restoreRequestedStepFromQuery = function() {
        try {
            const params = new URLSearchParams(window.location.search || '');
            const step = Number(params.get('step'));
            if (Number.isFinite(step) && step >= 1 && step <= 4) {
                W.goToStep(step);
            }
        } catch (e) {}
    };

    W.descargarExpedienteFinal = async function() {
        if (!W.registeredImportId) {
            await W.guardarOperacionGeneral(true);
        }
        
        if (W.registeredImportId) {
            const btnPdf = document.getElementById('btnDownloadPDF');
            const originalText = btnPdf ? btnPdf.innerHTML : "Descargar resumen PDF";
            
            if (btnPdf) {
                btnPdf.disabled = true;
                let percent = 0;
                const interval = setInterval(() => {
                    percent += 10;
                    if (percent <= 30) {
                        btnPdf.innerHTML = `Generando Estructura (${percent}%)`;
                    } else if (percent <= 60) {
                        btnPdf.innerHTML = `Firmando con RSA (${percent}%)`;
                    } else if (percent <= 90) {
                        btnPdf.innerHTML = `Compilando PDF (${percent}%)`;
                    } else {
                        btnPdf.innerHTML = `Finalizando...`;
                    }
                    
                    if (percent >= 100) {
                        clearInterval(interval);
                        btnPdf.innerHTML = originalText;
                        btnPdf.disabled = false;
                        window.location.href = `${window.ctx}/api/importacion/descargarPdf?id=${W.registeredImportId}`;
                        W.mostrarNotificacion("Descargando resumen PDF de forma segura");
                    }
                }, 120);
            } else {
                window.location.href = `${window.ctx}/api/importacion/descargarPdf?id=${W.registeredImportId}`;
                W.mostrarNotificacion("Descargando resumen PDF");
            }
        }
    };

    W.guardarOperacionGeneral = async function(silent = false) {
        W.captureFields();
        if (!W.validateStep(1) || !W.validateStep(2) || !W.validateStep(4)) {
            return;
        }
        
        const payload = {
            hsCode: W.wizardData.selectedHS.codigo,
            fob: W.wizardData.logFob.toString(),
            flete: W.wizardData.logFlete.toString(),
            seguro: W.wizardData.logSeguro.toString(),
            tipo: W.wizardData.opTipo,
            tipoRuta: W.wizardData.opTipo,
            productoDesc: W.wizardData.prodNombre || W.wizardData.opNombre,
            paisOrigen: W.wizardData.opPaisOrigen,
            incoterm: W.wizardData.opIncoterm,
            perfilFiscal: W.wizardData.tribPerfil,
            aplicaTlc: W.wizardData.tribTlc,
            usado: document.getElementById('qUsado') ? (document.getElementById('qUsado').value === 'SI').toString() : 'false'
        };

        try {
            const res = await fetch(`${window.ctx}/api/importacion/cotizar`, {
                method: 'POST',
                headers: W.csrfHeaders({ 'Content-Type': 'application/json' }),
                body: JSON.stringify(payload)
            });
            
            const data = await res.json();
            
            if (data.id) {
                W.registeredImportId = data.id;
                W.refreshDocFileIndicators();
                
                // Registrar documentos asociados
                for (const docTipo in W.wizardData.docs) {
                    if (W.wizardData.docs[docTipo]) {
                        if (['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'].includes(docTipo)) {
                            await fetch(`${window.ctx}/api/importacion/registrarDocumentoOperacion?id=${data.id}&tipo=${encodeURIComponent(docTipo)}`, {
                                method: 'POST',
                                headers: W.csrfHeaders()
                            });
                        }
                    }
                }

                // Cambiar estado aduanero según checklist
                const pendingDocs = W.hasPendingMandatoryDocs();
                const finalState = pendingDocs ? 'DOCS_PENDIENTES' : 'LISTA_DESPACHO';
                
                await fetch(`${window.ctx}/api/importacion/cambiarEstado?id=${data.id}&nuevoEstado=${encodeURIComponent(finalState)}`, {
                    method: 'POST',
                    headers: W.csrfHeaders()
                });

                // Registrar solicitud en pre-VUCE
                if (W.isVuceRequired() && (W.wizardData.vuceSuce || W.wizardData.vuceResolucion)) {
                    const vucePayload = {
                        operacionId: data.id,
                        codigoEntidad: W.wizardData.selectedHS.entidadVuce,
                        tipoPermiso: "Licencia de Internamiento",
                        suce: W.wizardData.vuceSuce || "SUCE-2026-MOCK",
                        resolucion: W.wizardData.vuceResolucion || "RD-2026-MOCK",
                        obs: W.wizardData.vuceObs || "Aprobado didácticamente por importEase",
                        datos: Object.entries(W.wizardData.tupaDatos).map(([k, v]) => ({ campo: k, valor: v }))
                    };

                    await fetch(`${window.ctx}/api/permisos/crear-solicitud`, {
                        method: 'POST',
                        headers: W.csrfHeaders({ 'Content-Type': 'application/json' }),
                        body: JSON.stringify(vucePayload)
                    });
                }

                if (!silent) {
                    W.mostrarNotificacion("Evaluacion guardada");
                    try {
                        localStorage.removeItem(W.WIZARD_DRAFT_KEY);
                    } catch (e) {}
                    setTimeout(() => {
                        window.location.href = "seguimiento.jsp";
                    }, 1500);
                }
            } else {
                W.mostrarNotificacion("No pudimos guardar la evaluacion.", 'error');
            }
        } catch (e) {
            console.error("Error al guardar operación", e);
            W.mostrarNotificacion("No hay conexion con el servidor. Intenta nuevamente.", 'error');
        }
    };

    // Expose select globals to legacy scripts in JSP
    window.checkAndPromptDraft = W.checkAndPromptDraft;
    window.saveWizardDraft = W.saveWizardDraft;

})(window.ImportEaseWizard);
