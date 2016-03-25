/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License"). You
 * may not use this file except in compliance with the License. You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package sample;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import Browser.MyBrowser;
import Interface.InterfaceDJ;
import Interface.MainInterface;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import model.AzureDB;
import model.Model;



public class DJScreenController implements Initializable, ControlledScreen, InterfaceDJ, MainInterface {

    ScreensController myController;
    Model mainModel;
    AzureDB db;
    public boolean  bool1;

    public DJScreenController()
    {
        Task animate2 = new Task<Void>() {
            @Override public Void call() {
                while(true) {
                    try {
                        Thread.sleep(1000);
                        if (mainModel != null) {
                            final String message = mainModel.readSongRequest();
                            System.out.print("\n DJ returned " );
                            if (message != null && !message.equals("")) {
                                Platform.runLater(() -> {
                                    songRequest2.appendText("\n" + message);
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        System.out.print("\n interrupted");
                        e.printStackTrace();
                    }
                }
            }
        };
      //  new Thread(animate2).start();
    }

    @FXML
    Button playBtn;

    @FXML
    Button skipBtn;

    @FXML
    Button addBtn;

    @FXML
    Button logoutBtn;

    @FXML
    Button switchBtn;

    @FXML
    ListView songList2;

    @FXML
    ListView queueList2;

    @FXML
    TextArea songRequest2;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        assert playBtn != null : "playBtn not injected!";;
        assert skipBtn != null : "skipBtn not injected!";;
        assert addBtn != null : "addBtn not injected!";
        assert logoutBtn != null : "logoutBtn not injected!";
        assert switchBtn != null : "switchBtn not injected!";
        assert songList2 != null : "DJSelection not injected!";
        assert queueList2 != null : "DJSongQueue not injected!";
        assert songRequest2 != null : "DJRequests not injected!";
        bool1 = true;


    }



    @FXML
    private void goToScreen1(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen1ID);
    }
    
    @FXML
    private void goToScreen2(ActionEvent event){
        myController.setScreen(MusicHostFramework.screen2ID);
    }

    /********************************************************/
    public void iPlay() {
        Platform.runLater(() -> {
            mainModel.doThreadStuff();
            songRequest2.appendText("\ntests");
        });
//        bool1 = false;
//        System.out.println("test interface play");
//        mainModel.playSong(this.getClass());
//        if ("skipBtn".equals(playBtn.getText())) {
//            playBtn.setText("PlayBtn");
//        }
//        else if ("*****".equals(skipBtn.getText())) {
//            playBtn.setText("skipBtn");
//        }
    }

    public void iSkip() {
        System.out.println("test interface skip");
        mainModel.stopConnection();

      //  mainModel.skipSong();
    }

    public void iAddToQueue() {
        System.out.println("test interface add");
        Platform.runLater(new Runnable() {
            @Override public void run() {
                for (int i = 0; i < 5; i++) {
                    songList2.getItems().add(mainModel.getSongInfo(i));//update gui with selection info
                    songList2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
                }
            }
        });
    }

    public void iRemoveFromQueue() {
        System.out.println("test interface remove");
    }

    public void iLogout() {
        System.out.println("test interface logout");
    }

    /**DJ Interface*/
    @Override
    public void DJplayMe() {

    }

    @Override
    public void DJskipMe() {
        System.out.println("test interface skip");
        mainModel.skipSong();
    }

    @Override
    public void DJpauseMe() {

    }

    @Override
    public void DJfadeMeIn(float deltaTime) {

    }

    @Override
    public void DJfadeMeOut(float deltaTime) {

    }

    @Override
    public void DJDoSomething() {

    }
    /******************/

    @Override
    public void playSong(Class instance) {
      //  System.out
    }

    @Override
    public void skipSong() {

    }

    @Override
    public void pauseSong() {

    }

    /***??????????????????????????????????????????????????***/
    public void setScreenParent(ScreensController screenParent, Model model, AzureDB database){
        myController = screenParent;
        mainModel = model;
        db = database;
    }

    public void setBrowser(MyBrowser myBrowser){

    }
}
