package Models;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RendezVous {

    private int idRdv;
    private Date dateRdv;
    private String heure;
    private String statut;
    private String typeCons;
    private int idPsychologue;

    // Champs pour les jointures (infos du psychologue et cabinet)
    private String nomPsychologue;
    private String prenomPsychologue;
    private String specialitePsychologue;
    private String nomCabinet;
    private String villeCabinet;

    // Informations patient (AJOUTÉ)
    private String nomPatient;
    private String prenomPatient;
    private String telephonePatient;

    // Suivi des rappels (AJOUTÉ)
    private boolean rappelEnvoye;
    private Date dateRappel;

    private String emailPatient;


    // Constructeur vide
    public RendezVous() {
    }

    // Constructeur SANS id (pour INSERT)
    public RendezVous(Date dateRdv, String heure,
                      String statut, String typeCons,
                      int idPsychologue) {
        this.dateRdv = dateRdv;
        this.heure = heure;
        this.statut = statut;
        this.typeCons = typeCons;
        this.idPsychologue = idPsychologue;
        this.rappelEnvoye = false;
    }

    // Constructeur AVEC id (pour SELECT)
    public RendezVous(int idRdv, Date dateRdv,
                      String heure, String statut,
                      String typeCons, int idPsychologue) {
        this.idRdv = idRdv;
        this.dateRdv = dateRdv;
        this.heure = heure;
        this.statut = statut;
        this.typeCons = typeCons;
        this.idPsychologue = idPsychologue;
        this.rappelEnvoye = false;
    }

    // ================= GETTERS & SETTERS =================

    public int getIdRdv() {
        return idRdv;
    }

    public void setIdRdv(int idRdv) {
        this.idRdv = idRdv;
    }

    public Date getDateRdv() {
        return dateRdv;
    }

    public void setDateRdv(Date dateRdv) {
        this.dateRdv = dateRdv;
    }

    public String getHeure() {
        return heure;
    }

    public void setHeure(String heure) {
        this.heure = heure;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getTypeCons() {
        return typeCons;
    }

    public void setTypeCons(String typeCons) {
        this.typeCons = typeCons;
    }

    public int getIdPsychologue() {
        return idPsychologue;
    }

    public void setIdPsychologue(int idPsychologue) {
        this.idPsychologue = idPsychologue;
    }

    // ========== GETTERS & SETTERS PATIENT (NOUVEAU) ==========
    public String getNomPatient() { return nomPatient; }
    public void setNomPatient(String nomPatient) { this.nomPatient = nomPatient; }

    public String getPrenomPatient() { return prenomPatient; }
    public void setPrenomPatient(String prenomPatient) { this.prenomPatient = prenomPatient; }

    public String getTelephonePatient() { return telephonePatient; }
    public void setTelephonePatient(String telephonePatient) {
        this.telephonePatient = telephonePatient;
    }

    // ========== GETTERS & SETTERS RAPPELS (NOUVEAU) ==========
    public boolean isRappelEnvoye() { return rappelEnvoye; }
    public void setRappelEnvoye(boolean rappelEnvoye) { this.rappelEnvoye = rappelEnvoye; }

    public Date getDateRappel() { return dateRappel; }
    public void setDateRappel(Date dateRappel) { this.dateRappel = dateRappel; }


    // ========== GETTERS & SETTERS POUR LES JOINTS ==========

    public String getNomPsychologue() {
        return nomPsychologue;
    }

    public void setNomPsychologue(String nomPsychologue) {
        this.nomPsychologue = nomPsychologue;
    }

    public String getPrenomPsychologue() {
        return prenomPsychologue;
    }

    public void setPrenomPsychologue(String prenomPsychologue) {
        this.prenomPsychologue = prenomPsychologue;
    }

    public String getSpecialitePsychologue() {
        return specialitePsychologue;
    }

    public void setSpecialitePsychologue(String specialitePsychologue) {
        this.specialitePsychologue = specialitePsychologue;
    }

    public String getNomCabinet() {
        return nomCabinet;
    }

    public void setNomCabinet(String nomCabinet) {
        this.nomCabinet = nomCabinet;
    }

    public String getVilleCabinet() {
        return villeCabinet;
    }

    public void setVilleCabinet(String villeCabinet) {
        this.villeCabinet = villeCabinet;
    }


    public String getEmailPatient() { return emailPatient; }
    public void setEmailPatient(String emailPatient) { this.emailPatient = emailPatient; }

    // ========== MÉTHODES UTILITAIRES ==========
    public String getNomCompletPatient() {
        if (nomPatient == null && prenomPatient == null) return "Non renseigné";
        return (prenomPatient != null ? prenomPatient : "") + " " +
                (nomPatient != null ? nomPatient : "");
    }

    // ================= TOSTRING AMÉLIORÉ =================
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = (dateRdv != null) ? sdf.format(dateRdv) : "Date inconnue";

        StringBuilder sb = new StringBuilder();
        sb.append("📅 ").append(dateStr).append(" ").append(heure);
        sb.append(" | ").append(statut);
        sb.append(" | ").append(typeCons);

        if (nomPsychologue != null && prenomPsychologue != null) {
            sb.append(" | Dr. ").append(nomPsychologue).append(" ").append(prenomPsychologue);
        }

        if (nomCabinet != null) {
            sb.append(" (").append(nomCabinet).append(")");
        }

        return sb.toString();
    }

}