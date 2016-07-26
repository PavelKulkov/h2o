package detectionservice;

import net.data.technology.jraft.RaftContext;
import net.data.technology.jraft.RaftParameters;
import net.data.technology.jraft.RaftServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Main {

    public static void main(String[] args) throws IOException {

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
