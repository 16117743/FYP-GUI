package sample;

import java.net.URL;
import java.util.ResourceBundle;

import Browser.MyBrowser;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.*;

public class LoginSceneController implements Initializable, ControlledScreen {

    ScreensController myController;
    Model mainModel;
    final int LOGIN_STATE = 0;
    final int MAIN_STATE = 1;
    final int DJ_STATE = 2;

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
    }

    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
    myController = screenParent;
    mainModel = model;
    }

    public void setModel(Model model){
    mainModel = model;
    }

    @FXML
    private void goToScreen2(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen2ID);
       /* if(Login())
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
                    myController.setScreen(MusicHostFramework.screen2ID);
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
        }*/
    }

    private boolean Login(){
        String user = userLogin.getText();
        String pw = passwordField.getText();
        int check = mainModel.confirmLogin(user,pw);
        if(check!=-1){
            mainModel.setUserID(check);
            mainModel.setBoolArray(MAIN_STATE,true);
            return true;
        }
        else
            return false;
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }

    public void setBrowser(MyBrowser myBrowser){
      //  region = myBrowser;
    }
}
