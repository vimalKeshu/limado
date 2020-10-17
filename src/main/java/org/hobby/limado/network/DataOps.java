package org.hobby.limado.network;

import java.io.IOException;

public class DataOps {
    private static DataOps newInstance = new DataOps();

    private DataOps() {
    }

    public static DataOps getInstance() {return newInstance;}

    public void start() throws IOException {
        DataRead.getInstance().start();
        DataWrite.getInstance().start();
    }
}
