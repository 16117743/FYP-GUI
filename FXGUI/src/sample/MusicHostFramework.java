package sample;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicHostFramework extends Application {
    
    public static String loginScrenID = "main";
    public static String loginScreenFile = "/View/Screen1.fxml";
    public static String mainScreenID = "screen2";
    public static String mainScreenFile = "/View/Screen2a.fxml";

    @Override
    public void start(Stage primaryStage) {
        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen(MusicHostFramework.loginScrenID, MusicHostFramework.loginScreenFile);

        mainContainer.loadScreen(MusicHostFramework.mainScreenID, MusicHostFramework.mainScreenFile);

        mainContainer.setScreen(MusicHostFramework.loginScrenID);

        Group root = new Group();//Constructs a group consisting of children.
        root.getChildren().addAll(mainContainer);/*Gets the list of children of this {Group}. return the list of children of this {Group}.*/
        Scene scene = new Scene(root);//add children to the scene

        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);//Specify the scene to be used on this stage.
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
 //   public static void main(String[] args) {
  //      launch(args);
 //   }
}
