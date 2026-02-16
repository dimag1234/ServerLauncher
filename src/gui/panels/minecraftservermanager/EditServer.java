package gui.panels.minecraftservermanager;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditServer extends JPanel {

    public EditServer(String serverName) {
        // Темная тема и отступы как в MPanelManager
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(new Color(45, 45, 45));
        this.setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- ВЕРХНЯЯ ЧАСТЬ (Заголовок) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Настройки: " + serverName);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        this.add(headerPanel, BorderLayout.NORTH);

        // --- ЦЕНТРАЛЬНАЯ ЧАСТЬ (Контент настроек) ---
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(60, 63, 65));
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(30, 30, 30)),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Пример текстового поля (информационного)
        addInfoRow(contentPanel, "Путь к серверу:", "/home/user/Servers/" + serverName);
        addInfoRow(contentPanel, "Версия ядра:", "Paper 1.21.1");
        addInfoRow(contentPanel, "Статус:", "Готов к запуску");

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        this.add(scrollPane, BorderLayout.CENTER);
    }

    private void addInfoRow(JPanel container, String labelText, String valueText) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(new EmptyBorder(5, 0, 5, 0));

        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(new Color(180, 180, 180));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel val = new JLabel(valueText);
        val.setForeground(Color.WHITE);
        val.setFont(new Font("Monospaced", Font.BOLD, 14));

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        container.add(row);
        container.add(Box.createRigidArea(new Dimension(0, 5))); // Отступ между строками
    }
}
