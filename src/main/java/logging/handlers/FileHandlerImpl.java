package logging.handlers;

import logging.LogConfig;
import logging.Loggers;
import logging.formatters.Formatter;

import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class FileHandlerImpl extends Handler {
    private final String loggerName;
    private final LogConfig config;
    private volatile boolean isClosed = false;

    public FileHandlerImpl(String loggerName, LogConfig config) {
        this.loggerName = loggerName;
        this.config = config;
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

        PrintStream ps = Loggers.getFilePrintStream(config);
        if (ps != null) {
            String msg = getFormatter().format(record);
            synchronized (Loggers.getFileStreamLock()) {
                ps.print(msg);
                ps.flush();
            }
        }
    }

    @Override
    public void flush() {
        PrintStream ps = Loggers.getFilePrintStream(config);
        if (ps != null) {
            synchronized (Loggers.getFileStreamLock()) {
                ps.flush();
            }
        }
    }

    @Override
    public void close() throws SecurityException {
        isClosed = true;
    }
}
