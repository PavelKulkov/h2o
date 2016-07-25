package detectionservice;


import net.data.technology.jraft.ClusterServer;

public class Node /*extends ClusterServer*/ {
    private int id;
    private String endpoint;

    public Node() {
        this.id = -1;
        this.endpoint = "null";
    }

    public Node(int id, String endpoint) {
        this.id = id;
        this.endpoint = endpoint;
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

}
