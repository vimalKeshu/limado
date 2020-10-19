package org.hobby.limado.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;

public class StorageRead {
    private static final Logger logger = LogManager.getLogger(StorageRead.class);
    private static StorageRead newInstance = new StorageRead();
    private String storageFilePath;
    private FileChannel reader;


    private StorageRead(){}

    public static StorageRead getInstance() {return newInstance;}

    public void stop()  {
        if (this.storageFilePath == null || this.storageFilePath.isEmpty()) return;
        try {
            if (this.reader != null) this.reader.close();
        } catch (Exception ex){}
    }

    public void init(final String storageFilePath) throws IOException {
        this.storageFilePath = storageFilePath;
        Path dataFile = Paths.get(this.storageFilePath);
        Set<OpenOption> readerOption = new HashSet<>();
        readerOption.add(StandardOpenOption.READ);
        this.reader = FileChannel.open(dataFile, readerOption);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> StorageRead.getInstance().stop()));
    }

    public String read(int tPointer) throws IOException {
        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        this.reader.read(lengthBuffer, tPointer);
        lengthBuffer.rewind();
        int size = lengthBuffer.getInt();


        ByteBuffer valueBuffer = ByteBuffer.allocate(size);
        this.reader.read(valueBuffer, tPointer + Integer.BYTES + 1);
        valueBuffer.flip();
        return new String(valueBuffer.array()).trim();
    }

}
