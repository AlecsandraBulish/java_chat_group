package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public PasswordField passField;
    @FXML
    public TextField logField;
    @FXML
    public HBox authenticField;
    @FXML
    public HBox sendMsgField;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private boolean isAuthenticated;
    private Stage stage;

    private String nickName;

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
        sendMsgField.setVisible(isAuthenticated);
        sendMsgField.setManaged(isAuthenticated);
        authenticField.setVisible(!isAuthenticated);
        authenticField.setManaged(!isAuthenticated);

        if (!isAuthenticated) {
            nickName = "";
        }
        textArea.clear();
        setTitle(nickName);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
          stage = (Stage) textArea.getScene().getWindow();
        });
        setAuthenticated(false);

    }
    public void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    // цикл аутоинтефикации
                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith("/")) {
                            if (str.equals(Command.END)) {
                                throw new RuntimeException("Сервак нас отключает");
                            }
                            if (str.startsWith(Command.AUTH_OK)) {
                                String[] token = str.split(" ");
                                nickName = token[1];
                                setAuthenticated(true);
                                break;
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();

                        if (str.equals(Command.END)) {
                            System.out.println("Client disconnected");
                            break;
                        }

                        textArea.appendText(str + "\n");
                    }
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendOnClick(ActionEvent actionEvent) {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToLogin(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s",Command.AUTH, logField.getText().trim(), passField.getText().trim()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setTitle(String nickName) {
        Platform.runLater(() -> {
            if (nickName.equals("")) {
                stage.setTitle("Chat");
            } else {
                stage.setTitle(String.format("Chat - [ %s ]", nickName));
            }

        });
    }
}
