package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.data.technology.jraft.*;
import proxy.ProxyService;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectionThread {

    private static final Logger anonymousLogger = Logger.getAnonymousLogger();
    private static RaftServer raftServer;
    private static RaftClient raftClient;
    private static DetectionCluster cluster;

    public static void main(String[] args) throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {
        final DatagramSocket socket;
        try {
            socket = new DatagramSocket(Constants.DETECTION_PORT);
        } catch (IOException e) {
            anonymousLogger.log(Level.SEVERE, e.getMessage(), e);
            return;
        }

        RaftDirectory dir = new RaftDirectory();
        dir.clearDirectory(Constants.RAFT_PATH);
        dir.createClusterFile(Constants.RAFT_PATH);
        dir.createPropFile(Constants.RAFT_PATH);

        raftClient = ClientSingleton.getInstance();
        raftServer = RaftServerSingleton.getInstance();
        cluster = ClusterSingleton.getInstance();

        final Thread senderThread = new Thread(new Sender(socket, Constants.BUFFER_SIZE));
        final Thread receiverThread = new Thread(new Receiver(socket, Constants.BUFFER_SIZE));
        final Thread proxyThread = new Thread(new ProxyService());

        senderThread.start();
        receiverThread.start();
        proxyThread.start();

        Field f = null;
        ServerRole role;
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();

        for (int i = 0; i < 10000; i++) {
            f = raftServer.getClass().getDeclaredField("role");
            f.setAccessible(true);
            role = (ServerRole) f.get(raftServer);
            anonymousLogger.info(role.toString() + "    " + gson.toJson(cluster));
            Thread.sleep(5000);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                anonymousLogger.info("Попали в хук.");
                socket.close();

                senderThread.interrupt();
                receiverThread.interrupt();
                proxyThread.interrupt();

                try {
                    receiverThread.join(5000);
                    senderThread.join(5000);
                    proxyThread.join(5000);
                } catch (InterruptedException e) {
                    anonymousLogger.info("Поток завершения был прерван.");
                }
            }
        });
    }
}
