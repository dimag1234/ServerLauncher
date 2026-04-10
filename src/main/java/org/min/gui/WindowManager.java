package org.min.gui;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.gui.dialogs.SettingsDialog;
import org.min.gui.panels.server_manager_panel.SMPanel;
import org.min.logging.ILogger;
import org.min.logging.Loggers;
import org.min.settings.AppSettings;

public class WindowManager {

    private static final ILogger logger = Loggers.get(WindowManager.class);
    private static Stage      mainStage;
    private static BorderPane mainRoot;   // kept for live theme/font updates

    public static void createWindow(Stage stage, String title) {
        logger.info("Creating window: %s", title);

        mainStage = stage;
        mainStage.setTitle(title);
        mainStage.setOnCloseRequest(e -> { e.consume(); handleWindowClose(); });

        // ── Root layout ─────────────────────────────────────
        mainRoot = new BorderPane();
        mainRoot.setTop(createMenuBar());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab("Серверы", new SMPanel()));
        mainRoot.setCenter(tabPane);

        // ── Scene ────────────────────────────────────────────
        Scene scene = new Scene(mainRoot, 1280, 780);
        scene.getStylesheets().add(
                WindowManager.class.getResource(Theme.CSS_PATH).toExternalForm());

        mainStage.setScene(scene);
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(600);
        mainStage.show();

        // ── Apply saved theme & font ─────────────────────────
        AppSettings as = AppSettings.getInstance();
        applyTheme(as.getTheme());
        applyFont(as.getFontFamily(), as.getFontSize(), as.getFontStyle());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application %s via shutdown hook", "shutting down");
            Loggers.shutdown();
        }));

        logger.info("Window %s successfully", "created");
    }

    // ── Live theme application ────────────────────────────────
    /** Call after saving settings to update the UI without restarting. */
    public static void applyTheme(String theme) {
        if (mainRoot == null) return;
        Platform.runLater(() -> {
            if ("LIGHT".equals(theme)) {
                if (!mainRoot.getStyleClass().contains("light-theme"))
                    mainRoot.getStyleClass().add("light-theme");
            } else {
                mainRoot.getStyleClass().remove("light-theme");
            }
            logger.info("Theme %s applied", theme);
        });
    }

    /** Call after saving settings to update the font without restarting. */
    public static void applyFont(String family, int size, String style) {
        if (mainRoot == null) return;
        Platform.runLater(() -> {
            String weight = style.contains("BOLD")   ? "bold"   : "normal";
            String italic = style.contains("ITALIC")  ? "italic" : "normal";
            mainRoot.setStyle(
                    "-fx-font-family: '" + family + "';" +
                            "-fx-font-size: " + size + "px;" +
                            "-fx-font-weight: " + weight + ";" +
                            "-fx-font-style: " + italic + ";"
            );
            logger.info("Font applied: %s %spx %s", family, size, style);
        });
    }

    /** Returns the current theme string ("DARK" or "LIGHT"). */
    public static String getCurrentTheme() {
        return AppSettings.getInstance().getTheme();
    }

    // ── Menu bar ─────────────────────────────────────────────
    private static MenuBar createMenuBar() {
        MenuBar bar = new MenuBar();

        Menu settingsMenu = new Menu("Настройки");

        MenuItem settingsItem = new MenuItem("Параметры");
        settingsItem.setOnAction(e -> new SettingsDialog(mainStage).showAndWait());

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> handleWindowClose());

        settingsMenu.getItems().addAll(settingsItem, sep, exitItem);
        bar.getMenus().add(settingsMenu);
        return bar;
    }

    // ── Exit confirmation ─────────────────────────────────────
    private static void handleWindowClose() {
        logger.info("Window close %s", "requested");

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение выхода");
        confirm.setHeaderText(null);
        confirm.setContentText("Вы действительно хотите выйти?");
        confirm.initOwner(mainStage);
        FxUtils.style(confirm);

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                logger.info("Shutting down %s", "application");
                Loggers.shutdown();
                Platform.exit();
                System.exit(0);
            } else {
                logger.debug("Window close %s by user", "cancelled");
            }
        });
    }

    public static Stage getMainStage() { return mainStage; }
}