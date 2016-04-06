package Interface;

/**
 * Created by user on 27/03/2016.
 */
public interface MusicHostInterface {
    void AvailableOptionsTx(String msg);
    String AvailableOptionsRx();

    void SongSelectionTx(String msg);
    String SongSelectionRx();

    void SongSelectedTx(String msg);
    String SongSelectedRx();

    void DJCommentTx(String msg);
    String DJCommentRx();

    void SkipSongTX();
    void SkipSongRx();

    boolean sendMessageByBluetooth(String msg, int whatToDo);
}
