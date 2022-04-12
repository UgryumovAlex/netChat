package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю.
     * Если учетки с таким логином и паролем нет то вернет
     * Если учетка есть то вернет никнейм.
     * @return никнейм если есть совпадение по логину и паролю, null если нет совпадения
     * */
    String getNicknameByLoginAndPassword(String login, String password );

    /**
     * Попытка регистрации новой учетной записи
     * */
    boolean registration(String login, String password, String nickname);

    /**
     * Метод для смены Nickname пользователя
     *
     * */
    public boolean setNewNickname(String newNickName, String oldNickName, UserLoginData userLoginData);
}
