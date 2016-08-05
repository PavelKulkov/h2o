package proxy;

import org.h2.value.Transfer;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Pavel Kulkov  on 01.08.2016.
 */
public class ClientThread implements Runnable {

    private Socket proxySocket = null;
    private Transfer transfer = null;

    ClientThread(Socket proxySocket) {
        this.proxySocket = proxySocket;
        this.transfer = new Transfer(null);
    }

    @Override
    public void run() {
        boolean flag = true;
        byte[] bytes = new byte[1024];
        ProxyTransfer proxyTransfer = new ProxyTransfer(null);
        try (Socket dbSocket = new Socket("localhost", 9092)) {
            while (!Thread.currentThread().isInterrupted()) {
                int count = proxySocket.getInputStream().read(bytes);
                System.out.println("received:");
                System.out.println(new String(bytes, 0, count));

                proxyTransfer.init(bytes, 0, count);
                if (flag) {
                    System.out.println(proxyTransfer.getConn());
                    flag = false;
                } else {
                    System.out.println(proxyTransfer.getQuery());
                }
                proxyTransfer.close();

                dbSocket.getOutputStream().write(bytes, 0, count);
                count = dbSocket.getInputStream().read(bytes);

                System.out.println("from db:");
                System.out.println(new String(bytes, 0, count));

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
