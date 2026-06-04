# SKILL STUDY — AGENT 3: ARQUITECTO JAVA/JSP/SERVLETS/MYSQL

## 1. DIAGNÓSTICO DE LA SKILL

La skill `importease-java-jsp-refactor/SKILL.md` es **buena en principios pero débil en concreción**. Define los valores correctos (no reescribir, mantener stack, separar capas, validar en backend, auditar) pero **no aterriza en código real del proyecto**.

### Respuestas a las 10 preguntas:

| # | Pregunta | Diagnóstico |
|---|----------|-------------|
| 1 | ¿Evita reescrituras innecesarias? | **Sí.** Principio 1-2: "No reescribir todo", "cambios pequeños verificables". |
| 2 | ¿Mantiene el stack actual? | **Sí.** Principio 15: "No cambiar el stack sin autorización". Explícito. |
| 3 | ¿Define bien responsabilidades? | **Parcial.** Da paquetes sugeridos y nombres de servicios, pero no responsabilidades concretas ni límites entre ellos. |
| 4 | ¿Evita lógica normativa en JSP? | **Sí.** Lista explícita de qué no hacer en JSP (no SQL, no DTA, no plazos, no régimen). |
| 5 | ¿Evita validaciones solo en frontend? | **Sí.** Principio 8: "Validar en backend" + tabla de validaciones obligatorias. |
| 6 | ¿Exige autorización por expediente? | **Sí.** Principio de seguridad: "autorización por expediente". Pero no dice cómo verificarla. |
| 7 | ¿Exige auditoría? | **Sí.** Principio 11 + `AuditService` listado. |
| 8 | ¿Propone suficientes DTOs? | **No.** Da 14 nombres de DTOs pero **0 campos**. No hay `ExpedienteDTO` con atributos, no hay `GuidedStepDTO` con estructura. |
| 9 | ¿Propone suficientes servicios? | **No.** Da 17 nombres de servicios pero **0 responsabilidades**. No diferencia qué hace `ExpedienteService` vs `ExpedienteGuidedFlowService`. |
| 10 | ¿Necesita ejemplos concretos por archivo? | **Sí, crítico.** La skill menciona `expediente-aduanero.jsp` pero no dice qué líneas mover, qué servicios crear, ni qué DAOs refactorizar. |

### Diagnóstico de brecha skill vs. código real:

| Aspecto | Skill dice | Código real tiene |
|---------|-----------|-------------------|
| Paquete | `com.importease.service.*` | `negocio.*` (sin subpaquetes) |
| Servicios | `ExpedienteService` | **No existe** — todo está en `AduanasService` (1177 líneas) |
| DTOs | `ExpedienteDTO`, etc. | No hay DTOs, solo `Map<String, Object>` |
| Servlets | Reglas de 9 pasos | `AduanasController` + 19 controllers en `controlador.*` |
| DAOs | PreparedStatement | `modeloDAO.*` ya usa PreparedStatement |
| Modelo de metadata | `source`, `sourceType`, `confidence` | `ResponseEnvelope` + `FuenteMetadataBuilder` ya lo implementan |

---

## 2. VACÍOS TÉCNICOS DETECTADOS

### V1: God class `AduanasService` (1177 líneas)
Hace TODO: régimen, modalidad, manifiesto, PRE-DAM, DTA, timeline, alertas, garantías, base legal, checklist documental, health panel. Violación total de SRP.

### V2: `Map<String, Object>` como lenguaje universal
Todos los métodos devuelven `Map<String, Object>`. Sin tipado, sin contratos, sin documentación de campos esperados. Cada llamada es un "adivina qué devuelve".

### V3: Paquete `negocio` monolítico
45 clases en un solo paquete. Static methods everywhere. `PlazoCriticoService.calcularPlazos()` es static. `PredamValidationService.validate()` es static.

### V4: Sin subpaquetes
No existe `service`, `validation`, `rules`, `dao`, `dto`, `security`, `audit`. Todo plano.

### V5: JSP recibe datos vía fetch() y los renderiza con innerHTML
`expediente-aduanero.jsp` (842 líneas) es principalmente HTML+JS que llama a `/api/aduanas/*` y pinta con JS. La lógica de UI es correcta (JSP no calcula), pero la responsabilidad de *orquestar* qué mostrar está en JS del navegador, no en un servlet.

### V6: Sin DTOs de request/response
No hay objetos que modelen peticiones. Los servlets reciben `Map<String, Object>` del body JSON. No hay validación estructurada.

### V7: WizardServlet mezcla autenticación + validación de pasos + redirección
El wizard usa sesión HTTP para state, sin servicio dedicado.

