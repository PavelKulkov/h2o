package raft;

import net.data.technology.jraft.RpcClient;
import net.data.technology.jraft.RpcClientFactory;
import raft.RpcTcpClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;

/**
 * Created by Pavel Kulkov  on 18.07.2016.
 */
public class RpcTcpClientFactory implements RpcClientFactory {
    private ExecutorService executorService;

    public RpcTcpClientFactory(ExecutorService executorService){
        this.executorService = executorService;
    }

    @Override
    public RpcClient createRpcClient(String endpoint) {
        try {
            URI uri = new URI(endpoint);
            return new RpcTcpClient(new InetSocketAddress(uri.getHost(), uri.getPort()), this.executorService);
        } catch (URISyntaxException e) {
            org.apache.log4j.LogManager.getLogger(getClass()).error(String.format("%s is not a valid uri", endpoint));
            throw new IllegalArgumentException("invalid uri for endpoint");
        }
    }

}
