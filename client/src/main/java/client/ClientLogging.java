package client;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ClientLogging {
    private String clientLogin;
    private File logFile;
    private FileWriter writer;
    private final int HISTORY_DEPTH = 15;

    public ClientLogging(String clientLogin) throws IOException {
        this.clientLogin = clientLogin;
        logFile = new File("client/log/history_"+clientLogin + ".txt");
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        writer = new FileWriter(logFile, true);
    }

    public void LoggingClose() throws IOException {
        writer.close();
    }

    public void LogMessage(String msg) throws IOException {
        writer.write(msg);
    }

    public List<String> getPrevLogData() throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(logFile.getPath()), StandardCharsets.UTF_8);
        if (lines.size() <= HISTORY_DEPTH) {
            return lines;
        } else {
            return lines.subList(lines.size()-HISTORY_DEPTH, lines.size());
        }
    }
}
