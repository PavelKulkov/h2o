package proxy;

import detectionservice.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyService implements Runnable {

    public void run() {
        Thread clientThread = Thread.currentThread();
        ExecutorService clientThreads = Executors.newCachedThreadPool();
        try (ServerSocket serverSocket = new ServerSocket(Constants.PROXY_PORT)) {
            while (!clientThread.isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                //clientThreads.submit(new ClientThread(clientSocket));
                Thread thread = new Thread(new ClientThread(clientSocket));
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}