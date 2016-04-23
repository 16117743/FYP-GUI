package com.framework;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MusicHostFramework extends Application {
    
    public static String loginScreenID = "LoginView";
    public static String loginScreenFile = "/com/View/LoginView.fxml";
    public static String mainScreenID = "MainView";
    public static String mainScreenFile = "/com/View/MainView.fxml";

    @Override
    public void start(Stage primaryStage) {
        ScreensController mainContainer = new ScreensController();
        mainContainer.loadScreen(MusicHostFramework.loginScreenID, MusicHostFramework.loginScreenFile);

        mainContainer.loadScreen(MusicHostFramework.mainScreenID, MusicHostFramework.mainScreenFile);

        mainContainer.setScreen(MusicHostFramework.loginScreenID);

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
