// ==========================================================================
// IMPORTEASE - PROGRAMMATIC EVENT BINDINGS (dom-bindings.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    W.bindWizardActions = function() {
        const bindClick = (selector, handler) => {
            const el = document.querySelector(selector);
            if (!el) return;
            el.removeAttribute('onclick');
            el.addEventListener('click', (event) => {
                event.preventDefault();
                handler(el, event);
            });
        };

        bindClick('#routeCardPersonal', () => W.selectImportRoute('PERSONAL'));
        bindClick('#routeCardComercial', () => W.selectImportRoute('COMERCIAL'));
        bindClick('#btnPrevStep', () => W.prevStep());
        bindClick('#btnNextStep', () => W.nextStep());
        bindClick('#btnDownloadPDF', () => W.descargarExpedienteFinal());
        bindClick('#sideNextActionButton', (el) => {
            if (el.dataset.action === 'save') {
                W.guardarOperacionGeneral();
                return;
            }
            const targetStep = Number(el.dataset.step || W.currentStep + 1);
            W.goToStep(targetStep);
        });

        bindClick('#btnSelectManualHS', () => W.selectManualHS());
        bindClick('#btnOpenIncotermsLab', () => W.openIncotermsLab());
        bindClick('#btnSaveAndExit', () => {
            W.saveWizardDraft();
            window.location.href = 'dashboard.jsp';
        });
        bindClick('#btnSaveOperationGeneral', () => W.guardarOperacionGeneral());

        // Bind example pills
        document.querySelectorAll('#examplesPillContainer button[data-example]').forEach(el => {
            el.addEventListener('click', (e) => {
                e.preventDefault();
                W.cargarEjemplo(el.dataset.example);
            });
        });

        // Bind Incoterm buttons
        document.querySelectorAll('#incotermSelectorContainer button[data-incoterm]').forEach(el => {
            el.addEventListener('click', (e) => {
                e.preventDefault();
                W.selectIncotermOption(el.dataset.incoterm, el);
            });
        });

        // Bind checklist buttons
        document.querySelectorAll('#checklistDocsArea button[data-doc]').forEach(el => {
            el.addEventListener('click', (e) => {
                e.preventDefault();
                W.toggleDoc(el.dataset.doc);
            });
        });

        // Bind checklist checkboxes
        document.querySelectorAll('#checklistDocsArea input[data-check-doc]').forEach(el => {
            el.addEventListener('change', () => {
                W.toggleDoc(el.dataset.checkDoc, el.checked);
            });
        });

        // Bind help buttons with data-knowledge
        document.querySelectorAll('[data-knowledge]').forEach(el => {
            el.removeAttribute('onclick');
            el.addEventListener('click', (e) => {
                e.preventDefault();
                if (typeof window.openKnowledgePanel === 'function') {
                    window.openKnowledgePanel(el.dataset.knowledge);
                }
            });
        });

        const prodUso = document.getElementById('prodUso');
        if (prodUso) {
            prodUso.removeAttribute('onchange');
            prodUso.addEventListener('change', W.onProdUsoChange);
        }

        const qUsado = document.getElementById('qUsado');
        if (qUsado) {
            qUsado.removeAttribute('onchange');
            qUsado.addEventListener('change', () => {
                W.updateTaxes();
                W.updateSidebar();
                W.evalInputRealTimeAlerts();
                W.saveWizardDraft();
            });
        }

        ['logFob', 'logFlete', 'logSeguro', 'prodCantidad'].forEach((id) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.removeAttribute('oninput');
            el.addEventListener('input', () => {
                if (id !== 'prodCantidad') {
                    W.wizardData.manualCostsModified = true;
                }
                W.updateCIFCalculations();
                W.saveWizardDraft();
            });
        });

        ['tribPerfil', 'tribTlc'].forEach((id) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.removeAttribute('onchange');
            W.updateTaxes();
            el.addEventListener('change', () => {
                W.updateTaxes();
                W.updateSidebar();
                W.saveWizardDraft();
            });
        });

        // Dynamic questions handler delegation
        const dynamicQuestionArea = document.getElementById('dynamicQuestionArea');
        if (dynamicQuestionArea) {
            dynamicQuestionArea.addEventListener('click', (e) => {
                const btn = e.target.closest('button');
                if (!btn) return;
                const group = btn.closest('[data-question-group]');
                if (!group) return;
                const questionId = group.dataset.questionGroup;
                const btnText = btn.innerText.trim();
                const value = btnText === 'Sí' ? 'SI' : (btnText === 'No' ? 'NO' : 'NOSE');
                W.selectDynamicQuestionOption(questionId, value, btn);
                W.onVuceQuestionChange();
            });
        }

        bindClick('#explainHSBtn', () => {
            if (typeof window.openKnowledgePanel === 'function') {
                window.openKnowledgePanel('codigo_hs');
            }
        });
        bindClick('#btnWhySuggested', W.openWhySuggestedModal);
        bindClick('#btnConfirmHS', () => {
            if (W.wizardData.selectedHS) {
                W.wizardData.selectedHSOrigin = 'MANUAL';
                W.saveWizardDraft();
                W.mostrarNotificacion("Código arancelario confirmado: " + W.formatHSCode(W.wizardData.selectedHS.codigo));
                W.goToStep(3);
            } else {
                W.mostrarNotificacion("Por favor, busca y elige un código arancelario antes de continuar.", "warning");
            }
        });
        bindClick('#btnVerTupaDetalle', () => {
            const assessment = W.getVuceAssessment();
            if (typeof window.openKnowledgePanel === 'function') {
                window.openKnowledgePanel('permiso_autorizacion', {
                    titulo: assessment.title || 'Requisitos TUPA',
                    queEs: assessment.message || 'Detalle del trámite de importación restringida.',
                    paraQueSirve: assessment.action || 'Sustentar la importación regulada.'
                });
            }
        });

        // Sweep legacy handlers on other buttons for complete safety
        document.querySelectorAll('[onclick]').forEach((el) => {
            const code = el.getAttribute('onclick') || '';
            let action = null;
            let match = code.match(/^cargarEjemplo\('([^']+)'\)/);
            if (match) action = () => W.cargarEjemplo(match[1]);

            match = code.match(/^openKnowledgePanel\('([^']+)'/);
            if (!action && match && typeof window.openKnowledgePanel === 'function') {
                action = () => window.openKnowledgePanel(match[1]);
            }

            match = code.match(/^selectQuestionOption\('([^']+)',\s*'([^']+)'/);
            if (!action && match) {
                action = () => {
                    const hiddenInput = document.getElementById(match[1]);
                    if (hiddenInput) {
                        hiddenInput.value = match[2];
                    }
                    const btnGroup = el.parentElement;
                    if (btnGroup) {
                        const buttons = btnGroup.querySelectorAll('.q-btn');
                        buttons.forEach(btn => btn.classList.remove('active'));
                    }
                    el.classList.add('active');
                    W.onVuceQuestionChange();
                };
            }

            match = code.match(/^selectIncotermOption\('([^']+)'/);
            if (!action && match) action = () => W.selectIncotermOption(match[1], el);

            match = code.match(/^toggleDoc\('([^']+)'\)/);
            if (!action && match) action = () => W.toggleDoc(match[1]);

            if (!action && code.startsWith('openHsAssistant()')) action = W.openHsAssistant;
            if (!action && code.startsWith('openIncotermsLab()')) action = W.openIncotermsLab;
            if (!action && code.startsWith('selectManualHS()')) action = W.selectManualHS;
            if (!action && code.startsWith('guardarOperacionGeneral()')) action = () => W.guardarOperacionGeneral();
            if (!action && code.startsWith('descargarExpedienteFinal()')) action = W.descargarExpedienteFinal;

            if (!action) return;
            el.removeAttribute('onclick');
            el.addEventListener('click', (event) => {
                event.preventDefault();
                action();
            });
        });
    };

    // Expose selectQuestionOption to window since it is called from JSP inline script
    window.selectQuestionOption = function(fieldId, value, buttonEl) {
        const hiddenInput = document.getElementById(fieldId);
        if (hiddenInput) {
            hiddenInput.value = value;
        }
        if (buttonEl && buttonEl.parentElement) {
            const buttons = buttonEl.parentElement.querySelectorAll('.q-btn');
            buttons.forEach(btn => btn.classList.remove('active'));
            buttonEl.classList.add('active');
        }
        W.onVuceQuestionChange();
    };

})(window.ImportEaseWizard);
