package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class Main {

    private static final Logger anonymousLogger = Logger.getAnonymousLogger();
    static final int PORT = 9001;
    static final int BUFFER_SIZE = 2048;
    public static volatile JsonCluster cluster;

    public static void main(String[] args) throws IOException, InterruptedException { /*
        cluster = new JsonCluster();
        try {
            Receiver receiver = new Receiver();
            Sender sender = new Sender();
            cluster.add(new Node(transmission.getPid(), "tcp://" + receiving.getMyIP() + ":9003"));
            Node tmp;
            for (int i = 0; i < 20 *//*&& !receiving.isCluster()*//*; i++) {
                sender.run();
                receiver.run();
                if (!cluster.contains(tmp) && tmp != null) {
                    cluster.add(tmp);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        createJsonFile();
    }*/
        cluster = new JsonCluster();
        createJsonFile();
        final DatagramSocket socket;
        try {
            socket = new DatagramSocket(PORT);
        } catch (IOException e) {
            anonymousLogger.log(Level.SEVERE, e.getMessage(), e);
            return;
        }

        final Thread senderThread = new Thread(new Sender(socket, BUFFER_SIZE));
        final Thread receiverThread = new Thread(new Receiver(socket, BUFFER_SIZE));

        senderThread.start();
        receiverThread.start();

        Scanner lel = new Scanner(System.in);
        if (lel.toString().toLowerCase().equals("exit")) {
            System.exit(0);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                anonymousLogger.info("Попали в хук.");
                socket.close();

                senderThread.interrupt();
                receiverThread.interrupt();

                try {
                    receiverThread.join(5000);
                    senderThread.join(5000);
                } catch (InterruptedException e) {
                    anonymousLogger.info("Поток завершения был прерван.");
                }
            }
        });
    }

    public synchronized static void createJsonFile() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
//        System.out.println(gson.toJson(cluster));
        FileWriter fileWriter = new FileWriter("cluster.json");
        fileWriter.write(gson.toJson(cluster));
        fileWriter.close();
    }
}
