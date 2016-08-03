package proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Pavel Kulkov  on 28.07.2016.
 */
public class ProxyService {

    private static final int PORT = 14000;

    public static void main(String[] args) {
        ExecutorService clientThreads = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientThread clientThread = new ClientThread(clientSocket);
                Thread thread = new Thread(clientThread);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}