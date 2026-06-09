package com.importease.proyecto.dto;

import java.util.List;

/**
 * DTO de vista para seguimiento.jsp.
 * Contiene todos los datos calculados por SeguimientoServicio.
 * El JSP solo muestra — nunca calcula.
 */
public class SeguimientoDTO {

    // ── Estado del flujo ──────────────────────────────────────────────
    private int pasoActualNumero;        // 1-8 (paso interno de FlujoGuiadoServicio)
    private String pasoActualNombre;     // "Datos básicos", "HS Code", etc.
    private String pasoEstado;           // PENDIENTE | COMPLETO | OBSERVADO | BLOQUEADO
    private String motivoBloqueo;        // Si hay OBSERVADO/BLOQUEADO
    private int porcentajeAvance;        // 0-100, calculado desde pasos reales

    // ── Stage visual (4 pasos del stepper UI) ────────────────────────
    // PREPARACION | PERMISOS | EXPEDIENTE | LISTA
    private String stageVisual;

    // ── Textos dinámicos según paso ───────────────────────────────────
    private String heroBannerTitulo;     // "Tu importación está casi lista"
    private String estadoLabel;          // "Lista para revisión final"
    private String estadoBadgeColor;     // "green" | "orange" | "red" | "blue"
    private String siguientePasoTitulo;  // "Revisar y confirmar"
    private String siguientePasoDesc;    // "Verifica que los documentos de Caballas ..."
    private String siguientePasoUrl;     // "expediente-aduanero.jsp?operacionId=108"
    private String siguientePasoIcono;   // "document" | "shield" | "upload" | "check"

    // ── Datos del producto ────────────────────────────────────────────
    private String producto;             // Nombre completo de BD
    private String displayProducto;      // Nombre corto (sin paréntesis científicos)
    private String codigoArancelario;    // "0302440000" o "Pendiente"
    private String entidadRevisora;      // "DIGESA", "Sin alerta directa", etc.
    private String entidadNombreCompleto; // "Dirección General de Salud Ambiental..."

    // ── Detalles técnicos ─────────────────────────────────────────────
    private int numeroOperacion;
    private String costoBase;            // "$0.00"
    private String ultimaActualizacion;  // "10:53 p.m."

    // ── Documentos ────────────────────────────────────────────────────
    private boolean hasFactura;
    private boolean hasBl;
    private boolean hasCert;

    // ── Alertas contextuales ──────────────────────────────────────────
    private List<String> alertas;

    // ── Flags de estado ───────────────────────────────────────────────
    private boolean sinImportacion;      // true si no hay importación activa
    private boolean bloqueado;
    private boolean observado;
    private boolean completo;

    public SeguimientoDTO() {}

    // ── Getters y Setters ─────────────────────────────────────────────

    public int getPasoActualNumero() { return pasoActualNumero; }
    public void setPasoActualNumero(int pasoActualNumero) { this.pasoActualNumero = pasoActualNumero; }

    public String getPasoActualNombre() { return pasoActualNombre; }
    public void setPasoActualNombre(String pasoActualNombre) { this.pasoActualNombre = pasoActualNombre; }

    public String getPasoEstado() { return pasoEstado; }
    public void setPasoEstado(String pasoEstado) { this.pasoEstado = pasoEstado; }

