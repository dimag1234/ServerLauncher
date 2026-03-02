package min.logging;

import min.logging.handlers.CustomConsoleHandler;
import min.logging.formatters.Formatter;
import min.logging.handlers.FileHandlerImpl;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerImpl implements ILogger {
    private final String name;
    private final Logger logger;
    private final LogConfig config;
    private volatile boolean isClosed = false;

    public LoggerImpl(String name, LogConfig config) {
        this.name = name;
        this.config = config;
        this.logger = createJavaLogger(name, config);
    }

    private Logger createJavaLogger(String name, LogConfig config) {
        String loggerName = "app." + name;
        Logger logger = Logger.getLogger(loggerName);

        for (Handler handler : logger.getHandlers()) {
            logger.removeHandler(handler);
            handler.close();
        }

        logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);

        if (config.isFileEnabled()) {
            FileHandlerImpl fileHandler = new FileHandlerImpl(name, config);
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

    @Override
    public void error(String format, Object... args) {
        if (!isClosed) {
            String message = String.format(format, args);
            logger.severe(message);
        }
    }

    @Override
    public void error(String message, Throwable t) {
        if (!isClosed) {
            logger.log(Level.SEVERE, message, t);
        }
    }

    @Override
    public void warn(String format, Object... args) {
        if (!isClosed) {
            String message = String.format(format, args);
            logger.warning(message);
        }
    }

    @Override
    public void info(String format, Object... args) {
        if (!isClosed) {
            String message = String.format(format, args);
            logger.info(message);
        }
    }

    @Override
    public void debug(String format, Object... args) {
        if (!isClosed) {
            String message = String.format(format, args);
            logger.fine(message);
        }
    }

    @Override
    public void trace(String format, Object... args) {
        if (!isClosed) {
            String message = String.format(format, args);
            logger.finer(message);
        }
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
