const ENTIDAD_URLS = {
    'DIGESA': 'https://www.digesa.minsa.gob.pe/',
    'MTC': 'https://portal.mtc.gob.pe/',
    'DIGEMID': 'https://www.digemid.minsa.gob.pe/',
    'SENASA': 'https://www.gob.pe/senasa',
    'SUCAMEC': 'https://www.sucamec.gob.pe/',
    'SANIPES': 'https://www.sanipes.gob.pe/'
};

let codigoActual = '';
let debounceTimer = null;
let selectedHsForWizard = null;
const WIZARD_HS_RETURN_KEY = 'importease_wizard_hs_selection';
const returnToWizard = new URLSearchParams(window.location.search).get('returnWizard') === '1';

// Caché local en memoria y controlador de aborto
const SUGGESTIONS_CACHE = new Map();
let searchAbortController = null;

function normalizarTexto(descripcion) {
    if (!descripcion) return "";
    return descripcion.toLowerCase()
        .normalize("NFD").replace(/[\u0300-\u036f]/g, "")
        .replace(/ñ/g, "n")
        .replace(/[^a-z0-9\s]/g, " ")
        .replace(/\b(pcs|pc|units|unit|kg|gr|g|ml|l|cm|m|in|oz|lb|pack|pk|set|ctn|box|unidades|unidad|kilos|gramos|litros)\b/g, " ")
        .replace(/\b\d+[a-z]*\b/g, " ")
        .replace(/\b[a-z]*\d+\b/g, " ")
        .replace(/\s+/g, " ")
        .trim();
}

function initBuscadorEvents() {
    const input = document.getElementById('hsSearch');
    const btnBuscar = document.getElementById('btnBuscar');
    const acBox = document.getElementById('autocompleteBox');

    if (input) {
        input.addEventListener('input', (e) => onInput(e.target.value));
        input.addEventListener('keydown', onKeyDown);
    }

    if (btnBuscar) {
        btnBuscar.addEventListener('click', buscar);
    }

    // Cierra el autocompletado al hacer clic fuera del buscador
    document.addEventListener('click', (e) => {
        if (!acBox || !input) return;
        if (!acBox.contains(e.target) && e.target !== input) {
            closeAC();
        }
    });

    syncWizardReturnState();
}

function syncWizardReturnState() {
    const banner = document.getElementById('wizardReturnBanner');
    const btn = document.getElementById('btnUsarEnImportacion');
    if (banner) banner.classList.remove('hidden');
    if (btn) btn.classList.toggle('hidden', !returnToWizard || !selectedHsForWizard);
}

function usarCodigoEnImportacion() {
    if (!returnToWizard || !selectedHsForWizard) return;
    localStorage.setItem(WIZARD_HS_RETURN_KEY, JSON.stringify(selectedHsForWizard));
    window.location.href = 'evaluacion.jsp';
}

function verMercadoHs() {
    const code = codigoActual || (selectedHsForWizard && selectedHsForWizard.codigo) || '';
    if (!code) return;
    window.location.href = 'observatorio-hs.jsp?codigo=' + encodeURIComponent(String(code).replace(/\./g, ''));
}

function sourceLabel(type) {
    const value = (type || 'PENDIENTE_VALIDACION').toUpperCase();
    const labels = {
        OFICIAL_API: 'OFICIAL/API',
        OFICIAL_WEB: 'OFICIAL/WEB',
        TERCERO_API: 'TERCERO',
        BD_LOCAL: 'BD LOCAL',
        ESTIMADO: 'ESTIMADO',
        SIMULADO: 'SIMULADO',
        FALLBACK: 'FALLBACK',
        CACHE: 'CACHE',
        MANUAL: 'MANUAL',
        MANUAL_VERIFICADO: 'MANUAL VERIFICADO',
        PENDIENTE_CREDENCIALES: 'PENDIENTE CREDENCIALES',
        PENDIENTE_VALIDACION: 'PENDIENTE VALIDACION'
    };
    return labels[value] || value.replace(/_/g, ' ');
}

function setSourceChip(id, type, label) {
    const el = document.getElementById(id);
    if (!el) return;
    const value = (type || 'PENDIENTE_VALIDACION').toUpperCase();
    const group = value.includes('OFICIAL') ? 'official'
        : value === 'BD_LOCAL' ? 'bd'
        : value === 'CACHE' ? 'cache'
        : value === 'FALLBACK' ? 'fallback'
        : value === 'SIMULADO' ? 'simulated'
        : value === 'ESTIMADO' || value === 'MANUAL' || value === 'MANUAL_VERIFICADO' ? 'estimated'
        : value === 'TERCERO_API' ? 'third'
        : 'pending';
    el.className = 'source-chip source-chip--' + group;
    el.textContent = label || sourceLabel(value);
}

