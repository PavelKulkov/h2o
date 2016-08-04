package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import net.data.technology.jraft.ClusterServer;
import net.data.technology.jraft.RaftClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Receiver implements Runnable {

    private static final Logger logger = Logger.getLogger(Receiver.class.getName());
    private final byte[] buffer;
    private final DatagramSocket socket;
    private final DetectionCluster cluster = ClusterSingleton.getInstance();
    private final RaftClient client = ClientSingleton.getInstance();

    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.create();

    Receiver(DatagramSocket socket, int bufferSize) throws IOException {
        this.socket = socket;
        buffer = new byte[bufferSize];
    }

    public void run() {
        Thread thread = Thread.currentThread();
        String receive;
        DetectionCluster tempCluster;
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            while (!thread.isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                byteOut.write(buffer, packet.getOffset(), packet.getLength());
                if (packet.getLength() > 0 && buffer[packet.getLength() - 1] == 0x00) {
                    byte[] bytes = byteOut.toByteArray();
                    receive = new String(Arrays.copyOf(bytes, bytes.length - 1));

//                    logger.info("Receive message: " + receive);
                    byteOut = new ByteArrayOutputStream();

                    JsonReader reader = new JsonReader(new StringReader(receive));
                    reader.setLenient(true);
                    tempCluster = gson.fromJson(reader, DetectionCluster.class);
                    addNewServers(tempCluster);

                }
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        logger.info("Thread receiver is interrupted.");
    }

    private void addNewServers(DetectionCluster tempCluster) throws ExecutionException, InterruptedException {
        Iterator<Node> iterator = tempCluster.getNodes().iterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            if (cluster.contains(node)) {
                cluster.remove(node);
                cluster.add(node);
            } else if ((new Date().getTime() - node.getTime()) < 5000) {
                if (client.addServer(node.toClusterServer()).get()) {
                    cluster.add(node);
                    logger.info("New node " + node.getEndpoint() + " is added!");
                    continue;
                }
            }
        }
    }
}
