package model;

import ignore.Ignore;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import sample.InterfaceModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Model {
List<Song> songQueue = new ArrayList<>();
List<Song> selection = new ArrayList<>();
final Label currentlyPlaying = new Label();
final ProgressBar progress = new ProgressBar();
private ChangeListener<Duration> progressChangeListener;
final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
/***********************************/
private String con;
private Ignore ignore;
private Connection connection;
private boolean changedBool;

public Model() {
    init();
}

/*********
 * Constructor methods
 *************************************************************/
public void init() {
    try {
        ignore = new Ignore();
        con = ignore.getCon();
        String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        Class.forName(driver);
        connection = DriverManager.getConnection(con);
    } catch (ClassNotFoundException e) {
        e.printStackTrace();
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

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

public void removeSong() {}

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

//synchronized(MyClass.class){
//    log.writeln(msg1);
//    log.writeln(msg2);
//}

public synchronized boolean changed(){
    if (changedBool == true){//it has changed
       // changedBool = false;
       // System.out.print("\n changed true");
        return true;// return it has changed
    }
    else {
       // changedBool = false;
        return false;
     //   System.out.print("\n changed false");
    }

}

public synchronized void setChanged(boolean bool){
    System.out.print("\n test set changed to " +  bool);
    changedBool = bool;
}

/****************
 * Helper methods
 ************************************************/
public void Play() {
    songQueue.get(0).getPlayer().play();
    songQueue.get(0).play();
}

public void Pause() {
    songQueue.get(0).getPlayer().pause();
}

public void Skip() {
    if (songQueue.size() > 0) {
        songQueue.get(0).getPlayer().stop();
        songQueue.get(1 % songQueue.size()).getPlayer().play();
        songQueue.remove(0);
    } else
        System.out.print("\n no songs in queue");
}

public BlockingQueue<String> getMessageQueue() {
    return messageQueue;
}

public String getSongInfo(int index) {
    return selection.get(index).getSong();
}

public int getSelectionSize() {
    return selection.size();
}

/***********************************************************************************************************/
public void downloadSong(int index1) // enum state 1
{
        try
        {
            final int index = index1;
            final String indexStr = Integer.toString(index);
            final String query = "select data from UserSongs where S_Id = " + indexStr;

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);
            System.out.println("\nselection size =" + selection.size());

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                selection.get(index).setByteArray(fileBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

