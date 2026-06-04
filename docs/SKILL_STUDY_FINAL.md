# ESTUDIO INTEGRADO DE 3 SKILLS — IMPORTEASE ADUANERO

Fecha: 2026-05-30
Proyecto: ImportEase Aduanero (Java/JSP/Servlets/MySQL)

---

## 1. EVALUACIÓN GENERAL DE LAS 3 SKILLS

### 1.1 `importease-aduanas-domain-rules`

| Aspecto | Evaluación |
|---------|------------|
| **Utilidad** | 8/10 — Esencial para mantener coherencia aduanera. Sin esta skill, Codex implementaría reglas genéricas sin contexto peruano. |
| **Claridad** | 7/10 — Buenas secciones de reglas, pero en prosa sin formato estructurado. |
| **Vacíos** | 14 detectados. Críticos: sin tabla de severidad (BLOCKING/WARNING/INFO), sin tipología de usuario, abandono legal sin cómputo, garantías sin algoritmo, canales de control sin criterios. |
| **Riesgos** | Codex puede implementar bloqueos sin saber si son HARD/SOFT. Puede tratar al usuario principiante igual que a un agente de aduana. |
| **Mejoras** | Agregar tabla de severidad explícita, tipología de usuario (4 perfiles), reglas de abandono legal, fórmula de garantías, criterios de canal de control, reglas para courier/exportación/TLC. |
| **Prioridad** | P1 — Debe usarse primero en todo cambio funcional. |
| **Cuándo usarla** | Al diseñar flujo, implementar reglas de negocio, validar coherencia aduanera. |
| **Cuándo NO** | UI/UX puro, infraestructura, tests técnicos sin lógica aduanera. |

### 1.2 `importease-qa-offensive-audit`

| Aspecto | Evaluación |
|---------|------------|
| **Utilidad** | 7/10 — Sólida en 12 categorías de seguridad web clásica, pero omite 6 categorías modernas. |
| **Claridad** | 8/10 — Excelente formato de hallazgo con 11 campos. Severidad bien definida. |
| **Vacíos** | 6 críticos: race conditions, rate limiting generalizado, mass assignment, SSRF, log injection, integridad documental forense. Payloads de SQLi/XSS demasiado básicos. |
| **Riesgos** | Codex puede dejar pasar race conditions, mass assignment por Gson.fromJson(), path traversal con bypass `....//`. Ya hay vulnerabilidad real detectada en `DocumentoController.java:114`. |
| **Mejoras** | Agregar 6 nuevas categorías (K-R), 50 pruebas ofensivas específicas, payloads avanzados (time-based SQLi, DOM XSS, CRLF injection en logs). |
| **Prioridad** | P1 — Debe ejecutarse antes de aprobar cualquier cambio. |
| **Cuándo usarla** | Después de implementar cambios, antes de merge. También antes del refactor para conocer attack surface actual. |
| **Cuándo NO** | Durante diseño conceptual inicial, cuando no hay código que probar. |

### 1.3 `importease-java-jsp-refactor`

| Aspecto | Evaluación |
|---------|------------|
| **Utilidad** | 6/10 — Excelentes principios (no reescribir, mantener stack), pero débil en concreción. |
| **Claridad** | 5/10 — Da nombres de servicios/DTOs pero sin campos, responsabilidades ni ejemplos. |
| **Vacíos** | 8 críticos: no detecta god class `AduanasService` (1177 líneas), no menciona `Map<String, Object>` como antipatrón, no da campos de DTOs, no da responsabilidades de servicios, no dice qué líneas mover, no planifica orden de refactor. |
| **Riesgos** | Codex puede crear servicios sin responsabilidad clara, duplicar lógica, o no refactorizar la god class por no tener instrucciones explícitas. |
| **Mejoras** | 12 servicios con responsabilidades exactas, 14 DTOs con campos, 6 tablas SQL con columnas, 15 pasos de refactor incremental, 11 suites de prueba con escenarios concretos. |
| **Prioridad** | P1 — Debe usarse durante toda implementación técnica. |
| **Cuándo usarla** | Al crear/modificar servicios, servlets, DAOs, JSP. Al planificar arquitectura. |
| **Cuándo NO** | Cuando no hay cambios técnicos (solo documentación, solo testing funcional). |

---

## 2. VERSIÓN MEJORADA DE CADA SKILL (RESUMIDA)

