package detectionservice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static String LeaderIP;

    public static void main(String[] args) throws IOException {
        List <Node> nodes = new ArrayList<>();
        boolean flag = true;
        try {
            Receiving receiving = new Receiving(4000);
            Transmission transmission = new Transmission(4000);
            Node tmp;
            for (int i = 0; i < 10 && !receiving.isCluster(); i++) {
                transmission.send(TypeMessage.LEADER);
                tmp = receiving.run(TypeMessage.NODE);
                if(tmp.id != 0 ){
                    if(nodes.size() !=0) {
                        for (int j = 0; j < nodes.size(); j++) {
                            if(nodes.get(j).id == tmp.id){
                                flag = false;
                                break;
                            }
                        }
                        if(flag){
                            nodes.add(tmp);
                        }
                        flag=true;
                    }else{
                        nodes.add(tmp);
                        System.out.println(2);
                    }
                }
            }
            System.out.println(nodes.size());
            /*
            if (LeaderIP.equals("null")){

            }
*/
            JsonCluster jsonCluster = new JsonCluster(nodes);
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            System.out.println(gson.toJson(jsonCluster));
            FileWriter fileWriter = new FileWriter("cluster.json");
            fileWriter.write(gson.toJson(jsonCluster));
            fileWriter.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

//        ServerSocket serverSocket = new ServerSocket(9000);
//        Socket socket = serverSocket.accept();
//        byte[] buffer = new byte[255];
//        socket.getInputStream().read(buffer);
//        System.out.println(buffer);
    }
}
