package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender implements Runnable {

    private static final Logger logger = Logger.getLogger(Sender.class.getName());
    private String message;
    private static byte[] broadcast;
    private final int bufferSize;

    private final GsonBuilder gsonBuilder = new GsonBuilder();
    private final Gson gson = gsonBuilder.create();

    private final DatagramSocket socket;

    Sender(DatagramSocket socket, int bufferSize) {
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.broadcast = getHostIP();
    }

    public void run() {
        Thread thread = Thread.currentThread();

        try {
            byte[] buffer;
            logger.info(gson.toJson(DetectionThread.cluster));
            while (!thread.isInterrupted()) {
                try {
                    removeOldServers();
                } catch (ExecutionException e) {
                    logger.info(e.getMessage());
                }
                message = gson.toJson(DetectionThread.cluster);
                buffer = (message + "\u0000").getBytes(Charset.forName("UTF-8"));
                int offset = 0;
                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length,
                        InetAddress.getByAddress(broadcast),
                        DetectionThread.DETECTION_PORT);
                while (offset < buffer.length) {
                    packet.setData(Arrays.copyOfRange(buffer, offset, offset + bufferSize));
                    socket.send(packet);
                    offset += bufferSize;
                }
//                logger.info("Send message: "+message);
                Thread.sleep(500);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.info("Thread is interrupted.");
        }
        logger.info("Thread sender was terminated.");
    }


    private byte[] getHostIP() {
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            ip[3] = (byte) 255;
            ip = InetAddress.getByAddress(ip).getAddress();
            return ip;
        } catch (UnknownHostException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
        return null;
    }

    private int getID() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf("@"));
        // TODO: 20.07.2016 Придумать адекватный идентификатор
        return Integer.parseInt(pid);
    }

    private void removeOldServers() throws ExecutionException, InterruptedException {
        synchronized (DetectionThread.cluster) {
            for (Node node :
                    DetectionThread.cluster.getNodes()) {
                if (new Date().getTime() - node.getTime() > 30000) {
                    if (DetectionThread.client.removeServer(node.getId()).get()) {
                        DetectionThread.cluster.remove(node);
                        logger.info("Node " + node.getEndpoint() + " is removed!");
                    }
                }
            }
        }
    }


}
