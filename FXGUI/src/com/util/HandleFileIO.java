package com.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.Callable;

/**
 * Created by user on 02/04/2016.
 */
public class HandleFileIO implements Callable<MediaPlayer> {
    byte[] songData;
    String songName;
    public HandleFileIO(byte[] songData, String songName) {
        this.songData = songData;
        this.songName = songName;
    }

    @Override
    public MediaPlayer call() throws Exception {
        try
        {
            OutputStream targetFile=
                new FileOutputStream(
                    "C:\\test\\"+songName+".mp3");

            String name = songName + ".mp3";
            targetFile.write(songData);
            targetFile.close();
            String filePath = "file:///" + ("C:\\test\\" + "\\" + name).replace("\\", "/").replaceAll(" ", "%20");

            MediaPlayer player = new MediaPlayer(new Media(filePath));
            player.setOnError( ()->
            {
                System.out.println("Media error occurred: " + player.getError());
            });
            return player;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
