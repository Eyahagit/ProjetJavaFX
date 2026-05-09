package Models;

public class doctor extends users {
    private int id_user;
    private String specialty;
    private int experience;
    private String diplome;
    private boolean disponible;
    private double tarifConsultation;
    private boolean actif;

    public doctor() {
        super();
    }

    public doctor(int id, String name, String second_name, int age, String gender,
                  int phone_number, String birth_date, String email, String password,
                  String role, String dtype,String googleAuthenticatorSecret,int id_user, String specialty, int experience,
                  String diplome, boolean disponible, double tarifConsultation, boolean actif) {
        super(id, name, second_name, age, gender, phone_number, birth_date, email, password, role,dtype,googleAuthenticatorSecret);
        this.id_user = id_user;
        this.specialty = specialty;
        this.experience = experience;
        this.diplome = diplome;
        this.disponible = disponible;
        this.tarifConsultation = tarifConsultation;
        this.actif = actif;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public String getDiplome() { return diplome; }
    public void setDiplome(String diplome) { this.diplome = diplome; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public double getTarifConsultation() { return tarifConsultation; }
    public void setTarifConsultation(double tarifConsultation) { this.tarifConsultation = tarifConsultation; }

    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }

    @Override
    public String toString() {
        return super.toString() + " doctor{" +
                "id_doctor=" + id_user +
                ", specialty='" + specialty + '\'' +
                ", experience=" + experience +
                ", diplome='" + diplome + '\'' +
                ", disponible=" + disponible +
                ", tarifConsultation=" + tarifConsultation +
                ", actif=" + actif +
                '}';
    }
}