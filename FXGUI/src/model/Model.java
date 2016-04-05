package model;

import ignore.Ignore;
import org.json.JSONArray;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;

public class Model{

    final int LOGIN_STATE = 0;
    final int MAIN_STATE = 1;
    final int DJ_STATE = 2;

    /**GUI related*/
    List<QueueSong> songQueue = new ArrayList<>();
    List<SelectionSong> selection = new ArrayList<>();
    List <String> DJCommentsData = new ArrayList<>();

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    /** Server related */
    private ReadWriteLock rwlock;
    volatile String input = "";

    /** Enum state */
    boolean[] boolArray;

    private int userID = 1;

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
            boolArray = new boolean[3];
            boolArray[LOGIN_STATE] = true;
            DJCommentsData.add("Bob: I love this song!");
            DJCommentsData.add("Jane: I hate this song!");
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
                int id = rs.getInt(1);
                userID = id;
                return id;
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
            final String query = "select S_Id , songname, artistname  from UserSongs  where Id = " +Integer.toString(userID); // where Id ="+ Integer.toString(UserID);
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

    /** Getters and setters*/

    public String getSongInfo(int index) {
        return selection.get(index).getSong();
    }

    public List<QueueSong> getSongQueue() {return songQueue;}

    public List<SelectionSong> getSelection() {return selection;}

    public List getDJCommentsData() {return DJCommentsData;}

    public void setBoolArray(int arg,boolean bool) {
        for(int i=0;i<3;i++)
            boolArray[i] = false;

        this.boolArray[arg] = bool;
    }

    public void setUserID(int userID) {userID = userID;}

    /***********************SERVER CODE ********************************************************/
    public String songSelectionToJson(){
        ArrayList<SongBean> beanList = new ArrayList();
        for(int i =0; i<selection.size();i++){
            SongBean sb = new SongBean();
            sb.setSong(selection.get(i).getSong());
            sb.setArtist(selection.get(i).getArtist());
            sb.setVotes(0);
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);

        return  jsonAraay.toString();
    }

    public String songQueueToJson(){
        ArrayList<SongBean> beanList = new ArrayList();
        for(int i =0; i<songQueue.size();i++){
            SongBean sb = new SongBean();
            sb.setSong(songQueue.get(i).getSong());
            sb.setArtist(songQueue.get(i).getArtist());
            sb.setVotes(songQueue.get(i).getVotes());
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);

        return  jsonAraay.toString();
    }

    public String DJCommentToJson(){
        ArrayList<SongBean> beanList = new ArrayList();
        for(int i =0; i<DJCommentsData.size();i++){
            SongBean sb = new SongBean();
            sb.setDJComment(DJCommentsData.get(i));
            sb.setVotes(0);
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);

        return  jsonAraay.toString();
    }

    public void clearValuesBeforeLogginOut(){
        for(int i =0; i<selection.size(); i++)
            selection.remove(0);

        for(int i =0; i<songQueue.size(); i++)
            songQueue.remove(0);

        for(int i =0; i<DJCommentsData.size(); i++)
            DJCommentsData.remove(0);
    }
}















