package sample;
import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import model.*;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class Screen2Controller implements Initializable , ControlledScreen {
    ScreensController myController;
   // private TemperatureSensor sensor;

    final List<Song> Songs = new ArrayList<>();
    final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
    final Label currentlyPlaying = new Label();
    final ProgressBar progress = new ProgressBar();
    private ChangeListener<Duration> progressChangeListener;
   // MediaView mediaView;
   // MediaView mediaView = new MediaView();

    private String test = new String("");
    private String input = "";
    DataOutputStream dataOutputStream;
    /*****************************************/
    final Model model = new Model();
    final AtomicInteger count = new AtomicInteger(-1);
    final NumberFormat formatter = NumberFormat.getIntegerInstance();
    //formatter.setGroupingUsed(true);

    final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
    final LongProperty lastUpdate = new SimpleLongProperty();
    final long minUpdateInterval = 0 ;


    public class MessageProducer implements Runnable {
        private final BlockingQueue<String> messageQueue ;

        public MessageProducer(BlockingQueue<String> messageQueue) {
            this.messageQueue = messageQueue ;
        }

        @Override
        public void run() {
            long messageCount = 0 ;
            try {
                while (true) {
                    final String message;
                   // if(!input.equals(null)) {
                        message = input;
                        messageQueue.put(message);
                        input = "";
                        Thread.sleep(100);
                  //  }
                }
            } catch (InterruptedException exc) {
                System.out.println("Message producer interrupted: exiting.");
            }
        }
    }
    /*****************************************/

    @FXML
    Button playButton;

    @FXML
    MediaView mediaView;

    @FXML
    private Button skipButton; // value will be injected by the FXMLLoader

    @FXML
    private TextArea songRequest;

    @FXML
    private VBox vBox;

    public Screen2Controller(){
        // determine the source directory for the playlist
    }

    /*************************************************************************************************************************
     * Initializes the controller class.
     ****************************************************************************************************************/
    @Override
    public void initialize(URL url, ResourceBundle rb) {

//        System.out.println("screen2 intialized");
        assert skipButton != null : "fx:id=\"skipButton\" was not injected: check your FXML file 'simple.fxml'.";
        assert mediaView != null : "meh";
        assert playButton != null;
        assert songRequest != null : "songrequest not injected!";

        final File dir = new File("C:\\theset\\");
        if (!dir.exists() || !dir.isDirectory()) {
            System.out.println("Cannot find video source directory: " + dir);
            Platform.exit();
            //  return null;
        }

        // create some media players.
        //  final List<MediaPlayer> players = new ArrayList<MediaPlayer>();
        int ii = 0;
        for (String file : dir.list(new FilenameFilter() {@Override public boolean accept(File dir, String name) {return name.endsWith(".mp3");}}))
        {
            String path = "file:///" + (dir + "\\" + file).replace("\\", "/").replaceAll(" ", "%20");
            Songs.add(new Song(path));
            players.add(Songs.get(ii).getPlayer());
            ii++;
        }
        // %20 is immediately recognisable as a whitespace character -
        // while not really having any meaning in a URI it is encoded in order to avoid breaking the string into multiple "parts".

        if (players.isEmpty()) {
            System.out.println("No audio found in " + dir);
            Platform.exit();
            // return null;
        }

       // mediaView = new MediaView(players.get(0));
       mediaView.setMediaPlayer(players.get(0));
        // play each audio file in turn.
        for (int i = 0; i < players.size(); i++) {
            final MediaPlayer player     = players.get(i);
            final MediaPlayer nextPlayer = players.get((i + 1) % players.size());
            player.setOnEndOfMedia(new Runnable() {
                @Override public void run() {
                    player.currentTimeProperty().removeListener(progressChangeListener);
                    mediaView.setMediaPlayer(nextPlayer);
                    nextPlayer.play();
                }
            });
        }

        // display the name of the currently playing track.
        mediaView.mediaPlayerProperty().addListener(new ChangeListener<MediaPlayer>() {
            @Override public void changed(ObservableValue<? extends MediaPlayer> observableValue, MediaPlayer oldPlayer, MediaPlayer newPlayer) {
                setCurrentlyPlaying(newPlayer);
            }
        });

      //  mediaView.setMediaPlayer(players.get(0));
        //  mediaView.getMediaPlayer().play();
        setCurrentlyPlaying(mediaView.getMediaPlayer());

        input = "";

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if (now - lastUpdate.get() > minUpdateInterval) {
                    final String message = messageQueue.poll();
                    if (message != null && !message.equals("")) {
                            songRequest.appendText("\n" + message);
                    }
                    lastUpdate.set(now);
                }
            }

        };
        timer.start();

        /******????????????????????????????????????????????????????????????????**************/
    }
    
    public void setScreenParent(ScreensController screenParent){
        myController = screenParent;
    }
