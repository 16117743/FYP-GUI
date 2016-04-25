package com.framework;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

/***********************
 * Author: Thomas Flynn
 * Final Year Project: Music Host Interface
 * Date: 25/04/16
 * Description: Creates stack pane, Loads the loginView & mainView FXML files onto the stackpane.
 * Launches the application.
 */
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
}
