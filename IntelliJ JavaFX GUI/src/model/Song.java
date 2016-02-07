package model;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URI;

/**
 * Created by user on 07/02/2016.
 */
public class Song {
    private MediaPlayer player;
    private String artist;
    private String song;

    public Song(String uri){
        player = createPlayer(uri);
    }

    public Song(String uri, String iSong, String iArtist){
        player = createPlayer(uri);
        artist = iArtist;
        song = iSong;
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

    public MediaPlayer getPlayer(){
        return player;
    }

    public String getArtist(){
        return artist;
    }

    public String getSong(){
        return song;
    }
}
