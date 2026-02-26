package org.example.Services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {

    // 🔹 Mets ton email Gmail ici
    private static final String FROM_EMAIL = "ehamdi414@gmail.com";

    // 🔹 Mets ici ton mot de passe d’application Gmail (16 caractères)
    private static final String APP_PASSWORD = "hlzuavvwwebzphdd";

    /** Destinataire par défaut pour les notifications */
    public static final String DEFAULT_TO_EMAIL = "Hamdi.Eya1@esprit.tn";

    public static void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, false);
    }

    /** @param html if true, body is sent as HTML */
    public static void sendEmail(String to, String subject, String body, boolean html) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            message.setSubject(subject);

            if (html) {
                message.setContent(body, "text/html; charset=UTF-8");
            } else {
                message.setText(body);
            }

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès à : " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi mail : " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Envoi email échoué : " + e.getMessage(), e);
        }
    }

    /** Builds HTML body for "new resource" notification. */
    public static String buildNewResourceEmailHtml(String title, String description, String type, String category, String author, String date) {
        if (title == null) title = "";
        if (description == null) description = "";
        if (type == null) type = "—";
        if (category == null) category = "—";
        if (author == null) author = "—";
        if (date == null) date = "—";
        String t = escapeHtml(title);
        String d = escapeHtml(description);
        String ty = escapeHtml(type);
        String cat = escapeHtml(category);
        String a = escapeHtml(author);
        String dt = escapeHtml(date);

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"></head><body style=\"margin:0;font-family:'Segoe UI',Tahoma,sans-serif;background:#f0f4f8;\">"
                + "<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:linear-gradient(135deg,#4A6FA5 0%,#5FB49C 100%);padding:40px 20px;\">"
                + "<tr><td align=\"center\">"
                + "<table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:600px;background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 10px 40px rgba(0,0,0,0.15);\">"
                + "<tr><td style=\"background:linear-gradient(135deg,#4A6FA5 0%,#5FB49C 100%);padding:28px 32px;text-align:center;\">"
                + "<h1 style=\"margin:0;color:#fff;font-size:26px;font-weight:700;\">GrowMind</h1>"
                + "<p style=\"margin:8px 0 0 0;color:rgba(255,255,255,0.9);font-size:14px;\">Cultivez votre bien-être mental</p>"
                + "<span style=\"display:inline-block;margin-top:16px;background:rgba(255,255,255,0.2);color:#fff;padding:6px 14px;border-radius:20px;font-size:13px;font-weight:600;\">✨ Nouvelle ressource</span>"
                + "</td></tr>"
                + "<tr><td style=\"padding:32px;\">"
                + "<h2 style=\"margin:0 0 20px 0;color:#2c3e50;font-size:20px;font-weight:700;\">📚 " + t + "</h2>"
                + "<div style=\"background:#f8fbfd;border-radius:12px;border:1px solid #e0eef5;padding:20px;\">"
                + "<p style=\"margin:0 0 12px 0;color:#5a6c7d;font-size:13px;line-height:1.6;\"><strong style=\"color:#4A6FA5;\">Description</strong><br/>" + d + "</p>"
                + "<p style=\"margin:12px 0 0 0;color:#7f8c8d;font-size:13px;\"><strong>Type</strong> " + ty + " &nbsp;|&nbsp; <strong>Catégorie</strong> " + cat + "</p>"
                + "<p style=\"margin:8px 0 0 0;color:#7f8c8d;font-size:13px;\"><strong>Auteur</strong> " + a + " &nbsp;|&nbsp; <strong>Date</strong> " + dt + "</p>"
                + "</div></td></tr>"
                + "<tr><td style=\"padding:0 32px 28px;text-align:center;\">"
                + "<p style=\"margin:0;color:#95a5a6;font-size:12px;\">Notification automatique GrowMind</p>"
                + "<p style=\"margin:6px 0 0 0;color:#bdc3c7;font-size:11px;\">© 2024 GrowMind — Santé mentale & développement personnel</p>"
                + "</td></tr></table></td></tr></table></body></html>";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("\n", "<br/>");
    }
}