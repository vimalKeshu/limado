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

public class DataWrite {
    private static DataWrite ourInstance = new DataWrite();
    private static String HOST = "localhost";
    private static int PORT = 5455;
    private Selector selector;
    private ServerSocketChannel serverSocket;

    public static DataWrite getInstance() {
        return ourInstance;
    }

    private DataWrite() {
    }

    public void start() throws IOException {
        this.selector = Selector.open();
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(HOST, PORT));
        this.serverSocket.configureBlocking(false);
        this.serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> DataWrite.getInstance().stop()));
        while (true) {
            int channels = this.selector.select();
            //if (channels <= 0) return;
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
        System.out.println("Data: "+ stringBuilder.toString());
        String []keyValue = stringBuilder.toString().split(":");
        if (keyValue.length == 2){
            StorageOps.getInstance().write(keyValue[0],keyValue[1]);
        }
        byteBuffer.clear();
        byteBuffer.rewind();
        byteBuffer.get(("200").getBytes(StandardCharsets.UTF_8));
        socketChannel.write(byteBuffer);
        byteBuffer.clear();
    }

    private void registerChannel(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        Objects.requireNonNull(selector, "Selector object should not be null.");
        Objects.requireNonNull(serverSocket, "Server socket object should not be null.");

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
