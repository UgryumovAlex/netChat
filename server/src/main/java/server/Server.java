package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    private List<IClientHandler> clients;
    private AuthService authService;

    private ExecutorService service;

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new DatabaseAuthService(); //SimpleAuthService(); //15.04.2021 - поменял авторизацию на БД
        service = Executors.newCachedThreadPool();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while(true){
                socket = server.accept();
                System.out.println(socket.getLocalSocketAddress());
                System.out.println("Client connect: "+ socket.getRemoteSocketAddress());

                //new ClientHandler(this, socket);
                /**
                 * Используем паттерн билдер.
                 * Необходимость использования данного паттерна тут сильно притянута за уши, но
                 * проект слишком простой и более подходящего кандидата не нашёл.
                 * Подключать lombok посчитал неспортивным решением :)
                 * */
                ClientHandlerPattern.builder()
                        .server(this)
                        .socket(socket)
                        .build();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            service.shutdown();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(IClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNickname(), msg);
        for (IClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    public void privateMsg(IClientHandler sender, String reciever, String msg) {
        String message = String.format("%s : %s", sender.getNickname(), msg);
        sender.sendMsg(message);

        if (sender.getNickname().equalsIgnoreCase(reciever)) { return; } //Сообщение самому себе, выходим

        for (IClientHandler c : clients) {
            if (c.getNickname().equalsIgnoreCase(reciever)) {
                c.sendMsg(message);
                return;
            }
        }
        sender.sendMsg("Пользователь " + reciever + " не подключился");
    }

    public void subscribe(IClientHandler clientHandler){
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(IClientHandler clientHandler){
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/userlist");
        for (IClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String msg = sb.toString();

        for (IClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public boolean isLoginAuthenticated(String login) {
        for (IClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }

        return false;
    }

    public ExecutorService getService() {
        return service;
    }
}