### V8: Sin capa de autorización por expediente
`requireOwned()` existe pero está embebido en `AduanasService`. No hay un servicio reutilizable `AuthorizationService`.

---

## 3. NUEVA ARQUITECTURA RECOMENDADA

```text
┌─────────────────────────────────────────────────────────────────────┐
│                        NAVEGADOR (fetch + innerHTML)                │
└──────────────────────────┬──────────────────────────────────────────┘
                           │ HTTP (JSON)
┌──────────────────────────▼──────────────────────────────────────────┐
│                    Servlet / Controller                              │
│  - Autenticación (AuthFilter / SessionFilter)                        │
│  - Autorización por expediente (AuthorizationFilter)                 │
│  - CSRF check                                                        │
│  - Parseo + sanitización                                             │
│  - Invocar servicio → ResponseEnvelope                               │
│  - Auditoría en cambios de estado                                    │
└──────────────────────────┬──────────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────────┐
│                      Service Layer                                    │
│  ┌─────────────────────┐  ┌─────────────────────┐                    │
│  │ AduanasService      │  │ ExpedienteService   │  ← NUEVO          │
│  │ (orquestador)        │  │ (orquestador)        │                    │
│  └────────┬────────────┘  └────────┬────────────┘                    │
│           │                        │                                 │
│  ┌────────▼────────────────────────▼────────────┐                    │
│  │  Sub-servicios de dominio:                   │                    │
│  │  - RegimenService         - ModalidadService │                    │
│  │  - PredamService          - DtaService       │                    │
│  │  - DocumentoChecklistSvc  - TimelineService  │                    │
│  │  - PlazoCriticoService    - AlertaService    │                    │
│  │  - HealthPanelService     - NextActionService│                    │
│  │  - CoherenciaAduaneraSvc  - AuditService     │                    │
│  └──────────────────────┬───────────────────────┘                    │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────┐                    │
│  │  Validator Layer                               │                    │
│  │  - PredamValidationService (reglas mínimas)    │                    │
│  │  - RegimenValidationService (coherencia)       │                    │
│  │  - DocumentoValidationService (permisos)       │                    │
│  └──────────────────────┬───────────────────────┘                    │
│                         │                                           │
│  ┌──────────────────────▼───────────────────────┐                    │
│  │  DAO Layer                                    │                    │
│  │  - ImportacionDAO   - ManifiestoDAO          │                    │
│  │  - DamDAO           - DtaDAO                 │                    │
│  │  - EventoDAO        - DocumentoDAO           │                    │
│  │  - PlazoDAO         - AlertaDAO              │                    │
│  └──────────────────────┬───────────────────────┘                    │
│                         │                                           │
└─────────────────────────┼───────────────────────────────────────────┘
                          │ JDBC (HikariCP)
┌─────────────────────────▼───────────────────────────────────────────┐
│                      MySQL (MariaDB)                                 │
│  Tablas existentes + nuevas (expediente_guided_steps,               │
│  expediente_validaciones, expediente_next_actions, etc.)             │
└─────────────────────────────────────────────────────────────────────┘
```

### Principio de empaquetado nuevo

```text
com.importease.controller     ← servlets (mantener compatibilidad)
com.importease.service        ← orquestadores
com.importease.service.rules  ← reglas normativas
com.importease.service.validation ← validadores
com.importease.dao            ← data access
com.importease.dto            ← data transfer objects
com.importease.dto.request    ← DTOs de entrada
com.importease.dto.response   ← DTOs de salida
com.importease.model          ← entidades
com.importease.security       ← auth, csrf, filters
com.importease.audit          ← eventos de auditoría
com.importease.util           ← helpers
```

**Estrategia de migración:** No mover archivos. Crear clases nuevas en `com.importease.*` y que las nuevas deleguen en las viejas hasta que se puedan reemplazar.

---

## 4. SERVICIOS NUEVOS (con responsabilidades exactas)

### 4.1 Orquestadores

| Servicio | Responsabilidad | Reemplaza |
|----------|----------------|-----------|
| `ExpedienteService` | Orquestar carga completa del expediente (coordina 7 sub-servicios) | `AduanasService.obtenerExpediente()` |
| `AduanasService` (reducido) | Solo endpoints de consulta rápida (lista de aduanas, países) | Mantener para compatibilidad |

### 4.2 Sub-servicios de dominio (nuevos)

