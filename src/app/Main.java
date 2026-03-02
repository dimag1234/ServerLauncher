package app;

import gui.WindowManager;
import logging.ILogger;
import logging.Loggers;

import javax.swing.*;

public class Main {
    private static final ILogger logger = Loggers.get(Main.class);

    public static void main(String[] args) {
        logger.info("123");
        SwingUtilities.invokeLater(() -> {
            JFrame frame = WindowManager.createWindow("Менеджер серверов");
        });
    }
}
