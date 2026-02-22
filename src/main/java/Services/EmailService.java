package Services;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class.getName());
    private static EmailService instance;
    private final String username;
    private final String password;
    private final Properties props;

    private EmailService() {
        // Configuration Gmail (vous pouvez changer selon votre fournisseur)
        this.username = "benahmedhayder10@gmail.com"; // À remplacer
        this.password = "qoglwgceqograczh"; // À remplacer

        this.props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    }

    public static EmailService getInstance() {
        if (instance == null) {
            instance = new EmailService();
        }
        return instance;
    }

    public boolean sendResetCode(String toEmail, String code) {
        String subject = "🔐 Réinitialisation de mot de passe - GrowMind";
        String content = buildEmailContent(code);
        return sendEmail(toEmail, subject, content);
    }

    private boolean sendEmail(String to, String subject, String content) {
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            LOGGER.log(Level.INFO, "Email sent to {0}", to);
            return true;

        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email", e);
            return false;
        }
    }

    private String buildEmailContent(String code) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        margin: 0;
                        padding: 20px;
                    }
                    .container {
                        max-width: 500px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 20px;
                        padding: 40px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                    }
                    .header {
                        text-align: center;
                        margin-bottom: 30px;
                    }
                    .logo {
                        font-size: 32px;
                        font-weight: bold;
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        -webkit-background-clip: text;
                        -webkit-text-fill-color: transparent;
                        margin-bottom: 10px;
                    }
                    .subtitle {
                        color: #666;
                        font-size: 16px;
                    }
                    .code-container {
                        background: linear-gradient(135deg, #667eea, #764ba2);
                        border-radius: 15px;
                        padding: 30px;
                        text-align: center;
                        margin: 30px 0;
                    }
                    .code {
                        font-size: 48px;
                        font-weight: bold;
                        letter-spacing: 10px;
                        color: white;
                        font-family: 'Courier New', monospace;
                        background: rgba(255,255,255,0.2);
                        padding: 20px;
                        border-radius: 10px;
                        display: inline-block;
                    }
                    .info {
                        background: #f8f9fa;
                        border-left: 4px solid #667eea;
                        padding: 15px;
                        border-radius: 5px;
                        margin: 20px 0;
                        color: #555;
                    }
                    .warning {
                        background: #fff3cd;
                        color: #856404;
                        padding: 15px;
                        border-radius: 5px;
                        text-align: center;
                        font-size: 14px;
                        margin-top: 30px;
                    }
                    .footer {
                        text-align: center;
                        color: #999;
                        font-size: 12px;
                        margin-top: 30px;
                        padding-top: 20px;
                        border-top: 1px solid #eee;
                    }
                </style>
            </head>
            <body>
                <div class='container'>
                    <div class='header'>
                        <div class='logo'>✨ GrowMind</div>
                        <div class='subtitle'>Plateforme de bien-être mental</div>
                    </div>
                    
                    <h2 style='color: #333; text-align: center;'>🔐 Réinitialisation de mot de passe</h2>
                    
                    <p style='color: #555; font-size: 16px;'>Bonjour,</p>
                    
                    <p style='color: #555; font-size: 16px;'>
                        Vous avez demandé à réinitialiser votre mot de passe. 
                        Voici votre code de vérification :
                    </p>
                    
                    <div class='code-container'>
                        <div class='code'>%s</div>
                    </div>
                    
                    <div class='info'>
                        <strong>⏰ Valable 15 minutes</strong><br>
                        Ce code expirera dans 15 minutes pour des raisons de sécurité.
                    </div>
                    
                    <p style='color: #555;'>
                        <strong>Instructions :</strong><br>
                        1. Retournez à l'application<br>
                        2. Entrez ce code à 6 chiffres<br>
                        3. Créez votre nouveau mot de passe
                    </p>
                    
                    <div class='warning'>
                        ⚠️ Si vous n'avez pas demandé cette réinitialisation, 
                        ignorez cet email et votre mot de passe restera inchangé.
                    </div>
                    
                    <div class='footer'>
                        <p>© 2024 GrowMind. Tous droits réservés.</p>
                        <p>Cet email a été envoyé automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """, code);
    }
}