function onInput(val) {
    clearTimeout(debounceTimer);
    const cleanVal = (val || '').trim();
    if (cleanVal.length < 1) {
        syncNativeOptions([]);
        closeAC();
        return;
    }
    debounceTimer = setTimeout(() => liveSearch(val), 300);
}

async function liveSearch(val) {
    const cleanVal = normalizarTexto(val) || (val || '').trim().toLowerCase();
    if (!cleanVal) return;

    // 1. Verificar en caché local (0ms)
    if (SUGGESTIONS_CACHE.has(cleanVal)) {
        const cachedList = SUGGESTIONS_CACHE.get(cleanVal);
        syncNativeOptions(cachedList);
        renderAC(cachedList);
        return;
    }

    // 2. Abortar petición fetch activa previa si existe
    if (searchAbortController) {
        searchAbortController.abort();
    }
    searchAbortController = new AbortController();

    try {
        const res = await fetch(window.ctx + '/api/hs/sugerencias?termino=' + encodeURIComponent(cleanVal), {
            signal: searchAbortController.signal
        });
        if (!res.ok) {
            closeAC();
            return;
        }
        const lista = await res.json();
        if (!Array.isArray(lista)) {
            closeAC();
            return;
        }
        
        // Guardar en la caché
        SUGGESTIONS_CACHE.set(cleanVal, lista);

        syncNativeOptions(lista);
        renderAC(lista);
    } catch(e) {
        if (e.name !== 'AbortError') {
            syncNativeOptions([]);
        }
    }
}

function renderAC(lista) {
    const box = document.getElementById('acList');
    if (!lista || lista.length === 0) { closeAC(); return; }
    box.innerHTML = '';

    lista.forEach((hs, i) => {
        const item = document.createElement('button');
        item.className = 'ac-item w-full px-8 py-5 flex items-center gap-6 text-left border-transparent transition-all duration-200 group hover:bg-[#FAF6F0]';
        const restrictedBadge = hs.requiereVuce
            ? '<span class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-amber-50 text-orange-600 border border-orange-200">Revisar permiso</span>'
            : '<span class="px-2 py-0.5 rounded text-[8px] font-black uppercase bg-emerald-50 text-emerald-600 border border-emerald-200">Sin alerta</span>';

        item.innerHTML = `
            <div class="w-10 h-10 rounded-xl bg-gray-100 text-gray-400 flex items-center justify-center group-hover:scale-105 transition-all font-black text-xs">
                ${i + 1}
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-sm font-black font-mono text-[#1F2937] tracking-tight ac-code"></p>
                <p class="text-[10px] text-gray-400 truncate uppercase mt-1 font-bold tracking-tight ac-desc"></p>
            </div>
            <div class="text-right">
                ${restrictedBadge}
            </div>
        `;

        item.querySelector('.ac-code').textContent = hs.codigo;
        item.querySelector('.ac-desc').append(document.createTextNode(hs.descripcionEs));
        item.onclick = () => seleccionarAC(hs);
        box.appendChild(item);
    });
    document.getElementById('autocompleteBox').classList.remove('hidden');
}

function syncNativeOptions(lista) {
    const datalist = document.getElementById('hsSearchOptions');
    if (!datalist) return;
    datalist.innerHTML = '';
    if (!Array.isArray(lista)) return;

    lista.slice(0, 20).forEach((hs) => {
        const opt = document.createElement('option');
        opt.value = `${hs.codigo} - ${hs.descripcionEs}`;
        datalist.appendChild(opt);
    });
}

function seleccionarAC(hs) {
    document.getElementById('hsSearch').value = hs.codigo + ' - ' + hs.descripcionEs;
    closeAC();
    mostrarResultadoDirecto(hs);
}

function closeAC() {
    document.getElementById('autocompleteBox').classList.add('hidden');
}

function onKeyDown(e) {
    if (e.key === 'Enter') buscar();
    else if (e.key === 'Escape') closeAC();
}

/** Registra búsqueda en el motor de Inteligencia de Mercado */
function registrarBusqueda(termino, hsCode, tipo) {
    try {
        fetch(window.ctx + '/api/tendencias/registrar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': window.csrfToken || ''
            },
            body: JSON.stringify({ termino, hsCode, tipo })
        });
    } catch(e) { /* silencioso */ }
}

async function buscar() {
    closeAC();
    const val = document.getElementById('hsSearch').value.trim();
    if (!val) return;
    const rawVal = val.split(' - ')[0].trim();

    setBtnLoading(true);
    try {
        const esNum = /^\d+$/.test(rawVal.replace(/\./g, ''));
        const param = esNum ? 'codigo=' + rawVal.replace(/\./g, '') : 'producto=' + encodeURIComponent(rawVal);
        const res = await fetch(window.ctx + '/api/hs/buscar?' + param);
        const hs = await res.json();
        if (hs.error || !hs.hsCode) {
            mostrarError('No encontramos una coincidencia clara. Describe mejor el producto o prueba con marca, uso o material.');
        } else {
            mostrarResultado(hs);
            // Registrar búsqueda para Inteligencia de Mercado
            registrarBusqueda(rawVal, hs.hsCode, 'PRODUCTO');
        }
    } catch(e) {
        mostrarError('No pudimos consultar la fuente. Intenta nuevamente en unos segundos.');
    } finally {
        setBtnLoading(false);
    }
}

