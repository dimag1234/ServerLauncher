package gui;

import gui.panels.server_manager_panel.SMPanel;

import javax.swing.*;
import java.awt.*;

public class WindowManager {
    public static JFrame createWindow(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 2 / 3.0);
        int height = (int) (screenSize.height * 2 / 3.0);
        frame.setSize(width, height);

        JTabbedPane tabbedPane = new JTabbedPane();
//        tabbedPane.addTab("Вход", loginPanel);
//        tabbedPane.addTab("Регистрация", registrationPanel);
        tabbedPane.addTab("", new SMPanel());

        frame.add(tabbedPane);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }
}