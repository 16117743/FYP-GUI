package com.framework;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;
import com.Interface.ControlledScreen;
import com.Interface.MusicHostCommunication;
import com.util.HandleFileIO;
import com.util.QueueSong;
import com.util.SelectionSong;
import javafx.animation.PathTransition;
import javafx.animation.PathTransitionBuilder;
import javafx.animation.Timeline;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.util.Callback;
import javafx.util.Duration;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/***********************
 * Author: Thomas Flynn
 * Final Year Project: Music Host Interface
 * date: 25/04/16
 **********************/

/**
 * MainSceneController that reacts to the UI control by changing the com.model and displaying that change in this controller's  view.<br>
 *<br>
 * Features:<br>
 * 1- Initialise button: Sets up the application for running.<br>
 * 2- Server button: Enables/disables bluetooth server connection thread using Bluecove for connecting Android application clients.<br>
 * 3- Song request button: Enables/disables client's ability to request a song.<br>
 * 4- DJ comment button: Enables/disables client's ability to make a comment to the DJ.<br>
 * 5- Skip Enable button: Enables/disables client's ability to skip the current song playing.<br>
 * 6- Add button: User can select a song to add to the queue.<br>
 * 7- Play button: Plays/pauses the current song.<br>
 * 8- Skip button: User can skip the current song.<br>
 * 9- Volume slider: Adjusts the volume of the song playing.<br>
 * 10- Time slider: Adjusts the progress of the song playing.<br>
 * 11- Song progress bar: Displays the progress of the song playing.<br>
 * 12- Time label: Displays the time left in the song playing.<br>
 * 13- MediaView displays audio spectrum of the song playing.<br>
 * 14- Logout button- Allows the user log out.<br>
 */

public class MainSceneController implements Initializable , ControlledScreen {
    ScreensController myController;
    ProcessConnectionThread processThread;
    Boolean[] boolOptionsArray = new Boolean[3];
    private volatile Thread volatileThread;
    ExecutorService executorService1;
    ExecutorService clientExecutor;
    boolean serverStartFlag = false;
    byte[] nextNextPlayerBytes;
    String nextNextPlayerString;

    private final ReadWriteLock currentPlayerlock = new ReentrantReadWriteLock();
    private final Lock CPReadLock = currentPlayerlock.readLock();
    private final Lock CPWwriteLock = currentPlayerlock.writeLock();

    private final ReadWriteLock nextPlayerlock = new ReentrantReadWriteLock();
    private final Lock NPReadLock = nextPlayerlock.readLock();
    private final Lock NPWriteLock = nextPlayerlock.writeLock();

    private final ReadWriteLock nextNextPlayerByteslock = new ReentrantReadWriteLock();
    private final Lock NNPWwriteLock = nextNextPlayerByteslock.writeLock();

    AtomicInteger queueSizeAtomic = new AtomicInteger();
    volatile MediaPlayer currentPlayer,  nextPlayer;

    PathTransition addSongPathTransition;
    Path addSongPath;
    volatile boolean addAnimationFin = true;
    volatile boolean skipOK = true;
    volatile boolean loggingOut = false;
    ListChangeListener queueListener;

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
    ListView selectionView;

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
    Button skipButton;

    @FXML
    Button serverButton;

    @FXML
    Slider timeSlider;

    @FXML
    Label timeLabel;

    @FXML
    Circle progressBall;

    @FXML
    Circle playBall;

    @FXML
    ProgressBar songProgBar;

    private PathTransition pathTransitionCircle;

    private Path playPath;

    private ChangeListener<Duration> progressChangeListener;

