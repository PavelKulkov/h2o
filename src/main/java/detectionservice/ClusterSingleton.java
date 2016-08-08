package detectionservice;

import java.io.IOException;

public class ClusterSingleton {

    private static DetectionCluster cluster = null;

    private static DetectionCluster createInstance() throws IOException {
        cluster = new DetectionCluster(Constants.RAFT_PORT);
        return cluster;
    }

    public static DetectionCluster getInstance() throws IOException {
        if (cluster == null)
            cluster = ClusterSingleton.createInstance();
        return cluster;
    }

}
