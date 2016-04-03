package model;

import Interface.MainInterface;
import ignore.Ignore;
import org.json.JSONArray;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Model implements MainInterface{

    final int LOGIN_STATE = 0;
    final int MAIN_STATE = 1;
    final int DJ_STATE = 2;

    /**GUI related*/
    List<QueueSong> songQueue = new ArrayList<>();
    List<SelectionSong> selection = new ArrayList<>();

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    /** Server related */
    private ReadWriteLock rwlock;
    volatile String input = "";

    final BlockingQueue<String> messageQueue;

    /** Enum state */
    boolean[] boolArray;

    private int UserID = 2;

    /**Constructor*/
    public Model() {
        messageQueue = new ArrayBlockingQueue<>(1);
        rwlock = new ReentrantReadWriteLock();
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
            boolArray = new boolean[3];
            boolArray[LOGIN_STATE] = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /******Database related *****************************************************************************/
    public int confirmLogin(String user, String pw){
        try
        {
            final String query = "SELECT id from UserLogin " +
                "WHERE userName = '" + user + "' " +
                "AND password = '" + pw + "'";

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            if (rs.next()) {
                int test2 = rs.getInt(1);
                System.out.println(test2);
                return test2;
            }
                else
                    return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

/**
 * S_id int NOT NULL PRIMARY KEY,
 songName VARCHAR(MAX),
 artistName VARCHAR(MAX),
 dataSize INT(MAX),
 songData VARBINARY(MAX),
 id INT NOT NULL FOREIGN KEY REFERENCES UserLogin1(id)
 */
    /**
     * construct
     */
    public void initSongs() {
        try
        {
            final String query = "select S_Id , songname, artistname  from UserSongs  where Id = " +"2"; // where Id ="+ Integer.toString(UserID);
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            while (rs.next())
            {
                int sid = rs.getInt(1);
                String songTitle = rs.getString(2);
                String songArtist = rs.getString(3);
                selection.add(new SelectionSong(songTitle,songArtist, sid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Json();
    }

    /**
     *
     * @param index1
     * @return
     */
    public QueueSong createQueueSong(int index1) // enum state 1
    {
        try
        {
            int songForeignKey = selection.get(index1).getId();
            final String query = "select data from UserSongs where S_Id = " + Integer.toString(songForeignKey);

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                //songQueue.add(new QueueSong(selection.get(index1),fileBytes));
                return new QueueSong(selection.get(index1),fileBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * @param index1
     * @return
     */
    public byte[] downloadSongBytes(int index1) // enum state 1
    {
        try
        {
           // int songForeignKey = selection.get(index1).getId();
            final String query = "select data from UserSongs where S_Id = " + Integer.toString(index1);

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
            return fileBytes;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*******Database related *****************************************************************************/

    /**
     *
     * @param instance
     */
    @Override
    public void playSong(Class instance) {
        if(instance.equals(sample.DJScreenController.class)) {
            System.out.print("DJ\n");
        }
        else if(instance.equals(sample.MainSceneController.class)) {
            System.out.print("\nMain\n");
        }
    }

    @Override
    public void skipSong() {
        try {
            if (songQueue.size() > 0) {
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
        //songQueue.get(0).pauseMe();
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

    public boolean getBoolArray(int arg) {
        return boolArray[arg];
    }

    public List<QueueSong> getSongQueue() {return songQueue;}

    public List<SelectionSong> getSelection() {return selection;}

    public void setBoolArray(int arg,boolean bool) {
        for(int i=0;i<3;i++)
            boolArray[i] = false;

        this.boolArray[arg] = bool;
    }

    public void setUserID(int userID) {UserID = userID;}

    /***********************SERVER CODE ********************************************************/
    public String Json(){
        ArrayList<SongBean> beanList = new ArrayList();
        for(int i =0; i<selection.size();i++){
            SongBean sb = new SongBean();
            sb.setSong(selection.get(i).getSong());
            sb.setArtist("artist" + i);
            sb.setVotes(i);
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);
       // System.out.print( jsonAraay.toString());

        return  jsonAraay.toString();
    }
}















