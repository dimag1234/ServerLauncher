package logging;

public enum Level {
    ERROR, WARN, INFO, DEBUG, TRACE, OFF, ALL;

    public java.util.logging.Level toJavaLevel() {
        return switch (this) {
            case ERROR -> java.util.logging.Level.SEVERE;
            case WARN -> java.util.logging.Level.WARNING;
            case INFO -> java.util.logging.Level.INFO;
            case DEBUG -> java.util.logging.Level.FINE;
            case TRACE -> java.util.logging.Level.FINER;
            case OFF -> java.util.logging.Level.OFF;
            case ALL -> java.util.logging.Level.ALL;
        };
    }

    public static Level fromString(String s) {
        if (s == null) return INFO;
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return INFO;
        }
    }
}