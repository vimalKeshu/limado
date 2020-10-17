package org.hobby.limado.network;

import org.hobby.limado.storage.StorageOps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class DataRead {
    private static DataRead newInstance = new DataRead();
    private static String HOST = "localhost";
    private static int PORT = 5454;
    private Selector selector;
    private ServerSocketChannel serverSocket;

    private DataRead(){}

    public static DataRead getInstance() {return newInstance;}

    public void start() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(HOST, PORT));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> DataRead.getInstance().stop()));

        while (true) {
            int channels = this.selector.select();
           // if (channels <= 0) return;
            Set<SelectionKey> selectionKeys = this.selector.keys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()){
                    this.registerChannel(this.selector, this.serverSocket);
                }
                if (key.isReadable()){
                    handleRequest(key);
                }
                iterator.remove();
            }
        }
    }

    public void stop() {
        try {
            if (this.selector != null && this.selector.isOpen()) this.selector.close();
            if (this.selector != null && this.serverSocket.isOpen()) this.serverSocket.close();
        } catch (Exception ex){}
    }

    private void handleRequest(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        StringBuilder stringBuilder = new StringBuilder();
        while (socketChannel.read(byteBuffer) >=0) {
            byteBuffer.flip();
            stringBuilder.append(byteBuffer.array().toString());
            byteBuffer.clear();
            byteBuffer.rewind();
        }
        System.out.println("Key: "+stringBuilder.toString());
        String value = StorageOps.getInstance().read(stringBuilder.toString());
        if (value != null){
            ByteBuffer data = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
            socketChannel.write(data);
        } else socketChannel.write(ByteBuffer.wrap("404".getBytes(StandardCharsets.UTF_8)));
    }

    private void registerChannel(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        Objects.requireNonNull(selector, "Selector object should not be null.");
        Objects.requireNonNull(serverSocket, "Server socket object should not be null.");

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
