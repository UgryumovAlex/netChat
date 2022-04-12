package server;

import java.util.HashMap;
import java.util.Map;

public class DatabaseAuthServiceProxy implements AuthService{

    private final DatabaseAuthService databaseAuthService = new DatabaseAuthService();
    private final Map<UserLoginData, String> loginsStorage = new HashMap<>();


    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        UserLoginData userLoginData = new UserLoginData(login, password);

        if (loginsStorage.containsKey(userLoginData)) {
            return loginsStorage.get(userLoginData);
        } else {
            String nickName = databaseAuthService.getNicknameByLoginAndPassword(login, password);
            loginsStorage.put(userLoginData, nickName);
            return nickName;
        }
    }

    @Override
    public boolean registration(String login, String password, String nickname) {

        UserLoginData userLoginData = new UserLoginData(login, password);

        if (!loginsStorage.containsKey(userLoginData)) {
            return databaseAuthService.registration(login, password, nickname);
        }
        return false;
    }

    @Override
    public boolean setNewNickname(String newNickName, String oldNickName, UserLoginData userLoginData) {
        if (loginsStorage.containsKey(userLoginData)) {
            loginsStorage.put(userLoginData, newNickName);
            return true;
        } else {
            return databaseAuthService.setNewNickname(newNickName, oldNickName, userLoginData);
        }
    }
}
