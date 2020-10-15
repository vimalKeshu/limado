package org.hobby.storage.limado;

public class AppMain {
    public static void main(String[] args) throws Exception{
        StorageOps.getInstance().init("C:\\Users\\vc8342\\data");
        System.out.println(StorageOps.getInstance().read("1"));
    }
}
