package closingProgram;


import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import comWithWindows.ConfirmBox;

public class Main extends Application {

    Stage window;
    Button button;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("JavaFX");

        window.setOnCloseRequest(e -> {
            e.consume();// handling event manually through our method
            closeProgram();
        });

        button = new Button("Click Me");
        button.setOnAction(e -> closeProgram());

        StackPane layout = new StackPane();
        layout.getChildren().add(button);
        Scene scene = new Scene(layout, 300, 250);
        window.setScene(scene);
        window.show();
    }

    private void closeProgram(){
        Boolean answer = ConfirmBox.display("title", "are sure you want to exit?");
        System.out.println("file saved before closing");
        if(answer)
            window.show();
    }
}