    public String getMotivoBloqueao() { return motivoBloqueo; }
    public void setMotivoBloqueao(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public int getPorcentajeAvance() { return porcentajeAvance; }
    public void setPorcentajeAvance(int porcentajeAvance) { this.porcentajeAvance = porcentajeAvance; }

    public String getStageVisual() { return stageVisual; }
    public void setStageVisual(String stageVisual) { this.stageVisual = stageVisual; }

    public String getHeroBannerTitulo() { return heroBannerTitulo; }
    public void setHeroBannerTitulo(String heroBannerTitulo) { this.heroBannerTitulo = heroBannerTitulo; }

    public String getEstadoLabel() { return estadoLabel; }
    public void setEstadoLabel(String estadoLabel) { this.estadoLabel = estadoLabel; }

    public String getEstadoBadgeColor() { return estadoBadgeColor; }
    public void setEstadoBadgeColor(String estadoBadgeColor) { this.estadoBadgeColor = estadoBadgeColor; }

    public String getSiguientePasoTitulo() { return siguientePasoTitulo; }
    public void setSiguientePasoTitulo(String siguientePasoTitulo) { this.siguientePasoTitulo = siguientePasoTitulo; }

    public String getSiguientePasoDesc() { return siguientePasoDesc; }
    public void setSiguientePasoDesc(String siguientePasoDesc) { this.siguientePasoDesc = siguientePasoDesc; }

    public String getSiguientePasoUrl() { return siguientePasoUrl; }
    public void setSiguientePasoUrl(String siguientePasoUrl) { this.siguientePasoUrl = siguientePasoUrl; }

    public String getSiguientePasoIcono() { return siguientePasoIcono; }
    public void setSiguientePasoIcono(String siguientePasoIcono) { this.siguientePasoIcono = siguientePasoIcono; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public String getDisplayProducto() { return displayProducto; }
    public void setDisplayProducto(String displayProducto) { this.displayProducto = displayProducto; }

    public String getCodigoArancelario() { return codigoArancelario; }
    public void setCodigoArancelario(String codigoArancelario) { this.codigoArancelario = codigoArancelario; }

    public String getEntidadRevisora() { return entidadRevisora; }
    public void setEntidadRevisora(String entidadRevisora) { this.entidadRevisora = entidadRevisora; }

    public String getEntidadNombreCompleto() { return entidadNombreCompleto; }
    public void setEntidadNombreCompleto(String entidadNombreCompleto) { this.entidadNombreCompleto = entidadNombreCompleto; }

    public int getNumeroOperacion() { return numeroOperacion; }
    public void setNumeroOperacion(int numeroOperacion) { this.numeroOperacion = numeroOperacion; }

    public String getCostoBase() { return costoBase; }
    public void setCostoBase(String costoBase) { this.costoBase = costoBase; }

    public String getUltimaActualizacion() { return ultimaActualizacion; }
    public void setUltimaActualizacion(String ultimaActualizacion) { this.ultimaActualizacion = ultimaActualizacion; }

    public boolean isHasFactura() { return hasFactura; }
    public void setHasFactura(boolean hasFactura) { this.hasFactura = hasFactura; }

    public boolean isHasBl() { return hasBl; }
    public void setHasBl(boolean hasBl) { this.hasBl = hasBl; }

    public boolean isHasCert() { return hasCert; }
    public void setHasCert(boolean hasCert) { this.hasCert = hasCert; }

    public List<String> getAlertas() { return alertas; }
    public void setAlertas(List<String> alertas) { this.alertas = alertas; }

    public boolean isSinImportacion() { return sinImportacion; }
    public void setSinImportacion(boolean sinImportacion) { this.sinImportacion = sinImportacion; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public boolean isObservado() { return observado; }
    public void setObservado(boolean observado) { this.observado = observado; }

    public boolean isCompleto() { return completo; }
    public void setCompleto(boolean completo) { this.completo = completo; }

    /** Conveniencia: devuelve true si hay alertas que mostrar */
    public boolean tieneAlertas() { return alertas != null && !alertas.isEmpty(); }

    /** Conveniencia: paso del stepper visual (1-4) derivado del paso interno (1-8) */
    public int getStepperVisual() {
        if (pasoActualNumero <= 2) return 1;  // Preparación / Datos básicos
        if (pasoActualNumero == 3) return 2;  // HS Code / Permisos
        if (pasoActualNumero <= 5) return 3;  // Transporte + Documentos
        return 4;                             // Coherencia + DTA + Revisión final
    }

    /** Conveniencia: texto de progreso para la barra */
    public String getProgresoTexto() {
        return porcentajeAvance + "% completado";
    }
}