function mostrarResultado(hs) {
    const code = (hs.hsCode || '').replace(/\./g, '');
    actualizarDigito('digit1', code.substring(0, 2) || '--');
    actualizarDigito('digit2', code.substring(2, 4) || '--');
    actualizarDigito('digit3', code.substring(4, 6) || '--');
    actualizarDigito('digit4', code.substring(6)   || '----');
    codigoActual = code;
    selectedHsForWizard = {
        codigo: hs.hsCode || hs.codigo || code,
        descripcionEs: hs.descripcion || hs.descripcionEs || '',
        adValorem: parseFloat(hs.adValorem) || 0,
        requiereVuce: !!(hs.requiereVUCE || hs.requiereVuce),
        entidadVuce: hs.entidadVUCE || hs.entidadVuce || 'SUNAT',
        sourceType: hs.sourceType || hs.fuenteDato || (String(hs.descripcion || hs.descripcionEs || '').includes('[Aproximado]') ? 'ESTIMADO' : 'BD_LOCAL'),
        confidence: typeof hs.confidence === 'number' ? hs.confidence : 0.85
    };
    setSourceChip('hsSearchSourceChip', selectedHsForWizard.sourceType);
    const confidenceEl = document.getElementById('hsSearchConfidenceText');
    if (confidenceEl) {
        confidenceEl.textContent = `Fuente: ${sourceLabel(selectedHsForWizard.sourceType)} | Confianza ${Math.round(selectedHsForWizard.confidence * 100)}%`;
    }
    const marketBtn = document.getElementById('btnVerMercado');
    if (marketBtn) marketBtn.classList.remove('hidden');
    syncWizardReturnState();

    const desc = document.getElementById('hsDescripcion');
    desc.style.opacity = '0';
    setTimeout(() => {
        desc.innerText = hs.descripcion || 'Descripcion referencial no disponible.';
        desc.style.opacity = '1';
        desc.classList.remove('italic', 'text-gray-500');
        desc.classList.add('text-[#1F2937]');
    }, 200);

    const av = parseFloat(hs.adValorem) || 0;
    const isc = parseFloat(hs.isc) || 0;
    animarTributo('tribAV', 'barAV', av, 11);
    animarTributo('tribIGV', 'barIGV', 18, 18);
    animarTributo('tribISC', 'barISC', isc, 20);

    // Mostrar info del producto
    mostrarInfoProducto(code, hs.descripcion || '', av, hs.requiereVUCE, hs.entidadVUCE);

    const badge = document.getElementById('vuceBadge');
    const iconBox = document.getElementById('vuceBadgeIconBox');
    badge.classList.remove('hidden');
    
    if (hs.requiereVUCE) {
        badge.className = 'p-6 rounded-2xl flex items-center gap-5 bg-orange-50 border border-orange-200 text-orange-800 fade-up';
        iconBox.className = 'w-14 h-14 rounded-xl bg-orange-100 text-orange-600 flex items-center justify-center border border-orange-200 shrink-0';
        iconBox.innerText = '!';
        document.getElementById('vuceBadgeTitle').innerText = 'Revisa permiso con ' + hs.entidadVUCE;
        document.getElementById('vuceBadgeDesc').innerText = 'Antes de comprar o embarcar, confirma si necesitas autorizacion.';
        document.getElementById('vuceBadgeLink').href = ENTIDAD_URLS[hs.entidadVUCE] || 'https://www.vuce.gob.pe';
        document.getElementById('vuceBadgeLink').innerText = 'Ver entidad';
    } else {
        badge.className = 'p-6 rounded-2xl flex items-center gap-5 bg-emerald-50 border border-emerald-200 text-emerald-800 fade-up';
        iconBox.className = 'w-14 h-14 rounded-xl bg-emerald-100 text-emerald-600 flex items-center justify-center border border-emerald-200 shrink-0';
        iconBox.innerText = 'OK';
        document.getElementById('vuceBadgeTitle').innerText = 'Sin permiso evidente';
        document.getElementById('vuceBadgeDesc').innerText = 'Con la informacion disponible, no vemos una alerta de permiso.';
        document.getElementById('vuceBadgeLink').innerText = 'Ver fuente oficial';
        document.getElementById('vuceBadgeLink').href = 'https://www.vuce.gob.pe';
    }

    const mentor = document.getElementById('mentorConsejo');
    mentor.style.opacity = '0';
    setTimeout(() => {
        if (av === 0) {
            mentor.innerHTML = '<strong>Buena señal:</strong> este producto aparece con impuesto base 0%. Igual revisa si necesitas documento de origen o permiso especial.';
        } else {
            mentor.innerHTML = '<strong>Impuesto base detectado:</strong> aparece una tasa de ' + av + '%. Compara origen y documentos antes de pagar al proveedor.';
        }
        mentor.style.opacity = '1';
    }, 300);
    document.getElementById('mentorLink').classList.remove('hidden');
}

