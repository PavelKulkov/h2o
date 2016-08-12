package raft;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Pavel Kulkov on 11.08.2016.
 */
public class Replicator {

    public static void Replicate (byte[] query){
        try(Socket dbSocket = new Socket("localhost",9092)){
            dbSocket.getOutputStream().write(query);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