| # | Servicio | Responsabilidad | Método clave |
|---|----------|----------------|-------------|
| 1 | `RegimenService` | Determinar régimen aduanero según inputs del usuario + validar coherencia | `determinarRegimen(input)` |
| 2 | `ModalidadService` | Determinar modalidad de despacho según régimen + FOB + restricciones | `determinarModalidad(regimen, input)` |
| 3 | `PredamService` | Generar PRE-DAM referencial (solo después de validación) | `generarPredam(expedienteId)` |
| 4 | `DtaService` | Calcular DTA con BigDecimal, desglose por tributo, metadata de fuente | `calcularDta(importacion)` |
| 5 | `PlazoCriticoService` | Refactorizar el existente: extraer consultas DB a DAO, devolver DTOs | `calcularPlazos(expedienteId)` |
| 6 | `HealthPanelService` | Calcular % de completitud, estado general del expediente | `calcularSalud(expedienteId)` |
| 7 | `NextActionService` | Determinar qué debe hacer el usuario ahora según estado | `siguienteAccion(expedienteId)` |
| 8 | `GuidedFlowService` | Manejar paso actual del flujo guiado, validar transiciones | `avanzarPaso(expedienteId, paso)` |
| 9 | `CoherenciaAduaneraService` | Detectar contradicciones en datos del expediente | `verificarCoherencia(expedienteId)` |
| 10 | `DocumentoChecklistService` | Calcular documentos necesarios vs. presentes | `checklist(expedienteId)` |
| 11 | `AuditService` | Registrar eventos de auditoría con firma | `registrar(usuarioId, accion, detalle)` |
| 12 | `AuthorizationService` | Verificar propiedad del expediente + rol | `tieneAcceso(usuarioId, expedienteId)` |

### 4.3 Servicios de validación

| Servicio | Responsabilidad |
|----------|----------------|
| `PredamValidationService` | Refactorizar para devolver `ValidationResultDTO` en vez de excepción |
| `RegimenValidationService` | Validar coherencia de inputs de régimen |
| `DocumentoValidationService` | Validar tipo, tamaño, contenido de documentos subidos |

---

## 5. DTOS NUEVOS (con campos exactos)

### `ExpedienteDTO` (salida del orquestador)

```java
public class ExpedienteDTO {
    private int id;
    private int operacionId;
    private String estado;
    private RegimenDTO regimen;
    private ModalidadDTO modalidad;
    private GuidedStepDTO pasoActual;
    private HealthPanelDTO salud;
    private NextActionDTO siguienteAccion;
    private List<DocumentoChecklistDTO> documentos;
    private List<TimelineEventDTO> timeline;
    private List<PlazoCriticoDTO> plazos;
    private List<AlertaDTO> alertas;
    private DtaDTO dta;
    private PredamDTO predam;
    private List<CoherenciaIssueDTO> incoherencias;
    private FuenteMetadata metadata;
    private List<AuditEventDTO> auditoria;
}
```

### `GuidedStepDTO`

```java
public class GuidedStepDTO {
    private String stepCode;        // "DATOS_BASICOS", "REGIMEN", "MANIFIESTO", "DOCUMENTOS", "DTA", "PREDAM", "REVISION"
    private int stepNumber;
    private String nombre;
    private String estado;           // "PENDIENTE", "COMPLETADO", "BLOQUEADO", "EN_PROGRESO"
    private boolean habilitado;
    private String mensajeAyuda;
    private List<String> requisitosPrevios;
    private FuenteMetadata metadata;
}
```

### `RegimenDTO`

```java
public class RegimenDTO {
    private String codigo;           // "10", "36", "80", "ADM_TEMP", "TRANSBORDO"
    private String nombre;           // "Importación para el consumo"
    private String motivo;
    private String siguientePaso;
    private String destino;          // "PERU", "TRANSITO", etc.
    private List<ItemInterpretado> preguntasInterpretadas;
    private FuenteMetadata metadata;
}
```

### `ModalidadDTO`

```java
public class ModalidadDTO {
    private String codigo;           // "ANTICIPADO", "DIFERIDO", "URGENTE", etc.
    private String nombre;
    private String motivo;
    private String plazoTexto;
    private List<String> excepciones;
    private FuenteMetadata metadata;
}
```

### `HealthPanelDTO`

```java
public class HealthPanelDTO {
    private int completitudPorcentaje;    // 0-100
    private int docsCompletos;
    private int docsRequeridos;
    private int alertasCriticas;
    private int alertasTotales;
    private int validacionesPendientes;
    private boolean predamBloqueada;
    private String estadoGeneral;        // "VERDE", "AMARILLO", "ROJO"
    private String siguienteAccionTexto;
    private FuenteMetadata metadata;
}
```

### `NextActionDTO`

