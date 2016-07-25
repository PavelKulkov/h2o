package detectionservice;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver implements Runnable {

    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    private final byte[] buffer;
    private final DatagramSocket socket;

    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.create();

    Receiver(DatagramSocket socket, int bufferSize) {
        this.socket = socket;
        buffer = new byte[bufferSize];
    }

    public void run() {
        Thread thread = Thread.currentThread();
        String receive;
        JsonCluster tempCluster;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            while (!thread.isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byteOut.write(buffer, packet.getOffset(), packet.getLength());
                if (packet.getLength() > 0 && buffer[packet.getLength() - 1] == 0x00) {
                    byte[] bytes = byteOut.toByteArray();
                    receive = new String(Arrays.copyOf(bytes, bytes.length - 1));
//                    receive = receive.get
                    logger.info("Receive message: " + receive);
                    byteOut = new ByteArrayOutputStream();

                    JsonReader reader = new JsonReader(new StringReader(receive));
                    reader.setLenient(true);
                    tempCluster = gson.fromJson(reader, JsonCluster.class);
                    if(!Main.cluster.containsAll(tempCluster)){
                        Main.cluster.addAll(tempCluster);
                        Main.createJsonFile();
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
        logger.info("Thread receiver is interrupted.");
    }
}
