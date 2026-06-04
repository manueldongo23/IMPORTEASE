package com.importease.proyecto.service.aduanas;

import com.importease.proyecto.model.HsCode;
import com.importease.proyecto.model.Importacion;
import com.importease.proyecto.model.Usuario;
import com.importease.proyecto.repository.HsCodeDAO;
import com.importease.proyecto.repository.UsuarioDAO;
import com.importease.proyecto.service.ArancelService;
import com.importease.proyecto.service.CalculadoraTributaria;
import com.importease.proyecto.service.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

public class AduanasDamService {

    public long ensureDam(Connection con, Importacion imp, String numeroDam, String regimenCodigo, String modalidadCodigo, String canalControl) throws Exception {
        String find = "SELECT id FROM dam_cabecera WHERE operacion_id = ?";
        try (PreparedStatement ps = con.prepareStatement(find)) {
            ps.setInt(1, imp.getId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
        }

        String sql = "INSERT INTO dam_cabecera (operacion_id, numero_dam, regimen_codigo, modalidad_codigo, canal_control, canal_es_oficial, estado, fecha_numeracion, fuente, source_type, confidence) VALUES (?, ?, ?, ?, ?, FALSE, 'PRE_DAM_REFERENCIAL', NOW(), 'SIMULACION_ACADEMICA', 'SIMULADO', 0.20) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, imp.getId());
            ps.setString(2, numeroDam);
            ps.setString(3, regimenCodigo);
            ps.setString(4, modalidadCodigo);
            ps.setString(5, canalControl);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new IllegalStateException("No se creo DAM.");
    }

    public void ensureSerie(Connection con, long damId, Importacion imp, BigDecimal adValoremPct, boolean requierePermiso) throws Exception {
        String sql = "INSERT INTO dam_series (dam_id, numero_serie, hs_code, descripcion_mercancia, pais_origen, valor_fob, flete, seguro, valor_cif, ad_valorem_pct, igv_pct, ipm_pct, isc_pct, requiere_permiso) VALUES (?, 1, ?, ?, ?, ?, ?, ?, ?, ?, 16.00, 2.00, 0.00, ?) ON DUPLICATE KEY UPDATE hs_code=VALUES(hs_code), descripcion_mercancia=VALUES(descripcion_mercancia), valor_fob=VALUES(valor_fob), flete=VALUES(flete), seguro=VALUES(seguro), valor_cif=VALUES(valor_cif), requiere_permiso=VALUES(requiere_permiso)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, damId);
            ps.setString(2, imp.getHsCode());
            ps.setString(3, safeText(imp.getProductoDesc(), "Mercancia por describir"));
            ps.setString(4, safeText(imp.getPaisOrigen(), "POR_CONFIRMAR"));
            ps.setBigDecimal(5, imp.getValorFobBD());
            ps.setBigDecimal(6, imp.getFleteBD());
            ps.setBigDecimal(7, imp.getSeguroBD());
            ps.setBigDecimal(8, imp.getValorCifBD());
            ps.setBigDecimal(9, adValoremPct);
            ps.setBoolean(10, requierePermiso);
            ps.executeUpdate();
        }
    }

    public void ensureDta(Connection con, long damId, Importacion imp) throws Exception {
        Timestamp fechaNumeracion = obtenerFechaNumeracion(con, damId);
        CalculadoraTributaria.ResultadoTributario tributos = recalcularTributos(imp);
        Timestamp fechaExigibilidad = siguienteDiaHabil(fechaNumeracion);

        String sql = "INSERT INTO deuda_tributaria_aduanera (operacion_id, dam_id, fecha_nacimiento, fecha_exigibilidad, base_imponible_cif, ad_valorem, isc, igv, ipm, percepcion, total, estado, source_type, confidence) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'REFERENCIAL', 'ESTIMADO', 0.60) ON DUPLICATE KEY UPDATE dam_id=VALUES(dam_id), fecha_nacimiento=VALUES(fecha_nacimiento), fecha_exigibilidad=VALUES(fecha_exigibilidad), base_imponible_cif=VALUES(base_imponible_cif), ad_valorem=VALUES(ad_valorem), isc=VALUES(isc), igv=VALUES(igv), ipm=VALUES(ipm), percepcion=VALUES(percepcion), total=VALUES(total)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, imp.getId());
            ps.setLong(2, damId);
            ps.setTimestamp(3, fechaNumeracion);
            ps.setTimestamp(4, fechaExigibilidad);
            ps.setBigDecimal(5, tributos.getCif());
            ps.setBigDecimal(6, tributos.getArancel());
            ps.setBigDecimal(7, tributos.getIsc());
            ps.setBigDecimal(8, tributos.getIgv());
            ps.setBigDecimal(9, tributos.getIpm());
            ps.setBigDecimal(10, tributos.getPercepcion());
            ps.setBigDecimal(11, tributos.getTotalImpuestos());
            ps.executeUpdate();
        }
    }

    private Timestamp obtenerFechaNumeracion(Connection con, long damId) throws Exception {
        Timestamp fechaNumeracion = new Timestamp(System.currentTimeMillis());
        String sqlSelect = "SELECT fecha_numeracion FROM dam_cabecera WHERE id = ? LIMIT 1";
        try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
            psSel.setLong(1, damId);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_numeracion");
                    if (ts != null) fechaNumeracion = ts;
                }
            }
        }
        return fechaNumeracion;
    }

    private CalculadoraTributaria.ResultadoTributario recalcularTributos(Importacion imp) {
        try {
            HsCodeDAO hsDao = new HsCodeDAO();
            HsCode hs = hsDao.obtenerPorCodigo(imp.getHsCode());
            if (hs == null) {
                ArancelService arancelService = new ArancelService();
                hs = arancelService.consultarArancel(imp.getHsCode());
            }
            Usuario usuario = new UsuarioDAO().buscarPorId(imp.getUsuarioId());
            if (hs != null) {
                return CalculadoraTributaria.calcularImpuestos(
                        hs, usuario,
                        imp.getValorFobBD(), imp.getFleteBD(), imp.getSeguroBD(),
                        imp.getPaisOrigen(), imp.getTipoCambioBD(), imp.getIncoterm(), imp.isUsado()
                );
            }
        } catch (Exception calcEx) {
            LoggerUtil.warn("Error al recalcular tributos en ensureDta, usando valores previos: " + calcEx.getMessage());
        }

        CalculadoraTributaria.ResultadoTributario resultado = new CalculadoraTributaria.ResultadoTributario();
        resultado.setCif(imp.getValorCifBD());
        resultado.setArancel(imp.getMontoAdValoremBD());
        resultado.setIsc(imp.getMontoIscBD());
        resultado.setIgv(imp.getMontoIgbBD());
        resultado.setIpm(imp.getMontoIpmBD());
        resultado.setPercepcion(imp.getMontoPercepcionBD());
        resultado.setTotalImpuestos(imp.getTotalImpuestosBD());
        resultado.setTotalNacionalizado(imp.getValorCifBD().add(imp.getTotalImpuestosBD()));
        return resultado;
    }

    private Timestamp siguienteDiaHabil(Timestamp fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fecha.getTime());
        int dia;
        do {
            cal.add(Calendar.DAY_OF_MONTH, 1);
            dia = cal.get(Calendar.DAY_OF_WEEK);
        } while (dia == Calendar.SATURDAY || dia == Calendar.SUNDAY);
        return new Timestamp(cal.getTimeInMillis());
    }

    private String safeText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
