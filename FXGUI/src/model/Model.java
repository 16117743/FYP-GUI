package model;

import ignore.Ignore;
import org.json.JSONArray;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/***********************
 * Author: Thomas Flynn
 * date: 25/04/16
 **********************/

/**
 * Holds data for the Application and connection to the Azure SQL database
 */
public class Model{
    /**GUI related*/
    List<QueueSong> songQueue = new ArrayList<>();
    List<SelectionSong> selection = new ArrayList<>();
    List <String> DJCommentsData = new ArrayList<>();

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    private int userID = -1;

    ResultSet rs = null;
    Statement state = null;

    /**Constructor*/
    public Model() {
        try {
            ignore = new Ignore();
            con = ignore.getCon();
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName(driver);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Confirms the user's login and password
     * @param user username
     * @param pw user password
     * @return the user ID if it's valid, else return -1
     */
    public int confirmLogin(String user, String pw){
        try
        {
            connection = DriverManager.getConnection(con);
            final String query = "SELECT id from UserLogin " +
                "WHERE userName = '" + user + "' " +
                "AND password = '" + pw + "'";

            state = connection.createStatement();
            rs = state.executeQuery(query);

            if (rs.next()) {
                int id = rs.getInt(1);
                userID = id;
                return id;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                rs.close();
            } catch (Exception e) { /* ignored */ }
            try {
                state.close();
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
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
     * Compiles a list of SongSelection objects depending on forieng keys matching primary keys in the user login table
     */
    public void initSongs() {
        try
        {
            connection = DriverManager.getConnection(con);

            final String query = "select S_Id , songname, artistname  from UserSongs  where Id = 1";
                //+Integer.toString(userID); // where Id ="+ Integer.toString(UserID);
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
        } finally {
            try {
                rs.close();
            } catch (Exception e) { /* ignored */ }
            try {
                state.close();
            } catch (Exception e) { /* ignored */ }
            try {
                connection.close();
            } catch (Exception e) { /* ignored */ }
        }
    }

    /**
     * Downloads the necessary bytes for creating an mp3 file
     * @param index1 The foreign key
     * @return the bytes for creating an mp3 file
     */
    public byte[] downloadSongBytes(int index1) // enum state 1
    {
        try
        {
            connection = DriverManager.getConnection(con);
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

    /** Getters and setters*/
    public List<QueueSong> getSongQueue() {return songQueue;}

    public List<SelectionSong> getSelection() {return selection;}

    public List getDJCommentsData() {return DJCommentsData;}

    public void setUserID(int userID) {userID = userID;}


    /**
     * Returns the JSON of the song selection list
     * @return Returns the JSON of the song selection list
     */
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


    /**
     * Returns the JSON of the song queue list
     * @return Returns the JSON of the song queue list
     */
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


    /**
     * Returns the JSON of the DJ comments list
     * @return Returns the JSON of the DJ comments list
     */
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

}















