package detectionservice;

import java.util.ArrayList;
import java.util.List;

public class JsonCluster {
    private int logIndex = 0;
    private int lastLogIndex = 0;
    private List<Node> servers;

    public JsonCluster() {
        servers = new ArrayList<>();
    }

    public JsonCluster(List<Node> servers) {
        this.servers = servers;
    }

    public void remove(Node node) {
        servers.remove(node);
    }

    public void add(Node node) {
        servers.add(node);
    }

    public boolean contains(Node node) {
        return servers.contains(node);
    }
}
