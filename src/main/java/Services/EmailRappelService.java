package Services;

import Models.RendezVous;
import Service.EmailService;
import utils.EmailConfig;

public class EmailRappelService {

    private final Service.EmailService emailService;

    public EmailRappelService() {
        this.emailService = new EmailService(
                EmailConfig.SMTP_HOST,
                EmailConfig.SMTP_PORT,
                EmailConfig.EMAIL_EXPEDITEUR,
                EmailConfig.MOT_DE_PASSE
        );
    }

    /**
     * Envoyer un rappel de rendez-vous par email
     */
    public boolean envoyerRappel(RendezVous rdv) {

        String email = rdv.getEmailPatient();
        if (email == null || email.isEmpty()) {
            System.err.println("❌ Pas d'email pour ce rendez-vous #" + rdv.getIdRdv());
            return false;
        }

        String sujet = EmailConfig.SUJET_RAPPEL;
        String contenu = genererContenuRappel(rdv);

        return emailService.envoyerEmail(email, sujet, contenu);
    }

    /**
     * Générer le contenu de l'email de rappel
     */
    private String genererContenuRappel(RendezVous rdv) {

        String prenom = getSafe(rdv.getPrenomPatient(), "Patient");
        String nom = getSafe(rdv.getNomPatient(), "");
        String nomPsycho = getSafe(rdv.getNomPsychologue(), "Psychologue");
        String prenomPsycho = getSafe(rdv.getPrenomPsychologue(), "");
        String cabinet = getSafe(rdv.getNomCabinet(), "Cabinet");
        String ville = getSafe(rdv.getVilleCabinet(), "");

        String dateFormatee = formatDate(rdv.getDateRdv());
        String heure = getSafe(rdv.getHeure(), "Heure non précisée");

        return String.format(
                "=== RAPPEL DE RENDEZ-VOUS - GrowMind ===\n\n" +
                        "Bonjour %s %s,\n\n" +
                        "Ceci est un rappel pour votre rendez-vous :\n\n" +
                        "📅 Date: %s\n" +
                        "⏰ Heure: %s\n" +
                        "👨‍⚕️ Psychologue: Dr. %s %s\n" +
                        "📍 Lieu: %s (%s)\n\n" +
                        "🌟 Nous vous rappelons l'importance de votre rendez-vous.\n" +
                        "En cas d'empêchement, merci de nous prévenir 24h à l'avance.\n\n" +
                        "---\n" +
                        "Cet email a été envoyé automatiquement par GrowMind.\n" +
                        "Merci de votre confiance !",
                prenom, nom,
                dateFormatee, heure,
                nomPsycho, prenomPsycho,
                cabinet, ville
        );
    }

    private String getSafe(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String formatDate(java.util.Date date) {
        try {
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
        } catch (Exception e) {
            return "Date inconnue";
        }
    }
}