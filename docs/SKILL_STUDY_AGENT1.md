# SKILL STUDY — AGENT 1: ANALISTA DE DOMINIO ADUANERO Y REGLAS FUNCIONALES

## 1. DIAGNÓSTICO DE LA SKILL

### Fortalezas

| Aspecto | Descripción |
|---|---|
| **Diferenciación de fuentes** | Define 8 tipos (`OFFICIAL_API`, `OFFICIAL_PROCEDURE`, `MANUAL_USER_INPUT`, `CACHE_OFFICIAL`, `SYSTEM_RULE`, `SIMULATED`, `REFERENTIAL`, `UNKNOWN`) — mapeados correctamente en `FuenteMetadataBuilder.java` y `DataConfidenceService.java`. |
| **PRE-DAM no oficial** | Prohíbe explícitamente tratar PRE-DAM como documento oficial (regla 2-3). |
| **Datos mínimos** | Lista 14 campos obligatorios para PRE-DAM (regla 4), validados en `PredamValidationService.java`. |
| **Plazos con trazabilidad** | Exige código de regla, evento base, fecha base, plazo, fecha límite, estado, fuente normativa, tipo de fuente y nivel de confianza (regla 5). |
| **Flujo guiado** | Define 7 pasos obligatorios (intención → datos básicos → clasificación → transporte → checklist → validación → DTA/PRE-DAM/timeline). |
| **Preguntas iniciales** | 7 preguntas para elegir régimen, implementadas en `AduanasService.evaluarRegimen()`. |
| **Regímenes cubiertos** | Importación consumo, modalidades (anticipado/diferido/urgente), reimportación, admisión temporal, transbordo, tránsito. |
| **Panel de salud** | Define 8 indicadores de expediente. |
| **Siguiente acción** | Propone formato de siguiente acción con motivo, desbloqueo y riesgo. |
| **Frases obligatorias** | 3 frases pedagógicas para PRE-DAM, fuente referencial y bloqueo. |

### Debilidades

| Aspecto | Descripción |
|---|---|
| **Sin tabla de severidad de reglas** | No clasifica reglas como `BLOCKING`, `WARNING`, `INFO`. El código (`PredamValidationService`) ya usa `BLOCKING` pero la skill no lo define. |
| **Sin parametrización por país** | Todas las reglas son Perú-genéricas. No hay instrucciones para adaptar a otros países (Colombia, Chile, México). |
| **Sin categorización de usuarios** | Trata al usuario como genérico. No diferencia entre principiante, importador frecuente, agente de aduana o técnico académico. |
| **Abandono legal** | Mencionado pero sin reglas formales. No define umbrales, cálculo de días ni procedimiento de rescate. |
| **Garantías** | Mencionadas para admisión temporal y tránsito, pero sin reglas de cálculo, vencimiento ni liberación. El código (`ensureGarantiaIfNeeded`) ya implementa cálculo básico pero la skill no lo prescribe. |
| **Canales de control** | Menciona verde/naranja/rojo pero sin reglas de asignación. El `RiskScoringService` ya genera canales pero la skill no estandariza criterios. |
| **Sin reglas de integridad referencial** | No cubre casos como: documento de transporte sin manifiesto, contenedor sin BL, permiso vencido vs carga embarcada. |
| **Sin reglas de auditoría obligatoria** | Menciona auditoría pero no define qué eventos son obligatorios. El código `ExpedienteAuditService` ya registra validaciones. |
| **Sin tabla de acciones bloqueadas específicas** | La regla 4 enumera datos mínimos pero no dice exactamente qué se bloquea si faltan. |

### Gaps vs. Implementación Real

| Gap | Skill | Código Real |
|---|---|---|
| `dataConfidenceService` | No referenciado | Implementado con 20+ tipos de fuente con niveles de confianza |
| `CITES / SERFOR` | No mencionado | Implementado en `PredamValidationService` (línea 45-56) |
| Alerta cruzada MTC+DIGEMID | No mencionado | Implementado (línea 59-65) |
| Hash chain en auditoría | No mencionado | En BD (upgrade add_hash_chain_auditoria) |
| RUC validado / estado / condición | No mencionado | Verificado en `PredamValidationService.checkImporterIdentity()` |
| `INCOTERMS` con tabla OMC | No profundizado | `IncotermService.java` existe |
| Franquicia courier (FOB < 200 / < 2000) | No mencionado | Implementado en `ImportacionService` |

