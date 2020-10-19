package org.hobby.limado;

import org.hobby.limado.network.DataOps;
import org.hobby.limado.storage.StorageOps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class AppMain {
    public static void main(String[] args) throws Exception{
        StorageOps.getInstance().init("C:\\Users\\vc8342\\data");
        DataOps.getInstance().start();
        String key = "10";
        String value = "xyz abc llll";

        writeTest(key, value);
        readTest(key);




    }

    private static void readTest(String key) throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5454));
        client.write(ByteBuffer.wrap(key.getBytes(StandardCharsets.UTF_8)));
        System.out.println(String.format("Key: %s, Value: %s", key, handleResponse(client)));
    }

    private static void writeTest(String key, String value) throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5455));
        client.write(ByteBuffer.wrap((String.format("%s:%s",key,value)).getBytes(StandardCharsets.UTF_8)));
        System.out.println(String.format("Key:%s",key)+", Value: "+handleResponse(client));
    }

    private static String handleResponse(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        StringBuilder stringBuilder = new StringBuilder();
        while (socketChannel.read(byteBuffer) >=0) {
            byteBuffer.flip();
            stringBuilder.append(new String(byteBuffer.array()));
            byteBuffer.clear();
        }
        return stringBuilder.toString();
    }
}
