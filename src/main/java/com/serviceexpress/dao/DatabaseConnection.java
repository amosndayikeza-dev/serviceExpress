package com.serviceexpress.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() throws SQLException {
        // Lire les variables d'environnement
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USERNAME");
        String password = System.getenv("DB_PASSWORD");

        // Si variables non trouvées, utiliser valeurs par défaut (local)
        if (url == null) {
            url = "jdbc:mysql://localhost:3306/service_express?useSSL=false";
            user = "root";
            password = "";
            System.out.println("⚠️ Utilisation base locale");
        } else {
            System.out.println("✅ Utilisation DB_URL: " + url);
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL chargé");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Driver non trouvé: " + e.getMessage());
        }

        System.out.println("🔌 Connexion à MySQL...");
        return DriverManager.getConnection(url, user, password);
    }
}