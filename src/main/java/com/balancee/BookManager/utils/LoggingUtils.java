package com.balancee.BookManager.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

public class LoggingUtils {
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtils.class);

    public static void DebugInfo(String msg) {
        logger.info(new Date() + " " + msg);
    }

    public static void WarningInfo(String msg) {
        logger.warn(new Date() + " " + msg);

    }

    public static void ExceptionInfo(Exception ex) {
        logger.info(Arrays.toString(ex.getStackTrace()).replaceAll(",", "\n"));

    }
}
