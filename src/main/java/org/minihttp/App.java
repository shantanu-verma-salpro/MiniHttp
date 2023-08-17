package org.minihttp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

class EventLoop implements Runnable {
    final AtomicReference<Selector> selectorRef = new AtomicReference<>();

    public EventLoop() throws IOException {
        selectorRef.set(Selector.open());
    }

    @Override
    public void run() {
        Selector selector = selectorRef.get();
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    ((Runnable) selectionKey.attachment()).run();
                    iterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class MiniServer {

    private final EventLoop mainAcceptor;
    private final EventLoop[] workers;
    private final ExecutorService pool;
    private final ServerSocketChannel serverSocketChannel;
    public final HttpParser parser = new HttpParser();
    public final Dispatcher dispatcher = new Dispatcher();


    public MiniServer() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8080));
        int cpus = Runtime.getRuntime().availableProcessors() * 2;
        workers = new EventLoop[cpus - 1];
        for (int i = 0; i < cpus - 1; i++) {
            workers[i] = new EventLoop();
        }
        mainAcceptor = new EventLoop();
        pool = Executors.newFixedThreadPool(cpus);
        serverSocketChannel.register(mainAcceptor.selectorRef.get(), SelectionKey.OP_ACCEPT, new Acceptor());
    }
    void route(String url,HttpMethod method,Handler handle){
        dispatcher.add(method,url,handle);
    }

    public void start() {
        pool.submit(mainAcceptor);
        for (EventLoop ev : workers) {
            pool.submit(ev);
        }
    }

    class Acceptor implements Runnable {
        private final AtomicInteger nextWorkerIndex = new AtomicInteger(0);

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();

                if (socketChannel != null) {
                    int workerIdx = nextWorkerIndex.getAndIncrement() % workers.length;
                    EventLoop worker = workers[workerIdx];
                    new ReaderWriter(worker, socketChannel,dispatcher,parser);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

class ReaderWriter implements Runnable {
    private final SocketChannel socketChannel;
    private final SelectionKey key;
    private final ByteBuffer buffer = ByteBuffer.allocate(1024);
    private final Dispatcher dispatcher;
    private final HttpParser parser;


    ReaderWriter(EventLoop ev, SocketChannel socketChannel,Dispatcher d,HttpParser p) throws IOException, InterruptedException {
        this.dispatcher = d;
        this.parser = p;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);
        ev.selectorRef.get().wakeup();
        key = socketChannel.register(ev.selectorRef.get(), 0);
        key.attach(this);
        key.interestOps(SelectionKey.OP_READ);
        key.selector().wakeup();
    }

    @Override
    public void run() {
        try {
            if (socketChannel.isConnected()) {
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) {
                    closeChannel();
                    return;
                }
                buffer.flip();
                HttpRequest x = parser.parse_(buffer);
                Pair<PathParameters, Handler> y = dispatcher.find(x.getMethod(),x.getUrl().split("\\?")[0]);
                HttpResponse resp;
                if(y == null){
                    System.out.println("null");
                }
                resp = y.getValue().handle(x,y.getKey());
                resp.toByteBuffer(buffer);
                socketChannel.write(buffer);
                socketChannel.close();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            closeChannel();
        }
    }

    void closeChannel() {
        try {
            socketChannel.close();
            key.cancel();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

public class App {
    public static void main(String[] args) throws IOException {
        MiniServer x = new MiniServer();
        x.route("/books",HttpMethod.GET,BookController::handleGetAll);
        x.start();
    }
    public static class BookController {
        public static HttpResponse handleGetAll(HttpRequest req, PathParameters p) {
            // Handle GET request for all books
            String responseString = "Handling GET all books";
            return new HttpResponse.Builder()
                    .statusCode(200)
                    .body(responseString)
                    .build();
        }


    }
}