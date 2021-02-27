package client;

import commands.Command;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.CookieManager;
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
    @FXML
    public ListView<String> listView;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final int PORT = 8189;
    private final String IP_ADDRESS = "localhost";
    private boolean isAuthenticated;
    private Stage stage;
    private Stage regStage;
    private RegController regController;


    private String nickName;

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
        sendMsgField.setVisible(isAuthenticated);
        sendMsgField.setManaged(isAuthenticated);
        authenticField.setVisible(!isAuthenticated);
        authenticField.setManaged(!isAuthenticated);
        listView.setVisible(isAuthenticated);
        listView.setManaged(isAuthenticated);

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
          stage.setOnCloseRequest(event -> {
              System.out.println("Buy");
              if (socket != null && !socket.isClosed()) {
                  try {
                      out.writeUTF(Command.END);
                  } catch (IOException e) {
                      e.printStackTrace();
                  }
              }
          });
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
                        if (str.startsWith(Command.SERVICE_MSG)) {
                            if (str.equals(Command.END)) {
                                throw new RuntimeException("Сервак нас отключает");
                            }
                            if (str.startsWith(Command.AUTH_OK)) {
                                String[] token = str.split(" ");
                                nickName = token[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.equals(Command.REG_OK)) {
                                regController.resultTryToReg(Command.REG_OK);
                            }
                            if (str.equals(Command.REG_NO)) {
                                regController.resultTryToReg(Command.REG_NO);
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }

                    while (true) {
                        String str = in.readUTF();
                        if (str.startsWith(Command.SERVICE_MSG)) {
                            if (str.equals(Command.END)) {
                                System.out.println("Client disconnected");
                                break;
                            }
                            if (str.startsWith(Command.CLIENT_LIST)) {
                                String [] token = str.split("\\s");
                                Platform.runLater(() -> {
                                    listView.getItems().clear();
                                    for (int i = 1; i < token.length ; i++) {
                                        listView.getItems().add(token[i]);
                                    }
                                });
                            }
                        } else {
                            textArea.appendText(str + "\n");
                        }
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

    @FXML
    public void clockOnClient(MouseEvent mouseEvent) {
        System.out.println(listView.getSelectionModel().getSelectedItems());
        String msg = String.format("%s %s ", Command.PRIVATE_MSG, listView.getSelectionModel().getSelectedItem());
        textField.setText(msg);
    }

    @FXML
    public void showRegWindow(ActionEvent actionEvent) {
        if (regStage == null) {
            initRegWindow();
        }
        regStage.show();

    }

    public void initRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage = new Stage();
            regStage.setTitle("Chat registration");
            regStage.setScene(new Scene(root, 500, 375));
            regStage.initStyle(StageStyle.UTILITY);
            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registration(String log, String pass, String nick) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        try {
            out.writeUTF(String.format("%s %s %s %s", Command.REG, log, pass, nick));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
