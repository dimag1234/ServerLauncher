package auth;

public class Account {
    private final String email;
    private final String password;
    private final String gender;

    // Конструктор теперь package-private (доступен только для AccountManager)
    Account(String email, String password, String gender) {
        this.email = email;
        this.password = password;
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getGender() {
        return gender;
    }
}
