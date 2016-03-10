package model;

import Interface.MainInterface;
import com.google.gson.Gson;
import ignore.Ignore;
import javafx.scene.media.MediaPlayer;
import org.json.JSONArray;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Model implements MainInterface{

    /**GUI related*/
    List<Song> songQueue = new ArrayList<>();
    List<Song> selection = new ArrayList<>();

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    /** Server related */
    private ServerModel serverModel;
    DataOutputStream dataOutputStream;
    String input = "";
    final BlockingQueue<String> messageQueue;
    String jsonStr;

    /**Constructor*/
    public Model() {
        messageQueue = new ArrayBlockingQueue<>(1);
        init();

    }

    /**Constructor methods*/
    public void init() {
        try {
            createQueue();
            ignore = new Ignore();
            con = ignore.getCon();
            String driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            Class.forName(driver);
            connection = DriverManager.getConnection(con);
            serverModel = new ServerModel();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /******Database related *****************************************************************************/


    public void initSongs() {
        try
        {
            final String query = "select songname from UserSongs";//where songName = 'song3'
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            while (rs.next())
            {
                String test = rs.getString(1);
                System.out.println(test);
                selection.add(new Song(test, selection.size()));//call 2 arg song constructor
                System.out.println("\nselection size =" + selection.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Json();
    }

    public void downloadSong(int index1) // enum state 1
    {
        try
        {
            final String indexStr = Integer.toString(index1);
            final String query = "select data from UserSongs where S_Id = " + indexStr;

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);
            System.out.println("\nselection size =" + selection.size());

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                selection.get(index1).setByteArray(fileBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void addSong(int index) {
        if (!selection.get(index).getBool())
        {
            selection.get(index).createPlayer2();
            selection.get(index).setBool(true);
            songQueue.add(selection.get(index));
        }
    }
    /*******Database related *****************************************************************************/

    /**Main Interface*/
    @Override
    public void playSong(Class instance) {
        if(instance.equals(sample.DJScreenController.class)) {
            System.out.print("DJ\n");
        }
        else if(instance.equals(sample.MainSceneController.class)) {
            System.out.print("Main\n");
        }
    }

    @Override
    public void skipSong() {
        try {
            if (songQueue.size() > 0) {
                songQueue.get(0).skipMe();
                songQueue.get(1 % songQueue.size()).playMe();
            }
            else
                System.out.print("\n no songs in queue");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            songQueue.remove(0);
        }
    }

    @Override
    public void pauseSong() {
        songQueue.get(0).pauseMe();
    }

    /**DJ interface**/



    /** Getters and setters*/
    public BlockingQueue<String> getMessageQueue() {
        return messageQueue;
    }

    public String getSongInfo(int index) {
        return selection.get(index).getSong();
    }

    public int getSelectionSize() {
        return selection.size();
    }

    /***********************SERVER CODE ********************************************************/
    public String Json(){
        ArrayList<SongBean> beanList = new ArrayList();
        for(int i =0; i<selection.size();i++){
            SongBean sb = new SongBean();
            sb.setSong(selection.get(i).getSong());
            sb.setArtist("artist" + i);
            sb.setVotes(i);
            beanList.add(sb);
        }
        JSONArray jsonAraay = new JSONArray(beanList);
        System.out.print( jsonAraay.toString());

        return  jsonAraay.toString();
    }

    public boolean sendMessageByBluetooth(String msg,int whatToDo){
        try
        {
            if(dataOutputStream != null){
                // msg.getBytes() jsonStr

              //  dataOutputStream.write(msg.getBytes());
              //  dataOutputStream.flush();
                String test = "test1";
                /********************/
                dataOutputStream.writeInt(whatToDo);
                dataOutputStream.flush();
                dataOutputStream.write(msg.getBytes());
                dataOutputStream.flush();
                return true;
            }else{
                return false;
            }
        } catch (IOException e) {
            return false;
        }
    }

public boolean sendJsonByBluetooth(JSONArray msg){
    try
    {
        if(dataOutputStream != null){
            // msg.getBytes() jsonStr
            dataOutputStream.write(jsonStr .getBytes());
            dataOutputStream.flush();
            return true;
        }else{
            return false;
        }
    } catch (IOException e) {
        return false;
    }
}

public void doThreadStuff(){
    try
    {
        new Thread(){
            public void run() {
                Boolean flag = false;
                StreamConnectionNotifier notifier = null;
                StreamConnection connection = null;

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

                while (flag == false)
                {
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
                    sendMessageByBluetooth("testing123",1);
                    while (true)
                    {
                        if (dataInputStream.available() > 0) {
                            byte[] msg = new byte[dataInputStream.available()];
                            dataInputStream.read(msg, 0, dataInputStream.available());
                            String msgstring = new String(msg);
                            input = msgstring;
                            System.out.print(msgstring + "\n");

                            sendMessageByBluetooth("testing again",2);

                        }
                    }
                }//end try 2
                catch (IOException e) {
                    e.printStackTrace();
                }
            }//end run()
        }.start();//end new Thread();
    }//end try 1
    catch(Exception e)
    {
        e.printStackTrace();
    }

}

public void createQueue(){
    MessageProducer producer = new MessageProducer(messageQueue);
    Thread t = new Thread(producer);
    t.setDaemon(true);
    t.start();
}

public String pollQueue(){return messageQueue.poll();}

public class MessageProducer implements Runnable
{
    /** data*/
    private final BlockingQueue<String> messageQueue1;
    /** constructor*/
    public MessageProducer(BlockingQueue<String> messageQueue) {this.messageQueue1 = messageQueue;}

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                final String message;
                message = input;
                this.messageQueue1.put(message);
                input = "";
                Thread.sleep(100);
            }
        } catch (InterruptedException exc) {
            System.out.println("Message producer interrupted: exiting.");
        }
    }//end run
}//Message Producer class
}

/*
private void setCurrentlyPlaying(final MediaPlayer newPlayer) {
//    progress.setProgress(0);
//    progressChangeListener = new ChangeListener<Duration>() {
//        @Override
//        public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
//            progress.setProgress(1.0 * newPlayer.getCurrentTime().toMillis() / newPlayer.getTotalDuration().toMillis());
//        }                               //queueList.get(0).getCurrentTime() / queueList.get(0).getTotalDuration()
//    };
//
//    newPlayer.currentTimeProperty().addListener(progressChangeListener);
}*/


