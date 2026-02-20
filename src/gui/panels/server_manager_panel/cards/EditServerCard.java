package gui.panels.server_manager_panel.cards;

import gui.share.Theme;
import gui.share.Utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditServerCard extends JPanel {


    public EditServerCard(String serverName) {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND_DARK);
        setBorder(new EmptyBorder(Theme.PADDING_LARGE, Theme.PADDING_LARGE,
                Theme.PADDING_LARGE, Theme.PADDING_LARGE));

        add(createHeaderPanel(serverName), BorderLayout.NORTH);

        add(createContentPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel(String serverName) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Настройки: " + serverName);
        title.setFont(Theme.FONT_SUBTITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        headerPanel.add(title, BorderLayout.WEST);
        return headerPanel;
    }

    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.BACKGROUND_MEDIUM);
        contentPanel.setBorder(Utils.createContentBorder());

        addInfoRow(contentPanel, "Путь к серверу:", "");
        addInfoRow(contentPanel, "Версия ядра:", "Paper 1.21.11");
        addInfoRow(contentPanel, "Статус:", "Готов к запуску");

        return contentPanel;
    }

    private void addInfoRow(JPanel container, String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setFont(Theme.FONT_REGULAR);

        JLabel value = new JLabel(valueText);
        value.setForeground(Theme.TEXT_PRIMARY);
        value.setFont(Theme.FONT_MONOSPACE);

        row.add(label, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);

        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
    }
}