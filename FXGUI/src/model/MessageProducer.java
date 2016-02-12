package model;

import java.util.concurrent.BlockingQueue;

class MessageProducer implements Runnable {
            private final BlockingQueue<String> messageQueue ;

            public MessageProducer(BlockingQueue<String> messageQueue) {
                this.messageQueue = messageQueue ;
            }

            @Override
            public void run() {
                long messageCount = 0 ;
                try {
                    while (true) {
                        final String message = "Message " + (++messageCount);
                        messageQueue.put(message);
                    }
                } catch (InterruptedException exc) {
                    System.out.println("Message producer interrupted: exiting.");
                }
            }
        }