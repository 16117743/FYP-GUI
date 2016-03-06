package model;

import Interface.MainInterface;
import ignore.Ignore;
import javafx.beans.value.ChangeListener;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Model implements MainInterface{

    /**GUI related*/
    List<Song> songQueue = new ArrayList<>();
    List<Song> selection = new ArrayList<>();
    private ChangeListener<Duration> progressChangeListener;
    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    /** meh */
    private boolean changedBool;
    private ServerModel serverModel;

    /**Constructor*/
    public Model() {
        init();
    }

    /**Constructor methods*/
    public void init() {
        try {
            ignore = new Ignore();
            con = ignore.getCon();
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName(driver);
            connection = DriverManager.getConnection(con);
            serverModel = new ServerModel();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void startServer(){
        serverModel.doThreadStuff();
    }

    public String ServerPollQueue(){
        return serverModel.pollQueue();
    }

    /**song methods*/
    public void initSongs() {
        try
        {
            final String query = "select songname from UserSongs";//where songName = 'song3'
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            while (rs.next())
            {
                String test = rs.getString(1);
                System.out.println(test);
                selection.add(new Song(test, selection.size()));//call 2 arg song constructor
                System.out.println("\nselection size =" + selection.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addSong(int index) {
        if (!selection.get(index).getBool())
        {
            selection.get(index).createPlayer2();
            selection.get(index).setBool(true);
            songQueue.add(selection.get(index));
        }
    }

    public void downloadSong(int index1) // enum state 1
    {
        try
        {
            final String indexStr = Integer.toString(index1);
            final String query = "select data from UserSongs where S_Id = " + indexStr;

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);
            System.out.println("\nselection size =" + selection.size());

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                selection.get(index1).setByteArray(fileBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /***********************************************************************************************************/
    /**Main Interface*/
    @Override
    public void playSong(Class instance) {
        if(instance.equals(sample.DJScreenController.class)) {
            System.out.print("DJ\n");
        }
        else if(instance.equals(sample.MainSceneController.class)) {
            System.out.print("Main\n");
        }
    }

    @Override
    public void skipSong() {
        try {
            if (songQueue.size() > 0) {
                songQueue.get(0).skipMe();
                songQueue.get(1 % songQueue.size()).playMe();
            }
            else
                System.out.print("\n no songs in queue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            songQueue.remove(0);
        }
    }

    @Override
    public void pauseSong() {
        songQueue.get(0).pauseMe();
    }

    /**DJ interface**/



    /** Getters and setters*/
    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public String getSongInfo(int index) {
        return selection.get(index).getSong();
    }

    public int getSelectionSize() {
        return selection.size();
    }

    private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
//    progress.setProgress(0);
//    progressChangeListener = new ChangeListener<Duration>() {
//        @Override
//        public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
//            progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
//        }                               //queueList.get(0).getCurrentTime() / queueList.get(0).getTotalDuration()
//    };
//
//    newPlayer.currentTimeProperty().addListener(progressChangeListener);
    }
}



