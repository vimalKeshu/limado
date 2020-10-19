package org.hobby.limado.network;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hobby.limado.storage.StorageIndex;
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
    private static final Logger logger = LogManager.getLogger(DataWrite.class);
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
        //Runtime.getRuntime().addShutdownHook(new Thread(() -> DataWrite.getInstance().stop()));

        logger.info(String.format("Storage server started listening at %s:%d for data write.",HOST,PORT));

        while (true) {
            int channels = this.selector.select();
            //if (channels <= 0) return;
            Iterator<SelectionKey> iterator = this.selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()){
                    this.registerChannel(this.selector, this.serverSocket);
                    logger.warn("Accepted the connection..");
                }
                if (key.isReadable()){
                    handleRequest(key);
                    logger.warn("Writing the data..");
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
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
            StringBuilder stringBuilder = new StringBuilder();
            while (socketChannel.read(byteBuffer) > 0) {
                byteBuffer.flip();
                stringBuilder.append(new String(byteBuffer.array()));
                byteBuffer.clear();
            }
            String payload = stringBuilder.toString().trim();
            logger.debug("Data: "+ payload);
            String []keyValue = payload.split(":");
            if (keyValue.length == 2){
                StorageOps.getInstance().write(keyValue[0],keyValue[1]);
                socketChannel.write(ByteBuffer.wrap(("200").getBytes(StandardCharsets.UTF_8)));
                logger.debug("Data inserted..");
            } else {
                socketChannel.write(ByteBuffer.wrap(("400").getBytes(StandardCharsets.UTF_8)));
                logger.debug("Data not inserted..");
            }

        } catch (IOException ex){
            logger.error(ExceptionUtils.getStackTrace(ex));
            throw ex;
        } catch (Exception ex) {
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
