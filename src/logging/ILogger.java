package logging;

public interface ILogger extends AutoCloseable {
    void error(String message);
    void error(String message, Throwable t);
    void warn(String message);
    void info(String message);
    void debug(String message);
    void debug(String format, Object... args);
    void trace(String message);
    boolean isDebugEnabled();
    boolean isTraceEnabled();

    @Override
    void close();
}