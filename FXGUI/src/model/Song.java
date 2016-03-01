package model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;

/**
 * Created by user on 07/02/2016.
 */
public class Song {
    private MediaPlayer player;
    private String artist;

private String song;
    private byte[] songByte;
    private Boolean bool = true;
    private int id = 0;
    private String name = "0";
    private String path = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");

    public Song(String uri, int id, String iSong){
        player = createPlayer(uri);
        this.id = id;
        this.name = Integer.toString(id);
    }
    public Song(String name, int id){
    // player = createPlayer(uri);
    this.id = id;
    this.song = name;
        this.name = name;
    this.bool = false;
      //  song = iSong;
}

    public Song(String uri, String iSong, String iArtist){
        player = createPlayer(uri);
        artist = iArtist;
        song = iSong;
    }

    public Song(byte[] bytes, int id, String iSong){
    songByte = bytes;
    bool = false;
    this.id = id;
    this.name = Integer.toString(id);
    song = iSong;
//        artist = iArtist;
    // song = iSong;
    }


    public Song(byte[] bytes, int id){
        songByte = bytes;
        bool = false;
        this.id = id;
        this.name = Integer.toString(id);
//        artist = iArtist;
       // song = iSong;
    }

    private MediaPlayer createPlayer(String aMediaSrc) {
        //System.out.println("Creating player for: " + aMediaSrc);
        final MediaPlayer player = new MediaPlayer(new Media(aMediaSrc));
        player.setOnError(new Runnable() {
            @Override
            public void run() {
                System.out.println("Media error occurred: " + player.getError());
            }
        });
        return player;
    }
    public MediaPlayer createPlayer2() {
    //System.out.println("Creating player for: " + aMediaSrc);
        try {
            OutputStream targetFile=
                new FileOutputStream(
                    "C:\\test\\fromDB" + Integer.toString(id) + ".mp3");

            String name = "fromDB" + Integer.toString(id) + ".mp3";

            targetFile.write(songByte);
            targetFile.close();
            String path = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");
            player = new MediaPlayer(new Media(path));
            player.setOnError(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Media error occurred: " + player.getError());
                }
            });
            return player;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
}

    public boolean getBool(){
        return bool;
    }

    public void setBool(boolean bool1){
        bool = bool1;
    }

    public void setBool(Boolean bool) {
        this.bool = bool;
    }

    public MediaPlayer getPlayer(){
        return player;
    }

    public String getArtist(){
        return artist;
    }

    public String getSong(){
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }


    public void setArtist(String artist) {
        this.artist = artist;
    }
//    public int returnSongSize(){
//        return ;
//    }
    public void setByteArray(byte[]fileBytes){

        songByte = fileBytes;
    }
}