function mostrarInfoProducto(code, desc, av, reqVuce, entVuce) {
    var panel = document.getElementById('productInfoPanel');
    var content = document.getElementById('productInfoContent');
    var tipDiv = document.getElementById('productTip');
    panel.classList.remove('hidden');

    var cap = code.length >= 2 ? parseInt(code.substring(0, 2)) : 0;
    var info = getProductInfo(cap, code, desc);

    var html = '';
    html += '<div class="info-row"><span class="info-label">Categoria</span><span class="info-value">' + info.categoria + '</span></div>';
    html += '<div class="info-row"><span class="info-label">Grupo del producto</span><span class="info-value">Grupo ' + (cap < 10 ? '0' : '') + cap + ' - ' + info.capitulo + '</span></div>';
    html += '<div class="info-row"><span class="info-label">Origenes frecuentes</span><span class="info-value">' + info.origenes + '</span></div>';
    html += '<div class="info-row"><span class="info-label">Impuestos aproximados</span><span class="info-value text-[#0A5C4A] font-bold">' + (av + 18).toFixed(1) + '%</span></div>';
    if (reqVuce) {
        html += '<div class="info-row"><span class="info-label">Quien podria revisar</span><span class="info-value text-orange-600 font-bold">' + (entVuce || 'Entidad por confirmar') + '</span></div>';
    }
    content.innerHTML = html;

    if (info.tip) {
        tipDiv.classList.remove('hidden');
        tipDiv.innerHTML = info.tip;
    } else {
        tipDiv.classList.add('hidden');
    }
}

function getProductInfo(cap, code, desc) {
    var d = desc.toUpperCase();
    if (d.indexOf('OLIVA') >= 0) return {categoria:'Aceites Comestibles',capitulo:'Grasas y aceites',origenes:'🇪🇸 España, 🇮🇹 Italia, 🇬🇷 Grecia',tip:'El aceite de oliva virgen extra tiene arancel 0% desde países UE con TLC. Exige certificado de origen EUR.1.'};
    if (d.indexOf('GIRASOL') >= 0) return {categoria:'Aceites Vegetales',capitulo:'Grasas y aceites',origenes:'🇦🇷 Argentina, 🇺🇦 Ucrania, 🇷🇺 Rusia',tip:'Argentina es el principal proveedor. Verifica requisitos fitosanitarios de SENASA.'};
    if (d.indexOf('LUBRICANT') >= 0 || d.indexOf('MOTOR') >= 0) return {categoria:'Aceites Industriales',capitulo:'Combustibles y aceites minerales',origenes:'🇺🇸 USA, 🇩🇪 Alemania, 🇯🇵 Japón',tip:'Los lubricantes no requieren VUCE pero sí ficha técnica de seguridad (MSDS).'};
    if (d.indexOf('LAPTOP') >= 0 || d.indexOf('NOTEBOOK') >= 0 || code.startsWith('8471')) return {categoria:'Equipos Informáticos',capitulo:'Máquinas y aparatos',origenes:'🇨🇳 China, 🇹🇼 Taiwán, 🇻🇳 Vietnam',tip:'Laptops con arancel 0%. Si incluyen WiFi/Bluetooth, verificar homologación MTC no requerida para uso personal.'};
    if (d.indexOf('CELULAR') >= 0 || d.indexOf('SMARTPHONE') >= 0 || code.startsWith('8517')) return {categoria:'Telecomunicaciones',capitulo:'Aparatos eléctricos de telecom',origenes:'🇨🇳 China, 🇰🇷 Corea, 🇻🇳 Vietnam',tip:'Todo equipo celular requiere homologación MTC. El trámite es gratuito vía VUCE (15 días hábiles).'};
    if (d.indexOf('MEDICAMENT') >= 0 || d.indexOf('FARMAC') >= 0 || cap === 30) return {categoria:'Productos Farmacéuticos',capitulo:'Productos farmacéuticos',origenes:'🇮🇳 India, 🇩🇪 Alemania, 🇺🇸 USA',tip:'Requiere Registro Sanitario de DIGEMID. Trámite puede tomar 90 días. Planifica con anticipación.'};
    if (d.indexOf('PERFUME') >= 0 || d.indexOf('COSMET') >= 0 || cap === 33) return {categoria:'Perfumería y Cosmética',capitulo:'Aceites esenciales y cosméticos',origenes:'🇫🇷 Francia, 🇺🇸 USA, 🇰🇷 Corea',tip:'Cosméticos requieren Notificación Sanitaria Obligatoria (NSO) de DIGESA. Ad-Valorem puede llegar al 17%.'};
    if (d.indexOf('LECHE') >= 0 || cap === 4) return {categoria:'Productos Lácteos',capitulo:'Leche y productos lácteos',origenes:'🇳🇿 N. Zelanda, 🇺🇸 USA, 🇦🇷 Argentina',tip:'La leche en polvo requiere doble certificación SENASA + DIGESA. Verifica cuota de importación vigente.'};
    if (d.indexOf('CARNE') >= 0 || cap >= 2 && cap <= 5) return {categoria:'Productos Agropecuarios',capitulo:'Carnes y despojos',origenes:'🇧🇷 Brasil, 🇦🇷 Argentina, 🇺🇸 USA',tip:'Carnes requieren certificado zoosanitario de SENASA del país de origen. Cadena de frío obligatoria.'};
    if (cap >= 61 && cap <= 63) return {categoria:'Prendas de Vestir',capitulo:'Prendas y complementos',origenes:'🇨🇳 China, 🇧🇩 Bangladesh, 🇻🇳 Vietnam',tip:'Ad-Valorem del 11%. Considera importar de China con TLC para reducir a 0%. Etiquetado obligatorio en español.'};
    // Default
    var cats = {1:'Animales vivos',2:'Carnes',3:'Pescados',4:'Lácteos',5:'Productos animales',6:'Plantas vivas',7:'Hortalizas',8:'Frutas',9:'Café y especias',10:'Cereales',
        15:'Grasas y aceites',16:'Preparaciones de carne',17:'Azúcar',18:'Cacao',19:'Preparaciones de cereales',20:'Preparaciones de vegetales',21:'Preparaciones alimenticias',22:'Bebidas',
        27:'Combustibles',28:'Productos químicos inorgánicos',29:'Productos químicos orgánicos',38:'Productos químicos diversos',39:'Plásticos',40:'Caucho',
        44:'Madera',48:'Papel',61:'Tejidos de punto',62:'Prendas',70:'Vidrio',71:'Piedras preciosas',72:'Hierro y acero',73:'Manufacturas de hierro',
        84:'Máquinas',85:'Aparatos eléctricos',87:'Vehículos',90:'Instrumentos ópticos',94:'Muebles',95:'Juguetes',96:'Manufacturas diversas'};
    return {categoria: 'Mercancía General', capitulo: cats[cap] || 'Capítulo ' + cap, origenes: '🇨🇳 China, 🇺🇸 USA, 🇩🇪 Alemania', tip:''};
}

