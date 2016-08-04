package detectionservice;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DetectionCluster {
    private transient final int MY_ID;

    private volatile List<Node> servers;

    public DetectionCluster() throws UnknownHostException {
        this(Constants.DETECTION_PORT);
    }

    public DetectionCluster(int port) throws UnknownHostException {
        super();
        List<Node> list = new CopyOnWriteArrayList<>();
        Node node = new MyNode(port);
        MY_ID = node.getId();
        list.add(node);
        this.servers = list;
    }

    public DetectionCluster addAllNew(DetectionCluster cluster) {
        cluster.servers.stream().filter(node -> !this.contains(node)).forEach(this::add);
        return this;
    }

    public boolean containsAll(DetectionCluster cluster) {
        return this.servers.containsAll(cluster.servers);
    }

    public Node getMe() {
        for (Node node :
                this.servers) {
            if (node.getId() == MY_ID) {
                return node;
            }
        }
        return null;
    }

    public boolean remove(Node node) {
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
