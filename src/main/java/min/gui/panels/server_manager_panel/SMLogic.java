package min.gui.panels.server_manager_panel;

import min.Parser.Parser;
import min.gui.common.Theme;
import min.gui.panels.server_manager_panel.cards.EditServerCard;
import min.gui.panels.server_manager_panel.cards.ManageServerCard;
import min.gui.panels.server_manager_panel.cards.PluginsCard;
import min.gui.panels.server_manager_panel.cards.PluginsCard;
import min.gui.panels.server_manager_panel.cards.ServerCard;
import min.settings.ServerSettings;
import min.settings.ServerSettings.ServerCardSettings;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static min.Parser.Parser.ServerType.PAPER;

public class SMLogic {



    private final ConcurrentHashMap<String, Process> runningProcesses = new ConcurrentHashMap<>();
    private final Set<String> activeLogReaders = ConcurrentHashMap.newKeySet(); // ← защита от двойного запуска
    private final ConcurrentHashMap<String, StringBuilder> serverLogs = new ConcurrentHashMap<>(); // ← сохранение логов
    private final ConcurrentHashMap<String, PrintWriter> processInputs = new ConcurrentHashMap<>(); // ← главный фикс
    private final ConcurrentHashMap<String, List<JTextArea>> activeConsoles = new ConcurrentHashMap<>();
    private final SMPanel panel;
    private final ServerSettings ss = ServerSettings.getInstance();
    private final ConcurrentHashMap<String, List<JLabel>> statusLabelsMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<JButton>> toggleButtonsMap = new ConcurrentHashMap<>();

    private int serverCounter = 0;

    public SMLogic(SMPanel panel) {
        this.panel = panel;
    }

    public void registerServerUI(String serverName, JLabel statusLabel, JButton toggleButton) {
        statusLabelsMap.computeIfAbsent(serverName, k -> new CopyOnWriteArrayList<>()).add(statusLabel);
        toggleButtonsMap.computeIfAbsent(serverName, k -> new CopyOnWriteArrayList<>()).add(toggleButton);
    }

    public boolean isServerRunning(String serverName) {
        Process p = runningProcesses.get(serverName);
        return p != null && p.isAlive();
    }

    private void updateAllServerUI(String serverName, boolean isRunning) {
        String statusText = isRunning ? "Запущен" : "Остановлен";
        Color borderColor = isRunning ? Color.RED : Color.GREEN;
        String btnText = isRunning ? "Stop" : "Start";

        SwingUtilities.invokeLater(() -> {
            List<JLabel> labels = statusLabelsMap.get(serverName);
            if (labels != null) {
                labels.forEach(label -> {
                    label.setText(statusText);
                });
            }

            List<JButton> buttons = toggleButtonsMap.get(serverName);
            if (buttons != null) {
                buttons.forEach(btn -> {
                    btn.setText(btnText);
                    btn.setBorder(BorderFactory.createLineBorder(borderColor, 2));
                });
            }
        });
    }


    public void loadExistingServers() {
        File[] folders = ss.getServersPath().toFile().listFiles(File::isDirectory);
        if (folders != null) {
            for (File folder : folders) {
                String name = folder.getName();
                ServerCardSettings card = ss.loadServerCardSettings(name);
                panel.addServerCard(new ServerCard(card, this));
                updateServerCounter(name);
            }
        }
    }

