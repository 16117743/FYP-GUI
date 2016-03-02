package model;

import com.sun.media.jfxmedia.control.VideoRenderControl;
import com.sun.media.jfxmedia.effects.AudioEqualizer;
import com.sun.media.jfxmedia.effects.AudioSpectrum;
import com.sun.media.jfxmedia.events.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by user on 07/02/2016.
 */
public class Song implements com.sun.media.jfxmedia.MediaPlayer{
    /**data*/
    private MediaPlayer player;
    private String artist;
    private String song;
    private byte[] songByte;
    private Boolean bool = true;
    private int id = 0;
    private String name = "0";

    /**contructors*/
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
        } catch (IOException e) {
            e.printStackTrace();
        }
}

/********* Getters and Setters *******************/

    public boolean getBool(){return bool;}

    public void setBool(boolean bool1){bool = bool1;}

    public void setBool(Boolean bool) {this.bool = bool;}

    public MediaPlayer getPlayer(){return player;}

    public String getArtist(){return artist;}

    public String getSong(){return song;}

    public void setSong(String song) {this.song = song;}

    public void setArtist(String artist) {this.artist = artist;}

    public void setByteArray(byte[]fileBytes){songByte = fileBytes;}

    /*********************************************************/

@Override
public void addMediaErrorListener(MediaErrorListener listener) {

}

@Override
public void removeMediaErrorListener(MediaErrorListener listener) {

}

@Override
public void addMediaPlayerListener(PlayerStateListener listener) {

}

@Override
public void removeMediaPlayerListener(PlayerStateListener listener) {

}

@Override
public void addMediaTimeListener(PlayerTimeListener listener) {

}

@Override
public void removeMediaTimeListener(PlayerTimeListener listener) {

}

@Override
public void addVideoTrackSizeListener(VideoTrackSizeListener listener) {

}

@Override
public void removeVideoTrackSizeListener(VideoTrackSizeListener listener) {

}

@Override
public void addMarkerListener(MarkerListener listener) {

}

@Override
public void removeMarkerListener(MarkerListener listener) {

}

@Override
public void addBufferListener(BufferListener listener) {

}

@Override
public void removeBufferListener(BufferListener listener) {

}

@Override
public void addAudioSpectrumListener(AudioSpectrumListener listener) {

}

@Override
public void removeAudioSpectrumListener(AudioSpectrumListener listener) {

}

@Override
public VideoRenderControl getVideoRenderControl() {
    return null;
}

@Override
public com.sun.media.jfxmedia.Media getMedia() {
    return null;
}

@Override
public void setAudioSyncDelay(long delay) {

}

@Override
public long getAudioSyncDelay() {
    return 0;
}

@Override
public void play() {

}

@Override
public void stop() {

}

@Override
public void pause() {

}

@Override
public float getRate() {
    return 0;
}

@Override
public void setRate(float rate) {

}

@Override
public double getPresentationTime() {
    return 0;
}

@Override
public float getVolume() {
    return 0;
}

@Override
public void setVolume(float volume) {

}

@Override
public boolean getMute() {
    return false;
}

@Override
public void setMute(boolean enable) {

}

@Override
public float getBalance() {
    return 0;
}

@Override
public void setBalance(float balance) {

}

@Override
public AudioEqualizer getEqualizer() {
    return null;
}

@Override
public AudioSpectrum getAudioSpectrum() {
    return null;
}

@Override
public double getDuration() {
    return 0;
}

@Override
public double getStartTime() {
    return 0;
}

@Override
public void setStartTime(double streamTime) {

}

@Override
public double getStopTime() {
    return 0;
}

@Override
public void setStopTime(double streamTime) {

}

@Override
public void seek(double streamTime) {

}

@Override
public PlayerStateEvent.PlayerState getState() {
    return null;
}

@Override
public void dispose() {

}
}
