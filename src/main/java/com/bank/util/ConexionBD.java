package com.bank.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    // Configurar tu usuario y contraseña del SGBD, cuidado con el puerto
    private static final String URL = "jdbc:mariadb://localhost:3307/java_bank";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}