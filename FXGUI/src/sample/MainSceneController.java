package sample;
import java.net.URL;
import java.util.ResourceBundle;


import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    public static final ObservableList data = FXCollections.observableArrayList();

    @FXML
    ProgressBar progBar;

    @FXML
    ProgressIndicator prog;

    @FXML
    Button playButton;

    @FXML
    Button initbtn;
    @FXML
    Button addbtn;

    @FXML
    Button removebtn;

    @FXML
    ListView queueList;

    @FXML
    ListView songList;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    public MainSceneController(){

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    if(mainModel != null)
                    {
                        final String message = mainModel.pollQueue();
                        if (message != null && !message.equals("")) {
                            Platform.runLater(() -> {
                                songRequest.appendText("\n" + message);
                            });
                        }
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
        progBar.setProgress(0);
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
     private void add(ActionEvent event) {
        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = mainModel.getSelectionSize() - 1;;
                for (int i=1; i<=max ; i++) {
                    try {
                        mainModel.downloadSong(i);//step 1
                        System.out.print("\n DL song " + i);
                        mainModel.addSong(i);//step 2
                        System.out.print("\n added song " + i + "to queue");
                        updateProgress(i, max);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.print("\n cancelled");
                    }
                    if (isCancelled()) {
                        break;
                    }
                }

                Platform.runLater(new Runnable() {
                    @Override public void run() {
                        for (int i = 0; i < max; i++) {
                            songList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                            queueList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                            songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                        }
                    }
                });
                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    @FXML
    private void init(ActionEvent event)
    {
        Task task = new Task<Void>()
        {
            @Override public Void call() {
                try
                {
                    final int max = mainModel.getSelectionSize();
                    mainModel.initSongs();//read from database and initialize selection list with song & artist names
                    for (int i = 0; i < max; i++) {//for the amount of songs in the selection
                        songList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                        songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    }
                   // mainModel.setChanged(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    @FXML
    private void play(ActionEvent event){
        mainModel.playSong(this.getClass());
//        if ("Pause".equals(playButton.getText())) {
//            mainModel.Pause();
//            playButton.setText("Play");
//        } else {
//            mainModel.Play();
//            playButton.setText("Pause");
//        }
    }

    @FXML
    private void goToScreen1(ActionEvent event) {}

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }

    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
    myController = screenParent;
    mainModel = model;
    db = database;
    }

    @FXML
    private void startServer(ActionEvent event) {
//        serverModel.
//        serverModel.createQueue();
        mainModel.doThreadStuff();
    }

    /***************************************************/

    public void iPlay() {
        System.out.println("test interface play");
        mainModel.playSong(this.getClass());
        if ("Pause".equals(playButton.getText())) {
            mainModel.pauseSong();
            playButton.setText("Play");
        } else {
            mainModel.playSong(this.getClass());
            playButton.setText("Pause");
        }
    }

    public void iSkip() {
        queueList.getItems().remove(0);
        mainModel.skipSong();
        if ("Play".equals(playButton.getText())) {
            playButton.setText("Pause");
        }
    }

}