---

## 2. VACÍOS DETECTADOS

| # | Vacío | Severidad | Descripción |
|---|---|---|---|
| V1 | Sin tabla de severidad explícita | **ALTA** | Reglas sin clasificar como `BLOCKING` / `WARNING` / `INFO` / `ADVISORY`. El código ya usa `BLOCKING` pero la skill debería estandarizar. |
| V2 | Sin tipología de usuario | **ALTA** | No diferencia flujo para: principiante (quiero importar mi primer producto), agente de aduana (necesito validar consistencia), estudiante (ejercicio académico), revisor técnico (auditar cumplimiento). |
| V3 | Abandono legal sin reglas de cómputo | **ALTA** | Menciona abandono legal pero no define días exactos, prórrogas, rescate, ni umbrales de alerta progresiva. |
| V4 | Garantías sin algoritmo de cálculo | **ALTA** | El código `ensureGarantiaIfNeeded` ya calcula: tránsito 30 días, admisión temporal 540 días. La skill no prescribe la fórmula. |
| V5 | Canales de control sin criterios | **ALTA** | Menciona verde/naranja/rojo pero no define las reglas de asignación (valor, HS Code, perfil importador, historial, restricciones). |
| V6 | Sin reglas de machine-readable | **MEDIA** | Las reglas están en prosa. No hay formato estructurado (JSON, YAML, tabla) que permita validación automatizada. |
| V7 | Sin reglas para exportación | **MEDIA** | El skill cubre importación, reimportación y tránsito. No hay reglas para exportación definitiva (régimen 40/41). |
| V8 | Sin reglas de courier/EMS | **MEDIA** | La segmentación courier vs comercial está en `ImportacionService` (líneas 87-163) pero no en la skill. |
| V9 | Sin reglas de integridad entre tablas | **MEDIA** | No exige validar: `contenedores.precinto` vs `BL`, `documentos_transporte` vs `manifiestos_carga`, `permisos` vs `fecha_embarque`. |
| V10 | Sin reglas de liquidación de tributos | **MEDIA** | La skill menciona DTA pero no prescribe el orden: CIF → Ad Valorem → ISC → IGV → IPM → Percepción → Total. |
| V11 | Sin reglas de redondeo | **MEDIA** | Prohíbe redondeo silencioso pero no define escala (2 decimales, modo HALF_UP, etc.). |
| V12 | Sin reglas para mercancía usada | **BAJA** | `Importacion.setUsado()` existe pero la skill no trata el impacto en valoración, restricciones ni permisos. |
| V13 | Sin reglas de TLC / acuerdos comerciales | **BAJA** | `TlcService.verificarTlc()` existe. La skill no cubre preferencias arancelarias, certificados de origen ni tratados. |
| V14 | Sin reglas de trazabilidad forense | **BAJA** | La BD tiene hash chain en auditoría pero la skill no exige inmutabilidad. |

---

## 3. MEJORAS PROPUESTAS

### A. Reglas de intención del usuario

**Problema**: La skill tiene 7 preguntas iniciales pero no distingue perfil de usuario ni adapta el flujo.

**Mejora concreta**:

```
AL INICIAR, preguntar:

1. ¿Eres importador principiante, agente de aduana, estudiante o revisor técnico?
   - Principiante → flujo guiado completo con explicaciones pedagógicas
   - Agente → flujo rápido con validación de consistencia y alertas
   - Estudiante → entorno simulado sin restricciones de bloqueo
   - Revisor técnico → auditoría forense con trazabilidad completa

2. ¿Qué tipo de operación deseas realizar?
   - Importar mercancía que se queda en Perú
   - Reimportar mercancía que salió antes de Perú
   - Ingresar mercancía temporalmente
   - Enviar mercancía en tránsito por Perú
   - Transbordar mercancía a otro medio
   - Exportar mercancía desde Perú
   - No lo sé / Ayúdame a decidir

3. ¿Ya tienes algunos datos de la operación?
   - Sí, tengo HS Code / Factura / BL
   - No, empiezo desde cero

4. SI "No lo sé": mostrar árbol de decisión basado en:
   - ¿La mercancía se queda en Perú? → Importación consumo
   - ¿Solo pasa por Perú? → Tránsito
   - ¿Cambia de transporte y sale? → Transbordo
   - ¿Entra por tiempo limitado? → Admisión temporal
   - ¿Salió antes de Perú y vuelve? → Reimportación
```

