package Models;

import java.time.LocalDateTime;

public class Avis {
    private int idAvis;
    private int idReservation;
    private int utilisateurId;
    private int note;
    private String commentaire;
    private LocalDateTime dateAvis;
    
    public Avis() {}
    
    public Avis(int idAvis, int idReservation, int utilisateurId, int note, String commentaire, LocalDateTime dateAvis) {
        this.idAvis = idAvis;
        this.idReservation = idReservation;
        this.utilisateurId = utilisateurId;
        this.note = note;
        this.commentaire = commentaire;
        this.dateAvis = dateAvis;
    }
    
    // Getters and Setters
    public int getIdAvis() { return idAvis; }
    public void setIdAvis(int idAvis) { this.idAvis = idAvis; }
    
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }
    
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    
    public int getNote() { return note; }
    public void setNote(int note) { this.note = note; }
    
    public String getCommentaire() { return commentaire; }
    public void setCommentaire(String commentaire) { this.commentaire = commentaire; }
    
    public LocalDateTime getDateAvis() { return dateAvis; }
    public void setDateAvis(LocalDateTime dateAvis) { this.dateAvis = dateAvis; }
}
