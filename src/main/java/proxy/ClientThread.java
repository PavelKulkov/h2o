package proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Pavel Kulkov  on 01.08.2016.
 */
public class ClientThread implements Runnable {

    private Socket proxySocket = null;

    ClientThread(Socket proxySocket) {
        this.proxySocket = proxySocket;
    }

    @Override
    public void run() {
        byte[] bytes = new byte[1024];
        try (Socket dbSocket = new Socket("localhost", 9092)) {
            while (!Thread.currentThread().isInterrupted()) {
                int count = proxySocket.getInputStream().read(bytes);

                System.out.println("received:");
                System.out.println(new String(bytes, 0, count));

                dbSocket.getOutputStream().write(bytes, 0, count);
                count = dbSocket.getInputStream().read(bytes);

                System.out.println("from db:");
                System.out.println(Arrays.toString(Arrays.copyOf(bytes, count)));

                proxySocket.getOutputStream().write(bytes, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                proxySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
