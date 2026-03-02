package logging;

import logging.formatters.Formatter;
import logging.handlers.CustomConsoleHandler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.*;

public final class Loggers {
    private static final Map<String, ILogger> CACHE = new ConcurrentHashMap<>();
    private static final AtomicReference<LogConfig> CONFIG = new AtomicReference<>(new LogConfig());
    private static final Object RELOAD_LOCK = new Object();

    private static volatile PrintStream filePrintStream;
    private static final Object FILE_STREAM_LOCK = new Object();

    private Loggers() {}

    public static ILogger get(Class<?> cls) {
        return get(cls.getSimpleName());
    }

    public static ILogger get(String name) {
        return CACHE.computeIfAbsent(name, k -> new LoggerImpl(k, CONFIG.get()));
    }

    public static void reload() {
        synchronized (RELOAD_LOCK) {
            closeFileStream();

            LogConfig newConfig = new LogConfig();
            CONFIG.set(newConfig);

            Map<String, ILogger> newCache = new ConcurrentHashMap<>();
            for (String name : CACHE.keySet()) {
                newCache.put(name, new LoggerImpl(name, newConfig));
            }

            for (ILogger logger : CACHE.values()) {
                try {
                    logger.close();
                } catch (Exception e) {
                    // ignore
                }
            }

            CACHE.clear();
            CACHE.putAll(newCache);

            if (newConfig.hasValidationErrors()) {
                System.err.println("Logging configuration warnings:");
                System.err.println(newConfig.getValidationErrors());
            }
        }
    }

    public static void shutdown() {
        synchronized (RELOAD_LOCK) {
            for (ILogger logger : CACHE.values()) {
                try {
                    logger.close();
                } catch (Exception e) {
                    // ignore
                }
            }
            CACHE.clear();
            closeFileStream();
        }
    }

    private static void closeFileStream() {
        if (filePrintStream != null) {
            try {
                filePrintStream.close();
            } catch (Exception e) {
                // ignore
            } finally {
                filePrintStream = null;
            }
        }
    }

    private static PrintStream getFilePrintStream(LogConfig config) {
        if (filePrintStream == null) {
            synchronized (FILE_STREAM_LOCK) {
                if (filePrintStream == null && config.isFileEnabled()) {
                    try {
                        Path logDir = Paths.get(config.getLogDir());
                        Files.createDirectories(logDir);

                        Path logFile = logDir.resolve("app.log");

                        FileOutputStream fos = new FileOutputStream(logFile.toFile(), config.isFileAppend());

                        filePrintStream = new PrintStream(fos, true, "UTF-8");

                    } catch (IOException e) {
                        System.err.println("Cannot create file stream: " + e.getMessage());
                    }
                }
            }
        }
        return filePrintStream;
    }

    private static final class LoggerImpl implements ILogger {
        private final String name;
        private final java.util.logging.Logger logger;
        private final LogConfig config;
        private volatile boolean isClosed = false;

        LoggerImpl(String name, LogConfig config) {
            this.name = name;
            this.config = config;
            this.logger = createJavaLogger(name, config);
        }

        private java.util.logging.Logger createJavaLogger(String name, LogConfig config) {
            String loggerName = "app." + name;
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);

            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
                handler.close();
            }

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            if (config.isFileEnabled()) {
                FileHandler fileHandler = new FileHandler(name, config);
                fileHandler.setLevel(config.getFileLevel());
                logger.addHandler(fileHandler);
            }

            if (config.isConsoleEnabled()) {
                CustomConsoleHandler ch = new CustomConsoleHandler(config);
                ch.setFormatter(new Formatter(
                        config.isConsoleTimestamp(),
                        config.isShowThread(),
                        name
                ));
                ch.setLevel(config.getConsoleLevel());
                logger.addHandler(ch);
            }

            return logger;
        }

        private class FileHandler extends Handler {
            private final String loggerName;

            public FileHandler(String loggerName, LogConfig config) {
                this.loggerName = loggerName;
                setFormatter(new Formatter(
                        config.isConsoleTimestamp(),
                        false,
                        loggerName
                ));
            }

            @Override
            public void publish(LogRecord record) {
                if (isClosed || !isLoggable(record)) {
                    return;
                }

                PrintStream ps = getFilePrintStream(config);
                if (ps != null) {
                    String msg = getFormatter().format(record);
                    synchronized (FILE_STREAM_LOCK) {
                        ps.print(msg);
                        ps.flush();
                    }
                }
            }

            @Override
            public void flush() {
                PrintStream ps = filePrintStream;
                if (ps != null) {
                    synchronized (FILE_STREAM_LOCK) {
                        ps.flush();
                    }
                }
            }

            @Override
            public void close() throws SecurityException {
            }
        }

        @Override
        public void error(String message) {
            if (!isClosed) logger.severe(message);
        }

        @Override
        public void error(String message, Throwable t) {
            if (!isClosed) logger.log(Level.SEVERE, message, t);
        }

        @Override
        public void warn(String message) {
            if (!isClosed) logger.warning(message);
        }

        @Override
        public void info(String message) {
            if (!isClosed) logger.info(message);
        }

        @Override
        public void debug(String message) {
            if (!isClosed) logger.fine(message);
        }

        @Override
        public void debug(String format, Object... args) {
            if (!isClosed) logger.fine(String.format(format, args));
        }

        @Override
        public void trace(String message) {
            if (!isClosed) logger.finer(message);
        }

        @Override
        public boolean isDebugEnabled() {
            return !isClosed && logger.isLoggable(Level.FINE);
        }

        @Override
        public boolean isTraceEnabled() {
            return !isClosed && logger.isLoggable(Level.FINER);
        }

        @Override
        public void close() {
            if (!isClosed) {
                isClosed = true;
                for (Handler handler : logger.getHandlers()) {
                    try {
                        handler.close();
                    } catch (Exception ignored) {}
                    logger.removeHandler(handler);
                }
            }
        }
    }
}