package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static JsonCluster cluster;

    public static void main(String[] args) throws IOException {
        cluster = new JsonCluster();
        try {
            Receiving receiving = new Receiving(4000);
            Transmission transmission = new Transmission(4000);
            cluster.add(new Node(transmission.getPid(), "tcp://" + receiving.getMyIP() + ":9003"));
            Node tmp;
            for (int i = 0; i < 20 /*&& !receiving.isCluster()*/; i++) {
                transmission.send(TypeMessage.NODE);
                tmp = receiving.run(TypeMessage.NODE);
                if (!cluster.contains(tmp) && tmp != null) {
                    cluster.add(tmp);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        createJsonFile();
    }

    private static void createJsonFile() throws IOException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        System.out.println(gson.toJson(cluster));
        FileWriter fileWriter = new FileWriter("cluster.json");
        fileWriter.write(gson.toJson(cluster));
        fileWriter.close();
    }
}
