package detectionservice;


import net.data.technology.jraft.ClusterServer;

import java.util.Date;

public class Node {
    private /*transient*/ int id;
    private String endpoint;
    private long time;

    public Node() {
        this.id = -1;
        this.endpoint = "null";
        this.time = new Date().getTime();
    }

    public Node(int id, String endpoint) {
        this.id = id;
        this.endpoint = endpoint;
        this.time = new Date().getTime();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;

        Node node = (Node) o;

        if (id == node.id && endpoint.equals(node.endpoint)) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + endpoint.hashCode();
        return result;
    }

    public ClusterServer toClusterServer() {
        ClusterServer server = new ClusterServer();
        server.setId(id);
        server.setEndpoint(endpoint);
        return  server;
    }

}
