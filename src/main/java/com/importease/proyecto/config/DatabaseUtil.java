package com.importease.proyecto.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utility class for obtaining JDBC connections.
 * Delegates to business ConexionDB helper.
 */
public class DatabaseUtil {

    /**
     * Returns a new {@link Connection}. Caller is responsible for closing it.
     */
    public static Connection getConnection() throws SQLException {
        return com.importease.proyecto.service.ConexionDB.obtenerConexion();
    }
}


