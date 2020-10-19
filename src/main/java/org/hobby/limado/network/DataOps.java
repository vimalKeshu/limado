package org.hobby.limado.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DataOps {
    private static final Logger logger = LogManager.getLogger(DataOps.class);
    private static DataOps newInstance = new DataOps();

    private DataOps() {
    }

    public static DataOps getInstance() {return newInstance;}

    public void start() {
        new Thread(() -> {
            try {
                DataRead.getInstance().start();
            }catch (Exception ex){
                ex.printStackTrace();
                logger.error(ex.getMessage());
                DataRead.getInstance().stop();
                System.exit(-1);
            }
        },"DataRead").start();

        new Thread(() -> {
            try {
                DataWrite.getInstance().start();
            }catch (Exception ex){
                logger.error(ex.getLocalizedMessage());
                DataWrite.getInstance().stop();
                System.exit(-1);
            }
        },"DataWrite").start();

    }
}
