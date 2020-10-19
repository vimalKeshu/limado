package org.hobby.limado.storage;

public class StorageOpsTest {
    public static void main(String[] args) throws Exception{
        StorageOps.getInstance().init("C:\\Users\\vc8342\\data");
        StorageOps.getInstance().write("9999","evan..");
        //StorageIndex.getInstance().storeIndex("10", 598);
        System.out.println("Value "+StorageOps.getInstance().read("9999"));
    }
}
