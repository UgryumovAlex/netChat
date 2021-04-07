package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{

    private class UserData {
        String login;
        String password;
        String nickname;

        public UserData(String login, String password, String nickname) {
            this.login = login;
            this.password = password;
            this.nickname = nickname;
        }
    }

    private List<UserData> users;

    public SimpleAuthService() {
        users = new ArrayList<>();

        users.add(new UserData("lesha", "devastator", "Папа"));
        users.add(new UserData("natasha", "boss", "Мама"));
        users.add(new UserData("dima", "tractor", "Димон"));
    }

    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        for (UserData u : users) {
            if(u.login.equals(login) && u.password.equals(password)){
                return u.nickname;
            }
        }

        return null;
    }
}
