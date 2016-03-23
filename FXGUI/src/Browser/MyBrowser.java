package Browser;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.net.URL;

public class MyBrowser extends Region {

HBox toolbar;
VBox toolbox;

 WebView webView = new WebView();
 WebEngine webEngine = webView.getEngine();

public MyBrowser(){
    //   this.webEngine = webEngine;
    //   this.webView = webView;

    final URL urlHello = getClass().getResource("hello.html");
    webEngine.load(urlHello.toExternalForm());

    webEngine.getLoadWorker().stateProperty().addListener(
        new ChangeListener<Worker.State>(){

            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if(newState == Worker.State.SUCCEEDED){
                    JSObject window = (JSObject)webEngine.executeScript("window");
                    window.setMember("app", new JavaApplication());
                }
            }
        });


    JSObject window = (JSObject)webEngine.executeScript("window");
    window.setMember("app", new JavaApplication());

    final TextField textField = new TextField ();
    textField.setPromptText("Hello! Who are?");

    Button buttonEnter = new Button("Enter");
    buttonEnter.setOnAction(new EventHandler<ActionEvent>(){

        @Override
        public void handle(ActionEvent arg0) {
            webEngine.executeScript( " updateHello(' " + textField.getText() + " ') " );
        }
    });

    Button buttonClear = new Button("Clear");
    buttonClear.setOnAction(new EventHandler<ActionEvent>(){

        @Override
        public void handle(ActionEvent arg0) {
            webEngine.executeScript( "clearHello()" );
        }
    });

    toolbar = new HBox();
    toolbar.setPadding(new Insets(10, 10, 10, 10));
    toolbar.setSpacing(10);
    toolbar.setStyle("-fx-background-color: #336699");
    toolbar.getChildren().addAll(textField, buttonEnter, buttonClear);

    toolbox = new VBox();
    //labelFromJavascript = new Label();
    toolbox.getChildren().addAll(toolbar);
    //labelFromJavascript.setText("Wait");

    getChildren().add(toolbox);
    getChildren().add(webView);

}

@Override
protected void layoutChildren(){
    double w = getWidth();
    double h = getHeight();
    double toolboxHeight = toolbox.prefHeight(w);
     layoutInArea(webView, 0, 0, w, h-toolboxHeight, 0, HPos.CENTER, VPos.CENTER);
    layoutInArea(toolbox, 0, h-toolboxHeight, w, toolboxHeight, 0, HPos.CENTER, VPos.CENTER);
}

public class JavaApplication {
    public void callFromJavascript(String msg) {
       // labelFromJavascript.setText("Click from Javascript: " + msg);
        System.out.println("from java script");
    }
}

}

