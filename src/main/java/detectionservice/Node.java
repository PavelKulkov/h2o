package detectionservice;


public class Node {
    private int id;
    private String endpoint;

    public Node(int id, String endpoint) {
        this.id = id;
        this.endpoint = endpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
