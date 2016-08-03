package detectionservice;

import net.data.technology.jraft.RaftContext;
import net.data.technology.jraft.RaftParameters;
import net.data.technology.jraft.RaftServer;
import raft.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RaftServerSingleton {

    private static RaftServer raftServer = null;
    private static int port = 9000;
    private static String path = "./server";

    private static RaftServer createInstance() {
        FileBasedServerStateManager stateManager = new FileBasedServerStateManager(path);
        Path baseDir = Paths.get(path);
        MessagePrinter messagePrinter = new MessagePrinter(baseDir, port);
        RaftParameters raftParameters = new RaftParameters()
                .withElectionTimeoutUpper(5000)
                .withElectionTimeoutLower(3000)
                .withHeartbeatInterval(1500)
                .withRpcFailureBackoff(500)
                .withMaximumAppendingSize(200)
                .withLogSyncBatchSize(5)
                .withLogSyncStoppingGap(5)
                .withSnapshotEnabled(5000)
                .withSyncSnapshotBlockSize(0);
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2);
        RpcTcpListener rpcTcpListener = new RpcTcpListener(port, executor);
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        RpcTcpClientFactory rpcTcpClientFactory = new RpcTcpClientFactory(executor);
        RaftContext raftContext = new RaftContext(stateManager, messagePrinter, raftParameters, rpcTcpListener,
                loggerFactory, rpcTcpClientFactory);
        raftServer = new RaftServer(raftContext);
        raftContext.getRpcListener().startListening(raftServer);
        return raftServer;
    }

    public static void setParameters(int port, String path) {
        RaftServerSingleton.port = port;
        RaftServerSingleton.path = path;
    }

    public static RaftServer getInstance() {
        if (raftServer == null)
            raftServer =  RaftServerSingleton.createInstance();
        return raftServer;
    }
}
