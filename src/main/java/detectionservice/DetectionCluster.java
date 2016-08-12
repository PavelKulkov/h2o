package detectionservice;


import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DetectionCluster {
    private transient final int MY_ID;

    public final int proxyPort = Constants.PROXY_PORT;
    private volatile List<Node> servers;

    public DetectionCluster() throws UnknownHostException {
        this(Constants.RAFT_PORT);
    }

    public DetectionCluster(int port) throws UnknownHostException {
        super();
        List<Node> list = new CopyOnWriteArrayList<>();
        Node node = new MyNode(port, 2);
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

    public MyNode getMe() {
        return (MyNode)this.servers.stream().filter((Node node) ->  node.getId() == MY_ID).findFirst().get();
    }

    public boolean remove(Node node) {
        if (node != null)
            if (node.getId() != MY_ID) {
                return servers.remove(node);
            }
        return false;
    }

    public boolean add(Node node) {
        if (node != null)
            if (node.getId() != MY_ID) {
                return servers.add(node);
            }
        return false;
    }

    public void updateTime(Node node) throws UnknownHostException {
        if (node.getId() == MY_ID) {
            MyNode me = getMe();
            me.setCurrentTime();
            servers.set(servers.indexOf(node),me);
        } else {
            int i = servers.indexOf(node);
            servers.set(i, node);
        }
    }

    public boolean contains(Node node) {
        if (node != null)
            return servers.contains(node);
        return false;
    }

    public void setRmv(Node node) {
        this.servers.stream().filter(node::equals).forEach((n) -> n.setRmv(true));
    }

    public List<Node> getNodes() {
        return servers;
    }
}
