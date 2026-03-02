package logging;

import logging.formatters.Formatter;
import logging.handlers.CustomConsoleHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class Loggers {
    private static final Map<String, ILogger> CACHE = new ConcurrentHashMap<>();
    private static final AtomicReference<LogConfig> CONFIG = new AtomicReference<>(new LogConfig());
    private static final Object RELOAD_LOCK = new Object();

    private Loggers() {}

    public static ILogger get(Class<?> cls) {
        return get(cls.getSimpleName());
    }

    public static ILogger get(String name) {
        return CACHE.computeIfAbsent(name, k -> new LoggerImpl(k, CONFIG.get()));
    }

    public static void reload() {
        synchronized (RELOAD_LOCK) {
            LogConfig newConfig = new LogConfig();
            CONFIG.set(newConfig);

            Map<String, ILogger> newCache = new ConcurrentHashMap<>();
            for (String name : CACHE.keySet()) {
                newCache.put(name, new LoggerImpl(name, newConfig));
            }

            // Закрываем старые логгеры
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
        }
    }

    private static final class LoggerImpl implements ILogger {
        private final String name;
        private final java.util.logging.Logger logger;
        private volatile boolean isClosed = false;

        LoggerImpl(String name, LogConfig config) {
            this.name = name;
            this.logger = createJavaLogger(name, config);
        }

        private java.util.logging.Logger createJavaLogger(String name, LogConfig config) {
            String loggerName = "app." + name;
            java.util.logging.Logger logger = java.util.logging.Logger.getLogger(loggerName);

            // Удаляем все существующие обработчики
            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
                handler.close();
            }

            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            addHandlers(logger, config, name);

            return logger;
        }

        private void addHandlers(java.util.logging.Logger logger, LogConfig config, String loggerName) {
            // Файловый обработчик - отдельный для каждого логгера, но в один файл
            if (config.isFileEnabled()) {
                try {
                    Path logDir = Paths.get(config.getLogDir());
                    Files.createDirectories(logDir);

                    // Используем единый файл для всех логов
                    String pattern = logDir.resolve("app.log").toString();

                    // Важно: каждый логгер должен иметь свой FileHandler,
                    // но все они пишут в один файл
                    FileHandler fileHandler = new FileHandler(
                            pattern,           // один файл для всех
                            10 * 1024 * 1024,  // 10MB max size
                            1,                  // только 1 файл (без ротации по индексам)
                            config.isFileAppend()
                    );

                    fileHandler.setLevel(config.getFileLevel());
                    fileHandler.setFormatter(new Formatter(
                            config.isConsoleTimestamp(),  // timestamp
                            false,                         // showThread = false
                            loggerName                      // имя класса для этого логгера
                    ));

                    logger.addHandler(fileHandler);

                } catch (IOException e) {
                    System.err.println("Cannot create file handler for " + loggerName + ": " + e.getMessage());
                }
            }

            // Консольный обработчик
            if (config.isConsoleEnabled()) {
                CustomConsoleHandler ch = new CustomConsoleHandler(config);
                ch.setFormatter(new Formatter(
                        config.isConsoleTimestamp(),
                        config.isShowThread(),
                        loggerName
                ));
                ch.setLevel(config.getConsoleLevel());
                logger.addHandler(ch);
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