package detectionservice;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.*;

public class Transmission {

    public static final int DEFAULT_PORT = 4445;
    private int port;
    private String addressGroup;

    public Transmission() {
        this(DEFAULT_PORT);
    }

    public Transmission(int port) {
        this.port = port;
        try {
            byte[] ip = InetAddress.getLocalHost().getAddress();
            ip[3] = (byte) 255;
            addressGroup = InetAddress.getByAddress(ip).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    public void send(TypeMessage payload) throws UnknownHostException {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0,pid.indexOf("@"));
        try {
            FileWriter fileWriter = new FileWriter("config.properties");
            fileWriter.write("server.id="+pid);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (DatagramSocket socket = new DatagramSocket()) {
            byte[] buf;
            String msg = payload + "!"+ pid + "!tcp://" + InetAddress.getLocalHost().getHostAddress() + "&"; // // TODO: 12.07.2016 заменить на параметр
            buf = msg.getBytes();
            InetAddress group = InetAddress.getByName(addressGroup);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, port);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}