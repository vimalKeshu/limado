package org.hobby.limado.storage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class StorageOps {
    private static StorageOps newInstance = new StorageOps();
    private static final String INDEX_FILE_NAME = "index.ser";
    private static final String DATA_FILE_NAME = "data.ser";
    private String indexFilePath;
    private String dataFilePath;

    public static StorageOps getInstance() {
        return newInstance;
    }

    private StorageOps() {
    }

    public void init(String storageDir) throws Exception {
        Objects.requireNonNull(storageDir, "Storage directory should not be null.");
        this.indexFilePath = storageDir + File.separator + INDEX_FILE_NAME;
        StorageIndex.getInstance().init(this.indexFilePath);
        this.dataFilePath = storageDir + File.separator + DATA_FILE_NAME;
        StorageWrite.getInstance().init(this.dataFilePath);
        StorageRead.getInstance().init(this.dataFilePath);
    }

    public void write(String key, String value) throws IOException {
        int pointer = StorageWrite.getInstance().getFileSize();
        int tPointer = StorageWrite.getInstance().write(key, value, pointer);
        StorageIndex.getInstance().storeIndex(key, tPointer);
    }

    public String read(String key) throws IOException {
        Integer pointer = StorageIndex.getInstance().getIndex(key);
        if (pointer == null) return  null;
        else  return StorageRead.getInstance().read(pointer);
    }
}
