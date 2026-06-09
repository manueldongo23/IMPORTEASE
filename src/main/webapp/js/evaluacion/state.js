// ==========================================================================
// IMPORTEASE - STATE & UTILITIES MODULE (state.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {
    // Basic constants
    W.WIZARD_DRAFT_KEY = 'importease_wizard_draft';
    W.WIZARD_HS_RETURN_KEY = 'importease_wizard_hs_selection';
    W.WIZARD_INCOTERM_RETURN_KEY = 'importease_wizard_incoterm_decision';
    W.AUTOSAVE_TIME_FORMATTER = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit' });

    // Runtime state variables
    W.currentStep = 1;
    W.modoDidacticoActivo = true;
    W.registeredImportId = null;
    W.SUGGESTIONS_CACHE = new Map();
    W.remoteSearchAbortController = null;

    // Wizard Data Model
    W.wizardData = {
        opNombre: '',
        opTipo: 'PERSONAL',
        opRuc: '',
        opProveedor: '',
        opPaisOrigen: 'CHINA',
        opPaisEmbarque: 'CHINA',
        opIncoterm: 'FOB',
        opMoneda: 'USD',
        opFechaLlegada: '',
        
        prodNombre: '',
        prodTecnica: '',
        prodUso: 'PERSONAL',
        prodCantidad: 1,
        prodUnidad: 'Unidad',
        prodMarca: '',
        prodModelo: '',
        
        selectedHS: null,
        selectedHSOrigin: 'AUTO',
        
        logFob: 250.00,
        logFlete: 25.00,
        logSeguro: 5.00,
        logOtros: 0.00,
        logTC: 3.745,
        manualCostsModified: false,
        
        tribPerfil: 'ESTANDAR',
        tribTlc: 'NO',
        tribCertificado: 'Pendiente',
        
        vuceQuestions: {
            wifi: false,
            consumo: false,
            contacto: false,
            salud: false,
            usado: false,
            ficha: false
        },
        dynamicProfile: 'general',
        dynamicQuestions: [],
        dynamicAnswers: {},
        
        docs: {
            FACTURA_COMERCIAL: false,
            BILL_OF_LADING: false,
            CERTIFICADO_ORIGEN: false,
            PACKING_LIST: false,
            OTROS_DOCUMENTOS: false
        },
        
        vuceEstado: 'EXPEDIENTE_GENERADO',
        vuceSuce: '',
        vuceResolucion: '',
        vuceObs: '',

        tupaDatos: {}
    };

    // --- BASIC CORE UTILITIES ---
    W.getQuestionAnswer = function(fieldId) {
        const el = document.getElementById(fieldId);
        return el ? (el.value || 'NO') : 'NO';
    };

    W.normalizeWizardText = function(value) {
        return String(value || '')
            .toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '')
            .replace(/[^a-z0-9\s.-]/g, ' ')
            .replace(/\s+/g, ' ')
            .trim();
    };

    W.tokenizeWizardText = function(value) {
        return W.normalizeWizardText(value).split(' ').filter((token) => token.length >= 3);
    };

    W.getWizardSearchCorpus = function() {
        return [
            W.wizardData.prodNombre,
            W.wizardData.prodTecnica,
            W.wizardData.prodMarca,
            W.wizardData.prodModelo,
            W.wizardData.opNombre,
            W.wizardData.selectedHS ? W.wizardData.selectedHS.descripcionEs : '',
            W.wizardData.selectedHS ? W.wizardData.selectedHS.codigo : ''
        ].filter(Boolean).join(' ');
    };

    W.getProductInputCorpus = function() {
        return [
            W.wizardData.prodNombre,
            W.wizardData.prodTecnica,
            W.wizardData.prodMarca,
            W.wizardData.prodModelo,
            W.wizardData.opNombre
        ].filter(Boolean).join(' ');
    };

    W.setText = function(id, value) {
        const el = document.getElementById(id);
        if (el) el.innerText = value;
    };

    W.getFieldValue = function(id, fallback = '') {
        const el = document.getElementById(id);
        return el ? el.value : fallback;
    };

    W.setVal = function(id, value) {
        const el = document.getElementById(id);
        if (el && value !== undefined && value !== null) {
            el.value = value;
        }
    };

    W.escapeHtml = function(value) {
        if (value === null || value === undefined) return '';
        return value.toString()
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/\"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    W.csrfHeaders = function(extra = {}) {
        return Object.assign({ 'X-CSRF-TOKEN': window.csrfToken || '' }, extra);
    };

    W.mostrarNotificacion = function(msg, type = 'info') {
        const toast = document.createElement('div');
        toast.className = 'importease-toast importease-toast--' + type;
        toast.innerText = msg;
        document.body.appendChild(toast);
        setTimeout(() => toast.remove(), 2500);
    };

    W.validarRucSunatMod11 = function(ruc) {
        const rucStr = String(ruc || '').trim();
        if (rucStr.length !== 11 || !/^\d+$/.test(rucStr)) return false;
        
        const prefijo = rucStr.substring(0, 2);
        if (!['10', '20', '15', '17'].includes(prefijo)) return false;
        
        const factores = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
        let suma = 0;
        for (let i = 0; i < 10; i++) {
            suma += parseInt(rucStr.charAt(i)) * factores[i];
        }
        
        const residuo = suma % 11;
        let digitoVerificador = 11 - residuo;
        if (digitoVerificador === 10) digitoVerificador = 0;
        else if (digitoVerificador === 11) digitoVerificador = 1;
        
        const ultimoDigito = parseInt(rucStr.charAt(10));
        return digitoVerificador === ultimoDigito;
    };

    // Expose select globals to legacy scripts in JSP
    window.mostrarNotificacion = W.mostrarNotificacion;
    window.wizardData = W.wizardData;

})(window.ImportEaseWizard);
