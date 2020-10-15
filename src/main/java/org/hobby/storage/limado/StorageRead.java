package org.hobby.storage.limado;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class StorageRead {
    private static StorageRead newInstance = new StorageRead();
    private static String INDEX_FILE_NAME = "index.ser";
    private static String DATA_FILE_NAME = "data.ser";
    private String storageFilePath;
    private int pointer;
    private FileChannel reader;


    private StorageRead(){}

    public static StorageRead getInstance() {return newInstance;}

    public void stop()  {
        if (this.storageFilePath == null || this.storageFilePath.isEmpty()) return;
        try {
            if (this.reader != null) this.reader.close();
        } catch (Exception ex){}
    }

    public void init(final String storageFilePath) throws Exception  {
        this.storageFilePath = storageFilePath;
        Path dataFile = Paths.get(this.storageFilePath);
        Set<OpenOption> readerOption = new HashSet<>();
        readerOption.add(StandardOpenOption.READ);
        this.reader = FileChannel.open(dataFile, readerOption);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> StorageRead.getInstance().stop()));
    }

    public String read(int tPointer) throws Exception {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        this.reader.read(lengthBuffer, tPointer);
        lengthBuffer.rewind();
        int size = lengthBuffer.getInt();

        ByteBuffer valueBuffer = ByteBuffer.allocate(size);
        this.reader.read(valueBuffer, tPointer + Integer.BYTES + 1);
        valueBuffer.rewind();
        return new String(valueBuffer.array());
    }

}
