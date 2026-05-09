package Models;

public class admin extends users {
    private int id_user;
    private boolean actif;
    public admin(){}

    public admin(int id,String name, String second_name,int age,  String gender, int phone_number, String birth_date, String email, String password, String role,String dtype,String googleAuthenticatorSecret,int id_user ,boolean actif) {
        super(id,name,  second_name,age, gender, phone_number, birth_date, email, password, role,dtype,googleAuthenticatorSecret);
        this.id_user = id_user;
        this.actif = actif;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }

    public boolean isActif() {
        return actif;
    }
    public void setActif(boolean actif) {
        this.actif = actif;
    }

    @Override
    public String toString() {
        return "admin{" +
                "actif=" + actif +
                '}';
    }
}
