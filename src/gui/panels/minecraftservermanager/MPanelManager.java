package gui.panels.minecraftservermanager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MPanelManager extends JPanel {

    private final JPanel serverListPanel; // Панель, куда будут добавляться карточки серверов
    private int servernumber = 0;

    public MPanelManager() {
        // Темная тема и аккуратные отступы
        this.setLayout(new BorderLayout(10, 10));
        this.setBackground(new Color(45, 45, 45));
        this.setBorder(new EmptyBorder(15, 15, 15, 15));

        Path directoryPath = Paths.get("/home/user/Servers/");
        String fileName = "server.jar";

        // --- ВЕРХНЯЯ ПАНЕЛЬ (Заголовок и Кнопка) ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Менеджер серверов");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        headerPanel.add(title, BorderLayout.WEST);

        JButton createServerBtn = new JButton("+ Создать сервер");
        stylePrimaryButton(createServerBtn);
        headerPanel.add(createServerBtn, BorderLayout.EAST);

        this.add(headerPanel, BorderLayout.NORTH);

        // --- ЦЕНТРАЛЬНАЯ ЧАСТЬ (Список серверов с прокруткой) ---
        serverListPanel = new JPanel();
        serverListPanel.setLayout(new BoxLayout(serverListPanel, BoxLayout.Y_AXIS));
        serverListPanel.setBackground(new Color(60, 63, 65));

        JScrollPane scrollPane = new JScrollPane(serverListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(30, 30, 30)));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Плавная прокрутка
        this.add(scrollPane, BorderLayout.CENTER);

        // --- ЛОГИКА СОЗДАНИЯ ---
        createServerBtn.addActionListener(ev -> {
            String serverName = "server_" + servernumber;
            try {
                Path newServerPath = directoryPath.resolve(serverName);
                if (!Files.exists(newServerPath)) {
                    Files.createDirectories(newServerPath);
                    URL url = new URL("https://fill-data.papermc.io/v1/objects/e708e8c132dc143ffd73528cccb9532e2eb17628b1a0eee74469bf466c7003f8/paper-1.21.11-116.jar");
                    try (InputStream in = url.openStream()) {
                        // 3. Создаем путь для сохранения
                        Path targetPath = Paths.get(newServerPath.toString(), fileName);

                        // 4. Копируем файл
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println("Файл успешно скачан: " + targetPath.toString());
                    }
                    try {
                        // Указываем команду и путь к JAR
                        ProcessBuilder pb = new ProcessBuilder("java", "-jar", newServerPath + "/" + fileName);

                        // (Опционально) Устанавливаем рабочую директорию
                        pb.directory(new File(newServerPath.toString()));

                        // (Опционально) Перенаправляем вывод в консоль текущего приложения
                        pb.inheritIO();

                        // Запускаем процесс
                        Process process = pb.start();

                        // (Опционально) Ждем завершения
                        int exitCode = process.waitFor();
                        System.out.println("Процесс завершен с кодом: " + exitCode);

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    addServerCard(serverName, "Остановлен");
                    servernumber++;
                } else {
                    JOptionPane.showMessageDialog(this, "Папка уже существует!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // --- СИНХРОНИЗАЦИЯ СУЩЕСТВУЮЩИХ СЕРВЕРОВ ---
        syncExistingServers(directoryPath);

    }

    // Метод для добавления "карточки" сервера в список
    private void addServerCard(String name, String status) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        card.setBackground(new Color(70, 73, 75));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 40)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        JLabel nameLbl = new JLabel(name);
        nameLbl.setForeground(Color.WHITE);
        nameLbl.setFont(new Font("Monospaced", Font.BOLD, 14));

        JButton edit_server = new JButton("Edit");
        edit_server.setForeground(Color.WHITE);
        edit_server.setFont(new Font("Monospaced", Font.BOLD, 14));
        edit_server.setBackground(new Color(70, 73, 75));


        JLabel statusLbl = new JLabel(status);
        statusLbl.setForeground(new Color(200, 200, 200));

        card.add(nameLbl, BorderLayout.WEST);
        card.add(edit_server, BorderLayout.CENTER);
        card.add(statusLbl, BorderLayout.EAST);

        serverListPanel.add(card);
        serverListPanel.revalidate(); // Обновить интерфейс
        serverListPanel.repaint();


    }

    // Стилизация кнопки
    private void stylePrimaryButton(JButton btn) {
        btn.setBackground(new Color(70, 140, 70));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 15, 8, 15));
    }

    private void syncExistingServers(Path directoryPath) {
        File root = directoryPath.toFile();
        if (!root.exists() || !root.isDirectory()) return;

        File[] folders = root.listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                String folderName = folder.getName();

                // 1. Отрисовываем карточку (используем ваш метод для сохранения дизайна)
                addServerCard(folderName, "Остановлен");

                // 2. Обновляем счетчик servernumber, чтобы не было дубликатов имен
                if (folderName.startsWith("server_")) {
                    try {
                        int num = Integer.parseInt(folderName.replace("server_", ""));
                        if (num >= servernumber) {
                            servernumber = num + 1;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }


}