```java
public class NextActionDTO {
    private String actionCode;           // "REGISTRAR_MANIFIESTO", "VALIDAR_FUENTES", "GENERAR_PREDAM"
    private String actionLabel;
    private String descripcion;
    private String urgency;              // "ALTA", "MEDIA", "BAJA"
    private String stepCode;
    private boolean requiereAccion;
    private FuenteMetadata metadata;
}
```

### `PlazoCriticoDTO`

```java
public class PlazoCriticoDTO {
    private String code;                // "PLAZO_NUMERACION", "PLAZO_ABANDONO"
    private String label;
    private String baseEvent;
    private LocalDateTime baseDate;
    private LocalDateTime deadline;
    private long daysRemaining;
    private String status;              // "OK", "POR_VENCER", "VENCIDO"
    private String riskLevel;           // "BAJO", "MEDIO", "CRITICO"
    private FuenteMetadata metadata;
}
```

### `DtaDTO`

```java
public class DtaDTO {
    private BigDecimal baseImponibleCif;
    private BigDecimal adValorem;
    private BigDecimal isc;
    private BigDecimal igv;
    private BigDecimal ipm;
    private BigDecimal percepcion;
    private BigDecimal total;
    private String moneda;              // "USD"
    private BigDecimal tipoCambio;
    private LocalDateTime fechaCalculo;
    private boolean esSimulada;
    private List<DtaDesgloseDTO> desglose;
    private FuenteMetadata metadata;
}
```

### `PredamDTO`

```java
public class PredamDTO {
    private long damId;
    private String numeroDam;
    private String regimenCodigo;
    private String modalidadCodigo;
    private String canalControl;
    private boolean canalEsOficial;
    private String estadoDam;
    private boolean bloqueada;
    private List<String> faltantes;
    private FuenteMetadata metadata;
}
```

### `ValidationResultDTO`

```java
public class ValidationResultDTO {
    private boolean valido;
    private String errorCode;
    private String mensaje;
    private List<MissingFieldDTO> missingFields;
    private FuenteMetadata metadata;
}
```

### `CoherenciaIssueDTO`

```java
public class CoherenciaIssueDTO {
    private String issueCode;
    private String descripcion;
    private String severidad;
    private String campoA;
    private String campoB;
    private String accionRecomendada;
    private FuenteMetadata metadata;
}
```

### `ExpedienteResumenDTO` (para listados)

```java
public class ExpedienteResumenDTO {
    private int id;
    private int operacionId;
    private String productoDesc;
    private String hsCode;
    private String regimenCodigo;
    private String estado;
    private String pasoActualCode;
    private int completitud;
    private boolean tieneAlertasCriticas;
    private LocalDateTime ultimaActualizacion;
    private FuenteMetadata metadata;
}
```

---

## 6. TABLAS NUEVAS (con SQL CREATE)

