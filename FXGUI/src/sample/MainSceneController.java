package sample;
import java.io.*;
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
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/***********************
 * Author: Thomas Flynn
 * date: 25/04/16
 **********************/


/**
 * MainSceneController that reacts to the user's interface control by changing the model and displaying that change in the view.
 *
 * Features:
 * 1- Initialise button: Sets up the application for running.
 * 2- Server button: Enables/disables bluetooth server connection thread using Bluecove for connecting Android application clients.
 * 3- Song request button: Enables/disables client's ability to request a song.
 * 4- DJ comment button: Enables/disables client's ability to make a comment to the DJ.
 * 5- Skip Enable button: Enables/disables client's ability to skip the current song playing.
 *
 * 6- Add button: User can select a song to add to the queue.
 * 7- Play button: Plays/pauses the current song.
 * 8- Skip button: User can skip the current song.
 * 9- Volume slider: Adjusts the volume of the song playing.
 * 10- Time slider: Adjusts the progress of the song playing.
 * 11- Song progress bar: Displays the progress of the song playing.
 * 12- Time label: Displays the time left in the song playing.
 * 13- MediaView displays audio spectrum of the song playing.
 *
 * 14- Logout button- Allows the user log out.
 */
public class MainSceneController implements Initializable , ControlledScreen {
    Model mainModel;
    ScreensController myController;
    ProcessConnectionThread processThread;
    Boolean[] boolArray = new Boolean[3];
    private volatile Thread volatileThread;
    ExecutorService executorService1;
    ExecutorService executorService2;
    boolean serverStartFlag = false;

    private final ReadWriteLock currentPlayerlock = new ReentrantReadWriteLock();
    private final Lock CPReadLock = currentPlayerlock.readLock();
    private final Lock CPWwriteLock = currentPlayerlock.writeLock();

    private final ReadWriteLock nextPlayerlock = new ReentrantReadWriteLock();
    private final Lock NPReadLock = nextPlayerlock.readLock();
    private final Lock NPWriteLock = nextPlayerlock.writeLock();

    private final ReadWriteLock nextNextPlayerlock = new ReentrantReadWriteLock();
    private final Lock NNPWwriteLock = nextNextPlayerlock.writeLock();

    AtomicInteger queueSizeAtomic = new AtomicInteger();
    volatile MediaPlayer currentPlayer,  nextPlayer, nextNextPlayer;

//   @FXML
//    ProgressBar progBar;

//    @FXML
//    ProgressIndicator prog;

    @FXML
    Button playButton;

    @FXML
    Button initbtn;

    @FXML
    Button addbtn;

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
    Button logOutButton;

    @FXML
    Button boolRequest;

    @FXML
    Button boolDJComment;

    @FXML
    Button boolSkip;

    @FXML
    Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    Button serverButton;

    @FXML
    Slider timeSlider;

    @FXML
    Label timeLabel;

