package model;

import ignore.Ignore;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.*;
import java.sql.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Model {
    List<Song> Songs = new ArrayList<>();
    List<MediaPlayer> players = new ArrayList<MediaPlayer>();
    final Label currentlyPlaying = new Label();
    final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
    final File dir = new File("C:\\theset\\");
    //final File dir2 = new File("C:\\test\\");
    /***********************************/
    private String con;
    private int result;
    private PreparedStatement sqlInsertName = null;
    private PreparedStatement getSong = null;
    private Ignore ignore;
    private Connection connection;
    private ProgressBar progBar;

    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);

    // MediaView mediaView;
    // MediaView mediaView = new MediaView();

    //private String test = new String("");
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
        progBar = new ProgressBar();
        init();
    }

    /*********Constructor methods *************************************************************/
    public void init(){
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
            Songs.add(new Song(path,ii));
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
        for (int i = 0; i < players.size()-1; i++) {

            final MediaPlayer player     = players.get(i);
            final MediaPlayer nextPlayer = players.get((i + 1) % players.size());

            player.setOnEndOfMedia(new Runnable() {
                @Override public void run() {
                    player.currentTimeProperty().removeListener(progressChangeListener);
                    mediaView.setMediaPlayer(nextPlayer);
                    nextPlayer.play();
                }
            });
        // display the name of the currently playing track.
//        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
//            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
//                setCurrentlyPlaying(newPlayer);
//            }
//        });

        try
        {
            ignore = new Ignore();
            con = ignore.getCon();
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName( driver );
            connection = DriverManager.getConnection(con);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
         }


    }
    public void initSongs(){
            //final byte[] fileBytes = null;

            Task task = new Task<Void>() {
                @Override public Void call() {
                    try {
                        final String query = "select data from UserSongs";
                        //where songName = 'song3'
                        Statement state = connection.createStatement();
                        ResultSet rs = state.executeQuery(query);


                        int cnt = 0;
                        while (rs.next())
                        {
                            byte[] fileBytes = rs.getBytes(1);
                            //Song s = new Song( new Byte[]);
//                            OutputStream targetFile=
//                                new FileOutputStream(
//                                    "C:\\test\\fromDB" + Integer.toString(cnt) + ".mp3");
//
//                            String name = "fromDB" + Integer.toString(cnt) + ".mp3";
//
//                            targetFile.write(fileBytes);
//                            targetFile.close();
//                            String path = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");

                            //final Media media = new Media(ba);

                            Songs.add( new Song(fileBytes, Songs.size()));
                            System.out.println("\nsong size =" + Songs.size());
                            cnt ++;
                            updateProgress(cnt, 6);
                            //addSong(theSong);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {

                    }

                    System.out.println("done");
                    return null;
                }
            };
            //progBar = new ProgressBar();
            progBar.progressProperty().bind(task.progressProperty());
            new Thread(task).start();
    }

    public void addSong(){
//        for(int i=0;i<Songs.size()-1;i++) {
//            if (!Songs.get(i + 1).getBool()) {
//                Songs.get(i + 1).createPlayer2();
//                System.out.println("xxx");
//                players.add(Songs.get(i).getPlayer());
//            }
//        }
        mediaView.setVisible(true);

        for (int i = 1; i <= 3; i++) {

            if (!Songs.get(i).getBool()) {
                Songs.get(i).createPlayer2();
                System.out.println("created player 1");
                //players.add(Songs.get(i).getPlayer());
                Songs.get(i).setBool(true);
            }
            if (!Songs.get(i+1).getBool()) {
                Songs.get(i+1).createPlayer2();
                System.out.println("created player 2");
               // players.add(Songs.get(i+1).getPlayer());
                Songs.get(i+i).setBool(true);
            }
//            final MediaPlayer player = Songs.get(i).getPlayer();
//            final MediaPlayer nextPlayer = Songs.get((i + 1) % Songs.size()).getPlayer();
//
//            player.setOnEndOfMedia(new Runnable() {
//                @Override
//                public void run() {
//                    player.currentTimeProperty().removeListener(progressChangeListener);
//                    mediaView.setMediaPlayer(nextPlayer);
//                    nextPlayer.play();
//                }
//            });

            // display the name of the currently playing track.
//            mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
//                @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
//                    setCurrentlyPlaying(newPlayer);
//                }
//            });
        }
        System.out.println("\nsong size =" + Songs.size());
    }

    public void removeSong(){

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

    /****************Helper methods ************************************************/
    public void Play() {
        mediaView.getMediaPlayer().play();
    }
    public void Pause() {
        mediaView.getMediaPlayer().pause();
    }

    public void Skip(){
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        //MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
        // get index of song currently playing by ....
        // then feed it into method
        // Songs.remove first element
        //songs.getplayer of next element

        //int test = Songs.indexOf(2);
        if(curPlayer.equals(Songs.get(0).getPlayer())){
            System.out.println("true mofo");
        }


        MediaPlayer nextPlayer = Songs.get(1% Songs.size()).getPlayer();
        Songs.remove(0);

        mediaView.setMediaPlayer(nextPlayer);
//        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        nextPlayer.play();
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }
  }