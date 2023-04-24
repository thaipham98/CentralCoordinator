package com.example.centralcoordinator.Resource;

import java.util.logging.Logger;

public class MyLogger {

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$ta %1$tb %1$td, %1$tY %1$tl:%1$tM:%1$tS.%1$tL %1$Tp %2$s%n%4$s: %5$s%n");
    }

    //reference: https://www.logicbig.com/tutorials/core-java-tutorial/logging/customizing-default-format.html

    private static final Logger logger = Logger.getLogger(MyLogger.class.getName());
    private static MyLogger myLogger = null;
    private MyLogger() {}

    public static MyLogger getInstance() {
        if (myLogger == null)
            myLogger =  new MyLogger();

        return myLogger;
    }

    public static void info(String message){
        logger.info(message);
    }

    public static void warning(String error){
        logger.warning(error);
    }
}
