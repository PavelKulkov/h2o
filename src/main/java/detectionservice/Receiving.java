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

    public Node run(TypeMessage typeMessage) {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.setSoTimeout(timeout);
            socket.receive(packet);
            String received = new String(packet.getData(), packet.getOffset(), packet.getLength());
            //System.out.println(received);
            if (received.isEmpty()) {
                return new Node(0,"0");
            }
            String[] args = received.split("!");

            if (args[0].equals(TypeMessage.NODE.name()) && args[0].equals(typeMessage.name())) {
                received = received.substring(5, received.length() - 1);
                return new Node(Integer.parseInt(args[1]),args[2]);
            }
            if (args[0].equals(TypeMessage.LEADER.name()) && args[0].equals(typeMessage.name())) {
                received = received.substring(7, received.length() - 1);
                cluster = true;
                return new Node(Integer.parseInt(args[1]),args[2]);
            }

        } catch (SocketTimeoutException ste) {
            System.out.println(ste.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {

        }
        return new Node(0,"0");
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