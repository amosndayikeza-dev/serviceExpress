package com.serviceexpress.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    // Méthode pour lire les variables d'environnement
    private static String getDbUrl() {
        // Priorité aux variables d'environnement (Render)
        String renderUrl = System.getenv("DB_URL");
        if (renderUrl != null && !renderUrl.isEmpty()) {
            System.out.println("Utilisation DB_URL de Render: " + renderUrl);
            return renderUrl;
        }

        // Fallback pour le développement local
        System.out.println("Utilisation base locale");
        return "jdbc:mysql://localhost:3306/service_express?useSSL=false";
    }

    private static String getDbUsername() {
        String renderUser = System.getenv("DB_USERNAME");
        if (renderUser != null && !renderUser.isEmpty()) {
            return renderUser;
        }
        return "root"; // Local default
    }

    private static String getDbPassword() {
        String renderPass = System.getenv("DB_PASSWORD");
        if (renderPass != null && !renderPass.isEmpty()) {
            return renderPass;
        }
        return ""; // Local default
    }

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        String url = getDbUrl();
        String username = getDbUsername();
        String password = getDbPassword();

        System.out.println("Tentative de connexion à: " + url);
        System.out.println("Utilisateur: " + username);

        return DriverManager.getConnection(url, username, password);
    }
}