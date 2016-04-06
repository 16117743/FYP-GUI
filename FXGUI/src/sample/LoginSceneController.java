package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;

public class LoginSceneController implements Initializable, ControlledScreen {

    ScreensController myController;

    @FXML
    private TextField userLogin;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label response;

    /**Initializes the controller class.*/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert userLogin != null : "login not injected!";
        assert passwordField != null : "login not injected!";

        userLogin.setTooltip(new Tooltip("Enter username"));
        passwordField.setTooltip(new Tooltip("Enter password"));

    }

    public void setScreenParent(ScreensController screenParent){
    myController = screenParent;
    }

    @FXML
    private void goToScreen2(ActionEvent event){
        if(Login())
        {
            try {
                response.setText("succces!");
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Platform.runLater(() ->
            {
                try {
                    Thread.sleep(2000);
                    myController.setScreen(MusicHostFramework.mainScreenID);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        else {
            Platform.runLater(() ->
            {
                response.setText("Incorrect login");
            });
        }
    }

    private boolean Login(){
        String user = userLogin.getText();
        String pw = passwordField.getText();
        int check = myController.confirmLogin(user,pw);
        if(check!=-1){
            Platform.runLater(() ->
            {
                myController.setUserID(check);
            });

            return true;
        }
        else
            return false;
    }

}
