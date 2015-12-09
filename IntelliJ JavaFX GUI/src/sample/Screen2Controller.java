package sample;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import Server.*;

public class Screen2Controller implements Initializable , ControlledScreen {
    ScreensController myController;
   // private TemperatureSensor sensor;

    final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
    final Label currentlyPlaying = new Label();
    final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
    MediaView mediaView;
   // MediaView mediaView = new MediaView();

    private TemperatureSensor sensor;

    private String test = new String("");



    @FXML
    Button playButton;

//    @FXML
//    MediaView mediaView;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    public Screen2Controller(){
        // determine the source directory for the playlist
        final File dir = new File("C:\\the set\\");
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Cannot find video source directory: " + dir);
            Platform.exit();
            //  return null;
        }

        // create some media players.
        //  final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
        for (String file : dir.list(new FilenameFilter() {
            @Override public boolean accept(File dir, String name) {
                return name.endsWith(".mp3");
            }
        })) players.add(createPlayer("file:///" + (dir + "\\" + file).replace("\\", "/").replaceAll(" ", "%20")));
        // %20 is immediately recognisable as a whitespace character -
        // while not really having any meaning in a URI it is encoded in order to avoid breaking the string into multiple "parts".

        if (players.isEmpty()) {
            System.out.println("No audio found in " + dir);
            Platform.exit();
            // return null;
        }

        mediaView = new MediaView(players.get(0));

        // play each audio file in turn.
        for (int i = 0; i < players.size(); i++) {
            final MediaPlayer player     = players.get(i);
            final MediaPlayer nextPlayer = players.get((i + 1) % players.size());
            player.setOnEndOfMedia(new Runnable() {
                @Override public void run() {
                    player.currentTimeProperty().removeListener(progressChangeListener);
                    mediaView.setMediaPlayer(nextPlayer);
                    nextPlayer.play();
                }
            });
        }

        // display the name of the currently playing track.
        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                setCurrentlyPlaying(newPlayer);
            }
        });

        mediaView.setMediaPlayer(players.get(0));
      //  mediaView.getMediaPlayer().play();
        setCurrentlyPlaying(mediaView.getMediaPlayer());


    }



//    @FXML
//    private Button playButton; // value will be injected by the FXMLLoader
//    @FXML
//    private Button loadButton; // value will be injected by the FXMLLoader

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert mediaView != null : "meh";
        assert playButton != null;
        assert songRequest != null : "songrequest not injected!";
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }

    @FXML
    private void goToScreen1(ActionEvent event){
       myController.setScreen(ScreensFramework.screen1ID);
    }
    
    @FXML
    private void goToScreen3(ActionEvent event){
       myController.setScreen(ScreensFramework.screen3ID);
    }

    @FXML
    private void play(ActionEvent event){
        if ("Pause".equals(playButton.getText())) {
            mediaView.getMediaPlayer().pause();
            playButton.setText("Play");
        } else {
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
        }
    }

    @FXML
    private void skipMethod(ActionEvent event){
        skipButton.setText("Skip");
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
        mediaView.setMediaPlayer(nextPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        nextPlayer.play();
    }

    @FXML
    private void refreshMethod(ActionEvent event){
       // System.out.println("refresh");

        Platform.runLater(() -> {
            try {
                System.out.println(sensor.getInputReading());
                songRequest.appendText(sensor.getInputReading());
            //    System.out.println("button is clicked");
            } catch (Exception ex) {
                //Exceptions.printStackTrace(ex);
            }
        });
    }

    @FXML
    private void loadMusic(ActionEvent event) {
        sensor = new TemperatureSensor();
    }



    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
        progress.setProgress(0);
        progressChangeListener = new ChangeListener<Duration>() {
            @Override public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
            }
        };
        newPlayer.currentTimeProperty().addListener(progressChangeListener);

        String source = newPlayer.getMedia().getSource();
        source = source.substring(0, source.length() - ".mp4".length());
        source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
        currentlyPlaying.setText("Now Playing: " + source);
    }

    private MediaPlayer createPlayer(String aMediaSrc) {
        //System.out.println("Creating player for: " + aMediaSrc);
        final MediaPlayer player = new MediaPlayer(new Media(aMediaSrc));
        player.setOnError(new Runnable() {
            @Override public void run() {
                System.out.println("Media error occurred: " + player.getError());
            }
        });
        return player;
    }

    public class MyThread extends Thread {


    }


    public void updateTemperature(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("testing this");
                    Thread.sleep(2000);
                    //  skipButton.setText(sensor.getInputReading());
                    //   songRequest.appendText(sensor.getInputReading()+"\n");
                    sensor.getInputReading();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(!"".equals(sensor.getInputReading()))
                    test = sensor.getInputReading();
            }
        });
    }//updateTemperature
}
