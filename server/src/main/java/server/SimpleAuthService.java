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
        users.add(new UserData("ded", "biathlon", "Дед"));

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

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (SimpleAuthService.UserData u : users) {
            if(u.login.equals(login) || u.nickname.equals(nickname)){
                return false;
            }
        }
        users.add(new SimpleAuthService.UserData(login, password, nickname));
        return true;
    }

    @Override
    public boolean setNewNickname(String newNickName, String oldNickName, UserLoginData userLoginData) {
        //оставим заглушку, не реализуем
        return false;
    }
}
