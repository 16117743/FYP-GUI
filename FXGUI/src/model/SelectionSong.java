package model;

import Interface.SongInterfaceForModel;

import javafx.scene.media.MediaPlayer;

public class SelectionSong implements SongInterfaceForModel {
    /**data*/
    private MediaPlayer player;
    private String artist;
    private String song;
    private byte[] songByte;
    private Boolean bool = true;
    private int id = 0;

    /**Constructor*/
    public SelectionSong(String songname, String artist, int id)
    {
    this.id = id;
    this.song = songname;
    this.artist = artist;
    this.bool = false;
    }

    // initialise song with everything but the byte array data from the Azure database
    @Override
    public void initMe() {

    }

    /**Accessors*/
    public String getSong(){return song;}
    public MediaPlayer getPlayer(){return player;}
    public boolean getBool(){return bool;}
    public String getArtist(){return artist;}
    public int getId() {return id;}


    /**Mutators*/
    public void setBool(boolean bool1){bool = bool1;}
    public void setBool(Boolean bool) {this.bool = bool;}
    public void setSong(String song) {this.song = song;}
    public void setArtist(String artist) {this.artist = artist;}
    public void setByteArray(byte[]fileBytes){songByte = fileBytes;}
    public void setId(int id) {this.id = id;}
}
