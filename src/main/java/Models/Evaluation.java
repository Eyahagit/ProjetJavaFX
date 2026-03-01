package Models;

import java.time.LocalDate;

public class Evaluation {

    private int id;
    private int userId; // référence à l'utilisateur (ID)
    private Ressource ressource; // référence à la ressource
    private int note; // 1 à 5
    private String commentaire;
    private LocalDate dateEvaluation;

    // Constructeurs
    public Evaluation() {
    }

    public Evaluation(int userId, Ressource ressource, int note, String commentaire, LocalDate dateEvaluation) {
        this.userId = userId;
        this.ressource = ressource;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public Evaluation(int id, int userId, Ressource ressource, int note, String commentaire, LocalDate dateEvaluation) {
        this(userId, ressource, note, commentaire, dateEvaluation);
        this.id = id;
    }

    // Getters & Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Ressource getRessource() {
        return ressource;
    }

    public void setRessource(Ressource ressource) {
        this.ressource = ressource;
    }

    public int getNote() {
        return note;
    }

    public void setNote(int note) {
        this.note = note;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public LocalDate getDateEvaluation() {
        return dateEvaluation;
    }

    public void setDateEvaluation(LocalDate dateEvaluation) {
        this.dateEvaluation = dateEvaluation;
    }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", userId=" + userId +
                ", ressource=" + ressource +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateEvaluation=" + dateEvaluation +
                "}\n";
    }
}
