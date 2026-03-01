package Services;

import dao.SanteBienEtreDAO;
import Models.SanteBienEtre;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service layer for Health & Wellness business logic.
 * - Validates data
 * - Generates personalized recommendations based on mood, stress, sleep
 * - Delegates persistence to DAO
 *
 * @author Application Santé & Bien-être
 * @version 1.1
 */
public class SanteBienEtreService {

    private static final Logger LOGGER = Logger.getLogger(SanteBienEtreService.class.getName());
    private final SanteBienEtreDAO dao = new SanteBienEtreDAO();

    // Constantes pour les seuils
    private static final int STRESS_SEUIL_HAUT = 7;
    private static final int STRESS_SEUIL_MOYEN = 5;
    private static final int SOMMEIL_SEUIL_FAIBLE = 5;
    private static final int SOMMEIL_SEUIL_MOYEN = 7;
    private static final int NOTE_MIN = 1;
    private static final int NOTE_MAX = 10;

    /**
     * Adds a new record with validation and auto-generated recommendations.
     * @param s The entity to add
     * @return The id of the created record, or -1 on failure
     */
    public int addSanteBienEtre(SanteBienEtre s) {
        if (!validate(s)) {
            LOGGER.warning("Validation échouée pour l'ajout d'un enregistrement");
            return -1;
        }
        s.setRecommandations(generateRecommandations(s));
        int id = dao.addSanteBienEtre(s);
        if (id > 0) {
            LOGGER.info("Enregistrement ajouté avec succès, ID: " + id);
        }
        return id;
    }

    /**
     * Updates an existing record with validation and refreshed recommendations.
     * @param s The entity to update
     * @return true if update succeeded
     */
    public boolean updateSanteBienEtre(SanteBienEtre s) {
        if (!validate(s)) {
            LOGGER.warning("Validation échouée pour la modification ID: " + s.getId());
            return false;
        }
        s.setRecommandations(generateRecommandations(s));
        boolean success = dao.updateSanteBienEtre(s);
        if (success) {
            LOGGER.info("Enregistrement modifié avec succès, ID: " + s.getId());
        }
        return success;
    }

    /**
     * Deletes a record by id.
     */
    public boolean deleteSanteBienEtre(int id) {
        boolean success = dao.deleteSanteBienEtre(id);
        if (success) {
            LOGGER.info("Enregistrement supprimé avec succès, ID: " + id);
        }
        return success;
    }

    /**
     * Retrieves all distinct user IDs.
     * @return List of user IDs
     */
    public List<Integer> getAllUserIds() {
        List<Integer> userIds = dao.getAllUserIds();
        LOGGER.fine(userIds.size() + " user IDs récupérés");
        return userIds;
    }

    /**
     * Retrieves all records.
     */
    public List<SanteBienEtre> getAllSanteBienEtre() {
        List<SanteBienEtre> list = dao.getAllSanteBienEtre();
        LOGGER.fine(list.size() + " enregistrements récupérés");
        return list;
    }

    /**
     * Retrieves records for a specific user.
     */
    public List<SanteBienEtre> getSanteBienEtreByUser(int userId) {
        List<SanteBienEtre> list = dao.getSanteBienEtreByUser(userId);
        LOGGER.fine(list.size() + " enregistrements récupérés pour l'utilisateur " + userId);
        return list;
    }

    /**
     * Retrieves records by user and date range.
     */
    public List<SanteBienEtre> getSanteBienEtreByDateRange(int userId, LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            LOGGER.warning("Dates de filtre nulles");
            return new ArrayList<>();
        }
        if (start.isAfter(end)) {
            LOGGER.warning("Date de début postérieure à la date de fin");
            return new ArrayList<>();
        }
        List<SanteBienEtre> list = dao.getSanteBienEtreByDateRange(userId, start, end);
        LOGGER.fine(list.size() + " enregistrements trouvés pour l'utilisateur " + userId +
                " entre " + start + " et " + end);
        return list;
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
        if (s == null) {
            return "Données insuffisantes pour générer des recommandations.";
        }

        List<String> recs = new ArrayList<>();

        // Stress level recommendations
        int stress = s.getNiveauStress();
        if (stress > STRESS_SEUIL_HAUT) {
            recs.add("• Yoga et exercices de respiration (cohérence cardiaque)");
            recs.add("• Méditation 10-15 min par jour");
            recs.add("• Marche en nature pour réduire le cortisol");
        } else if (stress > STRESS_SEUIL_MOYEN) {
            recs.add("• Pilates ou stretching léger");
            recs.add("• Marche quotidienne 30 min");
        }

        // Sleep quality recommendations
        int sommeil = s.getQualiteSommeil();
        if (sommeil < SOMMEIL_SEUIL_FAIBLE) {
            recs.add("• Hygiène du sommeil : couché régulier, pas d'écrans 1h avant");
            recs.add("• Éviter caféine après 14h");
            recs.add("• Chambre fraîche et obscure");
        } else if (sommeil < SOMMEIL_SEUIL_MOYEN) {
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

        // Personal development (toujours ajouté)
        recs.add("• Développement personnel : journaling 5 min le soir");

        if (recs.isEmpty()) {
            return "Continuez comme ça ! Maintenez vos bonnes habitudes.";
        }

        return String.join("\n", recs);
    }

    /**
     * Validates the entity before persistence.
     * @param s The entity to validate
     * @return true if valid, false otherwise
     */
    private boolean validate(SanteBienEtre s) {
        if (s == null) {
            LOGGER.warning("Entité null");
            return false;
        }

        boolean valid = true;
        StringBuilder errors = new StringBuilder();

        if (s.getUserId() <= 0) {
            errors.append("- ID Utilisateur invalide: ").append(s.getUserId()).append("\n");
            valid = false;
        }

        if (s.getHumeur() == null || s.getHumeur().trim().isEmpty()) {
            errors.append("- Humeur manquante\n");
            valid = false;
        }

        int stress = s.getNiveauStress();
        if (stress < NOTE_MIN || stress > NOTE_MAX) {
            errors.append("- Niveau de stress doit être entre ").append(NOTE_MIN).append(" et ").append(NOTE_MAX).append("\n");
            valid = false;
        }

        int sommeil = s.getQualiteSommeil();
        if (sommeil < NOTE_MIN || sommeil > NOTE_MAX) {
            errors.append("- Qualité du sommeil doit être entre ").append(NOTE_MIN).append(" et ").append(NOTE_MAX).append("\n");
            valid = false;
        }

        if (s.getDateSuivi() == null) {
            s.setDateSuivi(LocalDate.now());
        }

        if (!valid) {
            LOGGER.warning("Erreurs de validation:\n" + errors.toString());
        }

        return valid;
    }
}