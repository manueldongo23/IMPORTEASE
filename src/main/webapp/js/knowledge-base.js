const IMPORTEASE_KNOWLEDGE = {
    dam: {
        titulo: 'Declaracion oficial de importacion',
        subtitulo: 'Es el documento formal que se usa cuando la importacion ya esta lista para presentarse.',
        queEs: 'Resume quien importa, que producto entra, cuanto vale, que codigo de producto usa y que impuestos le corresponden.',
        paraQueSirve: 'Sirve para sustentar la declaracion de la mercancia y permitir el control, calculo tributario y levante aduanero.',
        quienLoEmite: 'La preparan el importador o su agente de aduanas y se presenta ante SUNAT Aduanas.',
        cuandoAparece: 'Aparece cuando la operacion ya esta lista para declararse. Antes de eso, el sistema puede mostrar una vista interna solo para orientarte.',
        queDebesRevisar: 'Revisa numero de declaracion, importador, codigo de producto, base de calculo, impuestos estimados y estado de revision.',
        errorComun: 'Confundir una vista referencial con una declaracion oficial presentada. La vista interna solo te ayuda a entender la estructura.',
        relacionConTuCaso: 'Si todavia estas evaluando producto, permisos o documentos, tomalo como una referencia didactica y no como un tramite concluido.',
        etapa: 'Antes de declarar',
        recuerda: 'Si hoy solo recuerdas una cosa: la declaracion oficial aparece cuando la operacion ya esta lista, no desde la etapa de curiosidad.'
    },
    declaracion_aduanera: {
        titulo: 'Declaracion aduanera',
        subtitulo: 'Es el acto formal de informar a aduanas que va a entrar una mercancia al pais.',
        queEs: 'Incluye datos del importador, la mercancia, los documentos comerciales, el valor y los tributos.',
        paraQueSirve: 'Permite que la autoridad aduanera revise la operacion y determine el tratamiento que le corresponde.',
        quienLoEmite: 'La presenta el importador o su representante.',
        cuandoAparece: 'Cuando ya existe mercancia, documentos y una decision de importar en firme.',
        queDebesRevisar: 'Que la descripcion, el valor y el codigo HS correspondan de verdad al producto.',
        errorComun: 'Pensar que es solo un PDF. En realidad es una declaracion formal con efectos tributarios y de control.',
        relacionConTuCaso: 'Tu sistema te enseña esta logica antes para que no llegues perdido a la etapa formal.',
        etapa: 'Antes de declarar',
        recuerda: 'Lo importante no es memorizar el formato: es entender que la declaracion debe coincidir con tus documentos y con el producto real.'
    },
    factura_comercial: {
        titulo: 'Factura Comercial',
        subtitulo: 'Es el documento base de la compra internacional.',
        queEs: 'Prueba que hubo una venta entre tu proveedor y tu empresa o tu persona importadora.',
        paraQueSirve: 'Sustenta el valor FOB, la descripcion comercial y los datos del proveedor.',
        quienLoEmite: 'La emite el proveedor o exportador.',
        cuandoAparece: 'Debes pedirla antes del embarque y revisarla antes de pagar el saldo final.',
        queDebesRevisar: 'Nombre del comprador, descripcion del producto, cantidades, precio unitario, total, moneda e incoterm.',
        errorComun: 'Aceptar una factura generica que no coincide con el producto real o con el valor negociado.',
        relacionConTuCaso: 'Sin una buena factura comercial, tu valor declarado y tu expediente quedan debiles.',
        etapa: 'Antes de embarcar',
        recuerda: 'Si hoy solo recuerdas una cosa: la factura comercial debe describir exactamente lo que de verdad vas a importar.'
    },
    bill_of_lading: {
        titulo: 'BL / AWB',
        subtitulo: 'Es el documento de transporte internacional.',
        queEs: 'BL es Bill of Lading para carga maritima. AWB es Air Waybill para carga aerea.',
        paraQueSirve: 'Prueba que la carga fue embarcada y relaciona remitente, consignatario, bultos y ruta.',
        quienLoEmite: 'La naviera, aerolinea o su operador logístico.',
        cuandoAparece: 'Despues del embarque, cuando la carga ya fue recibida por el transportista.',
        queDebesRevisar: 'Consignatario, puerto o aeropuerto, numero de bultos, peso, descripcion general y fechas.',
        errorComun: 'No revisar que el consignatario sea correcto o que el documento coincida con la factura comercial.',
        relacionConTuCaso: 'Este documento conecta tu compra con el movimiento fisico de la mercancia.',
        etapa: 'Antes de declarar',
        recuerda: 'Sin documento de transporte, tu expediente no demuestra bien como entra la carga.'
    },
    certificado_origen: {
        titulo: 'Certificado de Origen',
        subtitulo: 'Sirve para probar desde que pais califica el producto para beneficios de TLC.',
        queEs: 'Acredita el origen preferencial de la mercancia cuando quieres usar un tratado comercial.',
        paraQueSirve: 'Puede ayudarte a reducir o eliminar aranceles si el tratado aplica.',
        quienLoEmite: 'Lo emite la entidad habilitada en el pais exportador, segun el tratado.',
        cuandoAparece: 'Se pide cuando el proveedor exporta desde un pais con TLC aplicable y quieres usar ese beneficio.',
        queDebesRevisar: 'Pais, productor/exportador, referencia de factura, mercancia y formato correcto del tratado.',
        errorComun: 'Creer que siempre es obligatorio. Solo hace falta si quieres sustentar beneficio arancelario o si tu caso lo exige.',
        relacionConTuCaso: 'Si tu compra depende de un arancel reducido, este documento puede cambiar bastante tu costo final.',
        etapa: 'Antes de declarar',
        recuerda: 'No pidas este documento por costumbre: pidelo cuando de verdad te ayude a sustentar origen preferencial.'
    },
    permiso_autorizacion: {
        titulo: 'Permiso o autorizacion',
        subtitulo: 'Es la validacion previa que algunas entidades exigen antes de importar ciertos productos.',
        queEs: 'No todos los productos lo necesitan. Se usa para mercancias reguladas por salud, telecomunicaciones, agricultura, fauna, flora u otras materias.',
        paraQueSirve: 'Demuestra que tu producto puede entrar legalmente al pais bajo reglas especiales.',
        quienLoEmite: 'La entidad competente, como MTC, DIGEMID, DIGESA, SENASA o SERFOR.',
        cuandoAparece: 'Antes de embarcar o antes de declarar, segun el tipo de mercancia.',
        queDebesRevisar: 'Entidad correcta, nombre del tramite, requisitos, vigencia y datos del producto.',
        errorComun: 'Comprar o embarcar primero y revisar el permiso despues.',
        relacionConTuCaso: 'Si tu producto es sensible, este punto puede definir si puedes importar o no.',
        etapa: 'Antes de comprar',
        recuerda: 'Lo clave es saber si tu producto cae en una entidad reguladora antes de comprometer dinero.'
    },
    suce: {
        titulo: 'SUCE',
        subtitulo: 'Es el numero de tramite o correlativo de una gestion regulatoria en la ventanilla correspondiente.',
        queEs: 'Te ayuda a identificar y seguir un expediente regulatorio en curso.',
        paraQueSirve: 'Sirve para vincular tu tramite con la operacion y mantener trazabilidad documental.',
        quienLoEmite: 'Lo genera la plataforma o flujo regulatorio que procesa tu solicitud.',
        cuandoAparece: 'Cuando ya registraste o iniciaste un expediente de permiso.',
        queDebesRevisar: 'Que el numero corresponda a la operacion correcta y que la entidad sea la esperada.',
        errorComun: 'Guardar un numero sin relacionarlo con el producto, la operacion y la entidad.',
        relacionConTuCaso: 'En el sistema lo usaras para saber si tu expediente regulatorio ya avanzo o sigue pendiente.',
        etapa: 'Antes de declarar',
        recuerda: 'El numero SUCE no reemplaza el permiso: solo te ayuda a rastrear el tramite.'
    },
    codigo_hs: {
        titulo: 'Codigo de producto',
        subtitulo: 'Es la forma en que aduanas clasifica tu producto.',
        queEs: 'Es el codigo oficial que aduanas usa para reconocer tu producto.',
        paraQueSirve: 'Ayuda a definir impuestos, permisos y tratamiento aduanero.',
        quienLoEmite: 'No lo emite el proveedor. Se determina segun la clasificacion aduanera del producto.',
        cuandoAparece: 'Desde la etapa temprana de analisis, antes de calcular costos o permisos.',
        queDebesRevisar: 'Uso real, material, funcion, conectividad y diferencias entre modelos.',
        errorComun: 'Elegir un codigo solo por parecido de nombre y no por la funcion real del producto.',
        relacionConTuCaso: 'Si el codigo cambia, tambien pueden cambiar permisos, impuestos y documentos.',
        etapa: 'Antes de comprar',
        recuerda: 'Clasificar bien al inicio evita errores en casi todo lo demas.'
    },
    valor_fob: {
        titulo: 'Precio del producto',
        subtitulo: 'Es el valor del producto antes de sumar envio y seguro internacional.',
        queEs: 'Representa el costo de la mercancia en origen, sin sumar flete ni seguro internacional.',
        paraQueSirve: 'Es una base clave para costear la importacion y revisar la factura comercial.',
        quienLoEmite: 'Lo acuerdan comprador y proveedor en la negociacion comercial.',
        cuandoAparece: 'Desde la cotizacion inicial y luego en la factura comercial.',
        queDebesRevisar: 'Que el FOB coincida con la oferta, la factura y el incoterm elegido.',
        errorComun: 'Confundir FOB con CIF o con el costo total puesto en Peru.',
        relacionConTuCaso: 'Te ayuda a separar el valor de la mercancia del costo logistico.',
        etapa: 'Antes de comprar',
        recuerda: 'FOB es producto en origen; aun no es el costo total de importar.'
    },
    valor_cif: {
        titulo: 'Base para impuestos',
        subtitulo: 'Es el valor que suma producto, envio y seguro internacional.',
        queEs: 'Es una base de calculo usada para estimar los impuestos de ingreso.',
        paraQueSirve: 'Permite calcular de forma mas completa el costo de importar.',
        quienLoEmite: 'Se construye con la informacion del proveedor y la logistica.',
        cuandoAparece: 'En la etapa de costeo y validacion tributaria.',
        queDebesRevisar: 'Precio del producto, envio, seguro y tipo de cambio usados en la simulacion.',
        errorComun: 'Pensar que CIF ya incluye todos los costos locales o toda la rentabilidad.',
        relacionConTuCaso: 'Tu sistema usa CIF para darte un escenario mas cercano al costo real de importacion.',
        etapa: 'Antes de declarar',
        recuerda: 'Esta base no es ganancia ni precio de venta: sirve para entender mejor tus impuestos.'
    }
};

