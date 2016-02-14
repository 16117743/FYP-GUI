package model;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by user on 13/02/2016.
 */
public class ServerModel {

    DataOutputStream dataOutputStream;
    String input = "";
   // private final BlockingQueue<String> messageQueue = null;
   final BlockingQueue<String> messageQueue;

    public ServerModel(){
        messageQueue = new ArrayBlockingQueue<>(1);
    }

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

public void createQueue(){
    MessageProducer producer = new MessageProducer(messageQueue);
    Thread t = new Thread(producer);
    t.setDaemon(true);
    t.start();
}

public String pollQueue(){
   return messageQueue.poll();
}

//public BlockingQueue<String> getMessageQueue() {
//    return messageQueue;
//}

    public class MessageProducer implements Runnable
    {
        private final BlockingQueue<String> messageQueue;

    public MessageProducer(BlockingQueue<String> messageQueue) {
        this.messageQueue = messageQueue;
    }

    @Override
    public void run()
    {
        long messageCount = 0;
        try
        {
            while (true)
            {
                final String message;
                // if(!input.equals(null)) {
                message = input;
                this.messageQueue.put(message);
                input = "";
                Thread.sleep(100);
                //  }
            }
        } catch (InterruptedException exc) {
            System.out.println("Message producer interrupted: exiting.");
        }
    }


    }


}

