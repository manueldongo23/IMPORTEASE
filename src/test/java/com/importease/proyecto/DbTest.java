package com.importease.proyecto;

import java.sql.Connection;
import java.sql.DriverManager;

public class DbTest {
    public static void main(String[] args) {
        String[] urls = {
            "jdbc:mysql://localhost:3306/importease_db?useSSL=false&serverTimezone=America/Lima",
            "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=America/Lima"
        };
        String[] users = {"root", "importease_app"};
        String[] passwords = {"", "importease_dev", "root_dev", "root", "admin", "123456", "mysql"};

        for (String url : urls) {
            for (String user : users) {
                for (String password : passwords) {
                    try {
                        Class.forName("com.mysql.cj.jdbc.Driver");
                        try (Connection conn = DriverManager.getConnection(url, user, password)) {
                            System.out.println("SUCCESS: URL=" + url + " | USER=" + user + " | PASS='" + password + "'");
                            return;
                        }
                    } catch (Exception e) {
                        // ignore and try next
                    }
                }
            }
        }
        System.out.println("FAILED: No credential combination worked.");
    }
}
