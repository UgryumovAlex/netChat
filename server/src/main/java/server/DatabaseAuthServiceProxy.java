package server;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAuthServiceProxy implements AuthService{

    private final DatabaseAuthService databaseAuthService = new DatabaseAuthService();
    private final Map<String, String> loginsStorage = new HashMap<>();


    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        if (loginsStorage.containsKey(login)) {
            return loginsStorage.get(login);
        } else {
            String nickName = databaseAuthService.getNicknameByLoginAndPassword(login, password);
            loginsStorage.put(login, nickName);
            return nickName;
        }
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        if (!loginsStorage.containsKey(login)) {
            return databaseAuthService.registration(login, password, nickname);
        }
        return false;
    }

    @Override
    public boolean setNewNickname(String newNickName, String oldNickName, String login) {
        if (loginsStorage.containsKey(login)) {
            loginsStorage.put(login, newNickName);
            return true;
        } else {
            return databaseAuthService.setNewNickname(newNickName, oldNickName, login);
        }
    }
}
