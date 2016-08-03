package detectionservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.UnknownHostException;

public class ClusterSingleton {

    private static DetectionCluster cluster = null;
    private static int port = 14880;

    private static DetectionCluster createInstance() throws IOException {
        cluster = new DetectionCluster(port);
        return cluster;
    }

    public static DetectionCluster getInstance() throws IOException {
        if (cluster == null)
            cluster = ClusterSingleton.createInstance();
        return cluster;
    }

    public static void setPort(int port) {
        ClusterSingleton.port = port;
    }

}
