package gui.panels.registration_panel;

import auth.AccountManager;

import javax.swing.*;
import java.awt.*;

public class RegistrationPanel extends JPanel {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox agreeCheckBox;
    private JButton registerButton;
    private JComboBox<String> genderComboBox;

    public RegistrationPanel() {
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

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel genderLabel = new JLabel("Пол:");
        String[] genders = {"", "Мужской", "Женский"}; // Массив элементов для выбора
        genderComboBox = new JComboBox<>(genders); // Передаём массив параметром
        genderComboBox.setSelectedIndex(0); // Устанавливаем значение по умолчанию
        genderPanel.add(genderLabel);
        genderPanel.add(genderComboBox);
        contentPanel.add(genderPanel);

        agreeCheckBox = new JCheckBox("Согласен с условиями пользовательского соглашения.");
        agreeCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(agreeCheckBox);
        registerButton = new JButton("Зарегистрироваться");
        registerButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerButton.addActionListener(e -> {
            registerUser();
        });
        contentPanel.add(registerButton);

        this.add(contentPanel);
    }

    private void registerUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String gender = (String) genderComboBox.getSelectedItem();

        // Цепочка проверок (Guard Clauses)
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email и пароль не могут быть пустыми");
            return;
        }

        if (gender == null || gender.isEmpty()) {
            showError("Выберите пол");
            return;
        }

        if (!agreeCheckBox.isSelected()) {
            showError("Необходимо согласиться с условиями");
            return;
        }

        if (AccountManager.getInstance().register(email, password, gender)) {
            JOptionPane.showMessageDialog(this, "Регистрация завершена!", "Успех", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
        } else {
            showError("Пользователь с таким Email уже зарегистрирован");
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Ошибка валидации", JOptionPane.WARNING_MESSAGE);
    }

    private void clearFields() {
        emailField.setText("");
        passwordField.setText("");
        genderComboBox.setSelectedIndex(0);
        agreeCheckBox.setSelected(false);
    }


}
