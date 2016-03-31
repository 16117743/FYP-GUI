package sample;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import Browser.MyBrowser;
import Interface.MusicHostInterface;
import javafx.application.Platform;
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
/************************/
import javafx.util.Callback;
import model.*;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class MainSceneController implements Initializable , ControlledScreen {
    Model mainModel;
    AzureDB db;
    ScreensController myController;
    /****************/
    ProcessConnectionThread processThread;
    private ReadWriteLock rwlock;
    volatile String input;
    Boolean[] boolArray = new Boolean[5];

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
    private TextArea songRequest;

    public MainSceneController()
    {
        rwlock = new ReentrantReadWriteLock();

        for(int i =0;i<5;i++)
            boolArray[i] = false;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert playButton != null;
        assert songRequest != null : "songrequest not injected!";
        assert prog != null : "songrequest not injected!";
        assert progBar != null : "songrequest not injected!";
        assert javascript != null : "songrequest not injected!";
       // assert DJComments != null : "songrequest not injected!";
        boolRequest.setStyle("-fx-background-color:red");
        boolDJComment.setStyle("-fx-background-color:red");
        boolSkip.setStyle("-fx-background-color:red");
        boolEcho.setStyle("-fx-background-color:red");
        boolBlob.setStyle("-fx-background-color:red");
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

        try {
            queueList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<QueueSong>() {
                @Override
                public void changed(ObservableValue<? extends QueueSong> observable, QueueSong oldValue, QueueSong newValue) {
                    QueueSong old = (QueueSong)oldValue;
                    QueueSong newv = (QueueSong)newValue;
                    System.out.println("ListView selection changed from oldValue = "
                        + old.getSong() + " to newValue = " + newv.getSong());
                    queueList.getSelectionModel().clearSelection();
                }
            });

            songList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SelectionSong>() {
                @Override
                public void changed(ObservableValue<? extends SelectionSong> observable, SelectionSong oldValue, SelectionSong newValue) {


                    System.out.println("ListView selection changed from oldValue = "
                         + " to newValue = " + newValue);
                }
            });
        } catch (Exception e) {

        }

