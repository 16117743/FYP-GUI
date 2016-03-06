package Interface;

import javafx.scene.control.ProgressBar;
import model.Model;

/**
 * Created by user on 13/02/2016.
 */
public interface SongInterfaceForModel {
    public void initMe();
    public void downloadMe();
    public void prepareMe();
    public void deleteMe();
    public int getProgress();

    public void iSkip();
}
