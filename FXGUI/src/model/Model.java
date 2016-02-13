package model;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Model {
    final List<Song> Songs = new ArrayList<>();
    final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
    final Label currentlyPlaying = new Label();
    final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
    // MediaView mediaView;
    // MediaView mediaView = new MediaView();

    private String test = new String("");
    //private String input = "";
    //DataOutputStream dataOutputStream;
    /*****************************************/
    //final Model model = new Model();
    //final AtomicInteger count = new AtomicInteger(-1);
    //final NumberFormat formatter = NumberFormat.getIntegerInstance();
    //formatter.setGroupingUsed(true);

    //final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
    //final LongProperty lastUpdate = new SimpleLongProperty();
    //final long minUpdateInterval = 0 ;
    public MediaView mediaView = new MediaView();

    public Model(){
        test = "testing model";


        /***********************/

        final File dir = new File("C:\\theset\\");
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Cannot find video source directory: " + dir);
            Platform.exit();
            //  return null;
        }

        // create some media players.
        //  final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
        int ii = 0;
        for (String file : dir.list(new FilenameFilter() {@Override public boolean accept(File dir, String name) {return name.endsWith(".mp3");}}))
        {
            String path = "file:///" + (dir + "\\" + file).replace("\\", "/").replaceAll(" ", "%20");
            Songs.add(new Song(path));
            players.add(Songs.get(ii).getPlayer());
            ii++;
        }
        // %20 is immediately recognisable as a whitespace character -
        // while not really having any meaning in a URI it is encoded in order to avoid breaking the string into multiple "parts".

        if (players.isEmpty()) {
            System.out.println("No audio found in " + dir);
            Platform.exit();
            // return null;
        }

        // mediaView = new MediaView(players.get(0));
        mediaView.setMediaPlayer(players.get(0));
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
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }


    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
    progress.setProgress(0);
    progressChangeListener = new ChangeListener<Duration>() {
        @Override
        public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
            progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
        }
    };

    newPlayer.currentTimeProperty().addListener(progressChangeListener);

    String source = newPlayer.getMedia().getSource();
    source = source.substring(0, source.length() - ".mp4".length());
    source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
    currentlyPlaying.setText("Now Playing: " + source);
    }

    /****************Helper methods ***************/
//    public void
//    mediaView.getMediaPlayer().pause();
//    playButton.setText("Play");

  }