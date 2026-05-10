package Models;

import java.time.LocalDateTime;

public class Avis {
    private int idAvis;
    private int idReservation;
    private int utilisateur_id;
    private int note;
    private String commentaire;
    private LocalDateTime dateAvis;
    private float sentiment_score;
    private float authenticity_score;
    private String review_category;
    private int is_verified;

    public Avis(){}

    public Avis(int idAvis, int idReservation, int utilisateur_id, int note, String commentaire, LocalDateTime dateAvis, float sentiment_score, float authenticity_score, String review_category, int is_verified) {

        this.idAvis = idAvis;
        this.idReservation = idReservation;
        this.utilisateur_id = utilisateur_id;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
        this.sentiment_score = sentiment_score;
        this.authenticity_score = authenticity_score;
        this.review_category = review_category;
        this.is_verified = is_verified;
    }

    public int getIdAvis() {
        return idAvis;
    }

    public void setIdAvis(int idAvis) {
        this.idAvis = idAvis;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public int getUtilisateur_id() {
        return utilisateur_id;
    }

    public void setUtilisateur_id(int utilisateur_id) {
        this.utilisateur_id = utilisateur_id;
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

    public LocalDateTime getDateAvis() {
        return dateAvis;
    }

    public void setDateAvis(LocalDateTime dateAvis) {
        this.dateAvis = dateAvis;
    }

    public float getSentiment_score() {
        return sentiment_score;
    }

    public void setSentiment_score(float sentiment_score) {
        this.sentiment_score = sentiment_score;
    }

    public float getAuthenticity_score() {
        return authenticity_score;
    }

    public void setAuthenticity_score(float authenticity_score) {
        this.authenticity_score = authenticity_score;
    }

    public String getReview_category() {
        return review_category;
    }

    public void setReview_category(String review_category) {
        this.review_category = review_category;
    }

    public int getIs_verified() {
        return is_verified;
    }

    public void setIs_verified(int is_verified) {
        this.is_verified = is_verified;
    }

    @Override
    public String toString() {
        return "Avis{" +
                "idAvis=" + idAvis +
                ", idReservation=" + idReservation +
                ", utilisateur_id=" + utilisateur_id +
                ", note=" + note +
                ", commentaire='" + commentaire + '\'' +
                ", dateAvis=" + dateAvis +
                ", sentiment_score=" + sentiment_score +
                ", authenticity_score=" + authenticity_score +
                ", review_category='" + review_category + '\'' +
                ", is_verified=" + is_verified +
                '}';
    }
}

