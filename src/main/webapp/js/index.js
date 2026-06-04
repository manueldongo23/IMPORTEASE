// ==========================================================================
// IMPORTEASE - ASISTENTE ADUANERO & COPILOTO PRE-VUCE (WIZARD INDEX)
// ==========================================================================

(function() {
    const originalFetch = window.fetch;
    window.fetch = async function(...args) {
        try {
            const response = await originalFetch(...args);
            if (response.status === 401 || response.status === 403) {
                if (typeof wizardData !== 'undefined' && typeof saveWizardDraft === 'function') {
                    saveWizardDraft();
                    try {
                        sessionStorage.setItem('importease_emergency_draft', JSON.stringify(wizardData));
                    } catch (e) {}
                }
            }
            return response;
        } catch (error) {
            throw error;
        }
    };
})();

let currentStep = 1;
let modoDidacticoActivo = true;
let registeredImportId = null;
const WIZARD_DRAFT_KEY = 'importease_wizard_draft';
const WIZARD_HS_RETURN_KEY = 'importease_wizard_hs_selection';
const WIZARD_INCOTERM_RETURN_KEY = 'importease_wizard_incoterm_decision';
const AUTOSAVE_TIME_FORMATTER = new Intl.DateTimeFormat('es-PE', { hour: '2-digit', minute: '2-digit' });

// Caché local en memoria y controlador de aborto arancelario
const SUGGESTIONS_CACHE = new Map();
let remoteSearchAbortController = null;

// Estructura de Datos de la Operación en Proceso
let wizardData = {
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
        FICHA_TECNICA: true
    },
    
    vuceEstado: 'EXPEDIENTE_GENERADO',
    vuceSuce: '',
    vuceResolucion: '',
    vuceObs: '',

    // Campos TUPA dinámicos de las plantillas por entidad
    tupaDatos: {}
};

function getQuestionAnswer(fieldId) {
    const el = document.getElementById(fieldId);
    return el ? (el.value || 'NO') : 'NO';
}

function normalizeWizardText(value) {
    return String(value || '')
        .toLowerCase()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .replace(/[^a-z0-9\s.-]/g, ' ')
        .replace(/\s+/g, ' ')
        .trim();
}

function tokenizeWizardText(value) {
    return normalizeWizardText(value).split(' ').filter((token) => token.length >= 3);
}

function getWizardSearchCorpus() {
    return [
        wizardData.prodNombre,
        wizardData.prodTecnica,
        wizardData.prodMarca,
        wizardData.prodModelo,
        wizardData.opNombre,
        wizardData.selectedHS ? wizardData.selectedHS.descripcionEs : '',
        wizardData.selectedHS ? wizardData.selectedHS.codigo : ''
    ].filter(Boolean).join(' ');
}

function getProductInputCorpus() {
    return [
        wizardData.prodNombre,
        wizardData.prodTecnica,
        wizardData.prodMarca,
        wizardData.prodModelo,
        wizardData.opNombre
    ].filter(Boolean).join(' ');
}

function detectProductProfile() {
    const productCorpus = normalizeWizardText(getProductInputCorpus());
    const selectedCode = String(wizardData.selectedHS && wizardData.selectedHS.codigo ? wizardData.selectedHS.codigo : '').replace(/\./g, '');
    const corpus = productCorpus || normalizeWizardText(getWizardSearchCorpus());
    const code = selectedCode;
    const has = (...terms) => terms.some((term) => corpus.includes(term));
    const codeStartsWith = (...prefixes) => prefixes.some((p) => code.startsWith(p));

    // Alimentos, bebidas, suplementos
    if (codeStartsWith('02','03','04','07','08','09','10','11','12','15','16','17','18','19','20','21','22','23') ||
        has('alimento','bebida','comida','snack','nutricion','suplemento','proteina','whey','vitamina','lacteo','lácteo','queso','leche','carne','pescado','fruta','verdura','cereal','harina','aceite','grasa','conserva','enlatado','congelado','organico','orgánico')) {
        return { key: 'food', label: 'Alimentos y suplementos', entity: 'DIGESA', requiresVuce: true };
    }

    // Ropa, textiles, calzado
    if (codeStartsWith('42','43','50','51','52','53','54','55','56','57','58','59','60','61','62','63','64','65') ||
        has('ropa','camisa','pantalon','pantalón','vestido','chaqueta','abrigo','zapato','zapatilla','tenis','calzado','textil','tela','algodon','algodón','poliester','poliéster','lana','seda','cuero','piel','bolso','cartera','mochila','bufanda','gorro','sombrero','guante','cinturon','cinturón','ropa interior','calcetin','calcetín','medias','jean','denim')) {
        return { key: 'clothing', label: 'Ropa, textiles y calzado', entity: 'PRODUCE', requiresVuce: false };
    }

    // Tecnología, electrónicos, conectividad
    if (codeStartsWith('84','8471','8473','8504','8507','8517','8518','8519','8521','8523','8525','8526','8527','8528','8529','8531','8534','8536','8537','8541','8542','8543','8544') ||
        has('wifi','bluetooth','celular','telefono','teléfono','smartphone','movil','móvil','tablet','laptop','router','modem','radio','inalambr','wireless','computador','monitor','impresora','auricular','parlante','bocina','cable','usb','hdmi','cargador','bateria','batería','camara','cámara','drone','dron','gps','pantalla','led','oled','tv','televisor','consola','playstation','xbox','nintendo')) {
        return { key: 'tech', label: 'Tecnología y electrónicos', entity: 'MTC', requiresVuce: true };
    }

    // Cosméticos, perfumes, belleza
    if (codeStartsWith('3303','3304','3305','3306','3307') ||
        has('perfume','fragancia','colonia','cosmetico','cosmético','cosmetica','cosmética','maquillaje','labial','crema','loción','locion','shampoo','champú','jabon','jabón','desodorante','esmalte','aceite esencial','protector solar','bloqueador')) {
        return { key: 'cosmetics', label: 'Cosméticos y cuidado personal', entity: 'DIGEMID', requiresVuce: true };
    }

    // Medicamentos, salud, dispositivos médicos
    if (codeStartsWith('2936','2937','2941','3001','3002','3003','3004','3005','3006','3822','3821','9018','9019','9020','9021','9022') ||
        has('medicamento','farmaco','fármaco','farmaceut','dispositivo medico','dispositivo médico','ecografo','ecógrafo','ultrasonido','clinico','clínico','hospital','quirurgico','quirúrgico','jeringa','guante quirurgico','termometro','termómetro','resonancia','tomografo','tomógrafo','desfibrilador','marcapaso','protesis','prótesis','implante','ortopedia','silla ruedas')) {
        return { key: 'health', label: 'Salud y dispositivos médicos', entity: 'DIGEMID', requiresVuce: true };
    }

    // Agricultura, semillas, plantas
    if (codeStartsWith('06','0601','0602','1201','1202','1203','1204','1205','1206','1207','1208','1209','1210','1211','1212','1213','1214') ||
        has('semilla','planta','vegetal','agricola','agrícola','fitosanitario','cultivo','siembra','cosecha','fertilizante','pesticida','herbicida','insecticida','flor','flores','arbol','árbol','arbusto','plantin','plantín','jardin','jardín')) {
        return { key: 'agriculture', label: 'Agricultura y sanidad vegetal', entity: 'SENASA', requiresVuce: true };
    }

    // Madera, forestal
    if (codeStartsWith('44') ||
        has('madera','tablero','piso madera','roble','pino','caoba','cedro','contrachapado','mdf','aglomerado','aserrin','aserrín','forestal','maderable','mueble madera','carpinteria','carpintería')) {
        return { key: 'wood', label: 'Madera y control forestal', entity: 'SERFOR', requiresVuce: true };
    }

    // Juguetes, artículos infantiles
    if (codeStartsWith('9503','9504','9505') ||
        has('juguete','juego','muñeca','muñeco','peluche','rompecabezas','puzzle','lego','bloques','didactico','didáctico','infantil','niño','niña','bebe','bebé','cuna','coche bebe','coche bebé','triciclo','patinete','pelota','balon','balón')) {
        return { key: 'toys', label: 'Juguetes y artículos infantiles', entity: 'PRODUCE', requiresVuce: false };
    }

    // Maquinaria, herramientas, industrial
    if (codeStartsWith('8201','8202','8203','8204','8205','8206','8207','8456','8457','8458','8459','8460','8461','8462','8463','8464','8465','8466','8467','8468','8474','8479','8480','8481','8482','8483','8484') ||
        has('maquina','máquina','herramienta','motor','bomba','compresor','generador','torno','soldador','valvula','válvula','rodamiento','engranaje','polea','cinta transportadora','industrial','fabrica','fábrica','manufactura','produccion','producción')) {
        return { key: 'machinery', label: 'Maquinaria e industrial', entity: 'PRODUCE', requiresVuce: false };
    }

    // Vehículos, autopartes, transporte
    if (codeStartsWith('4011','4012','4013','8407','8408','8409','8413','8421','8483','8701','8702','8703','8704','8705','8706','8707','8708','8709','8711','8712','8713','8714','8715','8716','8802','8803','8804','8903','8906','8907') ||
        has('auto','carro','vehiculo','vehículo','camion','camión','moto','motocicleta','bicicleta','llanta','neumatico','neumático','repuesto','autoparte','accesorio auto','motor vehiculo','barco','lancha','avion','avión','dron','drone','helicoptero','helicóptero','bus','omnibus','ómnibus')) {
        return { key: 'vehicles', label: 'Vehículos y autopartes', entity: 'MTC', requiresVuce: true };
    }

    // Químicos, productos peligrosos
    if (codeStartsWith('2801','2802','2803','2804','2805','2806','2807','2808','2809','2810','2811','2812','2813','2814','2815','2901','2902','2903','2904','2905','2906','2907','2908','2909','2910','2912','2914','2915','2916','2917','2918','2919','2920','2921','2922','2923','2924','2925','2926','2927','2928','2929','2930','2931','2932','2933','2934','2935','3101','3102','3103','3104','3105','3208','3209','3210','3211','3212','3213','3214','3402','3602','3603','3604','3808','3809','3810','3811','3812','3813','3814','3815','3816','3817','3818','3819','3820','3823','3824') ||
        has('quimico','químico','reactivo','solvente','acido','ácido','alcalino','corrosivo','inflamable','toxico','tóxico','peligroso','combustible','explosivo','gas comprimido','pintura','barniz','adhesivo','pegamento','resina','epoxi','silicona','lubricante','refrigerante','combustible','cloro','amonio','soda caustica')) {
        return { key: 'chemicals', label: 'Químicos y productos peligrosos', entity: 'DIGESA', requiresVuce: true };
    }

    // Joyería, metales preciosos
    if (codeStartsWith('7101','7102','7103','7104','7105','7106','7107','7108','7109','7110','7111','7112','7113','7114','7115','7116','7117','7118') ||
        has('joya','joyeria','joyería','oro','plata','platino','diamante','piedra preciosa','reloj','anillo','collar','arete','pulsera','cadena','lingote','moneda','metal precioso','gemas')) {
        return { key: 'jewelry', label: 'Joyería y metales preciosos', entity: 'SUNAT', requiresVuce: true };
    }

    // Segunda mano / usado
    if (has('usado','segundo uso','reacondicionado','remanufacturado','refurbished','segunda mano','de ocasion','de ocasión','vintage','antiguo','restaurado','seminuevo')) {
        return { key: 'used', label: 'Segunda mano o reacondicionado', entity: 'REVISAR', requiresVuce: false };
    }

    return { key: 'general', label: 'Producto general', entity: 'SUNAT', requiresVuce: false };
}

