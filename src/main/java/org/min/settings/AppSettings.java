package org.min.settings;

import org.min.logging.ILogger;
import org.min.logging.Loggers;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public class AppSettings {

    private static final ILogger logger = Loggers.get(AppSettings.class);
    private static final Path SETTINGS_FILE = Paths.get("config", "app.properties");
    private static volatile AppSettings instance;

    private String fontFamily = "Segoe UI";
    private int    fontSize   = 14;
    private String fontStyle  = "PLAIN";
    private String theme      = "DARK";

    private AppSettings() {
        logger.info("Initializing %s", "AppSettings");
        createConfigDirIfNeeded();
        load();
    }

    public static AppSettings getInstance() {
        if (instance == null) {
            synchronized (AppSettings.class) {
                if (instance == null) instance = new AppSettings();
            }
        }
        return instance;
    }

    // ──────────────────────────────────────────────────────────────
    private void createConfigDirIfNeeded() {
        try {
            Files.createDirectories(SETTINGS_FILE.getParent());
            logger.debug("Config directory %s at: %s", "created/verified", SETTINGS_FILE.getParent());
        } catch (IOException e) {
            logger.error("Cannot create config directory", e);
        }
    }

    public void load() {
        logger.info("Loading settings from: %s", SETTINGS_FILE.toAbsolutePath());
        if (!Files.exists(SETTINGS_FILE)) {
            logger.warn("Settings file %s, using defaults", "not found");
            return;
        }
        Properties props = new Properties();
        try (InputStream is = Files.newInputStream(SETTINGS_FILE)) {
            props.load(is);
            fontFamily = props.getProperty("font.family", "Segoe UI");
            fontSize   = Integer.parseInt(props.getProperty("font.size", "14"));
            fontStyle  = props.getProperty("font.style", "PLAIN");
            theme      = props.getProperty("theme", "DARK");
            logger.info("Settings %s successfully", "loaded");
            logger.debug("fontFamily=%s  fontSize=%s  fontStyle=%s  theme=%s",
                    fontFamily, fontSize, fontStyle, theme);
        } catch (Exception e) {
            logger.error("Error loading settings", e);
        }
    }

    public void save() {
        logger.info("Saving settings to: %s", SETTINGS_FILE.toAbsolutePath());
        Properties props = new Properties();
        props.setProperty("font.family", fontFamily);
        props.setProperty("font.size",   String.valueOf(fontSize));
        props.setProperty("font.style",  fontStyle);
        props.setProperty("theme",       theme);
        try (OutputStream os = Files.newOutputStream(SETTINGS_FILE)) {
            props.store(os, "App Settings");
            logger.info("Settings %s successfully", "saved");
        } catch (IOException e) {
            logger.error("Error saving settings", e);
        }
    }

    // ── Getters ──────────────────────────────────────────────────
    public String getFontFamily() { logger.trace("getFontFamily → %s", fontFamily); return fontFamily; }
    public int    getFontSize()   { logger.trace("getFontSize   → %s", fontSize);   return fontSize; }
    public String getFontStyle()  { logger.trace("getFontStyle  → %s", fontStyle);  return fontStyle; }
    public String getTheme()      { logger.trace("getTheme      → %s", theme);      return theme; }

    // ── Setters ──────────────────────────────────────────────────
    public void setFont(String family, int size, String style) {
        logger.info("Setting font to: %s %s %s", family, size, style);
        this.fontFamily = family;
        this.fontSize   = size;
        this.fontStyle  = style;
        save();
    }

    public void setTheme(String theme) {
        logger.info("Setting theme to: %s", theme);
        this.theme = theme;
        save();
    }

    /**
     * Builds a JavaFX-compatible inline CSS string for font properties.
     * Use with Node.setStyle() — e.g. root.setStyle(settings.getFontCss()).
     *
     * Previously returned HTML font shorthand syntax which is invalid in JavaFX.
     */
    public String getFontCss() {
        String weight = fontStyle.contains("BOLD")   ? "bold"   : "normal";
        String italic = fontStyle.contains("ITALIC")  ? "italic" : "normal";
        return "-fx-font-family: '" + fontFamily + "';" +
                "-fx-font-size: "   + fontSize   + "px;" +
                "-fx-font-weight: " + weight     + ";" +
                "-fx-font-style: "  + italic     + ";";
    }
}