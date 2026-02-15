package Models;

public class Cabinet {

    private int idCabinet;
    private String nomcabinet;
    private String adresse;
    private String ville;
    private int telephone;
    private String email;
    private String description;
    private String status;

    // Constructeur vide
    public Cabinet() {
    }

    // Constructeur SANS id (pour insertion)
    public Cabinet(String nomcabinet,
                   String adresse,
                   String ville,
                   int telephone,
                   String email,
                   String description,
                   String status) {

        this.nomcabinet = nomcabinet;
        this.adresse = adresse;
        this.ville = ville;
        this.telephone = telephone;
        this.email = email;
        this.description = description;
        this.status = status;
    }

    // Getters & Setters

    public int getIdCabinet() {
        return idCabinet;
    }

    public void setIdCabinet(int idCabinet) {
        this.idCabinet = idCabinet;
    }

    public String getNomcabinet() {
        return nomcabinet;
    }

    public void setNomcabinet(String nomcabinet) {
        this.nomcabinet = nomcabinet;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public int getTelephone() {
        return telephone;
    }

    public void setTelephone(int telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return nomcabinet + " - " + ville;
    }
}
