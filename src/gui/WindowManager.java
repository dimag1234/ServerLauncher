package gui;

import gui.panels.server_manager_panel.SMPanel;
import logging.ILogger;
import logging.Loggers;

import javax.swing.*;
import java.awt.*;

public class WindowManager {
    private static final ILogger logger = Loggers.get(WindowManager.class);

    public static JFrame createWindow(String title) {
        logger.info("Creating window: " + title);

        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 2 / 3.0);
        int height = (int) (screenSize.height * 2 / 3.0);

        frame.setSize(width, height);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("", new SMPanel());

        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        logger.info("Window created successfully");
        return frame;
    }
}