package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.data.technology.jraft.RaftContext;
import net.data.technology.jraft.RaftParameters;
import net.data.technology.jraft.RaftServer;

import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class DetectionThread {

    private static final Logger anonymousLogger = Logger.getAnonymousLogger();
    static final int PORT = 9001;
    static final int BUFFER_SIZE = 2048;
    public static volatile JsonCluster cluster;

    public static void main(String[] args) throws IOException, InterruptedException {

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

        createPropFile();

        Thread.sleep(5000);
        startRaft();

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
        FileWriter fileWriter = new FileWriter("./raft//cluster.json");
        fileWriter.write(gson.toJson(cluster));
        fileWriter.close();
    }

    public synchronized static void createPropFile() throws IOException {
        FileWriter fileWriter = new FileWriter("./raft//config.properties");
        fileWriter.write("server.id=" + cluster.getMe().getId());
        fileWriter.close();
    }

    public static void startRaft() {
        FileBasedServerStateManager fileBasedServerStateManager = new FileBasedServerStateManager("./raft");
        Path baseDir = Paths.get("./raft");
        MessagePrinter messagePrinter = new MessagePrinter(baseDir,9001);
        RaftParameters raftParameters = new RaftParameters();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
        RpcTcpListener rpcTcpListener = new RpcTcpListener(9001,executor);
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        RpcTcpClientFactory rpcTcpClientFactory = new RpcTcpClientFactory(executor);
        RaftContext raftContext = new RaftContext(fileBasedServerStateManager,messagePrinter,raftParameters,rpcTcpListener,
                loggerFactory,rpcTcpClientFactory);
        RaftServer raftServer = new RaftServer(raftContext);
    }
}
