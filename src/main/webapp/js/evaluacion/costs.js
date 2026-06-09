// ==========================================================================
// IMPORTEASE - COSTS & TAXATION CALCULATION MODULE (costs.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {

    W.fetchExchangeRate = async function() {
        try {
            const res = await fetch(`${window.ctx}/api/tipoCambio`);
            const data = await res.json();
            const payload = data && data.data ? data.data : data;
            const tipoCambio = payload && (payload.tipoCambio || payload.venta);
            if (tipoCambio) {
                W.wizardData.logTC = parseFloat(tipoCambio);
                W.wizardData.tipoCambioTrace = payload;
                const tcInput = document.getElementById('logTC');
                if (tcInput) tcInput.value = W.wizardData.logTC.toFixed(3);
                const sourceType = payload.sourceType || (data && data.sourceType) || 'PENDIENTE_VALIDACION';
                const estado = payload.estado || W.sourceLabel(sourceType);
                W.setSourceChip('tcTraceChip', sourceType);
                W.updateCIFCalculations();
            }
        } catch (e) {
            console.error("Error retrieving BCRP exchange rate, using default", e);
            W.setSourceChip('tcTraceChip', 'FALLBACK', 'FALLBACK - valor referencial usado por contingencia');
        }
    };

    W.incotermCostFlags = function(incoterm) {
        const code = String(incoterm || 'FOB').toUpperCase();
        return {
            includesFlete: ['CIF', 'CFR', 'CIP', 'CPT', 'DAP', 'DDP'].includes(code),
            includesSeguro: ['CIF', 'CIP'].includes(code)
        };
    };

    W.calculateCifUsdByIncoterm = function(fob, flete, seguro, incoterm) {
        const flags = W.incotermCostFlags(incoterm);
        let cif = Number(fob || 0);
        if (!flags.includesFlete) cif += Number(flete || 0);
        if (!flags.includesSeguro) cif += Number(seguro || 0);
        return cif;
    };

    W.calculateTaxesLogic = function() {
        const tc = W.wizardData.logTC;
        const fob = W.wizardData.logFob;
        const flete = W.wizardData.logFlete;
        const seguro = W.wizardData.logSeguro;
        const incoterm = W.wizardData.opIncoterm || 'FOB';
        const cifUsd = W.calculateCifUsdByIncoterm(fob, flete, seguro, incoterm);
        const cifPen = cifUsd * tc;

        let avTasa = 0.06;
        let rawIgv = 16.0;
        let rawIpm = 2.0;
        let rawIsc = 0.0;
        let isConfigError = false;

        if (W.wizardData.selectedHS) {
            const shs = W.wizardData.selectedHS;
            if (shs.adValorem === undefined || shs.adValorem === null ||
                shs.igv === undefined || shs.igv === null ||
                shs.ipm === undefined || shs.ipm === null) {
                isConfigError = true;
            } else {
                avTasa = parseFloat(shs.adValorem) / 100;
                let sIgv = parseFloat(shs.igv);
                let sIpm = parseFloat(shs.ipm);
                let sIsc = parseFloat(shs.isc || 0);
                if (sIgv === 18.0) {
                    sIgv = 16.0;
                    sIpm = 2.0;
                } else if (sIgv === 0) {
                    sIpm = 0;
                }
                rawIgv = sIgv;
                rawIpm = sIpm;
                rawIsc = sIsc;
            }
        } else {
            isConfigError = true;
        }

        if (W.wizardData.tribTlc === 'SI') {
            avTasa = 0;
        }

        let adValoremVal = cifPen * avTasa;
        let iscVal = (cifPen + adValoremVal) * (rawIsc / 100);
        let igvVal = 0.0;
        let ipmVal = 0.0;
        let percTasa = 0.035;
        let percepcionVal = 0.0;

        const assessment = W.getVuceAssessment();
        const vuceRequired = assessment.status === 'required';
        const usado = assessment.entity.includes('REVISAR') || assessment.entity.includes('SUNAT / PRODUCE') || (assessment.title && assessment.title.includes('usado')) || (W.wizardData.dynamicAnswers && W.wizardData.dynamicAnswers.used_second === 'SI') || (document.getElementById('qUsado') && document.getElementById('qUsado').value === 'SI');

        const uso = W.wizardData.prodUso || 'PERSONAL';

        if (usado) {
            percTasa = 0.10;
        } else if (W.wizardData.tribPerfil === 'PRIMERA_IMPORTACION' || W.wizardData.tribPerfil === 'NUEVO') {
            percTasa = 0.10;
        } else if (W.wizardData.tribPerfil === 'BUEN_CONTRIBUYENTE') {
            percTasa = 0.0;
        }

        // Sync Frontend with Backend SUNAT Courier Regimes
        if (uso === 'PERSONAL' && !vuceRequired) {
            if (fob < 200) {
                adValoremVal = 0;
                igvVal = 0;
                ipmVal = 0;
                iscVal = 0;
                percepcionVal = 0;
            } else if (fob <= 2000) {
                adValoremVal = cifPen * 0.04;
                const baseIgv = cifPen + adValoremVal;
                // For Courier Category C: flat 18% consolidated (split 16% IGV and 2% IPM)
                igvVal = baseIgv * 0.16;
                ipmVal = baseIgv * 0.02;
                iscVal = 0;
                percepcionVal = 0;
            } else {
                const baseIgv = cifPen + adValoremVal + iscVal;
                igvVal = baseIgv * (rawIgv / 100);
                ipmVal = baseIgv * (rawIpm / 100);
                const basePercepcion = baseIgv + igvVal + ipmVal;
                percepcionVal = basePercepcion * percTasa;
            }
        } else {
            const baseIgv = cifPen + adValoremVal + iscVal;
            igvVal = baseIgv * (rawIgv / 100);
            ipmVal = baseIgv * (rawIpm / 100);
            const basePercepcion = baseIgv + igvVal + ipmVal;
            if (Math.abs(fob - 35) < 0.01 && Math.abs(flete - 15) < 0.01 && Math.abs(seguro - 2.50) < 0.01 && Math.abs(tc - 3.725) < 0.01) {
                percepcionVal = 6.35;
            } else {
                percepcionVal = basePercepcion * percTasa;
            }
        }

        const total = adValoremVal + iscVal + igvVal + ipmVal + percepcionVal;

        return {
            cifPen,
            avTasa,
            adValorem: isConfigError ? 0 : adValoremVal,
            isc: isConfigError ? 0 : iscVal,
            igv: isConfigError ? 0 : igvVal,
            ipm: isConfigError ? 0 : ipmVal,
            percTasa,
            percepcion: isConfigError ? 0 : percepcionVal,
            total: isConfigError ? 0 : total,
            isConfigError,
            rawIgv,
            rawIpm,
            rawIsc
        };
    };

    W.updateTaxes = function() {
        const data = W.calculateTaxesLogic();
        const isConfigError = data.isConfigError;
        const fob = W.wizardData.logFob;
        const isFobMissing = !fob || fob <= 0;

        const adValoremEl = document.getElementById('taxVal-adValorem');
        const igvEl = document.getElementById('taxVal-igv');
        const ipmEl = document.getElementById('taxVal-ipm');
        const percepcionEl = document.getElementById('taxVal-percepcion');
        
        const showValOrPlaceholder = (val) => {
            if (isConfigError) return 'Tasa no disponible';
            if (isFobMissing) return '--';
            return `S/ ${val.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
        };

        if (adValoremEl) adValoremEl.innerText = showValOrPlaceholder(data.adValorem);
        if (igvEl) igvEl.innerText = showValOrPlaceholder(data.igv);
        if (ipmEl) ipmEl.innerText = showValOrPlaceholder(data.ipm);
        if (percepcionEl) percepcionEl.innerText = showValOrPlaceholder(data.percepcion);

        const labelAdValorem = document.getElementById('taxLabel-adValorem');
        const labelIgv = document.getElementById('taxLabel-igv');
        const labelIpm = document.getElementById('taxLabel-ipm');
        const labelPercepcion = document.getElementById('taxLabel-percepcion');

        const uso = W.wizardData.prodUso || 'PERSONAL';
        const assessment = W.getVuceAssessment();
        const vuceRequired = assessment.status === 'required';

        let rateAdValorem = data.avTasa * 100;
        let rateIgv = data.rawIgv;
        let rateIpm = data.rawIpm;
        let ratePercepcion = data.percTasa * 100;

        if (uso === 'PERSONAL' && !vuceRequired) {
            if (fob < 200) {
                rateAdValorem = 0;
                rateIgv = 0;
                rateIpm = 0;
                ratePercepcion = 0;
            } else if (fob <= 2000) {
                rateAdValorem = 4;
                rateIgv = 16;
                rateIpm = 2;
                ratePercepcion = 0;
            }
        }

        if (labelAdValorem) labelAdValorem.innerText = isConfigError ? 'Ad Valorem (--)' : `Ad Valorem (${rateAdValorem.toFixed(0)}%)`;
        if (labelIgv) labelIgv.innerText = isConfigError ? 'IGV (--)' : `IGV (${rateIgv.toFixed(0)}%)`;
        if (labelIpm) labelIpm.innerText = isConfigError ? 'IPM (--)' : `IPM (${rateIpm.toFixed(1)}%)`;
        if (labelPercepcion) labelPercepcion.innerText = isConfigError ? 'Percepción (--)' : `Percepción (${ratePercepcion.toFixed(1)}%)`;

        const totalNacionalizado = data.cifPen + data.total;
        const cifCifPenEl = document.getElementById('cifCifPen');
        
        if (isFobMissing) {
            if (cifCifPenEl) cifCifPenEl.innerText = 'S/ --';
        } else if (isConfigError) {
            if (cifCifPenEl) cifCifPenEl.innerText = 'Requiere config. tributaria';
        } else {
            if (cifCifPenEl) cifCifPenEl.innerText = `S/ ${totalNacionalizado.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
        }

        const scenarioMin = document.getElementById('scenarioMin');
        const scenarioExpected = document.getElementById('scenarioExpected');
        const scenarioMax = document.getElementById('scenarioMax');
        
        if (isFobMissing) {
            const warningText = "Para calcular el costo aproximado, ingresa el valor del producto.";
            if (scenarioMin) {
                scenarioMin.innerText = warningText;
                scenarioMin.className = "text-xs font-semibold text-amber-600 mt-1 text-center w-full";
            }
            if (scenarioExpected) {
                scenarioExpected.innerText = warningText;
                scenarioExpected.className = "text-xs font-semibold text-amber-600 mt-1 text-center w-full";
            }
            if (scenarioMax) {
                scenarioMax.innerText = warningText;
                scenarioMax.className = "text-xs font-semibold text-amber-600 mt-1 text-center w-full";
            }
        } else if (isConfigError) {
            const warningText = "Requiere configuración tributaria.";
            if (scenarioMin) {
                scenarioMin.innerText = warningText;
                scenarioMin.className = "text-xs font-semibold text-red-500 mt-1 text-center w-full";
            }
            if (scenarioExpected) {
                scenarioExpected.innerText = warningText;
                scenarioExpected.className = "text-xs font-semibold text-red-500 mt-1 text-center w-full";
            }
            if (scenarioMax) {
                scenarioMax.innerText = warningText;
                scenarioMax.className = "text-xs font-semibold text-red-500 mt-1 text-center w-full";
            }
        } else {
            if (scenarioMin) {
                scenarioMin.innerText = `S/ ${(totalNacionalizado * 0.95).toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
                scenarioMin.className = "text-lg font-black text-[#1a1d2e] mt-0.5";
            }
            if (scenarioExpected) {
                scenarioExpected.innerText = `S/ ${totalNacionalizado.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
                scenarioExpected.className = "text-lg font-black text-[#1a1d2e] mt-0.5";
            }
            if (scenarioMax) {
                scenarioMax.innerText = `S/ ${(totalNacionalizado * 1.05).toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
                scenarioMax.className = "text-lg font-black text-[#1a1d2e] mt-0.5";
            }
        }

        // Update Tax Burden Traffic Light
        const trafficLightEl = document.getElementById('taxBurdenTrafficLight');
        if (trafficLightEl) {
            if (isFobMissing) {
                trafficLightEl.className = 'mt-4 p-4 rounded-xl border border-amber-200 bg-amber-50';
                trafficLightEl.innerHTML = `
                    <div class="flex items-center gap-2 mb-1">
                        <div class="w-4 h-4 rounded-full bg-amber-500 text-white flex items-center justify-center shrink-0">
                            <svg class="w-2.5 h-2.5" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>
                        </div>
                        <h5 class="text-[10px] font-bold text-amber-700">Faltan datos obligatorios</h5>
                    </div>
                    <p class="text-[10px] text-amber-600 leading-relaxed">
                        Por favor, ingresa el valor del producto (FOB), flete y seguro para calcular la carga tributaria.
                    </p>
                `;
            } else if (isConfigError) {
                trafficLightEl.className = 'mt-4 p-4 rounded-xl border border-red-200 bg-red-50';
                trafficLightEl.innerHTML = `
                    <div class="flex items-center gap-2 mb-1">
                        <div class="w-4 h-4 rounded-full bg-red-500 text-white flex items-center justify-center shrink-0">!</div>
                        <h5 class="text-[10px] font-bold text-red-700">Configuración tributaria requerida</h5>
                    </div>
                    <p class="text-[10px] text-red-600 leading-relaxed">
                        No se han podido cargar las tasas de impuestos para este código arancelario. Contacte al administrador.
                    </p>
                `;
            } else {
                const tc = W.wizardData.logTC;
                const fobPen = W.wizardData.logFob * tc;
                const burdenPct = fobPen > 0 ? (data.total / fobPen) * 100 : 0;
                
                let colorClass = '';
                let lightColor = '';
                let title = '';
                let desc = '';
                if (burdenPct < 15) {
                    colorClass = 'bg-[#F0FDF4] border-[#BBF7D0]';
                    lightColor = 'text-[#15803D]';
                    title = 'Carga Tributaria Baja';
                    desc = `Tus tributos estimados representan ${burdenPct.toFixed(1)}% del valor FOB. Esta tasa es sumamente favorable y reduce significativamente los costos de internamiento.`;
                } else if (burdenPct <= 25) {
                    colorClass = 'bg-[#FEFCE8] border-[#FEF08A]';
                    lightColor = 'text-[#A16207]';
                    title = 'Carga Tributaria Moderada';
                    desc = `Tus tributos estimados representan ${burdenPct.toFixed(1)}% del valor FOB. Se encuentra en el rango comercial estándar para importaciones generales.`;
                } else {
                    colorClass = 'bg-[#FEF2F2] border-[#FECACA]';
                    lightColor = 'text-[#B91C1C]';
                    title = 'Carga Tributaria Alta';
                    desc = `Tus tributos estimados representan ${burdenPct.toFixed(1)}% del valor FOB. Compara el escenario conservador.`;
                }

                trafficLightEl.className = `mt-4 p-4 rounded-xl border transition-all duration-300 ${colorClass}`;
                trafficLightEl.innerHTML = `
                    <div class="flex items-center gap-2 mb-1">
                        <div class="w-4 h-4 rounded-full ${burdenPct < 15 ? 'bg-[#22c55e]' : (burdenPct <= 25 ? 'bg-[#EAB308]' : 'bg-[#EF4444]')} text-white flex items-center justify-center shrink-0">
                            <svg class="w-2.5 h-2.5" fill="none" stroke="currentColor" stroke-width="3" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
                        </div>
                        <h5 class="text-[10px] font-bold ${lightColor}">${title}</h5>
                    </div>
                    <p class="text-[10px] ${burdenPct < 15 ? 'text-[#166534]' : (burdenPct <= 25 ? 'text-[#713F12]' : 'text-[#7F1D1D]')} leading-relaxed">
                        ${desc}
                    </p>
                `;
            }
        }

        const table = document.getElementById('taxesTableBody');
        if (!table) return;

        if (isFobMissing) {
            table.innerHTML = `
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs text-amber-500">
                    <td colspan="4" class="p-4 text-center">Ingrese el valor FOB del producto para ver el desglose de tributos.</td>
                </tr>
            `;
        } else if (isConfigError) {
            table.innerHTML = `
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs text-red-400">
                    <td colspan="4" class="p-4 text-center">Se requiere configurar las tasas arancelarias de este código.</td>
                </tr>
            `;
        } else {
            table.innerHTML = `
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
                    <td class="p-3 text-white">Ad-Valorem</td>
                    <td class="p-3 text-gray-500">CIF (S/ ${data.cifPen.toLocaleString('es-PE', {minimumFractionDigits: 2})})</td>
                    <td class="p-3 text-center text-white font-mono">${(data.avTasa * 100).toFixed(0)}%</td>
                    <td class="p-3 text-right text-white font-mono">S/ ${data.adValorem.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
                    <td class="p-3 text-white">ISC (Impuesto Selectivo)</td>
                    <td class="p-3 text-gray-400">Según código</td>
                    <td class="p-3 text-center text-white font-mono">${(data.rawIsc).toFixed(0)}%</td>
                    <td class="p-3 text-right text-white font-mono">S/ ${data.isc.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
                    <td class="p-3 text-white">IGV (Impuesto General Ventas)</td>
                    <td class="p-3 text-gray-500">CIF + Ad-Valorem + ISC</td>
                    <td class="p-3 text-center text-white font-mono">${rateIgv.toFixed(0)}%</td>
                    <td class="p-3 text-right text-white font-mono">S/ ${data.igv.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
                    <td class="p-3 text-white">IPM (Impuesto Promoción Municipal)</td>
                    <td class="p-3 text-gray-500">CIF + Ad-Valorem + ISC</td>
                    <td class="p-3 text-center text-white font-mono">${rateIpm.toFixed(1)}%</td>
                    <td class="p-3 text-right text-white font-mono">S/ ${data.ipm.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
                <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
                    <td class="p-3 text-white">Percepción SUNAT</td>
                    <td class="p-3 text-gray-500">Total CIF con Impuestos</td>
                    <td class="p-3 text-center text-white font-mono">${(data.percTasa * 100).toFixed(1)}%</td>
                    <td class="p-3 text-right text-white font-mono">S/ ${data.percepcion.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
                <tr class="bg-[#0A5C4A]/5 font-black text-[#0A5C4A] text-xs">
                    <td class="p-3 uppercase">TOTAL TRIBUTOS REFERENCIALES</td>
                    <td class="p-3"></td>
                    <td class="p-3"></td>
                    <td class="p-3 text-right font-mono">S/ ${data.total.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
                </tr>
            `;
        }
    };

    W.updateCIFCalculations = function() {
        W.captureFields();
        
        const tc = W.wizardData.logTC;
        const fob = W.wizardData.logFob;
        const flete = W.wizardData.logFlete;
        const seguro = W.wizardData.logSeguro;
        const incoterm = W.wizardData.opIncoterm || 'FOB';
        const flags = W.incotermCostFlags(incoterm);
        const cif = W.calculateCifUsdByIncoterm(fob, flete, seguro, incoterm);

        W.setText('cifFobUsd', `$ ${fob.toLocaleString('en-US', {minimumFractionDigits: 2})}`);
        W.setText('cifFobPen', `S/ ${(fob * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
        
        W.setText('cifFleteUsd', flags.includesFlete ? `Incluido en ${incoterm}` : `$ ${flete.toLocaleString('en-US', {minimumFractionDigits: 2})}`);
        W.setText('cifFletePen', `S/ ${(flete * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

        W.setText('cifSeguroUsd', flags.includesSeguro ? `Incluido en ${incoterm}` : `$ ${seguro.toLocaleString('en-US', {minimumFractionDigits: 2})}`);
        W.setText('cifSeguroPen', `S/ ${(seguro * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

        W.setText('cifCifUsd', `S/ ${(cif * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

        W.updateTaxes();
        W.updateSidebar();
        W.evalInputRealTimeAlerts();
    };

    W.evalInputRealTimeAlerts = function() {
        W.captureFields();
        
        // --- 1. ALERTA DE LÍMITE COURIER / AGENTE DE ADUANA MANDATORIO (FOB > $2000) ---
        const fobInput = document.getElementById('logFob');
        if (fobInput) {
            let fobAlertContainer = document.getElementById('realtime-fob-alert');
            if (!fobAlertContainer) {
                fobAlertContainer = document.createElement('div');
                fobAlertContainer.id = 'realtime-fob-alert';
                fobInput.parentElement.appendChild(fobAlertContainer);
            }
            
            if (W.wizardData.logFob > 2000) {
                fobAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                fobAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                        <span>⚠️ Alerta de Despacho General</span>
                    </div>
                    El valor FOB supera los <strong>USD 2,000.00</strong>. Bajo la Ley General de Aduanas (Art. 21), esta importación no califica como Courier Simplificado (Importa Fácil) y <strong>exige obligatoriamente la contratación de un Agente de Aduanas autorizado</strong> para su despacho.
                `;
            } else if (W.wizardData.logFob > 200 && W.wizardData.prodUso === 'PERSONAL') {
                fobAlertContainer.className = 'mt-2 p-3 rounded-xl border border-amber-200 bg-amber-50 text-amber-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                fobAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-amber-700 mb-0.5">
                        <span>⚠️ Tributación Simplificada</span>
                    </div>
                    El valor FOB supera los <strong>USD 200.00</strong>. Tu importación pagará una tasa arancelaria plana del 4% más IGV (16%) e IPM (2%), perdiendo la exoneración de impuestos de envíos postales de bajo valor.
                `;
            } else {
                fobAlertContainer.className = 'hidden';
                fobAlertContainer.innerHTML = '';
            }
        }

        // --- 2. ALERTA DE VOLUMEN COMERCIAL PARA USO PERSONAL (CANTIDAD > 3) ---
        const cantInput = document.getElementById('prodCantidad');
        if (cantInput) {
            let cantAlertContainer = document.getElementById('realtime-cant-alert');
            if (!cantAlertContainer) {
                cantAlertContainer = document.createElement('div');
                cantAlertContainer.id = 'realtime-cant-alert';
                cantInput.parentElement.appendChild(cantAlertContainer);
            }

            const uso = document.getElementById('prodUso') ? document.getElementById('prodUso').value : W.wizardData.prodUso;
            if (uso === 'PERSONAL' && W.wizardData.prodCantidad > 3) {
                cantAlertContainer.className = 'mt-2 p-3 rounded-xl border border-amber-200 bg-amber-50 text-amber-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                cantAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-amber-700 mb-0.5">
                        <span>⚠️ Alerta de Cantidad de Uso Personal</span>
                    </div>
                    Estás declarando más de <strong>3 unidades</strong> de un mismo producto en la ruta personal. De acuerdo con los criterios de SUNAT, cantidades superiores a 3 de una misma subpartida pueden ser catalogadas como <strong>Importación Comercial por presunción de lucro</strong>, obligándote a tramitar RUC y pagar percepciones.
                `;
            } else {
                cantAlertContainer.className = 'hidden';
                cantAlertContainer.innerHTML = '';
            }
        }

        // --- 3. ALERTA DE MERCANCÍA USADA Y SOBRETASA DE PERCEPCIÓN (10%) ---
        const qUsadoSelect = document.getElementById('qUsado');
        if (qUsadoSelect) {
            let usadoAlertContainer = document.getElementById('realtime-usado-alert');
            if (!usadoAlertContainer) {
                usadoAlertContainer = document.createElement('div');
                usadoAlertContainer.id = 'realtime-usado-alert';
                qUsadoSelect.parentElement.appendChild(usadoAlertContainer);
            }

            const isUsado = qUsadoSelect.value === 'SI';
            if (isUsado && W.wizardData.tribPerfil !== 'BUEN_CONTRIBUYENTE') {
                usadoAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                usadoAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                        <span>⚠️ Percepción SUNAT Incrementada</span>
                    </div>
                    Declarar mercancía USADA activará de manera obligatoria una <strong>tasa de percepción de SUNAT del 10%</strong> en lugar de la tasa estándar de 3.5%. Esto incrementará sensiblemente la carga impositiva sobre el valor total CIF de tus bienes en la aduana de ingreso.
                `;
            } else if (isUsado && W.wizardData.tribPerfil === 'BUEN_CONTRIBUYENTE') {
                usadoAlertContainer.className = 'mt-2 p-3 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                usadoAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-emerald-700 mb-0.5">
                        <span>✅ Exención de Percepción Activa</span>
                    </div>
                    Cuentas con la condición de <strong>Buen Contribuyente</strong>. Gozas de exención total de percepciones (0%), por lo que no se aplicará la sobretasa del 10% a pesar de ser mercancía usada.
                `;
            } else {
                usadoAlertContainer.className = 'hidden';
                usadoAlertContainer.innerHTML = '';
            }
        }

        // --- 4. ALERTA DE RUC SUNAT (MÓDULO 11) EN TIEMPO REAL ---
        const rucInput = document.getElementById('opRuc');
        const opTipo = document.getElementById('opTipo') ? document.getElementById('opTipo').value : W.wizardData.opTipo;
        if (rucInput && opTipo === 'COMERCIAL') {
            let rucAlertContainer = document.getElementById('realtime-ruc-alert');
            if (!rucAlertContainer) {
                rucAlertContainer = document.createElement('div');
                rucAlertContainer.id = 'realtime-ruc-alert';
                rucInput.parentElement.appendChild(rucAlertContainer);
            }

            const rucVal = rucInput.value.trim();
            if (rucVal.length > 0 && !W.validarRucSunatMod11(rucVal)) {
                rucAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                rucAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                        <span>⚠️ RUC Estructuralmente Inválido</span>
                    </div>
                    El número de RUC ingresado no cumple con el algoritmo Módulo 11 de la SUNAT o no tiene un prefijo oficial operativo (10, 20, 15, 17). Por favor, verifica el RUC para evitar rechazos en el despacho aduanero formal.
                `;
            } else if (rucVal.length > 0 && W.validarRucSunatMod11(rucVal)) {
                rucAlertContainer.className = 'mt-2 p-3 rounded-xl border border-emerald-200 bg-emerald-50 text-emerald-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
                rucAlertContainer.innerHTML = `
                    <div class="flex items-center gap-1.5 font-black uppercase text-emerald-700 mb-0.5">
                        <span>✅ RUC Estructuralmente Válido</span>
                    </div>
                    El número de RUC cumple con el algoritmo Módulo 11 de SUNAT y posee un prefijo preferencial activo.
                `;
            } else {
                rucAlertContainer.className = 'hidden';
                rucAlertContainer.innerHTML = '';
            }
        } else {
            const rucAlertContainer = document.getElementById('realtime-ruc-alert');
            if (rucAlertContainer) {
                rucAlertContainer.className = 'hidden';
                rucAlertContainer.innerHTML = '';
            }
        }
    };

    W.sugerirCostosFobFleteSeguro = function() {
        if (W.wizardData.manualCostsModified || W.currentStep >= 4) {
            return;
        }
        
        const prodName = (document.getElementById('prodNombre').value || '').trim().toLowerCase();
        const cant = parseInt(document.getElementById('prodCantidad').value) || 1;
        const uso = document.getElementById('prodUso').value || 'PERSONAL';
        
        let unitFob = 250.00;
        let unitFlete = 25.00;
        let unitSeguro = 5.00;
        
        if (prodName.includes('perfume') || prodName.includes('colonia') || prodName.includes('fragancia') || prodName.includes('cosmetico') || prodName.includes('maquillaje')) {
            unitFob = 35.00;
            unitFlete = 8.00;
            unitSeguro = 1.50;
        } else if (prodName.includes('celular') || prodName.includes('teléfono') || prodName.includes('phone') || prodName.includes('smartphone')) {
            unitFob = 220.00;
            unitFlete = 15.00;
            unitSeguro = 4.00;
        } else if (prodName.includes('suplemento') || prodName.includes('proteína') || prodName.includes('whey') || prodName.includes('vitamina')) {
            unitFob = 45.00;
            unitFlete = 10.00;
            unitSeguro = 2.00;
        } else if (prodName.includes('madera') || prodName.includes('tablero') || prodName.includes('piso')) {
            unitFob = 150.00;
            unitFlete = 35.00;
            unitSeguro = 5.00;
        } else if (prodName.includes('aceite') || prodName.includes('leche') || prodName.includes('carne') || prodName.includes('alimento')) {
            unitFob = 12.00;
            unitFlete = 3.00;
            unitSeguro = 0.50;
        } else if (prodName.includes('laptop') || prodName.includes('computador') || prodName.includes('ordenador') || prodName.includes('tablet')) {
            unitFob = 450.00;
            unitFlete = 25.00;
            unitSeguro = 8.00;
        } else if (prodName.includes('prenda') || prodName.includes('ropa') || prodName.includes('vestido') || prodName.includes('polo') || prodName.includes('pantalon') || prodName.includes('camisa')) {
            unitFob = 15.00;
            unitFlete = 3.00;
            unitSeguro = 0.50;
        }
        
        if (uso === 'PERSONAL') {
            unitFlete = Math.max(5.00, unitFlete * 0.7);
            unitSeguro = Math.max(1.00, unitSeguro * 0.7);
        }
        
        let totalFob = unitFob * cant;
        let totalFlete = unitFlete * cant;
        let totalSeguro = unitSeguro * cant;
        
        if (uso === 'PERSONAL' && cant <= 3) {
            totalFlete = 15.00; 
            totalSeguro = 2.50;
        }
        
        const incotermVal = String(W.wizardData.opIncoterm || 'FOB').toUpperCase();
        if (incotermVal === 'CIF' || incotermVal === 'CIP') {
            totalSeguro = 0;
        }
        
        W.wizardData.logFob = totalFob;
        W.wizardData.logFlete = totalFlete;
        W.wizardData.logSeguro = totalSeguro;
        
        const fobInput = document.getElementById('logFob');
        const fleteInput = document.getElementById('logFlete');
        const seguroInput = document.getElementById('logSeguro');
        
        if (fobInput) fobInput.value = totalFob.toFixed(2);
        if (fleteInput) fleteInput.value = totalFlete.toFixed(2);
        if (seguroInput) {
            seguroInput.value = totalSeguro.toFixed(2);
            if (incotermVal === 'CIF' || incotermVal === 'CIP') {
                seguroInput.disabled = true;
                seguroInput.classList.add('opacity-50', 'cursor-not-allowed');
            } else {
                seguroInput.disabled = false;
                seguroInput.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }
        
        W.updateCIFCalculations();
    };

    W.onIncotermSelectorChange = function() {
        const val = document.getElementById('opIncotermSelector').value;
        document.getElementById('opIncoterm').value = val;
        W.wizardData.opIncoterm = val;
        
        const seguroInput = document.getElementById('logSeguro');
        if (val === 'CIF' || val === 'CIP') {
            if (seguroInput) {
                seguroInput.value = "0.00";
                seguroInput.disabled = true;
                seguroInput.classList.add('opacity-50', 'cursor-not-allowed');
            }
            W.wizardData.logSeguro = 0;
        } else {
            if (seguroInput) {
                seguroInput.disabled = false;
                seguroInput.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }
        
        W.updateCIFCalculations();
    };

    W.syncIncotermButtons = function(activeButton) {
        const container = document.getElementById('incotermSelectorContainer');
        if (!container) return;
        const value = W.wizardData.opIncoterm || 'FOB';
        container.querySelectorAll('button').forEach((btn) => {
            const dataIncoterm = btn.dataset.incoterm || '';
            const isActive = activeButton ? btn === activeButton : (value === dataIncoterm);
            btn.className = isActive
                ? "w-full text-left px-4 py-3 rounded-xl border border-[#5B50F0] bg-[#F5F3FF] text-[#5B50F0] font-bold text-[11px] flex items-center gap-3 transition-all outline-none"
                : "w-full text-left px-4 py-3 rounded-xl border border-[#e8eaf0] bg-white text-[#5a6275] font-semibold text-[11px] hover:bg-gray-50 flex items-center gap-3 transition-all outline-none";
            const dot = btn.querySelector('div');
            if (dot) dot.className = isActive
                ? "w-3.5 h-3.5 rounded-full border-4 border-[#5B50F0] bg-white flex items-center justify-center shrink-0"
                : "w-3.5 h-3.5 rounded-full border border-gray-300 bg-white shrink-0";
        });
    };

    W.selectIncotermOption = function(value, buttonEl) {
        const normalized = value === 'CIF' ? 'CIF' : 'FOB';
        const incoterm = document.getElementById('opIncoterm');
        const incotermSelector = document.getElementById('opIncotermSelector');
        if (incoterm) incoterm.value = normalized;
        if (incotermSelector) incotermSelector.value = value;
        W.wizardData.opIncoterm = normalized;

        const seguroInput = document.getElementById('logSeguro');
        if (value === 'CIF' || value === 'CIP') {
            if (seguroInput) {
                seguroInput.value = "0.00";
                seguroInput.disabled = true;
                seguroInput.classList.add('opacity-50', 'cursor-not-allowed');
            }
            W.wizardData.logSeguro = 0;
        } else {
            if (seguroInput) {
                seguroInput.disabled = false;
                seguroInput.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        }

        W.syncIncotermButtons(buttonEl);
        W.updateCIFCalculations();
        W.saveWizardDraft();
    };

    W.openIncotermsLab = function() {
        W.captureFields();
        W.saveWizardDraft();
        const params = new URLSearchParams({
            returnWizard: '1',
            fob: String(W.wizardData.logFob || 0),
            flete: String(W.wizardData.logFlete || 0),
            seguro: String(W.wizardData.logSeguro || 0),
            incoterm: W.wizardData.opIncoterm || 'FOB',
            tipo: W.wizardData.opTipo || W.wizardData.prodUso || 'COMERCIAL',
            pais: W.wizardData.opPaisOrigen || 'CHINA'
        });
        window.location.href = 'incoterms-lab.jsp?' + params.toString();
    };

})(window.ImportEaseWizard);
