package gui.panels.server_manager_panel.cards;

import gui.panels.server_manager_panel.SMLogic;
import gui.share.Theme;
import gui.share.Utils;
import javax.swing.*;
import java.awt.*;

public class ServerCard extends JPanel {

    public ServerCard(String serverName, String status, SMLogic logic) {
        setLayout(new BorderLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setBackground(Theme.BACKGROUND_LIGHT);
        setBorder(Utils.createCardBorder());

        JLabel nameLabel = new JLabel(serverName);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setFont(Theme.FONT_MONOSPACE);

        JButton editButton = new JButton("Edit");
        editButton.setForeground(Theme.TEXT_PRIMARY);
        editButton.setFont(Theme.FONT_MONOSPACE);
        editButton.setBackground(Theme.BACKGROUND_LIGHT);
        editButton.setBorderPainted(false);
        editButton.setFocusPainted(false);
        editButton.addActionListener(e -> logic.openEditServer(serverName));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);

        add(nameLabel, BorderLayout.WEST);
        add(editButton, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.EAST);
    }
}