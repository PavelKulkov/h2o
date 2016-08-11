package detectionservice;

import net.data.technology.jraft.RaftClient;

import java.util.concurrent.ExecutionException;

public class TestThread implements Runnable {

    private RaftClient client = ClientSingleton.getInstance();
    private String message = "Zavali";

    public void run() {
        while (true)
            try {
                Thread.sleep(10000);
                boolean accepted = client.appendEntries(new byte[][]{message.getBytes()}).get();
                System.out.println("Accepted: " + String.valueOf(accepted));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
    }
}