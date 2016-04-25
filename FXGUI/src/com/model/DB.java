package com.model;

import com.ignore.Ignore;
import com.util.SelectionSong;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Thomas Flynn
 * date: 25/04/16
 * Description: Communicates with the Azure DB.
 */
public class DB {
    /** Azure DB related*/
    private String connectionString;
    private Ignore ignore;
    private Connection connection;
    ResultSet rs = null;
    Statement state = null;

    public DB() {
        try
        {
            ignore = new Ignore();
            //get the connection String from the class that's git ignored
            connectionString = ignore.getCon();
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
            connection = DriverManager.getConnection(connectionString);
            final String query = "SELECT id from UserLogin " +
                "WHERE userName = '" + user + "' " +
                "AND password = '" + pw + "'";

            state = connection.createStatement();
            rs = state.executeQuery(query);

            if (rs.next()) {
                int id = rs.getInt(1);
                return id;
            }

        } catch (Exception e) {
            return -1;
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
 * Compiles a list of SongSelection objects depending on forieng keys matching primary keys in the user login table
 */
public List<SelectionSong> initSongs(int userID)
{
    List<SelectionSong> returnList = new ArrayList<>();
    try
    {
        if (userID != -1)
        {
            connection = DriverManager.getConnection(connectionString);

            final String query = "select S_Id , songname, artistname  from UserSongs  where Id = " + Integer.toString(userID);
            //+Integer.toString(userID); // where Id ="+ Integer.toString(UserID);
            state = connection.createStatement();
            rs = state.executeQuery(query);

            while (rs.next())
            {
                int sid = rs.getInt(1);
                String songTitle = rs.getString(2);
                String songArtist = rs.getString(3);
                returnList.add(new SelectionSong(songTitle, songArtist, sid));
            }
        }//end if
    }//end try
    catch(Exception e){
        e.printStackTrace();
    }finally{
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
    return returnList;
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
            connection = DriverManager.getConnection(connectionString);
            final String query = "select data from UserSongs where S_Id = " + Integer.toString(index1);

            state = connection.createStatement();
            rs = state.executeQuery(query);

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                return fileBytes;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally{
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
        return null;
    }
}
