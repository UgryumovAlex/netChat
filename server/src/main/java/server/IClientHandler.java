package server;

public interface IClientHandler {
    void sendMsg(String msg);
    String getNickname();
    String getLogin();
}
