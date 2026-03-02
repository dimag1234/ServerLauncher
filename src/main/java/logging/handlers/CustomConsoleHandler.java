package logging.handlers;

import logging.LogConfig;

import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;

public class CustomConsoleHandler extends ConsoleHandler {
    private final LogConfig config;
    private volatile boolean isClosed = false;

    private static final String
            RESET = "\u001B[0m",
            RED = "\u001B[31m",
            GREEN = "\u001B[32m",
            YELLOW = "\u001B[33m",
            CYAN = "\u001B[36m";

    private static final boolean SUPPORTS_COLOR = System.console() != null ||
            "true".equals(System.getenv("TERM")) ||
            System.getenv("ANSICON") != null;

    public CustomConsoleHandler(LogConfig config) {
        this.config = config;
    }

    @Override
    public void publish(LogRecord record) {
        if (isClosed || !isLoggable(record)) {
            return;
        }

        String msg = getFormatter().format(record);

        if (config.isColorEnabled() && SUPPORTS_COLOR) {
            String color = switch (record.getLevel().getName()) {
                case "SEVERE" -> RED;
                case "WARNING" -> YELLOW;
                case "INFO" -> GREEN;
                case "FINE", "FINER", "FINEST" -> CYAN;
                default -> "";
            };

            if (!color.isEmpty()) {
                msg = color + msg + RESET;
            }
        }

        synchronized (System.out) {
            System.out.print(msg);
            System.out.flush();
        }
    }

    @Override
    public void close() throws SecurityException {
        isClosed = true;
        super.close();
    }

    @Override
    public void flush() {
        if (!isClosed) {
            super.flush();
        }
    }
}