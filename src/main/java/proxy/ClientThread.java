package proxy;

import detectionservice.ClientSingleton;
import net.data.technology.jraft.RaftClient;
import net.data.technology.jraft.RaftServer;
import org.h2.value.Transfer;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class ClientThread implements Runnable {

    private Socket proxySocket = null;
    private Transfer transfer = null;
    private RaftClient raftClient = null;

    ClientThread(Socket proxySocket) {
        this.proxySocket = proxySocket;
        this.transfer = new Transfer(null);
        this.raftClient = ClientSingleton.getInstance();
    }

    @Override
    public void run() {
        boolean flag = true;
        byte[] bytes = new byte[1024];
        String query = "";
        ProxyTransfer proxyTransfer = new ProxyTransfer(null);
        try (Socket dbSocket = new Socket("localhost", 9092)) {
            while (!Thread.currentThread().isInterrupted()) {
                int count = proxySocket.getInputStream().read(bytes);
                //System.out.println("received:");
                //System.out.println(new String(bytes, 0, count));
                proxyTransfer.init(bytes, 0, count);
                if (flag) {
//                    ProxyService.send(new String(bytes));
                    flag = false;
                } else {
                    query = proxyTransfer.getQuery();
                    if (proxyTransfer.isWriteQuery(query)) {
                        ProxyService.send(query);
                    }
                }
                proxyTransfer.close();

                dbSocket.getOutputStream().write(bytes, 0, count);
                count = dbSocket.getInputStream().read(bytes);

                //System.out.println("from db:");
                //System.out.println(new String(bytes, 0, count));

                proxySocket.getOutputStream().write(bytes, 0, count);
            }
        } catch (
                IOException e
                )

        {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally

        {
            try {
                proxySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
