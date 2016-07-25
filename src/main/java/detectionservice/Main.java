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
    public static String LeaderIP;

    public static void main(String[] args) throws IOException {
        /*
        List<Node> nodes = new ArrayList<>();

        boolean flag = true;
        try {
            Receiving receiving = new Receiving(4000);
            Transmission transmission = new Transmission(4000);
            Node tmp;
            for (int i = 0; i < 10; i++) {
                transmission.send(TypeMessage.NODE);
                tmp = receiving.run(TypeMessage.NODE);
                if (tmp.id != 0) {
                    if (nodes.size() != 0) {
                        for (int j = 0; j < nodes.size(); j++) {
                            if (nodes.get(j).id == tmp.id) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            nodes.add(tmp);
                        }
                        flag = true;
                    } else {
                        nodes.add(tmp);
                    }
                }
            }

            JsonCluster jsonCluster = new JsonCluster(nodes);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            System.out.println(gson.toJson(jsonCluster));
            FileWriter fileWriter = new FileWriter("cluster.json");
            fileWriter.write(gson.toJson(jsonCluster));
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

*/
        FileBasedServerStateManager fileBasedServerStateManager = new FileBasedServerStateManager("d:/raft");
        Path baseDir = Paths.get("d:/raft");
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
