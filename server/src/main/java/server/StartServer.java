package server;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

public class StartServer {

    public static void main(String[] args) {
        try {
            LogManager.getLogManager().readConfiguration(new FileInputStream("server/logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Запускаем сервер
        new Server();
    }
}
