# Estado de Migracion DAO a Spring Data JPA

Fecha de corte: 31-05-2026

## Resumen Ejecutivo

- Migradas en modo hibrido (JPA primero + fallback JDBC): `ImportacionDAO`, `UsuarioDAO`, `OperacionDAO`, `PermisoDAO`, `HsCodeDAO`.
- Legacy JDBC pendientes de migrar: `BusquedaDAO`, `IncotermDAO`, `ObservatorioDAO`, `VuceRestriccionDAO`.
- CI validado con `mvn verify` y JaCoCo en verde.

## Estado Por DAO

| DAO | Estado | Notas |
|---|---|---|
| `ImportacionDAO` | Hibrido | Segunda ola completada previamente en operaciones criticas de expediente/importacion. |
| `UsuarioDAO` | Hibrido | Login/perfil con JPA y fallback JDBC. |
| `OperacionDAO` | Hibrido | CRUD principal de operaciones + estadisticas en JPA con fallback. |
| `PermisoDAO` | Hibrido | Flujo regulatorio/permisos migrado a JPA en catalogos y datos de solicitud con fallback. |
| `HsCodeDAO` | Hibrido | Migrado en esta ola; sinonimos, busquedas y mantenimiento con JPA + fallback JDBC. |
| `TrackingDAO` | Hibrido | Migrado en esta ola; envios/eventos/sync con JPA + fallback JDBC y control por usuario. |
| `BusquedaDAO` | Legacy JDBC | Pendiente. |
| `IncotermDAO` | Legacy JDBC | Pendiente. |
| `ObservatorioDAO` | Legacy JDBC | Pendiente. |
| `VuceRestriccionDAO` | Legacy JDBC | Pendiente. |

## Detalle Metodo a Metodo: HsCodeDAO (Nueva Ola)

| Metodo | Estado |
|---|---|
| `buscarPorDescripcion(String)` | JPA + fallback JDBC |
| `obtenerPorCodigo(String)` | JPA + fallback JDBC |
| `listarTodos()` | JPA + fallback JDBC |
| `buscarSugerencias(String)` | JPA + fallback JDBC |
| `insertar(HsCode)` | JPA + fallback JDBC |
| `actualizar(HsCode)` | JPA + fallback JDBC |
| `buscarCodigoPorSinonimo(String)` | JPA + fallback JDBC |

## Metodos Migrados en Olas Previas (Referencia Rapida)

### PermisoDAO

- `listarEntidades`, `obtenerEntidad`
- `buscarReglasPorHsCode`, `buscarReglasPorPalabraClave`
- `obtenerPreguntasPorEntidad`, `obtenerDocumentosPorPermiso`
- `crearSolicitud`, `actualizarEstado`, `registrarSuce`, `obtenerSolicitud`
- `listarSolicitudesPorUsuario`, `listarSolicitudesPorOperacion`
- `guardarDatosSolicitud`, `eliminarDatosSolicitud`, `guardarRespuestasPermiso`

### UsuarioDAO

- `crearUsuario`, `buscarPorRuc`, `buscarPorId`, `buscarPorEmail`
- `listarUsuarios`, `actualizarPassword`

### OperacionDAO

- `guardar`, `obtenerPorId`, `listarPorUsuario`, `actualizarDam`, `obtenerEstadisticas`

### ImportacionDAO

- `validarDocumentosParaDespacho`, `insertar`, `actualizarEstado`
- `marcarDocumentoSubido`, `actualizarDam`
- `listarPorUsuario`, `buscarPorId`, `obtenerEstadisticas`
- `registrarHistorialEstado`

## Proxima Prioridad Recomendada

1. `BusquedaDAO` (impacta analitica y paneles).
2. `VuceRestriccionDAO` (modulo regulatorio auxiliar).
3. `ObservatorioDAO` (cache/sincronizacion de fuentes de mercado).
