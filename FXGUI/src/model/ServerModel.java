package model;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 13/02/2016.
 */
public class ServerModel {

    DataOutputStream dataOutputStream;
    String input = "";
    final BlockingQueue<String> messageQueue;
    JSONArray jsonArray;
    Gson gson;
    JSONArray jsArray;
    String jsonStr;

    /**contructor*/
    public ServerModel(){
        messageQueue = new ArrayBlockingQueue<>(1);

       // jsonArray = new JSONArray(readlocationFeed);
        gson = new Gson();

        //ArrayList<String> arrayList= (ArrayList<String>) ClassName.getArrayList();

// Serializing to a JSON element node
    //    JsonElement jsonElement = gson.toJsonTree(arrayList);
     //   System.out.println(jsonElement.isJsonArray()); // true

// Or, directly to JSON string
     //   String json = gson.toJson(arrayList);
     //   System.out.println(json);

        ArrayList<String> list = new ArrayList<String>();
        list.add("blah1");
        list.add("bleh2");
        jsArray = new JSONArray(list);
        jsonStr = jsArray.toString();
    }

    public boolean sendMessageByBluetooth(String msg){
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
                    sendMessageByBluetooth("");
                    while (true)
                    {
                        if (dataInputStream.available() > 0) {
                            byte[] msg = new byte[dataInputStream.available()];
                            dataInputStream.read(msg, 0, dataInputStream.available());
                            String msgstring = new String(msg);
                            input = msgstring;
                            System.out.print(msgstring + "\n");
                            sendMessageByBluetooth(msgstring);
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
    createQueue();
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
        private final BlockingQueue<String> messageQueue;
        /** constructor*/
         public MessageProducer(BlockingQueue<String> messageQueue) {this.messageQueue = messageQueue;}

        @Override
        public void run()
        {
            try
            {
                while (true)
                {
                    final String message;
                    message = input;
                    this.messageQueue.put(message);
                    input = "";
                    Thread.sleep(100);
                }
            } catch (InterruptedException exc) {
                System.out.println("Message producer interrupted: exiting.");
            }
        }//end run
    }//Message Producer class
}//end server model

