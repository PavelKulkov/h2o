package detectionservice;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class RaftDirectory {

    private DetectionCluster cluster = ClusterSingleton.getInstance();

    public RaftDirectory() throws IOException {

    }

    public void clearDirectory(String path) {
        File fPath = new File(path);
        if (fPath.exists()) {
            if (fPath.isDirectory()) {
                for (File f : fPath.listFiles()) {
                    if (f.isDirectory()) clearDirectory(f.getPath());
                    else f.delete();
                }
            }
            fPath.delete();
        }
        fPath.mkdirs();
    }

    public void createClusterFile(String path) throws IOException {
        FileWriter fileWriter = new FileWriter(path + "//cluster.json");
        String beginRaft = "{\"logIndex\":0,\"lastLogIndex\":0,\"servers\":[" +
                "{\"id\":" + cluster.getMe().getId() +
                ",\"endpoint\":\"" + cluster.getMe().getEndpoint() + "\"}" +
                "]}\n";
        fileWriter.write(beginRaft);
        fileWriter.close();
    }

    public void createPropFile(String path) throws IOException {
        FileWriter fileWriter = new FileWriter(path + "//config.properties");
        fileWriter.write("server.id=" + cluster.getMe().getId());
        fileWriter.close();
    }
}
