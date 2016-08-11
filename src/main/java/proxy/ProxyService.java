package proxy;

import detectionservice.Constants;
import net.data.technology.jraft.ClusterConfiguration;
import net.data.technology.jraft.RaftClient;
import raft.FileBasedServerStateManager;
import raft.Log4jLoggerFactory;
import raft.RpcTcpClientFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ProxyService implements Runnable {

    public void run() {
        Thread clientThread = Thread.currentThread();
        ExecutorService clientThreads = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(Constants.PROXY_PORT)) {
            while (!clientThread.isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                //clientThreads.submit(new ClientThread(clientSocket));
                Thread thread = new Thread(new ClientThread(clientSocket));
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void send(String msg) throws Exception {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);

        FileBasedServerStateManager stateManager = new FileBasedServerStateManager(Constants.RAFT_PATH);
        ClusterConfiguration config = stateManager.loadClusterConfiguration();

        RaftClient client = new RaftClient(new RpcTcpClientFactory(executor), config, new Log4jLoggerFactory());
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean accepted = client.appendEntries(new byte[][]{msg.getBytes()}).get();
        System.out.println("Accepted: " + String.valueOf(accepted));
    }
}