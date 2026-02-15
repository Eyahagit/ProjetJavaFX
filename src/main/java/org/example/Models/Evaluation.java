package org.example.Models;

import java.time.LocalDate;

public class Evaluation {

    private int id;
    private User user;          // référence à l'utilisateur
    private Ressource ressource; // référence à la ressource
    private int note;           // 1 à 5
    private String commentaire;
    private LocalDate dateEvaluation;

    // Constructeurs
    public Evaluation() { }

    public Evaluation(User user, Ressource ressource, int note, String commentaire, LocalDate dateEvaluation) {
        this.user = user;
        this.ressource = ressource;
        this.note = note;
        this.commentaire = commentaire;
        this.dateEvaluation = dateEvaluation;
    }

    public Evaluation(int id, User user, Ressource ressource, int note, String commentaire, LocalDate dateEvaluation) {
        this(user, ressource, note, commentaire, dateEvaluation);
        this.id = id;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Ressource getRessource() { return ressource; }
    public void setRessource(Ressource ressource) { this.ressource = ressource; }
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    public LocalDate getDateEvaluation() { return dateEvaluation; }
    public void setDateEvaluation(LocalDate dateEvaluation) { this.dateEvaluation = dateEvaluation; }

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", user=" + user +
                ", ressource=" + ressource +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateEvaluation=" + dateEvaluation +
                "}\n";
    }
}
