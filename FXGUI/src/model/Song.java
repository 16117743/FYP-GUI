package model;

import Interface.InterfaceDJ;
import Interface.MainInterface;
import Interface.SongInterfaceForModel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class Song implements SongInterfaceForModel {
    /**data*/
    private MediaPlayer player;
    private String artist;
    private String song;
    private byte[] songByte;
    private Boolean bool = true;
    private int id = 0;
    private String name = "0";

    /**Constructor*/
    public Song(String name, int id)
    {
    this.id = id;
    this.song = name;
    this.name = name;
    this.bool = false;
    }

    public Song(byte[] bytes, int id, String iSong){
    songByte = bytes;
    bool = false;
    this.id = id;
    this.name = Integer.toString(id);
    song = iSong;
    }

    public Song(byte[] bytes, int id){
        songByte = bytes;
        bool = false;
        this.id = id;
        this.name = Integer.toString(id);
    }


    /**Common Interface*/
    public void createPlayer2() {
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
    } catch (IOException e)
        {
    e.printStackTrace();
    }
    }

    // initialise song with everything but the byte array data from the Azure database
    @Override
    public void initMe() {

    }

    // initialise song with everything but the byte array data from the Azure database
    @Override
    public void downloadMe() {
    }

    @Override
    public void prepareMe() {
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteMe() {

    }

    @Override
    public int getProgress() {
        return 0;
    }

    /**Main Interface*/

    public void playMe() {
        player.play();
    }


    public void skipMe() {
        player.stop();
        //garbage collect player and byte array
    }


    public void pauseMe() {
        player.stop();
    }

    /**DJ Interface*/
    public void DJfadeMeOut(float deltaTime) {
        float volume = 1;
        float speed = 0.05f;
        player.setVolume(volume);
        volume += speed* deltaTime;
    }


    public void DJfadeMeIn(float deltaTime) {
        float volume = 1;
        float speed = 0.05f;
        player.setVolume(volume);
        volume -= speed* deltaTime;
    }


    public void DJDoSomething() {

    }


    public void DJplayMe() {

    }


    public void DJskipMe() {

    }


    public void DJpauseMe() {

    }

    @Override
    public void iSkip() {

    }

/**Accessors*/
    public String getSong(){return song;}
    public MediaPlayer getPlayer(){return player;}
    public boolean getBool(){return bool;}
    public String getArtist(){return artist;}

    /**Mutators*/
    public void setBool(boolean bool1){bool = bool1;}
    public void setBool(Boolean bool) {this.bool = bool;}
    public void setSong(String song) {this.song = song;}
    public void setArtist(String artist) {this.artist = artist;}
    public void setByteArray(byte[]fileBytes){songByte = fileBytes;}
}
