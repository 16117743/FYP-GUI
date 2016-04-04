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
import java.net.URL;
import java.util.ResourceBundle;
import Interface.InterfaceDJ;
import Interface.MainInterface;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;
import model.Model;
import model.QueueSong;
import model.SelectionSong;

public class DJScreenController implements Initializable, ControlledScreen{
    ScreensController myController;
    Model mainModel;

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

    ObservableList<QueueSong> SongQueueObservableList;

    ObservableList<SelectionSong> SongSelectionObservableList;

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

        songList2.setCellFactory(new Callback<ListView<SelectionSong>, ListCell<SelectionSong>>(){

            @Override
            public ListCell<SelectionSong> call(ListView<SelectionSong> p) {

                ListCell<SelectionSong> cell = new ListCell<SelectionSong>(){

                    @Override
                    protected void updateItem(SelectionSong t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            // setText(t.getDay() + ":" + t.getNumber());
                            setText(t.getSong() + " by " + t.getArtist());
                        }
                    }
                };

                return cell;
            }
        });

        queueList2.setCellFactory(new Callback<ListView<QueueSong>, ListCell<QueueSong>>() {

            @Override
            public ListCell<QueueSong> call(ListView<QueueSong> p) {

                ListCell<QueueSong> cell = new ListCell<QueueSong>() {

                    @Override
                    protected void updateItem(QueueSong t, boolean bln) {
                        super.updateItem(t, bln);
                        if (t != null) {
                            // setText(t.getDay() + ":" + t.getNumber());
                            setText(t.getSong() + " votes= " + t.getVotes());
                        }
                    }
                };

                return cell;
            }
        });
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

    }

    public void iSkip() {
        System.out.println("test interface skip");
        SongQueueObservableList = FXCollections.observableList(mainModel.getSongQueue());
        queueList2.setItems(SongQueueObservableList);
        System.out.print(SongQueueObservableList);

        SongSelectionObservableList = FXCollections.observableList(mainModel.getSelection());
        System.out.print(SongSelectionObservableList);
        songList2.setItems(SongSelectionObservableList);
    }

    public void iRemoveFromQueue() {
        System.out.println("test interface remove");
    }

    public void iLogout() {
        System.out.println("test interface logout");
    }

    /***??????????????????????????????????????????????????***/
    public void setScreenParent(ScreensController screenParent, Model model){
        myController = screenParent;
        mainModel = model;
    }

}
