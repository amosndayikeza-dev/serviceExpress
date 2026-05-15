package com.dao;

import com.model.Ad;
import java.sql.*;
import java.util.*;

public class AdDAO {

    // Récupérer toutes les annonces
    public List<Ad> getAllAds() throws SQLException {
        List<Ad> ads = new ArrayList<>();
        String sql = "SELECT * FROM ad ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ads.add(mapResultSetToAd(rs));
            }
        }
        return ads;
    }


    // ⬇️ AJOUTEZ CETTE MÉTHODE ⬇️
    public List<String> getAllCategories() throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM ad ORDER BY category";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
        }

        // Si aucune catégorie n'existe, retourner les catégories par défaut
        if (categories.isEmpty()) {
            categories = Arrays.asList("Coiffure", "Réparation", "Menuiserie",
                    "Couture", "Plomberie", "Électricité", "Autre");
        }

        return categories;
    }

    // Filtrer par catégorie
    public List<Ad> getAdsByCategory(String category) throws SQLException {
        List<Ad> ads = new ArrayList<>();
        String sql = "SELECT * FROM ad WHERE category = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ads.add(mapResultSetToAd(rs));
            }
        }
        return ads;
    }

    // Rechercher par mot-clé
    public List<Ad> searchAds(String keyword) throws SQLException {
        List<Ad> ads = new ArrayList<>();
        String sql = "SELECT * FROM ad WHERE service_name LIKE ? OR description LIKE ? OR location LIKE ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ads.add(mapResultSetToAd(rs));
            }
        }
        return ads;
    }

    // Insérer une annonce
    public boolean createAd(Ad ad) throws SQLException {
        String sql = "INSERT INTO ad (service_name, description, price, location, phone, category, artisan_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, ad.getServiceName());
            pstmt.setString(2, ad.getDescription());
            pstmt.setObject(3, ad.getPrice());
            pstmt.setString(4, ad.getLocation());
            pstmt.setString(5, ad.getPhone());
            pstmt.setString(6, ad.getCategory());
            pstmt.setString(7, ad.getArtisanName());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    ad.setId(generatedKeys.getLong(1));
                }
                return true;
            }
            return false;
        }
    }

    // Supprimer une annonce
    public boolean deleteAd(Long id) throws SQLException {
        String sql = "DELETE FROM ad WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        }
    }

    // Récupérer une annonce par ID
    public Ad getAdById(Long id) throws SQLException {
        String sql = "SELECT * FROM ad WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAd(rs);
            }
            return null;
        }
    }

    // Mapper ResultSet vers objet Ad
    private Ad mapResultSetToAd(ResultSet rs) throws SQLException {
        Ad ad = new Ad();
        ad.setId(rs.getLong("id"));
        ad.setServiceName(rs.getString("service_name"));
        ad.setDescription(rs.getString("description"));
        ad.setPrice(rs.getInt("price"));
        ad.setLocation(rs.getString("location"));
        ad.setPhone(rs.getString("phone"));
        ad.setCategory(rs.getString("category"));
        ad.setArtisanName(rs.getString("artisan_name"));
        ad.setCreatedAt(rs.getTimestamp("created_at"));
        return ad;
    }


    // Optionnel : méthode pour compter les annonces par catégorie
    public Map<String, Long> getCategoryCounts() throws SQLException {
        Map<String, Long> counts = new HashMap<>();
        String sql = "SELECT category, COUNT(*) as count FROM ad GROUP BY category";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                counts.put(rs.getString("category"), rs.getLong("count"));
            }
        }

        return counts;
    }
}