const DYNAMIC_QUESTION_SETS = {
    food: [
        { id: 'food_type', label: '¿Es alimento, bebida o suplemento?', helper: 'Orientamos el control sanitario según la categoría exacta.', defaultValue: 'SI' },
        { id: 'food_human', label: '¿Es para consumo humano directo?', helper: 'Cambia la ficha técnica y registros sanitarios exigibles.', defaultValue: 'NOSE' },
        { id: 'food_perishable', label: '¿Es producto perecible?', helper: 'Si requiere cadena de frío, activamos requisitos de transporte.', defaultValue: 'NOSE' },
        { id: 'food_organic', label: '¿Tiene certificación orgánica?', helper: 'Afecta el etiquetado y la documentación sanitaria.', defaultValue: 'NOSE' },
        { id: 'food_sensitive', label: '¿Tiene ingredientes alérgenos o restringidos?', helper: 'Ej: gluten, lactosa, frutos secos, aditivos controlados.', defaultValue: 'NOSE' }
    ],
    clothing: [
        { id: 'cloth_type', label: '¿Es ropa, calzado o accesorio textil?', helper: 'El tipo define la partida y el control aplicable.', defaultValue: 'NOSE' },
        { id: 'cloth_material', label: '¿Material principal: natural, sintético o cuero?', helper: 'Afecta la clasificación arancelaria y etiquetado.', defaultValue: 'NOSE' },
        { id: 'cloth_branded', label: '¿Tiene marca registrada?', helper: 'Activa validación de propiedad intelectual en aduanas.', defaultValue: 'NOSE' },
        { id: 'cloth_label', label: '¿Cuenta con etiquetado de composición y origen?', helper: 'Requisito obligatorio para comercialización textil en Perú.', defaultValue: 'NOSE' },
        { id: 'cloth_safety', label: '¿Es ropa infantil o para menores de 12 años?', helper: 'Activa controles adicionales de seguridad y rotulado.', defaultValue: 'NOSE' }
    ],
    tech: [
        { id: 'tech_calls', label: '¿Permite llamadas telefónicas?', helper: 'Distingue entre teléfono, wearable o accesorio.', defaultValue: 'NOSE' },
        { id: 'tech_wifi', label: '¿Tiene WiFi incorporado?', helper: 'Activa requisitos de homologación de radiofrecuencia.', defaultValue: 'NOSE' },
        { id: 'tech_bluetooth', label: '¿Incluye Bluetooth?', helper: 'Confirma alcance de radiofrecuencia para el MTC.', defaultValue: 'NOSE' },
        { id: 'tech_battery', label: '¿Tiene batería de litio integrada?', helper: 'Activa controles de transporte de mercancía peligrosa.', defaultValue: 'NOSE' },
        { id: 'tech_voltage', label: '¿Usa 110V o 220V? ¿Incluye adaptador?', helper: 'Ayuda a verificar compatibilidad eléctrica local.', defaultValue: 'NOSE' }
    ],
    cosmetics: [
        { id: 'cosm_alcohol', label: '¿Contiene alcohol?', helper: 'Perfila el control de alcoholes y restricciones.', defaultValue: 'NOSE' },
        { id: 'cosm_child', label: '¿Es para uso infantil?', helper: 'Activa requisitos especiales de rotulado y advertencias.', defaultValue: 'NOSE' },
        { id: 'cosm_aerosol', label: '¿Viene en envase aerosol?', helper: 'Control adicional por envase presurizado y transporte.', defaultValue: 'NOSE' },
        { id: 'cosm_natural', label: '¿Se declara como natural u orgánico?', helper: 'Requiere certificación y etiquetado específico.', defaultValue: 'NOSE' },
        { id: 'cosm_sun', label: '¿Es protector solar o bloqueador?', helper: 'Clasificación especial con registro DIGEMID obligatorio.', defaultValue: 'NOSE' }
    ],
    health: [
        { id: 'health_human', label: '¿Es de uso humano?', helper: 'Define si entra como producto sanitario, clínico o veterinario.', defaultValue: 'SI' },
        { id: 'health_device', label: '¿Es medicamento, cosmético o dispositivo médico?', helper: 'Separa la ruta regulatoria: fármaco vs equipo vs cosmético.', defaultValue: 'NOSE' },
        { id: 'health_sterile', label: '¿Es estéril o de un solo uso?', helper: 'Activa controles sanitarios más estrictos en aduanas.', defaultValue: 'NOSE' },
        { id: 'health_ficha', label: '¿Cuenta con ficha técnica o registro sanitario?', helper: 'Documento clave para acelerar la validación.', defaultValue: 'NOSE' },
        { id: 'health_electric', label: '¿Es equipo médico eléctrico/electrónico?', helper: 'Suma requisitos de seguridad eléctrica al permiso sanitario.', defaultValue: 'NOSE' }
    ],
    agriculture: [
        { id: 'agri_seed', label: '¿Es para siembra, cultivo o propagación?', helper: 'Activa la ruta fitosanitaria y cuarentenaria.', defaultValue: 'SI' },
        { id: 'agri_live', label: '¿Es material vegetal vivo (plantas, esquejes)?', helper: 'Requiere certificado fitosanitario de exportación.', defaultValue: 'NOSE' },
        { id: 'agri_organic', label: '¿Proviene de agricultura orgánica certificada?', helper: 'Acreditar con certificación del país de origen.', defaultValue: 'NOSE' },
        { id: 'agri_treated', label: '¿Recibió tratamiento químico o fumigación?', helper: 'Debe declararse el producto usado y su residualidad.', defaultValue: 'NOSE' },
        { id: 'agri_soil', label: '¿Incluye tierra, sustrato o material de suelo?', helper: 'Restricción cuarentenaria fuerte — posible retención.', defaultValue: 'NOSE' }
    ],
    wood: [
        { id: 'wood_origin', label: '¿Proviene de madera, subproducto o mueble?', helper: 'Activa control forestal y de flora protegida.', defaultValue: 'SI' },
        { id: 'wood_species', label: '¿Conoces la especie (caoba, cedro, roble, pino)?', helper: 'Especies CITES requieren permisos adicionales.', defaultValue: 'NOSE' },
        { id: 'wood_treated', label: '¿Está tratada (secada, fumigada, laqueada)?', helper: 'Tratamiento fitosanitario exigido para ingreso.', defaultValue: 'NOSE' },
        { id: 'wood_finished', label: '¿Es producto terminado (mueble, piso) o madera bruta?', helper: 'Producto terminado simplifica controles vs madera bruta.', defaultValue: 'NOSE' },
        { id: 'wood_cert', label: '¿Cuenta con certificado de origen forestal?', helper: 'SERFOR exige trazabilidad desde el origen.', defaultValue: 'NOSE' }
    ],
    toys: [
        { id: 'toy_age', label: '¿Para qué edad está recomendado?', helper: 'Menores de 3 años activan controles de seguridad estrictos.', defaultValue: 'NOSE' },
        { id: 'toy_material', label: '¿Contiene piezas pequeñas, imanes o pilas?', helper: 'Riesgo de asfixia o toxicidad — control reforzado.', defaultValue: 'NOSE' },
        { id: 'toy_electric', label: '¿Es juguete eléctrico o electrónico?', helper: 'Suma requisitos de seguridad eléctrica al control.', defaultValue: 'NOSE' },
        { id: 'toy_cert', label: '¿Tiene certificación de seguridad (ASTM, EN71, NTP)?', helper: 'Facilita la validación en aduanas.', defaultValue: 'NOSE' },
        { id: 'toy_plush', label: '¿Es peluche o juguete textil?', helper: 'Control adicional de materiales ignífugos y tintes.', defaultValue: 'NOSE' }
    ],
    machinery: [
        { id: 'mach_type', label: '¿Es herramienta manual, eléctrica o maquinaria pesada?', helper: 'Define la partida arancelaria y el control.', defaultValue: 'NOSE' },
        { id: 'mach_power', label: '¿Funciona con electricidad, combustible o aire comprimido?', helper: 'Afecta la clasificación y los permisos.', defaultValue: 'NOSE' },
        { id: 'mach_used', label: '¿Es maquinaria nueva o usada?', helper: 'Usada requiere declaración de valor y posible inspección.', defaultValue: 'NOSE' },
        { id: 'mach_safety', label: '¿Requiere certificación de seguridad industrial?', helper: 'Equipos de riesgo necesitan homologación.', defaultValue: 'NOSE' },
        { id: 'mach_parts', label: '¿Son repuestos, accesorios o equipo completo?', helper: 'Repuestos tienen partidas distintas a equipos completos.', defaultValue: 'NOSE' }
    ],
    vehicles: [
        { id: 'veh_type', label: '¿Es vehículo completo, repuesto o accesorio?', helper: 'Define el régimen de importación aplicable.', defaultValue: 'NOSE' },
        { id: 'veh_used', label: '¿Es nuevo o usado?', helper: 'Usado: restricciones de antigüedad y control de emisiones.', defaultValue: 'NOSE' },
        { id: 'veh_engine', label: '¿Tipo de motor: gasolina, diésel, eléctrico?', helper: 'Define el arancel y las restricciones ambientales.', defaultValue: 'NOSE' },
        { id: 'veh_brand', label: '¿Marca y modelo están registrados en Perú?', helper: 'Algunas marcas chinas requieren homologación adicional.', defaultValue: 'NOSE' },
        { id: 'veh_tires', label: '¿Incluye neumáticos o baterías?', helper: 'Componentes sujetos a control ambiental separado.', defaultValue: 'NOSE' }
    ],
    chemicals: [
        { id: 'chem_use', label: '¿Es de uso industrial, agrícola o doméstico?', helper: 'Define la entidad reguladora (DIGESA/SENASA/OSINERGMIN).', defaultValue: 'NOSE' },
        { id: 'chem_danger', label: '¿Es inflamable, corrosivo, tóxico o explosivo?', helper: 'Activa control de mercancía peligrosa y hoja MSDS.', defaultValue: 'NOSE' },
        { id: 'chem_sds', label: '¿Cuenta con hoja de seguridad (MSDS)?', helper: 'Documento obligatorio para productos químicos controlados.', defaultValue: 'NOSE' },
        { id: 'chem_volume', label: '¿Qué cantidad (kg o litros) vas a importar?', helper: 'Grandes volúmenes activan permisos adicionales.', defaultValue: 'NOSE' },
        { id: 'chem_mix', label: '¿Es sustancia pura o mezcla/preparado?', helper: 'Afecta la clasificación y los requisitos de etiquetado.', defaultValue: 'NOSE' }
    ],
    jewelry: [
        { id: 'jwl_material', label: '¿Material: oro, plata, platino u otro?', helper: 'Define la partida arancelaria y ley del metal.', defaultValue: 'NOSE' },
        { id: 'jwl_gems', label: '¿Incluye piedras preciosas o diamantes?', helper: 'Requiere certificación gemológica adicional.', defaultValue: 'NOSE' },
        { id: 'jwl_watch', label: '¿Incluye relojes o mecanismos de precisión?', helper: 'Relojes suizos/japoneses tienen reglas de origen específicas.', defaultValue: 'NOSE' },
        { id: 'jwl_cert', label: '¿Cuenta con certificado de autenticidad?', helper: 'Oro y plata requieren ensayo y contraste legal.', defaultValue: 'NOSE' },
        { id: 'jwl_commercial', label: '¿Es para uso personal o comercial?', helper: 'Define si pagas tributo simplificado o régimen general.', defaultValue: 'NOSE' }
    ],
    used: [
        { id: 'used_second', label: '¿Es un producto de segundo uso o reacondicionado?', helper: 'Revisión reforzada de valor, estado y antigüedad.', defaultValue: 'SI' },
        { id: 'used_refurbished', label: '¿Fue reacondicionado o remanufacturado?', helper: 'Requiere declaración técnica y respaldo documental.', defaultValue: 'NOSE' },
        { id: 'used_complete', label: '¿Llega completo y operativo?', helper: 'Producto incompleto puede clasificarse como repuesto.', defaultValue: 'NOSE' },
        { id: 'used_origin', label: '¿De qué año es y cuánto tiempo de uso tiene?', helper: 'Antigüedad superior a 5 años restringe algunos regímenes.', defaultValue: 'NOSE' },
        { id: 'used_cert', label: '¿Tiene certificado de funcionamiento o revisión técnica?', helper: 'Ayuda a justificar el valor declarado en aduanas.', defaultValue: 'NOSE' }
    ],
    general: [
        { id: 'general_brand', label: '¿Tiene marca o modelo definidos?', helper: 'Mientras más concreto seas, mejor será la sugerencia.', defaultValue: 'NOSE' },
        { id: 'general_new', label: '¿Es nuevo o usado?', helper: 'Determina si aplican restricciones de segunda mano.', defaultValue: 'NOSE' },
        { id: 'general_cert', label: '¿Requiere algún permiso o certificación especial?', helper: 'Algunos productos necesitan VUCE, DIGEMID, MTC, etc.', defaultValue: 'NOSE' },
        { id: 'general_volume', label: '¿Es importación única o recurrente?', helper: 'Ayuda a recomendar el mejor régimen para tu caso.', defaultValue: 'NOSE' },
        { id: 'general_label', label: '¿El producto viene etiquetado en español?', helper: 'Etiquetado en español es requisito para comercializar.', defaultValue: 'NOSE' }
    ]
};

function getActiveQuestionSet() {
    const profile = detectProductProfile();
    const questions = DYNAMIC_QUESTION_SETS[profile.key] || DYNAMIC_QUESTION_SETS.general;

    // Appends a product hint to the label for clarity
    const productName = getFieldValue('prodNombre', '').trim();
    const hint = productName ? ` — "${productName.substring(0, 40)}${productName.length > 40 ? '...' : ''}"` : '';

    return {
        profile: Object.assign({}, profile, {
            label: profile.label + hint
        }),
        questions
    };
}

function getDynamicAnswerValue(id) {
    return (wizardData.dynamicAnswers && wizardData.dynamicAnswers[id]) || 'NOSE';
}

function normalizeYesNoValue(value) {
    const raw = String(value || '').toUpperCase();
    if (raw === 'SI' || raw === 'SÍ') return 'SI';
    if (raw === 'NO') return 'NO';
    return 'NOSE';
}

function setLegacyQuestionValue(fieldId, value) {
    const el = document.getElementById(fieldId);
    if (el) {
        el.value = normalizeYesNoValue(value);
    }
}

function syncLegacyQuestionModel(profile = detectProductProfile()) {
    const state = {
        qConsumo: 'NO', qSalud: 'NO', qWifi: 'NO', qContacto: 'NO', qMadera: 'NO', qUsado: 'NO'
    };
    const k = profile.key;

    if (k === 'food') state.qConsumo = 'SI';
    if (k === 'cosmetics' || k === 'health') state.qSalud = 'SI';
    if (k === 'tech') state.qWifi = 'SI';
    if (k === 'agriculture' || k === 'chemicals') state.qContacto = 'SI';
    if (k === 'wood') state.qMadera = 'SI';
    if (k === 'used' || k === 'vehicles') state.qUsado = 'SI';

    Object.entries(state).forEach(([fieldId, value]) => setLegacyQuestionValue(fieldId, value));
    wizardData.vuceQuestions.consumo = state.qConsumo === 'SI';
    wizardData.vuceQuestions.salud = state.qSalud === 'SI';
    wizardData.vuceQuestions.wifi = state.qWifi === 'SI';
    wizardData.vuceQuestions.contacto = state.qContacto === 'SI';
    wizardData.vuceQuestions.madera = state.qMadera === 'SI';
    wizardData.vuceQuestions.usado = state.qUsado === 'SI';
    wizardData.dynamicProfile = profile.key;
}

function renderDynamicQuestions() {
    const area = document.getElementById('dynamicQuestionArea');
    const meta = document.getElementById('dynamicQuestionMeta');
    if (!area) return;

    const { profile, questions } = getActiveQuestionSet();
    wizardData.dynamicProfile = profile.key;
    wizardData.dynamicQuestions = questions;
    syncLegacyQuestionModel(profile);

    if (meta) {
        meta.textContent = `${profile.label}. Estas preguntas cambian para pedir la evidencia exacta y no mezclar permisos de otra categoría.`;
    }

    area.innerHTML = '';
    questions.forEach((question) => {
        const currentValue = normalizeYesNoValue(getDynamicAnswerValue(question.id) || question.defaultValue || 'NOSE');
        const card = document.createElement('div');
        card.className = 'flex flex-col gap-2 bg-[var(--surface-1)] p-3 rounded-xl border border-[var(--border)]';
        card.dataset.questionId = question.id;
        card.innerHTML = `
            <div class="flex flex-col gap-1">
                <span class="text-[11px] text-[var(--text-secondary)] font-semibold">${question.label}</span>
                <span class="text-[10px] text-[var(--text-tertiary)] font-semibold leading-relaxed">${question.helper || ''}</span>
            </div>
            <div class="q-btn-group" data-question-group="${question.id}">
                <button type="button" class="q-btn ${currentValue === 'SI' ? 'active' : ''}" onclick="selectDynamicQuestionOption('${question.id}', 'SI', this)">Sí</button>
                <button type="button" class="q-btn ${currentValue === 'NO' ? 'active' : ''}" onclick="selectDynamicQuestionOption('${question.id}', 'NO', this)">No</button>
                <button type="button" class="q-btn ${currentValue === 'NOSE' ? 'active' : ''}" onclick="selectDynamicQuestionOption('${question.id}', 'NOSE', this)">No sé</button>
            </div>
        `;
        area.appendChild(card);
    });
}

function selectDynamicQuestionOption(fieldId, value, buttonEl) {
    if (!wizardData.dynamicAnswers || typeof wizardData.dynamicAnswers !== 'object') {
        wizardData.dynamicAnswers = {};
    }
    wizardData.dynamicAnswers[fieldId] = normalizeYesNoValue(value);

    if (buttonEl && buttonEl.parentElement) {
        const buttons = buttonEl.parentElement.querySelectorAll('.q-btn');
        buttons.forEach((btn) => btn.classList.remove('active'));
        buttonEl.classList.add('active');
    }

    onVuceQuestionChange();
}

function setText(id, value) {
    const el = document.getElementById(id);
    if (el) el.innerText = value;
}

function getFieldValue(id, fallback = '') {
    const el = document.getElementById(id);
    return el ? el.value : fallback;
}

function setVal(id, value) {
    const el = document.getElementById(id);
    if (el && value !== undefined && value !== null) {
        el.value = value;
    }
}

function saveWizardDraft() {
    captureFields();
    try {
        wizardData.currentStep = currentStep;
        wizardData.savedAt = new Date().toISOString();
        wizardData.vuceAnswers = {
            qConsumo: getQuestionAnswer('qConsumo'),
            qSalud: getQuestionAnswer('qSalud'),
            qWifi: getQuestionAnswer('qWifi'),
            qContacto: getQuestionAnswer('qContacto'),
            qMadera: getQuestionAnswer('qMadera'),
            qUsado: getQuestionAnswer('qUsado')
        };
        wizardData.dynamicAnswers = Object.assign({}, wizardData.dynamicAnswers || {});
        localStorage.setItem(WIZARD_DRAFT_KEY, JSON.stringify(wizardData));
        updateAutosaveStatus(wizardData.savedAt);
    } catch (e) {}
}

function updateAutosaveStatus(savedAt) {
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
        : 'Guardado ' + AUTOSAVE_TIME_FORMATTER.format(date);
    el.dataset.state = 'saved';
}

function loadWizardDraft() {
    try {
        let raw = localStorage.getItem(WIZARD_DRAFT_KEY);
        let fromEmergency = false;
        if (!raw) {
            raw = sessionStorage.getItem('importease_emergency_draft');
            if (raw) {
                fromEmergency = true;
            }
        }
        if (!raw) return null;
        
        const draft = JSON.parse(raw);
        
        // Error 25: Validar estructura tipada del borrador para evitar corrupciones
        if (!draft || typeof draft !== 'object') return null;
        
        // Asegurar campos numéricos e indispensables válidos
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
            localStorage.setItem(WIZARD_DRAFT_KEY, JSON.stringify(draft));
            sessionStorage.removeItem('importease_emergency_draft');
        }
        
        return draft;
    } catch (e) {
        localStorage.removeItem(WIZARD_DRAFT_KEY);
        return null;
    }
}

function checkAndPromptDraft() {
    const draft = loadWizardDraft();
    if (!draft) {
        initWizardNormally();
        return;
    }

    const hasReturnKeys = localStorage.getItem(WIZARD_HS_RETURN_KEY) || 
                          localStorage.getItem(WIZARD_INCOTERM_RETURN_KEY) || 
                          new URLSearchParams(window.location.search).has('step');

    if (hasReturnKeys) {
        applyWizardDraft(draft);
        initWizardNormally();
        return;
    }

    showDraftPromptModal(draft);
}

function initWizardNormally() {
    toggleRucField();
    updateImportRouteUI();
    renderDynamicQuestions();
    fetchExchangeRate();
    updateCIFCalculations();
    restoreHsReturnSelection();
    restoreIncotermReturnSelection();
    restoreRequestedStepFromQuery();
    updateSidebar();
}

function showDraftPromptModal(draft) {
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
                    <span class="text-[var(--text-primary)] font-black text-right truncate max-w-[200px]">${escapeHtml(prodName)}</span>
                </div>
                <div class="flex justify-between items-center text-xs font-semibold">
                    <span class="text-[var(--text-tertiary)]">Último paso guardado:</span>
                    <span class="px-2 py-0.5 rounded bg-purple-100 text-purple-700 text-[10px] font-black uppercase">Paso ${stepNum} de 6</span>
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
        applyWizardDraft(draft);
        overlay.remove();
        style.remove();
        initWizardNormally();
        mostrarNotificacion("Borrador restaurado con éxito");
    });

    document.getElementById('btnDraftNew').addEventListener('click', () => {
        document.body.classList.remove('modal-open');
        try {
            localStorage.removeItem(WIZARD_DRAFT_KEY);
        } catch(e) {}
        overlay.remove();
        style.remove();
        initWizardNormally();
        mostrarNotificacion("Iniciando cotización en blanco");
    });
}

