package app;

import gui.WindowManager;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = WindowManager.createWindow("Форма для авторизации");
        });
    }
}
