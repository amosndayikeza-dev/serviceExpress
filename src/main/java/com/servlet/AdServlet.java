package com.servlet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.dao.AdDAO;
import com.model.Ad;
import com.utils.ValidationUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/ads/*")
public class AdServlet extends HttpServlet {
    private AdDAO adDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        adDAO = new AdDAO();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String category = request.getParameter("category");
        String search = request.getParameter("search");

        // ⬇️ NOUVEAU : Vérifier si c'est une requête pour les catégories ⬇️
        String categoriesParam = request.getParameter("categories");

        PrintWriter out = response.getWriter();

        try {
            // ⬇️ NOUVEAU : Si demande de catégories ⬇️
            if (categoriesParam != null && categoriesParam.equals("true")) {
                List<String> categories = adDAO.getAllCategories();
                Map<String, Object> response_map = new HashMap<>();
                response_map.put("success", true);
                response_map.put("categories", categories);
                out.print(gson.toJson(response_map));
                return;
            }

            List<Ad> ads = null;

            // Si c'est une requête pour une annonce spécifique
            if (pathInfo != null && !pathInfo.equals("/")) {
                String[] splits = pathInfo.split("/");
                if (splits.length == 2) {
                    Long id = Long.parseLong(splits[1]);
                    Ad ad = adDAO.getAdById(id);
                    if (ad != null) {
                        out.print(gson.toJson(ad));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Annonce non trouvée\"}");
                    }
                }
            }
            // Recherche par mot-clé
            else if (search != null && !search.isEmpty()) {
                ads = adDAO.searchAds(search);
                Map<String, Object> response_map = new HashMap<>();
                response_map.put("success", true);
                response_map.put("data", ads);
                response_map.put("count", ads.size());
                out.print(gson.toJson(response_map));
            }
            // Filtre par catégorie
            else if (category != null && !category.isEmpty()) {
                ads = adDAO.getAdsByCategory(category);
                Map<String, Object> response_map = new HashMap<>();
                response_map.put("success", true);
                response_map.put("data", ads);
                response_map.put("count", ads.size());
                out.print(gson.toJson(response_map));
            }
            // Liste toutes les annonces
            else {
                ads = adDAO.getAllAds();
                Map<String, Object> response_map = new HashMap<>();
                response_map.put("success", true);
                response_map.put("data", ads);
                response_map.put("count", ads.size());
                out.print(gson.toJson(response_map));
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String adminPassword = request.getHeader("X-Admin-Password");
        if (adminPassword == null || !"admin123".equals(adminPassword)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"success\": false, \"message\": \"Mot de passe incorrect\"}");
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"success\": false, \"message\": \"ID manquant\"}");
            return;
        }

        try {
            Long id = Long.parseLong(pathInfo.substring(1));
            boolean deleted = adDAO.deleteAd(id);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().print("{\"success\": true, \"message\": \"Annonce supprimée avec succès\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().print("{\"success\": false, \"message\": \"Annonce non trouvée\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"success\": false, \"message\": \"ID invalide\"}");
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        Ad ad = gson.fromJson(sb.toString(), Ad.class);
        Map<String, String> errors = new HashMap<>();

        if (!validateAndSanitizeAd(ad, errors)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", errors.values().iterator().next());
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(errorResponse));
            return;
        }

        try {
            boolean created = adDAO.createAd(ad);
            if (created) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                Map<String, Object> successResponse = new HashMap<>();
                successResponse.put("success", true);
                successResponse.put("message", "Annonce publiée avec succès!");
                successResponse.put("id", ad.getId());
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(successResponse));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Erreur lors de la création");
                PrintWriter out = response.getWriter();
                out.print(gson.toJson(errorResponse));
            }
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            PrintWriter out = response.getWriter();
            out.print(gson.toJson(errorResponse));
            e.printStackTrace();
        }
    }

    private boolean validateAndSanitizeAd(Ad ad, Map<String, String> errors) {
        Map<String, String> validationErrors = ValidationUtils.validateAd(ad);

        if (!validationErrors.isEmpty()) {
            errors.putAll(validationErrors);
            return false;
        }

        Ad sanitized = ValidationUtils.sanitizeAd(ad);
        ad.setServiceName(sanitized.getServiceName());
        ad.setDescription(sanitized.getDescription());
        ad.setLocation(sanitized.getLocation());
        ad.setPhone(sanitized.getPhone());
        ad.setCategory(sanitized.getCategory());
        ad.setArtisanName(sanitized.getArtisanName());

        return true;
    }
}