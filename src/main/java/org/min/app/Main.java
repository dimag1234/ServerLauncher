package org.min.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.min.gui.WindowManager;
import org.min.logging.ILogger;
import org.min.logging.Loggers;

public class Main extends Application {

    private static final ILogger logger = Loggers.get(Main.class);

    public static void main(String[] args) {
        logger.info("Application %s", "starting");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Force Modena — prevents Astra Linux / GTK from hijacking JavaFX styles
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        try {
            WindowManager.createWindow(primaryStage, "Менеджер Серверов");
            logger.info("Main window %s successfully", "created");
        } catch (Exception e) {
            logger.error("Failed to create main window", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Startup Error");
            alert.setHeaderText("Critical error");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            Platform.exit();
        }
    }
}