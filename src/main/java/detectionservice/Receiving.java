package detectionservice;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;


public class Receiving {
    public static final int DEFAULT_PORT = 4445;
    public static final int DEFAULT_TIMEOUT = 200;
    private int port;
    private int timeout;
    private String myIP;
    private boolean cluster;


    public Receiving() throws IOException {
        this(DEFAULT_PORT, DEFAULT_TIMEOUT);
    }

    public Receiving(int port) throws IOException {
        this(port, DEFAULT_TIMEOUT);
    }

    public Receiving(int port, int timeout) throws IOException {
        this.port = port;
        this.timeout = timeout;
        myIP = InetAddress.getLocalHost().getHostAddress();
        cluster = false;
    }

    public String run(TypeMessage typeMessage) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(timeout);
            socket.receive(packet);
            String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
            System.out.println(received);
            if (received.isEmpty()) {
                return "null";
            }
            String typeReceive = received.substring(0, received.indexOf('/'));

            if (typeReceive.equals(TypeMessage.NODE) && typeReceive.equals(typeMessage)) {
                received = received.substring(5, received.length() - 1);
                return received;
            }
            if (typeReceive.equals(TypeMessage.LEADER) && typeReceive.equals(typeMessage)) {
                received = received.substring(7, received.length() - 1);
                cluster = true;
                return received;
            }

        } catch (SocketTimeoutException ste) {
            System.out.println(ste.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            return "null";
        }
    }

    public int getPORT() {
        return port;
    }

    public String getMyIP() {
        return myIP;
    }

    public int getTimeout() {
        return timeout;
    }

    public boolean isCluster() {
        return cluster;
    }
}