    private ChangeListener<Duration> progressChangeListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setGUIOptions();
        setCellFactoryForListViews();
        addMediaViewPropertyListener();
       // progBar.setProgress(0);
    }

    /**
     * for setting GUI properties upon initialization and logging out
     */
    public void setGUIOptions(){

        for(int i =0;i<3;i++)
            boolArray[i] = false;

        boolRequest.setText("OFF");
        boolRequest.setStyle("-fx-background-color:red");

        boolDJComment.setText("OFF");
        boolDJComment.setStyle("-fx-background-color:red");

        boolSkip.setText("OFF");
        boolSkip.setStyle("-fx-background-color:red");

        playButton.setStyle("-fx-background-color:red");
        serverButton.setStyle("-fx-background-color:red");
        initbtn.setStyle("-fx-background-color:green");

    }


    /**
     * The listView cells have to be customized for the individual SelectionSong and QueueSong objects
     */
    private void setCellFactoryForListViews(){
        songList.setCellFactory(new Callback<ListView<SelectionSong>, ListCell<SelectionSong>>(){
            @Override
            public ListCell<SelectionSong> call(ListView<SelectionSong> p) {
                ListCell<SelectionSong> cell = new ListCell<SelectionSong>(){
                    @Override
                    protected void updateItem(SelectionSong selectionSong, boolean empty) {
                        super.updateItem(selectionSong, empty);
                        setText((empty || selectionSong == null) ? null : selectionSong.getSong() + " by " + selectionSong.getArtist());
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

    /**
     * Add listener for the mediaView which acts as the container for playing songs
     */
    private void addMediaViewPropertyListener(){
        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                if(newPlayer!= null) {
                    System.out.println("shifting media players\n");
                    writeToCurrentPlayer(newPlayer);
                    writeToNextPlayer(nextNextPlayer);

                    if(queueSizeAtomic.get() > 1)
                        SongQueueObservableList.remove(0);//remove case event is fired

                    Platform.runLater( () -> {
                        setCurrentlyPlaying(currentPlayer);
                        currentPlayer.play();
                    });
                }
            }
        });

        mediaView.getEffect();
    }

    @FXML
    private void testing(){

    }

    @FXML          /*********%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%*/
    private void removeSong(){}

    /**
     * sets the currently playing label to the label of the new media player and updates the progress monitor.
     * @param newPlayer the new player
     */
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

    /**
     * Grabs the index of the highlighted selection song in the Listview
     * @param event user presses add song button
     */
    @FXML
     private void addSongButtonFunc(ActionEvent event)
    {
        int index = songList.getSelectionModel().getSelectedIndex();
        addSongTask(index);
    }

    /**
     * Creates a light weight QueueSong object by putting the SelectionSong into the QueueSong constructor
     * @param selectionSongIndex index of selectionSong list
     */
    public void addSongTask(int selectionSongIndex)
    {
        if(selectionSongIndex != -1) {
            final int index = selectionSongIndex;

            QueueSong qs0 = new QueueSong(SongSelectionObservableList.get(index), index);
            Platform.runLater(() -> {
                SongQueueObservableList.add(qs0);
            });
        }
    }

    /**
     * Sets up the requirements for running the application that could not be run at compile time due to null pointer exceptions
     * @param event User initializes the application
     */
    @FXML
    private void init(ActionEvent event)
    {
        //init button can only be pressed once
        if("-fx-background-color:green".equals(initbtn.getStyle())) {
            initbtn.setStyle("-fx-background-color:red");
            //Listeners cannot be called in initialize because mainmodel throws a null pointer exception
            addFXObservableListeners();
            addVolumeAndTimeSliderListeners();
            observableDJComments.addAll("Bob: I love this song!", "Jane: I hate this song!");
            // create a list of SelectionSong objects from the database
            initSongSelection();
        }
    }


    /**
     * Add listeners for the observable song queue and DJ comments list view
     */
    public void addFXObservableListeners()
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
                        songRemovedfileIOFunc(removedSong);
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
     * Acquire lock for writing to the current player
     * @param nextPlayer the next player
     */
    public void writeToCurrentPlayer(MediaPlayer nextPlayer){
        CPWwriteLock.lock();
        try{
            currentPlayer = nextPlayer;
        }finally{
            CPWwriteLock.unlock();
        }
    }

    /**
     * Acquire lock for writing to the nextPlayer
     * @param nextNextPlayer the nextNextPlayer
     */
    public void writeToNextPlayer(MediaPlayer nextNextPlayer){
        NPWriteLock.lock();
        try{
            nextPlayer = nextNextPlayer;
        }finally{
            NPWriteLock.unlock();
        }
    }

    /**
     * Acquire lock for writing to the nextNextPlayer
     * @param newestPlayer the newest player
     */
    public void writeToNextNextPlayer(MediaPlayer newestPlayer){
        NNPWwriteLock.lock();
        try{
            nextNextPlayer = newestPlayer;
        }finally{
            NNPWwriteLock.unlock();
        }
    }

    /**
     * Called any time a song is added to the queue. It's operation depends on the state of the song queue.
     *
     * Song added case A:
     * 1- Write to the current player
     * 2- Add a special case end of media listener for last song in the queue
     *
     * Song added case B:
     * 1- Write to the next player
     * 2- Add normal end of media listener to the current player that links current player to the next player
     *
     * Song added case C:
     * 1- Write to the nextNextPlayer
     * 2- Add normal end of media listener that links the next player to the nextNextPlayer that links to the nextNextPlayer
     */
    public void songAddedfileIOFunc(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer;
        try
        {
            /** Song added case A:
             * 1- Write to the current player
             * 2- Add a special case end of media listener for last song in the queue*/
            if (queueSizeAtomic.get() == 1 )
            {
                int index = SongQueueObservableList.get(0).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(0).getSong()));
                MediaPlayer freshPlayer = futureMediaPlayer.get();
                Platform.runLater( () ->
                {
                    writeToCurrentPlayer(freshPlayer);
                    setCurrentlyPlaying(currentPlayer);
                    addAmITheLastSong(currentPlayer);
                    //when set, the mediaView listener kicks of the first song
                    mediaView.setMediaPlayer(currentPlayer);
                });
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            /** Song added case B:
             * 1- Write to the next player
             * 2- Add normal end of media listener to the current player that links current player to the next player*/
            else if (queueSizeAtomic.get() == 2)
            {
                int index = SongQueueObservableList.get(1).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(1).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                Platform.runLater( () ->
                {
                writeToNextPlayer(nextSongPlayer);
                addEndOfMediaListener(currentPlayer, nextPlayer);
                });
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            /** Song added case C:
             * 1- Write to the nextNextPlayer
             * 2- Add normal end of media listener that links the next player to the nextNextPlayer that links to the nextNextPlayer*/
            else if (queueSizeAtomic.get() == 3)
            {
                int index = SongQueueObservableList.get(2).getAzureForeignKey();
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                Platform.runLater( () ->
                {
                writeToNextNextPlayer(nextSongPlayer);
                addEndOfMediaListener(nextPlayer, nextSongPlayer);
                });
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

    /**
     * Called any time a song is removed from the queue. It's operation depends on the state of the song queue.
     *
     * Remove case A: song removed and no next song available.
     * 1- Delete the file associated with the song removed.
     *
     * Remove case B: song removed and no next song available.
     * 1- Add am I the last song listener to the current player.
     * 2- Delete the file associated with the song removed.
     *
     * Remove case C: song removed and no next song available.
     * 1- Delete the file associated with the song removed.
     *
     * Remove case D: song removed and Queue is greater than 2
     * 1- Check the next next song in the queue to see if it has created a media player for itself
     * 2- Executor service future creates the file required for the nextNextPlayer and returns a mediaPlayer object
     * 3- While future is running, delete the file associated with the song removed
     * 4- Get the future MediaPlayer and add end of media Player to it
     * 5- Write the future MediaPlayer to the nextNextPlayer
     *
     * @param removedSong the song that has just end or skipped.
     */
    public void songRemovedfileIOFunc(QueueSong removedSong){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer;
        try
        {
            /** Remove case A: song removed and no next song available
             * 1- Delete the file associated with the song removed*/
            if (queueSizeAtomic.get() == 0){
                deleteRemovedSongFile(removedSong.getSong());
            }
            /** Remove case B: song removed and no next song available
             * 1- Add am I the last song listener to the current player
             * 2- Delete the file associated with the song removed*/
            else if (queueSizeAtomic.get() == 1){
                addAmITheLastSong(currentPlayer);
                deleteRemovedSongFile(removedSong.getSong());
            }
            /** Remove case C: song removed and no next song available
             * 1- Delete the file associated with the song removed*/
            else if (queueSizeAtomic.get() == 2){
                deleteRemovedSongFile(removedSong.getSong());
            }

            /** Remove case D: song removed and Queue is greater than 2
             * 1- Check the next next song in the queue to see if it has created a media player for itself
             * 2- Executor service future creates the file required for the nextNextPlayer and returns a mediaPlayer object
             * 3- While future is running, delete the file associated with the song removed
             * 4- Get the future MediaPlayer and add end of media Player to it
             * 5- Write the future MediaPlayer to the nextNextPlayer*/
            else if (queueSizeAtomic.get() > 2)
            {
                if(! SongQueueObservableList.get(2).getPreparedBool())
                {
                    SongQueueObservableList.get(2).setPreparedBool(true);
                    int index = SongQueueObservableList.get(2).getAzureForeignKey();

                    futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));

                    deleteRemovedSongFile(removedSong.getSong());
                    writeToNextPlayer(nextNextPlayer);

                    MediaPlayer newNextNextPlayer = futureMediaPlayer.get();
                    addEndOfMediaListener(nextPlayer, newNextNextPlayer);
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
     * Deletes the file associated with the last song played
     * @param songName name of the file
     */
    public void deleteRemovedSongFile(String songName) {
        String filePath = "C:\\test\\"+songName+".mp3";
        File file = new File(filePath);
        try {
            OutputStream targetFile=
                new FileOutputStream(
                    "C:\\test\\"+songName+".mp3");

            targetFile.write(0);
            targetFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(file.delete()){
            System.out.println(file.getName() + " is deleted!");
        }else{
            System.out.println(file.getName() + " Delete operation failed.");
        }
    }


    /**
     * Called after future returns a MediaPlayer object.
     * Adds an end of media listener to the mediaPlayer.
     * @param link1 the mediaPlayer that receives the end of media listener
     * @param link2 the mediaPlayer that link1 goes to when it end's
     */
    private void addEndOfMediaListener(MediaPlayer link1, MediaPlayer link2){
        final MediaPlayer link1Final = link1;
        final MediaPlayer link2Final = link2;
        link1Final.setOnEndOfMedia(() ->
        {
            link1Final.currentTimeProperty().removeListener(progressChangeListener);
            link1Final.stop();
            link1Final.dispose();//release file stream link

            if(queueSizeAtomic.get()>1)
            {
                Platform.runLater( () -> {
                    //mediaView listener will be triggered which will remove it's old player in place for the new player
                    mediaView.setMediaPlayer(link2Final);
                });
            }
        });
    }

    /**
     * Special case end of media listener for the last song in the queue
     * @param link1 the last song in the queue
     */
    private void addAmITheLastSong(MediaPlayer link1){
        final MediaPlayer link1Final = link1;
        link1Final.setOnEndOfMedia(() ->
        {
            link1Final.currentTimeProperty().removeListener(progressChangeListener);
            link1Final.stop();
            link1Final.dispose();//release file stream link

            /** Remove case D: next song available
             *  OR
             *  Add case A: first song in the queue*/
            if(queueSizeAtomic.get()==1)
            {
                Platform.runLater( () -> {
                    //remove file association for deletion
                    mediaView.getMediaPlayer().dispose();
                    timeLabel.setText(null);
                    //triggers remove case A: just delete the removed song
                    SongQueueObservableList.remove(0);
                });
            }
        });
    }

    /**
     * adds Volume and Time Slider Listeners for the GUI controls
     */
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

    /**
     * Initialize the song selection listview with songs from the database
     */
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
        new Thread(task).start();
    }

    /**
     * Logs the user out.
     * @param event User hits the button to log out
     */
    @FXML
    private void logOut(ActionEvent event) {
        synchronized(this) {
            Platform.runLater( () -> {
                initbtn.setStyle("-fx-background-color:green");
                System.out.println("removing everything");
                mediaView.getMediaPlayer().stop();
                mediaView.getMediaPlayer().dispose();
            });

            Task task = new Task<Void>()
            {
                @Override public Void call() {
                    try
                    {
                        deleteRemovedSongFile(SongQueueObservableList.get(0).getSong());
                        clearValuesBeforeLogginOut();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            new Thread(task).start();

            Platform.runLater( () -> {
                myController.setScreen(MusicHostFramework.screen1ID);
            });
        }
    }

    /**
     * clears GUI and model values before loggin out
     */
    public void clearValuesBeforeLogginOut(){

       if(serverStartFlag == true)
            stopServer();


        synchronized(this) {
            for (int i = 0; i < SongSelectionObservableList.size(); i++) {
                Platform.runLater( () -> {
                SongSelectionObservableList.remove(0);
                });
            }

            for (int i = 0; i < observableDJComments.size(); i++)
                Platform.runLater( () -> {
                observableDJComments.remove(0);
            });
            }

        for (int i = 0; i < SongQueueObservableList.size(); i++){
            Platform.runLater( () -> {
            SongQueueObservableList.remove(0);
            });
        }

        Platform.runLater( () -> {
            timeLabel.setText("");
            setGUIOptions();
        });

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

    /**
     * Interface injection of screenParent and main model for songs and DB
     * @param screenParent set the current screen parent
     * @param model set the reference to the model object
     */
    public void setScreenParent(ScreensController screenParent, Model model){
        myController = screenParent;
        mainModel = model;
    }

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
        else if(serverStartFlag==false && "OFF".equals(serverButton.getText())){
            serverButton.setStyle("-fx-background-color:green");
            serverButton.setText("ON");
            startServer();
        }
        else if("OFF".equals(serverButton.getText())){
            try {
                LocalDevice local = LocalDevice.getLocalDevice();
                local.setDiscoverable(DiscoveryAgent.GIAC);
            } catch (BluetoothStateException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Sets local bluetooth device not_discoverable
     */
    public void stopServer(){

        try {
            LocalDevice local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
        } catch (BluetoothStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function called from the start server button
     */
    public void startServer() {
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
                        //time allocated for the android client to make a choice
                        executorService2.awaitTermination(30, TimeUnit.SECONDS);
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
    final int OPTIONS = 0;
    final int SONG_SELECT = 1;
    final int SONG_SELECTED = 2;
    final int DJ_COMMENT = 3;
    final int SKIP_SONG  = 4;

    /** Constructor */
    public ProcessConnectionThread(StreamConnection connection)
    {
        mConnection = connection;
    }

        /** Runnable*/
        @Override
        public void run() {
            volatileThread = Thread.currentThread();
            Thread thisThread = Thread.currentThread();

            boolean endCom = false;

            try {
                dataInputStream = new DataInputStream(mConnection.openInputStream());
                dataOutputStream = new DataOutputStream(mConnection.openOutputStream());
                int whatToDo = dataInputStream.readInt();

                thisThread.sleep(300);
                if (dataInputStream.available() > 0) {
                    WhatToDoFunc(whatToDo);
                }
                while (volatileThread == thisThread) {
                    try {
                        //prevent EOF exceptions
                        thisThread.sleep(300);
                        if (dataInputStream.available() > 0) {
                            whatToDo = dataInputStream.readInt();
                            // if readint is outside of com protocol
                            if (whatToDo > 4)
                                whatToDo = -1;
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

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }


    /**
     * Determines what functions should execute depending on the read int value from the connection thread
     * @param whatToDo the read int value for the switch statement
     */
    public synchronized void WhatToDoFunc(int whatToDo)
    {
        switch(whatToDo)
        {
            case OPTIONS:
                AvailableOptionsTx(AvailableOptionsRx());
                break;
            case SONG_SELECT:
                SongSelectionTx(SongSelectionRx());
                break;
            case SONG_SELECTED:
                SongSelectedTx(SongSelectedRx());
                break;
            case DJ_COMMENT:
                DJCommentTx(DJCommentRx());
                break;
            case SKIP_SONG:
                SkipSongRx();
                SkipSongTX();
                break;
        }
    }

    /**
     * Reads data input stream
     * @return Returns information read from the data input stream
     */
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

    /**
     * Communication interface for transmission through the data output stream
     * @param msg The message to send
     * @param whatToDo What the receiver should do with the message
     * @return
     */
    @Override
    public boolean sendMessageByBluetooth(String msg,int whatToDo)
    {
        try
        {
            if(dataOutputStream != null){
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

    /**
     * searches selection for a match using java 8 stream API replacement of for each
     * @param songToSearch string of song to search
     * @return the index of the matching song
     */
    public int searchSelectionForMatch(String songToSearch){
        //java 8 stream API replacement of for each
        int [] indices = IntStream.range(0, SongSelectionObservableList.size())
            .filter(i -> songToSearch.equals(SongSelectionObservableList.get(i).getSong()))
            .toArray();

        if(indices.length == 1)
        {
            return indices[0];
        }
        return -1;
    }


    /**
     * searches song queue for a match using java 8 stream API replacement of for each
     * @param songToSearch string of song to search
     * @return the index of the matching song
     */
    public int searchQueueForMatch(String songToSearch)
    {
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

    /**
     *
     * @param song song selected by android client
     */
    public synchronized void ControllerAddSong(String song){

    }

    /**
     * Send options selecting to client
     */
    @Override
    public void AvailableOptionsTx(String msg) {
        System.out.print(msg);
        sendMessageByBluetooth(msg, 0);
    }

    /**
     * Received a Music Host Client
     */
    @Override
    public String AvailableOptionsRx() {
        String strings = Arrays.toString(boolArray);
        System.out.print(strings);
        return strings;
    }
    /**
     * Send song selecting to client
     */
    @Override
    public void SongSelectionTx(String msg) {
        String selectionAndQueue = mainModel.songSelectionToJson() + '&' + mainModel.songQueueToJson();
        sendMessageByBluetooth(selectionAndQueue, 1);
    }

    /**
     * Received view song selection request from client
     */
    @Override
    public String SongSelectionRx() {
        return procInput();
    }

    /**
     * Send song request ok to client
     */
    @Override
    public void SongSelectedTx(String msg) {
        sendMessageByBluetooth("The song " + msg + " has been added to the queue", 2);
    }

    /**
     * Received selected song from client
     */
    @Override
    public String SongSelectedRx() {
        String song = procInput();
        if(song!=null) {
            Platform.runLater(() -> {
                searchSelectionForMatch(song);
                addSongTask(searchSelectionForMatch(song));
            });
        }
        return song;
    }

    /**
     * Send song DJ comment history to client
     */
    @Override
    public void DJCommentTx(String msg) {
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
    public String DJCommentRx() {
        return procInput();
    }

    /**
     * Send skip ok to client
     */
    @Override
    public void SkipSongTX() {
            Platform.runLater( () -> {
                String DJCommentsAndQueue = mainModel.DJCommentToJson() + '&' + mainModel.songQueueToJson();
                sendMessageByBluetooth(DJCommentsAndQueue, SKIP_SONG);
            });
    }

    /**
     * Received skip request from client
     */
    @Override
    public void SkipSongRx() {
        Platform.runLater( () -> {
            if(SongQueueObservableList.get(0).decrementAndGetVotes()==0)
                iSkip();
        });
    }
}//end connection thread class

    /** Boolean button for song request option*/
    @FXML
    private void setSongRequestBool(){
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
    private void setDJCommentBool(){
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
    private void setSkipSongBool(){
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