package org.hobby.storage.limado;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class StorageWrite {
    private static StorageWrite newInstance = new StorageWrite();
    private String storageFilePath;
    private FileChannel writer;

    private StorageWrite(){}

    public static StorageWrite getInstance() {return newInstance;}

    public void stop()  {
        if (this.storageFilePath == null || this.storageFilePath.isEmpty()) return;
        try {
            if (this.writer != null) this.writer.close();
        } catch (Exception ex){}
    }

    public void init(final String storageFilePath) throws Exception  {
        this.storageFilePath = storageFilePath;
        Path dataFile = Paths.get(storageFilePath);
        Set<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.CREATE);
        openOptions.add(StandardOpenOption.APPEND);
        this.writer = FileChannel.open(dataFile, openOptions);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> StorageWrite.getInstance().stop()));
    }

    public int getFileSize() throws IOException {
        return (int) this.writer.size();
    }

    public int write(String key, String value, int pointer) throws Exception {
        byte []val = value.getBytes(StandardCharsets.UTF_8);
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(val.length);
        sizeBuffer.rewind();
        this.writer.write(sizeBuffer, pointer);
        pointer += Integer.BYTES + 1;

        ByteBuffer byteBuffer = ByteBuffer.wrap(val);
        byteBuffer.rewind();
        this.writer.write(byteBuffer, pointer);
        pointer += val.length + 1;

        return pointer;
    }

//    public static void writeTest() throws Exception {
//        getInstance().write("90","v1");
//        getInstance().write("20","v2");
//        getInstance().write("30","v3");
//    }
//
//    public static void readTest() throws Exception {
//        getInstance().offsetIndex.keySet().forEach(key -> {
//            try {
//                System.out.println("key: "+key+", value: "+getInstance().read(key));
//            } catch (Exception ex){}
//        });
//    }
//
//    public static void readWriteTest() throws Exception {
//        String key="test1111";
//        String value="test111 value";
//        getInstance().write(key,value);
//
//        System.out.println("key: "+key+", value: "+getInstance().read(key));
//    }

//    public static void main(String[] args) throws Exception {
//        StorageWrite keyStorageOps = StorageWrite.getInstance();
//        keyStorageOps.init("C:\\Users\\data");
//
//        writeTest();
//        readWriteTest();
//        readTest();
//
//    }
}
