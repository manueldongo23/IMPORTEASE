// ==========================================================================
// IMPORTEASE - MAIN WIZARD ENTRY & INITIALIZER MODULE (index.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    // Fetch interceptor to handle session timeout emergency saves
    // Se compone con el interceptor de progress bar via flag global
    (function() {
        const baseFetch = window.fetch;
        window.fetch = async function(...args) {
            try {
                const response = await baseFetch(...args);
                if (response.status === 401 || response.status === 403) {
                    if (typeof W.wizardData !== 'undefined' && typeof W.saveWizardDraft === 'function') {
                        W.saveWizardDraft();
                        try {
                            sessionStorage.setItem('importease_emergency_draft', JSON.stringify(W.wizardData));
                        } catch (e) {}
                    }
                }
                return response;
            } catch (error) {
                throw error;
            }
        };
        window.__fetchPatched = true;
    })();

    W.initWizardNormally = function() {
        W.toggleRucField();
        W.updateImportRouteUI();
        W.renderDynamicQuestions();
        W.fetchExchangeRate();
        W.updateCIFCalculations();
        W.restoreHsReturnSelection();
        W.restoreIncotermReturnSelection();
        W.restoreRequestedStepFromQuery();
        W.updateSidebar();
    };

    // Expose select globals to legacy scripts in JSP
    window.toggleModoDidactico = function() {
        W.modoDidacticoActivo = true;
        const boxes = document.querySelectorAll('.didactic-box');
        boxes.forEach((box) => box.classList.remove('hidden'));
    };

    // Initialize module setup on DOMContentLoaded
    document.addEventListener('DOMContentLoaded', () => {
        W.bindWizardActions();

        // Attach listeners to input fields for instant calculation feedback
        ['prodNombre', 'prodTecnica', 'prodCantidad', 'prodMarca', 'prodModelo', 'opPaisOrigen', 'opRuc'].forEach((id) => {
            const el = document.getElementById(id);
            if (!el) return;
            el.removeAttribute('oninput');
            el.removeAttribute('onchange');
            el.addEventListener('input', () => {
                if (id === 'prodNombre') W.syncOpNombre();
                W.sugerirCostosFobFleteSeguro();
                W.updateSidebar();
                W.evalInputRealTimeAlerts();
                W.saveWizardDraft();
            });
            el.addEventListener('change', () => {
                W.sugerirCostosFobFleteSeguro();
                W.updateSidebar();
                W.evalInputRealTimeAlerts();
                W.saveWizardDraft();
            });
        });
        
        window.toggleModoDidactico();
        W.checkAndPromptDraft();
    });

})(window.ImportEaseWizard);
