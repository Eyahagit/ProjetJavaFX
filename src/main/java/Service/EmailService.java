package Service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private final String smtpHost;
    private final String smtpPort;
    private final String expediteur;
    private final String motDePasse;

    public EmailService(String smtpHost, String smtpPort, String expediteur, String motDePasse) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.expediteur = expediteur;
        this.motDePasse = motDePasse;
    }

    /**
     * Envoyer un email
     */
    public boolean envoyerEmail(String destinataire, String sujet, String contenu) {

        if (destinataire == null || destinataire.isEmpty()) {
            System.err.println("❌ Destinataire email manquant");
            return false;
        }

        // Configurer les propriétés SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.ssl.trust", "*"); // Ignorer les problèmes de certificat

        // Créer la session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(expediteur, motDePasse);
            }
        });

        try {
            // Créer le message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(expediteur));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinataire));
            message.setSubject(sujet);
            message.setText(contenu);

            // Envoyer
            Transport.send(message);

            System.out.println("✅ Email envoyé à " + destinataire);
            System.out.println("📧 Sujet: " + sujet);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();

            // Mode test - afficher dans la console
            System.out.println("\n📧 SIMULATION EMAIL");
            System.out.println("À: " + destinataire);
            System.out.println("Sujet: " + sujet);
            System.out.println("Contenu:\n" + contenu);
            System.out.println("---\n");

            return false;
        }
    }
}