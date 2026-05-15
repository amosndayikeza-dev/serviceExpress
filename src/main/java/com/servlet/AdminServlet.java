package com.serviceexpress.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.serviceexpress.dao.AdDAO;
import com.serviceexpress.model.Ad;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/*")
public class AdminServlet extends HttpServlet {
    private AdDAO adDAO;
    private Gson gson;
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    public void init() throws ServletException {
        adDAO = new AdDAO();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        // Route: /admin/login - Connexion admin
        if ("/login".equals(pathInfo)) {
            handleLogin(request, response);
        }
        // Route: /admin/logout - Déconnexion admin
        else if ("/logout".equals(pathInfo)) {
            handleLogout(request, response);
        }
        // Route: /admin/delete - Supprimer annonce
        else if ("/delete".equals(pathInfo)) {
            handleDeleteAd(request, response);
        }
        // Route: /admin/update - Modifier annonce
        else if ("/update".equals(pathInfo)) {
            handleUpdateAd(request, response);
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Route non trouvée\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        // Vérifier la session admin
        HttpSession session = request.getSession(false);

        // Route: /admin/check - Vérifier si admin est connecté
        if ("/check".equals(pathInfo)) {
            boolean isLoggedIn = (session != null && session.getAttribute("admin") != null);
            Map<String, Object> result = new HashMap<>();
            result.put("loggedIn", isLoggedIn);
            out.print(gson.toJson(result));
        }
        // Route: /admin/ads - Liste des annonces pour admin
        else if ("/ads".equals(pathInfo)) {
            // Vérifier si admin est connecté
            if (session == null || session.getAttribute("admin") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Non autorisé. Veuillez vous connecter.\"}");
                return;
            }

            try {
                List<Ad> ads = adDAO.getAllAds();
                Map<String, Object> result = new HashMap<>();
                result.put("ads", ads);
                result.put("total", ads.size());
                out.print(gson.toJson(result));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        }
        // Route: /admin/stats - Statistiques
        else if ("/stats".equals(pathInfo)) {
            if (session == null || session.getAttribute("admin") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Non autorisé\"}");
                return;
            }

            try {
                List<Ad> ads = adDAO.getAllAds();
                Map<String, Object> stats = new HashMap<>();

                // Compter par catégorie
                Map<String, Integer> categoryCount = new HashMap<>();
                for (Ad ad : ads) {
                    String category = ad.getCategory();
                    categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
                }

                stats.put("totalAds", ads.size());
                stats.put("categoryDistribution", categoryCount);
                stats.put("lastUpdate", new java.util.Date());

                out.print(gson.toJson(stats));
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"" + e.getMessage() + "\"}");
                e.printStackTrace();
            }
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Route non trouvée\"}");
        }
    }

    // Gérer la connexion admin
    private void handleLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, String> loginData = gson.fromJson(sb.toString(), Map.class);
        String password = loginData.get("password");

        PrintWriter out = response.getWriter();

        if (ADMIN_PASSWORD.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("admin", true);
            session.setMaxInactiveInterval(1800); // 30 minutes

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Connexion réussie");
            result.put("redirect", "/ServiceExpress/admin.html");
            out.print(gson.toJson(result));
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "Mot de passe incorrect");
            out.print(gson.toJson(result));
        }
    }

    // Gérer la déconnexion admin
    private void handleLogout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        PrintWriter out = response.getWriter();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Déconnexion réussie");
        out.print(gson.toJson(result));
    }

    // Gérer la suppression d'une annonce
    private void handleDeleteAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Vérifier session admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\": \"Non autorisé\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> deleteData = gson.fromJson(sb.toString(), Map.class);
        Long id = ((Number) deleteData.get("id")).longValue();

        PrintWriter out = response.getWriter();

        try {
            boolean deleted = adDAO.deleteAd(id);
            if (deleted) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "Annonce supprimée avec succès");
                out.print(gson.toJson(result));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Annonce non trouvée");
                out.print(gson.toJson(result));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            out.print(gson.toJson(result));
            e.printStackTrace();
        }
    }

    // Gérer la modification d'une annonce
    private void handleUpdateAd(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Vérifier session admin
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("admin") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"error\": \"Non autorisé\"}");
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        Ad ad = gson.fromJson(sb.toString(), Ad.class);

        PrintWriter out = response.getWriter();

        try {
            // Vérifier si l'annonce existe
            Ad existingAd = adDAO.getAdById(ad.getId());
            if (existingAd == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", "Annonce non trouvée");
                out.print(gson.toJson(result));
                return;
            }

            // Mise à jour dans la base de données
            String sql = "UPDATE ad SET service_name=?, description=?, price=?, location=?, phone=?, category=?, artisan_name=? WHERE id=?";

            try (java.sql.Connection conn = com.serviceexpress.dao.DatabaseConnection.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, ad.getServiceName());
                pstmt.setString(2, ad.getDescription());
                pstmt.setObject(3, ad.getPrice());
                pstmt.setString(4, ad.getLocation());
                pstmt.setString(5, ad.getPhone());
                pstmt.setString(6, ad.getCategory());
                pstmt.setString(7, ad.getArtisanName());
                pstmt.setLong(8, ad.getId());

                int updated = pstmt.executeUpdate();

                if (updated > 0) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "Annonce modifiée avec succès");
                    result.put("ad", ad);
                    out.print(gson.toJson(result));
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", false);
                    result.put("error", "Erreur lors de la modification");
                    out.print(gson.toJson(result));
                }
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            out.print(gson.toJson(result));
            e.printStackTrace();
        }
    }
}