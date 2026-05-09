package Models;

public class patient extends users {
    //private int id_user;
    private String blood_type;
    private double weight;
    private double height;

    public patient() {
        super();
    }

    public patient(int id, String name, String second_name, int age, String gender,
                   int phone_number, String birth_date, String email, String password,
                   String role,String dtype , String googleAuthenticatorSecret,  String blood_type, double weight, double height) {
        super(id, name, second_name, age, gender, phone_number, birth_date, email, password, role,dtype,googleAuthenticatorSecret);
        this.blood_type = blood_type;
        this.weight = weight;
        this.height = height;
    }



    public String getBlood_type() { return blood_type; }
    public void setBlood_type(String blood_type) { this.blood_type = blood_type; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    @Override
    public String toString() {
        return super.toString() + " patient{" +
                ", blood_type='" + blood_type + '\'' +
                ", weight=" + weight +
                ", height=" + height +
                '}';
    }
}