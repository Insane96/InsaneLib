package insane96mcp.insanelib.util;

import insane96mcp.insanelib.InsaneLib;

public class ILLogger {
    public static void error(String s, Object... args) {
        InsaneLib.LOGGER.error(s, args);
    }

    public static void warn(String s, Object... args) {
        InsaneLib.LOGGER.warn(s, args);
    }

    public static void info(String s, Object... args) {
        InsaneLib.LOGGER.info(s, args);
    }

    public static void debug(String s, Object... args) {
        InsaneLib.LOGGER.debug(s, args);
    }
}