```sql
-- ============================================================
-- 1. FLUJO GUIADO - paso actual del expediente
-- ============================================================
CREATE TABLE expediente_guided_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    step_code VARCHAR(50) NOT NULL,
    step_number INT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE, EN_PROGRESO, COMPLETADO, BLOQUEADO
    habilitado TINYINT(1) NOT NULL DEFAULT 0,
    completado_at TIMESTAMP NULL,
    bloqueado_por VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (expediente_id) REFERENCES importaciones(id) ON DELETE CASCADE,
    UNIQUE KEY uk_expediente_step (expediente_id, step_code)
);

-- ============================================================
-- 2. VALIDACIONES POR EXPEDIENTE
-- ============================================================
CREATE TABLE expediente_validaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    validation_code VARCHAR(50) NOT NULL,
    validation_group VARCHAR(50) NOT NULL,  -- DOCUMENTOS, REGIMEN, COHERENCIA, FUENTES
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',  -- PENDIENTE, OK, ERROR, ADVERTENCIA
    mensaje TEXT NULL,
    base_legal VARCHAR(255) NULL,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',  -- INFO, WARNING, ERROR
    resuelto TINYINT(1) NOT NULL DEFAULT 0,
    resuelto_por INT NULL,
    resuelto_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expediente_id) REFERENCES importaciones(id) ON DELETE CASCADE,
    INDEX idx_validacion_estado (expediente_id, estado)
);

-- ============================================================
-- 3. SIGUIENTES ACCIONES RECOMENDADAS
-- ============================================================
CREATE TABLE expediente_next_actions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    action_code VARCHAR(50) NOT NULL,
    action_label VARCHAR(200) NOT NULL,
    descripcion TEXT NULL,
    urgency VARCHAR(20) NOT NULL DEFAULT 'MEDIA',
    step_code VARCHAR(50) NULL,
    activa TINYINT(1) NOT NULL DEFAULT 1,
    completada TINYINT(1) NOT NULL DEFAULT 0,
    completada_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expediente_id) REFERENCES importaciones(id) ON DELETE CASCADE,
    INDEX idx_next_action_activa (expediente_id, activa)
);

-- ============================================================
-- 4. INCOHERENCIAS DETECTADAS
-- ============================================================
CREATE TABLE expediente_coherencia_issues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    issue_code VARCHAR(50) NOT NULL,
    descripcion TEXT NOT NULL,
    severidad VARCHAR(20) NOT NULL DEFAULT 'WARNING',
    campo_a VARCHAR(100) NULL,
    campo_b VARCHAR(100) NULL,
    accion_recomendada TEXT NULL,
    resuelto TINYINT(1) NOT NULL DEFAULT 0,
    resuelto_por INT NULL,
    resuelto_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expediente_id) REFERENCES importaciones(id) ON DELETE CASCADE,
    INDEX idx_coherencia_expediente (expediente_id)
);

-- ============================================================
-- 5. REGLAS NORMATIVAS (catálogo)
-- ============================================================
CREATE TABLE reglas_normativas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_code VARCHAR(50) NOT NULL UNIQUE,
    rule_name VARCHAR(200) NOT NULL,
    descripcion TEXT NULL,
    base_legal VARCHAR(255) NULL,
    tipo VARCHAR(50) NOT NULL,        -- REGIMEN, MODALIDAD, DOCUMENTO, PLAZO, TRIBUTO
    condiciones TEXT NULL,             -- JSON con condiciones
    activa TINYINT(1) NOT NULL DEFAULT 1,
    version VARCHAR(10) NOT NULL DEFAULT '1.0',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ============================================================
-- 6. AUDITORÍA DE EXPEDIENTE (eventos)
-- ============================================================
CREATE TABLE expediente_audit_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    expediente_id INT NOT NULL,
    usuario_id INT NOT NULL,
    accion VARCHAR(100) NOT NULL,      -- EXPEDIENTE_CREADO, REGIMEN_CAMBIADO, PREDAM_GENERADA
    detalle TEXT NULL,
    ip_address VARCHAR(45) NULL,
    user_agent TEXT NULL,
    hash_encadenado VARCHAR(128) NULL, -- blockchain-like chain
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (expediente_id) REFERENCES importaciones(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    INDEX idx_audit_expediente (expediente_id, created_at DESC)
);
```

---

## 7. CAMBIOS POR ARCHIVO PROBABLE (plan file-by-file)

### Fase 1: Estructura base (sin romper nada)

| Archivo | Cambio |
|---------|--------|
| `AduanasService.java` | Extraer `evaluarRegimen()` a `RegimenService` (nuevo), mantener delegación |
| `AduanasService.java` | Extraer `evaluarModalidad()` a `ModalidadService` (nuevo), mantener delegación |
| `AduanasService.java` | Extraer `generarPredam()` a `PredamService` (nuevo), mantener delegación |
| `AduanasService.java` | Extraer DTA a `DtaService`, mantener delegación |
| `AduanasService.java` | Extraer `panelSalud()` a `HealthPanelService` |
| `AduanasService.java` | Extraer `obtenerTimeline()` a `TimelineService` |
| `AduanasService.java` | Extraer checklist documental a `DocumentoChecklistService` |
| **Resultado:** `AduanasService` pasa de 1177 a ~200 líneas, solo orquestando |

### Fase 2: DTOs

| Archivo | Cambio |
|---------|--------|
| `com.importease.dto.ExpedienteDTO` | Nuevo: campos del expediente completo |
| `com.importease.dto.GuidedStepDTO` | Nuevo: paso del flujo guiado |
| `com.importease.dto.RegimenDTO` | Nuevo: régimen aduanero |
| `com.importease.dto.ModalidadDTO` | Nuevo: modalidad de despacho |
| `com.importease.dto.HealthPanelDTO` | Nuevo: salud del expediente |
| `com.importease.dto.NextActionDTO` | Nuevo: siguiente acción |
| `com.importease.dto.PlazoCriticoDTO` | Nuevo: plazo con metadata |
| `com.importease.dto.DtaDTO` | Nuevo: DTA con desglose |
| `com.importease.dto.PredamDTO` | Nuevo: PRE-DAM |
| `com.importease.dto.ValidationResultDTO` | Nuevo: resultado de validación |
| `com.importease.dto.request.*` | Nuevos: DTOs de entrada para cada acción POST |

