package com.util;

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
