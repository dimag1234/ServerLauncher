package min.gui.panels.server_manager_panel.cards;

import min.gui.panels.server_manager_panel.SMLogic;
import min.gui.common.Theme;
import min.settings.ServerSettings.ServerCardSettings;

import com.google.gson.*;
import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

public class PluginsCard extends JPanel {

    private final String serverName;
    private final SMLogic logic;
    private final ServerCardSettings card;

    private final JTextField searchField = new JTextField(30);
    private final JButton searchBtn = new JButton("🔍 Искать");
    private final JLabel statusLabel = new JLabel("Загружаем популярные плагины...");
    private final DefaultListModel<PluginItem> listModel = new DefaultListModel<>();
    private final JList<PluginItem> resultsList = new JList<>(listModel);

    private final JButton downloadBtn = new JButton("⬇ Скачать последнюю версию");
    private final JButton openBrowserBtn = new JButton("🌐 Открыть на Modrinth");
    private final JButton prevBtn = new JButton("← Предыдущая");
    private final JButton nextBtn = new JButton("Следующая →");

    private int currentOffset = 0;
    private int totalHits = 0;
    private String lastQuery = "";
    private static final int PAGE_SIZE = 30;

    private record PluginItem(String slug, String title, String author, String description,
                              int downloads, String version) {
        @Override
        public String toString() { return title; }
    }

    public PluginsCard(String serverName, SMLogic logic) {
        this.serverName = serverName;
        this.logic = logic;
        this.card = logic.reloadCard(serverName);

        setLayout(new BorderLayout(12, 12));
        setBackground(Theme.BACKGROUND_MEDIUM);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // HEADER + SEARCH
        JLabel header = new JLabel("🧩 Плагины Modrinth", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 24));
        header.setForeground(Color.WHITE);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel versionInfo = new JLabel("Версия сервера: " + card.getVersion());
        versionInfo.setForeground(Theme.TEXT_SECONDARY);

        styleSearchField();
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchBtn.setBackground(new Color(0, 120, 215));
        searchBtn.setForeground(Color.WHITE);

        searchPanel.add(versionInfo);
        searchPanel.add(searchField);
        searchPanel.add(searchBtn);

        // RESULTS
        resultsList.setBackground(new Color(25, 25, 25));
        resultsList.setForeground(Color.WHITE);
        resultsList.setCellRenderer(new PluginCellRenderer());