/******************************************************************************************************
 *
 * **********************************************************************************/


    @FXML
    private void refreshMethod(ActionEvent event){
        // System.out.println("refresh");
        //model.start();
        MessageProducer producer = new MessageProducer(messageQueue);
        Thread t = new Thread(producer);
        t.setDaemon(true);
        t.start();

    }




    /******************************************************************************************/

    public boolean sendMessageByBluetooth(String msg){
        try {
            if(dataOutputStream != null){
                dataOutputStream.write(msg.getBytes());
                dataOutputStream.flush();
                return true;
            }else{
              //  sendHandler(ChatActivity.MSG_TOAST, context.getString(R.string.no_connection));
                return false;
            }
        } catch (IOException e) {
         //   LogUtil.e(e.getMessage());

         //   sendHandler(ChatActivity.MSG_TOAST, context.getString(R.string.failed_to_send_message));
            return false;
        }
    }

    public void doThreadStuff(){
            try
            {
                new Thread(){
                    public void run() {
                        Boolean flag = false;
                        // retrieve the local Bluetooth device object
                        StreamConnectionNotifier notifier = null;
                        StreamConnection connection = null;
                        String localInput = null;

                        try {
                            LocalDevice local = null;
                            local = LocalDevice.getLocalDevice();
                            local.setDiscoverable(DiscoveryAgent.GIAC);
                            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
                            String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
                            notifier = (StreamConnectionNotifier) Connector.open(url);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        connection = null;
                        // waiting for connection
                        while (flag == false) {
                            try {
                                System.out.println("waiting for connection...");
                                connection = notifier.acceptAndOpen();
                                System.out.println("connected!");
                                flag = true;
                                //Thread processThread = new Thread(new ProcessConnectionThread(connection));
                                // processThread.start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        try {
                            DataInputStream dataInputStream = new DataInputStream(connection.openInputStream());
                            dataOutputStream = new DataOutputStream(connection.openOutputStream());

                            System.out.println("waiting for input");
                            while (true) {
                                if (dataInputStream.available() > 0) {
                                    byte[] msg = new byte[dataInputStream.available()];
                                    dataInputStream.read(msg, 0, dataInputStream.available());
                                    String msgstring = new String(msg);
                                    input = msgstring;
                                    System.out.print(msgstring + "\n");
                                    sendMessageByBluetooth(msgstring);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }.start();
}
            catch(
            Exception e
            )

            {
                e.printStackTrace();
            }
            //}
        }

    @FXML
    private void startServer(ActionEvent event) {

        doThreadStuff();
        MessageProducer producer = new MessageProducer(messageQueue);
        Thread t = new Thread(producer);
        t.setDaemon(true);
        t.start();
        }

    /*******************MUSIC button methods **************************************************/
    @FXML
    private void play(ActionEvent event){
        if ("Pause".equals(playButton.getText())) {
            mediaView.getMediaPlayer().pause();
            playButton.setText("Play");
        } else {
            mediaView.getMediaPlayer().play();
            playButton.setText("Pause");
        }
    }

    @FXML
    private void skipMethod(ActionEvent event){
        skipButton.setText("Skip");
        final MediaPlayer curPlayer = mediaView.getMediaPlayer();
        MediaPlayer nextPlayer = players.get((players.indexOf(curPlayer) + 1) % players.size());
        mediaView.setMediaPlayer(nextPlayer);
        curPlayer.currentTimeProperty().removeListener(progressChangeListener);
        curPlayer.stop();
        nextPlayer.play();
    }

            /*******************MUSIC MODEL **************************************************/


            private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
                progress.setProgress(0);
                progressChangeListener = new ChangeListener<Duration>() {
                    @Override
                    public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                        progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
                    }
                };
                newPlayer.currentTimeProperty().addListener(progressChangeListener);

                String source = newPlayer.getMedia().getSource();
                source = source.substring(0, source.length() - ".mp4".length());
                source = source.substring(source.lastIndexOf("/") + 1).replaceAll("%20", " ");
                currentlyPlaying.setText("Now Playing: " + source);
            }

            private MediaPlayer createPlayer(String aMediaSrc) {
                //System.out.println("Creating player for: " + aMediaSrc);
                final MediaPlayer player = new MediaPlayer(new Media(aMediaSrc));
                player.setOnError(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Media error occurred: " + player.getError());
                    }
                });
                return player;
            }

    @FXML
    private void goToScreen1(ActionEvent event){
        myController.setScreen(ScreensFramework.screen1ID);
    }

    @FXML
    private void goToScreen3(ActionEvent event){
        myController.setScreen(ScreensFramework.screen3ID);
    }
        }
