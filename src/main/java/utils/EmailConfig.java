package utils;

public class EmailConfig {

    // ========== TES INFORMATIONS GMAIL ==========
    public static final String SMTP_HOST = "smtp.gmail.com";
    public static final String SMTP_PORT = "587";
    public static final String EMAIL_EXPEDITEUR = "rahmabenssib0@gmail.com"; // ← REMPLACE
    public static final String MOT_DE_PASSE = "vhmi geyb vzfd wmci"; // ← Mot de passe d'application
    public static final String NOM_APPLICATION = "GrowMind";

    // ========== OBJETS DES EMAILS ==========
    public static final String SUJET_RAPPEL = "🔔 Rappel de votre rendez-vous - GrowMind";
    public static final String SUJET_CONFIRMATION_PAIEMENT = "✅ Confirmation de paiement - GrowMind";

    public static boolean estConfigure() {
        return EMAIL_EXPEDITEUR != null && !EMAIL_EXPEDITEUR.isEmpty()
                && !EMAIL_EXPEDITEUR.equals("ton.email@gmail.com");
    }
}