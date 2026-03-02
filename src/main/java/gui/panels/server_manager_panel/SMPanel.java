package gui.panels.server_manager_panel;

import gui.common.Theme;
import gui.common.Utils;
import gui.panels.server_manager_panel.cards.ServerCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SMPanel extends JPanel {

    private final JPanel serverListPanel;
    private final SMLogic logic;

    public SMPanel() {
        this.logic = new SMLogic(this);

        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.BACKGROUND_DARK);
        setBorder(new EmptyBorder(
                Theme.PADDING_MEDIUM,
                Theme.PADDING_MEDIUM,
                Theme.PADDING_MEDIUM,
                Theme.PADDING_MEDIUM));

        add(createHeaderPanel(), BorderLayout.NORTH);

        serverListPanel = createServerListPanel();
        add(Utils.createScrollPane(serverListPanel), BorderLayout.CENTER);

        logic.loadExistingServers();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Доступные сервера");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JButton createButton = new JButton("+ Создать сервер");
        Utils.stylePrimaryButton(createButton);
        createButton.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this); // this — это твоя панель

            JDialog dlg = new JDialog(parent, "Создание нового сервера", true);
            dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dlg.getContentPane().setBackground(Theme.BACKGROUND_MEDIUM);
            dlg.setLayout(new BorderLayout(0, 20));
            dlg.setPreferredSize(new Dimension(520, 420));
            dlg.setResizable(false);

            // ====================== КРАСИВЫЙ ЗАГОЛОВОК ======================
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(25, 0, 10, 0));

            JLabel titled = new JLabel("Новый Minecraft сервер");
            titled.setFont(Theme.FONT_TITLE.deriveFont(26f));
            titled.setForeground(Theme.TEXT_PRIMARY);
            titled.setHorizontalAlignment(SwingConstants.CENTER);
            header.add(titled, BorderLayout.CENTER);
            dlg.add(header, BorderLayout.NORTH);

            // ====================== ФОРМА ======================
            JPanel form = new JPanel(new GridBagLayout());
            form.setOpaque(false);
            form.setBorder(new EmptyBorder(10, 40, 20, 40));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 0, 8, 0);
            gbc.weightx = 1.0;

            // Название сервера
            JLabel nameLabel = new JLabel("Motd сервера");
            nameLabel.setForeground(Theme.TEXT_PRIMARY);
            nameLabel.setFont(Theme.FONT_MONOSPACE.deriveFont(Font.BOLD, 13f));
            form.add(nameLabel, gbc);

            gbc.gridy++;
            JTextField nameField = new JTextField("My Awesome Server", 30);
            nameField.setFont(Theme.FONT_MONOSPACE);
            nameField.setPreferredSize(new Dimension(0, 42));
            form.add(nameField, gbc);

            // Выбор версии
            gbc.gridy++;
            JLabel versionLabel = new JLabel("Версия");
            versionLabel.setForeground(Theme.TEXT_PRIMARY);
            versionLabel.setFont(Theme.FONT_MONOSPACE.deriveFont(Font.BOLD, 13f));
            form.add(versionLabel, gbc);

            gbc.gridy++;
            JComboBox<String> versionBox = new JComboBox<>(new String[]{
                    "Paper 1.21.11", "Test"
            });
            versionBox.setFont(Theme.FONT_MONOSPACE);
            versionBox.setPreferredSize(new Dimension(0, 42));
            versionBox.setSelectedIndex(0);
            form.add(versionBox, gbc);

            // ОЗУ
            gbc.gridy++;
            JLabel ramLabel = new JLabel("Оперативная память (GB)");
            ramLabel.setForeground(Theme.TEXT_PRIMARY);
            ramLabel.setFont(Theme.FONT_MONOSPACE.deriveFont(Font.BOLD, 13f));
            form.add(ramLabel, gbc);

            gbc.gridy++;
            JSpinner ramSpinner = new JSpinner(new SpinnerNumberModel(4, 2, 16, 1));
            ramSpinner.setFont(Theme.FONT_MONOSPACE);
            ramSpinner.setPreferredSize(new Dimension(0, 42));
            form.add(ramSpinner, gbc);

            dlg.add(form, BorderLayout.CENTER);

            // ====================== КНОПКИ ======================
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            buttons.setOpaque(false);
            buttons.setBorder(new EmptyBorder(0, 0, 30, 0));

            JButton cancelBtn = new JButton("Отмена");
            Utils.stylePrimaryButton(cancelBtn);
            cancelBtn.setPreferredSize(new Dimension(130, 48));

            JButton createBtn = new JButton("Создать сервер");
            Utils.stylePrimaryButton(createBtn);
            createBtn.setPreferredSize(new Dimension(180, 48));
            createBtn.setBackground(new Color(0, 180, 80)); // красивый зелёный
            createBtn.setForeground(Color.WHITE);

            buttons.add(cancelBtn);
            buttons.add(createBtn);
            dlg.add(buttons, BorderLayout.SOUTH);

            // ====================== ДЕЙСТВИЯ ======================
            createBtn.addActionListener(eg2 -> {
                String serverName = nameField.getText().trim(); // ЭТО MOTD
                if (serverName.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Введите название сервера!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                logic.createNewServer(serverName, versionBox.getSelectedItem().toString(), (Integer) ramSpinner.getValue());


                dlg.dispose();
            });

            cancelBtn.addActionListener(eg -> dlg.dispose());

            dlg.pack();
            dlg.setLocationRelativeTo(parent);
            dlg.setVisible(true);
        });

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(createButton, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createServerListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BACKGROUND_MEDIUM);
        return panel;
    }

    public void addServerCard(ServerCard card) {
        serverListPanel.add(card);
        serverListPanel.revalidate();
        serverListPanel.repaint();
    }

    public void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}