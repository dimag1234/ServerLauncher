package min.gui;

import min.gui.dialogs.SettingsDialog;
import min.gui.panels.server_manager_panel.SMPanel;
import min.logging.ILogger;
import min.logging.Loggers;
import min.settings.AppSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowManager {
    private static final ILogger logger = Loggers.get(WindowManager.class);
    private static JFrame mainFrame;

    public static JFrame createWindow(String title) {
        logger.info("Creating window: %s", title);

        setGlobalFont();

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

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Application %s via shutdown hook", "shutting down");
            shutdownLogging();
        }));

        mainFrame.setVisible(true);
        logger.info("Window %s successfully", "created");
        return mainFrame;
    }

    private static void setGlobalFont() {
        try {
            AppSettings settings = AppSettings.getInstance();
            Font font = settings.getFont();

            UIManager.put("Button.font", font);
            UIManager.put("ToggleButton.font", font);
            UIManager.put("RadioButton.font", font);
            UIManager.put("CheckBox.font", font);
            UIManager.put("ColorChooser.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("Label.font", font);
            UIManager.put("List.font", font);
            UIManager.put("MenuBar.font", font);
            UIManager.put("Menu.font", font);
            UIManager.put("MenuItem.font", font);
            UIManager.put("RadioButtonMenuItem.font", font);
            UIManager.put("CheckBoxMenuItem.font", font);
            UIManager.put("PopupMenu.font", font);
            UIManager.put("OptionPane.font", font);
            UIManager.put("Panel.font", font);
            UIManager.put("ProgressBar.font", font);
            UIManager.put("ScrollPane.font", font);
            UIManager.put("Viewport.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("Table.font", font);
            UIManager.put("TableHeader.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("PasswordField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("TextPane.font", font);
            UIManager.put("EditorPane.font", font);
            UIManager.put("TitledBorder.font", font);
            UIManager.put("ToolBar.font", font);
            UIManager.put("ToolTip.font", font);
            UIManager.put("Tree.font", font);

            logger.debug("Global font set to: %s", font);
        } catch (Exception e) {
            logger.error("Failed to set global font", e);
        }
    }

    private static JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(Box.createHorizontalGlue());
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
        logger.info("Window close %s", "requested");

        int result = JOptionPane.showConfirmDialog(mainFrame,
                "Вы действительно хотите выйти?",
                "Подтверждение выхода",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            logger.info("Shutting down %s", "application");
            shutdownLogging();
            System.exit(0);
        } else {
            logger.debug("Window close %s by user", "cancelled");
        }
    }

    private static void shutdownLogging() {
        try {
            logger.info("Shutting down %s system", "min/logging");
            Loggers.shutdown();
        } catch (Exception e) {
            System.err.println("Error during logging shutdown: " + e.getMessage());
        }
    }
}