### B. Reglas de siguiente acción recomendada

**Problema**: La skill define el formato pero no es obligatorio en todas las pantallas ni especifica campos faltantes con prioridad.

**Mejora concreta**: Estructura obligatoria para toda respuesta del sistema:

```json
{
  "siguienteAccion": "Registrar manifiesto de carga",
  "motivo": "Sin manifiesto no se puede validar PRE-DAM ni controlar plazos de abandono legal",
  "desbloquea": "Validación de PRE-DAM, timeline de eventos, control de plazos críticos",
  "riesgoEvita": "Abandono legal por falta de destinación oportuna, multas por fuera de plazo",
  "camposFaltantes": [
    {"campo": "numeroManifiesto", "prioridad": "ALTA", "razon": "Identificador único del viaje"},
    {"campo": "fechaTerminoDescarga", "prioridad": "ALTA", "razon": "Base para plazo de destinación 15 días"},
    {"campo": "pesoBruto", "prioridad": "MEDIA", "razon": "Control de bultos en descarga"}
  ],
  "bloqueosActivos": ["PRE_DAM_SIN_MANIFIESTO"],
  "progresoExpediente": 45
}
```

**Reglas por pantalla**:

| Pantalla | Siguiente acción | Motivo |
|---|---|---|
| Dashboard | Completar datos básicos de operación | Sin HS Code y FOB no se puede clasificar ni simular tributos |
| Clasificación arancelaria | Verificar restricciones y permisos VUCE | Mercancía restringida sin permiso bloqueará PRE-DAM |
| Manifiesto | Validar coherencia: BL vs contenedores vs precintos | Inconsistencias pueden generar canal rojo |
| PRE-DAM | Revisar DTA estimada y plazos críticos | Antes de numerar, verificar flujo de caja y fechas |
| Timeline | Verificar alertas regulatorias activas | Plazos vencidos generan abandono legal |
| Expediente completo | Descargar reporte o continuar con gestión oficial | ImportEase no reemplaza SUNAT |

### C. Reglas de bloqueo

**Problema**: La skill define 14 datos mínimos pero no especifica exactamente qué bloquea ni en qué orden.

**Mejora concreta**: Tabla de reglas de bloqueo con severidad `BLOCKING`:

| Código | Regla | Condición | Acción | Excepción |
|---|---|---|---|---|
| `BLK-001` | PRE-DAM sin manifiesto | `manifiestos_carga.operacion_id` IS NULL | Bloquear generación de PRE-DAM | Despacho anticipado (permite PRE-DAM antes de llegada, pero requiere BL) |
| `BLK-002` | PRE-DAM sin FOB válido | `valorFob < 1 \|\| valorFob > 10_000_000` | Bloquear generación de PRE-DAM | Ninguna |
| `BLK-003` | PRE-DAM sin HS Code válido | HS Code no tiene 6-10 dígitos | Bloquear generación de PRE-DAM | Ninguna |
| `BLK-004` | PRE-DAM sin documento transporte | `blMasterId` IS NULL y no hay `documentos_transporte` | Bloquear generación de PRE-DAM | Permiso académico con advertencia |
| `BLK-005` | Reimportación sin exportación previa | Régimen 36 sin `DECLARACION_EXPORTACION` cargada | Bloquear sugerencia de reimportación | Ninguna |
| `BLK-006` | Admisión temporal sin garantía | Régimen ADM_TEMP sin registro en `garantias_aduaneras` | Bloquear numeración PRE-DAM | Garantía global vigente del importador |
| `BLK-007` | Tránsito sin ruta declarada | Régimen 80 sin aduana destino o ruta | Bloquear numeración PRE-DAM | Ninguna |
| `BLK-008` | Transbordo sin salida programada | Régimen TRANSBORDO sin aduana/aeropuerto salida | Bloquear numeración PRE-DAM | Ninguna |
| `BLK-009` | Mercancía restringida sin permiso | HS Code en `matriz_restricciones_hs` y `permiso_vuce_obtenido = FALSE` | Bloquear PRE-DAM con alerta CITES/MTC/DIGEMID si aplica | Envío académico |
| `BLK-010` | DTA sin moneda/tipo cambio | `tipoCambio <= 0` o moneda no definida | Bloquear DTA | Ninguna |
| `BLK-011` | Fuente desconocida | `source_type = 'UNKNOWN'` en datos críticos | Bloquear acción y exigir verificación manual | Modo académico |
| `BLK-012` | RUC no válido | `ruc_validado = FALSE \|\| estado_ruc != 'ACTIVO' \|\| condicion_ruc = 'NO_HABIDO'` | Bloquear PRE-DAM | Ninguna |
| `BLK-013` | FOB sin Incoterm | `incoterm` IS NULL | Bloquear construcción de CIF | Ninguna |
| `BLK-014` | País de origen no definido | `paisOrigen` IS NULL | Bloquear clasificación arancelaria | Ninguna |