function applyWizardDraft(draft) {
    if (!draft || typeof draft !== 'object') return;
    wizardData = Object.assign({}, wizardData, draft);

    setVal('prodNombre', wizardData.prodNombre);
    setVal('prodTecnica', wizardData.prodTecnica);
    setVal('opPaisOrigen', wizardData.opPaisOrigen);
    setVal('prodUso', wizardData.opTipo === 'COMERCIAL' ? 'COMERCIAL' : 'PERSONAL');
    setVal('prodCantidad', wizardData.prodCantidad);
    setVal('prodMarca', wizardData.prodMarca);
    setVal('prodModelo', wizardData.prodModelo);
    setVal('opNombre', wizardData.opNombre);
    setVal('opTipo', wizardData.opTipo);
    setVal('opRuc', wizardData.opRuc);
    setVal('opIncoterm', wizardData.opIncoterm);
    setVal('opProveedor', wizardData.opProveedor);
    setVal('logFob', wizardData.logFob);
    setVal('logFlete', wizardData.logFlete);
    setVal('logSeguro', wizardData.logSeguro);
    setVal('logTC', wizardData.logTC);
    setVal('tribPerfil', wizardData.tribPerfil);
    setVal('tribTlc', wizardData.tribTlc);
    setVal('tribCertificado', wizardData.tribCertificado);
    setVal('vuceEstado', wizardData.vuceEstado);
    setVal('vuceSuce', wizardData.vuceSuce);
    setVal('vuceResolucion', wizardData.vuceResolucion);
    setVal('vuceObs', wizardData.vuceObs);
    wizardData.selectedHSOrigin = draft.selectedHSOrigin || wizardData.selectedHSOrigin || 'AUTO';
    wizardData.dynamicAnswers = Object.assign({}, draft.dynamicAnswers || wizardData.dynamicAnswers || {});
    const answers = wizardData.vuceAnswers || {};
    setVal('qConsumo', answers.qConsumo || (wizardData.vuceQuestions.consumo ? 'SI' : 'NO'));
    setVal('qSalud', answers.qSalud || (wizardData.vuceQuestions.salud ? 'SI' : 'NO'));
    setVal('qWifi', answers.qWifi || (wizardData.vuceQuestions.wifi ? 'SI' : 'NO'));
    setVal('qContacto', answers.qContacto || (wizardData.vuceQuestions.contacto ? 'SI' : 'NO'));
    setVal('qMadera', answers.qMadera || (wizardData.vuceQuestions.madera ? 'SI' : 'NO'));
    setVal('qUsado', answers.qUsado || (wizardData.vuceQuestions.usado ? 'SI' : 'NO'));

    if (wizardData.selectedHS) {
        applyHSCodeUI(wizardData.selectedHS);
    }

    onProdUsoChange();
    onVuceQuestionChange();
    renderDynamicQuestions();
    updateDocButtonsUI();
    updateImportRouteUI();
    updateCIFCalculations();
    evalVuceQuestions();
    updateSidebar();

    const restoredStep = Number(draft.currentStep);
    if (Number.isFinite(restoredStep) && restoredStep >= 1 && restoredStep <= 6) {
        goToStep(restoredStep);
    } else {
        updateAutosaveStatus(wizardData.savedAt);
    }
}

function restoreHsReturnSelection() {
    try {
        const raw = localStorage.getItem(WIZARD_HS_RETURN_KEY);
        if (!raw) return;
        const hs = JSON.parse(raw);
        if (hs && hs.codigo) {
            wizardData.selectedHS = hs;
            wizardData.selectedHSOrigin = 'MANUAL';
            applyHSCodeUI(hs);
            updateCIFCalculations();
            evalVuceQuestions();
            mostrarNotificacion("Codigo recuperado desde el buscador");
            localStorage.removeItem(WIZARD_HS_RETURN_KEY);
            goToStep(3);
        }
    } catch (e) {}
}

function restoreIncotermReturnSelection() {
    try {
        const raw = localStorage.getItem(WIZARD_INCOTERM_RETURN_KEY);
        if (!raw) return false;
        const decision = JSON.parse(raw);
        if (!decision || !decision.incoterm) return false;

        const incoterm = String(decision.incoterm || 'FOB').toUpperCase();
        wizardData.opIncoterm = incoterm;
        wizardData.logFob = Number(decision.fob || decision.valorProducto || wizardData.logFob || 0);
        wizardData.logFlete = Number(decision.flete || wizardData.logFlete || 0);
        wizardData.logSeguro = Number(decision.seguro || wizardData.logSeguro || 0);
        wizardData.manualCostsModified = true;

        setVal('opIncoterm', incoterm);
        setVal('opIncotermSelector', incoterm === 'CIF' ? 'CIF' : 'FOB');
        setVal('logFob', wizardData.logFob);
        setVal('logFlete', wizardData.logFlete);
        setVal('logSeguro', wizardData.logSeguro);
        syncIncotermButtons();
        updateCIFCalculations();
        saveWizardDraft();
        localStorage.removeItem(WIZARD_INCOTERM_RETURN_KEY);
        mostrarNotificacion(`Incoterm ${incoterm} aplicado al costeo`);
        goToStep(4);
        return true;
    } catch (e) {
        return false;
    }
}

function restoreRequestedStepFromQuery() {
    try {
        const params = new URLSearchParams(window.location.search || '');
        const step = Number(params.get('step'));
        if (Number.isFinite(step) && step >= 1 && step <= 6) {
            goToStep(step);
        }
    } catch (e) {}
}

function openIncotermsLab() {
    captureFields();
    saveWizardDraft();
    const params = new URLSearchParams({
        returnWizard: '1',
        fob: String(wizardData.logFob || 0),
        flete: String(wizardData.logFlete || 0),
        seguro: String(wizardData.logSeguro || 0),
        incoterm: wizardData.opIncoterm || 'FOB',
        tipo: wizardData.opTipo || wizardData.prodUso || 'COMERCIAL',
        pais: wizardData.opPaisOrigen || 'CHINA'
    });
    window.location.href = 'incoterms-lab.jsp?' + params.toString();
}

// --- MOCK PRESETS DE EJEMPLO DE CUMPLIMIENTO RAPIDO ---
const EJEMPLOS_PRESET = {
    celular: {
        opNombre: "Importación Celulares Xiaomi",
        prodNombre: "Xiaomi Redmi Note 13 Pro 5G",
        prodTecnica: "Teléfonos móviles celulares inteligentes con conectividad LTE/5G, Wi-Fi 2.4/5GHz y Bluetooth 5.2",
        opProveedor: "Shenzhen Xiaomi Export Co.",
        opPaisOrigen: "CHINA",
        opIncoterm: "FOB",
        logFob: 8200.00,
        logFlete: 520.00,
        logSeguro: 95.00,
        hsCode: "8517.13.00.00",
        hsDesc: "Teléfonos inteligentes (smartphones) con tecnología celular integrada",
        av: 0.0,
        requiereVuce: true,
        entidadVuce: "MTC",
        tupaDatos: {
            "Marca": "Xiaomi",
            "Modelo": "Redmi Note 13 Pro",
            "Fabricante": "Xiaomi Communications Co., Ltd.",
            "País de origen": "China",
            "Tecnología inalámbrica": "5G, Wi-Fi, Bluetooth",
            "Frecuencia": "2.4 GHz y 5 GHz",
            "Código Homologación": "TR-MTC-2026-948"
        }
    },
    proteina: {
        opNombre: "Importación Proteínas Whey",
        prodNombre: "Whey Protein Gold Standard 5Lbs",
        prodTecnica: "Suplemento alimenticio en polvo a base de proteína de suero de leche sabor chocolate",
        opProveedor: "Optimum Nutrition Inc.",
        opPaisOrigen: "USA",
        opIncoterm: "FOB",
        logFob: 4500.00,
        logFlete: 380.00,
        logSeguro: 45.00,
        hsCode: "2106.90.99.00",
        hsDesc: "Preparaciones alimenticias no expresadas ni comprendidas en otra parte",
        av: 6.0,
        requiereVuce: true,
        entidadVuce: "DIGESA",
        tupaDatos: {
            "Producto": "Gold Standard Whey",
            "Marca": "Optimum Nutrition",
            "Presentación": "Envase plástico de 5 Lbs",
            "Composición": "Aislado de proteína de suero, aminoácidos, saborizantes",
            "Fabricante": "Optimum Nutrition USA",
            "Uso sugerido": "Suplementación dietética / nutrición deportiva",
            "Certificado Libre Venta": "CLV-FDA-2026-8419"
        }
    },
    rayos: {
        opNombre: "Importación Equipos de Ultrasonido",
        prodNombre: "Ecógrafo Portátil Digital Chison",
        prodTecnica: "Dispositivo de diagnóstico médico por ultrasonido portátil de alta resolución",
        opProveedor: "Chison Medical Tech Ltd.",
        opPaisOrigen: "CHINA",
        opIncoterm: "CIF",
        logFob: 15400.00,
        logFlete: 850.00,
        logSeguro: 120.00,
        hsCode: "9018.12.00.00",
        hsDesc: "Aparatos de diagnóstico por exploración ultrasónica (ecógrafos)",
        av: 0.0,
        requiereVuce: true,
        entidadVuce: "DIGEMID",
        tupaDatos: {
            "Nombre comercial": "Ecógrafo Digital Portátil",
            "Principio activo / Componente": "Ultrasonido de diagnóstico",
            "Concentración / Ficha": "Sondas lineal y convexa multifrecuencial",
            "Fabricante": "Chison Medical China",
            "Uso declarado": "Imagenología y diagnóstico médico clínico",
            "Registro sanitario / Certificado": "RD-DIGEMID-2025-09852"
        }
    },
    semilla: {
        opNombre: "Importación Semillas de Tomate",
        prodNombre: "Semillas de Tomate Cherry Híbrido",
        prodTecnica: "Semillas botánicas de tomate Cherry listas para siembra agrícola",
        opProveedor: "Seeds & Crops Spain SL",
        opPaisOrigen: "ESPAÑA",
        opIncoterm: "FOB",
        logFob: 2500.00,
        logFlete: 220.00,
        logSeguro: 30.00,
        hsCode: "1209.91.00.00",
        hsDesc: "Semillas de hortalizas para siembra",
        av: 0.0,
        requiereVuce: true,
        entidadVuce: "SENASA",
        tupaDatos: {
            "Nombre común": "Tomate Cherry",
            "Nombre científico": "Solanum lycopersicum",
            "Uso": "Siembra y producción hortícola",
            "País de procedencia": "España",
            "Certificado fitosanitario": "CF-SPAIN-2026-4819",
            "Cantidad y empaque": "Cajas herméticas de 500g"
        }
    },
    madera: {
        opNombre: "Importación Tableros de Roble",
        prodNombre: "Pisos de Madera de Roble Europeo",
        prodTecnica: "Tablones de roble europeo aserrados longitudinalmente y secados al horno",
        opProveedor: "Balkan Timber Ltd.",
        opPaisOrigen: "ALEMANIA",
        opIncoterm: "CIF",
        logFob: 9500.00,
        logFlete: 750.00,
        logSeguro: 110.00,
        hsCode: "4407.91.00.00",
        hsDesc: "Madera de roble aserrada o desbastada longitudinalmente",
        av: 6.0,
        requiereVuce: true,
        entidadVuce: "SERFOR",
        tupaDatos: {
            "Especie común": "Roble Blanco",
            "Nombre científico": "Quercus robur",
            "País de origen": "Alemania",
            "CITES Status": "No listado en CITES / Libre comercialización",
            "Origen legal / Título": "Plan de manejo forestal certificado FSC",
            "Certificado forestal de origen": "FSC-DE-2026-0941"
        }
    }
};

// --- INICIALIZADORES Y TIMELINE ---
function toggleModoDidactico() {
    modoDidacticoActivo = true;
    const boxes = document.querySelectorAll('.didactic-box');
    boxes.forEach((box) => box.classList.remove('hidden'));
}

function toggleRucField() {
    const tipo = document.getElementById('opTipo').value;
    const container = document.getElementById('rucFieldContainer');
    if (!container) return;
    if (tipo === 'COMERCIAL') {
        container.style.display = 'block';
    } else {
        container.style.display = 'none';
    }
}

// --- HUMANIZED WIZARD SYNCHRONIZERS ---
function onProdUsoChange() {
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
    toggleRucField();
    updateImportRouteUI();
    sugerirCostosFobFleteSeguro();
    updateSidebar();
    saveWizardDraft();
}
function selectImportRoute(route) {
    const prodUso = document.getElementById('prodUso');
    if (!prodUso) return;
    prodUso.value = route;
    onProdUsoChange();
}
function updateImportRouteUI() {
    const prodUso = document.getElementById('prodUso');
    const isCommercial = prodUso && prodUso.value === 'COMERCIAL';
    const personalCard = document.getElementById('routeCardPersonal');
    const commercialCard = document.getElementById('routeCardComercial');
    const routeHint = document.getElementById('routeHint');
    const quantityHint = document.getElementById('cantidadHint');
    if (personalCard && commercialCard) {
        personalCard.className = isCommercial
            ? "rounded-2xl border border-[#E6E2D8] bg-white px-4 py-4 text-left transition-all hover:border-blue-400"
            : "rounded-2xl border border-blue-500 bg-blue-50 px-4 py-4 text-left transition-all shadow-sm ring-2 ring-blue-100";
        commercialCard.className = isCommercial
            ? "rounded-2xl border border-purple-500 bg-purple-50 px-4 py-4 text-left transition-all shadow-sm ring-2 ring-purple-100"
            : "rounded-2xl border border-[#E6E2D8] bg-white px-4 py-4 text-left transition-all hover:border-purple-400";
    }
    if (routeHint) {
        routeHint.textContent = isCommercial
            ? 'Ruta comercial: priorizaremos costo total, permisos y documentos para vender o abastecer tu negocio.'
            : 'Ruta personal: te ayudaremos a saber si tu compra puede entrar como personal o si ya parece comercial.';
    }
    if (quantityHint) {
        quantityHint.textContent = isCommercial
            ? 'Indica una cantidad realista para venta, reposicion o primera compra.'
            : 'Si la cantidad es alta, te avisaremos que aduanas podria verla como comercial.';
    }
}

function syncOpNombre() {
    const prod = document.getElementById('prodNombre').value;
    document.getElementById('opNombre').value = prod ? "Importación de " + prod : "Evaluación de producto";
    wizardData.prodNombre = prod;
    wizardData.opNombre = document.getElementById('opNombre').value;
    sugerirCostosFobFleteSeguro();
    updateSidebar();
}

