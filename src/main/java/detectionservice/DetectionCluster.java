package detectionservice;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class DetectionCluster /*extends ClusterConfiguration */ {
    private static transient final int DEFAULT_PORT = 14880;
    private transient final int MY_ID;

    private long logIndex = 0;
    private long lastLogIndex = 0;
    private volatile List<Node> servers;

    public DetectionCluster() throws UnknownHostException {
        this(DEFAULT_PORT);
    }

    public DetectionCluster(int port) throws UnknownHostException {
        super();
        List<Node> list = new ArrayList<>();
        Node node = new MyNode(port, 1);
        MY_ID = node.getId();
        list.add(node);
        this.servers = list;
    }

    public synchronized DetectionCluster addAll(DetectionCluster cluster) {
        this.servers.stream().filter(node -> !cluster.contains(node)).forEach(cluster::add);
        this.servers = cluster.servers;
        return this;
    }

    public synchronized boolean containsAll(DetectionCluster cluster) {
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
        if (node != null)
            if (node.getId() != MY_ID) {
                return servers.remove(node);
            }
        return false;
    }

    public synchronized boolean add(Node node) {
        if (node != null)
            if (node.getId() != MY_ID) {
                return servers.add(node);
            }
        return false;
    }

    public boolean contains(Node node) {
        if (node != null)
            return servers.contains(node);
        return false;
    }

    public List<Node> getNodes() {
        return servers;
    }
}
