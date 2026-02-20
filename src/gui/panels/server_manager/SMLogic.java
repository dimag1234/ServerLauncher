package gui.panels.server_manager;

import gui.panels.server_manager.cards.EditServerCard;
import gui.panels.server_manager.cards.ServerCard;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public class SMLogic {

    private static final Path SERVERS_PATH = Paths.get(System.getProperty("user.home"), "Servers");
    private static final String SERVER_JAR_URL = "https://fill-data.papermc.io/v1/objects/e708e8c132dc143ffd73528cccb9532e2eb17628b1a0eee74469bf466c7003f8/paper-1.21.11-116.jar";
    private static final String SERVER_JAR_NAME = "server.jar";

    private final SMPanel panel;
    private int serverCounter = 0;

    public SMLogic(SMPanel panel) {
        this.panel = panel;
        createServersDirectoryIfNeeded();
    }

    private void createServersDirectoryIfNeeded() {
        try {
            Files.createDirectories(SERVERS_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadExistingServers() {
        File[] folders = SERVERS_PATH.toFile().listFiles(File::isDirectory);
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
            } catch (NumberFormatException ignored) {}
        }
    }

    public void createNewServer() {
        String serverName = "server_" + serverCounter;
        Path newServerPath = SERVERS_PATH.resolve(serverName);

        if (Files.exists(newServerPath)) {
            panel.showMessage("Папка уже существует!");
            return;
        }

        try {
            Files.createDirectories(newServerPath);
            CompletableFuture.runAsync(() -> downloadServerJar(newServerPath))
                    .thenRun(() -> startServerProcess(newServerPath))
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

    private void startServerProcess(Path serverPath) {
        try {
            String javaHome = System.getProperty("java.home");
            String javaPath = javaHome + File.separator + "bin" + File.separator + "java";

            ProcessBuilder pb = new ProcessBuilder(javaPath, "-jar", SERVER_JAR_NAME)
                    .directory(serverPath.toFile())
                    .inheritIO();

            Process process = pb.start();
            int exitCode = process.waitFor();
            System.out.println("Процесс завершен с кодом: " + exitCode);

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
}