### Fase 3: Servicios nuevos (en com.importease.service)

| Archivo | Responsabilidad |
|---------|----------------|
| `RegimenService.java` | `determinarRegimen(RegimenRequestDTO): RegimenDTO` |
| `ModalidadService.java` | `determinarModalidad(ModalidadRequestDTO): ModalidadDTO` |
| `PredamService.java` | `generarPredam(int expedienteId): PredamDTO` |
| `DtaService.java` | `calcularDta(int expedienteId): DtaDTO` |
| `HealthPanelService.java` | `calcularSalud(int expedienteId): HealthPanelDTO` |
| `NextActionService.java` | `calcularSiguienteAccion(int expedienteId): NextActionDTO` |
| `GuidedFlowService.java` | `obtenerPasoActual(int expedienteId): GuidedStepDTO`, `avanzarPaso()` |
| `CoherenciaAduaneraService.java` | `verificarCoherencia(int expedienteId): List<CoherenciaIssueDTO>` |
| `DocumentoChecklistService.java` | `obtenerChecklist(int expedienteId): List<DocumentoChecklistDTO>` |
| `AuthorizationService.java` | `tieneAcceso(int usuarioId, int expedienteId): boolean` |
| `AuditService.java` | `registrarEvento(int expedienteId, int usuarioId, String accion, String detalle)` |

### Fase 4: Refactor de Servlets

| Archivo | Cambio |
|---------|--------|
| `AduanasController.java` | Delegar en `ExpedienteService` en vez de `AduanasService` directamente |
| `WizardServlet.java` | Extraer lógica de pasos a `GuidedFlowService`, mantener sesión solo para estado transitorio |
| Nuevo: `ExpedienteController.java` | Servlet específico para operaciones de expediente (`/api/expediente/*`) |

### Fase 5: Refactor de JSP

| Archivo | Cambio |
|---------|--------|
| `expediente-aduanero.jsp` | No tocar estructura HTML. Si hay scriptlets con lógica de negocio, moverlos a un servlet. Mantener fetch() a `/api/expediente/*` |

---

## 8. PLAN DE REFACTOR INCREMENTAL (pasos ordenados)

### Paso 1: DTOs primero (seguro, no toca producción)
Crear todos los DTOs en `com.importease.dto.*`. Los actuales `Map<String, Object>` pueden coexistir. Los DTOs se usan como contratos de ahora en adelante.

### Paso 2: AuthorizationService (seguridad inmediata)
Crear `AuthorizationService.verificarAcceso(usuarioId, expedienteId)`. Refactorizar `AduanasService.requireOwned()` para usarlo.

### Paso 3: Extraer RegimenService (fácil, bajo riesgo)
`evaluarRegimen()` → `RegimenService`. El método original delega. Sin cambio funcional.

### Paso 4: Extraer ModalidadService
`evaluarModalidad()` → `ModalidadService`. Igual, delegar.

### Paso 5: Extraer HealthPanelService
`panelSalud()` → `HealthPanelService`.

### Paso 6: Extraer NextActionService
La lógica de "qué sigue" de `buildExpediente()` → `NextActionService`.

### Paso 7: Extraer DtaService
`ensureDta()` + `dtaMap()` → `DtaService`.

### Paso 8: Extraer PredamService
`generarPredam()` + `ensureDam()` → `PredamService`.

### Paso 9: Extraer DocumentoChecklistService
`checklist()` → `DocumentoChecklistService`.

### Paso 10: Crear ExpedienteService (orquestador)
Que coordine todos los sub-servicios. `AduanasService.buildExpediente()` se vuelve `ExpedienteService.cargarExpediente()`.

### Paso 11: AuditService
Crear `AuditService` unificado. Reemplazar llamadas directas a BD en `EventoUsuarioService` y `ExpedienteAuditService`.

### Paso 12: CoherenciaAduaneraService
Extraer detección de contradicciones (régimen contradictorio, flags inconsistentes, etc.)

### Paso 13: Refactor PlazoCriticoService
Convertir métodos static a instancia, devolver `PlazoCriticoDTO` en vez de `Map<String, Object>`.

### Paso 14: Nuevo endpoint `/api/expediente/{id}`
`ExpedienteController` que devuelve `ExpedienteDTO` completo. El JSP existente consume este endpoint.

### Paso 15: Limpiar AduanasService
Una vez que todos los métodos están extraídos, `AduanasService` solo mantiene compatibilidad hacia atrás delegando. Luego se depreca.

---

## 9. PRUEBAS JUNIT NECESARIAS (por servicio, qué testear)

