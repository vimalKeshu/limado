package org.hobby.storage.limado;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class KeyStorageOps implements IStorageOps<String, String> {
    private static KeyStorageOps newInstance = new KeyStorageOps();
    private static String INDEX_FILE_NAME = "index.ser";
    private static String DATA_FILE_NAME = "data.ser";
    private String storageDir;
    private Map<String, Integer> offsetIndex;
    private int pointer;
    private FileChannel writer;
    private FileChannel reader;


    private KeyStorageOps(){}

    public static KeyStorageOps getInstance() {return newInstance;}

    public void stop()  {
        if (this.storageDir == null || this.storageDir.isEmpty()) return;
        try {
            if (this.writer != null) this.writer.close();
            if (this.reader != null) this.reader.close();
            String indexFilePath = storageDir + File.separator + INDEX_FILE_NAME;
            try(FileOutputStream fileOutputStream = new FileOutputStream(indexFilePath);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
                objectOutputStream.writeObject(this.offsetIndex);
            }catch (Exception ex){
                System.out.println("Not able to serialize the storage index.");
            }
        } catch (Exception ex){}
    }

    public void init(final String storageDir) throws Exception  {
        this.storageDir = storageDir;
        String indexFilePath = storageDir + File.separator + INDEX_FILE_NAME;
        Path indexFile = Paths.get(indexFilePath);

        if (Files.exists(indexFile)){
            try(FileInputStream fileInputStream = new FileInputStream(indexFilePath);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);){
                this.offsetIndex = (HashMap)objectInputStream.readObject();
            } catch (Exception ex){
                throw ex;
            }
        } else {
            this.offsetIndex = new HashMap<>();
        }

        System.out.println("Start position "+this.pointer);
        String dataFilePath = storageDir + File.separator + DATA_FILE_NAME;
        Path dataFile = Paths.get(dataFilePath);
        Set<OpenOption> openOptions = new HashSet<>();
        openOptions.add(StandardOpenOption.CREATE);
        openOptions.add(StandardOpenOption.APPEND);
        this.writer = FileChannel.open(dataFile, openOptions);
        this.pointer = (int) this.writer.size();
        Set<OpenOption> readerOption = new HashSet<>();
        readerOption.add(StandardOpenOption.READ);
        this.reader = FileChannel.open(dataFile, readerOption);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> KeyStorageOps.getInstance().stop()));
    }

    public void write(String key, String value) throws Exception {
        int tPointer = this.pointer;
        byte []val = value.getBytes(StandardCharsets.UTF_8);
        ByteBuffer sizeBuffer = ByteBuffer.allocate(Integer.BYTES).putInt(val.length);
        sizeBuffer.rewind();
        this.writer.write(sizeBuffer, this.pointer);
        this.pointer += Integer.BYTES + 1;

        ByteBuffer byteBuffer = ByteBuffer.wrap(val);
        byteBuffer.rewind();
        this.writer.write(byteBuffer, this.pointer);
        this.pointer += val.length + 1;

        this.offsetIndex.put(key, tPointer);
    }

    public String read(String key) throws Exception {
        if (key == null || key.isEmpty()) return  null;
        if (!this.offsetIndex.containsKey(key)) return  null;

        int tPointer = this.offsetIndex.get(key);
        // System.out.println("tPointer "+tPointer);

        ByteBuffer lengthBuffer = ByteBuffer.allocate(Integer.BYTES);
        this.reader.read(lengthBuffer, tPointer);
        lengthBuffer.rewind();
        int size = lengthBuffer.getInt();
        // System.out.println("size "+size);

        ByteBuffer valueBuffer = ByteBuffer.allocate(size);
        this.reader.read(valueBuffer, tPointer + Integer.BYTES + 1);
        valueBuffer.rewind();
        return new String(valueBuffer.array());
    }

    public static void writeTest() throws Exception {
        getInstance().write("90","v1");
        getInstance().write("20","v2");
        getInstance().write("30","v3");
    }

    public static void readTest() throws Exception {
        getInstance().offsetIndex.keySet().forEach(key -> {
            try {
                System.out.println("key: "+key+", value: "+getInstance().read(key));
            } catch (Exception ex){}
        });
    }

    public static void readWriteTest() throws Exception {
        String key="test1111";
        String value="test111 value";
        getInstance().write(key,value);

        System.out.println("key: "+key+", value: "+getInstance().read(key));
    }

    public static void main(String[] args) throws Exception {
        KeyStorageOps keyStorageOps = KeyStorageOps.getInstance();
        keyStorageOps.init("C:\\Users\\data");

        writeTest();
        readWriteTest();
        readTest();

    }
}
