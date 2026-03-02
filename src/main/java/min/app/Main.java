package min.app;

import min.gui.WindowManager;
import min.logging.ILogger;
import min.logging.Loggers;

import javax.swing.*;

public class Main {
    private static final ILogger logger = Loggers.get(Main.class);

    public static void main(String[] args) {
        logger.info("Application %s", "starting");

        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = WindowManager.createWindow("Менеджер Серверов");
                logger.info("Main window %s successfully", "created");
            } catch (Exception e) {
                logger.error("Failed to create main window", e);
                JOptionPane.showMessageDialog(null,
                        "Critical error: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
