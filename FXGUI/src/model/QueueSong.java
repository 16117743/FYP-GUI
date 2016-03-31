package model;

import Interface.SongInterfaceForModel;
import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
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
    private String myFilePath;
    private String myFilePath2;

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

    public void deleteMyPlayer() {
        Platform.runLater( () ->
        {
         //   player.stop();
        //    player.dispose();//release file handle
            player = null;
        });
    }

    public void deleteMyFile() {
        Platform.runLater( () ->
        {
            File file = new File(myFilePath2);


            try {
                OutputStream targetFile=
                    new FileOutputStream(
                        "C:\\test\\"+songName+".mp3");

                String name = songName + ".mp3";
                targetFile.write(0);
                targetFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(file.delete()){
                System.out.println(file.getName() + " is deleted!");
            }else{
                System.out.println(file.getName() + " Delete operation failed.");
            }
        });
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
            myFilePath = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");
            myFilePath2 = "C:\\test\\"+songName+".mp3";
            player = new MediaPlayer(new Media(myFilePath));
            player.setOnError( ()->
            {
                    System.out.println("Media error occurred: " + player.getError());
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
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
