package com.importease.proyecto.service;

import com.importease.proyecto.dto.PasoGuiadoDTO;
import com.importease.proyecto.dto.SeguimientoDTO;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.repository.ImportacionRepositorio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * SeguimientoServicio
 * ─────────────────────────────────────────────────────────────────
 * Centraliza toda la lógica del módulo de seguimiento.
 * Conecta FlujoGuiadoServicio (8 pasos persistidos en BD) con la vista.
 * El JSP solo llama construir() y renderiza el DTO resultante.
 *
 * Responsabilidades:
 *  - Buscar importación activa del usuario (con validación de ownership)
 *  - Delegar el cálculo de pasos y porcentaje a FlujoGuiadoServicio
 *  - Mapear los 8 pasos internos a los 4 pasos visuales del stepper
 *  - Construir textos, alertas y URLs dinámicas según producto real
 *  - Verificar documentos subidos desde BD
 *  - Inferir entidad reguladora por HS Code
 */
public class SeguimientoServicio {

    private final ImportacionRepositorio repo;
    private final FlujoGuiadoServicio flujoServicio;

    public SeguimientoServicio() {
        this.repo = new ImportacionRepositorio();
        this.flujoServicio = new FlujoGuiadoServicio();
    }

    /**
     * Construye el DTO completo para seguimiento.jsp.
     *
     * @param usuarioId       ID del usuario autenticado (de session)
     * @param operacionIdParam parámetro URL opcional (?operacionId=X)
     * @return SeguimientoDTO listo para renderizar, nunca null
     */
    public SeguimientoDTO construir(int usuarioId, String operacionIdParam) {
        SeguimientoDTO dto = new SeguimientoDTO();

        // 1. Buscar importaciones del usuario
        List<Importacion> todas = new ArrayList<>();
        Importacion principal = null;

        try (Connection con = ConexionDB.obtenerConexion()) {
            todas = repo.listarPorUsuario(con, usuarioId);

            if (!todas.isEmpty()) {
                // Intentar cargar la operación pedida por URL
                if (operacionIdParam != null && !operacionIdParam.trim().isEmpty()) {
                    try {
                        int opId = Integer.parseInt(operacionIdParam.trim());
                        for (Importacion imp : todas) {
                            // Validación de ownership: solo las del propio usuario
                            if (imp.getId() == opId && imp.getUsuarioId() == usuarioId) {
                                principal = imp;
                                break;
                            }
                        }
                        // Si el ID no pertenece al usuario → ignorar silenciosamente
                    } catch (NumberFormatException e) {
                        // Parámetro inválido → ignorar
                    }
                }
                // Si no encontró por ID, usar la primera (más reciente)
                if (principal == null) {
                    principal = todas.get(0);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("SeguimientoServicio: error al cargar importaciones para usuario " + usuarioId, e);
        }

        // 2. Sin importación → buscar si existe un borrador activo en la base de datos
        if (principal == null) {
            com.importease.proyecto.model.Borrador borrador = new com.importease.proyecto.repository.BorradorRepositorio().obtenerBorrador(usuarioId);
            if (borrador != null) {
                return construirDesdeBorrador(borrador);
            }

            dto.setSinImportacion(true);
            dto.setAlertas(new ArrayList<>());
            dto.setPorcentajeAvance(0);
            dto.setStageVisual("VACIO");
            dto.setHeroBannerTitulo("Empieza tu primera importación");
            dto.setEstadoLabel("Sin importaciones");
            dto.setEstadoBadgeColor("gray");
            dto.setSiguientePasoTitulo("Crear mi primera importación");
            dto.setSiguientePasoDesc("Usa nuestro asistente inteligente para registrar el producto que deseas importar y te guiamos paso a paso.");
            dto.setSiguientePasoUrl("evaluacion.jsp");
            dto.setSiguientePasoIcono("plus");
            dto.setProducto("Sin importaciones");
            dto.setDisplayProducto("Sin importaciones");
            dto.setCodigoArancelario("Pendiente");
            dto.setEntidadRevisora("Sin alerta directa");
            dto.setEntidadNombreCompleto("");
            dto.setNumeroOperacion(0);
            dto.setCostoBase("$0.00");
            dto.setUltimaActualizacion("No disponible");
            dto.setPasoActualNumero(0);
            dto.setPasoActualNombre("Sin importación");
            dto.setPasoEstado("VACIO");
            return dto;
        }

        // 3. Hay importación → obtener paso actual desde FlujoGuiadoServicio (BD real)
        PasoGuiadoDTO pasoDTO = null;
        try {
            pasoDTO = flujoServicio.obtenerPasoActual(principal.getId());
        } catch (Exception e) {
            LoggerUtil.error("SeguimientoServicio: error al obtener paso actual para operacion " + principal.getId(), e);
        }

        // 4. Datos del producto
        String rawProducto = principal.getProductoDesc() != null ? principal.getProductoDesc() : "Producto sin descripción";
        String displayProducto = rawProducto;
        if (displayProducto.contains("(")) {
            displayProducto = displayProducto.substring(0, displayProducto.indexOf("(")).trim();
        }
        String hs = principal.getHsCode() != null ? principal.getHsCode().trim() : "";

        dto.setProducto(rawProducto);
        dto.setDisplayProducto(displayProducto);
        dto.setCodigoArancelario(hs.isEmpty() ? "Pendiente" : hs);
        dto.setNumeroOperacion(principal.getId());
        dto.setCostoBase(String.format("$%.2f", principal.getValorCif()));

        // Fecha de última actualización
        if (principal.getFechaCreacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("es", "PE"));
            dto.setUltimaActualizacion(sdf.format(new Date(principal.getFechaCreacion().getTime())));
        } else {
            dto.setUltimaActualizacion("No disponible todavía");
        }

        // 5. Entidad reguladora por HS Code
        inferirEntidad(dto, hs);

        // 6. Documentos subidos (desde BD)
        cargarEstadoDocumentos(dto, principal.getId());

        // 7. Mapear paso al DTO y construir textos dinámicos
        int pasoNum = (pasoDTO != null) ? pasoDTO.getStep() : 1;
        String pasoEstado = (pasoDTO != null) ? pasoDTO.getEstado() : "PENDIENTE";
        int porcentaje = (pasoDTO != null && pasoDTO.getPorcentajeGlobal() != null)
                ? pasoDTO.getPorcentajeGlobal().intValue() : 0;
        String motivoBloqueo = (pasoDTO != null) ? pasoDTO.getMensajeBloqueo() : null;

        dto.setPasoActualNumero(pasoNum);
        dto.setPasoActualNombre(flujoServicio.obtenerNombrePaso(pasoNum));
        dto.setPasoEstado(pasoEstado);
        dto.setMotivoBloqueao(motivoBloqueo);
        dto.setPorcentajeAvance(porcentaje);
        dto.setBloqueado("BLOQUEADO".equals(pasoEstado));
        dto.setObservado("OBSERVADO".equals(pasoEstado));

        // Estado COMPLETO si llegó al paso 8 y está completo
        String rawState = (principal.getEstado() != null ? principal.getEstado() : "BORRADOR").toUpperCase();
        boolean esNacionalizada = "NACIONALIZADA".equals(rawState) || "LISTA_DESPACHO".equals(rawState);
        dto.setCompleto(esNacionalizada || (pasoNum == 8 && "COMPLETO".equals(pasoEstado)));

        // 8. Mapear paso interno (1-8) a stage visual (4 pasos del stepper UI)
        mapearStageVisual(dto, pasoNum, pasoEstado, esNacionalizada, displayProducto, principal.getId());

        // 9. Construir alertas contextuales
        construirAlertas(dto, displayProducto, hs, rawState);

        dto.setSinImportacion(false);
        return dto;
    }

    // ─────────────────────────────────────────────────────────────────
    // Mapeo de paso interno → stage visual + textos dinámicos
    // ─────────────────────────────────────────────────────────────────

    private void mapearStageVisual(SeguimientoDTO dto, int paso, String pasoEstado,
                                   boolean esNacionalizada, String prodNombre, int opId) {
        // Caso especial: bloqueado/observado
        if ("BLOQUEADO".equals(pasoEstado)) {
            dto.setStageVisual(stageParaPaso(paso));
            dto.setHeroBannerTitulo("Tu importación necesita atención");
            dto.setEstadoLabel("Bloqueada");
            dto.setEstadoBadgeColor("red");
            dto.setSiguientePasoTitulo("Corregir observación");
            dto.setSiguientePasoDesc("Hay un bloqueo que impide continuar con la importación de " + prodNombre + ". Revisa el detalle para resolverlo.");
            dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
            dto.setSiguientePasoIcono("alert");
            return;
        }

        if ("OBSERVADO".equals(pasoEstado)) {
            dto.setStageVisual(stageParaPaso(paso));
            dto.setHeroBannerTitulo("Hay una observación pendiente");
            dto.setEstadoLabel("Con observación");
            dto.setEstadoBadgeColor("orange");
            dto.setSiguientePasoTitulo("Corregir observación");
            String motivo = dto.getMotivoBloqueao();
            dto.setSiguientePasoDesc(motivo != null && !motivo.isBlank() ? motivo :
                    "Revisa el expediente de " + prodNombre + " y corrige la observación para continuar.");
            dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
            dto.setSiguientePasoIcono("alert");
            return;
        }

        if (esNacionalizada) {
            dto.setStageVisual("COMPLETO");
            dto.setHeroBannerTitulo("¡Importación completada!");
            dto.setEstadoLabel("Importación completada");
            dto.setEstadoBadgeColor("green");
            dto.setSiguientePasoTitulo("Ver expediente final");
            dto.setSiguientePasoDesc("Tu importación de " + prodNombre + " fue nacionalizada exitosamente. Puedes descargar todos los documentos.");
            dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
            dto.setSiguientePasoIcono("check");
            return;
        }

        switch (paso) {
            case 1:
                dto.setStageVisual("PREPARACION");
                dto.setHeroBannerTitulo("Empieza registrando tu importación");
                dto.setEstadoLabel("En preparación");
                dto.setEstadoBadgeColor("blue");
                dto.setSiguientePasoTitulo("Definir régimen aduanero");
                dto.setSiguientePasoDesc("El primer paso es definir qué quieres hacer con " + prodNombre + ": importar para vender, para uso propio, o en consignación.");
                dto.setSiguientePasoUrl("evaluacion.jsp");
                dto.setSiguientePasoIcono("document");
                break;
            case 2:
                dto.setStageVisual("PREPARACION");
                dto.setHeroBannerTitulo("Completa los datos del producto");
                dto.setEstadoLabel("Datos básicos pendientes");
                dto.setEstadoBadgeColor("blue");
                dto.setSiguientePasoTitulo("Registrar FOB, país y descripción");
                dto.setSiguientePasoDesc("Para importar " + prodNombre + " necesitamos el valor FOB, el país de origen y el incoterm del contrato con tu proveedor.");
                dto.setSiguientePasoUrl("evaluacion.jsp");
                dto.setSiguientePasoIcono("document");
                break;
            case 3:
                dto.setStageVisual("PREPARACION");
                dto.setHeroBannerTitulo("Clasifica tu mercancía");
                dto.setEstadoLabel("Código arancelario pendiente");
                dto.setEstadoBadgeColor("blue");
                dto.setSiguientePasoTitulo("Buscar código arancelario");
                dto.setSiguientePasoDesc("Necesitas identificar el código HS de " + prodNombre + " para saber los aranceles e impuestos que aplican y si necesitas algún permiso especial.");
                dto.setSiguientePasoUrl("buscador.jsp");
                dto.setSiguientePasoIcono("search");
                break;
            case 4:
                dto.setStageVisual("PERMISOS");
                dto.setHeroBannerTitulo("Registra el transporte");
                dto.setEstadoLabel("Transporte pendiente");
                dto.setEstadoBadgeColor("orange");
                dto.setSiguientePasoTitulo("Registrar BL y fechas de embarque");
                dto.setSiguientePasoDesc("Vincula el Bill of Lading (BL) o guía aérea de " + prodNombre + " con las fechas de embarque y llegada estimada al puerto.");
                dto.setSiguientePasoUrl("documentos.jsp");
                dto.setSiguientePasoIcono("ship");
                break;
            case 5:
                dto.setStageVisual("EXPEDIENTE");
                dto.setHeroBannerTitulo("Sube tus documentos");
                dto.setEstadoLabel("Documentos pendientes");
                dto.setEstadoBadgeColor("orange");
                dto.setSiguientePasoTitulo("Subir documentos requeridos");
                dto.setSiguientePasoDesc("Para importar " + prodNombre + " necesitas subir la Factura Comercial, Bill of Lading y Certificado de Origen. Sin estos Aduanas no puede procesar tu carga.");
                dto.setSiguientePasoUrl("documentos.jsp");
                dto.setSiguientePasoIcono("upload");
                break;
            case 6:
                dto.setStageVisual("EXPEDIENTE");
                dto.setHeroBannerTitulo("Valida la coherencia del expediente");
                dto.setEstadoLabel("En validación");
                dto.setEstadoBadgeColor("orange");
                dto.setSiguientePasoTitulo("Validar coherencia aduanera");
                dto.setSiguientePasoDesc("El sistema revisará que todos los datos de " + prodNombre + " sean consistentes entre sí: valores, códigos, documentos y permisos.");
                dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
                dto.setSiguientePasoIcono("check");
                break;
            case 7:
                dto.setStageVisual("LISTA");
                dto.setHeroBannerTitulo("Calcula los impuestos");
                dto.setEstadoLabel("Cálculo tributario pendiente");
                dto.setEstadoBadgeColor("green");
                dto.setSiguientePasoTitulo("Calcular DTA y PRE-DAM");
                dto.setSiguientePasoDesc("Genera el cálculo de la Deuda Tributaria Aduanera (DTA) y el PRE-DAM referencial para " + prodNombre + ". Esto es necesario para la numeración de la DAM.");
                dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
                dto.setSiguientePasoIcono("calculator");
                break;
            case 8:
            default:
                dto.setStageVisual("LISTA");
                dto.setHeroBannerTitulo("Tu importación está casi lista");
                dto.setEstadoLabel("Lista para revisión final");
                dto.setEstadoBadgeColor("green");
                dto.setSiguientePasoTitulo("Revisar y confirmar");
                dto.setSiguientePasoDesc("Verifica que todos los datos de " + prodNombre + " estén correctos. Una vez confirmados, Aduanas procesará tu carga y asignará canal de despacho.");
                dto.setSiguientePasoUrl("expediente-aduanero.jsp?operacionId=" + opId);
                dto.setSiguientePasoIcono("check");
                break;
        }
    }

    private String stageParaPaso(int paso) {
        if (paso <= 3) return "PREPARACION";
        if (paso == 4) return "PERMISOS";
        if (paso <= 6) return "EXPEDIENTE";
        return "LISTA";
    }

    // ─────────────────────────────────────────────────────────────────
    // Entidad reguladora según HS Code
    // ─────────────────────────────────────────────────────────────────

    private void inferirEntidad(SeguimientoDTO dto, String hs) {
        if (hs == null || hs.isEmpty()) {
            dto.setEntidadRevisora("Sin alerta directa");
            dto.setEntidadNombreCompleto("");
            return;
        }
        // Alimentos / Productos del mar / Lácteos / Aceites / Carnes
        if (hs.startsWith("0301") || hs.startsWith("0302") || hs.startsWith("0303") ||
            hs.startsWith("0304") || hs.startsWith("0305") || hs.startsWith("1604") ||
            hs.startsWith("2106") || hs.startsWith("1901") || hs.startsWith("2009") ||
            hs.startsWith("0401") || hs.startsWith("0402") || hs.startsWith("1507") ||
            hs.startsWith("1516") || hs.startsWith("0201") || hs.startsWith("0202") ||
            hs.startsWith("0203") || hs.startsWith("0204") || hs.startsWith("0207")) {
            dto.setEntidadRevisora("DIGESA");
            dto.setEntidadNombreCompleto("Dirección General de Salud Ambiental e Inocuidad Alimentaria (DIGESA)");
        }
        // Medicamentos / Cosméticos / Dispositivos médicos
        else if (hs.startsWith("3004") || hs.startsWith("3003") || hs.startsWith("3303") ||
                 hs.startsWith("3304") || hs.startsWith("3305") || hs.startsWith("9018") ||
                 hs.startsWith("9019") || hs.startsWith("9021")) {
            dto.setEntidadRevisora("DIGEMID");
            dto.setEntidadNombreCompleto("Dirección General de Medicamentos, Insumos y Drogas (DIGEMID)");
        }
        // Plantas / Semillas / Animales vivos / Alimento animal
        else if (hs.startsWith("0601") || hs.startsWith("0602") || hs.startsWith("0603") ||
                 hs.startsWith("1209") || hs.startsWith("2309") || hs.startsWith("0101") ||
                 hs.startsWith("0102") || hs.startsWith("0103") || hs.startsWith("0104")) {
            dto.setEntidadRevisora("SENASA");
            dto.setEntidadNombreCompleto("Servicio Nacional de Sanidad Agraria (SENASA)");
        }
        // Maderas y derivados
        else if (hs.startsWith("4407") || hs.startsWith("4408") || hs.startsWith("4409") ||
                 hs.startsWith("4410") || hs.startsWith("4411") || hs.startsWith("4412")) {
            dto.setEntidadRevisora("SERFOR");
            dto.setEntidadNombreCompleto("Servicio Nacional Forestal y de Fauna Silvestre (SERFOR)");
        }
        // Telecomunicaciones / Electrónica de comunicación
        else if (hs.startsWith("8517") || hs.startsWith("8471") || hs.startsWith("8528") ||
                 hs.startsWith("8525") || hs.startsWith("8526")) {
            dto.setEntidadRevisora("MTC");
            dto.setEntidadNombreCompleto("Ministerio de Transportes y Comunicaciones (MTC)");
        }
        // Armas / Explosivos
        else if (hs.startsWith("9301") || hs.startsWith("9302") || hs.startsWith("9303") ||
                 hs.startsWith("3601") || hs.startsWith("3602")) {
            dto.setEntidadRevisora("SUCAMEC");
            dto.setEntidadNombreCompleto("Superintendencia Nacional de Control de Servicios de Seguridad, Armas, Municiones y Explosivos (SUCAMEC)");
        }
        // Productos químicos / Plaguicidas
        else if (hs.startsWith("3808") || hs.startsWith("2901") || hs.startsWith("2902")) {
            dto.setEntidadRevisora("SENASA/DIGESA");
            dto.setEntidadNombreCompleto("SENASA / Dirección General de Salud Ambiental (según tipo de producto)");
        }
        else {
            dto.setEntidadRevisora("Sin alerta directa");
            dto.setEntidadNombreCompleto("");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Documentos subidos
    // ─────────────────────────────────────────────────────────────────

    private void cargarEstadoDocumentos(SeguimientoDTO dto, int operacionId) {
        try (Connection con = ConexionDB.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(
                "SELECT documento_factura, documento_bl, documento_certificado_origen " +
                "FROM operaciones WHERE id = ?")) {
            ps.setInt(1, operacionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    dto.setHasFactura(rs.getBoolean("documento_factura"));
                    dto.setHasBl(rs.getBoolean("documento_bl"));
                    dto.setHasCert(rs.getBoolean("documento_certificado_origen"));
                }
            }
        } catch (Exception e) {
            // Si falla la consulta, asumir documentos no cargados (conservador)
            dto.setHasFactura(false);
            dto.setHasBl(false);
            dto.setHasCert(false);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Alertas contextuales con lenguaje simple para principiantes
    // ─────────────────────────────────────────────────────────────────

    private void construirAlertas(SeguimientoDTO dto, String prodNombre, String hs, String rawState) {
        List<String> alertas = new ArrayList<>();

        boolean esTerminada = "NACIONALIZADA".equals(rawState) || "LISTA_DESPACHO".equals(rawState);
        if (esTerminada) {
            dto.setAlertas(alertas);
            return;
        }

        // Alerta: sin código arancelario
        if (hs.isEmpty()) {
            alertas.add("Sin código arancelario: Para importar " + prodNombre +
                " primero debes identificar su posición arancelaria usando nuestro buscador. " +
                "Sin este código, Aduanas no puede procesar tu importación.");
        } else {
            // Alerta: entidad reguladora que requiere permiso
            String entidad = dto.getEntidadRevisora();
            if (!"Sin alerta directa".equals(entidad)) {
                String nombreCompleto = dto.getEntidadNombreCompleto();
                alertas.add("Permiso obligatorio: " + prodNombre + " (HS " + hs + ") requiere " +
                    "autorización previa de " + (nombreCompleto.isEmpty() ? entidad : nombreCompleto) +
                    " antes del despacho aduanero. Sin este permiso tu carga puede ser retenida en el puerto.");
            }
        }

        // Alerta: documentos faltantes
        List<String> docsFaltantes = new ArrayList<>();
        if (!dto.isHasFactura()) docsFaltantes.add("Factura Comercial (emitida y firmada por tu proveedor)");
        if (!dto.isHasBl())      docsFaltantes.add("Bill of Lading o Guía Aérea (emitida por la naviera o aerolínea)");
        if (!dto.isHasCert())    docsFaltantes.add("Certificado de Origen (del país exportador, legalizado si aplica)");

        if (!docsFaltantes.isEmpty()) {
            alertas.add("Documentos pendientes para " + prodNombre + ": " +
                String.join("; ", docsFaltantes) +
                ". Debes subirlos antes de que Aduanas pueda revisar tu carga.");
        }

        dto.setAlertas(alertas);
    }

    private SeguimientoDTO construirDesdeBorrador(com.importease.proyecto.model.Borrador borrador) {
        SeguimientoDTO dto = new SeguimientoDTO();
        dto.setSinImportacion(false);

        com.google.gson.Gson gson = new com.google.gson.Gson();
        java.util.Map<String, Object> map = null;
        try {
            map = gson.fromJson(borrador.getJsonBorrador(), new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType());
        } catch (Exception e) {
            LoggerUtil.error("Error parsing json_borrador in SeguimientoServicio", e);
        }

        if (map == null) {
            map = new java.util.HashMap<>();
        }

        String prodNombre = (String) map.get("prodNombre");
        if (prodNombre == null || prodNombre.isBlank()) {
            prodNombre = (String) map.get("opNombre");
        }
        if (prodNombre == null || prodNombre.isBlank()) {
            prodNombre = "Producto en cotización";
        }

        String displayProducto = prodNombre;
        if (displayProducto.contains("(")) {
            displayProducto = displayProducto.substring(0, displayProducto.indexOf("(")).trim();
        }

        dto.setProducto(prodNombre);
        dto.setDisplayProducto(displayProducto);

        String hsCode = "Pendiente";
        java.util.Map<String, Object> selectedHS = (java.util.Map<String, Object>) map.get("selectedHS");
        if (selectedHS != null && selectedHS.get("codigo") != null) {
            hsCode = (String) selectedHS.get("codigo");
        }
        dto.setCodigoArancelario(hsCode);

        int wizardStep = borrador.getPasoActual();
        int pasoNum = 1;
        int porcentaje = 12;

        if (wizardStep == 2) {
            pasoNum = 2;
            porcentaje = 25;
        } else if (wizardStep == 3) {
            pasoNum = 3;
            porcentaje = 37;
        } else if (wizardStep == 4) {
            pasoNum = 4;
            porcentaje = 50;
        }

        dto.setPasoActualNumero(pasoNum);
        dto.setPasoActualNombre(flujoServicio.obtenerNombrePaso(pasoNum));
        dto.setPasoEstado("PENDIENTE");
        dto.setPorcentajeAvance(porcentaje);
        dto.setBloqueado(false);
        dto.setObservado(false);
        dto.setCompleto(false);

        dto.setStageVisual("PREPARACION");
        dto.setHeroBannerTitulo("Tu cotización está en borrador");
        dto.setEstadoLabel("Borrador activo");
        dto.setEstadoBadgeColor("purple");

        if (pasoNum == 1) {
            dto.setSiguientePasoTitulo("Completar datos del producto");
            dto.setSiguientePasoDesc("Ingresa al asistente guiado para completar los datos básicos y buscar el código arancelario.");
            dto.setSiguientePasoUrl("evaluacion.jsp?step=1");
            dto.setSiguientePasoIcono("document");
        } else if (pasoNum == 2) {
            dto.setSiguientePasoTitulo("Seleccionar código arancelario");
            dto.setSiguientePasoDesc("Debes elegir un código arancelario para tu producto para determinar los tributos aplicables.");
            dto.setSiguientePasoUrl("evaluacion.jsp?step=2");
            dto.setSiguientePasoIcono("search");
        } else if (pasoNum == 3) {
            dto.setSiguientePasoTitulo("Calcular costos y tributos");
            dto.setSiguientePasoDesc("Revisa la simulación de aranceles, IGV y costos logísticos estimados en base al código seleccionado.");
            dto.setSiguientePasoUrl("evaluacion.jsp?step=3");
            dto.setSiguientePasoIcono("calculator");
        } else {
            dto.setSiguientePasoTitulo("Subir documentos de importación");
            dto.setSiguientePasoDesc("Prepara los documentos iniciales (factura, BL/guía aérea) para simular el expediente final.");
            dto.setSiguientePasoUrl("evaluacion.jsp?step=4");
            dto.setSiguientePasoIcono("upload");
        }

        double fob = 0;
        if (map.containsKey("logFob")) {
            Object rawFob = map.get("logFob");
            if (rawFob instanceof Number) {
                fob = ((Number) rawFob).doubleValue();
            } else if (rawFob instanceof String) {
                try {
                    fob = Double.parseDouble((String) rawFob);
                } catch (Exception ignored) {}
            }
        }
        dto.setCostoBase(String.format("$%.2f", fob));

        if (borrador.getFechaActualizacion() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", new Locale("es", "PE"));
            dto.setUltimaActualizacion(sdf.format(new java.util.Date(borrador.getFechaActualizacion().getTime())));
        } else {
            dto.setUltimaActualizacion("Borrador temporal");
        }

        dto.setNumeroOperacion(0);

        inferirEntidad(dto, hsCode);

        List<String> alertas = new ArrayList<>();
        alertas.add("Esta importación es un borrador: Aún no has confirmado tu cotización. Haz clic en 'Continuar' para reanudar el asistente.");
        dto.setAlertas(alertas);

        return dto;
    }
}
