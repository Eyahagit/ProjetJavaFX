package Service;

import Models.RendezVous;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SmartReminderService {

    // ========== IDENTIFIANTS TWILIO ==========
    // Remplace par TES identifiants
    private static final String ACCOUNT_SID = "AC24cd8001e602babe774bcd055e8af0f1";
    private static final String AUTH_TOKEN = "14c34701bc77233cbeb8e15a80504d9c";
    private static final String TWILIO_PHONE = "+18382063587"; // Ton numéro Twilio

    // ========== SERVICES ==========
    private ServiceRendezVous serviceRdv;
    private ScheduledExecutorService scheduler;

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
        List<RendezVous> historique = serviceRdv.getByTelephone(telephone);

        Map<String, Object> result = new HashMap<>();
        result.put("total", historique.size());

        // Compter les présences/absences
        long presents = historique.stream()
                .filter(r -> "confirmé".equals(r.getStatut()) || "terminé".equals(r.getStatut()))
                .count();

        long absents = historique.stream()
                .filter(r -> "annulé".equals(r.getStatut()))
                .count();

        result.put("presents", presents);
        result.put("absents", absents);

        // Taux de fiabilité
        double tauxFiabilite = historique.size() > 0 ?
                (double) presents / historique.size() : 0.5;
        result.put("tauxFiabilite", tauxFiabilite);

        // Analyser les heures préférées
        Map<Integer, Integer> heuresPreferees = new HashMap<>();
        for (RendezVous r : historique) {
            if ("confirmé".equals(r.getStatut()) || "terminé".equals(r.getStatut())) {
                int heure = Integer.parseInt(r.getHeure().split(":")[0]);
                heuresPreferees.put(heure, heuresPreferees.getOrDefault(heure, 0) + 1);
            }
        }
        result.put("heuresPreferees", heuresPreferees);

        return result;
    }

    /**
     * 2. PRÉDIRE LA MEILLEURE HEURE D'ENVOI
     */
    public int predictBestHour(String telephone) {
        Map<String, Object> analyse = analyserPatient(telephone);
        Map<Integer, Integer> heuresPreferees = (Map<Integer, Integer>) analyse.get("heuresPreferees");

        if (heuresPreferees.isEmpty()) {
            return 12; // Heure par défaut
        }

        // Trouver l'heure avec le plus de présences
        return heuresPreferees.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(12);
    }

    /**
     * 3. GÉNÉRER UN MESSAGE PERSONNALISÉ
     */
    public String generateMessage(RendezVous rdv) {
        Map<String, Object> analyse = analyserPatient(rdv.getTelephonePatient());
        double tauxFiabilite = (double) analyse.get("tauxFiabilite");
        long total = (long) analyse.get("total");
        long absents = (long) analyse.get("absents");

        String baseMsg = String.format(
                "🔔 RAPPEL INTELLIGENT - GrowMind\n\n" +
                        "Bonjour %s %s,\n\n" +
                        "Rendez-vous avec Dr. %s %s\n" +
                        "📅 Date: %s\n" +
                        "⏰ Heure: %s\n" +
                        "📍 Lieu: %s\n\n",
                rdv.getPrenomPatient(),
                rdv.getNomPatient(),
                rdv.getNomPsychologue(),
                rdv.getPrenomPsychologue(),
                new java.text.SimpleDateFormat("dd/MM/yyyy").format(rdv.getDateRdv()),
                rdv.getHeure(),
                rdv.getNomCabinet() != null ? rdv.getNomCabinet() : "Cabinet"
        );

        // Personnalisation intelligente
        if (total == 0) {
            return baseMsg + "🌟 Premier rendez-vous ? Arrivez 10 minutes avant !";
        } else if (tauxFiabilite > 0.8) {
            return baseMsg + "✨ Merci pour votre fidélité ! À très bientôt.";
        } else if (tauxFiabilite < 0.5) {
            return baseMsg + String.format(
                    "⚠️ Vous avez annulé %d rendez-vous récemment.\n" +
                            "Merci de confirmer votre présence 24h avant.",
                    absents
            );
        } else {
            return baseMsg + "📞 Pour tout changement, contactez-nous.";
        }
    }

    /**
     * 4. CALCULER LA DATE D'ENVOI OPTIMALE
     */
    public LocalDateTime calculateSendTime(RendezVous rdv, int bestHour) {
        LocalDateTime rdvTime = rdv.getDateRdv().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Envoyer 24h avant à l'heure optimale
        LocalDateTime sendTime = rdvTime.minusDays(1)
                .withHour(bestHour)
                .withMinute(0)
                .withSecond(0);

        // Si déjà passé, envoyer dans 5 minutes
        if (sendTime.isBefore(LocalDateTime.now())) {
            sendTime = LocalDateTime.now().plusMinutes(5);
        }

        return sendTime;
    }

    /**
     * 5. ENVOYER LE SMS VIA TWILIO
     */
    public void sendSMS(String toPhone, String message) {
        try {
            // Formater le numéro
            if (!toPhone.startsWith("+")) {
                toPhone = "+216" + toPhone; // Indicatif Tunisie
            }

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
        // Vérifier si le téléphone existe
        if (rdv.getTelephonePatient() == null || rdv.getTelephonePatient().isEmpty()) {
            System.err.println("❌ Pas de téléphone pour ce rendez-vous");
            return;
        }

        // Prédire la meilleure heure
        int bestHour = predictBestHour(rdv.getTelephonePatient());

        // Générer le message personnalisé
        String message = generateMessage(rdv);

        // Calculer la date d'envoi
        LocalDateTime sendTime = calculateSendTime(rdv, bestHour);

        // Calculer le délai en secondes
        long delay = ChronoUnit.SECONDS.between(LocalDateTime.now(), sendTime);

        if (delay < 0) {
            sendSMS(rdv.getTelephonePatient(), message);
            serviceRdv.marquerRappelEnvoye(rdv.getIdRdv(), new Date());
        } else {
            scheduler.schedule(() -> {
                sendSMS(rdv.getTelephonePatient(), message);
                serviceRdv.marquerRappelEnvoye(rdv.getIdRdv(), new Date());
            }, delay, TimeUnit.SECONDS);

            System.out.println("📅 Rappel programmé pour " + sendTime);
        }

        // Afficher les infos
        System.out.println("\n📊 ANALYSE INTELLIGENTE");
        System.out.println("Patient: " + rdv.getNomCompletPatient());
        System.out.println("Téléphone: " + rdv.getTelephonePatient());
        System.out.println("Rendez-vous: " + rdv.getDateRdv() + " à " + rdv.getHeure());
        System.out.println("Heure optimale prédite: " + bestHour + "h");
        System.out.println("Envoi programmé: " + sendTime);
        System.out.println("Type message: " + getMessageType(rdv.getTelephonePatient()));
        System.out.println("---\n");
    }

    /**
     * 7. DÉTERMINER LE TYPE DE MESSAGE
     */
    private String getMessageType(String telephone) {
        Map<String, Object> analyse = analyserPatient(telephone);
        double tauxFiabilite = (double) analyse.get("tauxFiabilite");
        long total = (long) analyse.get("total");

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
        stats.put("total", all.size());

        long rappelsEnvoyes = all.stream().filter(RendezVous::isRappelEnvoye).count();
        stats.put("rappelsEnvoyes", rappelsEnvoyes);

        // Compter par statut
        stats.put("confirmes", all.stream().filter(r -> "confirmé".equals(r.getStatut())).count());
        stats.put("annules", all.stream().filter(r -> "annulé".equals(r.getStatut())).count());
        stats.put("enAttente", all.stream().filter(r -> "en attente".equals(r.getStatut())).count());
        stats.put("termines", all.stream().filter(r -> "terminé".equals(r.getStatut())).count());

        return stats;
    }

    /**
     * 9. ARRÊTER LE PLANIFICATEUR
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