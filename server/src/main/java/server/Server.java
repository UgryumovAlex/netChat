package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Server {
    private static ServerSocket server;
    private static Socket socket;

    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;

    private ExecutorService service;

    private static final Logger logger = Logger.getLogger(server.Server.class.getName());

    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new DatabaseAuthService(); //SimpleAuthService(); //15.04.2021 - поменял авторизацию на БД
        service = Executors.newCachedThreadPool();

        LogManager.getLogManager().addLogger(logger);

        try {
            server = new ServerSocket(PORT);
            //System.out.println("Server started");
            logger.info("Server started");

            while(true){
                socket = server.accept();
                //System.out.println(socket.getLocalSocketAddress());
                //System.out.println("Client connect: "+ socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
            logger.warning("Server IOException " + e);

        } finally {
            service.shutdown();
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
                logger.info("Server closed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg(ClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNickname(), msg);
        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
        logger.fine(sender.getNickname() + " послал сообщение всем");
    }

    public void privateMsg(ClientHandler sender, String reciever, String msg) {
        String message = String.format("%s : %s", sender.getNickname(), msg);
        sender.sendMsg(message);

        if (sender.getNickname().equalsIgnoreCase(reciever)) { return; } //Сообщение самому себе, выходим

        for (ClientHandler c : clients) {
            if (c.getNickname().equalsIgnoreCase(reciever)) {
                c.sendMsg(message);
                logger.fine(sender.getNickname() + " послал сообщение " + c.getNickname());
                return;
            }
        }
        sender.sendMsg("Пользователь " + reciever + " не подключился");
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        logger.info(clientHandler.getNickname() + " подключился, " + clientHandler.getClientAddress());
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        logger.info(clientHandler.getNickname() + " отключился, " + clientHandler.getClientAddress());

        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/userlist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
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
