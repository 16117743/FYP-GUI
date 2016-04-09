package sample;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class LoginSceneController implements Initializable, ControlledScreen {

    ScreensController myController;

    @FXML
    private TextField userLogin;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label response;

    @FXML
    private Rectangle loginRect;

    @FXML
    private Text musicHostShape;

    private FadeTransition musicHostTextFade;

    private FadeTransition loginShapeFade;

    private PathTransition pathTransitionEllipse;
    private PathTransition pathTransitionCircle;

    @FXML
    Path path2;

    /**Initializes the controller class.*/
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert userLogin != null : "login not injected!";
        assert passwordField != null : "login not injected!";

        userLogin.setTooltip(new Tooltip("Enter username"));
        passwordField.setTooltip(new Tooltip("Enter password"));

        musicHostTextFade = new FadeTransition(Duration.seconds(1.5),musicHostShape);
        musicHostTextFade.setFromValue(1.0);
        musicHostTextFade.setToValue(0.5);
        musicHostTextFade.setCycleCount(Animation.INDEFINITE);
        musicHostTextFade.setAutoReverse(true);
        musicHostTextFade.play();

        path2 = createEllipsePath(25, 55, 40, 40, 0);

        pathTransitionCircle = PathTransitionBuilder.create()
            .duration(Duration.seconds(2))
            .path(path2)
            .node(loginRect)
            .orientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT)
            .cycleCount(Timeline.INDEFINITE)
            .autoReverse(false)
            .build();

        pathTransitionCircle.play();

       // myController.getReferenceToLoginRect(loginRect, musicHostTextFade, pathTransitionCircle);

    }

    private Path createEllipsePath(double centerX, double centerY, double radiusX, double radiusY, double rotate) {
        ArcTo arcTo = new ArcTo();
        arcTo.setX(centerX - radiusX + 1); // to simulate a full 360 degree celcius circle.
        arcTo.setY(centerY - radiusY);
        arcTo.setSweepFlag(false);
        arcTo.setLargeArcFlag(true);
        arcTo.setRadiusX(radiusX);
        arcTo.setRadiusY(radiusY);
        arcTo.setXAxisRotation(rotate);

        Path path = PathBuilder.create()
            .elements(
                new MoveTo(centerX - radiusX, centerY - radiusY),
                arcTo,
                new ClosePath()) // close 1 px gap.
            .build();
        path.setStroke(Color.DODGERBLUE);
        path.getStrokeDashArray().setAll(5d, 5d);
        return path;
    }

    public void setScreenParent(ScreensController screenParent){
    myController = screenParent;
    }

    @FXML
    private void goToScreen2(ActionEvent event){
        //myController.setScreen(MusicHostFramework.mainScreenID);
        Login();
    }

    @FXML
    private void testAnim(ActionEvent event){
        myController.setScreen(MusicHostFramework.mainScreenID);
    }

    private void Login(){
        myController.getReferenceToLoginRect(loginRect, musicHostTextFade, pathTransitionCircle);
        String user = userLogin.getText();
        String pw = passwordField.getText();
        int check = -1; // = myController.confirmLogin(user,pw);

        Task<Integer> task1 = new Task<Integer>() {
            final String Tpw = pw;
            final String Tuser = user;
            @Override protected Integer call() throws Exception {
                return myController.confirmLogin(Tuser,Tpw);
            }
        };
        new Thread(task1).start();
        try {
            check = task1.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //if it's a valid log , run thread to display green rectangle for 2.5 secs before logging in
        if(check!=-1){
            myController.setUserID(check);
            Task task = new Task<Void>()
            {
                @Override public Void call()
                {
                    try
                    {
                        Platform.runLater(() ->
                        {
                            loginRect.setFill(Color.GREEN);
                        });
                        Thread.sleep(2500);
                        Platform.runLater(() ->
                        {
                            musicHostTextFade.stop();
                            pathTransitionCircle.stop();
                            myController.setScreen(MusicHostFramework.mainScreenID);
                        });
                    }
                    catch (Exception e) {e.printStackTrace();}
                    return null;
                }
            };
            new Thread(task).start();
        }
        else {
            Task task = new Task<Void>()
            {
                @Override public Void call()
                {
                    try
                    {
                        Platform.runLater(() ->
                        {
                            loginRect.setFill(Color.RED);
                        });
                        Thread.sleep(2500);
                        Platform.runLater(() ->
                        {
                            loginRect.setFill(Color.DARKBLUE);
                        });
                    }
                    catch (Exception e) {e.printStackTrace();}
                    return null;
                }
            };
            new Thread(task).start();
        }
    }

}
