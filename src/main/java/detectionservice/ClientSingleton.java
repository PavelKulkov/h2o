package detectionservice;

import net.data.technology.jraft.ClusterConfiguration;
import net.data.technology.jraft.RaftClient;
import raft.FileBasedServerStateManager;
import raft.Log4jLoggerFactory;
import raft.RpcTcpClientFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ClientSingleton {

    private static RaftClient client = null;
    private static String path = "./server";

    private static RaftClient createInstance() {
        RaftClient client;
        FileBasedServerStateManager stateManager = new FileBasedServerStateManager(path);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
        RpcTcpClientFactory rpcTcpClientFactory = new RpcTcpClientFactory(executor);
        ClusterConfiguration config = stateManager.loadClusterConfiguration();
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();

        client = new RaftClient(rpcTcpClientFactory, config, loggerFactory);
        return client;
    }

    public static void setPath(String path) {
        ClientSingleton.path = path;
    }

    public static RaftClient getInstance(){
        if (client == null)
            client =  ClientSingleton.createInstance();
        return client;
    }
}