function mostrarResultadoDirecto(hs) {
    selectedHsForWizard = {
        codigo: hs.codigo,
        descripcionEs: hs.descripcionEs,
        adValorem: parseFloat(hs.adValorem) || 0,
        requiereVuce: !!hs.requiereVuce,
        entidadVuce: hs.entidadVuce || 'SUNAT'
    };
    syncWizardReturnState();
    mostrarResultado({
        hsCode: hs.codigo,
        descripcion: hs.descripcionEs,
        adValorem: hs.adValorem,
        isc: hs.isc,
        requiereVUCE: hs.requiereVuce,
        entidadVUCE: hs.entidadVuce
    });
}

function actualizarDigito(id, val) {
    const el = document.getElementById(id);
    el.innerText = val;
    el.classList.add('digit-flip');
    setTimeout(() => el.classList.remove('digit-flip'), 400);
}

function animarTributo(textoId, barId, valor, max) {
    document.getElementById(textoId).innerText = valor + '%';
    const pct = Math.min((valor / max) * 100, 100);
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            const bar = document.getElementById(barId);
            if (bar) bar.style.width = (pct || 0) + '%';
        });
    });
}

function mostrarError(msg) {
    const desc = document.getElementById('hsDescripcion');
    desc.innerText = msg;
    desc.className = 'text-sm text-rose-500 font-bold';
}

function setBtnLoading(loading) {
    const btn = document.getElementById('btnBuscar');
    const skeleton = document.getElementById('loadingSkeleton');
    const results = document.getElementById('mainResults');
    btn.disabled = loading;
    btn.innerHTML = loading ? 'Buscando...' : 'Buscar codigo';
    
    if (loading) {
        skeleton.classList.remove('hidden');
        results.classList.add('hidden');
    } else {
        skeleton.classList.add('hidden');
        results.classList.remove('hidden');
    }
}

function copiarCodigo() {
    if (!codigoActual) return;
    navigator.clipboard.writeText(codigoActual).then(() => {
        const btn = document.querySelector('button[onclick="copiarCodigo()"]');
        const old = btn.innerHTML;
        btn.innerText = 'Copiado ✓';
        btn.style.color = '#0A5C4A';
        setTimeout(() => { btn.innerHTML = old; btn.style.color = ''; }, 2000);
    });
}