### 2.1 `importease-aduanas-domain-rules` — Versión Mejorada

```markdown
# Domain Rules (v2 mejorada)

## Uso
- **INVOCAR** al diseñar flujo, implementar reglas de negocio, validar coherencia aduanera
- **NO INVOCAR** para UI, infraestructura, tests técnicos puros

## Reglas por severidad

### BLOCKING (impide acción)
BLK-001: PRE-DAM sin manifiesto
BLK-002: PRE-DAM sin FOB (1-10MM USD)
BLK-003: PRE-DAM sin HS Code válido (6-10 dígitos)
BLK-004: PRE-DAM sin documento de transporte
BLK-005: Reimportación sin exportación precedente
BLK-006: Admisión temporal sin garantía
BLK-007: Tránsito sin ruta declarada
BLK-008: Transbordo sin salida programada
BLK-009: Mercancía restringida sin permiso sectorial
BLK-010: DTA sin moneda/tipo cambio
BLK-011: Fuente desconocida en dato crítico
BLK-012: RUC no activo/no habido
BLK-013: Sin Incoterm (cuando aplica)
BLK-014: Sin país de origen

### WARNING (advierte, no bloquea)
WRN-001: Abandono legal próximo (10-30% plazo restante)
WRN-002: Incoterm FOB sin flete/seguro registrado
WRN-003: Permiso sectorial próximo a vencer
WRN-004: Garantía próxima a vencer
WRN-005: Diferencia de pesos entre BL y recepción
WRN-006: Precinto violado en contenedor
WRN-007: Importador nuevo sin historial
WRN-008: Mercancía usada sin declaración expresa

### INFO (solo informativo)
INF-001: Canal asignado y motivo
INF-002: DTA estimada con fuente/confianza
INF-003: Timeline de eventos estimados
INF-004: Documentos faltantes no bloqueantes
INF-005: TLC aplicable y % preferencia
INF-006: Próximo evento calendario aduanero

## Flujo guiado por perfil
- PRINCIPIANTE: Pasos detallados con explicaciones pedagógicas
- AGENTE ADUANA: Pasos resumidos, validaciones técnicas visibles
- ESTUDIANTE: Marco normativo visible, referencias a LGA/vía postal
- REVISOR TÉCNICO: Auditoría, hash chain, metadata de fuente, reglas aplicadas

## Preguntas de intención (orden estricto)
1. ¿La mercancía se quedará definitivamente en Perú? → Importación consumo / otra
2. ¿Fue exportada antes desde Perú? → Reimportación / otra
3. ¿Ingresará solo por tiempo determinado? → Admisión temporal / otra
4. ¿Solo pasará por Perú hacia otro destino? → Tránsito / otra
5. ¿Se cambiará de un medio de transporte a otro? → Transbordo / otra
6. ¿Es courier (FOB < 2000 USD)? → Segmentación courier
7. ¿Existe documento de transporte o manifiesto? → Anticipado / diferido / urgente

## DTA (orden de cálculo estricto)
CIF → Ad Valorem (0/4/6/11%) → ISC (si aplica) → IGV (16%) + IPM (2%) → Percepción (10%/3.5%)
BigDecimal siempre. Escala: 2 decimales, HALF_UP. Nunca double.
Output: desglose por tributo + total + fuente + fecha + moneda + advertencia "REFERENCIAL"

## Abandono legal
- Diferido: 30 días calendario desde término de descarga
- Anticipado: 15 días desde llegada
- Alertas: FALTAN < 30% plazo → naranja, < 10% → rojo
- Rescate: posible dentro de plazo con pago de tributos + multa

## Garantías
- Admisión temporal: 540 días (prorrogable) + monto = tributos suspendidos + 10%
- Tránsito: 30 días naturales + monto = 5% valor FOB
- Estado: ACTIVA / VENCIDA / LIBERADA / EJECUTADA
```

### 2.2 `importease-qa-offensive-audit` — Versión Mejorada (v2.1)

