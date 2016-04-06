package sample;

import java.util.HashMap;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.Model;
import model.QueueSong;
import model.SelectionSong;

//keep pane so we can remove add screens on top/bottom
public class ScreensController  extends StackPane {
    //Holds the screens to be displayed
    private HashMap<String, Node> screens = new HashMap<>();//id of screen, represents root of the screen graph for that scene
    public ScreensController() {
        super();//inherit StackPane class
    }

    private Model model = new Model();

    //Add the screen to the collection
    public void addScreen(String name, Node screen) {
        screens.put(name, screen);
    }

    //Returns the Node with the appropriate name
    public Node getScreen(String name) {
        return screens.get(name);
    }

    //Loads the fxml file, add the screen to the screens collection and
    //finally injects the screenPane to the controller.
    public boolean loadScreen(String name, String resource) {
        try {                            //fxml file
            FXMLLoader myLoader = new FXMLLoader(getClass().getResource(resource));
            Parent loadScreen = (Parent) myLoader.load();//class cast to controlled screen

            ControlledScreen myScreenControler = ((ControlledScreen) myLoader.getController());//Returns the controller associated with the root object.
            //inject screen controllers to myscreencontroller
            myScreenControler.setScreenParent(this);// inject screen controllers to each screen here

            addScreen(name, loadScreen);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    //This method tries to displayed the screen with a predefined name.
    //First it makes sure the screen has been already loaded.  Then if there is more than
    //one screen the new screen is been added second, and then the current screen is removed.
    // If there isn't any screen being displayed, the new screen is just added to the root.
    public boolean setScreen(final String name) {
        //do we have a screen loaded?
        if (screens.get(name) != null) {   //screen loaded
            final DoubleProperty opacity = opacityProperty();

            if (!getChildren().isEmpty())// getChildren returns a modifiable list of children
            {    //if there is more than one screen
                Timeline fade = new Timeline
                        (
                            new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)),
                            new KeyFrame
                                (
                                        new Duration(100),// once fade out is finished, call eventhandler
                                    new EventHandler<ActionEvent>()
                                    {
                                        @Override
                                        public void handle(ActionEvent t)
                                        {
                                             getChildren().remove(0);                    //remove the displayed screen
                                            getChildren().add(0, screens.get(name));     //add the screen, returns node
                                            Timeline fadeIn = new Timeline(
                                            new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                                            new KeyFrame(new Duration(500), new KeyValue(opacity, 1.0)));
                                            fadeIn.play();
                                        }
                                    }, new KeyValue(opacity, 0.0)
                                )//end new keyframe
                        );//end time line fade
                fade.play();
            } //end if (!getChildren().isEmpty())
           else {
                setOpacity(0.0);   // getting tree structure of screen given by name
                            //then adding it to the scene graph
                getChildren().add(screens.get(name));       //no one else been displayed, then just show
                Timeline fadeIn = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)),
                        new KeyFrame(new Duration(100), new KeyValue(opacity, 1.0)));
                fadeIn.play();
            }
            return true;
        } else {
            System.out.println("screen hasn't been loaded!!! \n");
            return false;
        }
    }

    //This method will remove the screen with the given name from the collection of screens
    public boolean unloadScreen(String name) {
        if (screens.remove(name) == null) {
            System.out.println("Screen didn't exist");
            return false;
        } else {
            return true;
        }
    }

    /**Database methods*/
    public int confirmLogin(String user, String pw){
        return model.confirmLogin(user,pw);
    }

    public void initSongs() {
        model.initSongs();
    }

    public byte[] downloadSongBytes(int index1) {
        return model.downloadSongBytes(index1);
    }

    /** Getters and setters*/
    public List<QueueSong> getSongQueue() {return model.getSongQueue();}

    public List<SelectionSong> getSelection() {return model.getSelection();}

    public List getDJCommentsData() {return model.getDJCommentsData();}

    public void setUserID(int userID) {model.setUserID(userID);}

    public String songSelectionToJson(){return model.songSelectionToJson();}

    public int getUserID(){
        return model.getUserID();
    }

    public void clearValuesBeforeLoggingOut(){
        model.clearValuesBeforeLoggingOut();
    }

    /**
     * Returns the JSON of the song queue list
     * @return Returns the JSON of the song queue list
     */
    public String songQueueToJson(){return model.songQueueToJson();}

    /**
     * Returns the JSON of the DJ comments list
     * @return Returns the JSON of the DJ comments list
     */
    public String DJCommentToJson(){return model.DJCommentToJson();}
}

