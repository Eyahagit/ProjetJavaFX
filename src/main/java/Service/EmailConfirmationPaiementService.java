package Service;

import Models.RendezVous;
import utils.EmailConfig;

public class EmailConfirmationPaiementService {

    private final EmailService emailService;

    public EmailConfirmationPaiementService() {
        this.emailService = new EmailService(
                EmailConfig.SMTP_HOST,
                EmailConfig.SMTP_PORT,
                EmailConfig.EMAIL_EXPEDITEUR,
                EmailConfig.MOT_DE_PASSE
        );
    }

    /**
     * Envoyer une confirmation de paiement par email
     */
    public boolean envoyerConfirmation(RendezVous rdv, double montant) {

        String email = rdv.getEmailPatient();
        if (email == null || email.isEmpty()) {
            System.err.println("❌ Pas d'email pour ce rendez-vous #" + rdv.getIdRdv());
            return false;
        }

        String sujet = EmailConfig.SUJET_CONFIRMATION_PAIEMENT;
        String contenu = genererContenuConfirmation(rdv, montant);

        return emailService.envoyerEmail(email, sujet, contenu);
    }

    /**
     * Générer le contenu de l'email de confirmation
     */
    private String genererContenuConfirmation(RendezVous rdv, double montant) {

        String prenom = getSafe(rdv.getPrenomPatient(), "Patient");
        String nom = getSafe(rdv.getNomPatient(), "");

        return String.format(
                "=== CONFIRMATION DE PAIEMENT - GrowMind ===\n\n" +
                        "Bonjour %s %s,\n\n" +
                        "Votre paiement a été accepté avec succès !\n\n" +
                        "💰 Montant: %.2f USD\n" +
                        "🆔 Référence: #%d\n\n" +
                        "Merci pour votre confiance.\n\n" +
                        "---\n" +
                        "Cet email a été envoyé automatiquement par GrowMind.",
                prenom, nom,
                montant,
                rdv.getIdRdv()
        );
    }

    private String getSafe(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }
}