package raft;

        import com.sun.xml.internal.bind.api.impl.NameConverter;

        import java.io.IOException;
        import java.net.Socket;
        import java.net.UnknownHostException;
        import java.nio.charset.StandardCharsets;
        import java.sql.*;

public class Replicator {

    private String url = "jdbc:h2:~/test";
    private String name = "sa";
    private String pass = "";


    public void executeQuery(String query) throws SQLException {
        Connection connection = null;
        try{
            connection = DriverManager.getConnection(url,name,pass);
            Statement statement = connection.createStatement();
            statement.execute(query);
//            statement.execute("INSERT INtO TEST (id, NAME) values (220, 'fg');");
//            ResultSet resulSet = statement.execute(query);
//            System.out.println("РЕЗУЛЬТАТ   "+resulSet);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }
}