package com.model;

import com.util.QueueSong;
import com.util.SelectionSong;
import com.util.ComBean;
import org.json.JSONArray;
import java.util.ArrayList;
import java.util.List;

/***********************
 * Author: Thomas Flynn
 * date: 25/04/16
 **********************/

/**
 * Holds songQueue,songSelection,DJComments and userLogin ID for the Application and communicates to the Azure SQL database.
 */
public class Model{
    /**GUI related*/
    List<QueueSong> songQueue;
    List<SelectionSong> selection;
    List <String> DJCommentsData;
    private int userID = -1;

    /**Constructor*/
    public Model() {
        songQueue = new ArrayList<>();
        selection = new ArrayList<>();
        DJCommentsData = new ArrayList<>();
    }

    /**
     * Confirms the user's login and password
     * @param user username
     * @param pw user password
     * @return the user ID if it's valid, else return -1
     */
    public boolean confirmLogin(String user, String pw) {
        int result = new DB().confirmLogin(user, pw);
        if(result != -1) {
            userID = result;
            return true;
        }
        else
            return false;
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
     * Compiles a list of SongSelection objects depending on foreign keys matching primary keys in the user login table
     */
    public void initSongs()
    {
        try
        {
            if (userID != -1) {
                selection.addAll(new DB().initSongs(userID));
            }
        }//end try
        catch(Exception e){
            return;
        }
    }

    /**
     * Downloads the necessary bytes for creating an mp3 file
     * @param index1 The foreign key
     * @return the bytes for creating an mp3 file
     */
    public byte[] downloadSongBytes(int index1)
    {
        try
        {
            return(new DB().downloadSongBytes(index1));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the JSON of the song selection list
     * @return Returns the JSON of the song selection list
     */
    public String songSelectionToJson(){
        ArrayList<ComBean> beanList = new ArrayList();
        for(int i =0; i<selection.size();i++){
            ComBean sb = new ComBean();
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
        ArrayList<ComBean> beanList = new ArrayList();
        for(int i =0; i<songQueue.size();i++){
            ComBean sb = new ComBean();
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
        ArrayList<ComBean> beanList = new ArrayList();
        for(int i =0; i<DJCommentsData.size();i++){
            ComBean sb = new ComBean();
            sb.setDJComment(DJCommentsData.get(i));
            sb.setVotes(0);
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);

        return  jsonAraay.toString();
    }

    /** Getters and setters*/
    public List<QueueSong> getSongQueue() {return songQueue;}

    public List<SelectionSong> getSelection() {return selection;}

    public List getDJCommentsData() {return DJCommentsData;}

    public void setUserID(int userID) {this.userID = userID;}

    public int getUserID() {return userID;}

    public void clearValuesBeforeLoggingOut(){
        songQueue.clear();
        selection.clear();
        DJCommentsData.clear();
    }

}















