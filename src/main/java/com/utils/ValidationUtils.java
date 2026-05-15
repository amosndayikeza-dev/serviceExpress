package com.serviceexpress.utils;

import com.serviceexpress.model.Ad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ValidationUtils {

    // Constantes
    private static final int MAX_SERVICE_NAME_LENGTH = 200;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final int MAX_LOCATION_LENGTH = 100;
    private static final int MAX_PHONE_LENGTH = 20;
    private static final int MAX_ARTISAN_NAME_LENGTH = 100;
    private static final int MIN_PRICE = 0;
    private static final int MAX_PRICE = 999999999;

    // Pattern pour le numéro de téléphone burundais
    // Accepte: 61234567, 62 34 56 78, 79 87 65 43, +25761234567
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+257)?[0-9]{8}$|^(\\+257)?[0-9]{2}[ -]?[0-9]{2}[ -]?[0-9]{2}[ -]?[0-9]{2}$"
    );

    // Pattern pour le prix (uniquement nombres positifs)
    private static final Pattern PRICE_PATTERN = Pattern.compile("^[0-9]+$");

    // Liste des catégories valides
    private static final List<String> VALID_CATEGORIES = List.of(
            "Coiffure",
            "Réparation téléphone",
            "Menuiserie",
            "Couture",
            "Plomberie",
            "Électricité",
            "Mécanique",
            "Informatique",
            "Photographie",
            "Autre"
    );

    /**
     * Valide une annonce complète
     * @param ad L'annonce à valider
     * @return Map contenant les erreurs (vide si tout est valide)
     */
    public static Map<String, String> validateAd(Ad ad) {
        Map<String, String> errors = new HashMap<>();

        if (ad == null) {
            errors.put("global", "L'annonce ne peut pas être nulle");
            return errors;
        }

        // Validation du nom du service
        String serviceNameError = validateServiceName(ad.getServiceName());
        if (serviceNameError != null) {
            errors.put("serviceName", serviceNameError);
        }

        // Validation de la description
        String descriptionError = validateDescription(ad.getDescription());
        if (descriptionError != null) {
            errors.put("description", descriptionError);
        }

        // Validation du prix (optionnel)
        String priceError = validatePrice(ad.getPrice());
        if (priceError != null) {
            errors.put("price", priceError);
        }

        // Validation du quartier
        String locationError = validateLocation(ad.getLocation());
        if (locationError != null) {
            errors.put("location", locationError);
        }

        // Validation du téléphone
        String phoneError = validatePhone(ad.getPhone());
        if (phoneError != null) {
            errors.put("phone", phoneError);
        }

        // Validation de la catégorie
        String categoryError = validateCategory(ad.getCategory());
        if (categoryError != null) {
            errors.put("category", categoryError);
        }

        // Validation du nom de l'artisan (optionnel)
        String artisanNameError = validateArtisanName(ad.getArtisanName());
        if (artisanNameError != null) {
            errors.put("artisanName", artisanNameError);
        }

        return errors;
    }

    /**
     * Valide le nom du service
     */
    public static String validateServiceName(String serviceName) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            return "Le nom du service est obligatoire";
        }

        String trimmed = serviceName.trim();

        if (trimmed.length() < 3) {
            return "Le nom du service doit contenir au moins 3 caractères";
        }

        if (trimmed.length() > MAX_SERVICE_NAME_LENGTH) {
            return "Le nom du service ne doit pas dépasser " + MAX_SERVICE_NAME_LENGTH + " caractères";
        }

        // Vérifier si le nom contient des caractères valides (lettres, chiffres, espaces, tirets)
        if (!trimmed.matches("^[a-zA-ZÀ-ÿ0-9\\s\\-\\']+$")) {
            return "Le nom du service contient des caractères non autorisés";
        }

        return null; // Valide
    }

    /**
     * Valide la description
     */
    public static String validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "La description est obligatoire";
        }

        String trimmed = description.trim();

        if (trimmed.length() < 10) {
            return "La description doit contenir au moins 10 caractères";
        }

        if (trimmed.length() > MAX_DESCRIPTION_LENGTH) {
            return "La description ne doit pas dépasser " + MAX_DESCRIPTION_LENGTH + " caractères";
        }

        return null; // Valide
    }

    /**
     * Valide le prix
     */
    public static String validatePrice(Integer price) {
        if (price == null) {
            return null; // Le prix est optionnel
        }

        if (price < MIN_PRICE) {
            return "Le prix ne peut pas être négatif";
        }

        if (price > MAX_PRICE) {
            return "Le prix est trop élevé (maximum: " + MAX_PRICE + ")";
        }

        return null; // Valide
    }

    /**
     * Valide le prix à partir d'une chaîne de caractères
     */
    public static String validatePriceString(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            return null; // Le prix est optionnel
        }

        String trimmed = priceStr.trim();

        if (!PRICE_PATTERN.matcher(trimmed).matches()) {
            return "Le prix doit être un nombre valide";
        }

        int price = Integer.parseInt(trimmed);
        return validatePrice(price);
    }

    /**
     * Valide le quartier/lieu
     */
    public static String validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return "Le quartier/lieu est obligatoire";
        }

        String trimmed = location.trim();

        if (trimmed.length() < 2) {
            return "Le quartier/lieu doit contenir au moins 2 caractères";
        }

        if (trimmed.length() > MAX_LOCATION_LENGTH) {
            return "Le quartier/lieu ne doit pas dépasser " + MAX_LOCATION_LENGTH + " caractères";
        }

        return null; // Valide
    }

    /**
     * Valide le numéro de téléphone
     */
    public static String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "Le numéro de téléphone est obligatoire";
        }

        String trimmed = phone.trim();

        // Supprimer les espaces et tirets pour la validation
        String cleanedPhone = trimmed.replaceAll("[\\s\\-]", "");

        if (!PHONE_PATTERN.matcher(cleanedPhone).matches()) {
            return "Numéro de téléphone invalide. Utilisez un format burundais (ex: 61234567 ou 61 23 45 67)";
        }

        if (trimmed.length() > MAX_PHONE_LENGTH) {
            return "Le numéro de téléphone est trop long";
        }

        return null; // Valide
    }

    /**
     * Formate le numéro de téléphone pour l'affichage
     */
    public static String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }

        String cleaned = phone.trim().replaceAll("[\\s\\-]", "");

        // Formater le numéro (ex: 61234567 -> 61 23 45 67)
        if (cleaned.length() == 8 && cleaned.matches("^[0-9]+$")) {
            return cleaned.substring(0, 2) + " " +
                    cleaned.substring(2, 4) + " " +
                    cleaned.substring(4, 6) + " " +
                    cleaned.substring(6, 8);
        }

        return phone; // Retourner tel quel si format non reconnu
    }

    /**
     * Valide la catégorie
     */
    public static String validateCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return "La catégorie est obligatoire";
        }

        String trimmed = category.trim();

        if (!VALID_CATEGORIES.contains(trimmed)) {
            return "Catégorie invalide. Choisissez parmi: " + String.join(", ", VALID_CATEGORIES);
        }

        return null; // Valide
    }

    /**
     * Valide le nom de l'artisan (optionnel)
     */
    public static String validateArtisanName(String artisanName) {
        if (artisanName == null || artisanName.trim().isEmpty()) {
            return null; // Optionnel
        }

        String trimmed = artisanName.trim();

        if (trimmed.length() < 2) {
            return "Le nom de l'artisan doit contenir au moins 2 caractères";
        }

        if (trimmed.length() > MAX_ARTISAN_NAME_LENGTH) {
            return "Le nom de l'artisan ne doit pas dépasser " + MAX_ARTISAN_NAME_LENGTH + " caractères";
        }

        // Vérifier les caractères valides
        if (!trimmed.matches("^[a-zA-ZÀ-ÿ\\s\\-\\']+$")) {
            return "Le nom de l'artisan contient des caractères non autorisés";
        }

        return null; // Valide
    }

    /**
     * Valide l'ID pour la suppression
     */
    public static String validateId(String idStr) {
        if (idStr == null || idStr.trim().isEmpty()) {
            return "L'ID est obligatoire";
        }

        try {
            Long id = Long.parseLong(idStr.trim());
            if (id <= 0) {
                return "L'ID doit être un nombre positif";
            }
            return null; // Valide
        } catch (NumberFormatException e) {
            return "L'ID doit être un nombre valide";
        }
    }

    /**
     * Nettoie une entrée utilisateur (prévention XSS basique)
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Remplacer les caractères dangereux
        return input.trim()
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Nettoie une annonce complète
     */
    public static Ad sanitizeAd(Ad ad) {
        if (ad == null) {
            return null;
        }

        Ad sanitized = new Ad();
        sanitized.setId(ad.getId());
        sanitized.setServiceName(sanitizeInput(ad.getServiceName()));
        sanitized.setDescription(sanitizeInput(ad.getDescription()));
        sanitized.setPrice(ad.getPrice());
        sanitized.setLocation(sanitizeInput(ad.getLocation()));
        sanitized.setPhone(sanitizeInput(ad.getPhone()));
        sanitized.setCategory(sanitizeInput(ad.getCategory()));
        sanitized.setArtisanName(sanitizeInput(ad.getArtisanName()));
        sanitized.setCreatedAt(ad.getCreatedAt());

        return sanitized;
    }

    /**
     * Vérifie si une chaîne est vide ou null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Tronque une chaîne à une longueur maximale
     */
    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * Retourne la liste des catégories valides
     */
    public static List<String> getValidCategories() {
        return new ArrayList<>(VALID_CATEGORIES);
    }

    /**
     * Valide la recherche (mot-clé)
     */
    public static String validateSearchKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return null; // La recherche peut être vide
        }

        String trimmed = keyword.trim();

        if (trimmed.length() < 2) {
            return "Le mot-clé doit contenir au moins 2 caractères";
        }

        if (trimmed.length() > 50) {
            return "Le mot-clé est trop long (maximum 50 caractères)";
        }

        return null; // Valide
    }

    /**
     * Valide le mot de passe admin (simplifié pour la démo)
     */
    public static boolean validateAdminPassword(String password) {
        return password != null && password.equals("admin123");
    }

    /**
     * Formate le prix pour l'affichage
     */
    public static String formatPrice(Integer price) {
        if (price == null) {
            return "Prix non spécifié";
        }
        return String.format("%,d FBu", price).replace(",", ".");
    }

    /**
     * Échappe les caractères spéciaux pour JSON
     */
    public static String escapeJson(String str) {
        if (str == null) {
            return "";
        }

        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}