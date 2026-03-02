package gui;

import gui.dialogs.SettingsDialog;
import gui.panels.server_manager_panel.SMPanel;
import logging.ILogger;
import logging.Loggers;
import logging.Loggers;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowManager {
    private static final ILogger logger = Loggers.get(WindowManager.class);
    private static JFrame mainFrame;

    public static JFrame createWindow(String title) {
        logger.info("Creating window: " + title);

        mainFrame = new JFrame(title);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 2 / 3.0);
        int height = (int) (screenSize.height * 2 / 3.0);
        mainFrame.setSize(width, height);
        mainFrame.setLocationRelativeTo(null);

        JMenuBar menuBar = getMenuBar();
        mainFrame.setJMenuBar(menuBar);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Сервера", new SMPanel());
        mainFrame.add(tabbedPane);

        // Добавляем обработчик закрытия через диспетчер задач (по сути то же самое)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application shutting down via shutdown hook");
            shutdownLogging();
        }));

        mainFrame.setVisible(true);
        logger.info("Window created successfully");
        return mainFrame;
    }

    private static JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu settingsMenu = new JMenu("Настройки");
        JMenuItem settingsItem = new JMenuItem("Параметры");
        settingsItem.addActionListener((ActionEvent _) -> {
            SettingsDialog dialog = new SettingsDialog(mainFrame);
            dialog.setVisible(true);
        });
        settingsMenu.add(settingsItem);

        JMenuItem exitItem = new JMenuItem("Выход");
        exitItem.addActionListener(e -> handleWindowClose());
        settingsMenu.addSeparator();
        settingsMenu.add(exitItem);

        menuBar.add(settingsMenu);
        return menuBar;
    }

    private static void handleWindowClose() {
        logger.info("Window close requested");

        // TODO: Here you can add checks for unsaved data.

        int result = JOptionPane.showConfirmDialog(mainFrame,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            logger.info("Shutting down application");

            // TODO: Stopping all running servers

            shutdownLogging();

            System.exit(0);
        } else {
            logger.debug("Window close cancelled by user");
        }
    }

    private static void shutdownLogging() {
        try {
            logger.info("Shutting down logging system");
            Loggers.shutdown();
        } catch (Exception e) {
            System.err.println("Error during logging shutdown: " + e.getMessage());
        }
    }
}
