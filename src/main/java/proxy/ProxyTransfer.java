package proxy;

import org.h2.api.ErrorCode;
import org.h2.command.Command;
import org.h2.engine.Constants;
import org.h2.engine.SessionInterface;
import org.h2.engine.SessionRemote;
import org.h2.expression.ParameterInterface;
import org.h2.expression.ParameterRemote;
import org.h2.message.DbException;
import org.h2.result.ResultColumn;
import org.h2.result.ResultInterface;
import org.h2.server.TcpServerThread;
import org.h2.store.LobStorageInterface;
import org.h2.util.IOUtils;
import org.h2.util.StringUtils;
import org.h2.value.Transfer;
import org.h2.value.Value;
import org.h2.value.ValueLobDb;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Pavel Kulkov  on 04.08.2016.
 */
public class ProxyTransfer extends Transfer {

    private ByteArrayInputStream byteArrayInputStream = null;
    private DataInputStream dataInputStream = null;



    public ProxyTransfer(SessionInterface sessionInterface) {
        super(sessionInterface);
    }


    public void init(byte[] bytes,int offset,int len) throws IOException {
        if (bytes.length != 0) {
            this.byteArrayInputStream = new ByteArrayInputStream(bytes,offset,len);
            dataInputStream = new DataInputStream(byteArrayInputStream);
        }
    }

    public String getConn() throws IOException {
        this.dataInputStream.readInt();
        this.dataInputStream.readInt();
        this.readString();
        return this.readString();
    }

    public String getQuery() throws IOException {
        int operation = dataInputStream.readInt();
        String sql = null;
        switch (operation) {
            case SessionRemote.SESSION_PREPARE_READ_PARAMS:
            case SessionRemote.SESSION_PREPARE: {
                int id = dataInputStream.readInt();
                 sql = this.readString();
            }
        }
        return sql;
    }

    @Override
    public String readString() throws IOException {
        int count = dataInputStream.readInt();
        //System.out.println(dataInputStream.available() + " " + count);
        if (count == -1 || dataInputStream.available() < count) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder(count);
            for (int i = 0; i < count; i++) {
                builder.append(this.dataInputStream.readChar());
            }

            String result = builder.toString();
            result = StringUtils.cache(result);
            return result;
        }
    }

    @Override
    public synchronized void close() {
        try {
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
