package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandlerPattern  implements IClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private UserLoginData userLoginData;
    private String authLogin;

    public ClientHandlerPattern(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            userLoginData = new UserLoginData();

            server.getService().execute(
                    new Thread(() -> {
                        try {
                            socket.setSoTimeout(120000);
                            // цикл аутентификации
                            while (true) {
                                String str = in.readUTF();

                                if (str.equals("/end")) {
                                    out.writeUTF("/end");
                                    throw new RuntimeException("Клиент решил отключиться");
                                }

                                // Аутентификация
                                if (str.startsWith("/auth")) {
                                    String[] token = str.split("\\s+", 3);
                                    if (token.length < 3) {
                                        continue;
                                    }

                                    authLogin = token[1];

                                    String newNick = server
                                            .getAuthService()
                                            .getNicknameByLoginAndPassword(authLogin, token[2]);
                                    if (newNick != null) {

                                        if (!server.isLoginAuthenticated(authLogin)) {

                                            userLoginData.setLogin(authLogin);
                                            userLoginData.setPassword(token[2]);

                                            nickname = newNick;
                                            sendMsg("/auth_ok " + nickname + " " + userLoginData.getLogin());
                                            server.subscribe(this);
                                            System.out.println("Client authenticated. nick: " + nickname +
                                                    " Address: " + socket.getRemoteSocketAddress());

                                            socket.setSoTimeout(0);

                                            break;
                                        } else {
                                            sendMsg("С этим логином уже авторизовались");
                                        }
                                    } else {
                                        sendMsg("Неверный логин / пароль");
                                    }
                                }

                                // Регистрация
                                if (str.startsWith("/reg")) {
                                    String[] token = str.split("\\s+", 4);
                                    if (token.length < 4) {
                                        continue;
                                    }
                                    boolean b = server.getAuthService()
                                            .registration(token[1], token[2], token[3]);
                                    if (b) {
                                        sendMsg("/reg_ok");
                                    } else {
                                        sendMsg("/reg_no");
                                    }
                                }
                            }

                            //цикл работы
                            while (true) {
                                String str = in.readUTF();

                                if (str.equals("/end")) {
                                    out.writeUTF("/end");
                                    break;
                                }

                                //Смена Nickname
                                if (str.startsWith("/newNick")) {
                                    String[] token = str.split("\\s");
                                    if (nickname.equals(token[1])) {
                                        sendMsg("nick " + token[1] + " уже используется");
                                    } else {
                                        if (server.getAuthService().setNewNickname(token[1], nickname, userLoginData)) {
                                            nickname = token[1];
                                            sendMsg("/newNick_ok " + nickname + " Успешно, новый nick ");
                                            server.broadcastClientList(); //Обновим список клиентов
                                        } else {
                                            sendMsg("Ошибка, nick не был изменён");
                                        }
                                    }
                                    continue;
                                }

                                //Для обращения к конкретному пользователю используем формат :
                                // -> nickname сообщение (Стрелочка ник пробел сообщение)
                                if (str.startsWith("->")) {
                                    String[] token = str.split("\\s+", 2);
                                    server.privateMsg(this, token[0].replaceFirst("->", ""), token[1]);
                                } else {
                                    server.broadcastMsg(this, str);
                                }
                            }

                        } catch(SocketTimeoutException e) {
                            try {
                                out.writeUTF("/end"); //Клиент слишком долго молчал, отключаем его
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            server.unsubscribe(this);
                            System.out.println("client disconnect " + socket.getRemoteSocketAddress());
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return userLoginData.getLogin();
    }

    public static ClientHandlerPattern.ClientHandlerPatternBuilder builder() {
        return new ClientHandlerPattern.ClientHandlerPatternBuilder();
    }

    public static class ClientHandlerPatternBuilder {
        private Server server;
        private Socket socket;

        public ClientHandlerPatternBuilder() {};

        public ClientHandlerPattern.ClientHandlerPatternBuilder server(final Server server) {
             this.server = server;
             return this;
        }

        public ClientHandlerPattern.ClientHandlerPatternBuilder socket(final Socket socket) {
            this.socket = socket;
            return this;
        }

        public ClientHandlerPattern build() {
            return new ClientHandlerPattern(this.server, this.socket);
        }
    }

}
