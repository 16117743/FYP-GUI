package sample;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.ResourceBundle;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import Browser.MyBrowser;
import Interface.MusicHostInterface;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.Button;
/************************/
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
    BitSet bitSet = new BitSet(5);
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

    @FXML
    ListView songList;

    @FXML
    Button javascript;

//    @FXML
//    ListView DJComments;
//
//    @FXML
//    Button boolDJComment;
//
//    @FXML
//    Button boolSkip;


    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    public MainSceneController()
    {
        rwlock = new ReentrantReadWriteLock();

        Task threadInputController = new Task<Void>() {
            @Override public Void call() {
                int switcher =0;
                while(true)
                {
                    try {
                        Thread.sleep(1000);
                        //prevent compilation error
                        if (mainModel != null)
                        {
                            final String message = readSongRequest();
                            /*****************/
                            /*******************/
                            //System.out.print("\n Main returned " + message);
                            if (message != null && !message.equals("")) {
                                /***/
                                Platform.runLater(() -> {
                                    songRequest.appendText("\n" + message);
                                    iSkip();
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
     //  new Thread(threadInputController).start();

        for(int i =0;i<5;i++)
            boolArray[i] = true;

        boolArray[2]=false;
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
        progBar.setProgress(0);

    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void testing(){
        //webEngine.executeScript( " updateHello(' " + "testing" + " ') " );
      //  System.out.print(region.test());
      //  mainModel.stopConnection();
        //region.script();
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

    @FXML
    private void startServer(ActionEvent event) {
        skipButton.setStyle("-fx-background-color:blue");
          doThreadStuff();
    }

    /***************************************************/
    public void iPlay() {
        playButton.setStyle("-fx-background-color:red");
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
                //sendmsgbyblut(json,2);
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
    public synchronized void inputControlSwitch(int whatToDo, String msg) {
        switch (whatToDo) {
            case 1:
                ControllerGetSongs();
                break;
            case 2:
                ControllerAddSong(msg);
                break;
            case 3:
                ControllerGetDJComments(msg);
                break;
            case 4:
                ControllerSkipSong();
                break;
            case 5:
                ControllerEchoSharedPreferencesSongs(msg);
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
        }
    }

    public synchronized void ControllerGetSongs(){
        Platform.runLater(() -> {
            System.out.print("\n ControllerGetSongs\n");
          //  iSkip();
        });
    }

    public synchronized void ControllerAddSong(String songs){
        Platform.runLater(() -> {
            System.out.print("\n ControllerAddSong");
            iSkip();
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
                thisThread.sleep(100);
                if (dataInputStream.available() > 0) {
                    whatToDo(whatToDo);
                }
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
                System.out.print("\n got 0");
                String msg0 = procInput();

//                if(msg0!=null)
//                    ControllerGetSongs();
                //sendMessageByBluetooth("response 1", 1);
                //String strings = Boolean.toString(boolArray[0]);
                String strings = Arrays.toString(boolArray);
                sendMessageByBluetooth(strings, 0);
                // start timeout
                break;
            case SONG_SELECT:
                System.out.print("\n got 1");
                String msg1 = procInput();

//                if(msg1!=null)
//                    ControllerGetSongs();
                //sendMessageByBluetooth("response 1", 1);
                System.out.print(mainModel.Json());
                sendMessageByBluetooth(mainModel.Json(), 1);
               // myStop();
                break;
            case SONG_SELECTED:
                System.out.print("\n got 2");
                String msg2 = procInput();
                if(msg2!=null)
                    ControllerAddSong(msg2);
                sendMessageByBluetooth("The song" + msg2 + "has been added to the queue", 2);
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
     * Send song selecting to client
     */
    @Override
    public void send1() {

    }

    /**
     * Received view song selection request from client
     */
    @Override
    public void recv1() {

    }

    /**
     * Send song request ok to client
     */
    @Override
    public void send2() {

    }

    /**
     * Received selected song from client
     */
    @Override
    public void recv2() {

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