```markdown
# QA Offensive Audit (v2.1)

## 18 categorías de prueba (vs 12 originales)

A. Autenticación y sesión
B. Autorización e IDOR
C. XSS (DOM-based, stored, reflected)
D. SQL Injection (time-based, error-based, union, out-of-band, ORDER BY)
E. CSRF
F. Carga documental insegura
G. Flujo aduanero roto
H. Flujo guiado roto
I. Fechas y plazos
J. DTA y cálculos
K. Fuente y trazabilidad
L. Errores y resiliencia
M. Race conditions (NUEVA)
N. Rate limiting (NUEVA)
O. Mass Assignment (NUEVA)
P. SSRF (NUEVA)
Q. Log Injection (NUEVA)
R. Integridad documental (NUEVA)

## Top 10 riesgos críticos (actualizado)
1. IDOR en expediente aduanero — AduanasController:41 opera con operacionId sin validación granular
2. Path traversal con bypass (`....//` supera `replace("../")`) — DocumentoController:114
3. Race condition en state machine — sin SELECT FOR UPDATE en cambios de estado
4. PRE-DAM generada sin validación completa en backend
5. Mass Assignment via Gson.fromJson() directo en 4 endpoints
6. DTA engañosa sin badge REFERENCIAL o sin fuente
7. Upload sin rate limiting — ZIP bomb, PDF con JS, doble extensión
8. CSRF inexistente en algunos endpoints POST
9. Log Injection — datos de usuario escritos sin sanitizar CRLF
10. Session fixation — no invalidar sesión previa en login

## Formato de hallazgo (mejorado con 3 campos adicionales)
ID | Categoría | Severidad | Dónde ocurre | Cómo romperlo | Resultado esperado | Resultado peligroso | Impacto | Causa probable | Corrección | Prueba regresión | CVE-like code (NUEVO) | PoC curl (NUEVO) | Regla asociada (NUEVO)

## 50 pruebas ofensivas (ver documento completo SKILL_STUDY_AGENT2.md)
## Criterios de aprobación
- 0 hallazgos CRÍTICOS
- 0 hallazgos ALTOS sin plan de corrección
- 100% de pruebas ofensivas de la categoría afectada ejecutadas
```

### 2.3 `importease-java-jsp-refactor` — Versión Mejorada

```markdown
# Java JSP Refactor (v2 mejorada)

## Prohibiciones absolutas
1. NO crear nuevas clases en paquete `negocio.*` — usar subpaquetes
2. NO devolver `Map<String, Object>` — usar DTOs tipados
3. NO poner lógica de régimen/modalidad/DTA/plazos en JSP
4. NO aceptar acciones críticas por GET
5. NO exponer stacktrace al usuario
6. NO usar double para dinero — BigDecimal siempre

## Arquitectura forzada
JSP (solo render) → Servlet (auth + autorización + CSRF + delegación) → Service → DAO
Paquetes: controller, service, service.validation, service.rules, dao, dto, model, security, audit, util

## Servicios con responsabilidades exactas
- RegimenService.determinarRegimen(input) → RegimenDTO
- ModalidadService.determinarModalidad(input) → ModalidadDTO
- PredamService.generarPredam(expedienteId) → PredamDTO
- DtaService.calcularDta(expedienteId) → DtaDTO
- HealthPanelService.calcularSalud(expedienteId) → HealthPanelDTO
- NextActionService.calcularSiguienteAccion(expedienteId) → NextActionDTO
- GuidedFlowService.obtenerPasoActual(expedienteId) → GuidedStepDTO
- CoherenciaAduaneraService.verificarCoherencia(expedienteId) → List<CoherenciaIssueDTO>
- DocumentoChecklistService.obtenerChecklist(expedienteId) → List<ChecklistItemDTO>
- PlazoCriticoService.calcularPlazos(expedienteId) → List<PlazoCriticoDTO>
- AuthorizationService.verificarAcceso(usuarioId, expedienteId) → boolean
- AuditService.registrarEvento(evento) → void

## DTOs con campos mínimos requeridos
Todo DTO crítico: + FuenteMetadata metadata (source, sourceType, confidence, generatedAt, isOfficial, isSimulated)

## Refactor AduanasService (1177 líneas) — orden estricto
1. DTOs (sin impacto funcional)
2. AuthorizationService
3-11. Servicios individuales + tests cada uno
12. ExpedienteService (orquestador)
13. AuditService + tests
14. Nuevo ExpedienteController
15. Deprecar AduanasService

