package com.importease.proyecto.model;

public class Importacion {
    private int id;
    private int usuarioId;
    private int proveedorId;
    private String hsCode;
    private Integer blMasterId; 
    
    private String productoDesc;
    private String paisOrigen;
    private String incoterm;
    private java.math.BigDecimal valorFob = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal flete = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal seguro = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal valorCif = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal tipoCambio = java.math.BigDecimal.ZERO;
    
    private java.math.BigDecimal montoAdValorem = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal montoIsc = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal montoIgb = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal montoIpm = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal montoPercepcion = java.math.BigDecimal.ZERO;
    private java.math.BigDecimal totalImpuestos = java.math.BigDecimal.ZERO;
    
    private String estado; 
    private String canalAsignado;
    private String numeroDam;
    private java.sql.Timestamp fechaNumeracion;
    private java.sql.Timestamp fechaCreacion;
    private boolean usado = false;

    public Importacion() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getProveedorId() { return proveedorId; }
    public void setProveedorId(int proveedorId) { this.proveedorId = proveedorId; }

    public String getHsCode() { return hsCode; }
    public void setHsCode(String hsCode) { this.hsCode = hsCode; }

    public Integer getBlMasterId() { return blMasterId; }
    public void setBlMasterId(Integer blMasterId) { this.blMasterId = blMasterId; }

    public String getProductoDesc() { return productoDesc; }
    public void setProductoDesc(String productoDesc) { this.productoDesc = productoDesc; }

    public String getPaisOrigen() { return paisOrigen; }
    public void setPaisOrigen(String paisOrigen) { this.paisOrigen = paisOrigen; }

    public String getIncoterm() { return incoterm; }
    public void setIncoterm(String incoterm) { this.incoterm = incoterm; }

    public double getValorFob() { return valorFob != null ? valorFob.doubleValue() : 0.0; }
    public void setValorFob(double valorFob) { this.valorFob = java.math.BigDecimal.valueOf(valorFob); }
    public java.math.BigDecimal getValorFobBD() { return valorFob; }
    public void setValorFobBD(java.math.BigDecimal valorFob) { this.valorFob = valorFob; }

    public double getFlete() { return flete != null ? flete.doubleValue() : 0.0; }
    public void setFlete(double flete) { this.flete = java.math.BigDecimal.valueOf(flete); }
    public java.math.BigDecimal getFleteBD() { return flete; }
    public void setFleteBD(java.math.BigDecimal flete) { this.flete = flete; }

    public double getSeguro() { return seguro != null ? seguro.doubleValue() : 0.0; }
    public void setSeguro(double seguro) { this.seguro = java.math.BigDecimal.valueOf(seguro); }
    public java.math.BigDecimal getSeguroBD() { return seguro; }
    public void setSeguroBD(java.math.BigDecimal seguro) { this.seguro = seguro; }

    public double getValorCif() { return valorCif != null ? valorCif.doubleValue() : 0.0; }
    public void setValorCif(double valorCif) { this.valorCif = java.math.BigDecimal.valueOf(valorCif); }
    public java.math.BigDecimal getValorCifBD() { return valorCif; }
    public void setValorCifBD(java.math.BigDecimal valorCif) { this.valorCif = valorCif; }

    public double getTipoCambio() { return tipoCambio != null ? tipoCambio.doubleValue() : 0.0; }
    public void setTipoCambio(double tipoCambio) { this.tipoCambio = java.math.BigDecimal.valueOf(tipoCambio); }
    public java.math.BigDecimal getTipoCambioBD() { return tipoCambio; }
    public void setTipoCambioBD(java.math.BigDecimal tipoCambio) { this.tipoCambio = tipoCambio; }

    public double getMontoAdValorem() { return montoAdValorem != null ? montoAdValorem.doubleValue() : 0.0; }
    public void setMontoAdValorem(double montoAdValorem) { this.montoAdValorem = java.math.BigDecimal.valueOf(montoAdValorem); }
    public java.math.BigDecimal getMontoAdValoremBD() { return montoAdValorem; }
    public void setMontoAdValoremBD(java.math.BigDecimal montoAdValorem) { this.montoAdValorem = montoAdValorem; }

    public double getMontoIsc() { return montoIsc != null ? montoIsc.doubleValue() : 0.0; }
    public void setMontoIsc(double montoIsc) { this.montoIsc = java.math.BigDecimal.valueOf(montoIsc); }
    public java.math.BigDecimal getMontoIscBD() { return montoIsc; }
    public void setMontoIscBD(java.math.BigDecimal montoIsc) { this.montoIsc = montoIsc; }

    public double getMontoIgb() { return montoIgb != null ? montoIgb.doubleValue() : 0.0; }
    public void setMontoIgb(double montoIgb) { this.montoIgb = java.math.BigDecimal.valueOf(montoIgb); }
    public java.math.BigDecimal getMontoIgbBD() { return montoIgb; }
    public void setMontoIgbBD(java.math.BigDecimal montoIgb) { this.montoIgb = montoIgb; }

    public double getMontoIpm() { return montoIpm != null ? montoIpm.doubleValue() : 0.0; }
    public void setMontoIpm(double montoIpm) { this.montoIpm = java.math.BigDecimal.valueOf(montoIpm); }
    public java.math.BigDecimal getMontoIpmBD() { return montoIpm; }
    public void setMontoIpmBD(java.math.BigDecimal montoIpm) { this.montoIpm = montoIpm; }

    public double getMontoPercepcion() { return montoPercepcion != null ? montoPercepcion.doubleValue() : 0.0; }
    public void setMontoPercepcion(double montoPercepcion) { this.montoPercepcion = java.math.BigDecimal.valueOf(montoPercepcion); }
    public java.math.BigDecimal getMontoPercepcionBD() { return montoPercepcion; }
    public void setMontoPercepcionBD(java.math.BigDecimal montoPercepcion) { this.montoPercepcion = montoPercepcion; }

    public double getTotalImpuestos() { return totalImpuestos != null ? totalImpuestos.doubleValue() : 0.0; }
    public void setTotalImpuestos(double totalImpuestos) { this.totalImpuestos = java.math.BigDecimal.valueOf(totalImpuestos); }
    public java.math.BigDecimal getTotalImpuestosBD() { return totalImpuestos; }
    public void setTotalImpuestosBD(java.math.BigDecimal totalImpuestos) { this.totalImpuestos = totalImpuestos; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCanalAsignado() { return canalAsignado; }
    public void setCanalAsignado(String canalAsignado) { this.canalAsignado = canalAsignado; }

    public String getNumeroDam() { return numeroDam; }
    public void setNumeroDam(String numeroDam) { this.numeroDam = numeroDam; }

    public java.sql.Timestamp getFechaNumeracion() { return fechaNumeracion; }
    public void setFechaNumeracion(java.sql.Timestamp fechaNumeracion) { this.fechaNumeracion = fechaNumeracion; }

    public java.sql.Timestamp getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(java.sql.Timestamp fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isUsado() { return usado; }
    public void setUsado(boolean usado) { this.usado = usado; }
}

