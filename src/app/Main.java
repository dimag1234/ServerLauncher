package app;

import gui.WindowManager;
import logging.ILogger;
import logging.Loggers;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = WindowManager.createWindow("Менеджер серверов");
        });
    }
}
