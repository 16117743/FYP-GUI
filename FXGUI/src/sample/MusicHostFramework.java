package sample;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.AzureDB;


public class MusicHostFramework extends Application {
    
    public static String screen1ID = "main";
    public static String screen1File = "/View/Screen1.fxml";
    public static String screen2ID = "screen2";
    public static String screen2File = "/View/Screen2a.fxml";
    public static String screen3ID = "screen3";
    public static String screen3File = "/View/Screen3.fxml";

    @Override
    public void start(Stage primaryStage) {
        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen(MusicHostFramework.screen1ID, MusicHostFramework.screen1File);

        mainContainer.loadScreen(MusicHostFramework.screen2ID, MusicHostFramework.screen2File);

        mainContainer.loadScreen(MusicHostFramework.screen3ID, MusicHostFramework.screen3File);
        
        mainContainer.setScreen(MusicHostFramework.screen1ID);

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
