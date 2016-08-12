package raft;

import net.data.technology.jraft.*;
import raft.AsyncUtility;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MessagePrinter implements StateMachine {

    private Path snapshotStore;
    private long commitIndex;
    private Map<String, String> messages = new ConcurrentHashMap<String, String>();
    private Map<String, String> pendingMessages = new ConcurrentHashMap<String, String>();
    private boolean snapshotInprogress = false;
    private int port;
    //private org.apache.log4j.Logger logger;
    private AsynchronousServerSocketChannel listener;
    private ExecutorService executorService;
    private RaftMessageSender messageSender;
    private Map<String, CompletableFuture<String>> uncommittedRequests = new ConcurrentHashMap<String, CompletableFuture<String>>();

    public MessagePrinter(Path baseDir, int listeningPort){
        this.port = listeningPort;
        //this.logger = LogManager.getLogger(getClass());
        this.snapshotStore = baseDir.resolve("snapshots");
        this.commitIndex = 0;
        if(!Files.isDirectory(this.snapshotStore)){
            try{
                Files.createDirectory(this.snapshotStore);
            }catch(Exception error){
                throw new IllegalArgumentException("bad baseDir");
            }
        }
    }

    public void run(RaftMessageSender messageSender){
        this.messageSender = messageSender;
        int processors = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(processors);
        try{
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
            this.listener = AsynchronousServerSocketChannel.open(channelGroup);
            this.listener.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            this.listener.bind(new InetSocketAddress(this.port));
            this.acceptRequests();
        }catch(IOException exception){
            System.out.println("failed to start the listener due to io error");
        }
    }

    public void stop(){
        if(this.listener != null){
            try {
                this.listener.close();
            } catch (IOException e) {
                System.out.println("failed to close the listener socket");
            }

            this.listener = null;
        }

        if(this.executorService != null){
            this.executorService.shutdown();
            this.executorService = null;
        }
    }

    @Override
    public void commit(long logIndex, byte[] data) {
        Replicator.Replicate(data);
        String message = new String(data, StandardCharsets.UTF_8);
        System.out.printf("commit: %d\t%s\n", logIndex, message);
        this.commitIndex = logIndex;
        this.addMessage(message);
    }

    @Override
    public void saveSnapshotData(Snapshot snapshot, long offset, byte[] data) {
        Path filePath = this.snapshotStore.resolve(String.format("%d-%d.s", snapshot.getLastLogIndex(), snapshot.getLastLogTerm()));
        try{
            if(!Files.exists(filePath)){
                Files.write(this.snapshotStore.resolve(String.format("%d.cnf", snapshot.getLastLogIndex())), snapshot.getLastConfig().toBytes(), StandardOpenOption.CREATE);
            }

            RandomAccessFile snapshotFile = new RandomAccessFile(filePath.toString(), "rw");
            snapshotFile.seek(offset);
            snapshotFile.write(data);
            snapshotFile.close();
        }catch(Exception error){
            throw new RuntimeException(error.getMessage());
        }
    }

    @Override
    public boolean applySnapshot(Snapshot snapshot) {
        Path filePath = this.snapshotStore.resolve(String.format("%d-%d.s", snapshot.getLastLogIndex(), snapshot.getLastLogTerm()));
        if(!Files.exists(filePath)){
            return false;
        }

        try{
            FileInputStream input = new FileInputStream(filePath.toString());
            InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8);
            BufferedReader bufferReader = new BufferedReader(reader);
            synchronized(this.messages){
                this.messages.clear();
                String line = null;
                while((line = bufferReader.readLine()) != null){
                    if(line.length() > 0){
                        System.out.printf("from snapshot: %s\n", line);
                        this.addMessage(line);
                    }
                }

                this.commitIndex = snapshot.getLastLogIndex();
            }

            bufferReader.close();
            reader.close();
            input.close();
        }catch(Exception error){
            System.out.println("failed to apply the snapshot");
            return false;
        }
        return true;
    }

    @Override
    public int readSnapshotData(Snapshot snapshot, long offset, byte[] buffer) {
        Path filePath = this.snapshotStore.resolve(String.format("%d-%d.s", snapshot.getLastLogIndex(), snapshot.getLastLogTerm()));
        if(!Files.exists(filePath)){
            return -1;
        }

        try{
            RandomAccessFile snapshotFile = new RandomAccessFile(filePath.toString(), "rw");
            snapshotFile.seek(offset);
            int bytesRead = read(snapshotFile, buffer);
            snapshotFile.close();
            return bytesRead;
        }catch(Exception error){
            System.out.println("failed read data from snapshot");
            return -1;
        }
    }

    @Override
    public CompletableFuture<Boolean> createSnapshot(Snapshot snapshot) {
        if(snapshot.getLastLogIndex() > this.commitIndex){
            return CompletableFuture.completedFuture(false);
        }

        List<String> copyOfMessages = new LinkedList<String>();
        synchronized(this.messages){
            if(this.snapshotInprogress){
                return CompletableFuture.completedFuture(false);
            }

            this.snapshotInprogress = true;
            copyOfMessages.addAll(this.messages.values());
        }

        return CompletableFuture.supplyAsync(() -> {
            Path filePath = this.snapshotStore.resolve(String.format("%d-%d.s", snapshot.getLastLogIndex(), snapshot.getLastLogTerm()));
            try{
                if(!Files.exists(filePath)){
                    Files.write(this.snapshotStore.resolve(String.format("%d.cnf", snapshot.getLastLogIndex())), snapshot.getLastConfig().toBytes(), StandardOpenOption.CREATE);
                }

                FileOutputStream stream = new FileOutputStream(filePath.toString());
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream, StandardCharsets.UTF_8));
                for(String msg: copyOfMessages){
                    writer.write(msg);
                    writer.write('\n');
                }
                writer.flush();
                writer.close();
                stream.close();
                synchronized(this.messages){
                    this.snapshotInprogress = false;
                }
                return true;
            }catch(Exception error){
                throw new RuntimeException(error.getMessage());
            }
        });
    }

    @Override
    public Snapshot getLastSnapshot() {
        try{
            Stream<Path> files = Files.list(this.snapshotStore);
            Path latestSnapshot = null;
            long maxLastLogIndex = 0;
            long term = 0;
            Pattern pattern = Pattern.compile("(\\d+)\\-(\\d+)\\.s");
            Iterator<Path> itor = files.iterator();
            while(itor.hasNext()){
                Path file = itor.next();
                if(Files.isRegularFile(file)){
                    Matcher matcher = pattern.matcher(file.getFileName().toString());
                    if(matcher.matches()){
                        long lastLogIndex = Long.parseLong(matcher.group(1));
                        if(lastLogIndex > maxLastLogIndex){
                            maxLastLogIndex = lastLogIndex;
                            term = Long.parseLong(matcher.group(2));
                            latestSnapshot = file;
                        }
                    }
                }
            }

            files.close();
            if(latestSnapshot != null){
                byte[] configData = Files.readAllBytes(this.snapshotStore.resolve(String.format("%d.cnf", maxLastLogIndex)));
                ClusterConfiguration config = ClusterConfiguration.fromBytes(configData);
                return new Snapshot(maxLastLogIndex, term, config, latestSnapshot.toFile().length());
            }
        }catch(Exception error){
            System.out.println("failed read snapshot info snapshot store");
        }

        return null;
    }

    @Override
    public void rollback(long logIndex, byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        int index = message.indexOf(':');
        if(index > 0){
            String key = message.substring(0, index);
            this.pendingMessages.remove(key);
            CompletableFuture<String> future = this.uncommittedRequests.get(key);
            if(future != null){
                future.complete("Rolled back.");
            }
        }

        System.out.println(String.format("Rollback index %d", logIndex));
    }

    @Override
    public void preCommit(long logIndex, byte[] data) {
        String message = new String(data, StandardCharsets.UTF_8);
        System.out.println(String.format("PreCommit:%s at %d", message, logIndex));
        int index = message.indexOf(':');
        if(index > 0){
            this.pendingMessages.put(message.substring(0, index), message);
        }
    }

    private static int read(RandomAccessFile stream, byte[] buffer){
        try{
            int offset = 0;
            int bytesRead = 0;
            while(offset < buffer.length && (bytesRead = stream.read(buffer, offset, buffer.length - offset)) != -1){
                offset += bytesRead;
            }

            return offset;
        }catch(IOException exception){
            return -1;
        }
    }

    private void acceptRequests(){
        try{
            this.listener.accept(null, AsyncUtility.handlerFrom(
                    (AsynchronousSocketChannel connection, Object ctx) -> {
                        readRequest(connection);
                        acceptRequests();
                    },
                    (Throwable error, Object ctx) -> {
                        System.out.println("accepting a new connection failed, will still keep accepting more requests");
                        acceptRequests();
                    }));
        }catch(Exception exception){
            System.out.println("failed to accept new requests, will retry");
            this.acceptRequests();
        }
    }

    private void readRequest(AsynchronousSocketChannel connection){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        try{
            AsyncUtility.readFromChannel(connection, buffer, null, handlerFrom((Integer bytesRead, Object ctx) -> {
                if(bytesRead.intValue() < 4){
                    System.out.println("failed to read the request header from client socket");
                    closeSocket(connection);
                }else{
                    try{
                        System.out.println("request header read, try to read the message");
                        int bodySize = 0;
                        for(int i = 0; i < 4; ++i){
                            int value = buffer.get(i);
                            bodySize = bodySize | (value << (i * 8));
                        }

                        if(bodySize > 1024){
                            sendResponse(connection, "Bad Request");
                            return;
                        }

                        ByteBuffer bodyBuffer = ByteBuffer.allocate(bodySize);
                        readBody(connection, bodyBuffer);
                    }catch(Throwable runtimeError){
                        // if there are any conversion errors, we need to close the client socket to prevent more errors
                        closeSocket(connection);
                        System.out.println("message reading/parsing error");
                    }
                }
            }, connection));
        }catch(Exception readError){
            System.out.println("failed to read more request from client socket");
            closeSocket(connection);
        }
    }

    private void readBody(AsynchronousSocketChannel connection, ByteBuffer bodyBuffer){
        try{
            AsyncUtility.readFromChannel(connection, bodyBuffer, null, handlerFrom((Integer bytesRead, Object ctx) -> {
                if(bytesRead.intValue() < bodyBuffer.limit()){
                    System.out.println("failed to read the request body from client socket");
                    closeSocket(connection);
                }else{
                    String message = new String(bodyBuffer.array(), StandardCharsets.UTF_8);
                    CompletableFuture<String> future = new CompletableFuture<String>();
                    future.whenCompleteAsync((String ack, Throwable err) -> {
                        if(err != null){
                            sendResponse(connection, err.getMessage());
                        }else{
                            sendResponse(connection, ack);
                        }
                    });
                    processMessage(message, future);
                }
            }, connection));
        }catch(Exception readError){
            System.out.println("failed to read more request from client socket");
            closeSocket(connection);
        }
    }

    private void processMessage(String message, CompletableFuture<String> future){
        System.out.println("Got message " + message);
        int index = message.indexOf(':');
        if(index <= 0){
            if("status".equalsIgnoreCase(message)){
                System.out.printf("Uncommitted Requests: %d\n", this.uncommittedRequests.size());
                System.out.printf("Pending Messages: %d\n", this.pendingMessages.size());
                System.out.printf("Committed Messages: %d\n", this.messages.size());
                future.complete("Done.");
            }else{
                future.complete("Bad message, No key");
            }

            return;
        }

        if(message.startsWith("addsrv:")){
            StringTokenizer tokenizer = new StringTokenizer(message.substring(index + 1), ",");
            ArrayList<String> values = new ArrayList<String>();
            while(tokenizer.hasMoreTokens()){
                values.add(tokenizer.nextToken());
            }

            if(values.size() == 2){
                ClusterServer server = new ClusterServer();
                server.setEndpoint(values.get(1));
                server.setId(Integer.parseInt(values.get(0)));
                messageSender.addServer(server).whenCompleteAsync((Boolean result, Throwable err) -> {
                    if(err != null){
                        future.complete("System faulted, please retry");
                    }else if(!result){
                        future.complete("System rejected");
                    }else{
                        future.complete("Accpeted, server is being added");
                    }
                });
            }else{
                future.complete("Bad request");
            }

            return;
        }else if(message.startsWith("rmsrv:")){
            try{
                int id = Integer.parseInt(message.substring(index + 1));
                messageSender.removeServer(id).whenCompleteAsync((Boolean result, Throwable err) -> {
                    if(err != null){
                        future.complete("System faulted, please retry");
                    }else if(!result){
                        future.complete("System rejected");
                    }else{
                        future.complete("Accpeted, server is being removed");
                    }
                });
            }catch(Throwable err){
                future.complete("Bad request");
            }

            return;
        }

        String key = message.substring(0, index);
        if(this.messages.containsKey(key)){
            future.complete("Already printed.");
            this.uncommittedRequests.remove(key);
            return;
        }

        if(this.pendingMessages.containsKey(key)){
            //BUG should be able to do chained completion with existing future related to that key
            this.uncommittedRequests.put(key, future);
            return;
        }

        uncommittedRequests.put(key, future);
        messageSender.appendEntries(new byte[][] { message.getBytes(StandardCharsets.UTF_8) }).whenCompleteAsync((Boolean result, Throwable err) -> {
            if(err != null){
                future.complete("System faulted, please retry");
                this.uncommittedRequests.remove(key);
            }else if(!result){
                future.complete("System is not ready");
                this.uncommittedRequests.remove(key);
            }
        });
    }

    private void addMessage(String message){
        int index = message.indexOf(':');
        if(index <= 0){
            return;
        }

        String key = message.substring(0, index);
        this.messages.put(key, message);
        this.pendingMessages.remove(key);
        CompletableFuture<String> req = this.uncommittedRequests.get(key);
        if(req != null){
            req.complete("Printed.");
            this.uncommittedRequests.remove(key);
        }
    }

    private void sendResponse(AsynchronousSocketChannel connection, String message){
        byte[] resp = message.getBytes(StandardCharsets.UTF_8);
        int respSize = resp.length;
        ByteBuffer respBuffer = ByteBuffer.allocate(respSize + 4);
        for(int i = 0; i < 4; ++i){
            int value = (respSize >> (i * 8));
            respBuffer.put((byte)(value & 0xFF));
        }

        respBuffer.put(resp);
        respBuffer.flip();
        try{
            AsyncUtility.writeToChannel(connection, respBuffer, null, handlerFrom((Integer bytesWrite, Object ctx) -> {
                if(bytesWrite < respBuffer.limit()){
                    System.out.println("failed to write all data back to response channel");
                    closeSocket(connection);
                }else{
                    readRequest(connection);
                }
            }, connection));
        }catch(Exception writeError){
            System.out.println("failed to write response to client socket");
            closeSocket(connection);
        }
    }

    private <V, A> CompletionHandler<V, A> handlerFrom(BiConsumer<V, A> completed, AsynchronousSocketChannel connection) {
        return AsyncUtility.handlerFrom(completed, (Throwable error, A attachment) -> {
            System.out.println("socket server failure");
            if(connection != null){
                closeSocket(connection);
            }
        });
    }

    private void closeSocket(AsynchronousSocketChannel connection){
        try{
            connection.close();
        }catch(IOException ex){
            System.out.println("failed to close client socket");
        }
    }
}
