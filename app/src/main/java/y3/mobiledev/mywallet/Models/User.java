package y3.mobiledev.mywallet.Models;

public class User {
    private int userId;
    private String email;
    private String name;
    private String password;

    public User(int userId, String email, String name, String password) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPassword() { return password; }

    public void setName(String name) { this.name = name; }
}