// ============================================================
// AduanaBot conversational chatbot system (Proposal 10)
// ============================================================

const ADUANABOT_KEYWORD_MAP = [
    {
        keywords: ['celular', 'telefono', 'smartphone', 'iphone', 'samsung', 'movil', 'huawei', 'xiaomi'],
        code: '8517130000',
        desc: 'Teléfonos móviles (smartphones)',
        taxes: 'Ad-Valorem: 0%, IGV: 16%, IPM: 2%',
        vuce: 'Restringido por MTC (Requiere Homologación de Equipos y Aparatos de Telecomunicaciones para ingreso comercial, o declaración jurada simplificada para uso personal de hasta 3 unidades).',
        tips: 'Los celulares tienen arancel 0% de forma global. El trámite de homologación ante el MTC se realiza gratis por internet a través del portal de la VUCE.'
    },
    {
        keywords: ['laptop', 'computadora', 'notebook', 'pc', 'ordenador', 'lenovo', 'tablet', 'macbook', 'asus', 'hp'],
        code: '8471300000',
        desc: 'Laptops y computadoras portátiles',
        taxes: 'Ad-Valorem: 0%, IGV: 16%, IPM: 2%',
        vuce: 'Libre (No requiere VUCE para importación convencional).',
        tips: 'Las laptops gozan de arancel 0% Ad-Valorem permanente. Verifica que el fabricante incluya descripciones mínimas detallando marca, modelo, procesador y número de serie en la factura.'
    },
    {
        keywords: ['perfume', 'cosmetico', 'maquillaje', 'labial', 'crema', 'shampoo', 'colonia', 'fragancia', 'delineador'],
        code: '3304990000',
        desc: 'Preparaciones de belleza, maquillaje y cuidado de la piel (cosméticos)',
        taxes: 'Ad-Valorem: 6%, IGV: 16%, IPM: 2%',
        vuce: 'Restringido por DIGEMID (Requiere Notificación Sanitaria Obligatoria - NSO previa a la importación).',
        tips: '¡Atención! La importación de cosméticos sin NSO es incautada. Si es para uso comercial, necesitas un Director Técnico Químico-Farmacéutico registrado y almacén autorizado.'
    },
    {
        keywords: ['vino', 'alcohol', 'licor', 'cerveza', 'pisco', 'ron', 'whisky', 'trago'],
        code: '2204210000',
        desc: 'Vino de uvas frescas en envases de hasta 2 litros',
        taxes: 'Ad-Valorem: 6%, IGV: 16%, IPM: 2%, ISC: 25% (Impuesto Selectivo al Consumo)',
        vuce: 'Restringido por DIGESA (Requiere Registro Sanitario de Alimentos y Bebidas de Consumo Humano).',
        tips: 'Las bebidas alcohólicas tributan ISC. Asegúrate de que las botellas cuenten con el rotulado reglamentario y registro de lote para evitar el rechazo en el aforo físico.'
    },
    {
        keywords: ['polo', 'camiseta', 'ropa', 'vestido', 'pantalon', 'abrigo', 'casaca', 'jean', 'sueter', 'textil', 'prenda', 'algodon'],
        code: '6109100000',
        desc: 'Camisetas (t-shirts) de punto, de algodón',
        taxes: 'Ad-Valorem: 11%, IGV: 16%, IPM: 2%',
        vuce: 'Libre (Sujeto a normas de etiquetado de confecciones, Ley 28405).',
        tips: 'La ropa tiene una tasa alta de Ad-Valorem (11%). Si importas de EE.UU. o China, puedes acogerte al beneficio del TLC para exonerar el 100% de aranceles presentando un Certificado de Origen válido.'
    },
    {
        keywords: ['pastilla', 'medicamento', 'jarabe', 'remedio', 'medicina', 'farmaco', 'vacuna', 'paracetamol', 'aspirina'],
        code: '3004902900',
        desc: 'Medicamentos acondicionados para la venta al por menor',
        taxes: 'Ad-Valorem: 0%, IGV: 16%, IPM: 2%',
        vuce: 'Restringido por DIGEMID (Requiere Registro Sanitario y autorización excepcional para droguerías).',
        tips: 'Los medicamentos esenciales están exentos de aranceles (0%). La importación para uso personal requiere receta médica visada por la DIGEMID en un trámite simplificado.'
    },
    {
        keywords: ['cafe', 'semilla', 'grano', 'planta', 'platano', 'manzana', 'fruta', 'verdura', 'agricola'],
        code: '0901110000',
        desc: 'Café sin tostar, sin descafeinar',
        taxes: 'Ad-Valorem: 0%, IGV: 16%, IPM: 2%',
        vuce: 'Restringido por SENASA (Requiere Certificado Fitosanitario de Importación e inspección en punto de ingreso).',
        tips: 'SENASA protege el ecosistema agrario peruano. Debes tramitar el Permiso Fitosanitario de Importación (PFI) antes del embarque de la mercancía.'
    },
    {
        keywords: ['juguete', 'muñeca', 'lego', 'rompecabezas', 'pelota', 'consola', 'playstation', 'nintendo', 'xbox'],
        code: '9503000000',
        desc: 'Juguetes y modelos recreativos',
        taxes: 'Ad-Valorem: 6%, IGV: 16%, IPM: 2%',
        vuce: 'Restringido por DIGESA (Requiere Registro de Importador de Juguetes y Autorización Sanitaria de Inocuidad).',
        tips: 'Las consolas de videojuegos no tienen restricción. Sin embargo, los juguetes físicos sí la tienen por ley contra metales pesados y plomo. Tramita la autorización sanitaria con DIGESA.'
    },
    {
        keywords: ['repuesto', 'freno', 'embrague', 'carro', 'auto', 'amortiguador', 'parachoques', 'autoparte', 'llanta'],
        code: '8708999090',
        desc: 'Partes y accesorios de vehículos automotores',
        taxes: 'Ad-Valorem: 6%, IGV: 16%, IPM: 2%',
        vuce: 'Libre.',
        tips: 'Los repuestos no requieren permisos sanitarios ni de telecomunicaciones. Asegúrate de detallar si son partes nuevas u usadas, ya que la importación de autopartes usadas está muy regulada en Perú.'
    }
];

