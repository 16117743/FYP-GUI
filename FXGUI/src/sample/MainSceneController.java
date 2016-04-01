package sample;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;
import Interface.MusicHostInterface;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Callback;
import javafx.util.Duration;
import model.*;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class MainSceneController implements Initializable , ControlledScreen {
    Model mainModel;
    ScreensController myController;
    /****************/
    ProcessConnectionThread processThread;
    Thread server;
    volatile String input;
    Boolean[] boolArray = new Boolean[5];
    volatile boolean stopFlag = false;
    private volatile Thread volatileThread;

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

    ObservableList<QueueSong> SongQueueObservableList;

    @FXML
    ListView songList;

    ObservableList<SelectionSong> SongSelectionObservableList;

    @FXML
    MediaView mediaView;

    @FXML
    ProgressBar songProgressBar;

    @FXML
    Slider volumeSlider;

    @FXML
    Button javascript;

    @FXML
    ListView DJComments;

    @FXML
    Button boolRequest;

    @FXML
    Button boolDJComment;

    @FXML
    Button boolSkip;

    @FXML
    Button boolEcho;
    @FXML
    Button boolBlob;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    Button serverButton;

    @FXML
    private TextArea songRequest;

    @FXML
    Slider timeSlider;

    @FXML
    Label timeLabel;

    private ChangeListener<Duration> progressChangeListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert playButton != null;
        assert songRequest != null : "songrequest not injected!";
        assert prog != null : "songrequest not injected!";
        assert progBar != null : "songrequest not injected!";
        assert javascript != null : "songrequest not injected!";
        boolRequest.setStyle("-fx-background-color:red");
        boolDJComment.setStyle("-fx-background-color:red");
        boolSkip.setStyle("-fx-background-color:red");
        boolEcho.setStyle("-fx-background-color:red");
        boolBlob.setStyle("-fx-background-color:red");
        playButton.setStyle("-fx-background-color:red");
        serverButton.setStyle("-fx-background-color:red");
        initbtn.setStyle("-fx-background-color:green");
        progBar.setProgress(0);

        queueList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        songList.setCellFactory(new Callback<ListView<SelectionSong>, ListCell<SelectionSong>>(){
            @Override
            public ListCell<SelectionSong> call(ListView<SelectionSong> p) {
                ListCell<SelectionSong> cell = new ListCell<SelectionSong>(){
                    @Override
                    protected void updateItem(SelectionSong t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            setText(t.getSong() + " by " + t.getArtist());
                        }
                        else
                            setText("\r");
                    }
                };
                return cell;
            }
        });

        queueList.setCellFactory(new Callback<ListView<QueueSong>, ListCell<QueueSong>>() {
            @Override
            public ListCell<QueueSong> call(ListView<QueueSong> myObjectListView) {
                ListCell<QueueSong> cell = new ListCell<QueueSong>(){
                    @Override
                    protected void updateItem(QueueSong myObject, boolean empty) {
                        super.updateItem(myObject, empty);
                        setText((empty || myObject == null) ? null : myObject.getSong());
                    }
                };
                return cell;
            }
        });

        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                setCurrentlyPlaying(newPlayer);
            }
        });

        for(int i =0;i<5;i++)
            boolArray[i] = false;
    }

    private void addMediaViewPropertyListener(){

    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void testing(){

    }

    private void addListenersToNewlyAdded(MediaPlayer current, MediaPlayer next){
            final MediaPlayer player     = current;
            final MediaPlayer nextPlayer = next;
            //set listener for end of current song
            player.setOnEndOfMedia(new Runnable() {
                @Override public void run() {
                    player.currentTimeProperty().removeListener(progressChangeListener);
                    player.stop();
                    player.dispose();//release filestream link
                    mediaView.setMediaPlayer(nextPlayer);
                    nextPlayer.play();
                    SongQueueObservableList.remove(0);
                }
            });
    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void removeSong(){
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        curPlayer.dispose();

        MediaPlayer nextPlayer = SongQueueObservableList.get(1).getPlayer();
        mediaView.setMediaPlayer(nextPlayer);
        nextPlayer.play();

        SongQueueObservableList.remove(0);
    }

    /** sets the currently playing label to the label of the new media player and updates the progress monitor. */
    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
        newPlayer.seek(Duration.ZERO);

        songProgressBar.setProgress(0);
        progressChangeListener = new ChangeListener<Duration>() {
            @Override public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                songProgressBar.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
                timeLabel.setText(formatTime(newPlayer.getCurrentTime(),  newPlayer.getTotalDuration()));
            }
        };
        newPlayer.currentTimeProperty().addListener(progressChangeListener);

        System.out.println("Now Playing: ");
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
     private void addSongButtonFunc(ActionEvent event) {
        Task task = new Task<Void>() {
            @Override public Void call() {
                QueueSong sq0 = mainModel.createQueueSong(songList.getSelectionModel().getSelectedIndex());
                Platform.runLater( () -> {
                    SongQueueObservableList.add(sq0);
                });
                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        //if valid selection
        if(songList.getSelectionModel().getSelectedIndex()>-1)
            new Thread(task).start();
    }

    public void addSongTask(int selectionSongIndex){
        Task task = new Task<Void>() {
            @Override public Void call() {
                final int index = selectionSongIndex;
                QueueSong sq0 = mainModel.createQueueSong(index);
                Platform.runLater( () -> {
                    SongQueueObservableList.add(sq0);
                });
                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     *
     * @param event sets up the requirements for running the application that could not be run at compile time due to null pointer exceptions
     */
    @FXML
    private void init(ActionEvent event)
    {
        //init button can only be pressed once
        if("-fx-background-color:green".equals(initbtn.getStyle())) {
            initbtn.setStyle("-fx-background-color:red");
            //this cannot be called in initialize because mainmodel throws a null pointer exception
            addObservableSongQueueListener();
            addVolumeAndTimeSliderListeners();
            initSongSelection();
        }
    }

    public void addObservableSongQueueListener(){
        SongQueueObservableList = FXCollections.observableList(mainModel.getSongQueue());
        queueList.setItems(SongQueueObservableList);
        SongQueueObservableList.addListener(new ListChangeListener<QueueSong>() {
            public void onChanged(ListChangeListener.Change<? extends QueueSong> change) {
                while (change.next()) {
                    if (change.wasUpdated()) {
                        for (QueueSong qs : change.getList()) {
                            System.out.println(qs.getSong() + " updated");
                        }
                    } else {
                        //song is skipped or has ended
                        for (QueueSong qs : change.getRemoved()) {
                            if (SongQueueObservableList.size() > 1) {
                                addListenersToNewlyAdded(SongQueueObservableList.get(0).getPlayer(), SongQueueObservableList.get(1).getPlayer());
                            }
                            qs.deleteMyPlayer();
                            qs.deleteMyFile();
                        }

                        for (QueueSong qs : change.getAddedSubList()) {
                            if (change.getFrom() == 1) {
                                addListenersToNewlyAdded(SongQueueObservableList.get(0).getPlayer(), qs.getPlayer());
                            }
                        }
                    }
                }
            }
        });
    }

    public void addVolumeAndTimeSliderListeners(){
        timeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (timeSlider.isValueChanging()) {
                    final MediaPlayer player = SongQueueObservableList.get(0).getPlayer();
                    if (progressChangeListener != null) {
                        // multiply duration by percentage calculated by timeSlider position
                        player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                    }
                }
            }
        });

        volumeSlider.valueProperty().addListener(new InvalidationListener() {
            public void invalidated(Observable ov) {
                if (volumeSlider.isValueChanging()) {
                    final MediaPlayer player = SongQueueObservableList.get(0).getPlayer();
                    player.setVolume(volumeSlider.getValue() / 100.0);
                }
            }
        });
    }

    public void initSongSelection(){
        Task task = new Task<Void>()
        {
            @Override public Void call() {
                try
                {
                    mainModel.initSongs();//read from database and initialize selection list with song & artist names
                } catch (Exception e) {
                    e.printStackTrace();
                }

                SongSelectionObservableList = FXCollections.observableList(mainModel.getSelection());
                songList.setItems(SongSelectionObservableList);

                return null;
            }
        };
        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    @FXML
    private void goToScreen1(ActionEvent event) {
       // myController.setScreen(MusicHostFramework.screen1ID);
        //reset logged in user ID
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }


    /************************************************************************************************/


    @FXML
    public void iPlay() {
        if ("Pause".equals(playButton.getText())) {
            mediaView.getMediaPlayer().pause();
            playButton.setText("Play");
            playButton.setStyle("-fx-background-color:green");
        } else if (SongQueueObservableList.size()>0){
            mediaView.setMediaPlayer(SongQueueObservableList.get(0).getPlayer());
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
            playButton.setStyle("-fx-background-color:red");
        }
    }

    @FXML
    public void iSkip() {
        //can't skip unless there is a song to follow
        if(SongQueueObservableList.size()>1) {
            final MediaPlayer curPlayer = mediaView.getMediaPlayer();
            curPlayer.currentTimeProperty().removeListener(progressChangeListener);
            curPlayer.stop();
            curPlayer.dispose();//remove file stream

            MediaPlayer nextPlayer = SongQueueObservableList.get(1).getPlayer();
            mediaView.setMediaPlayer(nextPlayer);
            nextPlayer.play();
            SongQueueObservableList.remove(0);

            //play button has to beupdated with "pause" to the user because skip "plays" a song
            if ("Play".equals(playButton.getText())) {
                playButton.setText("Pause");
                playButton.setStyle("-fx-background-color:green");
            }
        }
    }

    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model){
        myController = screenParent;
        mainModel = model;
    }

    /**??????????????????????????????????????????????????????????????????????????????***/



    public void stopServer(){
        volatileThread = null;
    }
/**********************************************************************************************************************/
    /**
     * threadInputController methods
     * */
    public void searchSelectionForMatch(String songToSearch){
        //java 8 stream API replacement of for each
        int [] indices = IntStream.range(0, SongSelectionObservableList.size())
            .filter(i -> songToSearch.equals(SongSelectionObservableList.get(i).getSong()))
            .toArray();

        if(indices.length == 1)
        {
            addSongTask(indices[0]);
        }
    }

    public int searchQueueForMatch(String songToSearch){
        //java 8 stream API replacement of for each
        int [] indices = IntStream.range(0, SongSelectionObservableList.size())
            .filter(i -> songToSearch.equals(SongSelectionObservableList.get(i).getSong()))
            .toArray();

        if(indices.length == 1)
        {
            return indices[0];
        }
        else
            return -1;
    }

    public synchronized void ControllerGetSongs(){
        Platform.runLater(() -> {
            System.out.print("\n ControllerGetSongs\n");
          //  return json string of songs
            //return mainmodel.getSongSelection();
        });
    }

    public synchronized void ControllerAddSong(String song){
        Platform.runLater(() -> {
            searchSelectionForMatch(song);
        });
    }

    public synchronized void ControllerGetDJComments(String songs){
        Platform.runLater(() -> {
            //1- update DJ comment on GUI
            //2- get updated comment list and put it into JSON DJ comments bean
            System.out.print("\n test 3");
            iSkip();
        });
    }

    public synchronized void ControllerSkipSong(){
        Platform.runLater(() -> {
            System.out.print("\n test 4");
            iSkip();
        });
    }

    public synchronized void ControllerEchoSharedPreferencesSongs(String songs) {
        Platform.runLater(() -> {
            System.out.print("\n test 5");
            iSkip();
        });
    }

    public synchronized void ControllerAddBlobSong(String [] songs){
        Platform.runLater(() -> {
            System.out.print("\n test 6");
            iSkip();
        });
    }

/**********************************************************************************************************************/

    @FXML
    private void startServer(ActionEvent event) {
        if("ON".equals(serverButton.getText())) {
            serverButton.setStyle("-fx-background-color:red");
            serverButton.setText("OFF");
            stopServer();
        }
        else{
            serverButton.setStyle("-fx-background-color:green");
            serverButton.setText("ON");
            startServer();
        }
    }
public void startServer(){
    try
    {

        new Thread(){
            public void run() {
                volatileThread = Thread.currentThread();
                Thread thisThread = Thread.currentThread();
                Boolean flag = false;
                StreamConnectionNotifier notifier = null;
                StreamConnection connection = null;

                try {
                    LocalDevice local = null;
                    local = LocalDevice.getLocalDevice();
                    local.setDiscoverable(DiscoveryAgent.GIAC);
                    UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
                    String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
                    notifier = (StreamConnectionNotifier) Connector.open(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try
                {
                    while (volatileThread == thisThread) {
                        thisThread.sleep(1000);
                        System.out.println("waiting for connectionsssssss...");
                        connection = notifier.acceptAndOpen();
                        System.out.println("connected!");
                        processThread = new ProcessConnectionThread(connection);
                        if (volatileThread == thisThread)
                            processThread.run();
                        System.out.print("\n exited!");
                    }
                }
                    catch (InterruptedException e) {
                        processThread.myStop();
                        Thread.currentThread().interrupt();
                    }
                    catch (NullPointerException e) {
                        System.out.print("\nexited through here 2 ");
                    }
                    catch (IOException e) {
                        System.out.print("\nexited through here 3");
                    }
                finally {

                    try {
                        notifier.close();
                        connection = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.print("\n finished!");
                return;
            }
        }.start();//end new Thread();
    }//end try 1
    catch(Exception e)
    {
        Thread.currentThread().interrupt();
        return;
    }
}
    /**
     * Server Connection Thread
     * */
    public class ProcessConnectionThread implements Runnable, MusicHostInterface {
    private volatile StreamConnection mConnection;
    private volatile Thread volatileThread;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    /**
     * CONSTANTS
     * */
    final int SONG_SELECT = 1;
    final int SONG_SELECTED = 2;
    final int DJ_COMMENT = 3;
    final int SKIP_SONG  = 4;
    final int ECHO_SHARED_PREF_SONGS = 5;
    final int ECHO_BLOB_SONGS = 6;
    final int REMOTE_SELECT = 7;
    final int WANT_END = 8;

    /**
     * Constructor
     * */
    public ProcessConnectionThread(StreamConnection connection)
    {
        mConnection = connection;
    }

    @Override
    public void run() {
        volatileThread = Thread.currentThread();
        Thread thisThread = Thread.currentThread();
        try
        {
            dataInputStream = new DataInputStream(mConnection.openInputStream());
            dataOutputStream = new DataOutputStream(mConnection.openOutputStream());

            try {
                int whatToDo = dataInputStream.readInt();
                System.out.print("\nread  " + whatToDo);
                thisThread.sleep(300);
                if (dataInputStream.available() > 0) {
                    System.out.print("\ninit data available  " + whatToDo);
                    whatToDo(whatToDo);
                }
                thisThread.sleep(300);
                whatToDo = dataInputStream.readInt();
                System.out.print("\nread  " + whatToDo);
                while (volatileThread == thisThread) {
                    try {
                        whatToDo = dataInputStream.readInt();
                        System.out.print("\nread  " + whatToDo);
                        thisThread.sleep(100);
                    } catch (InterruptedException e) {
                        System.out.print("\nexited through here");
                    }
                    if (dataInputStream.available() > 0) {
                        whatToDo(whatToDo);
                    }
                }
            } catch (Exception e) {}

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public synchronized void myStop(){
        volatileThread = null;
    }

    public synchronized void whatToDo(int whatToDo)
    {
        switch(whatToDo)
        {
            case 0:
                send0(recv0());
                break;
            case SONG_SELECT:
                send1(recv1());
                break;
            case SONG_SELECTED:
                send2(recv2());
               // myStop();
                break;
            case DJ_COMMENT:
                System.out.print("\n got 3");
                String msg3 = procInput();
                if(msg3!=null)
                    ControllerGetDJComments(msg3);
                sendMessageByBluetooth("response 3", 3);
                break;
            case SKIP_SONG:
                System.out.print("\n got 4");
                String msg4 = procInput();
                if(msg4!=null)
                    ControllerSkipSong();
                sendMessageByBluetooth("response 4", 4);
                break;
            case ECHO_SHARED_PREF_SONGS:
                System.out.print("\n got 5");
                String msg5 = procInput();
                if(msg5!=null)
                    ControllerEchoSharedPreferencesSongs(msg5);
                sendMessageByBluetooth("response 5", 5);
                break;
            case 6:
                System.out.print("\n got 6");
                String msg6 = procInput();
                if(msg6!=null)
                    sendMessageByBluetooth("response 6",6);
                break;
            case 7:
                System.out.print("\n got 7");
                procInput();
                sendMessageByBluetooth("response 7",7);
                break;
            case 8:
                System.out.print("\n got 8");
                procInput();
                sendMessageByBluetooth("response 8",8);
                break;
        }
    }

    public synchronized String procInput(){
        try {
            byte[] msg = new byte[dataInputStream.available()];
            dataInputStream.read(msg, 0, dataInputStream.available());
            String msgstring = new String(msg);
            return msgstring;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized boolean sendMessageByBluetooth(String msg,int whatToDo)
    {
        try
        {
            if(dataOutputStream != null){
                dataOutputStream.writeInt(whatToDo);
                dataOutputStream.flush();
                dataOutputStream.write(msg.getBytes());
                dataOutputStream.flush();
                System.out.print("sending here: " + msg);
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /*************************************************************************/
    /**
     * Send options selecting to client
     */
    @Override
    public void send0(String msg) {
        System.out.print(msg);
        sendMessageByBluetooth(msg, 0);
    }

    /**
     * Received a Music Host Client
     */
    @Override
    public String recv0() {
        String strings = Arrays.toString(boolArray);
        System.out.print(strings);
        return strings;
    }
    /**
     * Send song selecting to client
     */
    @Override
    public void send1(String msg) {
        sendMessageByBluetooth(mainModel.Json(), 1);
    }

    /**
     * Received view song selection request from client
     */
    @Override
    public String recv1() {
        return procInput();
    }

    /**
     * Send song request ok to client
     */
    @Override
    public void send2(String msg) {
        sendMessageByBluetooth("The song" + msg + "has been added to the queue", 2);
    }

    /**
     * Received selected song from client
     */
    @Override
    public String recv2() {
        String msg2 = procInput();
        if(msg2!=null)
            ControllerAddSong(msg2);
        return msg2;
    }

    /**
     * Send song DJ comment history to client
     */
    @Override
    public void send3() {

    }

    /**
     * Received DJ comment from client
     */
    @Override
    public void recv3() {

    }

    /**
     * Send skip ok to client
     */
    @Override
    public void send4() {

    }

    /**
     * Received skip request from client
     */
    @Override
    public void recv4() {

    }

    /**
     * Send songs that echoed with my selection to client
     */
    @Override
    public void send5() {

    }

    /**
     * Received shared preferences songs from client
     */
    @Override
    public void recv5() {

    }

    /**
     * Send send blob service to client
     */
    @Override
    public void send6() {

    }

    /**
     * Received song blob service song string from client
     */
    @Override
    public void recv6() {

    }

    /**
     * Send repeat song ok to client
     */
    @Override
    public void send7() {

    }

    /**
     * Received repeat song select from client
     */
    @Override
    public void recv7() {

    }

    /**
     * Send end repeat song select ok to client
     */
    @Override
    public void send8() {

    }

    /**
     * Received end repeat song select from client
     */
    @Override
    public void recv8() {

    }
}//end connection thread class

    /** Boolean button options*/
    @FXML
    private void setBool1(){
        if ("ON".equals(boolRequest.getText())) {
            boolRequest.setStyle("-fx-background-color:red");
            boolRequest.setText("OFF");
            boolArray[0] = false;
        } else {
            boolRequest.setStyle("-fx-background-color:green");
            boolRequest.setText("ON");
            boolArray[0] = true;
        }
    }

    @FXML
    private void setBool2(){
        if ("ON".equals(boolDJComment.getText())) {
            boolDJComment.setStyle("-fx-background-color:red");
            boolDJComment.setText("OFF");
            boolArray[1] = false;
        } else {
            boolDJComment.setStyle("-fx-background-color:green");
            boolDJComment.setText("ON");
            boolArray[1] = true;
        }
    }

    @FXML
    private void setBool3(){
        if ("ON".equals(boolSkip.getText())) {
            boolSkip.setStyle("-fx-background-color:red");
            boolSkip.setText("OFF");
            boolArray[2] = false;
        } else {
            boolSkip.setStyle("-fx-background-color:green");
            boolSkip.setText("ON");
            boolArray[2] = true;
        }
    }

    @FXML
    private void setBool4(){
        if ("ON".equals(boolEcho.getText())) {
            boolEcho.setStyle("-fx-background-color:red");
            boolEcho.setText("OFF");
            boolArray[3] = false;
        } else {
            boolEcho.setStyle("-fx-background-color:green");
            boolEcho.setText("ON");
            boolArray[3] = true;
        }
    }

    @FXML
    private void setBool5(){
        if ("ON".equals(boolBlob.getText())) {
            boolBlob.setStyle("-fx-background-color:red");
            boolBlob.setText("OFF");
            boolArray[4] = false;
        } else {
            boolBlob.setStyle("-fx-background-color:green");
            boolBlob.setText("ON");
            boolArray[4] = true;
        }
    }

private static String formatTime(Duration elapsed, Duration duration) {
    int intElapsed = (int)Math.floor(elapsed.toSeconds());
    int elapsedHours = intElapsed / (60 * 60);
    if (elapsedHours > 0) {
        intElapsed -= elapsedHours * 60 * 60;
    }
    int elapsedMinutes = intElapsed / 60;
    int elapsedSeconds = intElapsed - elapsedHours * 60 * 60
        - elapsedMinutes * 60;

    if (duration.greaterThan(Duration.ZERO)) {
        int intDuration = (int)Math.floor(duration.toSeconds());
        int durationHours = intDuration / (60 * 60);
        if (durationHours > 0) {
            intDuration -= durationHours * 60 * 60;
        }
        int durationMinutes = intDuration / 60;
        int durationSeconds = intDuration - durationHours * 60 * 60 -
            durationMinutes * 60;
        if (durationHours > 0) {
            return String.format("%d:%02d:%02d/%d:%02d:%02d",
                elapsedHours, elapsedMinutes, elapsedSeconds,
                durationHours, durationMinutes, durationSeconds);
        } else {
            return String.format("%02d:%02d/%02d:%02d",
                elapsedMinutes, elapsedSeconds,durationMinutes,
                durationSeconds);
        }
    } else {
        if (elapsedHours > 0) {
            return String.format("%d:%02d:%02d", elapsedHours,
                elapsedMinutes, elapsedSeconds);
        } else {
            return String.format("%02d:%02d",elapsedMinutes,
                elapsedSeconds);
        }
    }
}
}
