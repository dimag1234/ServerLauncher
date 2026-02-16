package utilities;

import javax.swing.*;
import java.awt.*;

public class ComponentStyler {
    private static final Dimension DEFAULT_BUTTON_SIZE = new Dimension(220, 40);
    private static final Dimension DEFAULT_LABEL_SIZE = new Dimension(150, 30);
    private static final Dimension DEFAULT_TEXTFIELD_SIZE = new Dimension(200, 30);
    private static final Dimension DEFAULT_TEXTAREA_SIZE = new Dimension(300, 100);
    private static final Dimension DEFAULT_COMBOBOX_SIZE = new Dimension(200, 40);
    private static final Dimension DEFAULT_SPINNER_SIZE = new Dimension(100, 30);
    private static final Dimension DEFAULT_SLIDER_SIZE = new Dimension(200, 40);
    private static Font DEFAULT_FONT = new Font("Segoe UI", Font.PLAIN, 26);
    private static Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static Color SECONDARY_COLOR = new Color(52, 152, 219);

    /**
     * Устанавливает стандартные стили для компонента в зависимости от его типа
     */
    public static void styleComponent(Component component) {
        if (component == null) return;

        component.setFont(DEFAULT_FONT);

        switch (component) {
            case JTextField jTextField -> styleTextField(jTextField);
            case JButton jButton -> styleButton(jButton);
            case JLabel jLabel -> styleLabel(jLabel);
            case JComboBox jComboBox -> styleComboBox(jComboBox);
            case JTextArea jTextArea -> styleTextArea(jTextArea);
            case JList jList -> styleList(jList);
            case JCheckBox checkBox -> styleCheckBox(checkBox);
            case JRadioButton jRadioButton -> styleRadioButton(jRadioButton);
            case JToggleButton jToggleButton -> styleToggleButton(jToggleButton);
            case JSpinner jSpinner -> styleSpinner(jSpinner);
            case JSlider jSlider -> styleSlider(jSlider);
            case JProgressBar jProgressBar -> styleProgressBar(jProgressBar);
            case JTable jTable -> styleTable(jTable);
            case JTree jTree -> styleTree(jTree);
            case JTabbedPane jTabbedPane -> styleTabbedPane(jTabbedPane);
            case JScrollPane jScrollPane -> styleScrollPane(jScrollPane);
            case JPanel jPanel -> stylePanel(jPanel);
            default -> {
            }
        }
    }

    private static void styleTextField(JTextField textField) {
        if (DEFAULT_TEXTFIELD_SIZE != null) {
            textField.setPreferredSize(DEFAULT_TEXTFIELD_SIZE);
            textField.setMaximumSize(DEFAULT_TEXTFIELD_SIZE);
        }
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
    }

    private static void styleButton(JButton button) {
        if (DEFAULT_BUTTON_SIZE != null) {
//            button.setPreferredSize(DEFAULT_BUTTON_SIZE);
//            button.setMaximumSize(DEFAULT_BUTTON_SIZE);
        }
        button.setFocusPainted(false);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
            }
        });
    }

    private static void styleLabel(JLabel label) {
        if (DEFAULT_LABEL_SIZE != null) {
            label.setPreferredSize(DEFAULT_LABEL_SIZE);
            label.setMaximumSize(DEFAULT_LABEL_SIZE);
        }
    }

    private static void styleComboBox(JComboBox<?> comboBox) {
        if (DEFAULT_COMBOBOX_SIZE != null) {
            comboBox.setPreferredSize(DEFAULT_COMBOBOX_SIZE);
            comboBox.setMaximumSize(DEFAULT_COMBOBOX_SIZE);
        }
        comboBox.setBackground(Color.WHITE);
    }

    private static void styleTextArea(JTextArea textArea) {
        if (DEFAULT_TEXTAREA_SIZE != null) {
            textArea.setPreferredSize(DEFAULT_TEXTAREA_SIZE);
        }
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    }

    private static void styleList(JList<?> list) {
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(5);
        list.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        list.setBackground(new Color(250, 250, 250));
    }

    private static void styleCheckBox(JCheckBox checkBox) {
        checkBox.setFocusPainted(false);
        checkBox.setBackground(null);
    }

    private static void styleRadioButton(JRadioButton radioButton) {
        radioButton.setFocusPainted(false);
        radioButton.setBackground(null);
    }

    private static void styleToggleButton(JToggleButton toggleButton) {
        toggleButton.setFocusPainted(false);
        toggleButton.setBackground(new Color(240, 240, 240));
        toggleButton.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    }

    private static void styleSpinner(JSpinner spinner) {
        if (DEFAULT_SPINNER_SIZE != null) {
            spinner.setPreferredSize(DEFAULT_SPINNER_SIZE);
            spinner.setMaximumSize(DEFAULT_SPINNER_SIZE);
        }
    }

    private static void styleSlider(JSlider slider) {
        if (DEFAULT_SLIDER_SIZE != null) {
            slider.setPreferredSize(DEFAULT_SLIDER_SIZE);
        }
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
    }

    private static void styleProgressBar(JProgressBar progressBar) {
        progressBar.setStringPainted(true);
        progressBar.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(25);
        table.getTableHeader().setFont(TITLE_FONT.deriveFont(Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(220, 235, 247));
    }

    private static void styleTree(JTree tree) {
        tree.setFont(DEFAULT_FONT);
        tree.setRowHeight(25);
    }

    private static void styleTabbedPane(JTabbedPane tabbedPane) {
        tabbedPane.setFont(TITLE_FONT.deriveFont(Font.PLAIN, 24));
        tabbedPane.setBackground(Color.WHITE);
    }

    private static void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    private static void stylePanel(JPanel panel) {
        panel.setBackground(Color.WHITE);
    }

    public static void styleComponents(Component... components) {
        for (Component component : components) {
            styleComponent(component);
        }
    }

    public static void styleContainer(Container container) {
        styleComponent(container);
        for (Component component : container.getComponents()) {
            styleComponent(component);
            if (component instanceof Container) {
                styleContainer((Container) component);
            }
        }
    }

    public static void setDefaultFont(Font font) {
        DEFAULT_FONT = new Font(font.getName(), font.getStyle(), font.getSize());
    }

    public static void setTitleFont(Font font) {
        TITLE_FONT = new Font(font.getName(), font.getStyle(), font.getSize());
    }

    public static void setPrimaryColor(Color color) {
        PRIMARY_COLOR = color;
    }

    public static void setSecondaryColor(Color color) {
        SECONDARY_COLOR = color;
    }

    public static void setFontSize(int size) {
        DEFAULT_FONT = DEFAULT_FONT.deriveFont((float) size);
    }
}