### 9.1 `RegimenServiceTest`
- `determinarRegimen()` con destino=PERU → código "10"
- `determinarRegimen()` con reimportacion=true → código "36"
- `determinarRegimen()` con transito=true → código "80"
- `determinarRegimen()` con flags contradictorios → lanza excepción o error estructurado
- Coherencia: quedaEnPeru=true + reimporta=true → issue detectado
- Metadata correcta (source, sourceType, confidence)

### 9.2 `ModalidadServiceTest`
- regimen="10", FOB=5000, no restringida → ANTICIPADO
- regimen="10", FOB=500, no restringida → DIFERIDO (excepción por FOB bajo)
- regimen="10", urgente=true, sin justificacion → URGENTE
- regimen="36" → DIFERIDO_ROJO
- regimen="80" → TRANSITO_NACIONAL

### 9.3 `PredamServiceTest`
- generarPredam con expediente completo → PRE-DAM generada + metadata SIMULATED
- generarPredam con FOB inválido → lanza PredamValidationException
- generarPredam sin HS Code → error estructurado
- Audit event registrado

### 9.4 `DtaServiceTest`
- calcularDta con valores conocidos → BigDecimal correcto
- Valores negativos → error
- Metadata: confidence=0.60 cuando es referencial
- Moneda y tipo de cambio correctos

### 9.5 `HealthPanelServiceTest`
- 100% completitud con todos los datos → score=100
- Sin manifiesto → score parcial, alerta específica
- Sin PRE-DAM → predam_bloqueada=true
- 0 documentos → score mínimo

### 9.6 `NextActionServiceTest`
- Sin manifiesto → action=REGISTRAR_MANIFIESTO
- Sin documentos → action=SUBIR_DOCUMENTOS
- Todo completo → action=REVISAR_PREDAM
- PRE-DAM generada → action=FIRMAR_DAM
- Urgencia correcta según plazos

### 9.7 `GuidedFlowServiceTest`
- Nuevo expediente → paso=1 (DATOS_BASICOS)
- Con datos básicos → paso=2 (REGIMEN)
- Con régimen → paso=3 (MANIFIESTO)
- Salto de paso inválido → error
- Completar paso → transición correcta

### 9.8 `CoherenciaAduaneraServiceTest`
- régimen=TRANSITO con manifiesto faltante → issue
- paísOrigen="CHINA" con TLC vigente → issue si falta certificado
- FOB > 0 con HS Code nulo → issue

### 9.9 `AuthorizationServiceTest`
- Usuario dueño del expediente → true
- Usuario no dueño → false
- Usuario admin con rol → true
- Expediente inexistente → false

### 9.10 `PredamValidationServiceTest`
- Testear cada regla de validación por separado (FOB mínimo, HS Code, etc.)
- Testear en lugar de mockear la BD: usar H2 o mock de DAO
- Testear que devuelve `ValidationResultDTO` no excepción

### 9.11 `PlazoCriticoServiceTest`
- Calcular plazos sin fechas → plazos estimados desde fechaCreacion
- Calcular plazos con fechas reales → plazos correctos
- Plazo vencido → status=VENCIDO, riskLevel=CRITICO
- Plazo próximo a vencer (< 5 días) → status=POR_VENCER

---

## 10. NUEVA VERSIÓN MEJORADA RESUMIDA DE LA SKILL

A continuación, los puntos que la skill actual **no cubre y debería**:

### Lo que falta en la skill (adiciones necesarias):

