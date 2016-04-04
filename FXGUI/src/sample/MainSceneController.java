package sample;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
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
    ProcessConnectionThread processThread;
    Boolean[] boolArray = new Boolean[5];
    private volatile Thread volatileThread;
    ExecutorService executorService1;
    ExecutorService executorService2;
    private final ReadWriteLock currentPlayerlock = new ReentrantReadWriteLock();
    private final Lock CPReadLock = currentPlayerlock.readLock();
    private final Lock CPWwriteLock = currentPlayerlock.writeLock();

    private final ReadWriteLock nextPlayerlock = new ReentrantReadWriteLock();
    private final Lock NPReadLock = nextPlayerlock.readLock();
    private final Lock NPWriteLock = nextPlayerlock.writeLock();

    private final ReadWriteLock nextNextPlayerlock = new ReentrantReadWriteLock();
    private final Lock NNPReadLock = nextNextPlayerlock.readLock();
    private final Lock NNPWwriteLock = nextNextPlayerlock.writeLock();

    AtomicInteger queueSizeAtomic = new AtomicInteger();
    volatile MediaPlayer currentPlayer,  nextPlayer, nextNextPlayer;

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
    ListView<String> dJComments;

    ObservableList<String> observableDJComments;

    @FXML
    MediaView mediaView;

    @FXML
    ProgressBar songProgressBar;

    @FXML
    Slider volumeSlider;

    @FXML
    Button javascript;

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
    Slider timeSlider;

    @FXML
    Label timeLabel;

    private ChangeListener<Duration> progressChangeListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert playButton != null;
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

        setCellFactoryForListViews();
        addMediaViewPropertyListener();

        for(int i =0;i<5;i++)
            boolArray[i] = false;
    }

    private void setCellFactoryForListViews(){
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
                    protected void updateItem(QueueSong queueSong, boolean empty) {
                        super.updateItem(queueSong, empty);
                            setText((empty || queueSong == null) ? null : queueSong.getSong());
                    }
                };
                return cell;
            }
        });

    }

    private void addMediaViewPropertyListener(){
        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                if(newPlayer!= null) {
                    System.out.println("shifting media players\n");
                    writeToCurrentPlayer(newPlayer);
                    writeNextPlayer(nextNextPlayer);

                    if(queueSizeAtomic.get() > 1)
                        SongQueueObservableList.remove(0);//remove case event is fired

                    Platform.runLater( () -> {
                        setCurrentlyPlaying(currentPlayer);
                        currentPlayer.play();
                    });
                }
            }
        });
    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void testing(){

    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void removeSong(){}

    /**
     *  sets the currently playing label to the label of the new media player and updates the progress monitor.
     *  */
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

        System.out.println("nextPlayer set to progress change listener? \n");
    }

    /*******************MUSIC button methods **************************************************/
    @FXML
     private void addSongButtonFunc(ActionEvent event) {
        Task task = new Task<Void>() {
            @Override public Void call() {
                int index1 = songList.getSelectionModel().getSelectedIndex();
                QueueSong qs1 = new QueueSong(SongSelectionObservableList.get(index1), index1-1);
                Platform.runLater( () -> {
                    SongQueueObservableList.add(qs1);
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
                    if(index >0) {
                    QueueSong sq0 = mainModel.createQueueSong(index);
                    Platform.runLater(() -> {
                        SongQueueObservableList.add(sq0);
                    });
                }
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

    public void addObservableSongQueueListener()
    {
        SongQueueObservableList = FXCollections.observableList(mainModel.getSongQueue());
        queueList.setItems(SongQueueObservableList);

        observableDJComments =  FXCollections.observableList(mainModel.getDJCommentsData());

        dJComments.setItems(observableDJComments);

        SongQueueObservableList.addListener(new ListChangeListener<QueueSong>() {
            public void onChanged(ListChangeListener.Change<? extends QueueSong> change)
            {
                while (change.next()) {
                    if (change.wasUpdated()) {
                        for (QueueSong qs : change.getList()) {
                            System.out.println(qs.getSong() + " updated");
                        }
                    } else {
                        for (QueueSong removedSong : change.getRemoved()) {
                            queueSizeAtomic.decrementAndGet();
                            songRemovedfileIOFunc();
                        }

                        for (QueueSong addedSong : change.getAddedSubList()) {
                            queueSizeAtomic.incrementAndGet();
                            songAddedfileIOFunc();
                        }
                    }
                }
            }
        });

        observableDJComments.addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> change)
            {
                while (change.next()) {
                        for (String comment : change.getAddedSubList()) {

                        }
                    }
                }
            });
    }

    /**
     * shifts the media players
     */
    public synchronized void shiftMediaPlayers(MediaPlayer newestPlayer){
        currentPlayer = nextPlayer;
        nextPlayer = newestPlayer;
    }

    public void writeToCurrentPlayer(MediaPlayer newestPlayer){
        CPWwriteLock.lock();
        try{
            currentPlayer = newestPlayer;
        }finally{
            CPWwriteLock.unlock();
        }
    }
    public void writeNextPlayer(MediaPlayer newestPlayer){
        NPWriteLock.lock();
        try{
            nextPlayer = newestPlayer;
        }finally{
            NPWriteLock.unlock();
        }
    }
    public void writeToNextNextPlayer(MediaPlayer newestPlayer){
        NNPWwriteLock.lock();
        try{
            nextNextPlayer = newestPlayer;
        }finally{
            NNPWwriteLock.unlock();
        }
    }

    /**
     * Everytime a song is added in the queue this function is called. It's operation depends on the state of the song queue
     */
    public void songAddedfileIOFunc(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer;
        try
        {
            /** Song added case A: */
            if (queueSizeAtomic.get() == 1 )
            {
                int index = SongQueueObservableList.get(0).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(0).getSong()));
                MediaPlayer freshPlayer = futureMediaPlayer.get();
                Platform.runLater( () ->
                {
                    writeToCurrentPlayer(freshPlayer);
                    setCurrentlyPlaying(currentPlayer);
                    mediaView.setMediaPlayer(currentPlayer);
                });
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            /** Song added case B: */
            else if (queueSizeAtomic.get() == 2)
            {
                int index = SongQueueObservableList.get(1).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(1).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                writeNextPlayer(nextSongPlayer);
                addListenersToNewlyAdded2(currentPlayer, nextPlayer);
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            /** Song added case C: */
            else if (queueSizeAtomic.get() == 3)
            {
                int index = SongQueueObservableList.get(2).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                writeToNextNextPlayer(nextSongPlayer);
                addListenersToNewlyAdded2(nextPlayer, nextSongPlayer);
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        finally {

        }
    }

    /** Called any time a song gets removed. It's operation depends on the state of the song queue*/
    public void songRemovedfileIOFunc(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer;
        try
        {
            /** Remove case B: song removed and next song available*/
            if (queueSizeAtomic.get() == 1)
            {}
            /** Remove case C: song removed and Queue is equal to 2 */
            else if (queueSizeAtomic.get() == 2)
            {
                if(! SongQueueObservableList.get(1).getPreparedBool())
                {}
            }
            /** Remove case C: song removed and Queue is greater than 2 */
            else if (queueSizeAtomic.get() > 2)
            {
                if(! SongQueueObservableList.get(2).getPreparedBool())
                {
                    SongQueueObservableList.get(2).setPreparedBool(true);
                    int index = SongQueueObservableList.get(2).getAzureForeignKey();

                    futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));
                    writeNextPlayer(nextNextPlayer);
                    MediaPlayer newNextNextPlayer = futureMediaPlayer.get();
                    addListenersToNewlyAdded2(nextPlayer,newNextNextPlayer);
                    Platform.runLater( () ->
                    {
                        writeToNextNextPlayer(newNextNextPlayer);
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after future returns a MediaPlayer object
     * adds an end of media listener */
    private void addListenersToNewlyAdded2(MediaPlayer link1, MediaPlayer link2){
        final MediaPlayer link1Final = link1;
        final MediaPlayer link2Final = link2;
        link1Final.setOnEndOfMedia(() ->
        {
            link1Final.currentTimeProperty().removeListener(progressChangeListener);
            link1Final.stop();
            link1Final.dispose();//release file stream link

            /** Remove case B: next song available*/
            if(queueSizeAtomic.get()>1)
            {
                Platform.runLater( () -> {
                    mediaView.setMediaPlayer(link2Final);
                });
            }
        });
    }

    public void addVolumeAndTimeSliderListeners(){
        Platform.runLater( () -> {
            timeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (timeSlider.isValueChanging()) {
                        final MediaPlayer player = mediaView.getMediaPlayer();
                        if (progressChangeListener != null) {
                            player.seek(player.getTotalDuration().multiply(timeSlider.getValue() / 100.0));
                        }
                    }
                }
            });
            volumeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (volumeSlider.isValueChanging()) {
                        final MediaPlayer player = mediaView.getMediaPlayer();
                        player.setVolume(volumeSlider.getValue() / 100.0);
                    }
                }
            });
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
        myController.setScreen(MusicHostFramework.screen1ID);
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen3ID);
    }

    /**
     * Play button function playing and pausing songs
     */
    @FXML
    public void iPlay() {
        if ("Pause".equals(playButton.getText())) {
            Platform.runLater( () -> {
                mediaView.getMediaPlayer().pause();
                playButton.setText("Play");
                playButton.setStyle("-fx-background-color:green");
            });

        } else if (queueSizeAtomic.get()>0){
            /********************************* song 0 requires read access ***********/
            mediaView.setMediaPlayer(currentPlayer);
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
            playButton.setStyle("-fx-background-color:red");
        }
    }

    /**
     * Skip button function for skipping songs in the queue
     */
    @FXML
    public void iSkip() {
        //can't skip unless there is a song to follow
        if(queueSizeAtomic.get() > 1) {
            final MediaPlayer curPlayer = mediaView.getMediaPlayer();
            curPlayer.currentTimeProperty().removeListener(progressChangeListener);
            curPlayer.stop();
            curPlayer.dispose();//remove file stream

            if(nextPlayer!=null)
                mediaView.setMediaPlayer(nextPlayer);

            if ("Play".equals(playButton.getText())) {
                playButton.setText("Pause");
                playButton.setStyle("-fx-background-color:red");
            }
        }
    }

    //interface injection of screenParent and main model for songs and DB
    public void setScreenParent(ScreensController screenParent, Model model){
        myController = screenParent;
        mainModel = model;
    }

    public void stopServer(){
        volatileThread = null;
    }
/**********************************************************************************************************************/

    /**********************************************************************************************************************/

    /**
     * Start's the bluetooth server communicating with the android client
     * @param event user hits button to start or stop server
     */
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

    /**
     * Function called from the start server button
     */
    public void startServer() {
        /*****************************/
        executorService1 = Executors.newSingleThreadExecutor();
        executorService1.submit(() ->
        {
            volatileThread = Thread.currentThread();
            Thread thisThread = Thread.currentThread();
            StreamConnectionNotifier notifier = null;
            StreamConnection connection = null;

            try {
                LocalDevice local = LocalDevice.getLocalDevice();

                //set The inquiry access code for General/Unlimited Inquiry
                local.setDiscoverable(DiscoveryAgent.GIAC);
                UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"

                String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
                notifier = (StreamConnectionNotifier) Connector.open(url);

                thisThread.sleep(1000);
                System.out.println("waiting for connections...");
                connection = notifier.acceptAndOpen();
                System.out.println("connected!");

                while (volatileThread == thisThread)
                {
                    executorService2 = Executors.newSingleThreadExecutor();
                    processThread = new ProcessConnectionThread(connection);
                    executorService2.execute(processThread);

                    try {
                        executorService2.shutdown();
                        executorService2.awaitTermination(20, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        System.err.println("exec 2 tasks interrupted");
                    } finally {
                        if (!executorService2.isTerminated()) {
                            System.err.println("cancel non-finished tasks exec 2");
                        }
                        executorService2.shutdownNow();
                        System.out.println("\nshutdown finished ");
                    }

                    System.out.println("waiting for connections...");
                    //blocking call waiting for client to connect
                    connection = notifier.acceptAndOpen();
                    System.out.println("connected!");
                }

                return;

            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
                return;
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            } finally {

            }
            return;
        });
    }

    /**
     * Server Connection Thread Class
     * */
    public class ProcessConnectionThread implements Runnable, MusicHostInterface {
    private volatile StreamConnection mConnection;
    private volatile Thread volatileThread;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    /**
     * Communication protocol CONSTANTS
     * */
    final int SONG_SELECT = 1;
    final int SONG_SELECTED = 2;
    final int DJ_COMMENT = 3;
    final int SKIP_SONG  = 4;
    final int ECHO_SHARED_PREF_SONGS = 5;
    final int REMOTE_SELECT = 6;
    final int WANT_END = 7;

    /** Constructor */
    public ProcessConnectionThread(StreamConnection connection)
    {
        mConnection = connection;
    }

        @Override
        public void run() {
            volatileThread = Thread.currentThread();
            Thread thisThread = Thread.currentThread();

            boolean endCom = false;

            try {
                dataInputStream = new DataInputStream(mConnection.openInputStream());
                dataOutputStream = new DataOutputStream(mConnection.openOutputStream());
                int whatToDo = dataInputStream.readInt();
                System.out.print("\nread  " + whatToDo);
                thisThread.sleep(300);
                if (dataInputStream.available() > 0) {
                    System.out.print("\ninit data available  " + whatToDo);
                    WhatToDoFunc(whatToDo);
                }
                while (volatileThread == thisThread) {
                    try {
                        //prevent EOF exceptions
                        thisThread.sleep(300);
                        if (dataInputStream.available() > 0) {
                            whatToDo = dataInputStream.readInt();
                            // if readint is outside of com protocol
                            if (whatToDo > 6)
                                whatToDo = -1;
                            System.out.print("\n# data available  = " + whatToDo);
                        }
                        thisThread.sleep(100);
                        if (dataInputStream.available() > 0) {
                            //in case readint read number outside of communication protocol
                            if (whatToDo > 0) {
                                WhatToDoFunc(whatToDo);
                                // "SONG_SELECT" is a special case in com protocol where client requests to see song selection
                                // and needs com to stay open to pick a song
                                if (whatToDo != 1)
                                    endCom = true;
                                whatToDo = -1;
                            }
                        }
                        if (endCom) {
                            volatileThread = null;
                            return;
                        }

                    } catch (InterruptedException e) {
                        System.out.print("\nexited through here callable interrupted");
                        //  Thread.currentThread().interrupt();
                    } finally {
//                    dataInputStream.close();
//                    dataOutputStream.close();
//                    mConnection.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ;
        }

    public synchronized void WhatToDoFunc(int whatToDo)
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
                send3(recv3());
                break;
            case SKIP_SONG:
                System.out.print("\n got 4");
                recv4();
                send4();
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

        /**
         * searches selection for a match
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
            });
        }

        /**
         *
         * @param song song selected by android client
         */
        public synchronized void ControllerAddSong(String song){
            Platform.runLater(() -> {
                searchSelectionForMatch(song);
            });
        }

        /**
         *
         * @param DJComment The DJ comment from the android client
         */
        public synchronized void ControllerGetDJComments(String DJComment){
            Platform.runLater(() -> {
                observableDJComments.add(DJComment);
            });
        }



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
        String selectionAndQueue = mainModel.songSelectionToJson() + '&' + mainModel.songQueueToJson();
        sendMessageByBluetooth(selectionAndQueue, 1);
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
        sendMessageByBluetooth("The song " + msg + " has been added to the queue", 2);
    }

    /**
     * Received selected song from client
     */
    @Override
    public String recv2() {
        String msg2 = procInput();
        if(msg2!=null) {
            //if( ControllerAddSong(msg2))-> return msg, else return "bad"
            ControllerAddSong(msg2);
        }
        return msg2;
    }

    /**
     * Send song DJ comment history to client
     */
    @Override
    public void send3(String msg) {
        Platform.runLater( () -> {
            observableDJComments.add(msg);
            String selectionAndQueue = mainModel.DJCommentToJson() + '&' + mainModel.songQueueToJson();
            sendMessageByBluetooth(selectionAndQueue, DJ_COMMENT);
        });
    }

    /**
     * Received DJ comment from client
     */
    @Override
    public String recv3() {
        return procInput();
    }

    /**
     * Send skip ok to client
     */
    @Override
    public void send4() {
            Platform.runLater( () -> {
                String DJCommentsAndQueue = mainModel.DJCommentToJson() + '&' + mainModel.songQueueToJson();
                sendMessageByBluetooth(DJCommentsAndQueue, SKIP_SONG);
            });
    }

    /**
     * Received skip request from client
     */
    @Override
    public void recv4() {
        Platform.runLater( () -> {
            if(SongQueueObservableList.get(0).decrementAndGetVotes()==0)
                iSkip();
        });
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

    public synchronized void ControllerEchoSharedPreferencesSongs(String songs) {
        Platform.runLater(() -> {
            System.out.print("\n test 5");
            iSkip();
        });
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

    /** Boolean button for song request option*/
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

    /** Boolean button for DJ comment option*/
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

    /** Boolean button for skip song option*/
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

    /** Boolean button for shared preferences echo option*/
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

    /** Boolean button for skip song option for remote control feature*/
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

    /** Formats time for the label of the song playing on the GUI*/
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