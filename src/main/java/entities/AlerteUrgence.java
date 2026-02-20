package entities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlerteUrgence {
    private int id;
    private int userId;
    private String nom;
    private String message;
    private String localisation;
    private LocalDateTime dateAlerte;
    private String statut;

    public AlerteUrgence(int userId, String nom, String message, String localisation) {
        this.userId = userId;
        this.nom = nom;
        this.message = message;
        this.localisation = localisation;
        this.dateAlerte = LocalDateTime.now();
        this.statut = "ENVOYEE";
    }

    public AlerteUrgence(int id, int userId, String nom, String message,
                         String localisation, LocalDateTime dateAlerte, String statut) {
        this.id = id;
        this.userId = userId;
        this.nom = nom;
        this.message = message;
        this.localisation = localisation;
        this.dateAlerte = dateAlerte;
        this.statut = statut;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public LocalDateTime getDateAlerte() { return dateAlerte; }
    public void setDateAlerte(LocalDateTime dateAlerte) { this.dateAlerte = dateAlerte; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateAlerteFormatted() {
        return dateAlerte.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}