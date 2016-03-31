package model;

import Interface.SongInterfaceForModel;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by user on 29/03/2016.
 */
public class QueueSong implements SongInterfaceForModel{

/**data*/
    private MediaPlayer player;
    private String artist;
    private String songName;
    private byte[] songByte;
    private Boolean upNextFlag;
    private int id = 0;
    private int votes;

public String getSongName1() {
    return songName1.get();
}

public StringProperty songName1Property() {
    return songName1;
}

public void setSongName1(String songName1) {
    this.songName1.set(songName1);
}

public String getArtistName() {
    return artistName.get();
}

public StringProperty artistNameProperty() {
    return artistName;
}

public void setArtistName(String artistName) {
    this.artistName.set(artistName);
}

private StringProperty songName1;
    private StringProperty artistName;

    /**Constructor*/
    public QueueSong(SelectionSong selectionSong, byte[] songBytes)
    {
        this.id = selectionSong.getId();
        this.songName = selectionSong.getSong();
        this.artist = selectionSong.getArtist();
        this.songByte = songBytes;
        this.upNextFlag = false;
        this.votes = 2;
        prepareMe();
    }

    public QueueSong(String name, String artist)
    {
        this.songName = name;
        this.artist = artist;
        this.votes = 2;
    }

    /**Common Interface*/
    public void createPlayer() {
    try {
        OutputStream targetFile=
            new FileOutputStream(
                "C:\\test\\"+songName+".mp3");

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

    public void downloadMe() {
    }

    public void prepareMe() {
        try
        {
            OutputStream targetFile=
                new FileOutputStream(
                    "C:\\test\\"+songName+".mp3");

            String name = songName + ".mp3";
            targetFile.write(songByte);
            targetFile.close();
            String path = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");
            player = new MediaPlayer(new Media(path));
            player.setOnError( ()->
            {
                    System.out.println("Media error occurred: " + player.getError());
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public void deleteMe() {

    }

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

    public void iSkip() {

    }

    /**Accessors*/
    public String getSong(){return songName;}
    public MediaPlayer getPlayer(){return player;}
    public boolean getBool(){return upNextFlag;}
    public String getArtist(){return artist;}
    public int getVotes() {return votes;}
    /**Mutators*/
    public void setBool(boolean bool1){upNextFlag = bool1;}
    public void setSong(String song) {this.songName = song;}
    public void setArtist(String artist) {this.artist = artist;}
    public void setByteArray(byte[]fileBytes){songByte = fileBytes;}
    public void setVotes(int votes) {this.votes = votes;}
    public void setPlayer(MediaPlayer player) {this.player = player;}
//    @Override
//    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
//
//    }

}
