package org.hobby.limado.network;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger logger = LogManager.getLogger(DataRead.class);
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

        logger.info(String.format("Storage server started listening at %s:%d for data read.",HOST,PORT));

        while (true) {
            int channels = this.selector.select();
           // if (channels <= 0) return;
            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()){
                    this.registerChannel(this.selector, this.serverSocket);
                    logger.warn("Accepted the connection..");
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
        logger.debug("Fetching the data..");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        Objects.requireNonNull(socketChannel, "Socket channel can not be null.");
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1);
            StringBuilder stringBuilder = new StringBuilder();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                stringBuilder.append(new String(byteBuffer.array()));
                byteBuffer.clear();
            }
            logger.debug("Key: "+stringBuilder.toString());
            String value = StorageOps.getInstance().read(stringBuilder.toString().trim());
            if (value != null){
                ByteBuffer data = ByteBuffer.wrap(value.getBytes(StandardCharsets.UTF_8));
                socketChannel.write(data);
            } else socketChannel.write(ByteBuffer.wrap("404".getBytes(StandardCharsets.UTF_8)));
        } catch (IOException ex){
            logger.error(ExceptionUtils.getStackTrace(ex));
            throw ex;
        } catch (Exception ex){
            logger.error(ExceptionUtils.getStackTrace(ex));
            //throw ex;
            socketChannel.write(ByteBuffer.wrap("500".getBytes(StandardCharsets.UTF_8)));
        } finally {
            socketChannel.close();
        }
    }

    private void registerChannel(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        Objects.requireNonNull(selector, "Selector object should not be null.");
        Objects.requireNonNull(serverSocket, "Server socket object should not be null.");

        SocketChannel client = serverSocket.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }
}