### D. Reglas de explicación al usuario

**Problema**: La skill tiene 3 frases obligatorias pero faltan mensajes pedagógicos para cada bloqueo.

**Mejora concreta**: Mensajes obligatorios para cada bloqueo:

| Contexto | Mensaje obligatorio |
|---|---|
| PRE-DAM bloqueada | "La PRE-DAM no puede generarse porque faltan datos obligatorios: [lista]. Estos datos son requeridos por SUNAT para iniciar el despacho aduanero según [norma]." |
| Manifiesto faltante | "El manifiesto de carga es el documento que acredita la llegada legal de la mercancía al territorio aduanero. Sin él, no es posible vincular la carga, controlar plazos de destinación ni evitar el abandono legal (artículo 178 LGA)." |
| Abandono legal inminente | "⚠️ La mercancía corre riesgo de ABANDONO LEGAL en [X] días. El abandono legal permite a SUNAT rematar, adjudicar o destruir la mercancía. Puedes solicitar prórroga de destinación por 15 días adicionales o realizar el rescate pagando multas (artículos 178-180 LGA)." |
| Garantía requerida | "El régimen de [admisión temporal/tránsito] requiere una garantía mínima de S/ [monto] para cubrir eventuales tributos. La garantía se libera al [reexportar/concluir el tránsito]." |
| Canal rojo | "Para reimportación, el canal de control es OBLIGATORIAMENTE ROJO (reconocimiento físico). Debes presentar la DAM de exportación original regularizada y probar documentalmente que la mercancía es idéntica." |
| DTA referencial | "La Deuda Tributaria Aduanera calculada es referencial y estimada. La SUNAT aplica su propia liquidación al momento de la numeración oficial de la DAM. Usa este cálculo solo para planificación financiera." |
| Incoterm inconsistente | "Si declaras FOB, el flete y seguro deben registrarse por separado para construir el valor CIF. Si no tienes póliza de seguro, SUNAT aplicará tabla de seguro promedio, lo que puede incrementar la base imponible." |
| Documento faltante | "El documento [nombre] es obligatorio según [norma]. Sin él, la SUNAT puede rechazar la declaración o asignar canal rojo automático." |
| Fuente desconocida | "Este dato proviene de una fuente NO VERIFICADA (UNKNOWN). No debe usarse para decisiones operativas ni legales sin validación previa." |

### E. Canales de control (verde/naranja/rojo)

**Problema**: La skill menciona canales pero no define reglas de asignación.

**Mejora concreta**: Reglas de asignación de canal de control:

