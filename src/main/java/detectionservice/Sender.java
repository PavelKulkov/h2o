package detectionservice;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender implements Runnable {

    private static final Logger logger = Logger.getLogger(Sender.class.getName());
    private String message;
    public static final int DEFAULT_PORT = 9001;
//    public static final int
    private static byte[] broadcast;
//    private static int port;
    private final int bufferSize;

    private final DatagramSocket socket;

    Sender(DatagramSocket socket, int bufferSize) {
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.broadcast = getIP();

        message = "Лол Кек Омг Азаза Лел Сёс Форчан Двач Дно Покемоны Деградация Овощь Пакет Яровой";
    }

    public void run() {
        Thread thread = Thread.currentThread();
        try {
            byte[] buffer = message.getBytes(Charset.forName("UTF-8"));
            while (!thread.isInterrupted()) {
                int offset = 0;
                DatagramPacket packet = new DatagramPacket(
                        buffer,
                        buffer.length,
                        InetAddress.getByAddress(broadcast),
                        4445);
                while (offset < buffer.length) {
                    packet.setData(Arrays.copyOfRange(buffer, offset, offset + bufferSize));
                    socket.send(packet);
                    offset += bufferSize;
                }

                Thread.sleep(500);
            }
        } catch (IOException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.info("Thread is interrupted.");
        }
        logger.info("Thread sender was terminated.");
    }


    private byte[] getIP() {
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            ip[3] = (byte) 255;
            return ip;
        } catch (UnknownHostException e) {
            logger.log(Level.INFO, e.getMessage(), e);
        }
        return null;
    }

    private int getID() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf("@"));
        // TODO: 20.07.2016 Придумать адекватный идентификатор
        return Integer.parseInt(pid);
    }

}
