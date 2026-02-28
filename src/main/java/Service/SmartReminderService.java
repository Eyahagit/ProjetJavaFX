package Service;

import Models.RendezVous;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// ========== IMPORTS POUR EMAIL ==========
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import utils.EmailConfig;

public class SmartReminderService {

    // ========== IDENTIFIANTS TWILIO ==========
    private static final String ACCOUNT_SID = "AC24cd8001e602babe774bcd055e8af0f1";
    private static final String AUTH_TOKEN = "4fe3f9652f782e153c794c34f0c40185";
    private static final String TWILIO_PHONE = "+1 838 206 3587";

    // ========== TON NUMÉRO POUR LES TESTS ==========
    // private static final String ALERT_PHONE_NUMBER = "+21624269512"; // Commenté si non utilisé

    // ========== SERVICES ==========
    private final ServiceRendezVous serviceRdv;
    private final ScheduledExecutorService scheduler;

    public SmartReminderService() {
        // Initialiser Twilio
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        // Initialiser les services
        this.serviceRdv = new ServiceRendezVous();
        this.scheduler = Executors.newScheduledThreadPool(5);

        System.out.println("✅ Service de rappels intelligents initialisé");
    }

    /**
     * 1. ANALYSER L'HISTORIQUE D'UN PATIENT
     */
    private Map<String, Object> analyserPatient(String telephone) {
        Map<String, Object> result = new HashMap<>();

        // Valeurs par défaut si téléphone null
        if (telephone == null || telephone.isEmpty()) {
            result.put("total", 0L);
            result.put("presents", 0L);
            result.put("absents", 0L);
            result.put("tauxFiabilite", 0.5);
            result.put("heuresPreferees", new HashMap<Integer, Integer>());
            return result;
        }

        List<RendezVous> historique = serviceRdv.getByTelephone(telephone);

        result.put("total", (long) historique.size());

        long presents = historique.stream()
                .filter(r -> "confirmé".equals(r.getStatut()) || "terminé".equals(r.getStatut()))
                .count();

        long absents = historique.stream()
                .filter(r -> "annulé".equals(r.getStatut()))
                .count();

        result.put("presents", presents);
        result.put("absents", absents);

        double tauxFiabilite = historique.size() > 0 ?
                (double) presents / historique.size() : 0.5;
        result.put("tauxFiabilite", tauxFiabilite);

        Map<Integer, Integer> heuresPreferees = new HashMap<>();
        for (RendezVous r : historique) {
            if (r.getHeure() != null &&
                    ("confirmé".equals(r.getStatut()) || "terminé".equals(r.getStatut()))) {
                try {
                    int heure = Integer.parseInt(r.getHeure().split(":")[0]);
                    heuresPreferees.put(heure, heuresPreferees.getOrDefault(heure, 0) + 1);
                } catch (Exception e) {
                    // Ignorer les heures invalides
                }
            }
        }
        result.put("heuresPreferees", heuresPreferees);

        return result;
    }

