package Models;

import java.time.LocalDateTime;

public class Reservation {
    private int idReservation;
    private int idEvenement;
    private int utilisateurId;  // ✅ AJOUTEZ CETTE LIGNE
    private String nom;
    private String email;
    private String telephone;
    private int nombrePersonnes;
    private LocalDateTime dateReservation;

    // Constructeurs
    public Reservation() {}

    public Reservation(int idEvenement, int utilisateurId, String nom, String email,  // ✅ AJOUTEZ utilisateurId
                       String telephone, int nombrePersonnes) {
        this.idEvenement = idEvenement;
        this.utilisateurId = utilisateurId;  // ✅ AJOUTEZ CETTE LIGNE
        this.nom = nom;
        this.email = email;
        this.telephone = telephone;
        this.nombrePersonnes = nombrePersonnes;
        this.dateReservation = LocalDateTime.now();
    }

    // ✅ AJOUTEZ CES GETTER/SETTER
    public int getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(int utilisateurId) { this.utilisateurId = utilisateurId; }
    // Getters et Setters
    public int getIdReservation() { return idReservation; }
    public void setIdReservation(int idReservation) { this.idReservation = idReservation; }

    public int getIdEvenement() { return idEvenement; }
    public void setIdEvenement(int idEvenement) { this.idEvenement = idEvenement; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }
}