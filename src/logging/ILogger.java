package logging;

public interface ILogger extends AutoCloseable {
    void error(String format, Object... args);
    void error(String message, Throwable t);
    void warn(String format, Object... args);
    void info(String format, Object... args);
    void debug(String format, Object... args);
    void trace(String format, Object... args);

    boolean isDebugEnabled();
    boolean isTraceEnabled();

    @Override
    void close();
}
