package model;

/**
 * Created by user on 06/03/2016.
 */
public class SongBean {

    public String song;
    public String artist;
    public int votes;
    public String DJComment;

public String getDJComment() {
    return DJComment;
}

public void setDJComment(String DJComment) {
    this.DJComment = DJComment;
}


    public String getSong() {
        return song;
    }

    public void setSong(String song) {
        this.song = song;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