function initAduanaBot() {
    const trigger = document.getElementById('aduanabot-trigger');
    const panel = document.getElementById('aduanabot-panel');
    const closeBtn = document.getElementById('aduanabot-close');
    const form = document.getElementById('aduanabot-form');
    const input = document.getElementById('aduanabot-input');

    if (!trigger || !panel) return;

    trigger.addEventListener('click', () => {
        panel.classList.toggle('hidden');
        if (!panel.classList.contains('hidden') && input) {
            input.focus();
        }
    });

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            panel.classList.add('hidden');
        });
    }

    if (form) {
        form.addEventListener('submit', handleAduanaBotSubmit);
    }
}

async function handleAduanaBotSubmit(e) {
    e.preventDefault();
    const input = document.getElementById('aduanabot-input');
    const query = (input.value || '').trim();
    if (!query) return;

    // Clear input
    input.value = '';

    // Append User Message
    appendAduanaBotMessage('user', escapeHtml(query));

    // Show Typing Indicator
    showTypingIndicator();

    // Small delay to feel realistic and "agentic"
    setTimeout(async () => {
        const queryLower = query.toLowerCase();

        // 1. Check local Peruvian customs synonyms mapping first
        let matched = null;
        for (const item of ADUANABOT_KEYWORD_MAP) {
            if (item.keywords.some(keyword => queryLower.includes(keyword))) {
                matched = item;
                break;
            }
        }

        if (matched) {
            let responseHtml = `<strong>Encontre una coincidencia probable:</strong><br><br>`;
            responseHtml += `<strong>Codigo recomendado:</strong> <code>${matched.code}</code> (${matched.desc})<br>`;
            responseHtml += `<strong>Impuestos base:</strong> ${matched.taxes}<br>`;
            responseHtml += `<strong>Permisos:</strong> ${matched.vuce}<br><br>`;
            responseHtml += `<strong>Consejo:</strong> ${matched.tips}`;

            removeTypingIndicator();
            appendAduanaBotMessage('bot', responseHtml, { code: matched.code, label: 'Usar este codigo' });
            return;
        }

        // 2. Query dynamic J2EE suggestions API as a second layer with Cache and AbortController
        try {
            // Check in suggestions cache first
            let results = null;
            if (SUGGESTIONS_CACHE.has(queryLower)) {
                results = SUGGESTIONS_CACHE.get(queryLower);
            } else {
                if (searchAbortController) {
                    searchAbortController.abort();
                }
                searchAbortController = new AbortController();

                const res = await fetch(window.ctx + '/api/hs/sugerencias?termino=' + encodeURIComponent(query), {
                    signal: searchAbortController.signal
                });
                if (res.ok) {
                    results = await res.json();
                    if (Array.isArray(results)) {
                        SUGGESTIONS_CACHE.set(queryLower, results);
                    }
                }
            }

            if (Array.isArray(results) && results.length > 0) {
                const topMatch = results[0];
                const code = topMatch.codigo || topMatch.hsCode;
                const desc = topMatch.descripcionEs || topMatch.descripcion;
                const requiresVuce = !!topMatch.requiereVuce;
                const vuceEnt = topMatch.entidadVuce || 'SUNAT';
                const adVal = parseFloat(topMatch.adValorem) || 0;

                let responseHtml = `<strong>Coincidencia probable encontrada:</strong><br><br>`;
                responseHtml += `<strong>Codigo sugerido:</strong> <code>${escapeHtml(code)}</code><br>`;
                responseHtml += `<strong>Descripcion:</strong> <em>${escapeHtml(desc)}</em><br>`;
                responseHtml += `<strong>Impuesto base:</strong> ${adVal}% | <strong>IGV/IPM:</strong> 18%<br>`;
                responseHtml += `<strong>Permisos:</strong> ${requiresVuce ? 'revisar con ' + escapeHtml(vuceEnt) + ' antes de embarcar.' : 'sin alerta evidente con la informacion disponible.'}`;

                removeTypingIndicator();
                appendAduanaBotMessage('bot', responseHtml, { code: code, label: 'Usar este codigo' });
                return;
            }
        } catch (e) {
            if (e.name !== 'AbortError') {
                // Silencioso, continúa al fallback
            } else {
                return; // Fue abortado por una búsqueda más nueva, salimos para no inyectar burbujas viejas
            }
        }

        // 3. Fallback message if no match found
        removeTypingIndicator();
        let fallbackText = `No he podido encontrar una subpartida exacta para <em>"${escapeHtml(query)}"</em>.<br><br>`;
        fallbackText += `Prueba con mas detalle: marca, material, uso o presentacion. Ejemplos: <strong>"perfumes de marca"</strong>, <strong>"laptops portatiles"</strong>, <strong>"celulares"</strong> o <strong>"ropa de algodon"</strong>.`;
        appendAduanaBotMessage('bot', fallbackText);
    }, 800);
}

