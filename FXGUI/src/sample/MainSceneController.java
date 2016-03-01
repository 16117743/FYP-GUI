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
        serverModel = new ServerModel();
        db = new AzureDB();

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = serverModel.pollQueue();
                    if (message != null && !message.equals("")) {
                        songRequest.appendText("\n" + message);
                       // progBar.se
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
        progBar.setProgress(0);
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
     private void add(ActionEvent event) {
      //  System.out.println("add");
//        progBar.setProgress(0);
        //int args = 4;
       // final int max = mainModel.getSelectionSize();

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
//                    if(i%1000 ==0)
//                        System.out.println(i);
//                    updateProgress(i, max);
                }

                for (int i = 0; i < max; i++) {
//                    System.out.println(mainModel.getSongInfo(i));
//                    songList.getItems().add(mainModel.getSongInfo(i));
                   // songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                    queueList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                    songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                }
                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();

    }

    @FXML
    private void remove(ActionEvent event) {
        //mainModel.removeSong();
    }

    @FXML  // add song args
    private void refreshMethod(ActionEvent event){


        Task task = new Task<Void>() {
            @Override public Void call() {
                System.out.println("test download");
                // progBar.setProgress(4);
                mainModel.downloadSong(0);
                System.out.print("done 1");
                mainModel.downloadSong(1);
                System.out.print("done 2");
                mainModel.downloadSong(2);
                System.out.print("done 3");
                mainModel.downloadSong(3);
                /**** REMOVE SONG FROM VIEW**/
                //songList.getItems().remove(4);
                //songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                return null;
            }
        };
        // progBar = new ProgressBar();
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }
    @FXML
    private void init(ActionEvent event) {
        //queueList.getItems().add(mainModel.get );
        //queueList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        mainModel.initSongs();

        for (int i = 0; i < 5; i++) {
            songList.getItems().add("test " + Integer.toString(i));//update gui with selection info
            System.out.print(mainModel.getSongInfo(i));
            songList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
            songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            //updateProgress(i, 5);
        }

        Task task = new Task<Void>() {
            @Override public Void call() {

                try {
                   // mainModel.initSongs();
                    Thread.sleep(1000);
                    for (int i = 0; i < 5; i++) {
                       // songList.getItems().add("test " + Integer.toString(i));//update gui with selection info
                        songList.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                        songList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                        updateProgress(i, 5);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return null;
            }
        };
        // progBar = new ProgressBar();

       // progBar.progressProperty().bind(task.progressProperty());
     //   new Thread(task).start();



    }

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
        if ("Play".equals(playButton.getText())) {
            //mainModel.Pause();
            playButton.setText("Pause");
        }
    }

    @FXML
    private void goToScreen1(ActionEvent event)
    {

    }

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
        serverModel.doThreadStuff();
        serverModel.createQueue();
    }

}
