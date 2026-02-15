package Models;

import java.util.Date;

public class Psychologue {
    // Champs existants
    private int idPsychologue;
    private String nom;
    private String prenom;
    private String specialite;
    private String diplome;
    private int experience;
    private double tarif;
    private String email;
    private String telephone;
    private int idCabinet;

    // NOUVEAUX CHAMPS pour les infos du cabinet (pas dans la BD)
    private String nomCabinet;
    private String villeCabinet;
    private String adresseCabinet;

    // Constructeurs existants
    public Psychologue() {}

    public Psychologue(String nom, String prenom, String specialite,
                       String diplome, int experience, double tarif,
                       String email, String telephone, int idCabinet) {
        this.nom = nom;
        this.prenom = prenom;
        this.specialite = specialite;
        this.diplome = diplome;
        this.experience = experience;
        this.tarif = tarif;
        this.email = email;
        this.telephone = telephone;
        this.idCabinet = idCabinet;
    }

    // GETTERS ET SETTERS existants
    public int getIdPsychologue() { return idPsychologue; }
    public void setIdPsychologue(int idPsychologue) { this.idPsychologue = idPsychologue; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getSpecialite() { return specialite; }
    public void setSpecialite(String specialite) { this.specialite = specialite; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public double getTarif() { return tarif; }
    public void setTarif(double tarif) { this.tarif = tarif; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public int getIdCabinet() { return idCabinet; }
    public void setIdCabinet(int idCabinet) { this.idCabinet = idCabinet; }

    // NOUVEAUX GETTERS ET SETTERS
    public String getNomCabinet() { return nomCabinet; }
    public void setNomCabinet(String nomCabinet) { this.nomCabinet = nomCabinet; }

    public String getVilleCabinet() { return villeCabinet; }
    public void setVilleCabinet(String villeCabinet) { this.villeCabinet = villeCabinet; }

    public String getAdresseCabinet() { return adresseCabinet; }
    public void setAdresseCabinet(String adresseCabinet) { this.adresseCabinet = adresseCabinet; }

    // toString amélioré
    @Override
    public String toString() {
        return nom + " " + prenom + " - " + specialite +
                (nomCabinet != null ? " (" + nomCabinet + ")" : "");
    }
}