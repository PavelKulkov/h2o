package detectionservice;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.function.BiConsumer;

/**
 * Created by Pavel Kulkov  on 18.07.2016.
 */
public class AsyncUtility {
    public static <V, A> CompletionHandler<V, A> handlerFrom(BiConsumer<V, A> completed, BiConsumer<Throwable, A> failed) {
        return new CompletionHandler<V, A>() {
            @Override
            public void completed(V result, A attachment) {
                completed.accept(result, attachment);
            }

            @Override
            public void failed(Throwable exc, A attachment) {
                failed.accept(exc, attachment);
            }
        };
    }

    public static <A> void readFromChannel(AsynchronousByteChannel channel, ByteBuffer buffer, A attachment, CompletionHandler<Integer, A> completionHandler){
        try{
            channel.read(
                    buffer,
                    new AsyncContext<A>(attachment, completionHandler),
                    handlerFrom(
                            (Integer result, AsyncContext<A> a) -> {
                                int bytesRead = result.intValue();
                                if(bytesRead == -1 || !buffer.hasRemaining()){
                                    a.completionHandler.completed(buffer.position(), a.attachment);
                                }else{
                                    readFromChannel(channel, buffer, a.attachment, a.completionHandler);
                                }
                            },
                            (Throwable error, AsyncContext<A> a) -> {
                                a.completionHandler.failed(error, a.attachment);
                            }));
        }catch(Throwable exception){
            completionHandler.failed(exception, attachment);
        }
    }

    public static <A> void writeToChannel(AsynchronousByteChannel channel, ByteBuffer buffer, A attachment, CompletionHandler<Integer, A> completionHandler){
        try{
            channel.write(
                    buffer,
                    new AsyncContext<A>(attachment, completionHandler),
                    handlerFrom(
                            (Integer result, AsyncContext<A> a) -> {
                                int bytesRead = result.intValue();
                                if(bytesRead == -1 || !buffer.hasRemaining()){
                                    a.completionHandler.completed(buffer.position(), a.attachment);
                                }else{
                                    writeToChannel(channel, buffer, a.attachment, a.completionHandler);
                                }
                            },
                            (Throwable error, AsyncContext<A> a) -> {
                                a.completionHandler.failed(error, a.attachment);
                            }));
        }catch(Throwable exception){
            completionHandler.failed(exception, attachment);
        }
    }

    static class AsyncContext<A>{
        private A attachment;
        private CompletionHandler<Integer, A> completionHandler;

        public AsyncContext(A attachment, CompletionHandler<Integer, A> handler){
            this.attachment = attachment;
            this.completionHandler = handler;
        }
    }
}