//                SongQueueObservableList.addListener(new ListChangeListener<QueueSong>() {
//            @Override
//            public void onChanged(Change<? extends QueueSong> c) {
//                System.out.print("\n SQ changed");
//            }
//        });
    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void testing(){
        //webEngine.executeScript( " updateHello(' " + "testing" + " ') " );
        //  System.out.print(region.test());
        //  mainModel.stopConnection();
        //region.script();
    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void removeSong(){
       // System.out.print("\n " + mainModel.getSongQueue());
        Platform.runLater( () -> {
            //queueList.getItems().remove(0);
            SongQueueObservableList.remove(0);
        });

       // queueList.getSelectionModel().clearSelection();

//        QueueSong sq = (QueueSong) queueList.getSelectionModel().getSelectedItem();
//        System.out.println(sq.getSong());
       // SongQueueObservableList.remove(queueList.getSelectionModel().getSelectedItem());

      //  System.out.println(queueList.getCellFactory());

       // System.out.println(queueList.getItems());
      //  queueList.setItems(SongQueueObservableList);
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
     private void add(ActionEvent event) {

//        SongQueueObservableList.add(new QueueSong("song 1", "artist 1"));
//        SongQueueObservableList.add(new QueueSong("song 2", "artist 2"));
//        SongQueueObservableList.add(new QueueSong("song 3", "artist 3"));
//        SongQueueObservableList.add(new QueueSong("song 4", "artist 4"));
//        queueList.getItems().add("1");
//        queueList.getItems().add("2");
//        queueList.getItems().add("3");
   //     queueList.setItems(SongQueueObservableList);

        Task task = new Task<Void>() {
            @Override public Void call() {
                QueueSong qs = mainModel.addSongToQueue(2);
                Platform.runLater( () -> {
                    SongQueueObservableList.add(qs);
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
        SongQueueObservableList = FXCollections.observableList(mainModel.getSongQueue());
        queueList.setItems(SongQueueObservableList);

//        SongSelectionObservableList = FXCollections.observableList(mainModel.getSelection());
//        songList.setItems(SongSelectionObservableList);

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
        SongQueueObservableList.remove(0);
        SongQueueObservableList.get(0).playMe();
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }

    @FXML
    private void startServer(ActionEvent event) {
       // skipButton.setStyle("-fx-background-color:green");
          doThreadStuff();
    }

    /****************************************************/
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

    /***************************************************/
    @FXML
    public void iPlay() {
        playButton.setStyle("-fx-background-color:red");
        System.out.println("test interface play");
       // mainModel.playSong(this.getClass());
        if ("Pause".equals(playButton.getText())) {
            SongQueueObservableList.get(0).pauseMe();
            playButton.setText("Play");
        } else {
            SongQueueObservableList.get(0).playMe();
           // mainModel.playSong(this.getClass());
            playButton.setText("Pause");
        }
    }

    @FXML
    public void iSkip() {
        if(SongQueueObservableList.size()>0) {
            SongQueueObservableList.get(0).pauseMe();
            SongQueueObservableList.remove(0);
        }

        if(SongQueueObservableList.size()>0)
            SongQueueObservableList.get(0).playMe();

        if ("Play".equals(playButton.getText())) {
            playButton.setText("Pause");
        }
    }

    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
        myController = screenParent;
        mainModel = model;
        db = database;
    }

    public void setBrowser(MyBrowser myBrowser){

    }

    /**??????????????????????????????????????????????????????????????????????????????***/

    public synchronized String readSongRequest(){
        rwlock.readLock().lock();
        try {
            if(input !=null) {
                String temp = input;
                input = null;
                return temp;
            }
            else
                return null;
        } finally {
            rwlock.readLock().unlock();
        }
    }

public void doThreadStuff(){
    try
    {
        new Thread(){
            public void run() {
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
                    System.out.print("stop");;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection = null;

                while (flag == false) {
                    try {
                        System.out.println("waiting for connectionsssssss...");
                        connection = notifier.acceptAndOpen();
                        System.out.println("connected!");
                        processThread = new ProcessConnectionThread(connection);
                        processThread.run();
                        System.out.print("\n exited!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /***&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&***/
            }
        }.start();//end new Thread();
    }//end try 1
    catch(Exception e)
    {
        Thread.currentThread().interrupt();
        return;
    }
    }
/**********************************************************************************************************************/
    /**
     * threadInputController methods
     * */

    public synchronized void ControllerGetSongs(){
        Platform.runLater(() -> {
            System.out.print("\n ControllerGetSongs\n");
          //  return json string of songs
            //return mainmodel.getSongSelection();
        });
    }

    public synchronized void ControllerAddSong(String song){
        Platform.runLater(() -> {
            System.out.print("\n ControllerAddSong\n");
            //search DB selection for song matching song string
            mainModel.addSongToQueue(0);
            mainModel.addSongToQueue(0);
            //songQueue.add(mainmodel.addsong(song))
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

    /**
     * Server Connection Thread
     * */
    public class ProcessConnectionThread implements Runnable, MusicHostInterface {
    private volatile StreamConnection mConnection;
    private volatile Thread volatileThread;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    /*****CONSTANTS ****************************/
    final int SONG_SELECT = 1;
    final int SONG_SELECTED = 2;
    final int DJ_COMMENT = 3;
    final int SKIP_SONG  = 4;
    final int ECHO_SHARED_PREF_SONGS = 5;
    final int ECHO_BLOB_SONGS = 6;
    final int REMOTE_SELECT = 7;
    final int WANT_END = 8;
    /********************************************/

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
           // System.out.println("sending options");
           // sendMessageByBluetooth("b",0);
            int whatToDo = 0;

            try {
                whatToDo = dataInputStream.readInt();
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
                    /****************/
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
            writeSongRequest(msgstring);
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
                /********************/
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

    public synchronized void writeSongRequest(String request){
        rwlock.writeLock().lock();
        try {
            input = request;
        } finally {
           rwlock.writeLock().unlock();
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
}
