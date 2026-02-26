package Service;

import Models.RendezVous;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_EXPEDITEUR = "rahmabenssib0@gmail.com"; // Remplace par ton email
    private static final String MOT_DE_PASSE = "vhmi geyb vzfd wmci"; // Mot de passe d'application Gmail

    /**
     * Envoyer un email de confirmation de paiement
     */
    public boolean envoyerConfirmationPaiement(RendezVous rdv, String emailDestinataire, double montant) {

        // Configurer les propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Créer la session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_EXPEDITEUR, MOT_DE_PASSE);
            }
        });

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_EXPEDITEUR));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailDestinataire));
            message.setSubject("✅ Confirmation de paiement - GrowMind");

            // Corps de l'email
            String contenu = String.format(
                    "Bonjour %s %s,\n\n" +
                            "Votre paiement a été effectué avec succès !\n\n" +
                            "📅 Rendez-vous : %s à %s\n" +
                            "👤 Psychologue : Dr. %s %s\n" +
                            "💰 Montant payé : %.2f USD\n\n" +
                            "Merci de votre confiance !\n" +
                            "L'équipe GrowMind",
                    rdv.getPrenomPatient(),
                    rdv.getNomPatient(),
                    rdv.getDateRdv(),
                    rdv.getHeure(),
                    rdv.getNomPsychologue(),
                    rdv.getPrenomPsychologue(),
                    montant
            );

            message.setText(contenu);

            // Envoyer
            Transport.send(message);

            System.out.println("✅ Email de confirmation envoyé à " + emailDestinataire);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}