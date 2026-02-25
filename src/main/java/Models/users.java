package Models;

public class users {
    private int id;
    private String name;
    private String second_name;
    private int age;
    private String gender;
    private int phone_number;
    private String birth_date;
    private String email;
    private String password;
    private String role;
    private boolean isBlocked;

    public users() {}

    public users(int id, String name, String second_name, int age, String gender, int phone_number, String birth_date, String email, String password, String role) {
        this.id = id;
        this.name = name;
        this.second_name = second_name;
        this.age = age;
        this.gender = gender;
        this.phone_number = phone_number;
        this.birth_date = birth_date;
        this.email = email;
        this.password = password;
        this.role = role;
        this.isBlocked = false;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSecond_name() { return second_name; }
    public void setSecond_name(String second_name) { this.second_name = second_name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getPhone_number() { return phone_number; }
    public void setPhone_number(int phone_number) { this.phone_number = phone_number; }

    public String getBirth_date() { return birth_date; }
    public void setBirth_date(String birth_date) { this.birth_date = birth_date; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "users{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", second_name='" + second_name + '\'' +
                ", age=" + age +
                ", gender='" + gender + '\'' +
                ", phone_number=" + phone_number +
                ", birth_date='" + birth_date + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}