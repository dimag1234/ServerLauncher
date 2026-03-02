package gui.common;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class Utils {

    public static void stylePrimaryButton(JButton button) {
        button.setBackground(Theme.BUTTON_PRIMARY);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setFocusPainted(false);
        button.setFont(Theme.FONT_REGULAR);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(Theme.PADDING_SMALL, Theme.PADDING_MEDIUM,
                Theme.PADDING_SMALL, Theme.PADDING_MEDIUM));
    }

    public static Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_DARK),
                new EmptyBorder(Theme.PADDING_MEDIUM, Theme.PADDING_MEDIUM,
                        Theme.PADDING_MEDIUM, Theme.PADDING_MEDIUM)
        );
    }

    public static Border createContentBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_DARK),
                new EmptyBorder(Theme.PADDING_MEDIUM, Theme.PADDING_MEDIUM,
                        Theme.PADDING_MEDIUM, Theme.PADDING_MEDIUM)
        );
    }

    public static JScrollPane createScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_DARK));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }
}