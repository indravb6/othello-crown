package othellocrown.account.model;

public class LoginData {
    private String username;
    private String password;

    public LoginData() {

    }

    public LoginData(String email, String password) {
        this.username = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
