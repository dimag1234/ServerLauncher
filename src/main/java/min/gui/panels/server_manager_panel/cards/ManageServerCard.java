package min.gui.panels.server_manager_panel.cards;

import min.gui.common.Theme;
import min.gui.common.Utils;
import min.gui.panels.server_manager_panel.SMLogic;
import min.settings.ServerSettings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ManageServerCard extends JPanel {
    private final String serverName;
    private final SMLogic logic;
    private final ServerSettings.ServerCardSettings card;

    private final JTextArea logArea = new JTextArea();
    private final JTextField inputcommands;

    private final JLabel statusLabel = new JLabel("Остановлен"); // Статус сервера
    private final JButton toggleBtn = new JButton("Start");

    public ManageServerCard(String serverName, SMLogic logic) {
        this.serverName = serverName;
        this.logic = logic;
        this.card = logic.reloadCard(serverName);

        setLayout(new BorderLayout(20, 20));
        setBackground(Theme.BACKGROUND_MEDIUM);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        if (logic.isServerRunning(serverName)) {
            toggleBtn.setText("Stop");
            toggleBtn.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
            statusLabel.setText("Запущен");
            statusLabel.setForeground(new Color(100, 255, 100));
        } else {
            toggleBtn.setText("Start");
            toggleBtn.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
            statusLabel.setText("Остановлен");
        }

        // Регистрация для синхронизации со всеми карточками
        logic.registerServerUI(serverName, statusLabel, toggleBtn);

        // --- ВЕРХНЯЯ ПАНЕЛЬ (Заголовок + Статус) ---
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        titlePanel.setOpaque(false);

        JLabel title = new JLabel(serverName.toUpperCase());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));

        titlePanel.add(title);
        titlePanel.add(statusLabel);
        header.add(titlePanel, BorderLayout.WEST);

        // --- ЦЕНТРАЛЬНАЯ ПАНЕЛЬ (Логи) ---
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);

        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        logArea.setBackground(new Color(20, 20, 20));
        logArea.setForeground(new Color(150, 255, 150));
        logArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50)));

        inputcommands = new JTextField();
        styleInputField(inputcommands);
        setupCommandInput();

        centerPanel.add(scroll, BorderLayout.CENTER);
        centerPanel.add(inputcommands, BorderLayout.SOUTH);

        // --- ПРАВАЯ ПАНЕЛЬ (Кнопки действий) ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(160, 0));

        // Основная кнопка управления (Start/Stop)
        styleActionBtn(toggleBtn, Color.GREEN);
        toggleBtn.addActionListener(e -> logic.startstopbutton(toggleBtn, serverName, statusLabel, card));

        JButton clearBtn = createSideButton("ОЧИСТИТЬ ЛОГ", new Color(100, 100, 100));
        clearBtn.addActionListener(e -> { logic.clearServerLog(serverName); logArea.setText(""); });

        sidebar.add(toggleBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(clearBtn);
        sidebar.add(Box.createVerticalGlue());

        // Сборка
        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(sidebar, BorderLayout.EAST);

        logic.LoggingToConsole(card.getServerFolderName(), logArea);
    }

    private void styleActionBtn(JButton btn, Color borderCol) {
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createLineBorder(borderCol, 2));
    }

    private void styleInputField(JTextField field) {
        field.setBackground(new Color(35, 35, 35));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.CYAN);
        field.setFont(new Font("Consolas", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    private JButton createSideButton(String text, Color color) {
        JButton btn = new JButton(text);
        styleActionBtn(btn, color);
        return btn;
    }

    private void setupCommandInput() {
        inputcommands.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String cmd = inputcommands.getText().trim();
                    if (!cmd.isEmpty()) {
                        logic.SendToServer(card.getServerFolderName(), cmd);
                        logArea.append("> " + cmd + "\n");
                        inputcommands.setText("");
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    }
                }
            }
        });
    }
}