## Tablas nuevas con columnas
expediente_guided_steps, expediente_validaciones, expediente_next_actions,
expediente_plazos, expediente_coherencia_issues, expediente_audit_events,
fuente_metadata, reglas_normativas
```

---

## 3. FLUJO RECOMENDADO DE USO EN CODEX

```
FASE 1 — ANÁLISIS (antes de tocar código)
┌─────────────────────────────────────────────────────────────┐
│ 1. importease-aduanas-domain-rules                            │
│    → Analizar coherencia funcional del flujo solicitado       │
│    → Determinar qué régimen, modalidad, documentos aplican    │
│    → Identificar reglas de bloqueo necesarias                 │
│    → Documentar reglas normativas aplicables                  │
└─────────────────────────────────────────────────────────────┘

FASE 2 — IMPLEMENTACIÓN
┌─────────────────────────────────────────────────────────────┐
│ 2. importease-java-jsp-refactor                               │
│    → Crear/refactorizar servicios (nunca en negocio.* plano)  │
│    → Crear DTOs tipados (nunca Map<String, Object>)           │
│    → Refactorizar servlets delgados                           │
│    → JSP solo renderiza, nunca calcula                        │
│    → Agregar PreparedStatement, BigDecimal, autorización      │
│    → Agregar pruebas JUnit por cada nuevo servicio            │
└─────────────────────────────────────────────────────────────┘

FASE 3 — QA OFENSIVO (antes de aprobar)
┌─────────────────────────────────────────────────────────────┐
│ 3. importease-qa-offensive-audit                               │
│    → Intentar romper el flujo guiado (saltar pasos, etc.)     │
│    → Probar autorización por expediente (IDOR)                │
│    → Probar carga documental (path traversal, ZIP bomb, etc.) │
│    → Probar race conditions, rate limiting, mass assignment   │
│    → Probar lógica aduanera (DTA con FOB negativo, etc.)      │
│    → Probar salida visual (badges, fuentes, advertencias)     │
└─────────────────────────────────────────────────────────────┘

FASE 4 — CORRECCIÓN
┌─────────────────────────────────────────────────────────────┐
│ 4. Repetir Fase 2 y 3 hasta 0 hallazgos CRÍTICOS + ALTOS    │
└─────────────────────────────────────────────────────────────┘

FASE 5 — EJECUTAR PRUEBAS
┌─────────────────────────────────────────────────────────────┐
│ 5. mvn test → 100% tests pasando                            │
└─────────────────────────────────────────────────────────────┘

