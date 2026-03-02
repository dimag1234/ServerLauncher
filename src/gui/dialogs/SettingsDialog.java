package gui.dialogs;

import gui.common.Theme;
import gui.common.Utils;
import logging.ILogger;
import logging.Loggers;
import settings.AppSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SettingsDialog extends JDialog {

    private static final ILogger logger = Loggers.get(SettingsDialog.class);
    private final AppSettings settings = AppSettings.getInstance();

    private JComboBox<String> fontFamilyBox;
    private JSpinner fontSizeSpinner;
    private JComboBox<String> fontStyleBox;
    private JComboBox<String> themeBox;

    public SettingsDialog(JFrame parent) {
        super(parent, "Настройки", true);
        logger.info("Opening settings dialog");
        initComponents();
        loadSettings();
        pack();
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleClose();
            }
        });

        logger.debug("Settings dialog initialized with size: %sx%s", getWidth(), getHeight());
    }

    private void initComponents() {
        logger.debug("Initializing settings dialog components");
        setLayout(new BorderLayout(10, 10));

        getContentPane().setBackground(Theme.BACKGROUND_MEDIUM);
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        JLabel fontTitle = new JLabel("Шрифт");
        fontTitle.setFont(Theme.FONT_TITLE);
        fontTitle.setForeground(Theme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(fontTitle, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(createLabel("Шрифт:"), gbc);

        gbc.gridx = 1;
        fontFamilyBox = new JComboBox<>(GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames());
        fontFamilyBox.setPreferredSize(new Dimension(200, 30));
        panel.add(fontFamilyBox, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(createLabel("Размер:"), gbc);

        gbc.gridx = 1;
        fontSizeSpinner = new JSpinner(new SpinnerNumberModel(14, 8, 72, 1));
        fontSizeSpinner.setPreferredSize(new Dimension(80, 30));

        JComponent editor = fontSizeSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JFormattedTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            textField.setFont(settings.getFont());
            textField.setHorizontalAlignment(JTextField.CENTER);
        }

        panel.add(fontSizeSpinner, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        panel.add(createLabel("Стиль:"), gbc);

        gbc.gridx = 1;
        fontStyleBox = new JComboBox<>(new String[]{"Обычный", "Жирный", "Курсив", "Жирный курсив"});
        fontStyleBox.setPreferredSize(new Dimension(150, 30));
        panel.add(fontStyleBox, gbc);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 5, 5, 5);
        panel.add(createLabel("Тема:"), gbc);

        gbc.gridx = 1;
        themeBox = new JComboBox<>(new String[]{"Тёмная", "Светлая"});
        themeBox.setPreferredSize(new Dimension(150, 30));
        panel.add(themeBox, gbc);

        add(panel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttons.setOpaque(false);

        JButton saveBtn = new JButton("Сохранить");
        Utils.stylePrimaryButton(saveBtn);
        saveBtn.addActionListener(e -> saveAndClose());

        JButton cancelBtn = new JButton("Отмена");
        Utils.stylePrimaryButton(cancelBtn);
        cancelBtn.addActionListener(e -> handleClose());

        buttons.add(saveBtn);
        buttons.add(cancelBtn);
        add(buttons, BorderLayout.SOUTH);
        logger.debug("Settings dialog components initialized");
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.TEXT_PRIMARY);
        return label;
    }

    private void loadSettings() {
        logger.debug("Loading current settings into dialog");

        Font currentFont = settings.getFont();

        fontFamilyBox.setSelectedItem(currentFont.getFamily());
        fontSizeSpinner.setValue(currentFont.getSize());

        int style = currentFont.getStyle();
        int selectedStyle = style == Font.PLAIN ? 0 :
                style == Font.BOLD ? 1 :
                        style == Font.ITALIC ? 2 : 3;
        fontStyleBox.setSelectedIndex(selectedStyle);
        logger.debug("Font style index: %s", selectedStyle);

        String theme = settings.getTheme();
        int themeIndex = theme.equals("DARK") ? 0 : 1;
        themeBox.setSelectedIndex(themeIndex);
        logger.debug("Theme index: %s (theme: %s)", themeIndex, theme);
    }

    private void saveAndClose() {
        logger.info("Saving settings");

        String family = (String) fontFamilyBox.getSelectedItem();
        int size = (Integer) fontSizeSpinner.getValue();
        int style = fontStyleBox.getSelectedIndex();
        int fontStyle = style == 0 ? Font.PLAIN :
                style == 1 ? Font.BOLD :
                        style == 2 ? Font.ITALIC : Font.BOLD | Font.ITALIC;

        logger.debug("New font settings - family: %s, size: %s, style: %s", family, size, style);
        settings.setFont(new Font(family, fontStyle, size));

        String theme = themeBox.getSelectedIndex() == 0 ? "DARK" : "LIGHT";
        logger.debug("New theme: %s", theme);
        settings.setTheme(theme);

        logger.info("Settings saved successfully");
        JOptionPane.showMessageDialog(this,
                "Настройки сохранены в config/app.properties\nИзменения будут применены при следующем запуске приложения.",
                "Успех",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
    }

    private void handleClose() {
        logger.info("Settings dialog cancelled");
        dispose();
    }
}
