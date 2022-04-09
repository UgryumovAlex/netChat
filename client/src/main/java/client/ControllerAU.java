package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import java.util.Date;

public class ControllerAU implements Initializable {
    @FXML
    public HBox authPanel;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    private TextArea chatArea;
    @FXML
    public HBox msgPanel;
    @FXML
    private TextField textSend;
    @FXML
    public ListView<String> userList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;
    private String login;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private ClientLogging logger;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);
        userList.setVisible(authenticated);
        userList.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
            login = "";
        }
        setTitle(nickname);
        chatArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) chatArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                String[] token = str.split("\\s+");
                                nickname = token[1];
                                login = token[2];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith("/reg_ok")) {
                                regController.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")) {
                                regController.showResult("/reg_no");
                            }
                        } else {
                            chatArea.appendText(str + "\n");
                        }
                    }

                    if (authenticated) { //Если авторизовались, то включаем логгер
                        logger = new ClientLogging(login);

                        for ( String historyMsg: logger.getPrevLogData() ) {
                            Platform.runLater(() -> {chatArea.appendText(historyMsg+"\n");});
                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            // Обновление списка клиентов
                            if (str.startsWith("/userlist")) {
                                String[] token = str.split("\\s+");
                                Platform.runLater(() -> {
                                    userList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        userList.getItems().add(token[i]);
                                    }
                                });
                            }

                            // Поменяем nick
                            if (str.startsWith("/newNick_ok")) {
                                String[] token = str.split("\\s+", 3);
                                nickname = token[1];
                                setTitle(nickname);

                                String newNickStr = token[2] + nickname + "\n";
                                chatArea.appendText(newNickStr);
                                logger.LogMessage(newNickStr);
                            }
                        } else {
                            chatArea.appendText(str + "\n");
                            logger.LogMessage(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("disconnect");
                    setAuthenticated(false);
                    try {
                        socket.close();
                        logger.LoggingClose();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void clickSend(ActionEvent actionEvent) {
        try {
            out.writeUTF(textSend.getText());
            textSend.clear();
            textSend.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void setTitle(String title) {
        Platform.runLater(() -> {
            if (title.equals("")) {
                stage.setTitle("net chat");
            } else {
                stage.setTitle(String.format("net chat: [ %s ]", title));
            }
        });
    }

    @FXML
    public void clickUserList(MouseEvent mouseEvent) {
        String receiver = userList.getSelectionModel().getSelectedItem();
        textSend.setText("->" + receiver + " ");
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("net Chat Регистрация");
            regStage.setScene(new Scene(root, 400, 320));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        Platform.runLater(() -> regStage.show());
    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
