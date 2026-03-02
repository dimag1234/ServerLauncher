package gui.common;

import settings.AppSettings;
import java.awt.*;

public class Theme {
    public static Color BACKGROUND_DARK;
    public static Color BACKGROUND_MEDIUM;
    public static Color BACKGROUND_LIGHT;
    public static Color BORDER_DARK;
    public static Color TEXT_PRIMARY;
    public static Color TEXT_SECONDARY;
    public static Color BUTTON_PRIMARY;

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_MONOSPACE = new Font("Monospaced", Font.BOLD, 14);

    public static final int PADDING_SMALL = 8;
    public static final int PADDING_MEDIUM = 15;
    public static final int PADDING_LARGE = 20;

    static {
        applyTheme();
    }

    public static void applyTheme() {
        AppSettings settings = AppSettings.getInstance();
        boolean isDark = "DARK".equals(settings.getTheme());

        if (isDark) {
            // Тёмная тема
            BACKGROUND_DARK = new Color(45, 45, 45);
            BACKGROUND_MEDIUM = new Color(60, 63, 65);
            BACKGROUND_LIGHT = new Color(70, 73, 75);
            BORDER_DARK = new Color(30, 30, 30);
            TEXT_PRIMARY = Color.WHITE;
            TEXT_SECONDARY = new Color(180, 180, 180);
            BUTTON_PRIMARY = new Color(70, 140, 70);
        } else {
            BACKGROUND_DARK = new Color(230, 230, 230);
            BACKGROUND_MEDIUM = new Color(245, 245, 245);
            BACKGROUND_LIGHT = Color.WHITE;
            BORDER_DARK = new Color(150, 150, 150);
            TEXT_PRIMARY = Color.BLACK;
            TEXT_SECONDARY = new Color(80, 80, 80);
            BUTTON_PRIMARY = new Color(0, 120, 200);
        }
    }
}
