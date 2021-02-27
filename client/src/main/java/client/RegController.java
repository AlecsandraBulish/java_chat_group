package client;

import commands.Command;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class RegController {
    @FXML
    private TextField logField;
    @FXML
    private PasswordField passField;
    @FXML
    private TextField nickField;
    @FXML
    private TextArea area;


    private Controller controller;

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void resultTryToReg(String command) {
        if (command.equals(Command.REG_OK)) {
            area.appendText("Регестрация прошла успешно");
        }
        if (command.equals(Command.REG_NO)) {
            area.appendText("Логин или Ник заняты");
        }
    }

    @FXML
    public void tryToReg(ActionEvent actionEvent) {
        String login = logField.getText().trim();
        String password = passField.getText().trim();
        String nickName = nickField.getText().trim();

        if (login.length()*password.length()*nickName.length() == 0) {
            return;
        }

        controller.registration(login,password,nickName);
    }
}