function appendAduanaBotMessage(sender, text, buttonConfig = null) {
    const box = document.getElementById('aduanabot-messages');
    if (!box) return;

    const row = document.createElement('div');
    row.className = 'flex gap-2 animate-scaleIn';

    let avatarHtml = '';
    let bubbleClass = '';
    if (sender === 'bot') {
        avatarHtml = '<div class="w-7 h-7 rounded-lg bg-blue-100 text-blue-600 flex items-center justify-center text-xs shrink-0 font-bold">🤖</div>';
        bubbleClass = 'bg-white text-[var(--text-secondary)] border border-[var(--border)] shadow-sm';
    } else {
        avatarHtml = '<div class="w-7 h-7 rounded-lg bg-blue-600 text-white flex items-center justify-center text-xs shrink-0 font-bold ml-auto">👤</div>';
        bubbleClass = 'bg-blue-600 text-white ml-auto';
        row.className += ' justify-end';
    }

    let buttonHtml = '';
    if (buttonConfig) {
        const safeCode = encodeURIComponent(buttonConfig.code);
        buttonHtml = `<button data-bot-code="${safeCode}" class="bot-search-btn mt-3 px-3 py-2 bg-blue-50 hover:bg-blue-100 text-blue-600 text-[10px] font-black uppercase tracking-wider rounded-xl border border-blue-200 active:scale-95 transition-all block w-full text-center cursor-pointer">
            ${escapeHtml(buttonConfig.label)}
        </button>`;
    }

    row.innerHTML = `
        ${sender === 'bot' ? avatarHtml : ''}
        <div class="px-4 py-3 rounded-2xl max-w-[80%] leading-relaxed ${bubbleClass}">
            ${text}
            ${buttonHtml}
        </div>
        ${sender === 'user' ? avatarHtml : ''}
    `;
    box.appendChild(row);
    box.scrollTop = box.scrollHeight;
}

function showTypingIndicator() {
    const box = document.getElementById('aduanabot-messages');
    if (!box) return;

    const indicator = document.createElement('div');
    indicator.id = 'aduanabot-typing';
    indicator.className = 'flex gap-2 items-center animate-pulse text-[10px] text-gray-400 font-bold font-mono pl-9';
    indicator.innerHTML = `<span>🤖</span> <span>AduanaBot está analizando...</span>`;
    box.appendChild(indicator);
    box.scrollTop = box.scrollHeight;
}

function removeTypingIndicator() {
    const indicator = document.getElementById('aduanabot-typing');
    if (indicator) indicator.remove();
}

window.triggerBotSearch = function(code) {
    const panel = document.getElementById('aduanabot-panel');
    if (panel) panel.classList.add('hidden');
    const searchInput = document.getElementById('hsSearch');
    if (searchInput) {
        searchInput.value = code;
        buscar();
    }
}

function escapeHtml(value) {
    return String(value || '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

// Bot search button event delegation (replaces inline onclick)
document.addEventListener('click', function(e) {
    const btn = e.target.closest('.bot-search-btn');
    if (btn) {
        const code = decodeURIComponent(btn.getAttribute('data-bot-code') || '');
        if (code) window.triggerBotSearch(code);
    }
});

// Bootstrap J2EE events
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        initBuscadorEvents();
        initAduanaBot();
    });
} else {
    initBuscadorEvents();
    initAduanaBot();
}

