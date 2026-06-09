package com.importease.proyecto.dto;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaDTO {
    private int totalOperaciones;
    private double totalFob;
    private double totalImpuestos;
    private int canalVerde;
    private int canalNaranja;
    private int canalRojo;
    private int totalAlertas;
    private int totalPermisos;

    private List<OperacionAuditoriaDTO> operaciones = new ArrayList<>();
    private List<PermisoAuditoriaDTO> permisos = new ArrayList<>();
    private List<PartidaAuditoriaDTO> topPartidas = new ArrayList<>();
    private List<IncidenciaDTO> logsIncidencias = new ArrayList<>();

    public static class OperacionAuditoriaDTO {
        private int id;
        private String hsCode;
        private double fob;
        private double cif;
        private String estado;
        private String canalAsignado;
        private String fechaCreacion;
        private double totalImpuestos;
        private boolean documentoFactura;
        private boolean documentoBl;
        private String entidad;
        private String productoDesc;
        private String paisOrigen;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getHsCode() { return hsCode; }
        public void setHsCode(String hsCode) { this.hsCode = hsCode; }
        public double getFob() { return fob; }
        public void setFob(double fob) { this.fob = fob; }
        public double getCif() { return cif; }
        public void setCif(double cif) { this.cif = cif; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getCanalAsignado() { return canalAsignado; }
        public void setCanalAsignado(String canalAsignado) { this.canalAsignado = canalAsignado; }
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
        public double getTotalImpuestos() { return totalImpuestos; }
        public void setTotalImpuestos(double totalImpuestos) { this.totalImpuestos = totalImpuestos; }
        public boolean isDocumentoFactura() { return documentoFactura; }
        public void setDocumentoFactura(boolean documentoFactura) { this.documentoFactura = documentoFactura; }
        public boolean isDocumentoBl() { return documentoBl; }
        public void setDocumentoBl(boolean documentoBl) { this.documentoBl = documentoBl; }
        public String getEntidad() { return entidad; }
        public void setEntidad(String entidad) { this.entidad = entidad; }
        public String getProductoDesc() { return productoDesc; }
        public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }
        public String getPaisOrigen() { return paisOrigen; }
        public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }
    }

    public static class PermisoAuditoriaDTO {
        private int id;
        private int operacionId;
        private String codigoEntidad;
        private String tipoPermiso;
        private String estado;
        private String numeroSuce;
        private String numeroDocumentoResolutivo;
        private String fechaCreacion;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getOperacionId() { return operacionId; }
        public void setOperacionId(int operacionId) { this.operacionId = operacionId; }
        public String getCodigoEntidad() { return codigoEntidad; }
        public void setCodigoEntidad(String codigoEntidad) { this.codigoEntidad = codigoEntidad; }
        public String getTipoPermiso() { return tipoPermiso; }
        public void setTipoPermiso(String tipoPermiso) { this.tipoPermiso = tipoPermiso; }
        public String getEstado() { return estado; }
        public void setEstado(String estado) { this.estado = estado; }
        public String getNumeroSuce() { return numeroSuce; }
        public void setNumeroSuce(String numeroSuce) { this.numeroSuce = numeroSuce; }
        public String getNumeroDocumentoResolutivo() { return numeroDocumentoResolutivo; }
        public void setNumeroDocumentoResolutivo(String numeroDocumentoResolutivo) { this.numeroDocumentoResolutivo = numeroDocumentoResolutivo; }
        public String getFechaCreacion() { return fechaCreacion; }
        public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    }

    public static class PartidaAuditoriaDTO {
        private String codigo;
        private int busquedas;

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }
        public int getBusquedas() { return busquedas; }
        public void setBusquedas(int busquedas) { this.busquedas = busquedas; }
    }

    public static class IncidenciaDTO {
        private String modulo;
        private String desc;
        private String origen;

        public IncidenciaDTO(String modulo, String desc, String origen) {
            this.modulo = modulo;
            this.desc = desc;
            this.origen = origen;
        }

        public String getModulo() { return modulo; }
        public void setModulo(String modulo) { this.modulo = modulo; }
        public String getDesc() { return desc; }
        public void setDesc(String desc) { this.desc = desc; }
        public String getOrigen() { return origen; }
        public void setOrigen(String origen) { this.origen = origen; }
    }

    // Getters and Setters for main class
    public int getTotalOperaciones() { return totalOperaciones; }
    public void setTotalOperaciones(int totalOperaciones) { this.totalOperaciones = totalOperaciones; }
    public double getTotalFob() { return totalFob; }
    public void setTotalFob(double totalFob) { this.totalFob = totalFob; }
    public double getTotalImpuestos() { return totalImpuestos; }
    public void setTotalImpuestos(double totalImpuestos) { this.totalImpuestos = totalImpuestos; }
    public int getCanalVerde() { return canalVerde; }
    public void setCanalVerde(int canalVerde) { this.canalVerde = canalVerde; }
    public int getCanalNaranja() { return canalNaranja; }
    public void setCanalNaranja(int canalNaranja) { this.canalNaranja = canalNaranja; }
    public int getCanalRojo() { return canalRojo; }
    public void setCanalRojo(int canalRojo) { this.canalRojo = canalRojo; }
    public int getTotalAlertas() { return totalAlertas; }
    public void setTotalAlertas(int totalAlertas) { this.totalAlertas = totalAlertas; }
    public int getTotalPermisos() { return totalPermisos; }
    public void setTotalPermisos(int totalPermisos) { this.totalPermisos = totalPermisos; }
    public List<OperacionAuditoriaDTO> getOperaciones() { return operaciones; }
    public void setOperaciones(List<OperacionAuditoriaDTO> operaciones) { this.operaciones = operaciones; }
    public List<PermisoAuditoriaDTO> getPermisos() { return permisos; }
    public void setPermisos(List<PermisoAuditoriaDTO> permisos) { this.permisos = permisos; }
    public List<PartidaAuditoriaDTO> getTopPartidas() { return topPartidas; }
    public void setTopPartidas(List<PartidaAuditoriaDTO> topPartidas) { this.topPartidas = topPartidas; }
    public List<IncidenciaDTO> getLogsIncidencias() { return logsIncidencias; }
    public void setLogsIncidencias(List<IncidenciaDTO> logsIncidencias) { this.logsIncidencias = logsIncidencias; }
}
