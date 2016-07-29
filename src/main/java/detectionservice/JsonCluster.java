package detectionservice;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class JsonCluster {
    private int logIndex = 0;
    private int lastLogIndex = 0;
    private static transient final int DEFAULT_PORT = 14880;
    private transient final int MY_ID;
    private volatile List<Node> servers;

    public JsonCluster() throws UnknownHostException {
        this(DEFAULT_PORT);
    }

    public JsonCluster(int port) throws UnknownHostException {
        List<Node> list = new ArrayList<>();
        Node node = new MyNode(port);
        MY_ID = node.getId();
        list.add(node);
        this.servers = list;
    }

    public synchronized JsonCluster addAll(JsonCluster cluster) {
        this.servers.stream().filter(node -> !cluster.contains(node)).forEach(cluster::add);
        this.servers = cluster.servers;
        return this;
    }

    public synchronized boolean containsAll(JsonCluster cluster) {
        return this.servers.containsAll(cluster.servers);
    }

    public synchronized Node getMe() {
        for (Node node :
                this.servers) {
            if (node.getId() == MY_ID) {
                return node;
            }
        }
        return null;
    }

    public synchronized boolean remove(Node node) {
        if (node.getId() != MY_ID) {
            return servers.remove(node);
        }
        return false;
    }

    public synchronized void add(Node node) {
        servers.add(node);
    }

    public boolean contains(Node node) {
        return servers.contains(node);
    }

    public List<Node> getServers() {
        return servers;
    }
}
