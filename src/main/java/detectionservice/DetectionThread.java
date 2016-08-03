package detectionservice;

import net.data.technology.jraft.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DetectionThread {

    private static final Logger anonymousLogger = Logger.getAnonymousLogger();
    private static RaftServer raftServer;
    private static DetectionCluster cluster;
    static final int DETECTION_PORT = 9002;
    static final int RAFT_PORT = 14880;
    static final int BUFFER_SIZE = 1024;
    static final String RAFT_PATH = "./server";

    public static void main(String[] args) throws IOException, InterruptedException, IllegalAccessException, NoSuchFieldException {

        final DatagramSocket socket;
        try {
            socket = new DatagramSocket(DETECTION_PORT);
        } catch (IOException e) {
            anonymousLogger.log(Level.SEVERE, e.getMessage(), e);
            return;
        }

        RaftDirectory dir = new RaftDirectory();
        dir.clearDirectory(RAFT_PATH);
        dir.createClusterFile(RAFT_PATH);
        dir.createPropFile(RAFT_PATH);

        RaftServerSingleton.setParameters(RAFT_PORT, RAFT_PATH);
        ClientSingleton.setPath(RAFT_PATH);
        raftServer = RaftServerSingleton.getInstance();
        cluster = ClusterSingleton.getInstance();

        final Thread senderThread = new Thread(new Sender(socket, BUFFER_SIZE));
        final Thread receiverThread = new Thread(new Receiver(socket, BUFFER_SIZE));

        senderThread.start();
        receiverThread.start();

        Field f = null;
        ServerRole role;
        for (int i = 0; i < 10000; i++) {
            f = raftServer.getClass().getDeclaredField("role");
            f.setAccessible(true);
            role = (ServerRole) f.get(raftServer);
            anonymousLogger.info(role.toString());
            Thread.sleep(5000);
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
}