function ensureKnowledgePanel() {
    if (document.getElementById('knowledgeDrawer')) return;
    const wrapper = document.createElement('div');
    wrapper.innerHTML = `
        <div id="knowledgeOverlay" class="knowledge-overlay hidden"></div>
        <aside id="knowledgeDrawer" class="knowledge-drawer hidden" role="dialog" aria-modal="true" aria-labelledby="knowledgeTitle" aria-describedby="knowledgeSubtitle" aria-hidden="true">
            <div class="knowledge-drawer__header">
                <div>
                    <p id="knowledgeStage" class="knowledge-stage">Antes de comprar</p>
                    <h3 id="knowledgeTitle" class="knowledge-title">Guia de importacion</h3>
                    <p id="knowledgeSubtitle" class="knowledge-subtitle">Explicacion simple para avanzar con criterio.</p>
                </div>
                <button type="button" class="knowledge-close" aria-label="Cerrar ayuda">×</button>
            </div>
            <div class="knowledge-remember">
                <span class="knowledge-remember__label">Si hoy solo recuerdas una cosa</span>
                <p id="knowledgeRemember" class="knowledge-remember__text"></p>
            </div>
            <div class="knowledge-drawer__body">
                <section class="knowledge-section"><h4>Que es</h4><p id="knowledgeWhat"></p></section>
                <section class="knowledge-section"><h4>Para que sirve</h4><p id="knowledgeWhy"></p></section>
                <section class="knowledge-grid">
                    <section class="knowledge-section"><h4>Quien lo emite</h4><p id="knowledgeIssuer"></p></section>
                    <section class="knowledge-section"><h4>Cuando aparece</h4><p id="knowledgeWhen"></p></section>
                </section>
                <section class="knowledge-section"><h4>Que debes revisar</h4><p id="knowledgeCheck"></p></section>
                <section class="knowledge-grid">
                    <section class="knowledge-section knowledge-warning"><h4>Error comun</h4><p id="knowledgeError"></p></section>
                    <section class="knowledge-section knowledge-impact"><h4>Como afecta tu importacion</h4><p id="knowledgeImpact"></p></section>
                </section>
            </div>
            <div class="knowledge-drawer__footer">
                <button id="knowledgeSecondaryAction" type="button" class="knowledge-btn knowledge-btn--secondary hidden"></button>
                <button type="button" class="knowledge-btn knowledge-btn--primary">Entendido</button>
            </div>
        </aside>
    `;
    document.body.appendChild(wrapper);
    document.getElementById('knowledgeOverlay').addEventListener('click', closeKnowledgePanel);
    const drawer = document.getElementById('knowledgeDrawer');
    drawer.querySelector('.knowledge-close').addEventListener('click', closeKnowledgePanel);
    drawer.querySelector('.knowledge-btn--primary').addEventListener('click', closeKnowledgePanel);
}