    /**
     * 2. PRÉDIRE LA MEILLEURE HEURE D'ENVOI
     */
    public int predictBestHour(String telephone) {
        if (telephone == null || telephone.isEmpty()) {
            return 12; // Heure par défaut
        }

        Map<String, Object> analyse = analyserPatient(telephone);

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> heuresPreferees = (Map<Integer, Integer>) analyse.get("heuresPreferees");

        if (heuresPreferees == null || heuresPreferees.isEmpty()) {
            return 12;
        }

        return heuresPreferees.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);
    }

    /**
     * 3. GÉNÉRER UN MESSAGE PERSONNALISÉ
     */
    public String generateMessage(RendezVous rdv) {
        // Analyse patient (avec fallback)
        Map<String, Object> analyse;
        try {
            analyse = analyserPatient(rdv.getTelephonePatient());
        } catch (Exception e) {
            analyse = new HashMap<>();
            analyse.put("tauxFiabilite", 0.5);
            analyse.put("total", 0L);
            analyse.put("absents", 0L);
        }

        double tauxFiabilite = (double) analyse.getOrDefault("tauxFiabilite", 0.5);
        long total = (long) analyse.getOrDefault("total", 0L);
        long absents = (long) analyse.getOrDefault("absents", 0L);

        // Message SANS émojis
        String baseMsg = String.format(
                "RAPPEL - GrowMind\n\nBonjour %s %s,\n\nRendez-vous avec Dr. %s %s\nDate: %s\nHeure: %s\nLieu: %s\n\n",
                getSafe(rdv.getPrenomPatient(), "Patient"),
                getSafe(rdv.getNomPatient(), ""),
                getSafe(rdv.getNomPsychologue(), "Psychologue"),
                getSafe(rdv.getPrenomPsychologue(), ""),
                formatDateSafe(rdv.getDateRdv()),
                getSafe(rdv.getHeure(), "Heure inconnue"),
                getSafe(rdv.getNomCabinet(), "Cabinet")
        );

        // Personnalisation sans émojis
        if (total == 0) return baseMsg + "Premier rendez-vous ? Arrivez 10 minutes avant !";
        if (tauxFiabilite > 0.8) return baseMsg + "Merci pour votre fidelite !";
        if (tauxFiabilite < 0.5) return baseMsg + String.format("Vous avez annule %d rendez-vous recemment.", absents);
        return baseMsg + "Pour tout changement, contactez-nous.";
    }

    private String getSafe(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String formatDateSafe(Date date) {
        try {
            return new java.text.SimpleDateFormat("dd/MM/yyyy").format(date);
        } catch (Exception e) {
            return "Date inconnue";
        }
    }

    /**
     * 4. CALCULER LA DATE D'ENVOI OPTIMALE
     */
    public LocalDateTime calculateSendTime(RendezVous rdv, int bestHour) {
        try {
            // Récupérer la date (gérer si c'est java.sql.Date ou java.util.Date)
            Date date = rdv.getDateRdv();
            LocalDate dateRdv;

            if (date instanceof java.sql.Date) {
                // Cas java.sql.Date
                dateRdv = ((java.sql.Date) date).toLocalDate();
            } else {
                // Cas java.util.Date
                dateRdv = date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            // Extraire l'heure (avec protection)
            String[] heureParts = rdv.getHeure().split(":");
            int heure = Integer.parseInt(heureParts[0]);
            int minute = Integer.parseInt(heureParts[1]);

            // Créer LocalDateTime
            LocalDateTime rdvTime = dateRdv.atTime(heure, minute);

            // Calculer la date d'envoi (24h avant à l'heure optimale)
            LocalDateTime sendTime = rdvTime.minusDays(1)
                    .withHour(bestHour)
                    .withMinute(0)
                    .withSecond(0);

            // Si déjà passé, envoyer dans 5 minutes
            if (sendTime.isBefore(LocalDateTime.now())) {
                sendTime = LocalDateTime.now().plusMinutes(5);
            }

            return sendTime;

        } catch (Exception e) {
            System.err.println("❌ Erreur calcul date: " + e.getMessage());
            // ENVOI IMMÉDIAT : retourne maintenant
            return LocalDateTime.now();
        }
    }

    /**
     * 5. ENVOYER LE SMS VIA TWILIO
     */
    public void sendSMS(String toPhone, String message) {
        try {
            // Formater le numéro
            String originalPhone = toPhone;
            if (!toPhone.startsWith("+")) {
                toPhone = "+216" + toPhone; // Indicatif Tunisie
            }

            System.out.println("📞 Envoi vers: " + toPhone);

            Message twilioMessage = Message.creator(
                    new PhoneNumber(toPhone),
                    new PhoneNumber(TWILIO_PHONE),
                    message
            ).create();

            System.out.println("✅ SMS envoyé! SID: " + twilioMessage.getSid());

        } catch (Exception e) {
            System.err.println("❌ Erreur Twilio: " + e.getMessage());
            // Mode test
            System.out.println("\n📱 SIMULATION SMS");
            System.out.println("À: " + toPhone);
            System.out.println("Message:\n" + message);
            System.out.println("---\n");
        }
    }

    /**
     * 6. PLANIFIER L'ENVOI INTELLIGENT
     */
    public void scheduleSmartReminder(RendezVous rdv) {
        // Vérifier si le rendez-vous est valide
        if (rdv == null) {
            System.err.println("❌ Rendez-vous null");
            return;
        }

        // Vérifier si le téléphone existe
        if (rdv.getTelephonePatient() == null || rdv.getTelephonePatient().trim().isEmpty()) {
            System.err.println("❌ Pas de téléphone pour ce rendez-vous #" + rdv.getIdRdv());
            return;
        }

        try {
            // Prédire la meilleure heure (optionnel, pour analyse)
            int bestHour = predictBestHour(rdv.getTelephonePatient());

            // Générer le message personnalisé
            String message = generateMessage(rdv);

            // === ENVOI IMMÉDIAT - PAS DE PLANIFICATION ===
            sendSMS(rdv.getTelephonePatient(), message);
            serviceRdv.marquerRappelEnvoye(rdv.getIdRdv(), new Date());

            // Afficher les infos
            System.out.println("\n📊 ANALYSE INTELLIGENTE");
            System.out.println("Patient: " + rdv.getNomCompletPatient());
            System.out.println("Téléphone: " + rdv.getTelephonePatient());
            System.out.println("Rendez-vous: " + rdv.getDateRdv() + " à " + rdv.getHeure());
            System.out.println("Heure optimale prédite: " + bestHour + "h");
            System.out.println("📱 ENVOI IMMÉDIAT !");
            System.out.println("Type message: " + getMessageType(rdv.getTelephonePatient()));
            System.out.println("---\n");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'envoi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 7. DÉTERMINER LE TYPE DE MESSAGE
     */
    private String getMessageType(String telephone) {
        if (telephone == null || telephone.isEmpty()) {
            return "PATIENT STANDARD";
        }

        Map<String, Object> analyse = analyserPatient(telephone);
        double tauxFiabilite = (double) analyse.getOrDefault("tauxFiabilite", 0.5);
        long total = (long) analyse.getOrDefault("total", 0L);

        if (total == 0) return "NOUVEAU PATIENT";
        if (tauxFiabilite > 0.8) return "PATIENT FIDÈLE";
        if (tauxFiabilite < 0.5) return "PATIENT ABSENTÉISTE";
        return "PATIENT STANDARD";
    }

    /**
     * 8. STATISTIQUES GLOBALES
     */
    public Map<String, Object> getGlobalStats() {
        List<RendezVous> all = serviceRdv.recuperer();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", (long) all.size());

        long rappelsEnvoyes = all.stream().filter(RendezVous::isRappelEnvoye).count();
        stats.put("rappelsEnvoyes", rappelsEnvoyes);

        // Compter par statut
        stats.put("confirmes", (long) all.stream().filter(r -> "confirmé".equals(r.getStatut())).count());
        stats.put("annules", (long) all.stream().filter(r -> "annulé".equals(r.getStatut())).count());
        stats.put("enAttente", (long) all.stream().filter(r -> "en attente".equals(r.getStatut())).count());
        stats.put("termines", (long) all.stream().filter(r -> "terminé".equals(r.getStatut())).count());

        return stats;
    }

    /**
     * 9. ENVOYER UN EMAIL (VERSION RÉELLE AVEC CONFIG)
     */
    public boolean sendEmail(String toEmail, String subject, String content) {
        System.out.println("📧 sendEmail() - Destinataire: " + toEmail);

        if (toEmail == null || toEmail.isEmpty()) {
            System.err.println("❌ Email destination manquant");
            return false;
        }

        try {
            String host = EmailConfig.SMTP_HOST;
            String port = EmailConfig.SMTP_PORT;
            String username = EmailConfig.EMAIL_EXPEDITEUR;
            String password = EmailConfig.MOT_DE_PASSE;

            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.ssl.trust", host);  // ← SOLUTION 1

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            System.out.println("✅ VRAI EMAIL envoyé à " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 10. ENVOYER UN RAPPEL PAR EMAIL
     */
    public void sendEmailReminder(RendezVous rdv) {
        System.out.println("📧 sendEmailReminder appelé pour: " + rdv.getIdRdv());

        if (rdv.getEmailPatient() == null || rdv.getEmailPatient().isEmpty()) {
            System.err.println("❌ Pas d'email pour ce rendez-vous #" + rdv.getIdRdv());
            return;
        }

        String sujet = "Rappel de rendez-vous - GrowMind";
        String contenu = String.format(
                "Bonjour %s %s,\n\n" +
                        "Ceci est un rappel pour votre rendez-vous :\n\n" +
                        "📅 Date: %s\n" +
                        "⏰ Heure: %s\n" +
                        "👨‍⚕️ Psychologue: Dr. %s %s\n" +
                        "📍 Lieu: %s\n\n" +
                        "Merci de votre confiance !\n" +
                        "L'équipe GrowMind",
                getSafe(rdv.getPrenomPatient(), "Patient"),
                getSafe(rdv.getNomPatient(), ""),
                formatDateSafe(rdv.getDateRdv()),
                getSafe(rdv.getHeure(), "Heure inconnue"),
                getSafe(rdv.getNomPsychologue(), "Psychologue"),
                getSafe(rdv.getPrenomPsychologue(), ""),
                getSafe(rdv.getNomCabinet(), "Cabinet")
        );

        System.out.println("📧 Envoi à: " + rdv.getEmailPatient());

        // Envoyer l'email
        boolean envoye = sendEmail(rdv.getEmailPatient(), sujet, contenu);

        // Marquer comme envoyé
        if (envoye) {
            serviceRdv.marquerRappelEnvoye(rdv.getIdRdv(), new Date());
            System.out.println("✅ Rappel EMAIL marqué comme envoyé pour rendez-vous #" + rdv.getIdRdv());
        }
    }

    /**
     * 11. ARRÊTER LE PLANIFICATEUR
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}