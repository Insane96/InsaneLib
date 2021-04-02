package insane96mcp.insanelib.utils;

import insane96mcp.insanelib.InsaneLib;
import org.apache.logging.log4j.Logger;

public class LogHelper {
    public static void error(String s, Object... args) {
        InsaneLib.LOGGER.error(String.format(s, args));
    }

    public static void warn(String s, Object... args) {
        InsaneLib.LOGGER.warn(String.format(s, args));
    }

    public static void info(String s, Object... args) {
        InsaneLib.LOGGER.info(String.format(s, args));
    }

    public static void error(Logger logger, String s, Object... args) {
        logger.error(String.format(s, args));
    }

    public static void warn(Logger logger, String s, Object... args) {
        logger.warn(String.format(s, args));
    }

    public static void info(Logger logger, String s, Object... args) {
        logger.info(String.format(s, args));
    }
}
