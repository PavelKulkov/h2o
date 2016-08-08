package detectionservice;

public class Constants {

    /**
     * Port for detection service work
     */
    public static final int DETECTION_PORT = 14880;

    /**
     * Port for raft work
     */
    public static final int RAFT_PORT = 9000;

    /**
     * Port for proxy work
     */
    public static final int PROXY_PORT = 14000;

    /**
     * Size of buffer for detection service work
     * Don't touch it!
     */
    public static final int BUFFER_SIZE = 1024;

    /**
     * Path for raft server
     */
    public static final String RAFT_PATH = "./server";

    /**
     * After the timeout has expired, node will be automatically deleted (millis)
     * Time must be synchronized on all nodes!
     */
    public static final int TIMEOUT = 30000;

}