```json
{
  "REGLAS_ASIGNACION_CANAL": [
    {
      "condicion": "Reimportación (régimen 36)",
      "canal": "ROJO",
      "esOficial": true,
      "motivo": "Canal rojo obligatorio según DESPA-PG.26.1 para verificar identidad física de la mercancía."
    },
    {
      "condicion": "Admisión temporal (ADM_TEMP) SIN garantía registrada",
      "canal": "ROJO",
      "esOficial": false,
      "motivo": "Simulación: garantía pendiente eleva perfil de riesgo."
    },
    {
      "condicion": "Mercancía restringida SIN permiso sectorial",
      "canal": "ROJO",
      "esOficial": false,
      "motivo": "Riesgo regulatorio alto. Se requiere permiso VUCE antes del despacho."
    },
    {
      "condicion": "Importador nuevo (primera importación) Y FOB > 10,000 USD",
      "canal": "NARANJA",
      "esOficial": false,
      "motivo": "Perfil de riesgo por falta de historial."
    },
    {
      "condicion": "RUC NO HABIDO o estado no ACTIVO",
      "canal": "ROJO",
      "esOficial": false,
      "motivo": "Importador con situación tributaria irregular."
    },
    {
      "condicion": "FOB entre 1 y 2000 USD (courier, régimen simplificado)",
      "canal": "VERDE",
      "esOficial": false,
      "motivo": "Franquicia courier de bajo valor."
    },
    {
      "condicion": "Caso contrario (sin factores de riesgo detectados)",
      "canal": "VERDE",
      "esOficial": false,
      "motivo": "Perfil de riesgo bajo según evaluación referencial."
    }
  ]
}
```

### F. Abandono legal

**Problema**: Solo mencionado. Sin reglas de cómputo, alertas progresivas ni rescate.

**Mejora concreta**:

```
REGLAS DE ABANDONO LEGAL:

1. Cómputo de días:
   - Diferido: 15 días calendario desde el día siguiente del término de descarga
   - Anticipado: la mercancía debe arribar dentro del plazo de la DAM
   - Urgente: hasta 15 días después del término de descarga
   - Tránsito: 30 días desde el levante (general) o 10 días (terrestre)
   - Transbordo: 30 días desde numeración
   - Admisión temporal: 18 meses desde levante

2. Alertas progresivas:
   - FALTAN > 30% del plazo → VERDE (sin riesgo)
   - FALTAN entre 10% y 30% → NARANJA (riesgo medio)
   - FALTAN < 10% del plazo → ROJO (riesgo alto)
   - PLAZO VENCIDO → CRÍTICO (abandono legal consumado)

3. Rescate:
   - Se puede solicitar prórroga de destinación por 15 días adicionales
   - El rescate exige pago de multas (25% de tributos) + tasas almacén
   - El abandono legal permite a SUNAT: rematar, adjudicar o destruir

4. Eventos de abandono a modelar:
   - ABANDONO_LEGAL_CONSUMADO
   - PRORROGA_DESTINACION
   - RESCATE_MERCANCIA
   - REMATE_ADJUDICACION
```

### G. Garantías

**Problema**: Mencionadas para admisión temporal y tránsito. Sin algoritmo, vencimiento ni liberación.

**Mejora concreta**:

```
REGLAS DE GARANTÍAS:

1. ¿Cuándo se requiere garantía?
   - Admisión temporal (ADM_TEMP): SIEMPRE
   - Tránsito aduanero (80): SIEMPRE
   - Transbordo con ingreso a depósito (M3_DEPOSITO): Recomendado
   - Importación consumo: No requiere (se pagan tributos)

2. Cálculo del monto mínimo:
   - Tránsito: MAX(FOB, CIF) × 1.10 (110% del valor)
   - Admisión temporal: MAX(TotalImpuestos, CIF × 0.25)
   - Garantía global: acumulado de operaciones activas

3. Vencimiento:
   - Tránsito: 30 días desde numeración
   - Admisión temporal: 540 días desde levante
   - Prórroga: renovación antes del vencimiento

4. Estados de garantía:
   - PENDIENTE → VIGENTE → VENCIDA → LIBERADA → EJECUTADA

5. Liberación:
   - Tránsito: al concluir en aduana de destino con regularización
   - Admisión temporal: al reexportar o nacionalizar la mercancía
   - Se requiere: DAM de regularización + documento de salida
```

---

## 4. NUEVA VERSIÓN MEJORADA RESUMIDA DE LA SKILL

A continuación, una versión concisa y accionable de la skill incorporando todas las mejoras:

