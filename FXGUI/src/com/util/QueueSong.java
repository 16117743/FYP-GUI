package com.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by user on 29/03/2016.
 */
public class QueueSong {

    /**data*/
    private String artist;
    private String songName;
    private Boolean preparedBool;
    private int azureForeignKey = 0;
    AtomicInteger votes = new AtomicInteger();

    /**Constructor for nth song added to queue*/
    public QueueSong(SelectionSong selectionSong, int songSelectionIndex)
    {
        this.azureForeignKey = selectionSong.getId();
        this.songName = selectionSong.getSong();
        this.artist = selectionSong.getArtist();
        this.votes.set(2);
        this.preparedBool = false;
    }

    /**Accessors*/
    public String getSong(){return songName;}
    public String getArtist(){return artist;}
    public int getVotes() {return votes.get();}

    public int getAzureForeignKey() {return azureForeignKey;}

    public Boolean getPreparedBool() {return preparedBool;}

    public int decrementAndGetVotes() {
        return votes.decrementAndGet();
    }

    public void incrementAntiSkipVote(){
       votes.incrementAndGet();
    }

    public void setPreparedBool(Boolean preparedBool) {this.preparedBool = preparedBool;}
}
