package server;

import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class UserLoginData {
    private String login;
    private String password;

    public UserLoginData(String login, String password) {
        this.login    = login;
        this.password = password;
    }

    public UserLoginData() {
    }
}
