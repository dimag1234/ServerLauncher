package logging.formatters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.LogRecord;

public class Formatter extends java.util.logging.Formatter {
    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private final boolean showTimestamp;
    private final boolean showThread;
    private final String loggerName;

    public Formatter(boolean showTimestamp, boolean showThread, String loggerName) {
        this.showTimestamp = showTimestamp;
        this.showThread = showThread;
        this.loggerName = loggerName;
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();

        if (showTimestamp) {
            sb.append('[').append(LocalDateTime.now().format(DT)).append("] ");
        }

        if (showThread) {
            sb.append('[').append(Thread.currentThread().getName()).append("] ");
        }

        sb.append('[').append(record.getLevel()).append("] ");
        sb.append('[').append(loggerName).append("] ");
        sb.append(record.getMessage());

        if (record.getThrown() != null) {
            sb.append('\n');
            StringWriter sw = new StringWriter();
            record.getThrown().printStackTrace(new PrintWriter(sw));
            sb.append(sw);
        }

        sb.append('\n');
        return sb.toString();
    }
}