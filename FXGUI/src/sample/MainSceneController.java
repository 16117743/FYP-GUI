package sample;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.*;

public class MainSceneController implements Initializable , ControlledScreen {
    Model mainModel;
    ServerModel serverModel;
    ScreensController myController;
    final LongProperty lastUpdate = new SimpleLongProperty();
    final long minUpdateInterval = 0 ;

    @FXML
    Button playButton;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    public MainSceneController(){
        serverModel = new ServerModel();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = serverModel.pollQueue();
                    if (message != null && !message.equals("")) {
                        songRequest.appendText("\n" + message);
                    }
                    lastUpdate.set(now);
                }
            }
        };
        timer.start();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert playButton != null;
        assert songRequest != null : "songrequest not injected!";
    }

    @FXML
    private void refreshMethod(ActionEvent event){
        System.out.println(mainModel.getTest());
    }

    @FXML
    private void startServer(ActionEvent event) {
        serverModel.doThreadStuff();
        serverModel.createQueue();
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
    private void play(ActionEvent event){
        if ("Pause".equals(playButton.getText())) {
            mainModel.Pause();
            playButton.setText("Play");
        } else {
            mainModel.Play();
            playButton.setText("Pause");
        }
    }

    @FXML
    private void skipMethod(ActionEvent event){
        mainModel.Skip();
    }

    @FXML
    private void goToScreen1(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen1ID);
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }
    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model){
    myController = screenParent;
    mainModel = model;
    }
}
