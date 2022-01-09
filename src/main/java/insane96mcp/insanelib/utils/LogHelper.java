package insane96mcp.insanelib.utils;

import insane96mcp.insanelib.InsaneLib;

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
}