        JScrollPane scroll = new JScrollPane(resultsList);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 50), 2));

        // BOTTOM BAR
        stylePaginationButtons();
        downloadBtn.setEnabled(false);
        openBrowserBtn.setEnabled(false);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        bottom.setOpaque(false);
        bottom.add(prevBtn);
        bottom.add(downloadBtn);
        bottom.add(openBrowserBtn);
        bottom.add(nextBtn);

        // STATUS
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        statusLabel.setForeground(new Color(100, 255, 100));

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(header, BorderLayout.NORTH);
        north.add(searchPanel, BorderLayout.CENTER);
        north.add(statusLabel, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // LISTENERS
        searchBtn.addActionListener(e -> performSearch());
        resultsList.addListSelectionListener(e -> updateButtons());
        downloadBtn.addActionListener(e -> downloadSelected());
        openBrowserBtn.addActionListener(e -> openInBrowser());
        prevBtn.addActionListener(e -> prevPage());
        nextBtn.addActionListener(e -> nextPage());

        // Автозагрузка при открытии вкладки
        SwingUtilities.invokeLater(this::loadPopularPlugins);
    }

    private void styleSearchField() {
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBackground(new Color(35, 35, 35));
        searchField.setForeground(Color.WHITE);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
    }

    private void stylePaginationButtons() {
        for (JButton b : new JButton[]{prevBtn, nextBtn}) {
            b.setFont(new Font("Segoe UI", Font.BOLD, 14));
            b.setBackground(new Color(45, 45, 45));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
        }
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);
    }

    private void loadPopularPlugins() {
        lastQuery = "";
        currentOffset = 0;
        statusLabel.setText("Загружаем популярные плагины по Relevance...");
        performSearchInternal("");
    }

    private void performSearch() {
        lastQuery = searchField.getText().trim();
        currentOffset = 0;
        performSearchInternal(lastQuery);
    }

    private void performSearchInternal(String query) {
        listModel.clear();
        downloadBtn.setEnabled(false);
        openBrowserBtn.setEnabled(false);
        prevBtn.setEnabled(false);
        nextBtn.setEnabled(false);

        String mcVersion = card.getVersion();

        CompletableFuture.runAsync(() -> {
            try {
                String facets = URLEncoder.encode(
                        "[[\"project_type:plugin\"],[\"categories:paper\"],[\"versions:" + mcVersion + "\"]]",
                        StandardCharsets.UTF_8);

                String url = "https://api.modrinth.com/v2/search?query=" +
                        URLEncoder.encode(query, StandardCharsets.UTF_8) +
                        "&facets=" + facets +
                        "&limit=" + PAGE_SIZE +
                        "&offset=" + currentOffset;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();

                if (json.has("error")) {
                    throw new Exception("Modrinth: " + json.get("description").getAsString());
                }

                JsonArray hits = json.has("hits") ? json.getAsJsonArray("hits") : new JsonArray();
                totalHits = json.has("total_hits") ? json.get("total_hits").getAsInt() : hits.size();

                SwingUtilities.invokeLater(() -> {
                    for (JsonElement el : hits) {
                        JsonObject p = el.getAsJsonObject();
                        listModel.addElement(new PluginItem(
                                p.get("slug").getAsString(),
                                p.get("title").getAsString(),
                                p.get("author").getAsString(),
                                p.has("description") ? p.get("description").getAsString() : "",
                                p.get("downloads").getAsInt(),
                                p.get("latest_version").getAsString()
                        ));
                    }

                    prevBtn.setEnabled(currentOffset > 0);
                    nextBtn.setEnabled(currentOffset + hits.size() < totalHits);

                    statusLabel.setText("✅ Показано " + hits.size() + " из " + totalHits +
                            " (Relevance, стр. " + (currentOffset / PAGE_SIZE + 1) + ")");
                    statusLabel.setForeground(new Color(100, 255, 100));
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("❌ " + ex.getMessage());
                    statusLabel.setForeground(Color.RED);
                });
            }
        });
    }

    private void prevPage() {
        if (currentOffset >= PAGE_SIZE) {
            currentOffset -= PAGE_SIZE;
            performSearchInternal(lastQuery);
        }
    }

    private void nextPage() {
        if (currentOffset + PAGE_SIZE < totalHits) {
            currentOffset += PAGE_SIZE;
            performSearchInternal(lastQuery);
        }
    }

    private void updateButtons() {
        boolean selected = resultsList.getSelectedValue() != null;
        downloadBtn.setEnabled(selected);
        openBrowserBtn.setEnabled(selected);
    }

    private void downloadSelected() {
        PluginItem item = resultsList.getSelectedValue();
        if (item == null) return;

        downloadBtn.setText("⏳ Скачиваем...");
        downloadBtn.setEnabled(false);

        CompletableFuture.runAsync(() -> {
            try {
                // ФИКС ОШИБКИ: правильное URL-кодирование параметров
                String loadersEncoded = URLEncoder.encode("[\"paper\"]", StandardCharsets.UTF_8);
                String versionsEncoded = URLEncoder.encode("[\"" + card.getVersion() + "\"]", StandardCharsets.UTF_8);

                String versionUrl = "https://api.modrinth.com/v2/project/" + item.slug +
                        "/version?loaders=" + loadersEncoded + "&game_versions=" + versionsEncoded;

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder(URI.create(versionUrl)).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

                JsonArray versions = JsonParser.parseString(resp.body()).getAsJsonArray();
                if (versions.isEmpty()) throw new Exception("Нет совместимой версии");

                JsonObject ver = versions.get(0).getAsJsonObject();
                JsonArray files = ver.getAsJsonArray("files");
                JsonObject primary = files.get(0).getAsJsonObject();

                String downloadUrl = primary.get("url").getAsString();
                String fileName = primary.get("filename").getAsString();

                Path pluginsDir = logic.getServerPath(serverName).resolve("plugins");
                Files.createDirectories(pluginsDir);
                Path target = pluginsDir.resolve(fileName);

                HttpRequest dlReq = HttpRequest.newBuilder(URI.create(downloadUrl)).GET().build();
                client.send(dlReq, HttpResponse.BodyHandlers.ofFile(target));

                SwingUtilities.invokeLater(() -> {
                    downloadBtn.setText("⬇ Скачать последнюю версию");
                    downloadBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "✅ Плагин " + fileName + " скачан в папку plugins/");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    downloadBtn.setText("⬇ Скачать последнюю версию");
                    downloadBtn.setEnabled(true);
                    JOptionPane.showMessageDialog(this, "❌ Ошибка скачивания:\n" + ex.getMessage());
                });
            }
        });
    }

    private void openInBrowser() {
        PluginItem item = resultsList.getSelectedValue();
        if (item == null) return;
        try {
            Desktop.getDesktop().browse(new URI("https://modrinth.com/plugin/" + item.slug));
        } catch (Exception ignored) {}
    }

    // Красивый рендерер
    private static class PluginCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            PluginItem item = (PluginItem) value;
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setBackground(isSelected ? new Color(0, 100, 200) : new Color(30, 30, 30));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

            JLabel title = new JLabel(item.title);
            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            title.setForeground(Color.WHITE);

            JLabel info = new JLabel(item.author + " • " + item.downloads + " скачиваний");
            info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            info.setForeground(new Color(180, 180, 180));

            JLabel desc = new JLabel("<html><i>" + (item.description.length() > 85
                    ? item.description.substring(0, 82) + "..." : item.description) + "</i></html>");
            desc.setForeground(new Color(200, 200, 200));

            JPanel text = new JPanel(new BorderLayout(0, 3));
            text.setOpaque(false);
            text.add(title, BorderLayout.NORTH);
            text.add(info, BorderLayout.CENTER);
            text.add(desc, BorderLayout.SOUTH);

            panel.add(text, BorderLayout.CENTER);
            return panel;
        }
    }
}