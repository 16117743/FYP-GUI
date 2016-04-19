package com.framework;

import java.net.URL;
import java.util.ResourceBundle;
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

/***********************
 * Author: Thomas Flynn
 * Final Year Project: Music Host Interface
 * date: 25/04/16
 **********************/

/**
 * Controller for the login view<br>
 */
public class LoginSceneController implements Initializable, ControlledScreen {

    ScreensController myController;

    @FXML
    private TextField userLogin;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Rectangle loginRect;

    @FXML
    private Text musicHostShape;

    private FadeTransition musicHostTextFade;

    private PathTransition pathTransitionCircle;

    @FXML
    Path loginRectPath;

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

        loginRectPath = createEllipsePathForLogin(25, 55, 40, 40, 0);

        pathTransitionCircle = PathTransitionBuilder.create()
            .duration(Duration.seconds(2))
            .path(loginRectPath)
            .node(loginRect)
            .orientation(PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT)
            .cycleCount(Timeline.INDEFINITE)
            .autoReverse(false)
            .build();

        pathTransitionCircle.play();
    }

    private Path createEllipsePathForLogin(double centerX, double centerY, double radiusX, double radiusY, double rotate) {
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
    private void loginAction(ActionEvent event){
        Login();
    }

    private void Login() {
        myController.getReferenceToLoginRect(loginRect, musicHostTextFade, pathTransitionCircle);
        String user = userLogin.getText();
        String pw = passwordField.getText();
        boolean check;

        Task task = new Task<Void>()
        {
            final String Tpw = pw;
            final String Tuser = user;
            @Override public Void call()
            {
                if(myController.confirmLogin(Tuser, Tpw))
                {
                    try {
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
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        Platform.runLater(() ->
                        {
                            //set red for incorrect login
                            loginRect.setFill(Color.RED);
                        });
                        Thread.sleep(2500);
                        Platform.runLater(() ->
                        {
                            loginRect.setFill(Color.DARKBLUE);
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        new Thread(task).start();

    }
}
