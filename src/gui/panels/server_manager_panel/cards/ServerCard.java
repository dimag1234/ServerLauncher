package gui.panels.server_manager_panel.cards;

import gui.panels.server_manager_panel.SMLogic;
import gui.share.Theme;
import gui.share.Utils;

import javax.swing.*;
import java.awt.*;

public class ServerCard extends JPanel {


    public ServerCard(String serverName, String status, SMLogic logic) {
        // Используем GridBagLayout для точного позиционирования
        setLayout(new GridBagLayout());
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        setBackground(Theme.BACKGROUND_LIGHT);
        setBorder(Utils.createCardBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        // Внутренние отступы для элементов
        gbc.insets = new Insets(0, 15, 0, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- ЛЕВАЯ ЧАСТЬ: Название сервера ---
        JLabel nameLabel = new JLabel(serverName);
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setFont(Theme.FONT_MONOSPACE);

        gbc.gridx = 0;
        gbc.weightx = 1.0; // Занимает доступное пространство слева
        gbc.anchor = GridBagConstraints.WEST;
        add(nameLabel, gbc);

        // --- ЦЕНТРАЛЬНАЯ ЧАСТЬ: Кнопка Edit ---
        JButton editButton = new JButton("Edit");
        styleButton(editButton);
        editButton.addActionListener(e -> logic.openEditServer(serverName));

        gbc.gridx = 1;
        gbc.weightx = 0.0; // Кнопка не растягивается
        gbc.anchor = GridBagConstraints.CENTER;
        add(editButton, gbc);

        // --- ПРАВАЯ ЧАСТЬ: Start и Status (вместе) ---
        // Создаем подпанель, чтобы кнопка и текст не накладывались
        JPanel rightGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightGroup.setOpaque(false);

        JLabel statusLabel = new JLabel(status);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);

        JButton startButton = new JButton("Start");
        styleButton(startButton);
        startButton.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3, false));
        startButton.setBorderPainted(true);
        startButton.addActionListener(e -> {
            logic.startstopbutton(startButton, serverName, statusLabel);
        });

        rightGroup.add(startButton);
        rightGroup.add(statusLabel);

        gbc.gridx = 2;
        gbc.weightx = 1.0; // Занимает доступное пространство справа
        gbc.anchor = GridBagConstraints.EAST;
        add(rightGroup, gbc);
    }

    /**
     * Вспомогательный метод для применения стилей к кнопкам
     */
    private void styleButton(JButton button) {
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setFont(Theme.FONT_MONOSPACE);
        button.setBackground(Theme.BACKGROUND_LIGHT);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); // Чтобы кнопка была плоской
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
