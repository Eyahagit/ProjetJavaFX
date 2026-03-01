package Models;

import java.time.LocalDate;

public class SanteBienEtre {
    private int id;
    private int userId;
    private String humeur;
    private int niveauStress;
    private int qualiteSommeil;
    private String nutrition;
    private String activitePhysique;
    private String developpementPersonnel;
    private LocalDate dateSuivi;
    private String recommandations;

    // Constructeur par défaut
    public SanteBienEtre() {}

    public SanteBienEtre(int id, int userId, String humeur, int qualiteSommeil, int niveauStress, String nutrition, String activitePhysique, String developpementPersonnel, LocalDate dateSuivi, String recommandations) {
        this.id = id;
        this.userId = userId;
        this.humeur = humeur;
        this.qualiteSommeil = qualiteSommeil;
        this.niveauStress = niveauStress;
        this.nutrition = nutrition;
        this.activitePhysique = activitePhysique;
        this.developpementPersonnel = developpementPersonnel;
        this.dateSuivi = dateSuivi;
        this.recommandations = recommandations;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getHumeur() { return humeur; }
    public void setHumeur(String humeur) { this.humeur = humeur; }

    public int getNiveauStress() { return niveauStress; }
    public void setNiveauStress(int niveauStress) { this.niveauStress = niveauStress; }

    public int getQualiteSommeil() { return qualiteSommeil; }
    public void setQualiteSommeil(int qualiteSommeil) { this.qualiteSommeil = qualiteSommeil; }

    public String getNutrition() { return nutrition; }
    public void setNutrition(String nutrition) { this.nutrition = nutrition; }

    public String getActivitePhysique() { return activitePhysique; }
    public void setActivitePhysique(String activitePhysique) { this.activitePhysique = activitePhysique; }

    public String getDeveloppementPersonnel() { return developpementPersonnel; }
    public void setDeveloppementPersonnel(String developpementPersonnel) { this.developpementPersonnel = developpementPersonnel; }

    public LocalDate getDateSuivi() { return dateSuivi; }
    public void setDateSuivi(LocalDate dateSuivi) { this.dateSuivi = dateSuivi; }

    public String getRecommandations() { return recommandations; }
    public void setRecommandations(String recommandations) { this.recommandations = recommandations; }

    @Override
    public String toString() {
        return "SanteBienEtre{" +
                "id=" + id +
                ", userId=" + userId +
                ", humeur='" + humeur + '\'' +
                ", niveauStress=" + niveauStress +
                ", qualiteSommeil=" + qualiteSommeil +
                ", dateSuivi=" + dateSuivi +
                '}';
    }
}