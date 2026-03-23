package org.min.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.*;

public class Loggers {

    private static final ConcurrentHashMap<String, ILogger> cache = new ConcurrentHashMap<>();

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %2$s%n  → %5$s%6$s%n");
    }

    public static ILogger get(Class<?> clazz) {
        return cache.computeIfAbsent(clazz.getName(), k -> new JulLogger(clazz));
    }

    public static void shutdown() {
        LogManager.getLogManager().reset();
    }

    // ──────────────────────────────────────────────────────────────
    private static class JulLogger implements ILogger {

        private final Logger logger;

        JulLogger(Class<?> clazz) {
            this.logger = Logger.getLogger(clazz.getName());
        }

        private String fmt(String msg, Object... args) {
            if (args == null || args.length == 0) return msg;
            try { return String.format(msg, args); } catch (Exception e) { return msg; }
        }

        @Override public void trace(String msg, Object... args) { logger.finest(fmt(msg, args)); }
        @Override public void debug(String msg, Object... args) { logger.fine(fmt(msg, args)); }
        @Override public void info (String msg, Object... args) { logger.info(fmt(msg, args)); }
        @Override public void warn (String msg, Object... args) { logger.warning(fmt(msg, args)); }
        @Override public void error(String msg, Object... args) { logger.severe(fmt(msg, args)); }
        @Override public void error(String msg, Throwable t)    { logger.log(Level.SEVERE, msg, t); }
    }
}
