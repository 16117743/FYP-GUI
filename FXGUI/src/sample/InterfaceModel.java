package sample;

import javafx.scene.control.ProgressBar;
import model.Model;

/**
 * Created by user on 13/02/2016.
 */
public interface InterfaceModel {

    //public void setModel(ProgressBar bar);

    public void iPlay();
    public void iSkip();
    public void iAddToQueue();
    public void iRemoveFromQueue();
    public void iLogout();

}
