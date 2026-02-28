package utils;

import java.util.HashMap;
import java.util.Map;

public class Translator {

    private static final Map<String, String> FR_TO_EN = new HashMap<>();
    private static final Map<String, String> EN_TO_FR = new HashMap<>();

    static {
        // ===== MENUS ET BOUTONS PRINCIPAUX =====
        FR_TO_EN.put("forum", "Forum");
        FR_TO_EN.put("accueil", "Home");
        FR_TO_EN.put("nouvelle discussion", "New discussion");
        FR_TO_EN.put("patients", "Patients");
        FR_TO_EN.put("médecins", "Doctors");
        FR_TO_EN.put("ressources", "Resources");
        FR_TO_EN.put("urgence", "Emergency");
        FR_TO_EN.put("changer rôle", "Switch role");

        // ===== ACTIONS =====
        FR_TO_EN.put("voir", "View");
        FR_TO_EN.put("modifier", "Edit");
        FR_TO_EN.put("supprimer", "Delete");
        FR_TO_EN.put("publier", "Publish");
        FR_TO_EN.put("mettre à jour", "Update");
        FR_TO_EN.put("annuler", "Cancel");
        FR_TO_EN.put("rechercher", "Search");
        FR_TO_EN.put("retour", "Back");
        FR_TO_EN.put("actualiser", "Refresh");

        // ===== FILTRES =====
        FR_TO_EN.put("toutes", "All");
        FR_TO_EN.put("tous", "All");
        FR_TO_EN.put("filtrer", "Filter");
        FR_TO_EN.put("rechercher une discussion", "Search a discussion");
        FR_TO_EN.put("populaires", "Popular");
        FR_TO_EN.put("récents", "Recent");

        // ===== THÉMATIQUES =====
        FR_TO_EN.put("thématiques populaires", "Popular Topics");
        FR_TO_EN.put("anxiété", "Anxiety");
        FR_TO_EN.put("stress", "Stress");
        FR_TO_EN.put("dépression", "Depression");
        FR_TO_EN.put("méditation", "Meditation");
        FR_TO_EN.put("sommeil", "Sleep");
        FR_TO_EN.put("estime de soi", "Self-esteem");
        FR_TO_EN.put("général", "General");

        // ===== RÔLES =====
        FR_TO_EN.put("patient", "Patient");
        FR_TO_EN.put("médecin", "Doctor");
        FR_TO_EN.put("thérapeute", "Therapist");

        // Inverser pour EN -> FR
        for (Map.Entry<String, String> entry : FR_TO_EN.entrySet()) {
            EN_TO_FR.put(entry.getValue(), entry.getKey());
        }
    }

    public static String translate(String text, String targetLang) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        Map<String, String> dict = targetLang.equalsIgnoreCase("en") ? FR_TO_EN : EN_TO_FR;
        String result = text;

        for (Map.Entry<String, String> entry : dict.entrySet()) {
            result = result.replaceAll("(?i)" + entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String getLanguageIcon(boolean isFrench) {
        return isFrench ? "🇫🇷" : "🇬🇧";
    }

    public static String getLanguageText(boolean isFrench) {
        return isFrench ? "Français" : "English";
    }
}