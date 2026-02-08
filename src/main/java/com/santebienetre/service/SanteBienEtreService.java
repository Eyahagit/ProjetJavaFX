package com.santebienetre.service;

import com.santebienetre.dao.SanteBienEtreDAO;
import com.santebienetre.model.SanteBienEtre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for Health & Wellness business logic.
 * - Validates data
 * - Generates personalized recommendations based on mood, stress, sleep
 * - Delegates persistence to DAO
 * 
 * @author Application Santé & Bien-être
 * @version 1.0
 */
public class SanteBienEtreService {

    private final SanteBienEtreDAO dao = new SanteBienEtreDAO();

    /**
     * Adds a new record with validation and auto-generated recommendations.
     * @param s The entity to add
     * @return The id of the created record, or -1 on failure
     */
    public int addSanteBienEtre(SanteBienEtre s) {
        if (!validate(s)) return -1;
        s.setRecommandations(generateRecommandations(s));
        return dao.addSanteBienEtre(s);
    }

    /**
     * Updates an existing record with validation and refreshed recommendations.
     * @param s The entity to update
     * @return true if update succeeded
     */
    public boolean updateSanteBienEtre(SanteBienEtre s) {
        if (!validate(s)) return false;
        s.setRecommandations(generateRecommandations(s));
        return dao.updateSanteBienEtre(s);
    }

    /**
     * Deletes a record by id.
     */
    public boolean deleteSanteBienEtre(int id) {
        return dao.deleteSanteBienEtre(id);
    }

    /**
     * Retrieves all records.
     */
    public List<SanteBienEtre> getAllSanteBienEtre() {
        return dao.getAllSanteBienEtre();
    }

    /**
     * Retrieves records for a specific user.
     */
    public List<SanteBienEtre> getSanteBienEtreByUser(int userId) {
        return dao.getSanteBienEtreByUser(userId);
    }

    /**
     * Retrieves records by user and date range.
     */
    public List<SanteBienEtre> getSanteBienEtreByDateRange(int userId, LocalDate start, LocalDate end) {
        return dao.getSanteBienEtreByDateRange(userId, start, end);
    }

    /**
     * Generates personalized recommendations based on health data.
     * Rules:
     * - Stress > 7 → yoga, breathing exercises, meditation
     * - Sleep < 5 → sleep hygiene tips
     * - Sleep 5-6 → moderate sleep tips
     * - Mood "Stressé" → relaxation activities
     * - Nutrition empty/poor → nutrition tips
     * - Low physical activity → walking, pilates
     */
    public String generateRecommandations(SanteBienEtre s) {
        List<String> recs = new ArrayList<>();

        // Stress level recommendations
        if (s.getNiveauStress() > 7) {
            recs.add("• Yoga et exercices de respiration (cohérence cardiaque)");
            recs.add("• Méditation 10-15 min par jour");
            recs.add("• Marche en nature pour réduire le cortisol");
        } else if (s.getNiveauStress() > 5) {
            recs.add("• Pilates ou stretching léger");
            recs.add("• Marche quotidienne 30 min");
        }

        // Sleep quality recommendations
        if (s.getQualiteSommeil() < 5) {
            recs.add("• Hygiène du sommeil : couché régulier, pas d'écrans 1h avant");
            recs.add("• Éviter caféine après 14h");
            recs.add("• Chambre fraîche et obscure");
        } else if (s.getQualiteSommeil() < 7) {
            recs.add("• Essayer de vous coucher 30 min plus tôt");
        }

        // Mood-based recommendations
        String humeur = s.getHumeur() != null ? s.getHumeur().toLowerCase() : "";
        if (humeur.contains("stressé") || humeur.contains("stress")) {
            recs.add("• Activités relaxantes : bain, lecture, musique douce");
        }
        if (humeur.contains("moyen") || humeur.contains("fatigué")) {
            recs.add("• Activité physique modérée pour booster l'énergie");
        }

        // Nutrition recommendations
        String nutrition = s.getNutrition() != null ? s.getNutrition().toLowerCase() : "";
        if (nutrition.isEmpty() || nutrition.contains("mauvais") || nutrition.contains("irrégulier")) {
            recs.add("• Nutrition : repas équilibrés, fruits et légumes");
            recs.add("• Hydratation : 1.5 à 2 L d'eau par jour");
        }

        // Physical activity recommendations
        String activite = s.getActivitePhysique() != null ? s.getActivitePhysique().toLowerCase() : "";
        if (activite.isEmpty() || activite.contains("aucun") || activite.contains("peu")) {
            recs.add("• Commencer par 15 min de marche par jour");
        }

        // Personal development
        recs.add("• Développement personnel : journaling 5 min le soir");

        if (recs.isEmpty()) {
            return "Continuez comme ça ! Maintenez vos bonnes habitudes.";
        }
        return String.join("\n", recs);
    }

    /**
     * Validates the entity before persistence.
     */
    private boolean validate(SanteBienEtre s) {
        if (s == null) return false;
        if (s.getUserId() <= 0) return false;
        if (s.getHumeur() == null || s.getHumeur().trim().isEmpty()) return false;
        if (s.getNiveauStress() < 1 || s.getNiveauStress() > 10) return false;
        if (s.getQualiteSommeil() < 1 || s.getQualiteSommeil() > 10) return false;
        if (s.getDateSuivi() == null) s.setDateSuivi(LocalDate.now());
        return true;
    }
}