FASE 6 — DOCUMENTAR CRITERIOS DE ACEPTACIÓN
┌─────────────────────────────────────────────────────────────┐
│ 6. Checklist de calidad (ver sección 6) completada           │
└─────────────────────────────────────────────────────────────┘
```

**Variante**: Si el cambio es puramente técnico (refactor sin lógica aduanera nueva), se puede saltar Fase 1. Si el cambio es de seguridad, usar QA Ofensivo (Fase 3) ANTES del refactor para conocer el attack surface actual.

---

## 4. MAPA DE RESPONSABILIDADES

| Área | Skill Responsable | Justificación |
|------|-------------------|---------------|
| **Régimen aduanero** | domain-rules | Reglas normativas de régimen 10, 21, 23, 40, 50, 60, 70 |
| **Modalidad despacho** | domain-rules | Anticipado/diferido/urgente con plazos y condiciones |
| **Manifiesto de carga** | domain-rules + java-refactor | Reglas: domain-rules (qué validar). Implementación: java-refactor (ManifiestoService) |
| **Documentos** | domain-rules + java-refactor | Reglas: domain-rules (docs por régimen). Validación: java-refactor (DocumentoValidationService) |
| **DTA** | domain-rules + java-refactor | Fórmula: domain-rules. Cálculo BigDecimal: java-refactor (DtaService) |
| **PRE-DAM** | domain-rules + java-refactor + qa-audit | Reglas: domain-rules (14 campos mínimos). Implementación: java-refactor (PredamService). QA: que nunca sea oficial |
| **Plazos críticos** | domain-rules + java-refactor | Reglas: domain-rules (abandono, destinación). Cálculo: java-refactor (PlazoCriticoService) |
| **Fuentes/metadata** | domain-rules + qa-audit | Clasificación: domain-rules (8 tipos). QA: que fuente desconocida no desbloquee acciones |
| **UI / Frontend** | java-refactor | JSP solo render, fetch a servlets. Sin lógica de negocio |
| **Backend / Servlets** | java-refactor | 9 pasos obligatorios por servlet |
| **Base de datos** | java-refactor | 8 tablas sugeridas + PreparedStatement + hash chain |
| **QA / Seguridad** | qa-audit | 18 categorías, 50 pruebas, top 10 riesgos |
| **Auditoría** | domain-rules + java-refactor + qa-audit | domain-rules: qué auditar. java-refactor: cómo auditar (AuditService, hash chain). qa-audit: que auditoría sea inmutable |

---

## 5. BACKLOG GENERADO DESDE LAS SKILLS

### Épica 1: Flujo guiado

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| Crear GuidedFlowService + GuidedStepDTO | java-refactor | negocio/GuidedFlowService.java, dto/GuidedStepDTO.java | P1 | Devuelve paso actual según datos del expediente |
| Crear HealthPanelService + HealthPanelDTO | java-refactor | negocio/HealthPanelService.java, dto/HealthPanelDTO.java | P1 | 8 indicadores: completitud, riesgos, estado |
| Crear NextActionService + NextActionDTO | java-refactor | negocio/NextActionService.java, dto/NextActionDTO.java | P1 | Cada pantalla tiene: acción, motivo, desbloqueo, riesgo |
| Implementar preguntas de intención en wizard | domain-rules | WizardServlet.java, importacion-aduanero.jsp | P1 | 7 preguntas en orden => régimen recomendado |
| Backend validation wizard steps | qa-audit | WizardServlet.java, PredamValidationService.java | P1 | No se puede saltar del paso 1 al 7 |

### Épica 2: PRE-DAM

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| PredamService con 14 validaciones BLOCKING | domain-rules + java-refactor | PredamService.java, PredamResponseDTO.java | P1 | 14 campos mínimos validados antes de generar |
| Badge "SIMULADO" en PRE-DAM | qa-audit | expediente-aduanero.jsp, PredamResponseDTO.java | P1 | PRE-DAM siempre visible con badge rojo SIMULADO |
| Hash chain en generación PRE-DAM | java-refactor | ExpedienteAuditService.java | P2 | Cada PRE-DAM tiene hash encadenado |

### Épica 3: DTA

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| DtaService con BigDecimal estricto | java-refactor | DtaService.java, DtaResponseDTO.java | P1 | Cálculo: CIF → AV → ISC → IGV+IPM → Percepción |
| DTA con badge REFERENCIAL | qa-audit | expediente-aduanero.jsp | P1 | Badge visible + advertencia "no oficial" |
| DTA con desglose completo | domain-rules | DtaResponseDTO.java | P1 | AV, ISC, IGV, IPM, Percepción, Total + fuente |

### Épica 4: Refactor AduanasService

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| Extraer RegimenService | java-refactor | RegimenService.java, RegimenDTO.java | P1 | AduanasService.evaluarRegimen() delega |
| Extraer ModalidadService | java-refactor | ModalidadService.java, ModalidadDTO.java | P1 | AduanasService.evaluarModalidad() delega |
| Extraer PredamService | java-refactor | PredamService.java | P1 | AduanasService.generarPredam() delega |
| Extraer DtaService | java-refactor | DtaService.java | P1 | AduanasService.ensureDta() delega |
| Extraer CoherenciaAduaneraService | java-refactor | CoherenciaAduaneraService.java | P2 | Separa validación de coherencia |
| AuthorizationService | java-refactor + qa-audit | AuthorizationService.java | P1 | requireOwned() reutilizable, no embebido |

### Épica 5: QA Ofensivo

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| Fix path traversal `....//` | qa-audit | DocumentoController.java:114 | P1 | `....//....//` no debe superar validación |
| Rate limiting en upload | qa-audit | DocumentoController.java | P1 | Máximo 5 uploads/minuto por usuario |
| SELECT FOR UPDATE en state changes | qa-audit | OperacionDAO.java | P1 | Race condition no debe permitir doble state change |
| Fix mass assignment Gson.fromJson() | qa-audit | AduanasController.java | P2 | Solo campos esperados en JSON |
| Validar Session fixation en login | qa-audit | LoginController.java | P1 | Login exitoso crea nueva sesión |
| CRLF sanitization en logs | qa-audit | AuditService.java | P2 | CRLF escapado antes de escribir log |

### Épica 6: Base de datos

