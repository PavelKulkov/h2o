package detectionservice;

import java.io.IOException;

public class Main {
    public static String LeaderIP;

    public static void main(String[] args) {
        try {
            Receiving receiving = new Receiving(4000);
            Transmission transmission = new Transmission(4000);
            for (int i = 0; i < 10 && !receiving.isCluster(); i++) {
                transmission.send(TypeMessage.NODE);
                LeaderIP = receiving.run(TypeMessage.LEADER);
            }
            if (LeaderIP.equals("null"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
