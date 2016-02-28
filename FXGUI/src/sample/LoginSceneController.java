package sample;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.PasswordField;
import model.*;

public class LoginSceneController implements Initializable, ControlledScreen {

    AzureDB db;
    ScreensController myController;
    Model mainModel;

    @FXML
    private PasswordField login;

    @FXML
    private PasswordField passwordField;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {


        assert login != null : "login not injected!";
        assert passwordField != null : "login not injected!";
        //   db.insertImage(conn,"C://theset//test2.mp3");
        //db.getImageData(conn);
    }

    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
    myController = screenParent;
    mainModel = model;
    db = database;
}

    public void setModel(Model model){
    mainModel = model;
    }

    @FXML
    private void goToScreen2(ActionEvent event){
       // Boolean b = Login();
        myController.setScreen(MusicHostFramework.screen2ID);
    }

    private Boolean Login(){

        System.out.println(mainModel.getTest());

        String id = "";
          id  = login.getText();
        String pw = "";
        pw = passwordField.getText();

        System.out.println(id +"\n" + pw);
        mainModel.setTest("test2");
        myController.getTest();
        return true;
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }
}