    private void updateServerCounter(String folderName) {
        if (folderName.startsWith("server_")) {
            try {
                int num = Integer.parseInt(folderName.replace("server_", ""));
                serverCounter = Math.max(serverCounter, num + 1);
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public void createNewServer(String motd, String version, int ram) {
        String serverName = "server_" + serverCounter++;
        Path serverPath = ss.getServerPath(serverName);

        if (Files.exists(serverPath)) {
            panel.showMessage("Папка уже существует!");
            return;
        }

        try {
            Files.createDirectories(serverPath);

            ServerCardSettings card = new ServerCardSettings(serverName, version, ram, motd);
            ss.saveServerCardSettings(serverName, card);

            // Показываем карточку сразу
            panel.addServerCard(new ServerCard(card, this));

            // Асинхронная подготовка файлов
            CompletableFuture.runAsync(() -> initializeServer(serverPath, card, version))
                    .thenRun(() -> System.out.println("✅ Сервер " + serverName + " создан"));

        } catch (IOException e) {
            e.printStackTrace();
            panel.showMessage("Ошибка создания: " + e.getMessage());
        }
    }

    private void initializeServer(Path serverPath, ServerCardSettings card, String version) {
        try {
            downloadJar(serverPath, version);
            createEula(serverPath);
            createBasicProperties(serverPath, card);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void downloadJar(Path serverPath, String version) {
        try (InputStream in = new URL(Parser.getDownloadUrl(PAPER, version)).openStream()) {
            Path target = serverPath.resolve(ServerSettings.SERVER_JAR_NAME);
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createEula(Path serverPath) {
        try {
            Files.writeString(serverPath.resolve(ServerSettings.EULA_TXT), "eula=true");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createBasicProperties(Path serverPath, ServerCardSettings card) {
        Path prop = serverPath.resolve(ServerSettings.SERVER_PROPERTIES);
        Properties p = new Properties();
        p.setProperty("server-port", String.valueOf(card.getPort()));
        p.setProperty("motd", card.getMotd());
        p.setProperty("max-players", "20");
        p.setProperty("online-mode", "true");

        try (OutputStream os = Files.newOutputStream(prop)) {
            p.store(os, "Generated by launcher");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServerProcess(String serverName, ServerCardSettings card) {
        try {
            Path path = ss.getServerPath(serverName);
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

            ProcessBuilder pb = new ProcessBuilder(
                    java,
                    "-Xmx" + card.getRamGB() + "G",
                    "-Xms" + Math.max(1, card.getRamGB() / 2) + "G",
                    "-jar", ServerSettings.SERVER_JAR_NAME,
                    "-nogui"
            ).directory(path.toFile());

            pb.redirectErrorStream(true);

            Process proc = pb.start();
            runningProcesses.put(serverName, proc);
            serverLogs.putIfAbsent(serverName, new StringBuilder());

            // Постоянный PrintWriter для отправки команд
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8), true);
            processInputs.put(serverName, pw);
            startLogReader(serverName);

        } catch (Exception e) {
            e.printStackTrace();
            panel.showMessage("Ошибка запуска: " + e.getMessage());
        }
    }

    public void startstopbutton(JButton btn, String serverName, JLabel status, ServerCardSettings card) {
        if (!isServerRunning(serverName)) {
            // START
            status.setText("Запуск...");

            CompletableFuture.runAsync(() -> {
                startServerProcess(serverName, card);
                updateAllServerUI(serverName, true);
            });
        } else {
            // STOP
            status.setText("Выключение...");

            Process p = runningProcesses.get(serverName);
            PrintWriter writer = processInputs.get(serverName);
            if (writer != null) {
                writer.println("stop");
                writer.flush();
            }

            p.onExit().orTimeout(10, TimeUnit.SECONDS)
                    .thenRun(() -> {
                        finishStop(serverName);
                        updateAllServerUI(serverName, false);
                    })
                    .exceptionally(ex -> {
                        if (p != null && p.isAlive()) p.destroyForcibly();
                        finishStop(serverName);
                        updateAllServerUI(serverName, false);
                        return null;
                    });
        }
    }

    // === Полностью замени метод finishStop ===
    private void finishStop(String serverName) {
        runningProcesses.remove(serverName);
        activeLogReaders.remove(serverName);

        PrintWriter pw = processInputs.remove(serverName);
        if (pw != null) try { pw.close(); } catch (Exception ignored) {}
        activeConsoles.remove(serverName);
    }

    private void startLogReader(String serverName) {
        if (!activeLogReaders.add(serverName)) return;

        Process p = runningProcesses.get(serverName);
        if (p == null || !p.isAlive()) {
            activeLogReaders.remove(serverName);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line + "\n";

                    // Сохраняем в историю
                    serverLogs.computeIfAbsent(serverName, k -> new StringBuilder()).append(finalLine);

                    // Отправляем во ВСЕ открытые консоли этого сервера
                    List<JTextArea> consoles = activeConsoles.get(serverName);
                    if (consoles != null) {
                        SwingUtilities.invokeLater(() -> {
                            for (JTextArea ta : consoles) {
                                if (ta.isDisplayable()) {
                                    ta.append(finalLine);
                                    ta.setCaretPosition(ta.getDocument().getLength());
                                }
                            }
                        });
                    }
                }
            } catch (IOException ignored) {
            } finally {
                activeLogReaders.remove(serverName);
            }
        });
    }

    public void LoggingToConsole(String serverName, JTextArea textArea) {
        // Регистрируем консоль для живого обновления
        activeConsoles.computeIfAbsent(serverName, k -> new CopyOnWriteArrayList<>()).add(textArea);

        // Восстанавливаем историю логов
        StringBuilder history = serverLogs.computeIfAbsent(serverName, k -> new StringBuilder());
        String currentLog;
        synchronized (history) {
            currentLog = history.toString();
        }
        SwingUtilities.invokeLater(() -> {
            textArea.setText(currentLog);
            textArea.setCaretPosition(textArea.getDocument().getLength());
        });

        // Запускаем/продолжаем чтение логов
        startLogReader(serverName);
    }

    public void clearServerLog(String serverName) {
        serverLogs.put(serverName, new StringBuilder());
    }

    public void openEditServer(String serverName) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(panel);
        JDialog dlg = new JDialog(parent, "Редактирование — " + serverName, true);
        JTabbedPane tabbedPane2 = new JTabbedPane();
        tabbedPane2.addTab("Консоль", new ManageServerCard(serverName, this));
        tabbedPane2.addTab("Настройки", new EditServerCard(serverName, this));
        tabbedPane2.addTab("Плагины", new PluginsCard(serverName, this));
        dlg.add(tabbedPane2);
        dlg.pack();
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }

    // Для EditServerCard — чтобы обновить карточку после сохранения
    public ServerCardSettings reloadCard(String serverName) {
        return ss.loadServerCardSettings(serverName);
    }

    public void SendToServer(String serverName, String command) {
        PrintWriter writer = processInputs.get(serverName);
        if (writer != null) {
            writer.println(command);
            writer.flush();
        }
    }

    public Path getServerPath(String serverName) {
        return ss.getServerPath(serverName);
    }
}