package model;

import ignore.Ignore;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import sample.InterfaceModel;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Model implements InterfaceModel{
    List<Song> songQueue = new ArrayList<>();
    List<Song> selection = new ArrayList<>();
    final Label currentlyPlaying = new Label();
    final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
    final File dir = new File("C:\\theset\\");
    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
    public MediaView mediaView = new MediaView();
    /***********************************/
    private String con;
    private int result;
    private PreparedStatement sqlInsertName = null;
    private PreparedStatement getSong = null;
    private Ignore ignore;
    private Connection connection;
    private ProgressBar progBar;

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

    public void initSongs(){
         //   Task task = new Task<Void>() {
         //       @Override public Void call() {
//                        try {
//                        final String query = "select data from UserSongs";
//                        //where songName = 'song3'
//                        Statement state = connection.createStatement();
//                        ResultSet rs = state.executeQuery(query);
//
//                        int cnt = 0;
//                        while (rs.next())
//                        {
//                            byte[] fileBytes = rs.getBytes(1);
//
//                            selection.add(new Song(fileBytes, selection.size()));
//                            System.out.println("\nselection size =" + selection.size());
//                            cnt ++;
//                       //     updateProgress(cnt, 6);
//                            //addSong(theSong);
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                    try {
                        final String query = "select songname from UserSongs";
                        //where songName = 'song3'
                        Statement state = connection.createStatement();
                        ResultSet rs = state.executeQuery(query);

                        int cnt = 0;
                        while (rs.next()) {
                            String test = rs.getString(1);
                            System.out.println(test);
                            //  selection.get(cnt).setSong(rs.getString(1));
                            selection.add(new Song(test, selection.size()));
                            //selection.add(new Song(fileBytes, selection.size()));
                            System.out.println("\nselection size =" + selection.size());
                            cnt++;
                            //updateProgress(cnt, 6);
                            //addSong(theSong);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    System.out.println("done");
                 //   selection.get(0).createPlayer2();
               //     mediaView.setMediaPlayer(selection.get(0).getPlayer());
              //      return null;
              //  }
         //   };
           // progBar.progressProperty().bind(task.progressProperty());
     //       new Thread(task).start();
    }

    public void addSong(int index){          //note : breaks at createplayer2
        System.out.println("started add " + index );
        if (!selection.get(index).getBool()) {
            System.out.println("\nsong bool false " + index);
            //selection.get(index).
            //setbyte array
            selection.get(index).createPlayer2();
            selection.get(index).setBool(true);
            System.out.println("\ncreated player from " + index);
            songQueue.add(selection.get(index));
           // selection.remove(index);
        }
        System.out.println("\nfinished add" + index);
    }



    public void removeSong() {
//        mediaView.setVisible(true);
//        mediaView.setMediaPlayer(songQueue.get(0).getPlayer());

        System.out.println("test1");
         progBar.setProgress(4);

        Task task = new Task<Void>() {
            @Override public Void call() {
                final int max = 100;
                for (int i=1; i<=max; i++) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (isCancelled()) {
                        break;
                    }
                    if(i%1000 ==0)
                        System.out.println(i);
                    updateProgress(i, max);
                }
                return null;
            }
        };
        // progBar = new ProgressBar();

        progBar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();

        System.out.println("test2");
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
        songQueue.get(0).getPlayer().play();

    }
    public void Pause() {
        songQueue.get(0).getPlayer().pause();
    }

    public void Skip(){
//        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
//        if(curPlayer.equals(songQueue.get(0).getPlayer())){
//            System.out.println("true mofo");
//        }

        if (songQueue.size() > 0) {
            //MediaPlayer nextPlayer = songQueue.get(1 % songQueue.size()).getPlayer();
            //mediaView.setMediaPlayer(nextPlayer);
//        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
          //  curPlayer.stop();
            songQueue.get(0).getPlayer().stop();
            songQueue.get(1 % songQueue.size()).getPlayer().play();
            songQueue.remove(0);
        }
        else
            System.out.print("\n no songs in queue");
    }

    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public String getSongInfo(int index){
        return selection.get(index).getSong();
    }

    public int getSelectionSize(){
        return selection.size();
    }

    @Override
    public void setModel(ProgressBar bar){
        progBar = bar;
    }

/***********************************************************************************************************/
 // step 1
public void downloadSong(int index1)//can setbyte array on song
{
//        if (!selection.get(index).getBool()) {
//            selection.get(index).createPlayer2();
//            selection.get(index).setBool(true);
//            System.out.println("created player from " + index);
//            songQueue.add(selection.get(index));
//            selection.remove(index);
//        }
//    Task task = new Task<Void>() {
//        @Override public Void call() {
    try {
        final int index = index1;
        final String indexStr = Integer.toString(index);
        final String query = "select data from UserSongs" +
            " where S_Id = " + indexStr;
        //where songName = 'song3'
        Statement state = connection.createStatement();
        ResultSet rs = state.executeQuery(query);
        System.out.println("\nselection size =" + selection.size());

        int cnt = 0;
        if (rs.next()) {
            byte[] fileBytes = rs.getBytes(1);
            // if (!selection.get(index).getBool()) {
            selection.get(index).setByteArray(fileBytes);
        }
            //   }
            // selection.get(index).createPlayer2();
            //    selection.get(index).setByteArray(fileBytes);
            // selection.add(new Song(fileBytes, 3));

           // cnt++;
            // updateProgress(cnt, 1);
            //addSong(theSong);

     //   }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
                                //                try {
                                //                    final String query = "select songname from UserSongs";
                                //                    //where songName = 'song3'
                                //                    Statement state = connection.createStatement();
                                //                    ResultSet rs = state.executeQuery(query);
                                //
                                //                    int cnt = 0;
                                //                    while (rs.next())
                                //                    {
                                //
                                //                        selection.get(cnt).setSong(rs.getString(1));
                                //
                                //                        //selection.add(new Song(fileBytes, selection.size()));
                                //                        System.out.println("\nselection size =" + selection.size());
                                //                        cnt ++;
                                //                        updateProgress(cnt, 6);
                                //                        //addSong(theSong);
                                //                    }
                                //
                                //                } catch (Exception e) {
                                //                    e.printStackTrace();
                                //                }
                                //
                                //                System.out.println("done");
                                //                selection.get(0).createPlayer2();
                                //                mediaView.setMediaPlayer(selection.get(0).getPlayer());
                                       //     return null;
                                        }
                                   // };
                                    // progBar.progressProperty().bind(task.progressProperty());
                                   // new Thread(task).start();
   // }


