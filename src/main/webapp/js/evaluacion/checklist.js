// ==========================================================================
// IMPORTEASE - DOCUMENTATION CHECKLIST MODULE (checklist.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    W.hasPendingMandatoryDocs = function() {
        const basePending = !W.wizardData.docs.FACTURA_COMERCIAL || !W.wizardData.docs.BILL_OF_LADING;
        const certPending = W.wizardData.tribTlc === 'SI' && !W.wizardData.docs.CERTIFICADO_ORIGEN;
        return basePending || certPending;
    };

    W.updateExpedienteReadiness = function(assessment, docPending) {
        const title = document.getElementById('expedienteReadyTitle');
        const text = document.getElementById('expedienteReadyText');
        if (!title || !text) return;

        if (assessment.status === 'required' && !W.wizardData.vuceSuce) {
            title.innerText = 'Primero revisa el permiso';
            text.innerText = 'El producto podria necesitar autorizacion. Prepara los datos de la entidad antes de cerrar la carpeta.';
        } else if (docPending) {
            title.innerText = 'Faltan documentos basicos';
            text.innerText = 'Necesitas al menos factura comercial y documento de transporte para dejar lista la carpeta.';
        } else {
            title.innerText = 'Listo para seguir';
            text.innerText = 'Tu carpeta base ya esta armada. Guarda la evaluacion y continua desde Seguimiento.';
        }
    };

    W.toggleDoc = function(tipo, forceValue) {
        if (typeof forceValue === 'boolean') {
            W.wizardData.docs[tipo] = forceValue;
        } else {
            W.wizardData.docs[tipo] = !W.wizardData.docs[tipo];
        }
        
        if (W.registeredImportId) {
            fetch(`${window.ctx}/api/importacion/registrarDocumentoOperacion?id=${W.registeredImportId}&tipo=${encodeURIComponent(tipo)}`, {
                method: 'POST',
                headers: W.csrfHeaders()
            })
            .then(r => r.json())
            .then(data => {
                if (data.warning && W.wizardData.docs[tipo]) {
                    W.mostrarNotificacion('Recuerda subir el archivo real del documento', 'warning');
                }
                W.updateDocFileIndicator(tipo, data.tieneArchivos !== false);
            })
            .catch(() => {});
        }

        W.updateDocButtonsUI();
        W.updateSidebar();
        
        W.mostrarNotificacion("Checklist documental actualizado");
    };

    W.updateDocButtonsUI = function() {
        const types = ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN', 'PACKING_LIST', 'OTROS_DOCUMENTOS'];
        types.forEach(tipo => {
            const btn = document.getElementById('btnDoc-' + tipo);
            if (btn) {
                if (W.wizardData.docs[tipo]) {
                    btn.className = "px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-600 border border-emerald-200 transition-all cursor-pointer";
                    btn.innerText = "Marcado como listo";
                } else {
                    btn.className = "px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-50 text-rose-600 border border-rose-200 transition-all cursor-pointer";
                    btn.innerText = "Pendiente";
                }
            }
            const checkbox = document.querySelector(`input[data-check-doc="${tipo}"]`);
            if (checkbox) {
                checkbox.checked = !!W.wizardData.docs[tipo];
            }
        });
    };

    W.updateDocFileIndicator = function(tipo, hasFile) {
        const indicator = document.getElementById('docFileIndicator-' + tipo);
        if (!indicator) return;
        if (!W.wizardData.docs[tipo]) {
            indicator.className = 'hidden';
            return;
        }
        if (hasFile) {
            indicator.className = 'ml-2 text-[9px] text-emerald-400 font-bold';
            indicator.innerText = '✓ Archivo subido';
            indicator.title = 'El archivo ha sido cargado correctamente';
        } else {
            indicator.className = 'ml-2 text-[9px] text-amber-400 font-bold';
            indicator.innerText = '⚠ Sin archivo';
            indicator.title = 'Recuerda subir el archivo real del documento';
        }
    };

    W.refreshDocFileIndicators = function() {
        if (!W.registeredImportId) return;
        fetch(`${window.ctx}/api/documentos/listar?importacionId=${W.registeredImportId}`, {
            headers: W.csrfHeaders()
        })
        .then(r => r.json())
        .then(docs => {
            const fileMap = {};
            if (Array.isArray(docs)) {
                docs.forEach(d => {
                    if (d.tipo_documento && d.ruta_archivo) {
                        fileMap[d.tipo_documento] = true;
                    }
                });
            }
            ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN', 'PACKING_LIST', 'OTROS_DOCUMENTOS'].forEach(tipo => {
                W.updateDocFileIndicator(tipo, !!fileMap[tipo]);
            });
        })
        .catch(() => {});
    };

})(window.ImportEaseWizard);
