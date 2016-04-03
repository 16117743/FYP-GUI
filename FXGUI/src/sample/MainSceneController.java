package sample;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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
    ProcessConnectionThread processThread;
    Boolean[] boolArray = new Boolean[5];
    volatile boolean stopFlag = false;
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

        for(int i =0;i<5;i++)
            boolArray[i] = false;
    }

    private void addMediaViewPropertyListener(){

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

    /***???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????***/
    public void addObservableSongQueueListener(){
        SongQueueObservableList = FXCollections.observableList(mainModel.getSongQueue());
        queueList.setItems(SongQueueObservableList);
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
                            /** Remove Case A: Song Ended and queue is empty
                             * Step 1: Dispose of currentPlayer file association
                             * Step 2: Perform file deletion IO
                             * */
                            //songRemovedfileIOFunc(removedSong);
                           // removedSong.deleteMyPlayer();
                            //removedSong.deleteMyFile();
                            /** Remove Case B: Song Ended and next song available and queue is 1
                             * Step 1: Get nextPlayer and setCurrently playing on mediaView
                             * Step 2: Dispose of currentPlayer file association
                             * Step 3: Perform file deletion IO
                             * */
                            if (queueSizeAtomic.get()== 1 ) {
                                System.out.println("remove Case B Event fired! :\n");
                                songRemovedfileIOFunc();
                                System.out.println("songRemovedfileIOFunc finished! :\n");
                               // addListenersWhenRemoved(currentPlayer,nextPlayer);
                            }
                            /** Remove Case C: Song Ended and next song available and queue is equal to 2
                             * Step 1: SongQueueObservable.get(1).getFilePath
                             * Step 2: performFileIO-> create nextNextPlayer
                             * Step 3: addEndOfSongListeners(NextPlayer,nextNextPlayer)
                             * Step 2: Dispose of currentPlayer file association
                             * Step 3: Perform file deletion IO
                             * */
                            else if (queueSizeAtomic.get() == 2) {
                                System.out.println("remove Case C Event fired:\n");
                                songRemovedfileIOFunc();
                            }
                            else if (queueSizeAtomic.get() > 2) {
                                System.out.println("remove Case D Event fired:\n");
                                songRemovedfileIOFunc();
                            }
                        }

                        for (QueueSong addedSong : change.getAddedSubList()) {
                            System.out.println("Added event \n  Qsize = " + queueSizeAtomic.incrementAndGet()+"\n");
                            /** Add Case A: First song to be added
                             * Step 1: QueueSong downloads bytes from azure
                             * Step 2: Perform file IO for the mediaPlayer
                             * Step 3: Add mediaPlayer to currentPlayer
                             * Step 4: Add currentPlayer to mediaView
                             * */
                            if (queueSizeAtomic.get()==1) {
                                System.out.println("Add Case A event fired: \n Q size =  " + queueSizeAtomic.get());
                                songAddedfileIOFunc();
                            }
                            /** Add Case B: Song added and queue is 2
                             * Step 1: QueueSong downloads bytes from azure
                             * Step 2: Perform file IO for the mediaPlayer
                             * Step 3: Add mediaPlayer to currentPlayer
                             * Step 3: Add nextPlayer to mediaView
                             * Step 5: Add End of song listener for both the current and next Player*/
                            else if (queueSizeAtomic.get()==2)
                            {
                                System.out.println("#Add Case B : event fired: \n Q size = " + queueSizeAtomic.get());/********************************* whole list requires read access ***********/
                                songAddedfileIOFunc();
                                //addedSong.deleteMyPlayer();
                                //addedSong.deleteMyPlayer();
                            }
                            /** Add Case C: Song added and queue is greater than 2
                             * Step 1: QueueSong downloads bytes from azure
                             */
                            else if (queueSizeAtomic.get()==3)
                            {
                                System.out.println("#Add Case C : event fired:  \n Q size = " + queueSizeAtomic.get());/********************************* whole list requires read access ***********/
                                songAddedfileIOFunc();

                                // addListenersToNewlyAdded(SongQueueObservableList.get(0).getPlayer(), addedSong.getPlayer());
                                //addedSong.deleteMyPlayer();
                                //addedSong.deleteMyPlayer();
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * shifts the media players
     */
    public synchronized void shiftMediaPlayers(MediaPlayer newestPlayer){
        try{
//                CPWwriteLock.lock();
//            NPReadLock.lock();
                currentPlayer = nextPlayer;
//                CPWwriteLock.unlock();
//            NPReadLock.unlock();

//            NPWriteLock.lock();
            nextPlayer = newestPlayer;
        }finally{
//            CPWwriteLock.unlock();
//            NPReadLock.unlock();
//            NPReadLock.unlock();
        }
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
     * If the song added is the first or second one in the queue then call this function to create files for MediaPlayer objects
     */
    public void songAddedfileIOFunc(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        Future<MediaPlayer> futureMediaPlayer = null;

        try
        {
            if (queueSizeAtomic.get() == 1 )
            {
                int index = SongQueueObservableList.get(0).getAzureForeignKey();
                System.out.println("\n File IO Added Case A: \n future media player started added song 1");
                //param1: song byte data                 , param2: song name for writing to file
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(0).getSong()));
                MediaPlayer freshPlayer = futureMediaPlayer.get();
                System.out.println("File IO Added Case A: \n future media player finished for testing 1\n");
                Platform.runLater( () ->
                {
                    writeToCurrentPlayer(freshPlayer);
                   // addListenersToNewlyAdded2(currentPlayer);
                    setCurrentlyPlaying(currentPlayer);
                    mediaView.setMediaPlayer(currentPlayer);
                });
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            //prepare songQueue(1) with a file and a media player by using future Executor service
            else if (queueSizeAtomic.get() == 2)
            {
                int index = SongQueueObservableList.get(1).getAzureForeignKey();
                System.out.println("\n Added Case B: \n future media player started added song");
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(1).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                System.out.println("Added Case B: \n future media player finished for added song\n");

               // addListenersToNewlyAdded2(currentPlayer, nextSongPlayer);
                System.out.println("Added Case B: \n Assigning nextplayer a fresh player \n");
                writeNextPlayer(nextSongPlayer);
                addListenersToNewlyAdded2(currentPlayer, nextPlayer);
                executorService.shutdown();
                executorService.awaitTermination(7, TimeUnit.SECONDS);
            }
            else if (queueSizeAtomic.get() == 3)
            {
                int index = SongQueueObservableList.get(2).getAzureForeignKey();
                System.out.println("\n Added Case C: \n future media player started added song");
                futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));
                MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                System.out.println("Added Case C: \n future media player finished for added song\n");
                //writeNextNextPlayer(nextSongPlayer);
                writeToNextNextPlayer(nextSongPlayer);
                addListenersToNewlyAdded2(nextPlayer, nextSongPlayer);
                System.out.println("Added Case C: \n Assigning nextNextplayer a fresh player \n");
               // nextNextPlayer = nextSongPlayer;
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
     * Removes songQueue(0)
     * Ensures songQueue(1) has a media player and a file
     * Prepares songQueue(2) with a file and a media player by using future Executor service
     */
    public void songRemovedfileIOFunc(){
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        //System.out.print("\n future media player started removed song");
        Future<MediaPlayer> futureMediaPlayer = null;
        try
        {
            System.out.println("songRemovedfileIOFunc() called, Q size = " + queueSizeAtomic.get());
            /** Remove case B: song removed and next song available*/
            if (queueSizeAtomic.get() == 1)
            {
                System.out.println("Remove case B: File IOFunction");
               // currentPlayer = nextPlayer;
            //    setCurrentlyPlaying(nextPlayer);
            }
            /** Remove case C: song removed and Queue is equal to 2 */
            else if (queueSizeAtomic.get() == 2)
            {
                System.out.println("Remove case C: File IOFunction\"");
                if(! SongQueueObservableList.get(1).getPreparedBool())
                {
                    SongQueueObservableList.get(1).setPreparedBool(true);
                    int index = SongQueueObservableList.get(1).getAzureForeignKey();
                    System.out.println("\n future media player started Remove case C: \n ");

                    futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(1).getSong()));
                    MediaPlayer nextSongPlayer = futureMediaPlayer.get();
                    System.out.println("future media player finished for Remove case C: \n");
                    Platform.runLater( () -> {
                       // nextNextPlayer = nextSongPlayer;
                    });
                }
            }
            /** Remove case C: song removed and Queue is greater than 2 */
            else if (queueSizeAtomic.get() > 2)
            {
                System.out.println("Remove case D: File IOFunction\"");
                if(! SongQueueObservableList.get(2).getPreparedBool())
                {
                    SongQueueObservableList.get(2).setPreparedBool(true);
                    int index = SongQueueObservableList.get(2).getAzureForeignKey();
                    System.out.println("\n future media player started Remove case D: \n ");

                    futureMediaPlayer = executorService.submit(new HandleFileIO(mainModel.downloadSongBytes(index), SongQueueObservableList.get(2).getSong()));
                    writeNextPlayer(nextNextPlayer);
                    MediaPlayer newNextNextPlayer = futureMediaPlayer.get();
                    addListenersToNewlyAdded2(nextPlayer,newNextNextPlayer);
                    Platform.runLater( () ->
                    {
                        writeToNextNextPlayer(newNextNextPlayer);
                    });
                    System.out.println("future media player finished for Remove case D: \n");

                   // nextNextPlayer = futureMediaPlayer.get();
                    //shiftMediaPlayers(nextSongPlayer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    /**
     * Called after future returns a MediaPlayer object
     * adds an end of media listener */
    private void addListenersToNewlyAdded(MediaPlayer newlyMadePlayer){
        nextPlayer = newlyMadePlayer;
        nextPlayer.setOnEndOfMedia(() ->
            {
                currentPlayer.currentTimeProperty().removeListener(progressChangeListener);
                currentPlayer.stop();
                currentPlayer.dispose();//release file stream link
                System.out.println("testing race condition removal 1\n");
                /** Remove case B: next song available*/
                if(queueSizeAtomic.get()>1){
                    System.out.println("About to perform end of run later  for listenerfunc1\n");
                        System.out.println("shifting media players player\n");
                        shiftMediaPlayers(nextNextPlayer);
                        mediaView.setMediaPlayer(currentPlayer);
                    Platform.runLater( () -> {
                        currentPlayer.play();
                        SongQueueObservableList.remove(0);
                    });
                }
            });
    }

/**
 * Called after future returns a MediaPlayer object
 * adds an end of media listener */
private void addListenersToNewlyAdded2(MediaPlayer link1, MediaPlayer link2){
    final MediaPlayer link1Final = link1;
    final MediaPlayer link2Final = link2;
    System.out.println("addListenersToNewlyAdded2 ()\n");
    link1Final.setOnEndOfMedia(() ->
    {
        System.out.println("end of song triggered!\n");
        link1Final.currentTimeProperty().removeListener(progressChangeListener);
        link1Final.stop();
        link1Final.dispose();//release file stream link

        /** Remove case B: next song available*/
        if(queueSizeAtomic.get()>1){
           // writeToCurrentPlayer(link2Final);

                //shiftMediaPlayers(nextPlayer);
            Platform.runLater( () -> {
                mediaView.setMediaPlayer(link2Final);
                //mediaView.getMediaPlayer().play();
              //  SongQueueObservableList.remove(0);
            });
        }
    });
}

public void addVolumeAndTimeSliderListeners(){
        Platform.runLater( () -> {
            timeSlider.valueProperty().addListener(new InvalidationListener() {
                public void invalidated(Observable ov) {
                    if (timeSlider.isValueChanging()) {
                        /********************************* song 0 requires read access ***********/
                        final MediaPlayer player = mediaView.getMediaPlayer();
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
                        /********************************* song 0 requires read access ***********/
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
            Platform.runLater( () -> {
                mediaView.getMediaPlayer().pause();
                playButton.setText("Play");
                playButton.setStyle("-fx-background-color:green");
            });
                 //if(queueSizeAtomic.get() > 0)
        } else if (SongQueueObservableList.size()>0){/********************************* whole list requires read access ***********/
            /********************************* song 0 requires read access ***********/
            mediaView.setMediaPlayer(currentPlayer);
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
            playButton.setStyle("-fx-background-color:red");
        }
    }

    @FXML
    public void iSkip() {
        //can't skip unless there is a song to follow
        if(queueSizeAtomic.get() > 1) {/********************************* whole list requires read access ***********/
            final MediaPlayer curPlayer = mediaView.getMediaPlayer();
            curPlayer.currentTimeProperty().removeListener(progressChangeListener);
            curPlayer.stop();
            curPlayer.dispose();//remove file stream

            //MediaPlayer nextPlayer = SongQueueObservableList.get(1).getPlayer();
           // currentPlayer = nextPlayer;
            if(nextPlayer!=null)
                mediaView.setMediaPlayer(nextPlayer);
            //nextPlayer.play();
            //SongQueueObservableList.remove(0);

            //play button has to beupdated with "pause" to the user because skip "plays" a song
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
public void startServer() {
    /*****************************/
    executorService1 = Executors.newSingleThreadExecutor();
    executorService1.submit(() ->
    {
        volatileThread = Thread.currentThread();
        Thread thisThread = Thread.currentThread();
        Boolean flag = false;
        StreamConnectionNotifier notifier = null;
        StreamConnection connection = null;

        try {
            LocalDevice local = LocalDevice.getLocalDevice();

            //set The inquiry access code for General/Unlimited Inquiry
            local.setDiscoverable(DiscoveryAgent.GIAC);
            //UUID uuid = new UUID("04c6093b-0000-1000-8000-00805f9b34fb", false);
            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
            System.out.println(uuid.toString());

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
                    System.out.println("attempt to shutdown executor");
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

                // thisThread.sleep(20000);
                System.out.println("waiting for connections...");
                //blocking call waiting for client to connect
                connection = notifier.acceptAndOpen();
                System.out.println("connected!");

                /************************************/
            }
            System.out.print("\n exited main loop!");

            return;
        } catch (InterruptedException e) {
            System.out.print("\nexited through here 1 ");
            Thread.currentThread().interrupt();
            return;
        } catch (NullPointerException e) {
            System.out.print("\nexited through here 2 ");
        } catch (IOException e) {
            System.out.print("\nexited through here 3");
            e.printStackTrace();
            return;
        } finally {

        }
        return;
    });
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
                            System.out.print("\n# init data available  " + whatToDo);
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
    public synchronized void myStop(){
        volatileThread = null;
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

//    private void addListenersWhenRemoved(MediaPlayer current, MediaPlayer next){
//        final MediaPlayer player     = current;
//        final MediaPlayer nextPlayer = next;
//
//        /** Remove Case A: Song Ended and queue is empty */
//        if(queueSizeAtomic.get()==0) {
//            System.out.println("add listener song removed and queuesize = 0\n");
//        }
//        /** Remove Case B: Song Ended and queue is 1 */
//        else if(queueSizeAtomic.get()==1){
//            System.out.println("add listener when song removed and Q size = 1\n");
//            player.setOnEndOfMedia(() ->
//            {
//                player.currentTimeProperty().removeListener(progressChangeListener);
//                System.out.println("stopping end of song");
//                player.stop();
//                player.dispose();//release filestream link
//                System.out.println("setting next player in Remove Case B");
//                mediaView.setMediaPlayer(current);
//                nextPlayer.play();
//            });
//        }
//        /** Remove Case C: Song Ended and queue is greater than 1*/
//        else if(queueSizeAtomic.get() >1){
//            System.out.println("\nI should be prepping nextnextPlayer? \n");
//        }
//    }