```markdown
# ImportEase Domain Rules (v2 — Mejorada)

## Cuándo usar
Usar cuando el agente deba analizar, diseñar, validar o implementar lógica
aduanera peruana: SUNAT, DAM, PRE-DAM, manifiesto, BL/AWB, DTA, regímenes,
despachos, restricciones, VUCE, plazos, abandono legal, garantías, canales
de control, fuentes oficiales vs simuladas.

## Principios
1. ImportEase NO reemplaza SUNAT, VUCE ni agentes de aduana.
2. Toda PRE-DAM es REFERENCIAL o SIMULADA, nunca OFICIAL.
3. Toda fuente debe etiquetarse con sourceType + confidence.
4. El sistema explica SIEMPRE por qué bloquea, permite, alerta o recomienda.
5. Cada pantalla debe mostrar: siguiente acción, motivo, desbloqueo, riesgo,
   campos faltantes (con prioridad ALTA/MEDIA/BAJA), bloqueos activos y
   progreso del expediente.

## Tipología de usuario
- PRINCIPIANTE: flujo guiado completo, explicaciones pedagógicas obligatorias
- AGENTE: flujo rápido, validación de consistencia + alertas
- ESTUDIANTE: simulación académica, sin bloqueos reales, con advertencias
- REVISOR_TECNICO: auditoría forense, trazabilidad completa + hash chain

## Reglas de bloqueo (BLOCKING)
Ver tabla formal en sección 3.C. Deben implementarse en orden de prioridad.

## Reglas de fuente de datos (sourceType + confidence)
| sourceType | confidence | Etiqueta UI |
|---|---|---|
| OFICIAL_API | 0.98 | Fuente oficial (API SUNAT/BCRP) |
| OFFICIAL_PROCEDURE | 0.96 | Procedimiento oficial SUNAT |
| CACHE_OFFICIAL | 0.90 | Cache de fuente oficial |
| BD_LOCAL | 0.85 | Base de datos local referencial |
| SYSTEM_RULE | 0.78 | Regla de sistema automatizada |
| REFERENTIAL | 0.60 | Dato referencial |
| MANUAL_USER_INPUT | 0.50 | Dato ingresado por usuario |
| SIMULATED | 0.20 | Simulación académica |
| UNKNOWN | 0.00 | Fuente no identificada |

## Frases obligatorias por contexto
Ver tabla completa en sección 3.D. Incluir siempre sourceType + confidence
en cada respuesta.

## Reglas de canales de control
Ver tabla formal en sección 3.E.

## Reglas de abandono legal
Ver reglas de cómputo, alertas progresivas y rescate en sección 3.F.

## Reglas de garantías
Ver condiciones, cálculo, vencimiento y liberación en sección 3.G.

## Salida esperada del agente
Incluir siempre: explicación funcional, impacto en flujo aduanero, archivos
afectados, reglas aplicadas (con código), validaciones necesarias, pruebas
mínimas, riesgos identificados, criterios de aceptación.
```

---

## 5. CASOS DE USO

### ✅ Cuándo INVOCAR esta skill

| Situación | Razón |
|---|---|
| Diseñar lógica de PRE-DAM / DAM | Reglas de datos mínimos, sourceType, etiquetado |
| Implementar validación de manifiesto de carga | BLK-001, requireOwned, coherencia BL-contenedor |
| Calcular DTA | Fórmula: CIF → AdValorem → ISC → IGV → IPM → Percepción |
| Controlar plazos críticos | Abandono legal, destinación, reimportación, transbordo |
| Asignar canal de control | Reglas de canal verde/naranja/rojo |
| Gestionar garantías | Admisión temporal, tránsito |
| Validar restricciones sectoriales | CITES/SERFOR, DIGEMID, MTC, VUCE |
| Auditar expediente aduanero | Trazabilidad, fuente, confianza, hash chain |
| Clasificar régimen aduanero | Árbol de decisión del flujo guiado |
| Segmentar courier vs comercial | FOB < 200 / FOB 200-2000 / FOB > 2000 |

### ❌ Cuándo NO invocar esta skill

| Situación | Razón |
|---|---|
| Lógica de marketplaces, compras, pagos | Dominio fuera de aduanas |
| Tracking de envíos internacionales | No es tracking logístico, es expediente aduanero |
| Cálculo de costos logísticos internos | Solo cubre tributos aduaneros |
| Gestión de inventarios o almacenes | Fuera del alcance aduanero |
| RRHH, facturación comercial | Dominio no relacionado |
| Implementación UI/UX pura | Skill funcional, no de frontend |

---

