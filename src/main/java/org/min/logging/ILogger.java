package org.min.logging;

public interface ILogger {
    void trace(String msg, Object... args);
    void debug(String msg, Object... args);
    void info(String msg, Object... args);
    void warn(String msg, Object... args);
    void error(String msg, Object... args);
    void error(String msg, Throwable t);
}