function fillKnowledgePanel(item, overrides = {}) {
    const merged = Object.assign({}, item, overrides);
    document.getElementById('knowledgeStage').textContent = merged.etapa || 'Ruta guiada';
    document.getElementById('knowledgeTitle').textContent = merged.titulo || 'Guia de importacion';
    document.getElementById('knowledgeSubtitle').textContent = merged.subtitulo || '';
    document.getElementById('knowledgeRemember').textContent = merged.recuerda || '';
    document.getElementById('knowledgeWhat').textContent = merged.queEs || '';
    document.getElementById('knowledgeWhy').textContent = merged.paraQueSirve || '';
    document.getElementById('knowledgeIssuer').textContent = merged.quienLoEmite || '';
    document.getElementById('knowledgeWhen').textContent = merged.cuandoAparece || '';
    document.getElementById('knowledgeCheck').textContent = merged.queDebesRevisar || '';
    document.getElementById('knowledgeError').textContent = merged.errorComun || '';
    document.getElementById('knowledgeImpact').textContent = merged.relacionConTuCaso || '';

    const secondary = document.getElementById('knowledgeSecondaryAction');
    secondary.classList.add('hidden');
    secondary.onclick = null;
    secondary.removeAttribute('href');

    if (merged.actionLabel && (merged.actionHref || merged.actionCallback)) {
        secondary.textContent = merged.actionLabel;
        secondary.classList.remove('hidden');
        if (merged.actionCallback) {
            secondary.onclick = merged.actionCallback;
        } else if (merged.actionHref) {
            secondary.onclick = () => {
                window.location.href = merged.actionHref;
            };
        }
    }
}

function openKnowledgePanel(key, overrides = {}) {
    const item = IMPORTEASE_KNOWLEDGE[key];
    if (!item) return;
    ensureKnowledgePanel();
    fillKnowledgePanel(item, overrides);
    document.body.classList.add('knowledge-modal-open');
    document.getElementById('knowledgeOverlay').classList.remove('hidden');
    const drawer = document.getElementById('knowledgeDrawer');
    drawer.classList.remove('hidden');
    drawer.setAttribute('aria-hidden', 'false');
    requestAnimationFrame(() => drawer.classList.add('is-open'));
}

function closeKnowledgePanel() {
    const overlay = document.getElementById('knowledgeOverlay');
    const drawer = document.getElementById('knowledgeDrawer');
    if (!overlay || !drawer) return;
    overlay.classList.add('hidden');
    drawer.classList.remove('is-open');
    document.body.classList.remove('knowledge-modal-open');
    window.setTimeout(() => {
        drawer.classList.add('hidden');
        drawer.setAttribute('aria-hidden', 'true');
    }, 180);
}

document.addEventListener('DOMContentLoaded', ensureKnowledgePanel);
