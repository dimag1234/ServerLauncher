package org.min.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.min.gui.common.Theme;
import org.min.gui.dialogs.SettingsDialog;
import org.min.gui.panels.server_manager_panel.SMPanel;
import org.min.logging.ILogger;
import org.min.logging.Loggers;

public class WindowManager {

    private static final ILogger logger = Loggers.get(WindowManager.class);
    private static Stage mainStage;

    public static void createWindow(Stage stage, String title) {
        logger.info("Creating window: %s", title);

        mainStage = stage;
        mainStage.setTitle(title);
        mainStage.setOnCloseRequest(e -> { e.consume(); handleWindowClose(); });

        // ── Root layout ─────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(createMenuBar());

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().add(new Tab("Серверы", new SMPanel()));
        root.setCenter(tabPane);

        // ── Scene ────────────────────────────────────────────
        Scene scene = new Scene(root, 1280, 780);
        scene.getStylesheets().add(
                WindowManager.class.getResource(Theme.CSS_PATH).toExternalForm());

        mainStage.setScene(scene);
        mainStage.setMinWidth(900);
        mainStage.setMinHeight(600);
        mainStage.show();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application %s via shutdown hook", "shutting down");
            Loggers.shutdown();
        }));

        logger.info("Window %s successfully", "created");
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
