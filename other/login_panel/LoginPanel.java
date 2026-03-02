package gui.panels.login_panel;

import auth.AccountManager;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox agreeCheckBox;
    private JButton registerButton;

    public LoginPanel() {
        this.setLayout(new GridBagLayout());
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        JPanel emailPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel emailLabel = new JLabel("Email:");
        emailField = new JTextField(20);
        emailPanel.add(emailLabel);
        emailPanel.add(emailField);
        contentPanel.add(emailPanel);

        JPanel passwordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel passwordLabel = new JLabel("Пароль:");
        passwordField = new JPasswordField(20);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordField);
        contentPanel.add(passwordPanel);

        agreeCheckBox = new JCheckBox("Согласен с условиями пользовательского соглашения.");
        agreeCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(agreeCheckBox);
        JButton LoginButton = new JButton("Войти");
        LoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        LoginButton.addActionListener(e -> {
            loginUser();
        });
        contentPanel.add(LoginButton);

        this.add(contentPanel);
    }

    private void loginUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        boolean isAuthenticated = AccountManager.getInstance().login(email, password);

        if (isAuthenticated) {
            JOptionPane.showMessageDialog(this,
                    "Вы успешно вошли в систему.",
                    "Вход успешен!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Неверный email или пароль.",
                    "Ошибка входа",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
