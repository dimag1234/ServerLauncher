package org.min.gui.panels.server_manager_panel;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.min.Parser.Parser;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.cards.*;
import org.min.settings.ServerSettings;
import org.min.settings.ServerSettings.ServerCardSettings;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static org.min.Parser.Parser.ServerType.PAPER;

public class SMLogic {

    // ── Running state ─────────────────────────────────────────
    private final ConcurrentHashMap<String, Process>       runningProcesses = new ConcurrentHashMap<>();
    private final Set<String>                               activeLogReaders = ConcurrentHashMap.newKeySet();
    private final ConcurrentHashMap<String, StringBuilder>  serverLogs       = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PrintWriter>    processInputs    = new ConcurrentHashMap<>();

    // ── UI registries (JavaFX controls, not Swing) ────────────
    private final ConcurrentHashMap<String, List<TextArea>> activeConsoles   = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Label>>    statusLabelsMap  = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Button>>   toggleButtonsMap = new ConcurrentHashMap<>();

    private final SMPanel       panel;
    private final ServerSettings ss    = ServerSettings.getInstance();
    private int serverCounter = 0;

    public SMLogic(SMPanel panel) { this.panel = panel; }

    // ── UI registration ───────────────────────────────────────

    /** Called by every card that wants to reflect start/stop state. */
    public void registerServerUI(String name, Label statusLabel, Button toggleButton) {
        statusLabelsMap .computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(statusLabel);
        toggleButtonsMap.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>()).add(toggleButton);
    }

    // ── State queries ─────────────────────────────────────────
    public boolean isServerRunning(String name) {
        Process p = runningProcesses.get(name);
        return p != null && p.isAlive();
    }

    // ── Sync all registered UI for a server ───────────────────
    private void updateAllServerUI(String name, boolean isRunning) {
        Platform.runLater(() -> {
            List<Label> labels = statusLabelsMap.get(name);
            if (labels != null) labels.forEach(l -> {
                if (isRunning) Theme.applyRunning(l); else Theme.applyStopped(l);
                l.setText(isRunning ? "Запущен" : "Остановлен");
            });

            List<Button> buttons = toggleButtonsMap.get(name);
            if (buttons != null) buttons.forEach(b -> {
                b.setText(isRunning ? "Stop" : "Start");
                if (isRunning) Theme.applyStop(b); else Theme.applyStart(b);
            });
        });
    }

    // ── Load existing servers from disk ───────────────────────
    public void loadExistingServers() {
        File[] folders = ss.getServersPath().toFile().listFiles(File::isDirectory);
        if (folders == null) return;
        for (File folder : folders) {
            String name = folder.getName();
            ServerCardSettings card = ss.loadServerCardSettings(name);
            panel.addServerCard(new ServerCard(card, this));
            updateServerCounter(name);
        }
    }

    private void updateServerCounter(String folderName) {
        if (folderName.startsWith("server_")) {
            try {
                int num = Integer.parseInt(folderName.replace("server_", ""));
                serverCounter = Math.max(serverCounter, num + 1);
            } catch (NumberFormatException ignored) {}
        }
    }

    // ── Create new server ─────────────────────────────────────
    public void createNewServer(String motd, String version, int ram) {
        String serverName = "server_" + serverCounter++;
        Path serverPath = ss.getServerPath(serverName);

        if (Files.exists(serverPath)) { panel.showMessage("Папка уже существует!"); return; }

        try {
            Files.createDirectories(serverPath);
            ServerCardSettings card = new ServerCardSettings(serverName, version, ram, motd);
            ss.saveServerCardSettings(serverName, card);
            panel.addServerCard(new ServerCard(card, this));
            CompletableFuture
                    .runAsync(() -> initializeServer(serverPath, card, version))
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void downloadJar(Path serverPath, String version) {
        try (InputStream in = new URL(Parser.getDownloadUrl(PAPER, version)).openStream()) {
            Files.copy(in, serverPath.resolve(ServerSettings.SERVER_JAR_NAME),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void createEula(Path serverPath) {
        try { Files.writeString(serverPath.resolve(ServerSettings.EULA_TXT), "eula=true"); }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void createBasicProperties(Path serverPath, ServerCardSettings card) {
        Properties p = new Properties();
        p.setProperty("server-port", String.valueOf(card.getPort()));
        p.setProperty("motd",        card.getMotd());
        p.setProperty("max-players", "20");
        p.setProperty("online-mode", "true");
        try (OutputStream os = Files.newOutputStream(serverPath.resolve(ServerSettings.SERVER_PROPERTIES))) {
            p.store(os, "Generated by launcher");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Start / Stop ──────────────────────────────────────────
    private void startServerProcess(String serverName, ServerCardSettings card) {
        try {
            Path path = ss.getServerPath(serverName);
            String java = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            ProcessBuilder pb = new ProcessBuilder(
                    java,
                    "-Xmx" + card.getRamGB() + "G",
                    "-Xms" + Math.max(1, card.getRamGB() / 2) + "G",
                    "-jar", ServerSettings.SERVER_JAR_NAME, "-nogui"
            ).directory(path.toFile());
            pb.redirectErrorStream(true);

            Process proc = pb.start();
            runningProcesses.put(serverName, proc);
            serverLogs.putIfAbsent(serverName, new StringBuilder());

            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(proc.getOutputStream(), StandardCharsets.UTF_8), true);
            processInputs.put(serverName, pw);
            startLogReader(serverName);
        } catch (Exception e) {
            e.printStackTrace();
            panel.showMessage("Ошибка запуска: " + e.getMessage());
        }
    }

    /** Called by start/stop buttons in any card. */
    public void startstopbutton(Button btn, String serverName, Label status, ServerCardSettings card) {
        if (!isServerRunning(serverName)) {
            // ── START ──
            Platform.runLater(() -> status.setText("Запуск..."));
            CompletableFuture.runAsync(() -> {
                startServerProcess(serverName, card);
                updateAllServerUI(serverName, true);
            });
        } else {
            // ── STOP ──
            Platform.runLater(() -> status.setText("Выключение..."));
            Process p = runningProcesses.get(serverName);
            PrintWriter writer = processInputs.get(serverName);
            if (writer != null) { writer.println("stop"); writer.flush(); }
            p.onExit().orTimeout(10, TimeUnit.SECONDS)
                    .thenRun(() -> { finishStop(serverName); updateAllServerUI(serverName, false); })
                    .exceptionally(ex -> {
                        if (p.isAlive()) p.destroyForcibly();
                        finishStop(serverName);
                        updateAllServerUI(serverName, false);
                        return null;
                    });
        }
    }

    private void finishStop(String name) {
        runningProcesses.remove(name);
        activeLogReaders.remove(name);
        PrintWriter pw = processInputs.remove(name);
        if (pw != null) try { pw.close(); } catch (Exception ignored) {}
        activeConsoles.remove(name);
    }

    // ── Log reader ────────────────────────────────────────────
    private void startLogReader(String serverName) {
        if (!activeLogReaders.add(serverName)) return;
        Process p = runningProcesses.get(serverName);
        if (p == null || !p.isAlive()) { activeLogReaders.remove(serverName); return; }

        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String finalLine = line + "\n";
                    serverLogs.computeIfAbsent(serverName, k -> new StringBuilder()).append(finalLine);
                    List<TextArea> consoles = activeConsoles.get(serverName);
                    if (consoles != null) {
                        Platform.runLater(() -> consoles.forEach(ta -> {
                            if (ta.getScene() != null) {
                                ta.appendText(finalLine);
                                ta.positionCaret(ta.getLength());
                            }
                        }));
                    }
                }
            } catch (IOException ignored) {
            } finally { activeLogReaders.remove(serverName); }
        });
    }

    /** Registers a TextArea to receive live log output and restores history. */
    public void LoggingToConsole(String serverName, TextArea textArea) {
        activeConsoles.computeIfAbsent(serverName, k -> new CopyOnWriteArrayList<>()).add(textArea);
        StringBuilder history = serverLogs.computeIfAbsent(serverName, k -> new StringBuilder());
        String current;
        synchronized (history) { current = history.toString(); }
        Platform.runLater(() -> {
            textArea.setText(current);
            textArea.positionCaret(textArea.getLength());
        });
        startLogReader(serverName);
    }

    public void clearServerLog(String name) { serverLogs.put(name, new StringBuilder()); }

    /** Opens the management dialog with Console / Settings / Plugins tabs. */
    public void openEditServer(String serverName) {
        Stage dlg = new Stage();
        dlg.initOwner(panel.getScene().getWindow());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Управление — " + serverName);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().addAll(
                new Tab("Консоль",   new ManageServerCard(serverName, this)),
                new Tab("Настройки", new EditServerCard(serverName, this)),
                new Tab("Плагины",   new PluginsCard(serverName, this))
        );

        // Apply current theme to this dialog — was missing
        FxUtils.applyThemeClass(tabs);

        Scene scene = new Scene(tabs, 1000, 650);
        scene.getStylesheets().add(
                getClass().getResource(Theme.CSS_PATH).toExternalForm());
        dlg.setScene(scene);
        dlg.showAndWait();
    }

    public ServerCardSettings reloadCard(String name) { return ss.loadServerCardSettings(name); }

    public void SendToServer(String serverName, String command) {
        PrintWriter writer = processInputs.get(serverName);
        if (writer != null) { writer.println(command); writer.flush(); }
    }

    public Path getServerPath(String serverName) { return ss.getServerPath(serverName); }
}