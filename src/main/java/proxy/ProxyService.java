package proxy;

import detectionservice.Constants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ProxyService implements Runnable {

    private static final Logger logger = Logger.getAnonymousLogger();

    public void run() {
        Thread thread = Thread.currentThread();
        ExecutorService clientThreads = Executors.newCachedThreadPool();
        logger.info("Proxy service is started");
        try (ServerSocket serverSocket = new ServerSocket(Constants.PROXY_PORT)) {
            logger.info(""+serverSocket.getInetAddress().getHostAddress()+"\n"+serverSocket.getLocalSocketAddress()+"\n"+serverSocket.getLocalPort());
            while (!thread.isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                clientThreads.submit(new ClientThread(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}