## 6. REGLAS FUNCIONALES OBLIGATORIAS (priorizadas)

### Prioridad 1 — Reglas de bloqueo absoluto (BLOCKING)

| Código | Regla | ¿Implementado? |
|---|---|---|
| BLK-001 | PRE-DAM sin manifiesto de carga | Sí (PredamValidationService) |
| BLK-002 | PRE-DAM sin FOB válido (1-10MM) | Sí |
| BLK-003 | PRE-DAM sin HS Code válido (6-10 díg) | Sí |
| BLK-004 | PRE-DAM sin documento transporte | Sí (checkDocTransporte) |
| BLK-005 | Reimportación sin exportación previa | Sí (evaluarReimportacion) |
| BLK-006 | Admisión temporal sin garantía | Parcial (ensureGarantiaIfNeeded) |
| BLK-007 | Tránsito sin ruta declarada | Falta en skill/código |
| BLK-008 | Transbordo sin salida programada | Falta en skill/código |
| BLK-009 | Merc. restringida sin permiso sectorial | Sí |
| BLK-010 | DTA sin moneda/tipo cambio | Parcial (tipoCambioService) |
| BLK-011 | Fuente desconocida en datos críticos | No implementado |
| BLK-012 | RUC no activo/habido | Sí (checkImporterIdentity) |
| BLK-013 | Sin Incoterm | Sí (checkCoreOperationFields) |
| BLK-014 | Sin país de origen | Sí |

### Prioridad 2 — Reglas de advertencia (WARNING)

| Código | Regla |
|---|---|
| WRN-001 | Abandono legal próximo (10-30% plazo restante) |
| WRN-002 | Incoterm FOB sin flete/seguro registrado |
| WRN-003 | Permiso sectorial próximo a vencer |
| WRN-004 | Garantía próxima a vencer |
| WRN-005 | Diferencia de pesos entre BL y recepción |
| WRN-006 | Precinto violado en contenedor |
| WRN-007 | Importador nuevo sin historial |
| WRN-008 | Mercancía usada sin declaración expresa |

### Prioridad 3 — Reglas informativas (INFO)

| Código | Regla |
|---|---|
| INF-001 | Canal asignado y motivo |
| INF-002 | DTA estimada con fuente/confianza |
| INF-003 | Timeline de eventos estimados |
| INF-004 | Documentos faltantes (no bloqueantes) |
| INF-005 | TLC aplicable y % preferencia |
| INF-006 | Próximo evento del calendario aduanero |

---

## 7. CRITERIOS DE ÉXITO

| # | Criterio | Métrica |
|---|---|---|
| 1 | Todo dato tiene sourceType + confidence explícitos | 100% de respuestas del sistema incluyen metadatos de fuente |
| 2 | PRE-DAM siempre marcada como SIMULATED o REFERENTIAL | 0% de casos donde PRE-DAM aparezca como OFICIAL |
| 3 | Bloqueos activos visibles con motivo normativo | Cada bloqueo incluye código (BLK-XXX) + artículo de la LGA |
| 4 | Siguiente acción visible en toda pantalla | Dashboard, expediente, PRE-DAM, timeline muestran acción recomendada |
| 5 | Plazos calculados con evento base verificable | Todo plazo muestra: código, evento base, fecha base, fecha límite, días restantes |
| 6 | Abandono legal alertado con 30% de anticipación | Alertas progresivas: FALTAN < 30% → naranja, < 10% → rojo |
| 7 | Usuario principiante completa flujo sin SUNAT | El flujo guiado lo lleva de intención → expediente sin depender de API SUNAT |
| 8 | Usuario técnico puede auditar cada decisión | Cada decisión del sistema muestra: regla aplicada (código), fuente normativa, timestamp, hash de auditoría |
| 9 | Garantías calculadas automáticamente | Para ADM_TEMP y tránsito: monto mínimo, fecha vencimiento, estado |
| 10 | Canales de control asignados por reglas explícitas | Verde/naranja/rojo con motivo documentado, visible en UI |
| 11 | Responsive a perfil de usuario | El flujo se adapta según: principiante / agente / estudiante / revisor técnico |
| 12 | 100% de reglas de bloqueo tienen test automatizado | Cada BLK-XXX tiene un test unitario que verifica bloqueo y mensaje |
