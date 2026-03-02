package logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;

public final class LogConfig {
    private static final Path CONFIG_PATH = Paths.get("config", "logging.properties");

    // File settings
    private final Level fileLevel;
    private final String logDir;
    private final String filePattern;
    private final boolean fileEnabled;
    private final boolean fileAppend;

    // Console settings
    private final Level consoleLevel;
    private final boolean consoleEnabled;
    private final boolean colorEnabled;
    private final boolean showThread;
    private final boolean consoleTimestamp;

    private final StringBuilder validationErrors = new StringBuilder();

    public LogConfig() {
        Properties props = loadProperties();

        // File settings
        this.fileEnabled = parseBoolean(props.getProperty("file.enabled"), true);
        this.logDir = validateLogDirectory(props.getProperty("file.directory", "logs"));
        this.fileLevel = Level.parse(props.getProperty("file.level", "INFO"));
        this.filePattern = props.getProperty("file.pattern", "app_%d{yyyy-MM-dd}.log");
        this.fileAppend = parseBoolean(props.getProperty("file.append"), true);

        // Console settings
        this.consoleEnabled = parseBoolean(props.getProperty("console.enabled"), true);
        this.consoleLevel = Level.parse(props.getProperty("console.level", "WARNING"));
        this.colorEnabled = parseBoolean(props.getProperty("console.color"), true);
        this.showThread = parseBoolean(props.getProperty("console.thread"), false);
        this.consoleTimestamp = parseBoolean(props.getProperty("console.timestamp"), true);

        if (!validationErrors.isEmpty()) {
            System.err.println("WARNING: Logging configuration validation errors:");
            System.err.println(validationErrors.toString());
        }
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (Reader r = Files.newBufferedReader(CONFIG_PATH)) {
                props.load(r);
            } catch (IOException e) {
                System.err.println("Failed to load logging config: " + e.getMessage());
                validationErrors.append("Failed to load config: ").append(e.getMessage()).append("\n");
            }
        } else {
            createDefaultConfig(props);
        }
        return props;
    }

    private void createDefaultConfig(Properties props) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer w = Files.newBufferedWriter(CONFIG_PATH)) {
                props.setProperty("file.enabled", "true");
                props.setProperty("file.directory", "logs");
                props.setProperty("file.level", "INFO");
                props.setProperty("file.pattern", "app_%d{yyyy-MM-dd}.log");
                props.setProperty("file.append", "true");

                props.setProperty("console.enabled", "true");
                props.setProperty("console.level", "WARNING");
                props.setProperty("console.color", "true");
                props.setProperty("console.thread", "false");
                props.setProperty("console.timestamp", "true");

                props.store(w, "Logging configuration");
            }
        } catch (IOException e) {
            System.err.println("Failed to create default config: " + e.getMessage());
            validationErrors.append("Failed to create default config: ").append(e.getMessage()).append("\n");
        }
    }

    private String validateLogDirectory(String dir) {
        if (dir == null || dir.trim().isEmpty()) {
            validationErrors.append("Log directory is empty, using default 'logs'\n");
            return "logs";
        }

        Path dirPath = Paths.get(dir);

        try {
            Files.createDirectories(dirPath);

            if (!Files.isWritable(dirPath)) {
                validationErrors.append("Log directory is not writable: ").append(dir).append("\n");
                String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "app_logs";
                Files.createDirectories(Paths.get(tempDir));
                validationErrors.append("Using fallback directory: ").append(tempDir).append("\n");
                return tempDir;
            }
        } catch (IOException e) {
            validationErrors.append("Cannot create log directory: ").append(dir)
                    .append(", error: ").append(e.getMessage()).append("\n");
            String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "app_logs";
            try {
                Files.createDirectories(Paths.get(tempDir));
                return tempDir;
            } catch (IOException ex) {
                validationErrors.append("Cannot create fallback directory either\n");
                return "logs";
            }
        }

        return dir;
    }

    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null) return defaultValue;
        value = value.trim().toLowerCase();
        if (value.equals("true") || value.equals("yes") || value.equals("on") || value.equals("1")) {
            return true;
        }
        if (value.equals("false") || value.equals("no") || value.equals("off") || value.equals("0")) {
            return false;
        }
        validationErrors.append("Invalid boolean value: '").append(value)
                .append("', using default: ").append(defaultValue).append("\n");
        return defaultValue;
    }

    // File settings
    public boolean isFileEnabled() { return fileEnabled; }
    public String getLogDir() { return logDir; }
    public Level getFileLevel() { return fileLevel; }
    public String getFilePattern() { return filePattern; }
    public boolean isFileAppend() { return fileAppend; }

    // Console settings
    public boolean isConsoleEnabled() { return consoleEnabled; }
    public Level getConsoleLevel() { return consoleLevel; }
    public boolean isColorEnabled() { return colorEnabled; }
    public boolean isShowThread() { return showThread; }
    public boolean isConsoleTimestamp() { return consoleTimestamp; }

    public boolean hasValidationErrors() {
        return !validationErrors.isEmpty();
    }

    public String getValidationErrors() {
        return validationErrors.toString();
    }
}