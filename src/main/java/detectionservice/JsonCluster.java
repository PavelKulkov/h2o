package detectionservice;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class JsonCluster {
    private int logIndex = 0;
    private int lastLogIndex = 0;
    private volatile List<Node> servers;

    public JsonCluster() throws UnknownHostException {
        this(new ArrayList<>());
    }

    public JsonCluster(List<Node> servers) throws UnknownHostException {
        this.servers = servers;
        this.add(new MyNode());
    }

    public synchronized void remove(Node node) {
        servers.remove(node);
    }

    public synchronized void add(Node node) {
        servers.add(node);
    }

    public boolean contains(Node node) {
        return servers.contains(node);
    }

    public synchronized JsonCluster addAll(JsonCluster cluster) {
        cluster.servers.stream().filter(node -> !this.contains(node)).forEach(this::add);
        return this;
    }

    public synchronized boolean containsAll(JsonCluster cluster) {
        return this.servers.containsAll(cluster.servers);
    }
}
