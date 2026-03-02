package settings;

import logging.ILogger;
import logging.Loggers;

import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class AppSettings {
    private static final ILogger logger = Loggers.get(AppSettings.class);
    private static final Path SETTINGS_FILE = Paths.get("config", "app.properties");
    private static volatile AppSettings instance;

    private String fontFamily = "Segoe UI";
    private int fontSize = 14;
    private String fontStyle = "PLAIN";
    private String theme = "DARK";

    private AppSettings() {
        logger.info("Initializing AppSettings");
        createConfigDirIfNeeded();
        load();
    }

    public static AppSettings getInstance() {
        if (instance == null) {
            synchronized (AppSettings.class) {
                if (instance == null) {
                    instance = new AppSettings();
                }
            }
        }
        return instance;
    }

    private void createConfigDirIfNeeded() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            logger.debug("Config directory created/verified at: %s", SETTINGS_FILE.getParent());
        } catch (IOException e) {
            logger.error("Cannot create config directory", e);
        }
    }

    public void load() {
        logger.info("Loading settings from: " + SETTINGS_FILE.toAbsolutePath());

        if (!Files.exists(SETTINGS_FILE)) {
            logger.warn("Settings file not found, using defaults");
            return;
        }

        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(SETTINGS_FILE)) {
            props.load(is);

            fontFamily = props.getProperty("font.family", "Segoe UI");
            fontSize = Integer.parseInt(props.getProperty("font.size", "14"));
            fontStyle = props.getProperty("font.style", "PLAIN");
            theme = props.getProperty("theme", "DARK");

            logger.info("Settings loaded successfully");
            logger.debug("Loaded values - fontFamily: %s, fontSize: %s, fontStyle: %s, theme: %s",
                    fontFamily, fontSize, fontStyle, theme);

        } catch (Exception e) {
            logger.error("Error loading settings", e);
        }
    }

    public void save() {
        logger.info("Saving settings to: " + SETTINGS_FILE.toAbsolutePath());

        Properties props = new Properties();

        props.setProperty("font.family", fontFamily);
        props.setProperty("font.size", String.valueOf(fontSize));
        props.setProperty("font.style", fontStyle);
        props.setProperty("theme", theme);

        try (OutputStream os = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(os, "SwingCoding Application Settings");
            logger.info("Settings saved successfully");
            logger.debug("Saved values - fontFamily: %s, fontSize: %s, fontStyle: %s, theme: %s",
                    fontFamily, fontSize, fontStyle, theme);
        } catch (IOException e) {
            logger.error("Error saving settings", e);
        }
    }

    public Font getFont() {
        int style = Font.PLAIN;
        style = switch (fontStyle.toUpperCase()) {
            case "BOLD" -> Font.BOLD;
            case "ITALIC" -> Font.ITALIC;
            case "BOLDITALIC" -> Font.BOLD | Font.ITALIC;
            default -> style;
        };
        Font font = new Font(fontFamily, style, fontSize);
        logger.trace("Getting font: " + font);
        return font;
    }

    public void setFont(Font font) {
        logger.info("Setting font to: " + font);
        this.fontFamily = font.getFamily();
        this.fontSize = font.getSize();
        this.fontStyle = font.isBold() && font.isItalic() ? "BOLDITALIC" :
                font.isBold() ? "BOLD" :
                        font.isItalic() ? "ITALIC" : "PLAIN";
        save();
    }

    public String getTheme() {
        logger.trace("Getting theme: " + theme);
        return theme;
    }

    public void setTheme(String theme) {
        logger.info("Setting theme to: " + theme);
        this.theme = theme;
        save();
    }
}