// ==========================================================================
// IMPORTEASE - VUCE & REGULATORY ASSESSMENT MODULE (vuce-assessment.js)
// ==========================================================================

window.ImportEaseWizard = window.ImportEaseWizard || {};

(function(W) {
    
    W.detectProductProfile = function() {
        const productCorpus = W.normalizeWizardText(W.getProductInputCorpus());
        const selectedCode = String(W.wizardData.selectedHS && W.wizardData.selectedHS.codigo ? W.wizardData.selectedHS.codigo : '').replace(/\./g, '');
        const corpus = productCorpus || W.normalizeWizardText(W.getWizardSearchCorpus());
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
    };

    W.DYNAMIC_QUESTION_SETS = {
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
            { id: 'cosm_perfume', label: '¿Es un perfume o colonia para aplicar en el cuerpo?', helper: 'Permite afinar la clasificación arancelaria exacta.', defaultValue: 'NOSE' },
            { id: 'cosm_use', label: '¿Se venderá como cosmético o cuidado personal?', helper: 'Establece la necesidad de registro DIGEMID.', defaultValue: 'NOSE' },
            { id: 'cosm_container', label: '¿Viene en frasco, spray o estuche?', helper: 'Verifica los requisitos de envase y presentación.', defaultValue: 'NOSE' },
            { id: 'cosm_alcohol', label: '¿Contiene alcohol?', helper: 'Perfila el control de alcoholes y restricciones.', defaultValue: 'NOSE' }
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

    W.getActiveQuestionSet = function() {
        const profile = W.detectProductProfile();
        const questions = W.DYNAMIC_QUESTION_SETS[profile.key] || W.DYNAMIC_QUESTION_SETS.general;
        const productName = W.getFieldValue('prodNombre', '').trim();
        const hint = productName ? ` — "${productName.substring(0, 40)}${productName.length > 40 ? '...' : ''}"` : '';

        return {
            profile: Object.assign({}, profile, {
                label: profile.label + hint
            }),
            questions
        };
    };

    W.getDynamicAnswerValue = function(id) {
        return (W.wizardData.dynamicAnswers && W.wizardData.dynamicAnswers[id]) || 'NOSE';
    };

    W.normalizeYesNoValue = function(value) {
        const raw = String(value || '').toUpperCase();
        if (raw === 'SI' || raw === 'SÍ') return 'SI';
        if (raw === 'NO') return 'NO';
        return 'NOSE';
    };

    W.setLegacyQuestionValue = function(fieldId, value) {
        const el = document.getElementById(fieldId);
        if (el) {
            el.value = W.normalizeYesNoValue(value);
        }
    };

    W.syncLegacyQuestionModel = function(profile = W.detectProductProfile()) {
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

        Object.entries(state).forEach(([fieldId, value]) => W.setLegacyQuestionValue(fieldId, value));
        W.wizardData.vuceQuestions.consumo = state.qConsumo === 'SI';
        W.wizardData.vuceQuestions.salud = state.qSalud === 'SI';
        W.wizardData.vuceQuestions.wifi = state.qWifi === 'SI';
        W.wizardData.vuceQuestions.contacto = state.qContacto === 'SI';
        W.wizardData.vuceQuestions.madera = state.qMadera === 'SI';
        W.wizardData.vuceQuestions.usado = state.qUsado === 'SI';
        W.wizardData.dynamicProfile = profile.key;
    };

    W.renderDynamicQuestions = function() {
        const area = document.getElementById('dynamicQuestionArea');
        const meta = document.getElementById('dynamicQuestionMeta');
        if (!area) return;

        const { profile, questions } = W.getActiveQuestionSet();
        W.wizardData.dynamicProfile = profile.key;
        W.wizardData.dynamicQuestions = questions;
        W.syncLegacyQuestionModel(profile);

        if (meta) {
            meta.textContent = "Estas respuestas nos ayudan a afinar el código y evitar confusiones.";
        }

        area.innerHTML = '';
        questions.forEach((question) => {
            const currentValue = W.normalizeYesNoValue(W.getDynamicAnswerValue(question.id) || question.defaultValue || 'NOSE');
            const card = document.createElement('div');
            card.className = 'flex flex-col justify-between gap-4 bg-white p-4 rounded-2xl border border-[#e8eaf0] shadow-sm transition-all hover:border-[#5B50F0]/30';
            card.dataset.questionId = question.id;
            card.innerHTML = `
                <span class="text-[11px] font-bold text-[#1a1d2e] leading-snug min-h-[40px]">${question.label}</span>
                <div class="flex gap-1 bg-[#F3F4F6] p-1 rounded-xl" data-question-group="${question.id}">
                    <button type="button" data-value="SI" class="flex-1 py-1.5 rounded-lg text-[10px] font-black uppercase transition-all cursor-pointer border-none ${currentValue === 'SI' ? 'bg-white text-[#5B50F0] shadow-sm font-extrabold' : 'bg-transparent text-[#6b7280] hover:text-[#1a1d2e]'}">Sí</button>
                    <button type="button" data-value="NO" class="flex-1 py-1.5 rounded-lg text-[10px] font-black uppercase transition-all cursor-pointer border-none ${currentValue === 'NO' ? 'bg-white text-[#5B50F0] shadow-sm font-extrabold' : 'bg-transparent text-[#6b7280] hover:text-[#1a1d2e]'}">No</button>
                    <button type="button" data-value="NOSE" class="flex-1 py-1.5 rounded-lg text-[10px] font-black uppercase transition-all cursor-pointer border-none ${currentValue === 'NOSE' ? 'bg-white text-[#5B50F0] shadow-sm font-extrabold' : 'bg-transparent text-[#6b7280] hover:text-[#1a1d2e]'}">No sé</button>
                </div>
            `;
            area.appendChild(card);
        });
    };

    W.selectDynamicQuestionOption = function(fieldId, value, buttonEl) {
        if (!W.wizardData.dynamicAnswers || typeof W.wizardData.dynamicAnswers !== 'object') {
            W.wizardData.dynamicAnswers = {};
        }
        W.wizardData.dynamicAnswers[fieldId] = W.normalizeYesNoValue(value);

        if (buttonEl && buttonEl.parentElement) {
            const buttons = buttonEl.parentElement.querySelectorAll('button');
            buttons.forEach((btn) => {
                btn.className = 'flex-1 py-1.5 rounded-lg text-[10px] font-black uppercase transition-all cursor-pointer border-none bg-transparent text-[#6b7280] hover:text-[#1a1d2e]';
            });
            buttonEl.className = 'flex-1 py-1.5 rounded-lg text-[10px] font-black uppercase transition-all cursor-pointer border-none bg-white text-[#5B50F0] shadow-sm font-extrabold';
        }

        W.onVuceQuestionChange();
    };

    W.getVuceStatusBadge = function(h) {
        if (h.requiereVuce) {
            const ent = h.entidadVuce || 'SUNAT';
            if (ent === 'DIGEMID' || ent === 'DIGESA') {
                return `<span class="px-2 py-1 rounded-full bg-[#FFF7ED] border border-[#FFEDD5] text-[#C2410C] text-[9px] font-bold uppercase font-sans">Posible revisión sanitaria</span>`;
            }
            if (ent === 'MTC') {
                return `<span class="px-2 py-1 rounded-full bg-[#FAF5FF] border border-[#F3E8FF] text-[#7E22CE] text-[9px] font-bold uppercase font-sans">Posible revisión MTC</span>`;
            }
            if (ent === 'SENASA' || ent === 'SERFOR') {
                return `<span class="px-2 py-1 rounded-full bg-[#FEF3C7] border border-[#FDE68A] text-[#B45309] text-[9px] font-bold uppercase font-sans">Posible revisión ${ent}</span>`;
            }
            return `<span class="px-2 py-1 rounded-full bg-[#FFF1F2] border border-[#FFE4E6] text-[#BE123C] text-[9px] font-bold uppercase font-sans">Posible revisión ${ent}</span>`;
        }
        
        const desc = (h.descripcionEs || '').toLowerCase();
        if (desc.includes('preparaciones para perfumar') || desc.includes('pulverizadores') || desc.includes('belleza')) {
            return `<span class="px-2 py-1 rounded-full bg-[#EFF6FF] border border-[#DBEAFE] text-[#1D4ED8] text-[9px] font-bold uppercase font-sans">Revisar uso declarado</span>`;
        }
        return `<span class="px-2 py-1 rounded-full bg-[#F3F4F6] border border-[#E5E7EB] text-[#4B5563] text-[9px] font-bold uppercase font-sans">Sin control evidente</span>`;
    };

    W.getSuggestedEntity = function() {
        const profile = W.detectProductProfile();
        if (W.wizardData.selectedHS && W.wizardData.selectedHS.entidadVuce && W.wizardData.selectedHS.entidadVuce !== 'SUNAT') {
            return W.wizardData.selectedHS.entidadVuce;
        }
        if (profile.entity && profile.entity !== 'SUNAT' && profile.entity !== 'REVISAR') {
            return profile.entity;
        }
        if (profile.key === 'used') return 'REVISAR';
        if (W.getQuestionAnswer('qSalud') === 'SI') return 'DIGEMID';
        if (W.getQuestionAnswer('qConsumo') === 'SI') return 'DIGESA';
        if (W.getQuestionAnswer('qWifi') === 'SI') return 'MTC';
        if (W.getQuestionAnswer('qContacto') === 'SI') return 'SENASA';
        if (W.getQuestionAnswer('qMadera') === 'SI') return 'SERFOR';
        if (W.getQuestionAnswer('qUsado') === 'SI') return 'REVISAR';
        return 'SUNAT';
    };

    W.getVuceAssessment = function() {
        const profile = W.detectProductProfile();
        const dynamic = W.wizardData.dynamicAnswers || {};
        const opTipo = (document.getElementById('opTipo') ? document.getElementById('opTipo').value : W.wizardData.opTipo) || 'PERSONAL';
        const quantity = Number(document.getElementById('prodCantidad') ? document.getElementById('prodCantidad').value : W.wizardData.prodCantidad) || 1;

        // Sync values from inputs or dynamic/legacy answers
        const wifiAnswer = dynamic.tech_wifi || W.getQuestionAnswer('qWifi');
        const bluetoothAnswer = 'NO'; // Default helper inside checks
        const usedAnswer = dynamic.used_second || W.getQuestionAnswer('qUsado');
        const finishedWoodAnswer = dynamic.wood_finished || 'NO';

        // Used products check (Clothing/Footwear vs Machinery/Electronics)
        const corpusLower = W.getProductInputCorpus().toLowerCase();
        const isClothing = profile.key === 'clothing' || 
                           /\b(ropa|camisa|pantalon|pantalón|vestido|chaqueta|abrigo|zapato|zapatilla|tenis|calzado|textil|tela|algodon|algodón|poliester|poliéster|prendas?)\b/.test(corpusLower);
        const isMachineryOrElectronics = profile.key === 'machinery' || profile.key === 'tech' ||
                                          /\b(maquina|máquina|equipo|motor|compresor|torno|herramienta|computadora|laptop|tablet|celular|telefono|teléfono|electronico|electrónico)\b/.test(corpusLower);

        if (usedAnswer === 'SI') {
            if (isClothing) {
                return {
                    status: 'required',
                    entity: 'PRODUCE',
                    title: 'Importación Prohibida',
                    message: 'La importación de ropa y calzado usado podría estar prohibida o restringida en el Perú por razones de salud pública y bioseguridad. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'No se recomienda continuar. Consulta restricciones oficiales.'
                };
            } else if (isMachineryOrElectronics) {
                return {
                    status: 'review',
                    entity: 'SUNAT / PRODUCE',
                    title: 'Revisión de bienes usados',
                    message: 'La maquinaria y equipos electrónicos usados podrían requerir inspección física y estar sujetos a regulaciones específicas y percepciones especiales. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Verifica la tasa de percepción y prepara la documentación técnica.'
                };
            } else {
                return {
                    status: 'review',
                    entity: 'REVISAR',
                    title: 'Revisión de bienes de segundo uso',
                    message: 'El producto es usado o reacondicionado. Podría requerir validar estado y destino. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Confirma estado, operatividad y regulaciones aplicables.'
                };
            }
        }

        // Tech check (WiFi/Bluetooth)
        if (profile.key === 'tech') {
            const hasWifi = wifiAnswer === 'SI';
            const hasBluetooth = bluetoothAnswer === 'SI';

            if (hasWifi || hasBluetooth) {
                return {
                    status: 'required',
                    entity: 'MTC',
                    title: 'Posible revisión MTC',
                    message: 'El producto cuenta con conectividad inalámbrica y podría requerir homologación o internamiento del MTC. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Prepara marca, modelo y consulta si el equipo requiere homologación referencial en el MTC.'
                };
            } else if (wifiAnswer === 'NOSE' || bluetoothAnswer === 'NOSE') {
                return {
                    status: 'review',
                    entity: 'MTC',
                    title: 'Revisión técnica de conectividad',
                    message: 'No se ha determinado si el equipo tiene WiFi o Bluetooth. Podría requerir revisión de especificaciones. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Consulta la ficha técnica del fabricante para confirmar si tiene radiofrecuencia.'
                };
            } else {
                return {
                    status: 'free',
                    entity: 'SUNAT',
                    title: 'Posible exención de control MTC',
                    message: 'Podría no requerir control MTC según las respuestas. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Valida técnicamente que no emita señales inalámbricas reguladas.'
                };
            }
        }

        // Cosmetics check (DIGEMID)
        if (profile.key === 'cosmetics') {
            if (opTipo === 'PERSONAL' && quantity <= 4) {
                return {
                    status: 'free',
                    entity: 'SUNAT',
                    title: 'Posible exención DIGEMID',
                    message: 'Podría no requerir control DIGEMID según las respuestas (cosméticos de uso personal hasta 4 unidades). Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Confirma que el destino sea strictly de uso personal.'
                };
            } else {
                return {
                    status: 'required',
                    entity: 'DIGEMID',
                    title: 'Posible revisión DIGEMID',
                    message: 'Podría requerir revisión DIGEMID. Los cosméticos en cantidad comercial (mayor a 4 unidades) sugieren Notificación Sanitaria Obligatoria. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Revisa requisitos de NSO, laboratorio local o importador autorizado.'
                };
            }
        }

        // Food check (DIGESA)
        if (profile.key === 'food') {
            if (opTipo === 'PERSONAL' && quantity <= 3) {
                return {
                    status: 'free',
                    entity: 'SUNAT',
                    title: 'Posible exención DIGESA',
                    message: 'Podría no requerir control DIGESA según las respuestas (alimentos/suplementos para uso personal hasta 3 unidades). Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Confirma que el destino sea de uso personal.'
                };
            } else {
                return {
                    status: 'required',
                    entity: 'DIGESA',
                    title: 'Posible revisión DIGESA',
                    message: 'Podría requerir revisión DIGESA o Registro Sanitario para fines comerciales o cantidades mayores. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Revisa requisitos de Registro Sanitario y etiquetado regulado.'
                };
            }
        }

        // Wood check (SERFOR)
        if (profile.key === 'wood') {
            if (finishedWoodAnswer === 'SI') {
                return {
                    status: 'free',
                    entity: 'SUNAT',
                    title: 'Posible exención SERFOR',
                    message: 'Podría no requerir control de SERFOR por tratarse de producto terminado de madera. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Confirma si es un producto completamente manufacturado.'
                };
            } else {
                return {
                    status: 'required',
                    entity: 'SERFOR',
                    title: 'Posible revisión SERFOR',
                    message: 'Podría requerir revisión SERFOR y certificado fitosanitario para madera en bruto o subproductos. Validación referencial: confirma con la entidad competente antes de embarcar.',
                    action: 'Prepara el certificado fitosanitario y los permisos de SERFOR.'
                };
            }
        }

        // Fallbacks for other profiles
        if (profile.key === 'health') {
            return {
                status: 'required',
                entity: 'DIGEMID',
                title: 'Posible revisión DIGEMID',
                message: 'La descripción o partida arancelaria apunta a un dispositivo o producto médico. Podría requerir revisión DIGEMID. Validación referencial: confirma con la entidad competente antes de embarcar.',
                action: 'Reúne ficha técnica y consulta requisitos con DIGEMID.'
            };
        }

        if (profile.key === 'agriculture') {
            return {
                status: 'required',
                entity: 'SENASA',
                title: 'Posible revisión SENASA',
                message: 'La categoría fitosanitaria podría requerir revisión y certificado fitosanitario por SENASA. Validación referencial: confirma con la entidad competente antes de embarcar.',
                action: 'Consulta requisitos fitosanitarios específicos para el origen.'
            };
        }

        const directRestriction = !!(W.wizardData.selectedHS && W.wizardData.selectedHS.requiereVuce);
        const answers = {
            consumo: W.getQuestionAnswer('qConsumo'),
            salud: W.getQuestionAnswer('qSalud'),
            wifi: W.getQuestionAnswer('qWifi'),
            contacto: W.getQuestionAnswer('qContacto'),
            madera: W.getQuestionAnswer('qMadera'),
            usado: W.getQuestionAnswer('qUsado')
        };
        const strongYes = ['consumo', 'salud', 'wifi', 'contacto', 'madera'].some((key) => answers[key] === 'SI');

        if (directRestriction || strongYes) {
            return {
                status: 'required',
                entity: profile.entity === 'SUNAT' ? 'MTC' : profile.entity,
                title: 'Posible revisión de control',
                message: 'Se detectó una característica regulada según la partida arancelaria o respuestas. Podría requerir revisión. Validación referencial: confirma con la entidad competente antes de embarcar.',
                action: 'Prepara la documentación técnica y realiza consultas previas.'
            };
        }

        return {
            status: 'free',
            entity: 'SUNAT',
            title: 'Sin controles evidentes',
            message: 'No se identifican restricciones o permisos obligatorios evidentes con la información disponible. Validación referencial: confirma con la entidad competente antes de embarcar.',
            action: 'Continúa con el costeo y prepara tus documentos comerciales.'
        };
    };

    W.isVuceRequired = function() {
        return W.getVuceAssessment().status === 'required';
    };

    W.onVuceQuestionChange = function() {
        const profile = W.detectProductProfile();
        if (W.wizardData.dynamicAnswers && Object.keys(W.wizardData.dynamicAnswers).length) {
            W.syncLegacyQuestionModel(profile);
        } else {
            W.wizardData.vuceQuestions.consumo = W.getQuestionAnswer('qConsumo') === 'SI';
            W.wizardData.vuceQuestions.salud = W.getQuestionAnswer('qSalud') === 'SI';
            W.wizardData.vuceQuestions.wifi = W.getQuestionAnswer('qWifi') === 'SI';
            W.wizardData.vuceQuestions.contacto = W.getQuestionAnswer('qContacto') === 'SI';
            W.wizardData.vuceQuestions.usado = W.getQuestionAnswer('qUsado') === 'SI';
            W.wizardData.vuceQuestions.madera = W.getQuestionAnswer('qMadera') === 'SI';
        }
        if (W.wizardData.vuceQuestions.madera && (!W.wizardData.selectedHS || !W.wizardData.selectedHS.requiereVuce)) {
            W.wizardData.selectedHS = {
                codigo: '4407.91.00.00',
                descripcionEs: 'Madera de roble aserrada longitudinalmente',
                adValorem: 6,
                requiereVuce: true,
                entidadVuce: 'SERFOR'
            };
            W.wizardData.selectedHSOrigin = 'AUTO';
            W.applyHSCodeUI(W.wizardData.selectedHS);
        }
        W.evalVuceQuestions();
        W.updateStep2RestrictionCard();
        W.updateSidebar();
        W.saveWizardDraft();
    };

    W.evalVuceQuestions = function() {
        W.captureFields();
        const assessment = W.getVuceAssessment();
        W.renderDynamicQuestions();
        
        const panel = document.getElementById('vuceSemaforoPanel');
        const semText = document.getElementById('vuceSemaforoText');
        const badge = document.getElementById('vuceSemaforoBadge');
        const msg = document.getElementById('vuceReglaMensaje');
        const sectorVal = document.getElementById('vuceSectorVal');
        const tipoVal = document.getElementById('vuceTipoRevisionVal');
        const actionVal = document.getElementById('vuceAccionVal');
        const iconContainer = document.getElementById('vuceIconContainer');
        const templateContainer = document.getElementById('entityTemplateContainer');
        const templateLabel = document.getElementById('tupaEntidadLabel');

        if (!panel) return;

        // Determine subcategory descriptive text
        let subcategory = 'Control general';
        const pKey = W.detectProductProfile().key;
        if (pKey === 'cosmetics') subcategory = 'Cosméticos / cuidado personal';
        else if (pKey === 'tech') subcategory = 'Equipos de telecomunicaciones';
        else if (pKey === 'food') subcategory = 'Alimentos y suplementos';
        else if (pKey === 'health') subcategory = 'Salud y dispositivos médicos';
        else if (pKey === 'agriculture') subcategory = 'Agricultura y fitosanidad';
        else if (pKey === 'wood') subcategory = 'Madera y control forestal';
        else if (pKey === 'used') subcategory = 'Mercancías usadas';

        if (tipoVal) tipoVal.textContent = subcategory;

        if (assessment.status === 'free') {
            panel.className = "border border-emerald-200 bg-[#F0FDF4] rounded-2xl p-5 flex flex-col justify-between gap-4 shadow-sm";
            if (semText) {
                semText.textContent = "Sin control evidente";
                semText.className = "text-xs font-black uppercase text-[#15803D] tracking-wider font-sans";
            }
            if (badge) {
                badge.textContent = "Sin control evidente";
                badge.className = "px-2 py-0.5 rounded bg-[#D1FAE5] text-[#15803D] border border-[#A7F3D0] text-[9px] font-bold uppercase font-sans";
            }
            if (iconContainer) {
                iconContainer.className = "w-7 h-7 rounded-lg bg-emerald-100 text-[#15803D] flex items-center justify-center shrink-0";
                iconContainer.innerHTML = `<svg class="w-4 h-4 text-[#15803D]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`;
            }
            if (msg) msg.textContent = assessment.message;
            if (sectorVal) sectorVal.textContent = assessment.entity;
            if (actionVal) {
                actionVal.textContent = "Listo para continuar (Referencial)";
                actionVal.className = "text-[#15803D]";
            }
            if (templateContainer) templateContainer.classList.add('hidden');
        } else if (assessment.status === 'review') {
            panel.className = "border border-blue-200 bg-[#EFF6FF] rounded-2xl p-5 flex flex-col justify-between gap-4 shadow-sm";
            if (semText) {
                semText.textContent = "Revisar";
                semText.className = "text-xs font-black uppercase text-[#1D4ED8] tracking-wider font-sans";
            }
            if (badge) {
                badge.textContent = "Por confirmar";
                badge.className = "px-2 py-0.5 rounded bg-[#DBEAFE] text-[#1D4ED8] border border-[#BFDBFE] text-[9px] font-bold uppercase font-sans";
            }
            if (iconContainer) {
                iconContainer.className = "w-7 h-7 rounded-lg bg-blue-100 text-[#1D4ED8] flex items-center justify-center shrink-0";
                iconContainer.innerHTML = `<svg class="w-4 h-4 text-[#1D4ED8]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>`;
            }
            if (msg) msg.textContent = assessment.message;
            if (sectorVal) sectorVal.textContent = assessment.entity;
            if (actionVal) {
                actionVal.textContent = "Revisar antes de continuar";
                actionVal.className = "text-[#1D4ED8]";
            }
            if (templateContainer) templateContainer.classList.add('hidden');
        } else {
            panel.className = "border border-amber-200 bg-[#FFF7ED] rounded-2xl p-5 flex flex-col justify-between gap-4 shadow-sm";
            if (semText) {
                semText.textContent = assessment.title || "Posible permiso sanitario";
                semText.className = "text-xs font-black uppercase text-[#D97706] tracking-wider font-sans";
            }
            if (badge) {
                badge.textContent = "Permiso posible";
                badge.className = "px-2 py-0.5 rounded bg-[#FEF3C7] text-[#D97706] border border-[#FDE68A] text-[9px] font-bold uppercase font-sans";
            }
            if (iconContainer) {
                iconContainer.className = "w-7 h-7 rounded-lg bg-amber-100 text-[#D97706] flex items-center justify-center shrink-0";
                iconContainer.innerHTML = `<svg class="w-4 h-4 text-[#D97706]" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/></svg>`;
            }
            if (msg) msg.textContent = assessment.message;
            if (sectorVal) sectorVal.textContent = assessment.entity;
            if (actionVal) {
                actionVal.textContent = "Revisar antes de continuar";
                actionVal.className = "text-[#D97706]";
            }
            if (templateContainer) {
                templateContainer.classList.remove('hidden');
                if (templateLabel) templateLabel.textContent = assessment.entity;
                W.renderTupaFields(assessment.entity, pKey);
            }
        }
        W.updateSidebar();
    };

    W.renderTupaFields = function(entity, profileKey) {
        const area = document.getElementById('tupaFieldsArea');
        if (!area) return;
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
            const val = W.wizardData.tupaDatos[f.label] || '';
            const div = document.createElement('div');
            div.className = 'flex flex-col gap-1';
            div.innerHTML = `
                <label class="text-[9px] font-bold text-gray-500 uppercase">${f.label}</label>
                <input type="${f.type}" data-label="${f.label}" class="bg-white border border-[#E6E2D8] rounded-xl px-3 py-2 text-xs text-[#1F2937] outline-none focus:border-[#0A5C4A] font-semibold" placeholder="${f.placeholder}" value="${W.escapeHtml(val)}">
            `;
            area.appendChild(div);
        });
    };

    W.updateStep2RestrictionCard = function() {
        const assessment = W.getVuceAssessment();
        const entityEl = document.getElementById('sideRestriccionEntidad');
        const tipoEl = document.getElementById('sideRestriccionTipo');
        const estadoEl = document.getElementById('sideRestriccionEstado');
        const titleEl = document.getElementById('sideRestriccionTitle');
        const descEl = document.getElementById('sideRestriccionDesc');
        const badgeEl = document.getElementById('sideRestriccionBadge');
        const containerEl = document.getElementById('sideRestriccionContainer');
        
        if (!entityEl || !tipoEl || !estadoEl || !titleEl || !descEl || !badgeEl || !containerEl) return;
        
        if (assessment.status === 'required') {
            containerEl.className = "border border-amber-200 bg-amber-50 rounded-2xl p-5 shadow-sm space-y-4";
            badgeEl.textContent = "Permiso posible";
            badgeEl.className = "px-2 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-bold uppercase";
            entityEl.textContent = assessment.entity;
            
            let revisionTipo = "Revisión regulatoria";
            if (assessment.entity === 'DIGEMID') {
                revisionTipo = "Cosméticos / cuidado personal / salud";
            } else if (assessment.entity === 'MTC') {
                revisionTipo = "Telecomunicaciones / Homologación";
            } else if (assessment.entity === 'DIGESA') {
                revisionTipo = "Alimentos / Bebidas / Suplementos";
            } else if (assessment.entity === 'SENASA') {
                revisionTipo = "Fitosanitario / Zoosanitario";
            } else if (assessment.entity === 'SERFOR') {
                revisionTipo = "Forestal / Flora y Fauna silvestre";
            }
            tipoEl.textContent = revisionTipo;
            
            estadoEl.textContent = "Revisar antes de continuar";
            estadoEl.className = "font-bold text-amber-600 text-right";
            
            titleEl.textContent = assessment.title || "Posible revisión de control";
            descEl.textContent = assessment.message || "Este producto requiere revisión sanitaria o regulatoria previa para ingresar legalmente.";
        } else {
            containerEl.className = "border border-emerald-200 bg-emerald-50 rounded-2xl p-5 shadow-sm space-y-4";
            badgeEl.textContent = "Sin control evidente";
            badgeEl.className = "px-2 py-0.5 rounded bg-emerald-100 text-emerald-800 text-[10px] font-bold uppercase";
            entityEl.textContent = "SUNAT";
            tipoEl.textContent = "Ninguno / Libre comercialización referencial";
            estadoEl.textContent = "Sin control evidente";
            estadoEl.className = "font-bold text-emerald-600 text-right";
            titleEl.textContent = "Sin controles evidentes (Referencial)";
            descEl.textContent = "Este producto no presenta restricciones de permisos sanitarios o técnicos evidentes en SUNAT. Validación referencial: confirma con la entidad competente antes de embarcar.";
        }
    };

})(window.ImportEaseWizard);
