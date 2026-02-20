package gui.panels.server_manager_panel;

import gui.panels.server_manager_panel.cards.EditServerCard;
import gui.panels.server_manager_panel.cards.ServerCard;
import settings.ServerSettings;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SMLogic {

    private static final String SERVER_JAR_URL = "https://fill-data.papermc.io/v1/objects/e708e8c132dc143ffd73528cccb9532e2eb17628b1a0eee74469bf466c7003f8/paper-1.21.11-116.jar";
    private static final String SERVER_JAR_NAME = "server.jar";
    private final ConcurrentHashMap<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final SMPanel panel;
    private boolean isPressedStartButton = false;
    private int serverCounter = 0;

    public SMLogic(SMPanel panel) {
        this.panel = panel;
        createServersDirectoryIfNeeded();
    }

    private void createServersDirectoryIfNeeded() {
        try {
            Files.createDirectories(ServerSettings.getInstance().getServersPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadExistingServers() {
        File[] folders = ServerSettings.getInstance().getServersPath().toFile().listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                String folderName = folder.getName();
                panel.addServerCard(new ServerCard(folderName, "Остановлен", this));
                updateServerCounter(folderName);
            }
        }
    }

    private void updateServerCounter(String folderName) {
        if (folderName.startsWith("server_")) {
            try {
                int num = Integer.parseInt(folderName.replace("server_", ""));
                if (num >= serverCounter) {
                    serverCounter = num + 1;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void createNewServer() {
        String serverName = "server_" + serverCounter;
        Path newServerPath = ServerSettings.getInstance().getServersPath().resolve(serverName);

        if (Files.exists(newServerPath)) {
            panel.showMessage("Папка уже существует!");
            return;
        }

        try {
            Files.createDirectories(newServerPath);
            CompletableFuture.runAsync(() -> downloadServerJar(newServerPath))
                    .thenRun(() -> startServerProcess(serverName, newServerPath))
                    .thenRun(() -> SwingUtilities.invokeLater(() -> {
                        panel.addServerCard(new ServerCard(serverName, "Остановлен", this));
                        serverCounter++;
                    }));
        } catch (IOException e) {
            e.printStackTrace();
            panel.showMessage("Ошибка при создании сервера: " + e.getMessage());
        }
    }

    private void downloadServerJar(Path serverPath) {
        try {
            Path targetPath = serverPath.resolve(SERVER_JAR_NAME);
            try (InputStream in = new URL(SERVER_JAR_URL).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Файл скачан: " + targetPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServerProcess(String serverName, Path serverPath) {
        try {
            String javaPath = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            ProcessBuilder pb = new ProcessBuilder(javaPath, "-Xmx2G", "-jar", SERVER_JAR_NAME, "-nogui")
                    .directory(serverPath.toFile());
//                    .inheritIO();

            Process process = pb.start();
            runningProcesses.put(serverName, process); // Сохраняем процесс

            int exitCode = process.waitFor();
            runningProcesses.remove(serverName); // Удаляем после выхода
            System.out.println("Сервер " + serverName + " остановлен. Код: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void openEditServer(String serverName) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        JDialog dialog = new JDialog(parentFrame, "Редактирование сервера", true);

        dialog.add(new EditServerCard(serverName));
        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
    }

    public void startstopbutton(JButton button, String serverName, JLabel status) {
        Path serverPath = ServerSettings.getInstance().getServersPath().resolve(serverName);

        // Проверяем, запущен ли сервер (вместо одной переменной isPressedStartButton)
        if (!runningProcesses.containsKey(serverName)) {
            // ЛОГИКА ЗАПУСКА
            button.setText("Stop");
            button.setBorder(BorderFactory.createLineBorder(Color.RED, 3));
            status.setText("Запуск...");

            CompletableFuture.runAsync(() -> startServerProcess(serverName, serverPath))
                    .thenRun(() -> SwingUtilities.invokeLater(() -> {
                        status.setText("Остановлен");
                        button.setText("Start");
                        button.setBorder(BorderFactory.createLineBorder(Color.GREEN, 3));
                    }));
        } else {
            // ЛОГИКА ОСТАНОВКИ
            Process process = runningProcesses.get(serverName);
            if (process != null && process.isAlive()) {
                try {
                    // Отправляем команду stop в стандартный ввод процесса
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    writer.write("stop");
                    writer.newLine();
                    writer.flush();
                    status.setText("Выключение...");
                } catch (IOException e) {
                    e.printStackTrace();
                    process.destroy(); // Если не получилось по-хорошему, закрываем принудительно
                }
            }
        }
    }

}