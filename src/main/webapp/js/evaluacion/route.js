// ==========================================================================
// IMPORTEASE - ROUTE & IMPORT PATH MODULE (route.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    W.toggleRucField = function() {
        const tipo = document.getElementById('opTipo').value;
        const container = document.getElementById('rucFieldContainer');
        if (!container) return;
        if (tipo === 'COMERCIAL') {
            container.style.display = 'block';
        } else {
            container.style.display = 'none';
        }
    };

    W.onProdUsoChange = function() {
        const uso = document.getElementById('prodUso').value;
        const opTipo = document.getElementById('opTipo');
        const opRucInput = document.getElementById('opRuc');
        if (uso === 'COMERCIAL') {
            opTipo.value = 'COMERCIAL';
            opRucInput.value = window.userRuc || '';
        } else {
            opTipo.value = 'PERSONAL';
            opRucInput.value = '';
        }
        W.toggleRucField();
        W.updateImportRouteUI();
        W.sugerirCostosFobFleteSeguro();
        W.updateSidebar();
        W.saveWizardDraft();
    };

    W.selectImportRoute = function(route) {
        const prodUso = document.getElementById('prodUso');
        if (!prodUso) return;
        prodUso.value = route;
        W.onProdUsoChange();
    };

    W.updateImportRouteUI = function() {
        const prodUso = document.getElementById('prodUso');
        const isCommercial = prodUso && prodUso.value === 'COMERCIAL';
        const personalCard = document.getElementById('routeCardPersonal');
        const commercialCard = document.getElementById('routeCardComercial');
        
        if (personalCard && commercialCard) {
            if (isCommercial) {
                commercialCard.classList.add('selected');
                personalCard.classList.remove('selected');
            } else {
                personalCard.classList.add('selected');
                commercialCard.classList.remove('selected');
            }
        }
        
        // Update active route panel (green card)
        const activeRouteTitle = document.getElementById('activeRouteTitle');
        const activeRouteDesc = document.getElementById('activeRouteDesc');
        const activeRouteIcon = document.getElementById('activeRouteIcon');
        
        if (activeRouteTitle) {
            activeRouteTitle.textContent = isCommercial ? 'Vender o Negocio' : 'Uso propio';
        }
        if (activeRouteDesc) {
            activeRouteDesc.textContent = isCommercial 
                ? 'Estamos configurando esta importación para comercialización o negocio.' 
                : 'Estamos configurando esta importación para uso personal.';
        }
        if (activeRouteIcon) {
            if (isCommercial) {
                activeRouteIcon.innerHTML = `<svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"/></svg>`;
            } else {
                activeRouteIcon.innerHTML = `<svg class="w-6 h-6" fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>`;
            }
        }
    };

    W.syncOpNombre = function() {
        const prod = document.getElementById('prodNombre').value;
        document.getElementById('opNombre').value = prod ? "Importación de " + prod : "Evaluación de producto";
        W.wizardData.prodNombre = prod;
        W.wizardData.opNombre = document.getElementById('opNombre').value;
        W.sugerirCostosFobFleteSeguro();
        W.updateSidebar();
    };

})(window.ImportEaseWizard);
