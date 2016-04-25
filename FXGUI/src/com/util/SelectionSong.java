package com.util;


/***********************
 * Author: Thomas Flynn
 * Final Year Project: Music Host Interface
 * Date: 25/04/16
 * Description: Created when the user hits the initialize button. For each song that a Music Host has on his/her account, <br>
 * a selection song is created. It holds, artist (String), song (String), id (int)
 */
public class SelectionSong {
    /**data*/
    private String artist;
    private String song;
    private int id = 0;

    /**Constructor*/
    public SelectionSong(String songname, String artist, int id)
    {
    this.id = id;
    this.song = songname;
    this.artist = artist;
    }

    /**Accessors*/
    public String getSong(){return song;}
    public String getArtist(){return artist;}
    public int getId() {return id;}

}
