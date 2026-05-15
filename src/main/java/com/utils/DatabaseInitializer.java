package com.serviceexpress.utils;

import com.serviceexpress.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initDatabase() {
        System.out.println("🚀 Initialisation de la base de données...");

        String createTableSQL = "CREATE TABLE IF NOT EXISTS ad (" +
                "id BIGINT PRIMARY KEY AUTO_INCREMENT," +
                "service_name VARCHAR(200) NOT NULL," +
                "description TEXT NOT NULL," +
                "price INT," +
                "location VARCHAR(100) NOT NULL," +
                "phone VARCHAR(20) NOT NULL," +
                "category VARCHAR(50) NOT NULL," +
                "artisan_name VARCHAR(100)," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Table 'ad' créée ou déjà existante !");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}