```text
## 1. REGLA DE ORO: NUNCA DEVOLVER Map<String, Object>
Prohibido crear nuevos métodos públicos que devuelvan Map<String, Object>.
Todo nuevo servicio debe devolver un DTO tipado o List<DTO>.
Los métodos existentes pueden mantener Map<String, Object> por compatibilidad
pero deben tener un delegado con DTO.

## 2. ESTRUCTURA DE PAQUETES OBLIGATORIA
Al crear una clase nueva, usar:
  com.importease.service.*      (servicios de dominio)
  com.importease.dto.*           (DTOs de salida)
  com.importease.dto.request.*   (DTOs de entrada)
  com.importease.dao.*           (DAOs con PreparedStatement)
  com.importease.security.*      (filtros, autorización)
  com.importease.audit.*         (auditoría)

NO crear nuevas clases en negocio.* (solo refactorizar las existentes).

## 3. SERVICIOS: RESPONSABILIDADES EXACTAS
  RegimenService.determinarRegimen(input): RegimenDTO
  ModalidadService.determinarModalidad(input): ModalidadDTO
  PredamService.generarPredam(expedienteId): PredamDTO
  DtaService.calcularDta(expedienteId): DtaDTO
  HealthPanelService.calcularSalud(expedienteId): HealthPanelDTO
  NextActionService.calcularSiguienteAccion(expedienteId): NextActionDTO
  GuidedFlowService.obtenerPasoActual(expedienteId): GuidedStepDTO
  CoherenciaAduaneraService.verificarCoherencia(expedienteId): List<CoherenciaIssueDTO>

  Prohibido: Un servicio que haga más de una de estas responsabilidades.

## 4. DTOS: CAMPOS MÍNIMOS REQUERIDOS
  Todo DTO crítico debe tener:
    - FuenteMetadata metadata (source, sourceType, confidence, generatedAt, isOfficial, isSimulated)
    - constructor, getters, setters (sin lógica de negocio)
    - toString() para debugging

  Ver sección "DTOS NUEVOS (con campos exactos)" más arriba para la especificación completa.

## 5. REFACTOR DE AduanasService (1177 LÍNEAS)
  Este es el archivo crítico. No tocar su interfaz pública. Extraer así:
    1. evaluarRegimen() → RegimenService (mantener delegación)
    2. evaluarModalidad() → ModalidadService
    3. generarPredam() → PredamService
    4. ensureDta() → DtaService
    5. panelSalud() → HealthPanelService
    6. checklist() → DocumentoChecklistService
    7. buildExpediente() → ExpedienteService.cargar()
  Al final, AduanasService solo contiene obtenerExpediente(), obtenerTimeline(),
  obtenerAlertas(), obtenerBaseLegal() y registrarManifiesto(), todos delegando.

## 6. ORDEN DE REFACTOR (IMPORTANTE)
  Hacer en este orden, validando con pruebas después de cada paso:
    1. DTOs (sin impacto funcional)
    2. AuthorizationService (se entrega inmediatamente)
    3. RegimenService + tests
    4. ModalidadService + tests
    5. HealthPanelService + tests
    6. NextActionService + tests
    7. DtaService + tests
    8. PredamService + tests
    9. DocumentoChecklistService + tests
    10. CoherenciaAduaneraService + tests
    11. GuidedFlowService + tests
    12. ExpedienteService (orquestador) + tests
    13. AuditService + tests
    14. Nuevo ExpedienteController
    15. Limpiar AduanasService (deprecar)

## 7. PRUEBAS: PORCENTAJE MÍNIMO
  Cada nuevo servicio debe tener:
    - 1 test de caso feliz
    - 1 test por cada regla de validación
    - 1 test de edge case (null, vacío, valores extremos)
    - 1 test de metadata (source, sourceType, confidence correctos)
  Usar Mockito para mockear DAOs. H2 para pruebas de integración si aplica.

## 8. EJEMPLO CONCRETO DE CAMBIO POR ARCHIVO
  Al extraer un método de AduanasService:
  ```
  Archivo: AduanasService.java
  Problema: evaluarRegimen() tiene 60 líneas con lógica de régimen aduanero
  Cambio:   Nueva clase RegimenService con método determinarRegimen()
            AduanasService.evaluarRegimen() ahora hace return new RegimenService().determinarRegimen(body)
  Pruebas:  RegimenServiceTest (5 casos)
  Riesgo:   Mínimo. La firma pública no cambia.
  ```
```

### Resumen de evaluación final de la skill original:

| Aspecto | Nota (1-10) | Comentario |
|---------|-------------|------------|
| Principios y valores | 9 | Excelente dirección |
| Evitar reescritura | 10 | Explícito y claro |
| Separación de capas | 7 | Buena teoría, falta concreción |
| DTOs | 3 | Solo nombres, sin campos |
| Servicios | 4 | Solo nombres, sin responsabilidades |
| Refactor JSP | 6 | Buena lista de qué evitar, pero no dice qué hacer |
| Refactor Servlets | 7 | Buen checklist de 9 pasos |
| Refactor DAOs | 7 | Buen ejemplo de PreparedStatement |
| Tablas sugeridas | 5 | Solo nombres, sin columnas ni tipos |
| Pruebas | 6 | Nombres de tests pero no qué probar |
| Ejemplos concretos | 2 | Críticamente ausentes |
| **Promedio** | **5.8** | **Buena base, necesita concreción urgente** |

La skill es un excelente manifiesto de principios pero no es lo suficientemente específica para guiar a un agente de código. Las secciones 4-9 de este documento (servicios con responsabilidades, DTOs con campos, SQL con columnas, plan paso a paso, pruebas con escenarios) son las adiciones necesarias para que la skill sea operativa.