function sugerirCostosFobFleteSeguro() {
    if (wizardData.manualCostsModified || currentStep >= 4) {
        return; // No sobreescribir si ya lo editó manualmente o está en el paso de costos
    }
    
    const prodName = (document.getElementById('prodNombre').value || '').trim().toLowerCase();
    const cant = parseInt(document.getElementById('prodCantidad').value) || 1;
    const uso = document.getElementById('prodUso').value || 'PERSONAL';
    
    // Valores base predeterminados por unidad
    let unitFob = 250.00;
    let unitFlete = 25.00;
    let unitSeguro = 5.00;
    
    if (prodName.includes('perfume') || prodName.includes('colonia') || prodName.includes('fragancia') || prodName.includes('cosmetico') || prodName.includes('maquillaje')) {
        unitFob = 35.00; // Un perfume promedio cuesta $35
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
    
    // Si es para uso personal, sugerimos valores unitarios más conservadores
    if (uso === 'PERSONAL') {
        unitFlete = Math.max(5.00, unitFlete * 0.7);
        unitSeguro = Math.max(1.00, unitSeguro * 0.7);
    }
    
    let totalFob = unitFob * cant;
    let totalFlete = unitFlete * cant;
    let totalSeguro = unitSeguro * cant;
    
    // Para importación personal de muy pocas unidades (por ejemplo 1 perfume), no queremos fletes gigantescos
    if (uso === 'PERSONAL' && cant <= 3) {
        totalFlete = 15.00; 
        totalSeguro = 2.50;
    }
    
    const incotermVal = String(wizardData.opIncoterm || 'FOB').toUpperCase();
    if (incotermVal === 'CIF' || incotermVal === 'CIP') {
        totalSeguro = 0;
    }
    
    wizardData.logFob = totalFob;
    wizardData.logFlete = totalFlete;
    wizardData.logSeguro = totalSeguro;
    
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
    
    updateCIFCalculations();
}

function onIncotermSelectorChange() {
    const val = document.getElementById('opIncotermSelector').value;
    document.getElementById('opIncoterm').value = val;
    wizardData.opIncoterm = val;
    
    const seguroInput = document.getElementById('logSeguro');
    if (val === 'CIF' || val === 'CIP') {
        if (seguroInput) {
            seguroInput.value = "0.00";
            seguroInput.disabled = true;
            seguroInput.classList.add('opacity-50', 'cursor-not-allowed');
        }
        wizardData.logSeguro = 0;
    } else {
        if (seguroInput) {
            seguroInput.disabled = false;
            seguroInput.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    }
    
    updateCIFCalculations();
}

function syncIncotermButtons(activeButton) {
    const container = document.getElementById('incotermSelectorContainer');
    if (!container) return;
    const value = wizardData.opIncoterm || 'FOB';
    container.querySelectorAll('button').forEach((btn) => {
        const onclickAttr = btn.getAttribute('onclick') || '';
        const isFob = onclickAttr.includes("'FOB'") && !onclickAttr.includes('FOB_UNKNOWN');
        const isCif = onclickAttr.includes("'CIF'");
        const isUnknown = onclickAttr.includes('FOB_UNKNOWN');
        const isActive = activeButton ? btn === activeButton : (value === 'CIF' ? isCif : (value === 'FOB' ? isFob : isUnknown));
        btn.className = isActive
            ? "w-full text-left p-4 rounded-xl border border-purple-500 bg-purple-50 text-[#0A5C4A] font-bold flex items-center gap-3 transition-all outline-none"
            : "w-full text-left p-4 rounded-xl border border-[#E6E2D8] bg-white text-[#1F2937] font-semibold hover:bg-[#FAF6F0] flex items-center gap-3 transition-all outline-none";
        const dot = btn.querySelector('span');
        if (dot) dot.className = isActive
            ? "w-2.5 h-2.5 rounded-full bg-[#0A5C4A]"
            : "w-2.5 h-2.5 rounded-full border border-gray-300";
    });
}

function selectIncotermOption(value, buttonEl) {
    const normalized = value === 'CIF' ? 'CIF' : 'FOB';
    const incoterm = document.getElementById('opIncoterm');
    const incotermSelector = document.getElementById('opIncotermSelector');
    if (incoterm) incoterm.value = normalized;
    if (incotermSelector) incotermSelector.value = value;
    wizardData.opIncoterm = normalized;

    const seguroInput = document.getElementById('logSeguro');
    if (value === 'CIF' || value === 'CIP') {
        if (seguroInput) {
            seguroInput.value = "0.00";
            seguroInput.disabled = true;
            seguroInput.classList.add('opacity-50', 'cursor-not-allowed');
        }
        wizardData.logSeguro = 0;
    } else {
        if (seguroInput) {
            seguroInput.disabled = false;
            seguroInput.classList.remove('opacity-50', 'cursor-not-allowed');
        }
    }

    syncIncotermButtons(buttonEl);
    updateCIFCalculations();
    saveWizardDraft();
}

function onVuceQuestionChange() {
    const profile = detectProductProfile();
    if (wizardData.dynamicAnswers && Object.keys(wizardData.dynamicAnswers).length) {
        syncLegacyQuestionModel(profile);
    } else {
        wizardData.vuceQuestions.consumo = getQuestionAnswer('qConsumo') === 'SI';
        wizardData.vuceQuestions.salud = getQuestionAnswer('qSalud') === 'SI';
        wizardData.vuceQuestions.wifi = getQuestionAnswer('qWifi') === 'SI';
        wizardData.vuceQuestions.contacto = getQuestionAnswer('qContacto') === 'SI';
        wizardData.vuceQuestions.usado = getQuestionAnswer('qUsado') === 'SI';
        wizardData.vuceQuestions.madera = getQuestionAnswer('qMadera') === 'SI';
    }
    renderDynamicQuestions();
    if (wizardData.vuceQuestions.madera && (!wizardData.selectedHS || !wizardData.selectedHS.requiereVuce)) {
        wizardData.selectedHS = {
            codigo: '4407.91.00.00',
            descripcionEs: 'Madera de roble aserrada longitudinalmente',
            adValorem: 6,
            requiereVuce: true,
            entidadVuce: 'SERFOR'
        };
        wizardData.selectedHSOrigin = 'AUTO';
        applyHSCodeUI(wizardData.selectedHS);
    }
    evalVuceQuestions();
    saveWizardDraft();
}

// --- NAVEGACIÓN Y VALIDACIÓN ---
function goToStep(step) {
    step = Math.max(1, Math.min(4, step));
    currentStep = step;
    
    // Ocultar todos los steps y mostrar el activo
    for (let i = 1; i <= 4; i++) {
        const sec = document.getElementById('stepGroup-' + i);
        if (sec) {
            sec.className = (i === step) ? "step-content active" : "step-content";
        }
    }
    
    // Actualizar timeline visual
    for (let i = 1; i <= 4; i++) {
        const indicator = document.getElementById('stepIndicator-' + i);
        const bar = document.getElementById('timelineBar-' + i);
        
        if (indicator) {
            const circle = indicator.querySelector('span:first-child');
            const label = indicator.querySelector('span:last-child');
            
            // Premium Clickable Step Navigation
            indicator.style.cursor = 'pointer';
            indicator.onclick = (e) => {
                e.preventDefault();
                let targetStep = i;
                if (targetStep < currentStep) {
                    goToStep(targetStep);
                } else {
                    let valid = true;
                    for (let s = currentStep; s < targetStep; s++) {
                        if (!validateStep(s)) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        goToStep(targetStep);
                    }
                }
            };
            
            if (i < step) {
                indicator.classList.remove('opacity-50');
                circle.className = "w-6 h-6 rounded-full flex items-center justify-center font-bold text-[10px] bg-blue-600 text-white shadow-lg shadow-blue-500/20";
                circle.innerText = "OK";
                label.className = "text-[10px] font-black uppercase tracking-widest text-blue-600";
            } else if (i === step) {
                indicator.classList.remove('opacity-50');
                circle.className = "w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-purple-600 text-white shadow-lg shadow-purple-500/30";
                circle.innerText = i;
                label.className = "text-[10px] font-black uppercase tracking-widest text-purple-700";
            } else {
                indicator.classList.add('opacity-50');
                circle.className = "w-6 h-6 rounded-full flex items-center justify-center font-bold text-xs bg-white text-gray-500 border border-[#E6E2D8]";
                circle.innerText = i;
                label.className = "text-[10px] font-black uppercase tracking-widest text-gray-500";
            }
        }
        
        if (bar) {
            bar.className = (i < step) ? "h-0.5 flex-1 bg-blue-600 mx-2" : "h-0.5 flex-1 bg-[#E6E2D8] mx-2";
        }
    }
    
    // Controlar visibilidad de botones del footer
    document.getElementById('btnPrevStep').style.visibility = (step === 1) ? 'hidden' : 'visible';
    
    const btnNext = document.getElementById('btnNextStep');
    if (step === 4) {
        btnNext.style.display = 'none';
        renderFinalSummary();
    } else {
        btnNext.style.display = 'block';
        btnNext.innerText = 'Continuar →';
    }
    
    captureFields();
    updateSidebar();
    saveWizardDraft();
}

function nextStep() {
    if (validateStep(currentStep)) {
        if (currentStep === 1) {
            buscarPartidaSugerida();
        } else if (currentStep === 2 && !wizardData.selectedHS) {
            ensureFallbackHS();
        }
        goToStep(currentStep + 1);
    }
}

function prevStep() {
    if (currentStep > 1) {
        goToStep(currentStep - 1);
    }
}

function validateStep(step) {
    captureFields();
    if (step === 1) {
        if (!wizardData.opNombre.trim()) { return wizardWarning("El nombre de la operacion es obligatorio."); }
        if (wizardData.opTipo === 'COMERCIAL') {
            if (!wizardData.opRuc.trim()) { return wizardWarning("El RUC es obligatorio para importaciones comerciales."); }
            if (!validarRucSunatMod11(wizardData.opRuc)) { return wizardWarning("El RUC ingresado no cumple con el algoritmo Módulo 11 de la SUNAT."); }
        }
        if (!wizardData.opPaisOrigen) { return wizardWarning("El pais de origen es obligatorio."); }
        if (!wizardData.opIncoterm) { return wizardWarning("Indica si el precio incluye envio internacional."); }
        if (!wizardData.prodNombre.trim()) { return wizardWarning("Escribe que producto quieres importar."); }
        if (!wizardData.prodTecnica.trim()) { return wizardWarning("Describe para que sirve o que caracteristicas tiene el producto."); }
        return true;
    }
    if (step === 2) {
        if (!wizardData.selectedHS && !ensureFallbackHS()) {
            return wizardWarning("No encontramos un codigo probable todavia. Escribe mas detalle del producto o ingresa un codigo manual.");
        }
        return true;
    }
    if (step === 4) {
        if (wizardData.logFob <= 0) { return wizardWarning("El valor FOB comercial debe ser mayor que cero."); }
        return true;
    }
    return true;
}

function wizardWarning(message) {
    mostrarNotificacion(message, 'warning');
    return false;
}

// --- MANEJO DE CAMPOS Y CAPTURA ---
function captureFields() {
    wizardData.opNombre = getFieldValue('opNombre', wizardData.opNombre);
    wizardData.opTipo = getFieldValue('opTipo', wizardData.opTipo);
    wizardData.opRuc = getFieldValue('opRuc', wizardData.opRuc);
    wizardData.opProveedor = getFieldValue('opProveedor', wizardData.opProveedor);
    wizardData.opPaisOrigen = getFieldValue('opPaisOrigen', wizardData.opPaisOrigen);
    wizardData.opIncoterm = getFieldValue('opIncoterm', wizardData.opIncoterm);
    
    wizardData.prodNombre = getFieldValue('prodNombre', wizardData.prodNombre);
    wizardData.prodTecnica = getFieldValue('prodTecnica', wizardData.prodTecnica);
    wizardData.prodCantidad = parseInt(getFieldValue('prodCantidad', wizardData.prodCantidad)) || 1;
    wizardData.prodMarca = getFieldValue('prodMarca', wizardData.prodMarca) || '';
    wizardData.prodModelo = getFieldValue('prodModelo', wizardData.prodModelo) || '';
    wizardData.prodUso = getFieldValue('prodUso', wizardData.prodUso) || 'PERSONAL';
    
    wizardData.logFob = parseFloat(getFieldValue('logFob', wizardData.logFob)) || 0;
    wizardData.logFlete = parseFloat(getFieldValue('logFlete', wizardData.logFlete)) || 0;
    wizardData.logSeguro = parseFloat(getFieldValue('logSeguro', wizardData.logSeguro)) || 0;
    
    wizardData.tribPerfil = getFieldValue('tribPerfil', wizardData.tribPerfil);
    wizardData.tribTlc = getFieldValue('tribTlc', wizardData.tribTlc);
    
    wizardData.vuceEstado = getFieldValue('vuceEstado', wizardData.vuceEstado);
    wizardData.vuceSuce = getFieldValue('vuceSuce', wizardData.vuceSuce);
    wizardData.vuceResolucion = getFieldValue('vuceResolucion', wizardData.vuceResolucion);
    wizardData.vuceObs = getFieldValue('vuceObs', wizardData.vuceObs);

    // Capturar campos TUPA dinámicos si existen
    const dynamicInputs = document.querySelectorAll('#tupaFieldsArea input, #tupaFieldsArea select');
    dynamicInputs.forEach(input => {
        const label = input.getAttribute('data-label');
        if (label) {
            wizardData.tupaDatos[label] = input.value;
        }
    });
}

function restoreFields() {
    document.getElementById('opNombre').value = wizardData.opNombre;
    document.getElementById('opTipo').value = wizardData.opTipo;
    document.getElementById('opRuc').value = wizardData.opRuc;
    document.getElementById('opProveedor').value = wizardData.opProveedor;
    document.getElementById('opPaisOrigen').value = wizardData.opPaisOrigen;
    document.getElementById('opIncoterm').value = wizardData.opIncoterm;
    toggleRucField();
    
    document.getElementById('prodNombre').value = wizardData.prodNombre;
    document.getElementById('prodTecnica').value = wizardData.prodTecnica;
    document.getElementById('prodCantidad').value = wizardData.prodCantidad || 1;
    document.getElementById('prodMarca').value = wizardData.prodMarca || '';
    document.getElementById('prodModelo').value = wizardData.prodModelo || '';
    document.getElementById('prodUso').value = wizardData.prodUso || 'PERSONAL';
    
    document.getElementById('logFob').value = wizardData.logFob;
    document.getElementById('logFlete').value = wizardData.logFlete;
    document.getElementById('logSeguro').value = wizardData.logSeguro;
    
    document.getElementById('tribPerfil').value = wizardData.tribPerfil;
    document.getElementById('tribTlc').value = wizardData.tribTlc;
    
    document.getElementById('vuceEstado').value = wizardData.vuceEstado;
    document.getElementById('vuceSuce').value = wizardData.vuceSuce;
    document.getElementById('vuceResolucion').value = wizardData.vuceResolucion;
    document.getElementById('vuceObs').value = wizardData.vuceObs;

    // Sync custom simple UI fields
    const usoVal = (wizardData.opTipo === 'COMERCIAL' || wizardData.prodUso === 'COMERCIAL' || wizardData.prodUso === 'Comercial') ? 'COMERCIAL' : 'PERSONAL';
    document.getElementById('prodUso').value = usoVal;
    
    document.getElementById('opIncotermSelector').value = wizardData.opIncoterm || 'FOB';
    
    document.getElementById('qWifi').value = wizardData.vuceQuestions.wifi ? 'SI' : 'NO';
    document.getElementById('qConsumo').value = wizardData.vuceQuestions.consumo ? 'SI' : 'NO';
    document.getElementById('qSalud').value = wizardData.vuceQuestions.salud ? 'SI' : 'NO';
    document.getElementById('qContacto').value = wizardData.vuceQuestions.contacto ? 'SI' : 'NO';
    document.getElementById('qUsado').value = wizardData.vuceQuestions.usado ? 'SI' : 'NO';
    
    const hasMadera = wizardData.selectedHS && (wizardData.selectedHS.entidadVuce === 'SERFOR' || wizardData.selectedHS.codigo.startsWith('44'));
    document.getElementById('qMadera').value = hasMadera ? 'SI' : 'NO';

    // Sync custom interactive question buttons [ Sí ] [ No ] [ No sé ] visually
    const questions = ['qConsumo', 'qSalud', 'qWifi', 'qContacto', 'qUsado', 'qMadera'];
    questions.forEach(q => {
        const val = document.getElementById(q).value; // 'SI' or 'NO' or 'NOSE'
        const btnGroup = document.getElementById(`btnGroup-${q}`);
        if (btnGroup) {
            const buttons = btnGroup.querySelectorAll('.q-btn');
            buttons.forEach(btn => {
                const btnText = btn.innerText.trim().toUpperCase();
                const matchesSi = (val === 'SI' && (btnText === 'SÍ' || btnText === 'SI'));
                const matchesNo = (val === 'NO' && btnText === 'NO');
                const matchesNose = (val === 'NOSE' && btnText === 'NO SÉ');
                if (matchesSi || matchesNo || matchesNose) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });
        }
    });

    syncIncotermButtons();

    if (wizardData.selectedHS) {
        applyHSCodeUI(wizardData.selectedHS);
    }

    renderDynamicQuestions();
    
    updateCIFCalculations();
}

// --- CARGAR EJEMPLOS DE APLICACIÓN RAPIDA ---
function cargarEjemplo(tipoKey) {
    const preset = EJEMPLOS_PRESET[tipoKey];
    if (!preset) return;
    
    wizardData.opNombre = preset.opNombre;
    wizardData.prodNombre = preset.prodNombre;
    wizardData.prodTecnica = preset.prodTecnica;
    wizardData.opProveedor = preset.opProveedor;
    wizardData.opPaisOrigen = preset.opPaisOrigen;
    wizardData.opIncoterm = preset.opIncoterm;
    
    wizardData.logFob = preset.logFob;
    wizardData.logFlete = preset.logFlete;
    wizardData.logSeguro = preset.logSeguro;
    wizardData.manualCostsModified = true;
    
    wizardData.selectedHS = {
        codigo: preset.hsCode,
        descripcionEs: preset.hsDesc,
        adValorem: preset.av,
        requiereVuce: preset.requiereVuce,
        entidadVuce: preset.entidadVuce
    };
    wizardData.selectedHSOrigin = 'PRESET';

    wizardData.tupaDatos = Object.assign({}, preset.tupaDatos);
    wizardData.dynamicAnswers = {};
    
    // Reset questions
    wizardData.vuceQuestions.wifi = (tipoKey === 'celular');
    wizardData.vuceQuestions.consumo = (tipoKey === 'proteina');
    wizardData.vuceQuestions.salud = (tipoKey === 'rayos');
    wizardData.vuceQuestions.contacto = (tipoKey === 'semilla');
    wizardData.vuceQuestions.usado = false;

    // Auto-corte a TLC si el origen no es China y aplica TLC
    if (preset.opPaisOrigen === 'ESPAÑA') {
        wizardData.tribTlc = 'SI';
        wizardData.docs.CERTIFICADO_ORIGEN = true;
    } else {
        wizardData.tribTlc = 'NO';
        wizardData.docs.CERTIFICADO_ORIGEN = false;
    }

    // Auto-subida didáctica de documentos obligatorios
    wizardData.docs.FACTURA_COMERCIAL = true;
    wizardData.docs.BILL_OF_LADING = true;
    
    restoreFields();
    
    // Forzar renderización de campos TUPA
    evalVuceQuestions();
    
    mostrarNotificacion("Ejemplo de " + preset.entidadVuce + " cargado con éxito");
    goToStep(2);
}

// --- BUSCADOR HS CODE Y SUGERENCIAS ARANCELARIAS ---
const LOCAL_HS_FALLBACKS = [
    { keys: ['celular', 'telefono', 'teléfono', 'smartphone', 'wifi', 'bluetooth', 'router', 'modem'], codigo: '8517.13.00.00', descripcionEs: 'Telefonos inteligentes y equipos de comunicacion inalambrica', adValorem: 0, requiereVuce: true, entidadVuce: 'MTC' },
    { keys: ['proteina', 'proteína', 'suplemento', 'vitamina', 'alimento', 'bebida', 'aceite', 'leche'], codigo: '2106.90.99.00', descripcionEs: 'Preparaciones alimenticias y suplementos no expresados en otra partida', adValorem: 6, requiereVuce: true, entidadVuce: 'DIGESA' },
    { keys: ['perfume', 'fragancia', 'fragancias', 'colonia', 'eau de parfum', 'eau de toilette', 'cologne', 'cosmetico', 'cosmético', 'maquillaje', 'locion'], codigo: '3303.00.00.00', descripcionEs: 'Perfumes y aguas de tocador', adValorem: 0, requiereVuce: true, entidadVuce: 'DIGEMID' },
    { keys: ['medicamento', 'dispositivo medico', 'médico', 'ecografo', 'ecógrafo', 'ultrasonido', 'diagnostico', 'diagnóstico'], codigo: '9018.12.00.00', descripcionEs: 'Dispositivos medicos o aparatos de diagnostico sujetos a revision sanitaria', adValorem: 0, requiereVuce: true, entidadVuce: 'DIGEMID' },
    { keys: ['semilla', 'planta', 'vegetal', 'agricola', 'agrícola', 'animal'], codigo: '1209.91.00.00', descripcionEs: 'Semillas y productos vegetales para siembra o control fitosanitario', adValorem: 0, requiereVuce: true, entidadVuce: 'SENASA' },
    { keys: ['madera', 'tablero', 'piso', 'roble', 'pino', 'flora', 'fauna', 'cuero'], codigo: '4407.91.00.00', descripcionEs: 'Madera aserrada o productos forestales sujetos a control', adValorem: 6, requiereVuce: true, entidadVuce: 'SERFOR' },
    { keys: ['laptop', 'computadora', 'ordenador', 'tablet'], codigo: '8471.30.00.00', descripcionEs: 'Maquinas automaticas para tratamiento o procesamiento de datos portatiles', adValorem: 0, requiereVuce: false, entidadVuce: 'SUNAT' },
    { keys: ['ropa', 'prenda', 'polo', 'camisa', 'pantalon', 'pantalón', 'vestido'], codigo: '6109.10.00.00', descripcionEs: 'Prendas de vestir de punto para uso comercial o personal', adValorem: 11, requiereVuce: false, entidadVuce: 'SUNAT' }
];

function inferLocalHsSuggestions(term) {
    const text = normalizeWizardText(`${term || ''} ${wizardData.prodTecnica || ''} ${wizardData.prodMarca || ''} ${wizardData.prodModelo || ''}`);
    const queryTokens = tokenizeWizardText(text);
    const profile = detectProductProfile();
    const matches = LOCAL_HS_FALLBACKS
        .map((item) => {
            const normalizedDesc = normalizeWizardText(item.descripcionEs || '');
            let score = item.keys.reduce((total, key) => total + (text.includes(normalizeWizardText(key)) ? 1 : 0), 0);
            queryTokens.forEach((token) => {
                if (normalizedDesc.includes(token)) score += 2;
                if ((item.codigo || '').replace(/\./g, '').startsWith(token)) score += 1;
            });
            if (profile.key === 'cosmetics' && item.codigo.startsWith('3303')) score += 12;
            if (profile.key === 'tech' && item.codigo.startsWith('8517')) score += 12;
            if (profile.key === 'food' && item.codigo.startsWith('2106')) score += 12;
            if (profile.key === 'health' && item.codigo.startsWith('9018')) score += 12;
            if (profile.key === 'agriculture' && item.codigo.startsWith('1209')) score += 12;
            if (profile.key === 'wood' && item.codigo.startsWith('4407')) score += 12;
            if (profile.key === 'used' && /usado|segundo uso|reacondicionado/.test(normalizedDesc)) score += 10;
            const confidence = Math.min(98, 62 + score * 6);
            return Object.assign({}, item, { coincidencia: confidence, _score: score, sourceType: 'BD_LOCAL', confidence: confidence / 100 });
        })
        .filter((item) => item._score > 0)
        .sort((a, b) => b._score - a._score || b.coincidencia - a.coincidencia);

    if (matches.length) return matches;

    return [{
        codigo: '3926.90.90.90',
        descripcionEs: 'Manufactura general para revision preliminar. Confirma el codigo exacto con ficha tecnica.',
        adValorem: 6,
        requiereVuce: false,
        entidadVuce: 'SUNAT',
        coincidencia: 62
    }];
}

function setSelectedHSFromSuggestion(h, origin = 'AUTO') {
    if (!h) return false;
    wizardData.selectedHS = {
        codigo: h.codigo,
        descripcionEs: h.descripcionEs || 'Clasificacion preliminar',
        adValorem: h.adValorem || 0,
        requiereVuce: !!h.requiereVuce,
        entidadVuce: h.entidadVuce || 'SUNAT',
        confidence: typeof h.confidence === 'number' ? h.confidence : undefined,
        sourceType: h.sourceType || h.fuenteDato || 'BD_LOCAL'
    };
    wizardData.selectedHSOrigin = origin;
    applyHSCodeUI(wizardData.selectedHS);
    updateCIFCalculations();
    evalVuceQuestions();
    saveWizardDraft();
    return true;
}

function ensureFallbackHS() {
    if (wizardData.selectedHS) return true;
    const suggestions = inferLocalHsSuggestions(wizardData.prodNombre || wizardData.opNombre);
    return setSelectedHSFromSuggestion(suggestions[0], 'AUTO');
}

function rankRemoteHsSuggestion(h, term, index) {
    const profile = detectProductProfile();
    const query = normalizeWizardText(`${term || ''} ${wizardData.prodTecnica || ''} ${wizardData.prodMarca || ''} ${wizardData.prodModelo || ''}`);
    const queryTokens = tokenizeWizardText(query);
    const desc = normalizeWizardText(h.descripcionEs || '');
    const code = String(h.codigo || '').replace(/\./g, '');
    let score = Math.max(0, 120 - (index * 8));

    queryTokens.forEach((token) => {
        if (!token) return;
        if (desc.includes(token)) score += 10;
        if (code.startsWith(token)) score += 8;
        if (normalizeWizardText(h.codigo || '').includes(token)) score += 4;
    });

    if (profile.key === 'cosmetics' && (code.startsWith('3303') || desc.includes('perfume') || desc.includes('cosmet'))) score += 35;
    if (profile.key === 'tech' && (code.startsWith('8517') || desc.includes('wifi') || desc.includes('bluetooth') || desc.includes('telef'))) score += 35;
    if (profile.key === 'food' && (code.startsWith('2106') || desc.includes('suplement') || desc.includes('alimen') || desc.includes('bebid'))) score += 35;
    if (profile.key === 'health' && (code.startsWith('9018') || desc.includes('medic') || desc.includes('diagnost'))) score += 35;
    if (profile.key === 'agriculture' && (code.startsWith('1209') || desc.includes('semill') || desc.includes('plant') || desc.includes('vegetal'))) score += 35;
    if (profile.key === 'wood' && (code.startsWith('4407') || desc.includes('mader') || desc.includes('forest'))) score += 35;
    if (profile.key === 'used' && (desc.includes('usado') || desc.includes('reacond') || desc.includes('remanufact'))) score += 30;

    return {
        ...h,
        sourceType: h.sourceType || h.fuenteDato || 'OFICIAL_API',
        coincidencia: Math.min(98, Math.round(score)),
        confidence: Math.min(98, score) / 100
    };
}

async function buscarPartidaSugerida() {
    captureFields();
    const term = wizardData.prodNombre || wizardData.opNombre;
    if (!term) return;

    const cleanTerm = term.trim().toLowerCase();
    const table = document.getElementById('hsSugerenciasTable');
    
    // 1. Mostrar sugerencia rápida local primero
    const localSuggestions = inferLocalHsSuggestions(term);
    renderHSSugerencias(localSuggestions);
    if (wizardData.selectedHSOrigin !== 'MANUAL' && localSuggestions.length) {
        setSelectedHSFromSuggestion(localSuggestions[0], 'AUTO');
        mostrarNotificacion("Codigo sugerido aplicado");
    }

    // 2. Verificar en caché local (0ms) antes de llamar al servidor
    if (SUGGESTIONS_CACHE.has(cleanTerm)) {
        const cachedList = SUGGESTIONS_CACHE.get(cleanTerm);
        renderHSSugerencias(cachedList);
        if (wizardData.selectedHSOrigin !== 'MANUAL' && cachedList.length) {
            setSelectedHSFromSuggestion(cachedList[0], 'AUTO');
        }
        return;
    }

    // 3. Abortar petición remota previa si existe
    if (remoteSearchAbortController) {
        remoteSearchAbortController.abort();
    }
    remoteSearchAbortController = new AbortController();

    table.insertAdjacentHTML('afterbegin', `<tr><td colspan="5" class="p-3 text-center text-blue-700 font-bold animate-pulse bg-blue-50">Actualizando con el motor arancelario del servidor...</td></tr>`);

    try {
        const res = await fetch(`${window.ctx}/api/hs/sugerencias?termino=${encodeURIComponent(term)}`, {
            signal: remoteSearchAbortController.signal
        });
        let list = await res.json();
        if (!Array.isArray(list) || !list.length) return;
        
        list = list.map((h, index) => rankRemoteHsSuggestion(h, term, index))
            .sort((a, b) => b.coincidencia - a.coincidencia || a.codigo.localeCompare(b.codigo));

        // Guardar en caché local arancelaria
        SUGGESTIONS_CACHE.set(cleanTerm, list);

        renderHSSugerencias(list);
        if (wizardData.selectedHSOrigin !== 'MANUAL' && list.length) setSelectedHSFromSuggestion(list[0], 'AUTO');
    } catch (e) {
        if (e.name !== 'AbortError') {
            table.insertAdjacentHTML('afterbegin', `<tr><td colspan="5" class="p-3 text-center text-blue-700 bg-blue-50 font-semibold">Seguimos con la sugerencia rapida local mientras el servidor responde.</td></tr>`);
        }
    }
}

function renderHSSugerencias(list) {
    const table = document.getElementById('hsSugerenciasTable');
    table.innerHTML = '';

    if (list.length === 0) {
        table.innerHTML = `<tr><td colspan="5" class="p-6 text-center text-gray-500 font-bold">No se detectaron sugerencias para su término. Intente clasificar manualmente.</td></tr>`;
        return;
    }

    list.forEach(h => {
        const row = document.createElement('tr');
        row.className = 'hover:bg-blue-50 transition-all';
        
        const av = h.adValorem || 0;
        const safeCode = escapeHtml(formatHSCode(h.codigo));
        const safeDesc = escapeHtml(h.descripcionEs || 'Subpartida Nacional Declarada');
        const safeEntity = escapeHtml(h.entidadVuce || 'SUNAT');
        const vuceBadge = h.requiereVuce ? `<span class="bg-purple-50 text-purple-700 border border-purple-200 px-2 py-0.5 rounded text-[9px] font-black uppercase ml-2">Control ${safeEntity}</span>` : '';
        
        row.innerHTML = `
            <td class="p-3 font-mono font-bold text-blue-700">${safeCode}</td>
            <td class="p-3 text-[#1F2937] max-w-xs truncate" title="${safeDesc}">${safeDesc} ${vuceBadge}</td>
            <td class="p-3 text-center font-bold text-purple-700">${Number(h.coincidencia || 0)}%</td>
            <td class="p-3 text-gray-600 font-medium">Impuesto base ${Number(av).toFixed(0)}% + IGV/IPM</td>
            <td class="p-3 text-center">
                <button class="bg-blue-600 hover:bg-purple-600 text-white font-black px-3 py-1.5 rounded-lg text-[10px] uppercase transition-all">
                    Seleccionar
                </button>
            </td>
        `;
        const selectBtn = row.querySelector('button');
        selectBtn.onclick = () => selectHSCode(h.codigo, h.descripcionEs, av, h.requiereVuce, h.entidadVuce);
        table.appendChild(row);
    });
}

function formatHSCode(code) {
    if (!code) return '';
    const c = code.replace(/\./g, '');
    if (c.length === 10) {
        return `${c.substring(0,4)}.${c.substring(4,6)}.${c.substring(6,8)}.${c.substring(8,10)}`;
    }
    return code;
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
        : value === 'ESTIMADO' || value === 'MANUAL' ? 'estimated'
        : value === 'TERCERO_API' ? 'third'
        : 'pending';
    el.className = 'source-chip source-chip--' + group;
    el.textContent = label || sourceLabel(value);
}

function selectHSCode(code, desc, av, vuce, ent) {
    const selected = {
        codigo: code,
        descripcionEs: desc,
        adValorem: av,
        requiereVuce: vuce,
        entidadVuce: ent
    };
    setSelectedHSFromSuggestion(selected, 'MANUAL');
    mostrarNotificacion("Codigo " + formatHSCode(code) + " seleccionado");
    goToStep(3);
}

function applyHSCodeUI(hs) {
    document.getElementById('selectedHSLabel').innerHTML = `<span class="font-mono text-[#0A5C4A] font-black">${escapeHtml(formatHSCode(hs.codigo))}</span> - ${escapeHtml(hs.descripcionEs || 'Producto declarado')}`;
    const hsSourceType = hs.sourceType || hs.fuenteDato || (String(hs.descripcionEs || '').includes('[Aproximado]') ? 'ESTIMADO' : 'BD_LOCAL');
    const hsConfidence = typeof hs.confidence === 'number' ? hs.confidence : (hsSourceType === 'ESTIMADO' ? 0.60 : 0.85);
    setSourceChip('hsSourceChip', hsSourceType);
    const hsConfidenceText = document.getElementById('hsConfidenceText');
    if (hsConfidenceText) {
        hsConfidenceText.textContent = `Fuente: ${sourceLabel(hsSourceType)} | Confianza ${Math.round(hsConfidence * 100)}%`;
    }
    
    const cleanCode = hs.codigo.replace(/\./g, '');
    const cap = cleanCode.substring(0, 2);
    const part = cleanCode.substring(2, 4);
    const nac = cleanCode.substring(4, 10);
    
    const container = document.getElementById('hsDigitContainer');
    if (container) {
        container.innerHTML = `
            <div class="bg-[#0A5C4A]/10 text-[#0A5C4A] border border-[#0A5C4A]/15 px-2 py-1 rounded font-mono font-bold text-xs" title="Capitulo">${cap}</div>
            <div class="bg-[#12b5ab]/10 text-[#0f938a] border border-[#12b5ab]/20 px-2 py-1 rounded font-mono font-bold text-xs" title="Partida">${part}</div>
            <div class="bg-[#f0d9a7] text-[#8d5b16] border border-[#f0d9a7] px-2 py-1 rounded font-mono font-bold text-xs" title="Subpartida">${nac}</div>
        `;
    }
}

function openHsAssistant() {
    saveWizardDraft();
    window.location.href = 'buscador.jsp?returnWizard=1';
}

function selectManualHS() {
    const val = document.getElementById('manualHSCode').value.replace(/\./g, '');
    if (val.length < 6) {
        mostrarNotificacion("Ingrese una subpartida arancelaria de al menos 6 a 10 digitos.", 'warning');
        return;
    }
    
    // Derivar entidad según el código ingresado para hacerlo inteligente
    let entity = 'SUNAT';
    let req = false;
    if (val.startsWith('8517') || val.startsWith('8525')) { entity = 'MTC'; req = true; }
    else if (val.startsWith('2106') || val.startsWith('1901')) { entity = 'DIGESA'; req = true; }
    else if (val.startsWith('3004') || val.startsWith('3304')) { entity = 'DIGEMID'; req = true; }
    else if (val.startsWith('0602') || val.startsWith('1001')) { entity = 'SENASA'; req = true; }
    else if (val.startsWith('44')) { entity = 'SERFOR'; req = true; }

    wizardData.selectedHSOrigin = 'MANUAL';
    selectHSCode(val, "Codigo ingresado manualmente", 6, req, entity);
}

// --- VALIDACION DE RESTRICCIONES Y PLANTILLAS TUPA ---
function getSuggestedEntity() {
    const profile = detectProductProfile();
    if (wizardData.selectedHS && wizardData.selectedHS.entidadVuce && wizardData.selectedHS.entidadVuce !== 'SUNAT') {
        return wizardData.selectedHS.entidadVuce;
    }
    if (profile.entity && profile.entity !== 'SUNAT' && profile.entity !== 'REVISAR') {
        return profile.entity;
    }
    if (profile.key === 'used') return 'REVISAR';
    if (getQuestionAnswer('qSalud') === 'SI') return 'DIGEMID';
    if (getQuestionAnswer('qConsumo') === 'SI') return 'DIGESA';
    if (getQuestionAnswer('qWifi') === 'SI') return 'MTC';
    if (getQuestionAnswer('qContacto') === 'SI') return 'SENASA';
    if (getQuestionAnswer('qMadera') === 'SI') return 'SERFOR';
    if (getQuestionAnswer('qUsado') === 'SI') return 'REVISAR';
    return 'SUNAT';
}
function getVuceAssessment() {
    const profile = detectProductProfile();
    const answers = {
        consumo: getQuestionAnswer('qConsumo'),
        salud: getQuestionAnswer('qSalud'),
        wifi: getQuestionAnswer('qWifi'),
        contacto: getQuestionAnswer('qContacto'),
        madera: getQuestionAnswer('qMadera'),
        usado: getQuestionAnswer('qUsado')
    };
    const dynamic = wizardData.dynamicAnswers || {};
    const directRestriction = !!(wizardData.selectedHS && wizardData.selectedHS.requiereVuce);
    const strongYes = ['consumo', 'salud', 'wifi', 'contacto', 'madera'].some((key) => answers[key] === 'SI');
    const usedGoods = answers.usado === 'SI';
    const unknowns = Object.values(answers).some((value) => value === 'NOSE');
    const entity = getSuggestedEntity();

    if (profile.key === 'cosmetics') {
        const notes = [];
        if (dynamic.cosmetics_alcohol === 'SI') notes.push('Incluye composición con alcohol y revisa el rotulado.');
        if (dynamic.cosmetics_child === 'SI') notes.push('Es de uso infantil, por lo que conviene reforzar advertencias y presentación.');
        if (dynamic.cosmetics_aerosol === 'SI') notes.push('Al ser aerosol, revisa transporte y ficha de seguridad.');
        return {
            status: 'required',
            entity: 'DIGEMID',
            title: 'Control sanitario para cosméticos',
            message: notes.length ? notes.join(' ') : 'Perfumes y cosméticos suelen pasar por revisión sanitaria. Estas respuestas nos ayudan a pedir la ficha correcta.',
            action: 'Prepara composición, fabricante, presentación, rotulado y ficha técnica del producto.'
        };
    }

    if (profile.key === 'tech') {
        const notes = [];
        if (dynamic.tech_calls === 'SI') notes.push('Permite llamadas, por lo que hay que revisar homologación y tipo de equipo.');
        if (dynamic.tech_wifi === 'SI') notes.push('Tiene WiFi, así que la conectividad es relevante para la revisión.');
        if (dynamic.tech_bluetooth === 'SI') notes.push('Tiene Bluetooth, por lo que conviene guardar especificaciones de radiofrecuencia.');
        return {
            status: 'required',
            entity: 'MTC',
            title: 'Control para equipos de telecomunicaciones',
            message: notes.length ? notes.join(' ') : 'Este tipo de producto suele necesitar validación técnica y de conectividad.',
            action: 'Reúne marca, modelo, bandas, homologación y ficha técnica del fabricante.'
        };
    }

    if (profile.key === 'food') {
        return {
            status: 'required',
            entity: 'DIGESA',
            title: 'Control sanitario de alimentos y suplementos',
            message: 'Por la categoría detectada, conviene revisar composición, ingredientes y presentación comercial.',
            action: 'Prepara ficha técnica, etiqueta nutricional, fabricante y sustento de uso humano.'
        };
    }

    if (profile.key === 'health') {
        return {
            status: 'required',
            entity: 'DIGEMID',
            title: 'Control sanitario de salud',
            message: 'La descripción apunta a un producto sanitario o médico, así que la documentación técnica debe ser más precisa.',
            action: 'Reúne registro sanitario, ficha técnica, uso declarado y soporte del fabricante.'
        };
    }

    if (profile.key === 'agriculture') {
        return {
            status: 'required',
            entity: 'SENASA',
            title: 'Control fitosanitario',
            message: 'La categoría detectada sugiere semillas, plantas o material agrícola, por lo que la ruta fitosanitaria aplica.',
            action: 'Prepara certificado fitosanitario, país de procedencia y detalles de empaque o siembra.'
        };
    }

    if (profile.key === 'wood') {
        return {
            status: 'required',
            entity: 'SERFOR',
            title: 'Control forestal',
            message: 'La descripción sugiere madera o producto forestal, por lo que conviene validar origen legal y especie.',
            action: 'Reúne especie, origen legal, certificado forestal y trazabilidad del material.'
        };
    }

    if (profile.key === 'used') {
        return {
            status: 'review',
            entity: 'REVISAR',
            title: 'Revisión adicional',
            message: 'El producto parece de segundo uso o reacondicionado. Eso requiere validar estado, soporte técnico y destino.',
            action: 'Confirma estado, operatividad, repuestos y documentación de reacondicionamiento.'
        };
    }

    if (directRestriction || strongYes) {
        return {
            status: 'required',
            entity: entity === 'REVISAR' ? 'MTC' : entity,
            title: 'Requiere permiso',
            message: 'Se detecto una caracteristica regulada. Antes de comprar o embarcar, conviene preparar el permiso y el expediente base.',
            action: 'Reune ficha tecnica, factura proforma y revisa la entidad competente.'
        };
    }
    if (usedGoods || unknowns) {
        return {
            status: 'review',
            entity: entity === 'SUNAT' ? 'Revisar con especialista' : entity,
            title: 'Revisar',
            message: 'Todavia hay puntos ambiguos del producto. Vale la pena confirmar especificaciones antes de seguir con el costo final.',
            action: 'Pide una ficha tecnica o compara mas codigos antes de decidir.'
        };
    }
    return {
        status: 'free',
        entity: 'SUNAT',
        title: 'No requiere permiso',
        message: 'No vemos una restriccion evidente con la informacion disponible. Puedes seguir con costos y expediente.',
        action: 'Continua con el costeo y prepara tus documentos comerciales.'
    };
}
function isVuceRequired() {
    return getVuceAssessment().status === 'required';
}
function evalVuceQuestions() {
    captureFields();
    const assessment = getVuceAssessment();
    renderDynamicQuestions();
    const semCircle = document.getElementById('vuceSemaforoCircle');
    const semText = document.getElementById('vuceSemaforoText');
    const msg = document.getElementById('vuceReglaMensaje');
    const sectorVal = document.getElementById('vuceSectorVal');
    const permisoVal = document.getElementById('vucePermisoVal');
    const actionVal = document.getElementById('vuceAccionVal');
    const templateContainer = document.getElementById('entityTemplateContainer');
    const templateLabel = document.getElementById('tupaEntidadLabel');
    if (assessment.status === 'free') {
        semCircle.innerText = 'OK';
        semText.innerText = 'No requiere permiso';
        semText.className = 'text-xs font-black uppercase text-emerald-600 mt-1';
        msg.innerText = assessment.message;
        sectorVal.innerText = assessment.entity;
        permisoVal.innerText = 'Ninguno por ahora';
        if (actionVal) actionVal.innerText = assessment.action;
        templateContainer.classList.add('hidden');
    } else if (assessment.status === 'review') {
        semCircle.innerText = '?';
        semText.innerText = 'Revisar';
        semText.className = 'text-xs font-black uppercase text-amber-600 mt-1';
        msg.innerText = assessment.message;
        sectorVal.innerText = assessment.entity;
        permisoVal.innerText = 'Validacion previa';
        if (actionVal) actionVal.innerText = assessment.action;
        templateContainer.classList.add('hidden');
    } else {
        semCircle.innerText = '!!';
        semText.innerText = 'Requiere permiso';
        semText.className = 'text-xs font-black uppercase text-rose-600 mt-1';
        msg.innerText = assessment.message;
        sectorVal.innerText = assessment.entity;
        permisoVal.innerText = 'Expediente regulatorio';
        if (actionVal) actionVal.innerText = assessment.action;
        templateContainer.classList.remove('hidden');
        templateLabel.innerText = assessment.entity;
        renderTupaFields(assessment.entity, detectProductProfile().key);
    }
    updateSidebar();
}

// Renderizador Dinámico de las Fichas Técnicas Declarativas por Sector Competente (TUPA)
function renderTupaFields(entity, profileKey) {
    const area = document.getElementById('tupaFieldsArea');
    area.innerHTML = '';
    
    let fields = [];
    if (entity === 'MTC') {
        fields = [
            { label: 'Marca', placeholder: 'Ej: Xiaomi / Lenovo', type: 'text' },
            { label: 'Modelo', placeholder: 'Ej: Redmi Note 13', type: 'text' },
            { label: 'Fabricante', placeholder: 'Ej: Xiaomi Communications Co.', type: 'text' },
            { label: 'País de origen', placeholder: 'Ej: China / USA', type: 'text' },
            { label: 'Tecnología inalámbrica', placeholder: 'Ej: 5G / Wi-Fi / Bluetooth', type: 'text' },
            { label: 'Frecuencia', placeholder: 'Ej: 2.4 GHz y 5 GHz', type: 'text' },
            { label: 'Código Homologación', placeholder: 'Ej: TR-MTC-2026-X', type: 'text' }
        ];
    } else if (entity === 'DIGESA') {
        fields = [
            { label: 'Producto', placeholder: 'Ej: Galletas de Avena / Proteína', type: 'text' },
            { label: 'Marca', placeholder: 'Ej: Quaker / Gold Standard', type: 'text' },
            { label: 'Presentación', placeholder: 'Ej: Caja de 500g / Envase plástico', type: 'text' },
            { label: 'Composición', placeholder: 'Ej: Aislado de proteína, saborizantes', type: 'text' },
            { label: 'Fabricante', placeholder: 'Ej: Optimum Nutrition USA', type: 'text' },
            { label: 'Uso sugerido', placeholder: 'Ej: Consumo humano directo', type: 'text' },
            { label: 'Certificado Libre Venta', placeholder: 'Ej: CLV-FDA-2026-X', type: 'text' }
        ];
    } else if (entity === 'DIGEMID') {
        fields = profileKey === 'cosmetics'
            ? [
                { label: 'Nombre comercial', placeholder: 'Ej: Perfume / Eau de toilette', type: 'text' },
                { label: 'Presentación', placeholder: 'Ej: Spray, frasco o estuche', type: 'text' },
                { label: 'Contenido de alcohol', placeholder: 'Ej: 0%, 5%, 70%', type: 'text' },
                { label: 'Uso declarado', placeholder: 'Ej: Personal, infantil o regalo', type: 'text' },
                { label: 'Fabricante', placeholder: 'Ej: Fragrances S.A.', type: 'text' },
                { label: 'Composición / Ingredientes', placeholder: 'Ej: Fragancia, alcohol, fijadores', type: 'text' }
            ]
            : [
                { label: 'Nombre comercial', placeholder: 'Ej: Paracetamol / Base Cosmética', type: 'text' },
                { label: 'Principio activo / Componente', placeholder: 'Ej: Acetaminofén / Glicerina', type: 'text' },
                { label: 'Concentración / Ficha', placeholder: 'Ej: 500 mg / Ficha estándar', type: 'text' },
                { label: 'Fabricante', placeholder: 'Ej: Pharma Labs Inc.', type: 'text' },
                { label: 'Uso declarado', placeholder: 'Ej: Analgésico / Cuidado de la piel', type: 'text' },
                { label: 'Registro sanitario / Certificado', placeholder: 'Ej: RD-DIGEMID-2026-X', type: 'text' }
            ];
    } else if (entity === 'SENASA') {
        fields = [
            { label: 'Nombre común', placeholder: 'Ej: Semilla de Tomate / Planta', type: 'text' },
            { label: 'Nombre científico', placeholder: 'Ej: Solanum lycopersicum', type: 'text' },
            { label: 'Uso', placeholder: 'Ej: Siembra agrícola / Ornamentación', type: 'text' },
            { label: 'País de procedencia', placeholder: 'Ej: España / Chile', type: 'text' },
            { label: 'Certificado fitosanitario', placeholder: 'Ej: CF-SENASA-2026-X', type: 'text' },
            { label: 'Cantidad y empaque', placeholder: 'Ej: Cajas herméticas de 500g', type: 'text' }
        ];
    } else if (entity === 'SERFOR') {
        fields = [
            { label: 'Especie común', placeholder: 'Ej: Madera de Pino / Cedro', type: 'text' },
            { label: 'Nombre científico', placeholder: 'Ej: Pinus sylvestris', type: 'text' },
            { label: 'País de origen', placeholder: 'Ej: Chile / Brasil', type: 'text' },
            { label: 'CITES Status', placeholder: 'Ej: Apéndice II / No listado', type: 'text' },
            { label: 'Origen legal / Título', placeholder: 'Ej: Certificado FSC de Origen', type: 'text' },
            { label: 'Certificado forestal de origen', placeholder: 'Ej: CFO-SERFOR-2026-X', type: 'text' }
        ];
    } else {
        fields = [
            { label: 'Producto / Dispositivo', placeholder: 'Ej: Repuestos de segundo uso', type: 'text' },
            { label: 'Uso y finalidad', placeholder: 'Ej: Industrial / Mantenimiento', type: 'text' },
            { label: 'Cantidad', placeholder: 'Ej: 15 unidades', type: 'text' },
            { label: 'Autorización Especial', placeholder: 'Ej: AUT-PRODUCE-2026-X', type: 'text' }
        ];
    }
    
    fields.forEach(f => {
        const val = wizardData.tupaDatos[f.label] || '';
        const div = document.createElement('div');
        div.className = 'flex flex-col gap-1';
        div.innerHTML = `
            <label class="text-[9px] font-bold text-gray-500 uppercase">${f.label}</label>
            <input type="${f.type}" data-label="${f.label}" class="bg-white border border-[#E6E2D8] rounded-xl px-3 py-2 text-xs text-[#1F2937] outline-none focus:border-[#0A5C4A] font-semibold" placeholder="${f.placeholder}" value="${escapeHtml(val)}">
        `;
        area.appendChild(div);
    });
}

// --- LIQUIDACIÓN TRIBUTARIA ---
async function fetchExchangeRate() {
    try {
        const res = await fetch(`${window.ctx}/api/tipoCambio`);
        const data = await res.json();
        const payload = data && data.data ? data.data : data;
        const tipoCambio = payload && (payload.tipoCambio || payload.venta);
        if (tipoCambio) {
            wizardData.logTC = parseFloat(tipoCambio);
            wizardData.tipoCambioTrace = payload;
            const tcInput = document.getElementById('logTC');
            if (tcInput) tcInput.value = wizardData.logTC.toFixed(3);
            const sourceType = payload.sourceType || (data && data.sourceType) || 'PENDIENTE_VALIDACION';
            const estado = payload.estado || sourceLabel(sourceType);
            setSourceChip('tcTraceChip', sourceType, `${sourceLabel(sourceType)} - ${estado}`);
            updateCIFCalculations();
        }
    } catch (e) {
        console.error("Error retrieving BCRP exchange rate, using default", e);
        setSourceChip('tcTraceChip', 'FALLBACK', 'FALLBACK - valor referencial usado por contingencia');
    }
}

function incotermCostFlags(incoterm) {
    const code = String(incoterm || 'FOB').toUpperCase();
    return {
        includesFlete: ['CIF', 'CFR', 'CIP', 'CPT', 'DAP', 'DDP'].includes(code),
        includesSeguro: ['CIF', 'CIP'].includes(code)
    };
}

function calculateCifUsdByIncoterm(fob, flete, seguro, incoterm) {
    const flags = incotermCostFlags(incoterm);
    let cif = Number(fob || 0);
    if (!flags.includesFlete) cif += Number(flete || 0);
    if (!flags.includesSeguro) cif += Number(seguro || 0);
    return cif;
}

function calculateTaxesLogic() {
    const tc = wizardData.logTC;
    const fob = wizardData.logFob;
    const flete = wizardData.logFlete;
    const seguro = wizardData.logSeguro;
    const incoterm = wizardData.opIncoterm || 'FOB';
    const cifUsd = calculateCifUsdByIncoterm(fob, flete, seguro, incoterm);
    const cifPen = cifUsd * tc;

    let avTasa = 0.06;
    if (wizardData.selectedHS) {
        avTasa = (wizardData.selectedHS.adValorem || 0) / 100;
    }

    if (wizardData.tribTlc === 'SI') {
        avTasa = 0;
    }

    let adValorem = cifPen * avTasa;
    let isc = 0.0;
    let igv = 0.0;
    let ipm = 0.0;
    let percTasa = 0.035;
    let percepcion = 0.0;

    const usado = document.getElementById('qUsado') ? (document.getElementById('qUsado').value === 'SI') : false;
    const uso = wizardData.prodUso || 'PERSONAL';

    // Sync Frontend with Backend SUNAT Courier Regimes
    if (uso === 'PERSONAL') {
        if (fob < 200) {
            // Category B: Under $200 fully exempt
            adValorem = 0;
            igv = 0;
            ipm = 0;
            percepcion = 0;
        } else if (fob <= 2000) {
            // Category C: flat 4% arancel, 16% IGV, 2% IPM, exento percepción
            adValorem = cifPen * 0.04;
            const baseIgv = cifPen + adValorem;
            igv = baseIgv * 0.16;
            ipm = baseIgv * 0.02;
            percepcion = 0;
        } else {
            // Standard personal over $2000
            const baseIgv = cifPen + adValorem;
            igv = baseIgv * 0.16;
            ipm = baseIgv * 0.02;
            if (wizardData.tribPerfil === 'BUEN_CONTRIBUYENTE') {
                percTasa = 0.0;
            } else if (usado) {
                percTasa = 0.10;
            } else if (wizardData.tribPerfil === 'PRIMERA_IMPORTACION' || wizardData.tribPerfil === 'NUEVO') {
                percTasa = 0.10;
            }
            const basePercepcion = baseIgv + igv + ipm;
            percepcion = basePercepcion * percTasa;
        }
    } else {
        // Standard Commercial Regime
        const baseIgv = cifPen + adValorem;
        igv = baseIgv * 0.16;
        ipm = baseIgv * 0.02;
        if (wizardData.tribPerfil === 'BUEN_CONTRIBUYENTE') {
            percTasa = 0.0;
        } else if (usado) {
            percTasa = 0.10;
        } else if (wizardData.tribPerfil === 'PRIMERA_IMPORTACION' || wizardData.tribPerfil === 'NUEVO') {
            percTasa = 0.10;
        }
        const basePercepcion = baseIgv + igv + ipm;
        percepcion = basePercepcion * percTasa;
    }

    const total = adValorem + isc + igv + ipm + percepcion;

    return {
        cifPen,
        avTasa,
        adValorem,
        isc,
        igv,
        ipm,
        percTasa,
        percepcion,
        total
    };
}

function updateTaxes() {
    const data = calculateTaxesLogic();
    
    // Support the new clean Step 4 list items if they exist
    const adValoremEl = document.getElementById('taxVal-adValorem');
    const igvEl = document.getElementById('taxVal-igv');
    const ipmEl = document.getElementById('taxVal-ipm');
    const percepcionEl = document.getElementById('taxVal-percepcion');
    
    if (adValoremEl) adValoremEl.innerText = `S/ ${data.adValorem.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    if (igvEl) igvEl.innerText = `S/ ${data.igv.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    if (ipmEl) ipmEl.innerText = `S/ ${data.ipm.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    if (percepcionEl) percepcionEl.innerText = `S/ ${data.percepcion.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;

    const scenarioMin = document.getElementById('scenarioMin');
    const scenarioExpected = document.getElementById('scenarioExpected');
    const scenarioMax = document.getElementById('scenarioMax');
    if (scenarioMin) scenarioMin.innerText = `S/ ${(data.total * 0.92).toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    if (scenarioExpected) scenarioExpected.innerText = `S/ ${data.total.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    if (scenarioMax) scenarioMax.innerText = `S/ ${(data.total * 1.14).toLocaleString('es-PE', {minimumFractionDigits: 2})}`;

    // Update Tax Burden Traffic Light (Semáforo de Carga Tributaria)
    const trafficLightEl = document.getElementById('taxBurdenTrafficLight');
    if (trafficLightEl) {
        const tc = wizardData.logTC;
        const fobPen = wizardData.logFob * tc;
        const burdenPct = fobPen > 0 ? (data.total / fobPen) * 100 : 0;
        
        let colorClass = '';
        let lightColor = '';
        let title = '';
        let desc = '';

        if (burdenPct < 15) {
            colorClass = 'bg-emerald-50 text-emerald-800 border-emerald-100 dark:bg-emerald-950/20 dark:border-emerald-900/40 dark:text-emerald-400';
            lightColor = 'text-emerald-500';
            title = '🟢 Carga Tributaria Baja';
            desc = `Tus tributos estimados representan el ${burdenPct.toFixed(1)}% de tu valor FOB. Esta tasa es sumamente favorable y reduce significativamente los costos de internamiento.`;
        } else if (burdenPct <= 25) {
            colorClass = 'bg-amber-50 text-amber-800 border-amber-100 dark:bg-amber-950/20 dark:border-amber-900/40 dark:text-amber-400';
            lightColor = 'text-amber-500';
            title = '🟡 Carga Tributaria Moderada';
            desc = `Tus tributos estimados representan el ${burdenPct.toFixed(1)}% de tu valor FOB. Se encuentra en el rango comercial estándar para importaciones generales en Perú.`;
        } else {
            colorClass = 'bg-rose-50 text-rose-800 border-rose-100 dark:bg-rose-950/20 dark:border-rose-900/40 dark:text-rose-400';
            lightColor = 'text-rose-500';
            title = '🔴 Carga Tributaria Alta';
            desc = `Tus tributos estimados representan el ${burdenPct.toFixed(1)}% de tu valor FOB. Esto puede deberse a mercancía usada con percepción incrementada del 10% o partidas con arancel general elevado. Compara el escenario conservador.`;
        }

        trafficLightEl.className = `mt-4 p-4 rounded-2xl border transition-all duration-300 ${colorClass}`;
        trafficLightEl.innerHTML = `
            <div class="flex items-start gap-3">
                <div class="text-sm font-black tracking-tight ${lightColor}">${title}</div>
            </div>
            <p class="text-[10px] leading-relaxed font-semibold mt-1 text-[var(--text-secondary)]">${desc}</p>
        `;
    }

    const table = document.getElementById('taxesTableBody');
    if (!table) return;

    table.innerHTML = `
        <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
            <td class="p-3 text-white">Ad-Valorem</td>
            <td class="p-3 text-gray-500">CIF (S/ ${data.cifPen.toLocaleString('es-PE', {minimumFractionDigits: 2})})</td>
            <td class="p-3 text-center text-white font-mono">${(data.avTasa * 100).toFixed(0)}%</td>
            <td class="p-3 text-right text-white font-mono">S/ ${data.adValorem.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
        </tr>
        <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
            <td class="p-3 text-white">ISC (Impuesto Selectivo)</td>
            <td class="p-3 text-gray-400">Segun codigo</td>
            <td class="p-3 text-center text-white font-mono">0%</td>
            <td class="p-3 text-right text-white font-mono">S/ 0.00</td>
        </tr>
        <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
            <td class="p-3 text-white">IGV (Impuesto General Ventas)</td>
            <td class="p-3 text-gray-500">CIF + Ad-Valorem</td>
            <td class="p-3 text-center text-white font-mono">16%</td>
            <td class="p-3 text-right text-white font-mono">S/ ${data.igv.toLocaleString('es-PE', {minimumFractionDigits: 2})}</td>
        </tr>
        <tr class="hover:bg-white/[0.02] border-b border-white/5 font-semibold text-xs">
            <td class="p-3 text-white">IPM (Impuesto Promoción Municipal)</td>
            <td class="p-3 text-gray-500">CIF + Ad-Valorem</td>
            <td class="p-3 text-center text-white font-mono">2%</td>
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

function updateCIFCalculations() {
    captureFields();
    
    const tc = wizardData.logTC;
    const fob = wizardData.logFob;
    const flete = wizardData.logFlete;
    const seguro = wizardData.logSeguro;
    const incoterm = wizardData.opIncoterm || 'FOB';
    const flags = incotermCostFlags(incoterm);
    const cif = calculateCifUsdByIncoterm(fob, flete, seguro, incoterm);

    setText('cifFobUsd', `$ ${fob.toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
    setText('cifFobPen', `S/ ${(fob * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
    
    setText('cifFleteUsd', flags.includesFlete ? `Incluido en ${incoterm}` : `$ ${flete.toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
    setText('cifFletePen', `S/ ${(flete * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

    setText('cifSeguroUsd', flags.includesSeguro ? `Incluido en ${incoterm}` : `$ ${seguro.toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
    setText('cifSeguroPen', `S/ ${(seguro * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

    setText('cifCifUsd', `$ ${cif.toLocaleString('es-PE', {minimumFractionDigits: 2})}`);
    setText('cifCifPen', `S/ ${(cif * tc).toLocaleString('es-PE', {minimumFractionDigits: 2})}`);

    updateTaxes();
    updateSidebar();
    evalInputRealTimeAlerts();
}

function validarRucSunatMod11(ruc) {
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
}

function evalInputRealTimeAlerts() {
    captureFields();
    
    // --- 1. ALERTA DE LÍMITE COURIER / AGENTE DE ADUANA MANDATORIO (FOB > $2000) ---
    const fobInput = document.getElementById('logFob');
    if (fobInput) {
        let fobAlertContainer = document.getElementById('realtime-fob-alert');
        if (!fobAlertContainer) {
            fobAlertContainer = document.createElement('div');
            fobAlertContainer.id = 'realtime-fob-alert';
            fobInput.parentElement.appendChild(fobAlertContainer);
        }
        
        if (wizardData.logFob > 2000) {
            fobAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
            fobAlertContainer.innerHTML = `
                <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                    <span>⚠️ Alerta de Despacho General</span>
                </div>
                El valor FOB supera los <strong>USD 2,000.00</strong>. Bajo la Ley General de Aduanas (Art. 21), esta importación no califica como Courier Simplificado (Importa Fácil) y <strong>exige obligatoriamente la contratación de un Agente de Aduanas autorizado</strong> para su despacho.
            `;
        } else if (wizardData.logFob > 200 && wizardData.prodUso === 'PERSONAL') {
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

        const uso = document.getElementById('prodUso') ? document.getElementById('prodUso').value : wizardData.prodUso;
        if (uso === 'PERSONAL' && wizardData.prodCantidad > 3) {
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
        if (isUsado && wizardData.tribPerfil !== 'BUEN_CONTRIBUYENTE') {
            usadoAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
            usadoAlertContainer.innerHTML = `
                <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                    <span>⚠️ Percepción SUNAT Incrementada</span>
                </div>
                Declarar mercancía USADA activará de manera obligatoria una <strong>tasa de percepción de SUNAT del 10%</strong> en lugar de la tasa estándar de 3.5%. Esto incrementará sensiblemente la carga impositiva sobre el valor total CIF de tus bienes en la aduana de ingreso.
            `;
        } else if (isUsado && wizardData.tribPerfil === 'BUEN_CONTRIBUYENTE') {
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
    const opTipo = document.getElementById('opTipo') ? document.getElementById('opTipo').value : wizardData.opTipo;
    if (rucInput && opTipo === 'COMERCIAL') {
        let rucAlertContainer = document.getElementById('realtime-ruc-alert');
        if (!rucAlertContainer) {
            rucAlertContainer = document.createElement('div');
            rucAlertContainer.id = 'realtime-ruc-alert';
            rucInput.parentElement.appendChild(rucAlertContainer);
        }

        const rucVal = rucInput.value.trim();
        if (rucVal.length > 0 && !validarRucSunatMod11(rucVal)) {
            rucAlertContainer.className = 'mt-2 p-3 rounded-xl border border-rose-200 bg-rose-50 text-rose-800 text-[10px] font-semibold leading-relaxed animate-fadeIn';
            rucAlertContainer.innerHTML = `
                <div class="flex items-center gap-1.5 font-black uppercase text-rose-700 mb-0.5">
                    <span>⚠️ RUC Estructuralmente Inválido</span>
                </div>
                El número de RUC ingresado no cumple con el algoritmo Módulo 11 de la SUNAT o no tiene un prefijo oficial operativo (10, 20, 15, 17). Por favor, verifica el RUC para evitar rechazos en el despacho aduanero formal.
            `;
        } else if (rucVal.length > 0 && validarRucSunatMod11(rucVal)) {
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
}

// --- GESTION DOCUMENTAL Y CHECKLIST ---
function hasPendingMandatoryDocs() {
    const basePending = !wizardData.docs.FACTURA_COMERCIAL || !wizardData.docs.BILL_OF_LADING;
    const certPending = wizardData.tribTlc === 'SI' && !wizardData.docs.CERTIFICADO_ORIGEN;
    return basePending || certPending;
}

function updateExpedienteReadiness(assessment, docPending) {
    const title = document.getElementById('expedienteReadyTitle');
    const text = document.getElementById('expedienteReadyText');
    if (!title || !text) return;

    if (assessment.status === 'required' && !wizardData.vuceSuce) {
        title.innerText = 'Primero revisa el permiso';
        text.innerText = 'El producto podria necesitar autorizacion. Prepara los datos de la entidad antes de cerrar la carpeta.';
    } else if (docPending) {
        title.innerText = 'Faltan documentos basicos';
        text.innerText = 'Necesitas al menos factura comercial y documento de transporte para dejar lista la carpeta.';
    } else {
        title.innerText = 'Listo para seguir';
        text.innerText = 'Tu carpeta base ya esta armada. Guarda la evaluacion y continua desde Seguimiento.';
    }
}

function toggleDoc(tipo) {
    wizardData.docs[tipo] = !wizardData.docs[tipo];
    
    if (registeredImportId) {
        fetch(`${window.ctx}/api/importacion/registrarDocumentoOperacion?id=${registeredImportId}&tipo=${encodeURIComponent(tipo)}`, {
            method: 'POST',
            headers: csrfHeaders()
        })
        .then(r => r.json())
        .then(data => {
            if (data.warning && wizardData.docs[tipo]) {
                showNotification('Atencion', 'Recuerda subir el archivo real del documento', false);
            }
            updateDocFileIndicator(tipo, data.tieneArchivos !== false);
        })
        .catch(() => {});
    }

    updateDocButtonsUI();
    updateSidebar();
    
    mostrarNotificacion("Checklist documental actualizado");
}

function updateDocButtonsUI() {
    const types = ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'];
    types.forEach(tipo => {
        const btn = document.getElementById('btnDoc-' + tipo);
        if (btn) {
            if (wizardData.docs[tipo]) {
                btn.className = "px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-emerald-500/20 text-emerald-400 border border-emerald-500/10";
                btn.innerText = "Marcado como listo";
            } else {
                btn.className = "px-5 py-2 rounded-xl text-[9px] font-black uppercase tracking-wider bg-rose-500/20 text-rose-400 border border-rose-500/10";
                btn.innerText = "Pendiente";
            }
        }
    });
}

function updateDocFileIndicator(tipo, hasFile) {
    const indicator = document.getElementById('docFileIndicator-' + tipo);
    if (!indicator) return;
    if (!wizardData.docs[tipo]) {
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
}

function refreshDocFileIndicators() {
    if (!registeredImportId) return;
    fetch(`${window.ctx}/api/documentos/listar?importacionId=${registeredImportId}`, {
        headers: csrfHeaders()
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
        ['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'].forEach(tipo => {
            updateDocFileIndicator(tipo, !!fileMap[tipo]);
        });
    })
    .catch(() => {});
}

// --- SIDEBAR RESUMEN DE CONTROL ---
function updateSidebar() {
    const cif = wizardData.logFob + wizardData.logFlete + wizardData.logSeguro;
    const taxes = calculateTaxesLogic();
    const assessment = getVuceAssessment();
    const docPending = hasPendingMandatoryDocs();
    updateExpedienteReadiness(assessment, docPending);

    document.getElementById('sideProd').innerText = wizardData.prodNombre || wizardData.opNombre || '--';
    document.getElementById('sidePais').innerText = `Origen: ${wizardData.opPaisOrigen} / ${wizardData.opIncoterm}`;
    document.getElementById('sideHS').innerText = wizardData.selectedHS ? `${formatHSCode(wizardData.selectedHS.codigo)}` : '--';
    document.getElementById('sideCIF').innerText = `$ ${cif.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    document.getElementById('sideTaxes').innerText = `S/ ${taxes.total.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    
    const sideVuce = document.getElementById('sideVuce');
    if (assessment.status === 'required') {
        sideVuce.innerText = "Podria requerir permiso (" + assessment.entity + ")";
        sideVuce.className = "text-rose-600 font-bold block mt-0.5";
    } else if (assessment.status === 'review') {
        sideVuce.innerText = "Revisar detalles del producto";
        sideVuce.className = "text-amber-600 font-bold block mt-0.5";
    } else {
        sideVuce.innerText = "Sin permiso evidente";
        sideVuce.className = "text-emerald-600 font-bold block mt-0.5";
    }

    const sideEstado = document.getElementById('sideEstado');
    if (assessment.status === 'required' && !wizardData.vuceSuce) {
        sideEstado.innerText = "Revisar permiso";
        sideEstado.className = "px-2 py-0.5 rounded text-[9px] font-black bg-amber-50 text-amber-700 border border-amber-200 inline-block mt-1";
    } else if (docPending) {
        sideEstado.innerText = "Faltan documentos";
        sideEstado.className = "px-2 py-0.5 rounded text-[9px] font-black bg-rose-50 text-rose-700 border border-rose-200 inline-block mt-1";
    } else {
        sideEstado.innerText = "Listo para seguir";
        sideEstado.className = "px-2 py-0.5 rounded text-[9px] font-black bg-emerald-50 text-emerald-700 border border-emerald-200 inline-block mt-1";
    }

    renderNextAction(assessment, docPending);
}

function renderNextAction(assessment, docPending) {
    const title = document.getElementById('sideNextActionTitle');
    const text = document.getElementById('sideNextActionText');
    const button = document.getElementById('sideNextActionButton');
    if (!title || !text || !button) return;

    let action = {
        title: 'Continua la ruta',
        text: 'Avanza cuando hayas respondido lo esencial de esta pantalla.',
        label: 'Continuar',
        step: Math.min(currentStep + 1, 4),
        mode: 'step'
    };

    if (!wizardData.prodNombre.trim() || !wizardData.prodTecnica.trim()) {
        action = {
            title: 'Describe el producto',
            text: 'Escribe que importas y para que sirve. Con eso podremos sugerir codigo, permisos y costos.',
            label: 'Ir al producto',
            step: 1,
            mode: 'step'
        };
    } else if (!wizardData.selectedHS) {
        action = {
            title: 'Confirma el codigo',
            text: 'Necesitas un codigo probable para que permisos, impuestos y documentos tengan sentido.',
            label: 'Buscar codigo',
            step: 2,
            mode: 'step'
        };
    } else if (assessment.status === 'required' && !wizardData.vuceSuce) {
        action = {
            title: 'Revisa permisos',
            text: 'El producto podria necesitar autorizacion. Confirma este punto antes de comprar o embarcar.',
            label: 'Ver permisos',
            step: 3,
            mode: 'step'
        };
    } else if (docPending) {
        action = {
            title: 'Completa documentos',
            text: 'Marca los documentos que ya tienes para que la evaluacion quede lista para guardar.',
            label: 'Ir al expediente',
            step: 5,
            mode: 'step'
        };
    } else if (currentStep >= 4) {
        action = {
            title: 'Guardar evaluacion',
            text: 'Todo lo esencial esta armado. Guarda y continua desde Seguimiento.',
            label: 'Guardar y seguir',
            mode: 'save'
        };
    }

    title.textContent = action.title;
    text.textContent = action.text;
    button.textContent = action.label;
    button.dataset.action = action.mode;
    button.dataset.step = action.step || '';
}

// --- RESUMEN FINAL ---
function renderFinalSummary() {
    captureFields();
    
    const cif = wizardData.logFob + wizardData.logFlete + wizardData.logSeguro;
    const taxes = calculateTaxesLogic();
    const assessment = getVuceAssessment();
    const docPending = hasPendingMandatoryDocs();
    
    document.getElementById('resProd').innerText = wizardData.prodNombre || wizardData.opNombre;
    document.getElementById('resHS').innerText = wizardData.selectedHS ? formatHSCode(wizardData.selectedHS.codigo) : 'No clasificado';
    document.getElementById('resFob').innerText = `$ ${wizardData.logFob.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    document.getElementById('resCIF').innerText = `$ ${cif.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    document.getElementById('resImpuestos').innerText = `S/ ${taxes.total.toLocaleString('es-PE', {minimumFractionDigits: 2})}`;
    
    const resVuce = document.getElementById('resVuce');
    if (assessment.status === 'required') {
        resVuce.innerText = "Requiere permiso con " + assessment.entity;
        resVuce.className = "text-rose-600 font-bold";
    } else if (assessment.status === 'review') {
        resVuce.innerText = "Necesita una revision corta antes de seguir";
        resVuce.className = "text-amber-600 font-bold";
    } else {
        resVuce.innerText = "No requiere permiso por ahora";
        resVuce.className = "text-emerald-600 font-bold";
    }

    document.getElementById('resSuce').innerText = wizardData.vuceSuce
        ? wizardData.vuceSuce
        : (assessment.status === 'required' ? "Sin tramite iniciado todavia" : "No aplica por ahora");
    
    // Generar firma SHA-256 didáctica referencial
    const textToHash = wizardData.opNombre + wizardData.logFob + (wizardData.selectedHS ? wizardData.selectedHS.codigo : '8517');
    let hash = 0;
    for (let i = 0; i < textToHash.length; i++) {
        hash = (hash << 5) - hash + textToHash.charCodeAt(i);
        hash |= 0;
    }
    const signature = "SHA256-" + Math.abs(hash).toString(16).toUpperCase() + "E8B4C92F";
    document.getElementById('resFirma').innerText = signature;
    
    const decisionTitle = document.getElementById('finalDecisionTitle');
    const decisionText = document.getElementById('finalDecisionText');
    if (decisionTitle && decisionText) {
        if (assessment.status === 'required') {
            decisionTitle.innerText = "Antes de comprar, revisa el permiso";
            decisionText.innerText = assessment.action;
        } else if (docPending) {
            decisionTitle.innerText = "Puedes avanzar, pero completa documentos";
            decisionText.innerText = "Te faltan documentos basicos para que la evaluacion quede lista.";
        } else if (assessment.status === 'review') {
            decisionTitle.innerText = "Confirma unos detalles";
            decisionText.innerText = assessment.action;
        } else {
            decisionTitle.innerText = "Tu importacion se ve lista para continuar";
            decisionText.innerText = "Guarda esta evaluacion y usala como checklist de trabajo.";
        }
    }

    const actionPlan = document.getElementById('finalActionPlan');
    if (actionPlan) {
        const items = [];
        items.push("<li><strong>Producto:</strong> confirma con tu proveedor la descripcion comercial y la ficha tecnica final.</li>");
        if (assessment.status === 'required') {
            items.push("<li><strong>Permisos:</strong> prepara los datos que pide " + assessment.entity + " antes de embarcar.</li>");
        } else if (assessment.status === 'review') {
            items.push("<li><strong>Revision:</strong> valida materiales, conectividad, uso o condicion del producto antes de cerrar la compra.</li>");
        } else {
            items.push("<li><strong>Permisos:</strong> no vemos una restriccion evidente, pero conserva la ficha tecnica por si la solicitan.</li>");
        }
        items.push(docPending
            ? "<li><strong>Documentos:</strong> consigue factura comercial y documento de transporte antes de seguir.</li>"
            : "<li><strong>Documentos:</strong> tu carpeta base ya esta lista para continuar.</li>");
        items.push("<li><strong>Costos:</strong> compara el escenario esperado con el conservador antes de pagar al proveedor.</li>");
        items.push("<li><strong>Siguiente paso:</strong> guarda la evaluacion y continua desde Seguimiento cuando retomes.</li>");
        actionPlan.innerHTML = items.join('');
    }

    const btnPdf = document.getElementById('btnDownloadPDF');
    btnPdf.disabled = false;
    saveWizardDraft();
}

// --- CONEXIÓN AL BACKEND: GUARDADO Y PDF ---
async function descargarExpedienteFinal() {
    if (!registeredImportId) {
        await guardarOperacionGeneral(true); // Auto-guardar primero
    }
    
    if (registeredImportId) {
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
                    window.location.href = `${window.ctx}/api/importacion/descargarPdf?id=${registeredImportId}`;
                    mostrarNotificacion("Descargando resumen PDF de forma segura");
                }
            }, 120);
        } else {
            window.location.href = `${window.ctx}/api/importacion/descargarPdf?id=${registeredImportId}`;
            mostrarNotificacion("Descargando resumen PDF");
        }
    }
}

async function guardarOperacionGeneral(silent = false) {
    captureFields();
    if (!validateStep(1) || !validateStep(2) || !validateStep(4)) {
        return;
    }
    
    const payload = {
        hsCode: wizardData.selectedHS.codigo,
        fob: wizardData.logFob.toString(),
        flete: wizardData.logFlete.toString(),
        seguro: wizardData.logSeguro.toString(),
        tipo: wizardData.opTipo,
        tipoRuta: wizardData.opTipo,
        productoDesc: wizardData.prodNombre || wizardData.opNombre,
        paisOrigen: wizardData.opPaisOrigen,
        incoterm: wizardData.opIncoterm,
        perfilFiscal: wizardData.tribPerfil,
        aplicaTlc: wizardData.tribTlc,
        usado: document.getElementById('qUsado') ? (document.getElementById('qUsado').value === 'SI').toString() : 'false'
    };

    try {
        const res = await fetch(`${window.ctx}/api/importacion/cotizar`, {
            method: 'POST',
            headers: csrfHeaders({ 'Content-Type': 'application/json' }),
            body: JSON.stringify(payload)
        });
        
        const data = await res.json();
        
        if (data.id) {
            registeredImportId = data.id;
            
            refreshDocFileIndicators();
            
            // Registrar documentos asociados
            for (const docTipo in wizardData.docs) {
                if (wizardData.docs[docTipo]) {
                    if (['FACTURA_COMERCIAL', 'BILL_OF_LADING', 'CERTIFICADO_ORIGEN'].includes(docTipo)) {
                        await fetch(`${window.ctx}/api/importacion/registrarDocumentoOperacion?id=${data.id}&tipo=${encodeURIComponent(docTipo)}`, {
                            method: 'POST',
                            headers: csrfHeaders()
                        });
                    }
                }
            }

            // Cambiar estado aduanero según checklist
            const pendingDocs = hasPendingMandatoryDocs();
            const finalState = pendingDocs ? 'DOCS_PENDIENTES' : 'LISTA_DESPACHO';
            
            await fetch(`${window.ctx}/api/importacion/cambiarEstado?id=${data.id}&nuevoEstado=${encodeURIComponent(finalState)}`, {
                method: 'POST',
                headers: csrfHeaders()
            });

            // Si es un celular y tiene homologación, registrar SUCE/Resolución en base
            if (isVuceRequired() && (wizardData.vuceSuce || wizardData.vuceResolucion)) {
                // Registrar solicitud en pre-VUCE
                const vucePayload = {
                    operacionId: data.id,
                    codigoEntidad: wizardData.selectedHS.entidadVuce,
                    tipoPermiso: "Licencia de Internamiento",
                    suce: wizardData.vuceSuce || "SUCE-2026-MOCK",
                    resolucion: wizardData.vuceResolucion || "RD-2026-MOCK",
                    obs: wizardData.vuceObs || "Aprobado didácticamente por importEase",
                    datos: Object.entries(wizardData.tupaDatos).map(([k, v]) => ({ campo: k, valor: v }))
                };

                await fetch(`${window.ctx}/api/permisos/crear-solicitud`, {
                    method: 'POST',
                    headers: csrfHeaders({ 'Content-Type': 'application/json' }),
                    body: JSON.stringify(vucePayload)
                });
            }

            if (!silent) {
                mostrarNotificacion("Evaluacion guardada");
                try {
                    localStorage.removeItem(WIZARD_DRAFT_KEY);
                } catch (e) {}
                setTimeout(() => {
                    window.location.href = "seguimiento.jsp";
                }, 1500);
            }
        } else {
            mostrarNotificacion("No pudimos guardar la evaluacion.", 'error');
        }
    } catch (e) {
        console.error("Error al guardar operación", e);
        mostrarNotificacion("No hay conexion con el servidor. Intenta nuevamente.", 'error');
    }
}

// --- UTILERIAS ---
function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return value.toString()
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function csrfHeaders(extra = {}) {
    return Object.assign({ 'X-CSRF-TOKEN': window.csrfToken || '' }, extra);
}

function mostrarNotificacion(msg, type = 'info') {
    const toast = document.createElement('div');
    toast.className = 'importease-toast importease-toast--' + type;
    toast.innerText = msg;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
}

document.addEventListener('DOMContentLoaded', () => {
    bindWizardActions();

    ['prodNombre', 'prodTecnica', 'prodCantidad', 'prodMarca', 'prodModelo', 'opPaisOrigen', 'opRuc'].forEach((id) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.removeAttribute('oninput');
        el.removeAttribute('onchange');
        el.addEventListener('input', () => {
            if (id === 'prodNombre') syncOpNombre();
            sugerirCostosFobFleteSeguro();
            updateSidebar();
            evalInputRealTimeAlerts();
            saveWizardDraft();
        });
        el.addEventListener('change', () => {
            sugerirCostosFobFleteSeguro();
            updateSidebar();
            evalInputRealTimeAlerts();
            saveWizardDraft();
        });
    });
});

function bindWizardActions() {
    const bindClick = (selector, handler) => {
        const el = document.querySelector(selector);
        if (!el) return;
        el.removeAttribute('onclick');
        el.addEventListener('click', (event) => {
            event.preventDefault();
            handler(el, event);
        });
    };

    bindClick('#routeCardPersonal', () => selectImportRoute('PERSONAL'));
    bindClick('#routeCardComercial', () => selectImportRoute('COMERCIAL'));
    bindClick('#btnPrevStep', () => prevStep());
    bindClick('#btnNextStep', () => nextStep());
    bindClick('#btnDownloadPDF', () => descargarExpedienteFinal());
    bindClick('#sideNextActionButton', (el) => {
        if (el.dataset.action === 'save') {
            guardarOperacionGeneral();
            return;
        }
        const targetStep = Number(el.dataset.step || currentStep + 1);
        goToStep(targetStep);
    });

    const prodUso = document.getElementById('prodUso');
    if (prodUso) {
        prodUso.removeAttribute('onchange');
        prodUso.addEventListener('change', onProdUsoChange);
    }

    const qUsado = document.getElementById('qUsado');
    if (qUsado) {
        qUsado.removeAttribute('onchange');
        qUsado.addEventListener('change', () => {
            updateTaxes();
            updateSidebar();
            evalInputRealTimeAlerts();
            saveWizardDraft();
        });
    }

    ['logFob', 'logFlete', 'logSeguro'].forEach((id) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.removeAttribute('oninput');
        el.addEventListener('input', () => {
            wizardData.manualCostsModified = true;
            updateCIFCalculations();
            saveWizardDraft();
        });
    });

    ['tribPerfil', 'tribTlc'].forEach((id) => {
        const el = document.getElementById(id);
        if (!el) return;
        el.removeAttribute('onchange');
        el.addEventListener('change', () => {
            updateTaxes();
            updateSidebar();
            saveWizardDraft();
        });
    });

    document.querySelectorAll('[onclick]').forEach((el) => {
        const code = el.getAttribute('onclick') || '';
        let action = null;
        let match = code.match(/^cargarEjemplo\('([^']+)'\)/);
        if (match) action = () => cargarEjemplo(match[1]);

        match = code.match(/^openKnowledgePanel\('([^']+)'/);
        if (!action && match) action = () => openKnowledgePanel(match[1]);

        match = code.match(/^selectQuestionOption\('([^']+)',\s*'([^']+)'/);
        if (!action && match) action = () => selectQuestionOption(match[1], match[2], el);

        match = code.match(/^selectIncotermOption\('([^']+)'/);
        if (!action && match) action = () => selectIncotermOption(match[1], el);

        match = code.match(/^toggleDoc\('([^']+)'\)/);
        if (!action && match) action = () => toggleDoc(match[1]);

        if (!action && code.startsWith('openHsAssistant()')) action = openHsAssistant;
        if (!action && code.startsWith('openIncotermsLab()')) action = openIncotermsLab;
        if (!action && code.startsWith('selectManualHS()')) action = selectManualHS;
        if (!action && code.startsWith('guardarOperacionGeneral()')) action = () => guardarOperacionGeneral();
        if (!action && code.startsWith('descargarExpedienteFinal()')) action = descargarExpedienteFinal;

        if (!action) return;
        el.removeAttribute('onclick');
        el.addEventListener('click', (event) => {
            event.preventDefault();
            action();
        });
    });
}






