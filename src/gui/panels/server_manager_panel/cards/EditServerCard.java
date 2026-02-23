package gui.panels.server_manager_panel.cards;

import gui.panels.server_manager_panel.SMLogic;
import gui.share.Theme;
import gui.share.Utils;
import settings.ServerSettings;
import settings.ServerSettings.ServerCardSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.CompletableFuture;

public class EditServerCard extends JPanel {

    private final String serverName;
    private final SMLogic logic;
    private final ServerCardSettings card;

    // Поля редактирования
    private final JTextField displayNameField;
    private final JLabel versionField;
    private final JSpinner ramSpinner;
    private final JSpinner portSpinner;
    private final JTextField motdField;
    private final JTextArea logArea;
    private final JScrollPane logAreafinal;
    private final JTextField inputcommands;

    public EditServerCard(String serverName, SMLogic logic) {

        this.serverName = serverName;
        this.logic = logic;
        this.card = logic.reloadCard(serverName);   // загружаем актуальные настройки

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.BACKGROUND_MEDIUM);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // ===================== ЗАГОЛОВОК =====================
        JLabel title = new JLabel("Редактирование сервера");
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        add(title, BorderLayout.NORTH);

        // ===================== ФОРМА =====================
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Folder name (только для информации)
        addFormLabel(form, gbc, row++, "Папка сервера:", serverName);

        // Display Name
        displayNameField = new JTextField(card.getDisplayName(), 25);
        addFormRow(form, gbc, row++, "Отображаемое имя:", displayNameField);

        // Version
        versionField = new JLabel(card.getVersion());
        addFormRow(form, gbc, row++, "Версия:", versionField);
        versionField.setForeground(Color.WHITE);
        versionField.setFont(Theme.FONT_TITLE);

        // RAM
        ramSpinner = new JSpinner(new SpinnerNumberModel(card.getRamGB(), 1, 32, 1));
        addFormRow(form, gbc, row++, "ОЗУ (GB):", ramSpinner);

        // Port
        portSpinner = new JSpinner(new SpinnerNumberModel(card.getPort(), 1024, 65535, 1));
        addFormRow(form, gbc, row++, "Порт:", portSpinner);

        // MOTD
        motdField = new JTextField(card.getMotd(), 30);
        addFormRow(form, gbc, row++, "MOTD:", motdField);

        // LOG AREA
        logArea = new JTextArea();
        logAreafinal = new JScrollPane(logArea);
        addFormRow(form, gbc, row++, "Console", logAreafinal);
        CompletableFuture.runAsync(() -> logic.LoggingToConsole(card.getServerFolderName(), logArea));

        // INPUT COMMANDS
        inputcommands = new JTextField();
        inputcommands.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (!inputcommands.getText().isEmpty()) {
                        logic.SendToServer(card.getServerFolderName(), inputcommands.getText());
                        logArea.append(inputcommands.getText());
                        inputcommands.setText("");
                    } else {
                        System.out.println("Строка комманд пуста");
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        addFormRow(form, gbc, row++, "Input", inputcommands);

        add(form, BorderLayout.CENTER);

        // ===================== КНОПКИ =====================
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttons.setOpaque(false);

        JButton saveBtn = new JButton("Сохранить");
        Utils.stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> saveAndClose());

        JButton cancelBtn = new JButton("Отмена");
        Utils.stylePrimaryButton(cancelBtn);           // можно сделать серой, но для простоты
        cancelBtn.addActionListener(e -> ((JDialog) SwingUtilities.getWindowAncestor(this)).dispose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);

        add(buttons, BorderLayout.SOUTH);

    }

    private void addFormLabel(JPanel panel, GridBagConstraints gbc, int row, String labelText, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setForeground(Theme.TEXT_SECONDARY);
        panel.add(val, gbc);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(labelText);
        lbl.setForeground(Theme.TEXT_PRIMARY);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(field, gbc);
    }

    private void saveAndClose() {
        try {
            // Обновляем объект настроек
            card.setDisplayName(displayNameField.getText().trim());
            card.setVersion(versionField.getText().trim());
            card.setRamGB((Integer) ramSpinner.getValue());
            card.setPort((Integer) portSpinner.getValue());
            card.setMotd(motdField.getText().trim());

            // Сохраняем
            ServerSettings.getInstance().saveServerCardSettings(serverName, card);

            // Если сделал ss private — замени на:
            // ServerSettings.getInstance().saveServerCardSettings(serverName, card);

            JOptionPane.showMessageDialog(this, "Настройки сохранены!", "Успех", JOptionPane.INFORMATION_MESSAGE);

            // Закрываем диалог
            Window w = SwingUtilities.getWindowAncestor(this);
            if (w instanceof JDialog) w.dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ошибка сохранения:\n" + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}