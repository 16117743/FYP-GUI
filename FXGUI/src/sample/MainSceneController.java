package sample;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.AnimationTimer;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.*;

public class MainSceneController implements Initializable , ControlledScreen {
    Model mainModel;
    ServerModel serverModel;
    AzureDB db;
    ScreensController myController;
    final LongProperty lastUpdate = new SimpleLongProperty();
    final long minUpdateInterval = 0 ;

    @FXML
    ProgressBar progBar;

    @FXML
    ProgressIndicator prog;

    @FXML
    Button playButton;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    public MainSceneController(){
        serverModel = new ServerModel();
        db = new AzureDB();
       // ControlledScreen myScreenControler = ((ControlledScreen) myLoader.getController());
       // InterfaceModel myInterface = ((InterfaceModel) db.getProgressBar());
        //myInterface.setModel(db.getProgressBar());
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = serverModel.pollQueue();
                    if (message != null && !message.equals("")) {
                        songRequest.appendText("\n" + message);
                        progBar.se
                        //System.out.println(progBar.get);
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
        assert prog != null : "songrequest not injected!";
        assert progBar != null : "songrequest not injected!";

        db.setModel(progBar);
    }

    @FXML
    private void refreshMethod(ActionEvent event){
        System.out.println(mainModel.getTest());
        db.test2();
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
    private void goToScreen1(ActionEvent event)
    {
        progBar.setVisible(true);
        System.out.print("go");
        //db.test1();
       // myController.setScreen(MusicHostFramework.screen1ID);
        progBar.setProgress(4);

        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = 100;
                for (int i=1; i<=max; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    updateProgress(i, max);
                }
                return null;
            }
        };
       // progBar = new ProgressBar();


        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    @FXML
    private void goToScreen3(ActionEvent event){

        System.out.print("go");
        //db.test1();
        // myController.setScreen(MusicHostFramework.screen1ID);
        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = 1000000;
                for (int i=1; i<=max; i++) {
                    if (isCancelled()) {
                        break;
                    }
                    updateProgress(i, max);
                }
                return null;
            }
        };
        progBar = new ProgressBar();
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
       // myController.setScreen(MusicHostFramework.screen3ID);
    }
    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
    myController = screenParent;
    mainModel = model;
    db = database;
    }


}
