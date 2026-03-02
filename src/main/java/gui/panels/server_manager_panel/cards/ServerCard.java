package gui.panels.server_manager_panel.cards;

import gui.panels.server_manager_panel.SMLogic;
import gui.common.Theme;
import gui.common.Utils;
import settings.ServerSettings.ServerCardSettings;

import javax.swing.*;
import java.awt.*;

public class ServerCard extends JPanel {

    private final ServerCardSettings card;
    private final SMLogic logic;
    private final JLabel statusLabel;
    private final JButton startBtn;

    public ServerCard(ServerCardSettings card, SMLogic logic) {
        this.card = card;
        this.logic = logic;

        setLayout(new GridBagLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        setBackground(Theme.BACKGROUND_LIGHT);
        setBorder(Utils.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 15, 8, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Название
        JLabel name = new JLabel(card.getDisplayName());
        name.setForeground(Theme.TEXT_PRIMARY);
        name.setFont(Theme.FONT_MONOSPACE.deriveFont(16f));

        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        add(name, gbc);

        // Edit
        JButton edit = new JButton("Edit");
        styleButton(edit);
        edit.addActionListener(e -> logic.openEditServer(card.getServerFolderName()));

        gbc.gridx = 1;
        gbc.weightx = 0;
        add(edit, gbc);

        // Правая часть
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        statusLabel = new JLabel("Остановлен");
        statusLabel.setForeground(Theme.TEXT_SECONDARY);

        startBtn = new JButton("Start");
        styleButton(startBtn);
        startBtn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
        startBtn.setBorderPainted(true);
        startBtn.addActionListener(e -> logic.startstopbutton(startBtn, card.getServerFolderName(), statusLabel, card));

        right.add(startBtn);
        right.add(statusLabel);

        gbc.gridx = 2;
        gbc.weightx = 0.6;
        gbc.anchor = GridBagConstraints.EAST;
        add(right, gbc);
    }

    private void styleButton(JButton b) {
        b.setForeground(Theme.TEXT_PRIMARY);
        b.setFont(Theme.FONT_MONOSPACE);
        b.setBackground(Theme.BACKGROUND_LIGHT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}