| Tarea | Skill | Archivos probables | Prioridad | Criterio de aceptación |
|-------|-------|--------------------|-----------|------------------------|
| Crear expediente_guided_steps | java-refactor | upgrade_v4.0_flujo_guiado.sql | P2 | Tabla con paso, estado, fecha, metadata |
| Crear expediente_next_actions | java-refactor | upgrade_v4.0_flujo_guiado.sql | P2 | Tabla con acción, motivo, prioridad |
| Crear expediente_plazos | java-refactor | upgrade_v4.0_flujo_guiado.sql | P2 | Tabla con código, evento, fecha, plazo, riesgo |
| Crear reglas_normativas | java-refactor | upgrade_v4.0_flujo_guiado.sql | P2 | Catálogo de reglas BLK/WRN/INF |

---

## 6. CHECKLIST DE CALIDAD FINAL

Usar este checklist para validar cualquier cambio antes de aprobarlo:

### Coherencia aduanera
- [ ] El flujo tiene sentido para un usuario que no conoce SUNAT
- [ ] PRE-DAM está marcada como SIMULADO/REFERENCIAL (nunca OFICIAL)
- [ ] DTA muestra advertencia "referencial - no oficial"
- [ ] Los plazos tienen evento base verificable
- [ ] Las reglas de bloqueo están clasificadas por severidad (BLOCKING/WARNING/INFO)
- [ ] El régimen recomendado corresponde a las respuestas de intención del usuario
- [ ] Los documentos requeridos corresponden al régimen seleccionado
- [ ] Mercancía restringida detectada y permiso solicitado

### Seguridad
- [ ] CSRF token validado en toda acción POST
- [ ] IDOR: endpoint verifica propiedad del expediente
- [ ] Autorización: endpoint verifica rol del usuario
- [ ] XSS: salida escapada en JSP (cHtml, fn:escapeXml)
- [ ] SQL Injection: PreparedStatement en toda consulta
- [ ] File upload: extensión + MIME + magic bytes + tamaño + hash
- [ ] Path traversal: canonical path validation (no String.replace)
- [ ] Stacktrace no visible al usuario
- [ ] Rate limiting en endpoints críticos (upload, delete, state change)
- [ ] Session fixation: login crea nueva sesión

### Fuente de datos
- [ ] Todo dato crítico tiene sourceType + confidence
- [ ] Dato OFICIAL_API tiene timestamp de última actualización
- [ ] Dato SIMULADO no puede desbloquear acciones críticas
- [ ] Dato CACHE tiene fecha de expiración visible
- [ ] Fuente desconocida genera advertencia visible

### UI guiada
- [ ] Cada pantalla muestra: paso actual (X de Y)
- [ ] Cada pantalla muestra: siguiente acción recomendada
- [ ] Cada pantalla muestra: qué está bloqueado (si aplica) y por qué
- [ ] Cada mensaje sigue formato pedagógico: "Este paso sirve para..."
- [ ] Badges visibles: SIMULADO, REFERENCIAL, OFICIAL, CACHE

### Auditoría
- [ ] Cambio de estado registrado en expediente_eventos_auditoria
- [ ] Generación PRE-DAM registrada con hash
- [ ] Subida/eliminación de documento registrada
- [ ] Cambio de régimen/modalidad registrado
- [ ] Auditoría con hash chain (prev_hash + current_hash)

### Pruebas
- [ ] Tests unitarios para cada nuevo servicio
- [ ] Tests de reglas BLOCKING (una prueba por regla)
- [ ] Tests de edge cases (null, vacío, valores extremos)
- [ ] Tests de metadata (source, sourceType, confidence)
- [ ] Tests de autorización (acceso a expediente ajeno)
- [ ] Tests de flujo guiado (no saltar pasos)
- [ ] Tests de integridad documental (checksum match)

### Base de datos
- [ ] Migración con IF NOT EXISTS
- [ ] Columnas con tipos correctos (DECIMAL para montos, TIMESTAMP para fechas)
- [ ] Índices en columnas de búsqueda frecuente
- [ ] Foreign keys donde aplica
- [ ] Charset utf8mb4

### Errores controlados
- [ ] Error 500 muestra página genérica sin stacktrace
- [ ] Error de validación muestra campos faltantes
- [ ] Error de API externa muestra fallback + aviso
- [ ] Timeout de BD muestra mensaje amigable
- [ ] Doble clic no duplica acción (idempotencia)