    private ChangeListener<MediaPlayer> endOfMediaListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setGUIOptions();
        setCellFactoryForListViews();
        addMediaViewPropertyListener();
        addVolumeAndTimeSliderListeners();
        buildSongPlayAnimation();
    }

    private void buildSongPlayAnimation(){
        playPath = createEllipsePathForPlayBall(25, 55, 40, 40, 0);

        pathTransitionCircle = PathTransitionBuilder.create()
            .duration(Duration.seconds(1))
            .path(playPath)
            .node(playBall)
            .orientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT)
            .cycleCount(Timeline.INDEFINITE)
            .autoReverse(false)
            .build();
    }

    private Path createEllipsePathForPlayBall(double centerX, double centerY, double radiusX, double radiusY, double rotate) {
        ArcTo arcTo = new ArcTo();
        arcTo.setX(centerX - radiusX + 1); // to simulate a full 360 degree celcius circle.
        arcTo.setY(centerY - radiusY);
        arcTo.setSweepFlag(true);
        arcTo.setLargeArcFlag(true);
        arcTo.setRadiusX(radiusX);
        arcTo.setRadiusY(radiusY);
        arcTo.setXAxisRotation(rotate);

        Path path = PathBuilder.create()
            .elements(
                new MoveTo(centerX - radiusX, centerY - radiusY),
                arcTo,
                new ClosePath()) // close 1 px gap.
            .build();
        path.setStroke(Color.DODGERBLUE);
        path.getStrokeDashArray().setAll(5d, 5d);
        return path;
    }

    /**
     * for setting GUI properties upon initialization and logging out
     */
    public void setGUIOptions(){
        for(int i =0;i<3;i++)
        boolOptionsArray[i] = false;

        boolRequest.setText("OFF");
        boolRequest.setStyle("-fx-background-color:red");

        boolRequest.setTooltip(new Tooltip("Enable/Disable Android Song Requests"));

        boolDJComment.setText("OFF");
        boolDJComment.setStyle("-fx-background-color:red");

        boolDJComment.setTooltip(new Tooltip("Enable/Disable Android DJ comments"));

        boolSkip.setText("OFF");
        boolSkip.setStyle("-fx-background-color:red");

        boolSkip.setTooltip(new Tooltip("Enable/Disable Android skip song votes"));

        skipButton.setStyle("-fx-background-color:deeppink");
        skipButton.setTooltip(new Tooltip("Skip a Song"));

        playButton.setStyle("-fx-background-color:darkred");

        playButton.setTooltip(new Tooltip("Play/Pause Song"));

        if(!serverStartFlag)
            serverButton.setStyle("-fx-background-color:red");

        serverButton.setTooltip(new Tooltip("Enable/Disable Bluetooth server for Android Clients"));

        initbtn.setStyle("-fx-background-color:green");
        initbtn.setTooltip(new Tooltip("Initialize the application"));
    }

    /**
     * Skip button function for skipping songs in the queue
     */
    @FXML
    public synchronized void iSkip()
    {
        //can't skip unless there is a song to follow and next song is ready to be played
        if(queueSizeAtomic.get() > 1 && skipOK)
        {
            if(mediaView.getMediaPlayer()!=null)
            {
                Platform.runLater( () -> {
                    final MediaPlayer curPlayer = mediaView.getMediaPlayer();
                    curPlayer.stop();
                    mediaView.setMediaPlayer(nextPlayer);
                });
            }

            if ("Play".equals(playButton.getText()))
            {
                Platform.runLater(() -> {
                    playButton.setText("Pause");
                    playButton.setStyle("-fx-background-color:red");
                });
            }
        }
    }

    /**
     * The listView cells have to be customized for the individual SelectionSong and QueueSong objects
     */
    private void setCellFactoryForListViews(){
        selectionView.setCellFactory(new Callback<ListView<SelectionSong>, ListCell<SelectionSong>>() {
            @Override
            public ListCell<SelectionSong> call(ListView<SelectionSong> p) {
                ListCell<SelectionSong> cell = new ListCell<SelectionSong>() {
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
                    //writeToNextPlayer(nextNextPlayer);
                    Platform.runLater( () -> {
                        if(queueSizeAtomic.get() > 1) {
                            oldPlayer.stop();
                            oldPlayer.dispose();
                            oldPlayer.onEndOfMediaProperty().unbind();
                            if(!loggingOut)
                                SongQueueObservableList.remove(0);//remove case event is fired
                        }
                            System.out.println("MediaView Listener triggered\n");
                            writeToCurrentPlayer(newPlayer);
                            setCurrentlyPlaying(newPlayer);
                            currentPlayer.play();
                            pathTransitionCircle.play();
                    });
                }
            }
        });
        mediaView.getEffect();
    }

    /**
     * sets the currently playing time label to the label of the new media player and updates the progress monitor.
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

        //System.out.println("nextPlayer set to progress change listener? \n");
    }

    /**
     * Grabs the index of the highlighted selection song in the Listview
     * @param event user presses add song button
     */
    @FXML
     private synchronized void addSongButtonFunc(ActionEvent event)
    {
        int index = selectionView.getSelectionModel().getSelectedIndex();
        addSongTask(index);
    }

    /**
     * Sets up the animation for adding a song
     */
    public synchronized void SongAnimationSetup(QueueSong addedSong){
        addSongPath = new Path();
        addSongPath.getElements().add(new MoveTo(180.0,-20.0));

        addSongPath.getElements().add(new CubicCurveTo(150.0, -400.0, -200, -60, -200, (-420 + (queueSizeAtomic.get()*23))));
        addSongPathTransition = new PathTransition();
        addSongPathTransition.setDuration(Duration.millis(4000));
        addSongPathTransition.setPath(addSongPath);
        addSongPathTransition.setNode(progressBall);
        addSongPathTransition.setOrientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
        addSongPathTransition.setCycleCount(1);
        addSongPathTransition.setAutoReverse(false);

        addSongPathTransition.setOnFinished(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent arg0) {
                //add song at the end of the animation
                progressBall.setFill(Color.GOLD);
                addAnimationFin = true;
                if (addedSong != null)
                    SongQueueObservableList.add(addedSong);
            }
        });
    }

    /**
     * Creates a light weight QueueSong object by putting the SelectionSong into the QueueSong constructor
     * @param selectionSongIndex index of selectionSong list
     * @return if the song is already in the queue return false
     */
    public synchronized boolean addSongTask(int selectionSongIndex)
    {
        // if this song is not already in the queue, then add it
        if(SongSelectionObservableList!=null && selectionSongIndex >-1) {
            String songToSearch = SongSelectionObservableList.get(selectionSongIndex).getSong();
            //if the selected song isn't already in the queue, add it
            if (searchQueueForMatch(songToSearch) == false) {

                final int index = selectionSongIndex;

                QueueSong newQueueSong = new QueueSong(SongSelectionObservableList.get(index), index);
                if(addAnimationFin)
                {
                    addAnimationFin = false;
                    Platform.runLater(() -> {
                        progressBall.setFill(Color.DEEPSKYBLUE);
                        progressBall.setVisible(true);
                        SongAnimationSetup(newQueueSong);
                        skipOK = false;
                        addSongPathTransition.play();
                    });
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * searches selection for a match using java 8 stream API replacement of for each
     * @param songToSearch string of song to search
     * @return the index of the matching song
     */
    public synchronized int searchSelectionForMatch(String songToSearch){
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
     * @return if it found a song already in the queue or not
     */
    public synchronized boolean searchQueueForMatch(String songToSearch)
    {
        //java 8 stream API replacement of for each
            int [] indices = IntStream.range(0, SongQueueObservableList.size())
                .filter(i -> songToSearch.equals(SongQueueObservableList.get(i).getSong()))
                .toArray();

            //if it found a unique match
            if(indices.length == 1){
                //found a song in the queue
                return true;
            }

        return false;
    }

    public synchronized int searchQueueForIndex(String songToSearch)
    {
        //java 8 stream API replacement of for each
        int [] indices = IntStream.range(0, SongQueueObservableList.size())
            .filter(i -> songToSearch.equals(SongQueueObservableList.get(i).getSong()))
            .toArray();

        //if it found a unique match
        if(indices.length == 1){
            //found a song in the queue
            return indices[0];
        }

        return -1;
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
            //Listeners cannot be called in initialize because myController.model throws a null pointer exception
            addFXObservableListeners();

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
    SongQueueObservableList = FXCollections.observableList(myController.getSongQueue());
    queueList.setItems(SongQueueObservableList);

    observableDJComments =  FXCollections.observableList(myController.getDJCommentsData());

    dJComments.setItems(observableDJComments);

        queueListener = new ListChangeListener<QueueSong>() {
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
        };

    SongQueueObservableList.addListener(queueListener);

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
    public synchronized void songAddedfileIOFunc() {

        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer;

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                updateProgress(0, 5);
                /** Song added case A:
                 * 1- Write to the current player
                 * 2- Add a special case end of media listener for last song in the queue*/
                try {
                    if (queueSizeAtomic.get() == 1) {
                        updateProgress(1, 5);
                        skipOK = false;
                        int index = SongQueueObservableList.get(0).getAzureForeignKey();
                        nextNextPlayerBytes = myController.downloadSongBytes(index);
                        updateProgress(2, 5);
                        final Future<MediaPlayer> futureMediaPlayer = executorService.submit(new HandleFileIO(nextNextPlayerBytes, SongQueueObservableList.get(0).getSong()));
                        MediaPlayer freshPlayer = futureMediaPlayer.get();
                        updateProgress(3, 5);
                        Platform.runLater(() ->
                        {
                            updateProgress(4, 5);
                            //add end of media Listener
                            addAmITheLastSong(freshPlayer);
                            mediaView.setMediaPlayer(freshPlayer);
                            updateProgress(5, 5);
                            skipOK = true;
                        });
                        executorService.shutdown();
                        executorService.awaitTermination(7, TimeUnit.SECONDS);
                    }
                    /** Song added case B:
                     * 1- Write to the next player
                     * 2- Add normal end of media listener to the current player that links current player to the next player*/
                    else if (queueSizeAtomic.get() == 2) {
                        updateProgress(1, 5);
                        skipOK = false;
                        int index = SongQueueObservableList.get(1).getAzureForeignKey();

                        nextNextPlayerBytes = myController.downloadSongBytes(index);
                        updateProgress(2, 5);

                        Future<MediaPlayer> futureMediaPlayer = executorService.submit(new HandleFileIO(nextNextPlayerBytes, SongQueueObservableList.get(1).getSong()));

                        MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                        updateProgress(3, 5);
                        Platform.runLater(() ->
                        {
                            writeToNextPlayer(nextSongPlayer);
                            updateProgress(4, 5);
                            addEndOfMediaListener(currentPlayer, nextPlayer);
                            updateProgress(5, 5);
                            skipOK = true;
                        });
                        executorService.shutdown();
                        executorService.awaitTermination(7, TimeUnit.SECONDS);
                    }
                    else if (queueSizeAtomic.get() > 2) {
                        updateProgress(5, 5);
                        skipOK = true;
                    }
                    /** Song added case C:
                     * 1- Write to the nextNextPlayer
                     * 2- Add normal end of media listener that links the next player to the nextNextPlayer that links to the nextNextPlayer*/
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
                }
            };
        ProgressBar bar = new ProgressBar();
        songProgBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
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
     * Remove case C: song removed and Queue is >= 2
     * 1- Check the next next song in the queue to see if it has downloaded the bytes necessary for creating mp3
     * 2- FX task downloads the bytes for the next next player
     * 3- Executor service future creates the file required for the nextPlayer and returns a mediaPlayer object
     * 4- While future is running, delete the file associated with the song removed
     * 5- Get the future MediaPlayer and write to the nextPlayer
     * 6- Link the current player to the nextPlayer using an end of media listener
     *
     * @param removedSong the song that has just ended or has been skipped.
     */
    public synchronized void songRemovedfileIOFunc(QueueSong removedSong) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        Task task = new Task<Void>() {
            @Override
            public Void call() {
                try {
                    skipOK = false;
                    /** Remove case A: song removed and no next song available
                     * 1- Delete the file associated with the song removed*/
                    if (queueSizeAtomic.get() == 0) {
                        Platform.runLater(() -> {
                            updateProgress(0, 4);
                            cleanUpUnusedFiles(removedSong.getSong());
                            //deleteRemovedSongFile(removedSong.getSong());
                            progressBall.setVisible(false);
                            pathTransitionCircle.stop();
                            updateProgress(4, 4);
                        });
                    }
                    /** Remove case B: song removed and no next song available
                     * 1- Add am I the last song listener to the current player
                     * 2- Delete the file associated with the song removed*/
                    else if (queueSizeAtomic.get() == 1) {
                        //final int max = 4;
                        updateProgress(0, 4);
                        writeToCurrentPlayer(nextPlayer);
                        updateProgress(1, 4);
                        addAmITheLastSong(currentPlayer);
                        updateProgress(2, 4);
                        Platform.runLater(() -> {
                            cleanUpUnusedFiles(removedSong.getSong());
                            updateProgress(3, 4);
                            progressBall.setCenterY(progressBall.getCenterY() - 21);
                            updateProgress(4, 4);
                        });
                    }
                    /** Remove case C: song removed and next song available
                     **/
                    else if (queueSizeAtomic.get() >= 2)
                    {
                        Platform.runLater(() -> {
                            updateProgress(0, 6);
                            progressBall.setCenterY(progressBall.getCenterY() - 21);
                        });

                        updateProgress(1, 6);

                        int index = SongQueueObservableList.get(1).getAzureForeignKey();
                        nextNextPlayerBytes = myController.downloadSongBytes(index);
                        updateProgress(2, 6);
                        nextNextPlayerString = SongQueueObservableList.get(1).getSong();
                        SongQueueObservableList.get(1).setPreparedBool(true);

                        // 3- Executor service future creates the file required for the nextPlayer and returns a mediaPlayer object
                        Future<MediaPlayer> futureMediaPlayer = executorService.submit(new HandleFileIO(nextNextPlayerBytes, nextNextPlayerString));

                        updateProgress(3, 6);

                        //get the mediaPlayer returned from future
                        MediaPlayer newNextPlayer = futureMediaPlayer.get();

                        updateProgress(4, 6);
                        //5- Write the future MediaPlayer to the nextNextPlayer by obtaining lock for writing to next player
                        writeToNextPlayer(newNextPlayer);
                        //6- Link the current player to the nextPlayer using an end of media listener
                        addEndOfMediaListener(currentPlayer, newNextPlayer);

                        updateProgress(5, 6);

                        Platform.runLater(() -> {
                            cleanUpUnusedFiles(removedSong.getSong());
                            skipOK = true;
                            updateProgress(6, 6);
                        });

                    }//end if queueSize == 2
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        ProgressBar bar = new ProgressBar();
        songProgBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
    }

    /**
     * Deletes unused files
     * @param removedSong
     */
    public void cleanUpUnusedFiles(String removedSong){

        String filePath = "C:\\test\\";
        File dir = new File(filePath);

        try
        {
            for (File file : dir.listFiles())
            {
                if (file.delete())
                    System.out.println(file.getName() + " is deleted!");
                else if (queueSizeAtomic.get() > 1)
                {
                    System.out.println(file.getName() + " Delete operation failed.");
                    String currentSong = SongQueueObservableList.get(0).getSong() + ".mp3";
                    String filename = file.getName();
                    String nextSong = SongQueueObservableList.get(1).getSong() + ".mp3";
                    //if the file delete operation failed and the file isn't the current or next song
                    if ((!filename.equals(currentSong)) && (!filename.equals(nextSong)))
                    {
                        OutputStream targetFile =
                            new FileOutputStream(
                                "C:\\test\\" + file.getName());

                        targetFile.write(0);
                        targetFile.close();
                    }
                }
                else if (queueSizeAtomic.get() == 1)
                {
                    System.out.println(file.getName() + " Delete operation failed.");
                    String currentSong = SongQueueObservableList.get(0).getSong() + ".mp3";
                    String filename = file.getName();
                    //if the file delete operation failed and the file isn't the current or next song
                    if (!filename.equals(currentSong))
                    {
                        OutputStream targetFile =
                            new FileOutputStream(
                                "C:\\test\\" + file.getName());

                        targetFile.write(0);
                        targetFile.close();
                    }
                }
                else if (queueSizeAtomic.get() == 0){
                    OutputStream targetFile =
                        new FileOutputStream(
                            "C:\\test\\" + removedSong + ".mp3");

                    targetFile.write(0);
                    targetFile.close();
                }
            }
        }catch (IOException e) {
        e.printStackTrace();
        }
    }

    /**
     * Called after future returns a MediaPlayer object.
     * Adds an end of media listener to the mediaPlayer.
     * @param link1 the mediaPlayer that receives the end of media listener
     * @param link2 the mediaPlayer that link1 goes to when it end's
     */
    private void addEndOfMediaListener(MediaPlayer link1, MediaPlayer link2)
    {
        final MediaPlayer link1Final = link1;
        final MediaPlayer link2Final = link2;
        link1Final.setOnEndOfMedia(() ->
        {
            link1Final.currentTimeProperty().removeListener(progressChangeListener);
            link1Final.stop();
            //link1Final.dispose();//release file stream link

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
    public void initSongSelection()
    {
        Task task = new Task<Void>()
        {
            @Override public Void call() {
                try
                {
                    myController.initSongs();//read from database and initialize selection list with song & artist names
                } catch (Exception e) {
                    e.printStackTrace();
                }

                SongSelectionObservableList = FXCollections.observableList(myController.getSelection());
                selectionView.setItems(SongSelectionObservableList);

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
    private void logOut(ActionEvent event)
    {
        //can't log out during animation or while a song is downloading
        if(skipOK && addAnimationFin)
        {
            synchronized (this) {
                Platform.runLater(() -> {
                    loggingOut = true;
                    //remove everything only if init button was pressed previously, else just log out
                    if (initbtn.getStyle().equals("-fx-background-color:red")) {
                        initbtn.setStyle("-fx-background-color:green");
                        System.out.println("removing everything");
                        if (queueSizeAtomic.get() > 0) {
                            mediaView.getMediaPlayer().stop();
                            mediaView.getMediaPlayer().dispose();
                        }
                        clearValuesBeforeLogginOut();
                        if(pathTransitionCircle!=null)
                            pathTransitionCircle.stop();
                        myController.restartAnimationUponLogout();
                        myController.logOut(MusicHostFramework.loginScreenID);
                    }
                    else{
                        myController.restartAnimationUponLogout();
                        myController.logOut(MusicHostFramework.loginScreenID);
                    }
                });
            }
        }
    }

    /**
     * clears GUI and com.model values before loggin out
     */
    public void clearValuesBeforeLogginOut()
    {
        //prevent the removing of songs from the queue in the model from triggering the listener attached
        SongQueueObservableList.removeListener(queueListener);
        SongSelectionObservableList = null;
        selectionView.getItems().clear();
        observableDJComments = null;
        dJComments.getItems().clear();

        if(queueSizeAtomic.get()>0) {
            SongQueueObservableList = null;
            queueList.getItems().clear();
        }

        Platform.runLater( () -> {
            myController.clearValuesBeforeLoggingOut();
            timeLabel.setText("");
            setGUIOptions();
            queueSizeAtomic.set(0);
        });

        progressBall.setVisible(false);
    }

    /**
     * Play button function playing and pausing songs
     */
    @FXML
    public void iPlay()
    {
        if ("Pause".equals(playButton.getText()) && queueSizeAtomic.get()>0)
        {
            Platform.runLater( () -> {
                if(mediaView.getMediaPlayer()!=null) {
                    mediaView.getMediaPlayer().pause();
                    playButton.setText("Play");
                    playButton.setStyle("-fx-background-color:green");
                    playBall.setFill(Color.RED);
                    pathTransitionCircle.pause();
                }
            });
        }
        else if (queueSizeAtomic.get()>0)
        {
            mediaView.setMediaPlayer(currentPlayer);
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
            playButton.setStyle("-fx-background-color:red");
            playBall.setFill(Color.GREEN);
            pathTransitionCircle.play();
        }
    }



    /**
     * com.Interface injection of screenParent which contains the main com.model for songs and DB
     * @param screenParent set the current screen parent
     */
    public void setScreenParent(ScreensController screenParent)
    {
        myController = screenParent;
    }

    /**
     * Start's the bluetooth server communicating with the android client
     * @param event user hits button to start or stop server
     */
    @FXML
    private void startServer(ActionEvent event)
    {
        if("ON".equals(serverButton.getText()))
        {
            serverButton.setStyle("-fx-background-color:red");
            serverButton.setText("OFF");
            stopServer();
        }
        else if(serverStartFlag==false && "OFF".equals(serverButton.getText()))
        {
            serverButton.setStyle("-fx-background-color:green");
            serverButton.setText("ON");
            startServer();
        }
    }

    /**
     * Shuts down server thread
     */
    public void stopServer()
    {
        try
        {
            LocalDevice local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);

            try {
                executorService1.shutdown();
                //time allocated for the android client to make a choice
                executorService1.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("server task interrupted");
            } finally {
                if (!executorService1.isTerminated()) {
                    System.err.println("cancel non-finished server task");
                }
                if(clientExecutor !=null)
                    clientExecutor.shutdownNow();
                System.out.println("\nserver shutdown finished ");
            }
        }
        catch (BluetoothStateException e) {e.printStackTrace();}
    }

    /**
     * Function called from the start server button
     */
    public void startServer() {
        serverStartFlag= true;
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
                System.out.println("\nconnected!");

                while (volatileThread == thisThread)
                {
                    clientExecutor = Executors.newSingleThreadExecutor();
                    processThread = new ProcessConnectionThread(connection);
                    clientExecutor.execute(processThread);

                    try {
                        clientExecutor.shutdown();
                        //time allocated for the android client to make a choice
                        clientExecutor.awaitTermination(20, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        System.err.println("Client task interrupted");
                    } finally {
                        if (!clientExecutor.isTerminated()) {
                            System.err.println("cancel non-finished Client task");
                        }
                        clientExecutor.shutdownNow();
                        System.out.println("\nClient shutdown finished ");
                    }

                    System.out.println("waiting for connections...");
                    //blocking call waiting for client to connect
                    connection = notifier.acceptAndOpen();
                    System.out.println("\nconnected!");
                }//end while volatile
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Server Connection Thread Class
     * */
    public class ProcessConnectionThread implements Runnable, MusicHostCommunication {
        private volatile StreamConnection mConnection;
        private volatile Thread volatileThread;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;

        /**
         * Communication protocol CONSTANTS
         */
        final int OPTIONS = 0;
        final int SONG_SELECT = 1;
        final int SONG_SELECTED = 2;
        final int DJ_COMMENT = 3;
        final int SKIP_SONG = 4;

        /**
         * Constructor
         */
        public ProcessConnectionThread(StreamConnection connection) {
            mConnection = connection;
        }

        /**
         * Runnable
         */
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
                if (dataInputStream.available() > 0)
                    WhatToDoFunc(whatToDo);

                while (volatileThread == thisThread && !endCom)
                {
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
                            if (whatToDo > 0 && whatToDo < 5) {
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
                }//end while (volatileThread == thisThread && !endCom)
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    mConnection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * Determines what functions should execute depending on the read int value from the connection thread
         *
         * @param whatToDo the read int value for the switch statement
         */
        public synchronized void WhatToDoFunc(int whatToDo) {
            try {
                System.out.print("\n got something ");
                switch (whatToDo) {
                    case OPTIONS:
                        System.out.print("\n got OPTIONS ");
                        AvailableOptionsTx(AvailableOptionsRx());
                        break;
                    case SONG_SELECT:
                        System.out.print("\n got SONG_SELECT ");
                        SongSelectionTx(SongSelectionRx());
                        break;
                    case SONG_SELECTED:
                        System.out.print("\n got SONG_SELECTED ");
                        SongSelectedTx(SongSelectedRx());
                        break;
                    case DJ_COMMENT:
                        System.out.print("\n got DJ_COMMENT ");
                        DJCommentTx(DJCommentRx());
                        break;
                    case SKIP_SONG:
                        SkipSongRx();
                        //prevents android app from crashing when FX thread has to process skipping a song
                        Thread.sleep(100);
                        SkipSongTX();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * Reads data input stream
         *
         * @return Returns information read from the data input stream
         */
        public synchronized String procInput() {
            try {
                byte[] msg = new byte[dataInputStream.available()];
                dataInputStream.read(msg, 0, dataInputStream.available());
                return new String(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * Communication interface for transmission through the data output stream
         *
         * @param msg      The message to send
         * @param whatToDo What the receiver should do with the message
         * @return whether communication was successful or not
         */
        @Override
        public boolean sendMessageByBluetooth(String msg, int whatToDo) {
            try {
                if (dataOutputStream != null) {
                    dataOutputStream.writeInt(whatToDo);
                    dataOutputStream.flush();
                    dataOutputStream.write(msg.getBytes());
                    dataOutputStream.flush();
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                return false;
            }
        }

        /**
         * Send options selecting to client
         */
        @Override
        public void AvailableOptionsTx(String msg) {
            sendMessageByBluetooth(msg, 0);
        }

        /**
         * Received a Music Host Client
         */
        @Override
        public String AvailableOptionsRx() {
            String strings = Arrays.toString(boolOptionsArray);
            System.out.print(strings);
            return strings;
        }

        /**
         * Send song selecting to client
         */
        @Override
        public void SongSelectionTx(String msg) {
            String selectionAndQueue = myController.songSelectionToJson() + '&' + myController.songQueueToJson();
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
            sendMessageByBluetooth(msg, 2);
        }

        /**
         * Receives selected song from client, if the song is already ready in the queue then +1 votes the selected song.
         */
        @Override
        public String SongSelectedRx() {
            String song = procInput();
            if (song != null) {

                if (addSongTask(searchSelectionForMatch(song))) {
                    return "The song " + song + " has been added to the queue";
                } else {
                    //song selected by Android client now requires +1 votes in order to be skipped
                    addAntiSkipVoteToSongInQueue(searchQueueForIndex(song));
                    return "The song " + song + " is already in the queue, \nRemember to swipe right to check the queue before making a selection";

                }
            }
            return "Sorry but that song is already in the queue";
        }

        public void addAntiSkipVoteToSongInQueue(int index) {
            //song now requires +1 more skip votes in order for it to be skipped
            SongQueueObservableList.get(index).incrementAntiSkipVote();
        }

        /**
         * Send song DJ comment history and song queue to client
         */
        @Override
        public void DJCommentTx(String msg) {
            Platform.runLater(() -> {
                observableDJComments.add(msg);
                String selectionAndQueue = myController.DJCommentToJson() + '&' + myController.songQueueToJson();
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
            Platform.runLater(() -> {
                String DJCommentsAndQueue = myController.DJCommentToJson() + '&' + myController.songQueueToJson();
                sendMessageByBluetooth(DJCommentsAndQueue, SKIP_SONG);
            });
        }

        /**
         * Received skip request from client
         */
        @Override
        public void SkipSongRx() {
            if(queueSizeAtomic.get() > 0)
            {
                if (SongQueueObservableList.get(0).decrementAndGetVotes() <= 0)
                {
                    Task task = new Task<Void>()
                    {
                        @Override public Void call() {
                            iSkip();
//                            String DJCommentsAndQueue = myController.DJCommentToJson() + '&' + myController.songQueueToJson();
//                            sendMessageByBluetooth(DJCommentsAndQueue, SKIP_SONG);
                            return null;
                        }
                    };
                    new Thread(task).start();
                }
            }
        }//end connection thread class
    }

    /** Boolean button for song request option*/
    @FXML
    private void setSongRequestBool(){
        if ("ON".equals(boolRequest.getText())) {
            boolRequest.setStyle("-fx-background-color:red");
            boolRequest.setText("OFF");
            boolOptionsArray[0] = false;
        } else {
            boolRequest.setStyle("-fx-background-color:green");
            boolRequest.setText("ON");
            boolOptionsArray[0] = true;
        }
    }

    /** Boolean button for DJ comment option*/
    @FXML
    private void setDJCommentBool(){
        if ("ON".equals(boolDJComment.getText())) {
            boolDJComment.setStyle("-fx-background-color:red");
            boolDJComment.setText("OFF");
            boolOptionsArray[1] = false;
        } else {
            boolDJComment.setStyle("-fx-background-color:green");
            boolDJComment.setText("ON");
            boolOptionsArray[1] = true;
        }
    }

    /** Boolean button for skip song option*/
    @FXML
    private void setSkipSongBool(){
        if ("ON".equals(boolSkip.getText())) {
            boolSkip.setStyle("-fx-background-color:red");
            boolSkip.setText("OFF");
            boolOptionsArray[2] = false;
        } else {
            boolSkip.setStyle("-fx-background-color:green");
            boolSkip.setText("ON");
            boolOptionsArray[2] = true;
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