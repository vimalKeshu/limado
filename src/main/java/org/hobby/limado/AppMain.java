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

        //writeTest();
        readTest();

    }

    private static void readTest() throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5454));
        client.write(ByteBuffer.wrap("1".getBytes(StandardCharsets.UTF_8)));
        System.out.println("Key: 1"+", Value: "+handleResponse(client));
    }

    private static void writeTest() throws IOException {
        SocketChannel client = SocketChannel.open(new InetSocketAddress("localhost", 5455));
        client.write(ByteBuffer.wrap("10:xyz abc".getBytes(StandardCharsets.UTF_8)));
        System.out.println("Key: 1"+", Value: "+handleResponse(client));
    }

    private static String handleResponse(SocketChannel socketChannel) throws IOException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        StringBuilder stringBuilder = new StringBuilder();
        while (socketChannel.read(byteBuffer) >=0) {
            byteBuffer.flip();
            stringBuilder.append(byteBuffer.array().toString());
            byteBuffer.clear();
            byteBuffer.rewind();
        }
        return stringBuilder.toString();
    }
}
