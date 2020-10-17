package org.hobby.limado.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StorageIndex {
    private static StorageIndex newInstance = new StorageIndex();
    private String indexFilePath;
    private Map<String, Integer> offsetIndex;

    private StorageIndex(){}
    public static StorageIndex getInstance() {return newInstance;}

    public void stop() {
        if (this.indexFilePath == null || this.indexFilePath.isEmpty()) return;
        try {
            try(FileOutputStream fileOutputStream = new FileOutputStream(indexFilePath);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
                objectOutputStream.writeObject(this.offsetIndex);
            }catch (Exception ex){
                System.out.println("Not able to serialize the storage index.");
            }
        } catch (Exception ex){}
    }

    public void init(final String indexFilePath) throws Exception {
        this.indexFilePath = indexFilePath;
        Path indexFile = Paths.get(indexFilePath);
        if (Files.exists(indexFile)){
            try(FileInputStream fileInputStream = new FileInputStream(indexFilePath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);){
                this.offsetIndex = (HashMap)objectInputStream.readObject();
            } catch (IOException ex){
                throw ex;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            this.offsetIndex = new HashMap<>();
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> StorageIndex.getInstance().stop()));
    }

    public Integer getIndex(String key) {
        return this.offsetIndex.get(key);
    }

    public void storeIndex(String key, Integer index) {
        Objects.requireNonNull(key, "Key should not be null.");
        Objects.requireNonNull(index, "Index should not be null.");
        this.offsetIndex.put(key, index);
    }
}
