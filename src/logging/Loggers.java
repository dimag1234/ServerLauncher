package logging;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

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

    public static PrintStream getFilePrintStream(LogConfig config) {
        if (filePrintStream == null) {
            synchronized (FILE_STREAM_LOCK) {
                if (filePrintStream == null && config.isFileEnabled()) {
                    try {
                        Path logDir = Paths.get(config.getLogDir());
                        Files.createDirectories(logDir);

                        Path logFile = logDir.resolve(config.getFilePattern());

                        FileOutputStream fos = new FileOutputStream(logFile.toFile(), config.isFileAppend());
                        filePrintStream = new PrintStream(fos, true, StandardCharsets.UTF_8);

                        System.out.println("Log file created: " + logFile.toAbsolutePath());
                    } catch (IOException e) {
                        System.err.println("Cannot create file stream: " + e.getMessage());
                    }
                }
            }
        }
        return filePrintStream;
    }

    public static Object getFileStreamLock() {
        return FILE_STREAM_LOCK;
    }
}
