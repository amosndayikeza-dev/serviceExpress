package com.test;

import com.dao.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("🔌 Test de connexion à la base de données...");

        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Connexion établie !");

            // Tester la table
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM ad");

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("📊 Nombre d'annonces dans la base: " + count);
            }

        } catch (SQLException e) {
            System.err.println("❌ Erreur: " + e.getMessage());

            // Afficher des conseils
            System.out.println("\n💡 Conseils de dépannage:");
            System.out.println("1. Vérifiez que MySQL est démarré (XAMPP/WAMP/MAMP)");
            System.out.println("2. Vérifiez les identifiants (root/'' par défaut)");
            System.out.println("3. Vérifiez que la base 'service_express' existe");
            System.out.println("4. Vérifiez que le driver MySQL est dans le classpath");
        }
    }
}