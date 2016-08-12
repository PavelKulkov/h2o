package detectionservice;

import net.data.technology.jraft.*;
import raft.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class RaftServerSingleton {

    private static RaftServer raftServer = null;

    private static RaftServer createInstance() throws URISyntaxException {
        FileBasedServerStateManager stateManager = new FileBasedServerStateManager(Constants.RAFT_PATH);
        Path baseDir = Paths.get(Constants.RAFT_PATH);
        MessagePrinter messagePrinter = new MessagePrinter(baseDir, Constants.RAFT_PORT);
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
        RpcTcpListener rpcTcpListener = new RpcTcpListener(Constants.RAFT_PORT, executor);
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        RpcTcpClientFactory rpcTcpClientFactory = new RpcTcpClientFactory(executor);

        RaftContext raftContext = new RaftContext(
                stateManager,
                messagePrinter,
                raftParameters,
                rpcTcpListener,
                loggerFactory,
                rpcTcpClientFactory,
                executor);

        raftServer = new RaftServer(raftContext);
        raftContext.getRpcListener().startListening(raftServer);
        MessagePrinter mp = new MessagePrinter(baseDir, Constants.RAFT_PORT);
        mp.run(raftServer.createMessageSender());

//        mp.run(RaftConsensus.run(raftContext));
        return raftServer;
    }

    public static RaftServer getInstance() throws URISyntaxException {
        if (raftServer == null)
            raftServer = RaftServerSingleton.createInstance();
        return raftServer;
    }
}
