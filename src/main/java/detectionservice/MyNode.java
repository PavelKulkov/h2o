package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Level;

public class MyNode extends Node {
    public static int DEFAULT_PORT = 42042;

    MyNode() throws UnknownHostException {
        this(DEFAULT_PORT);
    }

    MyNode(int port) throws UnknownHostException {
        super();
        setId(getMyID());
        setEndpoint("tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        setTime(new Date().getTime());
    }

    MyNode(int port, int ID) throws UnknownHostException {
        super();
        setId(ID);
        setEndpoint("tcp://" + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        setTime(new Date().getTime());
    }

    private int getMyID() {
        String pid = ManagementFactory.getRuntimeMXBean().getName();
        pid = pid.substring(0, pid.indexOf("@"));
        return Integer.parseInt(pid); // TODO: 20.07.2016 Придумать адекватный идентификатор
    }
}
