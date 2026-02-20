package gui.panels.server_manager_panel;

import gui.panels.server_manager_panel.cards.ServerCard;
import gui.share.Theme;
import gui.share.Utils;
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
        createButton.addActionListener(e -> logic.createNewServer());

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