package gui;

import gui.panels.LoginPanel;
import gui.panels.RegistrationPanel;
import gui.panels.minecraftservermanager.MPanelManager;
import utilities.ComponentStyler;

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

        LoginPanel loginPanel = new LoginPanel();
        RegistrationPanel registrationPanel = new RegistrationPanel();

        tabbedPane.addTab("Вход", loginPanel);
        tabbedPane.addTab("Регистрация", registrationPanel);
//        tabbedPane.addTab("Рисовалка", new DrawingPanel());
        tabbedPane.addTab("Servers", new MPanelManager());

        frame.add(tabbedPane);

        ComponentStyler.styleContainer(frame);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        return frame;
    }
}
