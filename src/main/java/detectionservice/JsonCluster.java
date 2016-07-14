package detectionservice;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pavel Kulkov  on 13.07.2016.
 */
public class JsonCluster {
    private int logIndex;
    private int lastLogIndex;
    private List<Node> servers = new ArrayList<>();

    public JsonCluster(List<Node> servers){
        logIndex = 0;
        lastLogIndex = 0;
        this.servers = servers;
    }

}
