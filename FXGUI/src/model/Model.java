package model;

import Interface.MainInterface;
import ignore.Ignore;
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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Model implements MainInterface{

    final int LOGIN_STATE = 0;
    final int MAIN_STATE = 1;
    final int DJ_STATE = 2;

    /**GUI related*/
    List<QueueSong> songQueue = new ArrayList<>();
    List<SelectionSong> selection = new ArrayList<>();

    /** Azure DB related*/
    private String con;
    private Ignore ignore;
    private Connection connection;

    /** Server related */
    private ReadWriteLock rwlock;
    volatile String input = "";

    final BlockingQueue<String> messageQueue;

    /** Enum state */
    boolean[] boolArray;

    ProcessConnectionThread processThread;
    boolean connectionThreadRunning = false;

    private int UserID = 2;

    /**Constructor*/
    public Model() {
        messageQueue = new ArrayBlockingQueue<>(1);
        rwlock = new ReentrantReadWriteLock();
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
            boolArray = new boolean[3];
            boolArray[LOGIN_STATE] = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /******Database related *****************************************************************************/
    public int confirmLogin(String user, String pw){
        try
        {
            final String query = "SELECT id from UserLogin " +
                "WHERE userName = '" + user + "' " +
                "AND password = '" + pw + "'";

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            if (rs.next()) {
                int test2 = rs.getInt(1);
                System.out.println(test2);
                return test2;
            }
                else
                    return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

/**
 * S_id int NOT NULL PRIMARY KEY,
 songName VARCHAR(MAX),
 artistName VARCHAR(MAX),
 dataSize INT(MAX),
 songData VARBINARY(MAX),
 id INT NOT NULL FOREIGN KEY REFERENCES UserLogin1(id)
 */


    public void initSongs() {
        try
        {
            final String query = "select S_Id , songname, artistname  from UserSongs"; // where Id ="+ Integer.toString(UserID);
            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            while (rs.next())
            {
                int sid = rs.getInt(1);
                String songTitle = rs.getString(2);
                String songArtist = rs.getString(3);
                selection.add(new SelectionSong(songTitle,songArtist, sid));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Json();
    }

    public QueueSong addSongToQueue(int index1) // enum state 1
    {
        try
        {
            int songForeignKey = selection.get(index1).getId();
            final String query = "select data from UserSongs where S_Id = " + Integer.toString(songForeignKey);

            Statement state = connection.createStatement();
            ResultSet rs = state.executeQuery(query);

            if (rs.next()) {
                byte[] fileBytes = rs.getBytes(1);
                //songQueue.add(new QueueSong(selection.get(index1),fileBytes));
                return new QueueSong(selection.get(index1),fileBytes);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*******Database related *****************************************************************************/

    /**Main Interface*/
    @Override
    public void playSong(Class instance) {
        if(instance.equals(sample.DJScreenController.class)) {
            System.out.print("DJ\n");
        }
        else if(instance.equals(sample.MainSceneController.class)) {
            System.out.print("\nMain\n");
          //  songQueue.get(0).playMe();
        }
    }

    @Override
    public void skipSong() {
        try {
            if (songQueue.size() > 0) {
            //    songQueue.get(0).skipMe();
            //    songQueue.get(1 % songQueue.size()).playMe();
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
        //songQueue.get(0).pauseMe();
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

    public boolean getBoolArray(int arg) {
        return boolArray[arg];
    }

    public List<QueueSong> getSongQueue() {return songQueue;}

    public List<SelectionSong> getSelection() {return selection;}

    public void setBoolArray(int arg,boolean bool) {
        for(int i=0;i<3;i++)
            boolArray[i] = false;

        this.boolArray[arg] = bool;
    }

    public void setUserID(int userID) {UserID = userID;}

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
       // System.out.print( jsonAraay.toString());

        return  jsonAraay.toString();
    }


    public void createQueue(){
//        MessageProducer producer = new MessageProducer(messageQueue);
//        Thread t = new Thread(producer);
//        t.setDaemon(true);
//        t.start();
    }

    public synchronized String pollQueue(){return messageQueue.poll();}

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
                String msg = null;
                while (true)
                {
                    msg = readSongRequest();
                    if(msg!=null)
                        messageQueue1.put(msg);

                    Thread.sleep(100);
                }
            } catch (InterruptedException exc) {
                System.out.println("Message producer interrupted: exiting.");
            }
        }//end run
    }//Message Producer class
    /*******************************************************************/
    public class ProcessConnectionThread implements Runnable {
        private volatile StreamConnection mConnection;
        private volatile Thread volatileThread;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;

        public ProcessConnectionThread(StreamConnection connection)
        {
            mConnection = connection;
        }

        @Override
        public void run() {
            volatileThread = Thread.currentThread();
            Thread thisThread = Thread.currentThread();
            try
            {
                dataInputStream = new DataInputStream(mConnection.openInputStream());
                dataOutputStream = new DataOutputStream(mConnection.openOutputStream());
                System.out.println("waiting for input");
                int whatToDo = 0;

                    try {
                        while (volatileThread == thisThread) {
                            try {
                                whatToDo = dataInputStream.readInt();
                                thisThread.sleep(100);
                            } catch (InterruptedException e) {
                                System.out.print("exited through here");
                            }
                            /****************/
                            if (dataInputStream.available() > 0) {
                                whatToDo(whatToDo);
                            }
                        }
                    } catch (Exception e) {}

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public synchronized void myStop(){
            volatileThread = null;
        }

        public synchronized void whatToDo(int whatToDo)
        {
            switch(whatToDo)
            {
                case -1:
                    System.out.print("\n got -1");
                    break;
                case 0:
                    System.out.print("\n got 0");
                    break;
                case 1:
                    System.out.print("\n got 1");
                    break;
                case 2:
                    System.out.print("\n got 2");
                    procInput();
                    break;
            }
        }

        public synchronized void respondOK(){

        }

        public synchronized void procInput(){
            try {
                byte[] msg = new byte[dataInputStream.available()];
                dataInputStream.read(msg, 0, dataInputStream.available());
                String msgstring = new String(msg);
                writeSongRequest(msgstring);
               // input = msgstring;
                System.out.print(msgstring + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public synchronized boolean sendMessageByBluetooth(String msg,int whatToDo)
        {
            try
            {
                if(dataOutputStream != null){
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

        public synchronized void writeSongRequest(String request){
            rwlock.writeLock().lock();
            try {
                input = request;
            } finally {
                rwlock.writeLock().unlock();
            }
        }
    }//end connection thread class

    public synchronized String readSongRequest(){
        rwlock.readLock().lock();
        try {
            if(input !=null) {
                String temp = input;
               // input = null;
                return temp;
            }
            else
                return null;
        } finally {
            rwlock.readLock().unlock();
        }
    }

    public synchronized void stopConnection() {
        //if(!connectionThreadRunning)
        if (processThread != null){
            synchronized (processThread) {
                processThread.myStop();
            }
            connectionThreadRunning = false;
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

                while (flag == false) {
                    try {
                        System.out.println("waiting for connection...");
                        connection = notifier.acceptAndOpen();
                        System.out.println("connected!");
                        processThread = new ProcessConnectionThread(connection);
                        processThread.run();
                        System.out.print("\n exited!");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /***&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&***/
                //sendmsgbyblut(json,2);
            }
        }.start();//end new Thread();
    }//end try 1
    catch(Exception e)
    {
        Thread.currentThread().interrupt();
        return;
    }
    }

    public synchronized String isTrue(){
        if(processThread == null)
            return "false";
        else
            return "true";
    }
}















