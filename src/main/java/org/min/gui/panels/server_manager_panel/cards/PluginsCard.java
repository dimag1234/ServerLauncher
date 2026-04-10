package org.min.gui.panels.server_manager_panel.cards;

import com.google.gson.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.SMLogic;
import org.min.logging.ILogger;
import org.min.logging.Loggers;
import org.min.settings.ServerSettings.ServerCardSettings;

import java.awt.Desktop;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.CompletableFuture;

public class PluginsCard extends BorderPane {

    private static final ILogger logger = Loggers.get(PluginsCard.class);

    private final String          serverName;
    private final SMLogic         logic;
    private final ServerCardSettings card;

    private final TextField    searchField  = new TextField();
    private final Button       searchBtn    = new Button("🔍 Искать");
    private final Label        statusLabel  = new Label("Загружаем популярные плагины...");
    private final ListView<PluginItem> resultsList = new ListView<>();

    private final Button downloadBtn    = new Button("⬇ Скачать");
    private final Button openBrowserBtn = new Button("🌐 Modrinth");
    private final Button prevBtn        = new Button("← Назад");
    private final Button nextBtn        = new Button("Вперёд →");

    private int    currentOffset = 0;
    private int    totalHits     = 0;
    private String lastQuery     = "";
    private static final int PAGE_SIZE = 30;

    // ── Data record ───────────────────────────────────────────
    private record PluginItem(String slug, String title, String author,
                              String description, int downloads, String version) {
        @Override public String toString() { return title; }
    }

    public PluginsCard(String serverName, SMLogic logic) {
        this.serverName = serverName;
        this.logic      = logic;
        this.card       = logic.reloadCard(serverName);

        logger.info("Opening PluginsCard for: %s", serverName);

        setStyle("-fx-background-color: -c-base;");
        setPadding(new Insets(22));

        setTop(buildHeader());
        setCenter(buildList());
        setBottom(buildBottomBar());

        // Listeners
        searchBtn .setOnAction(e -> performSearch());
        prevBtn   .setOnAction(e -> prevPage());
        nextBtn   .setOnAction(e -> nextPage());
        downloadBtn  .setOnAction(e -> downloadSelected());
        openBrowserBtn.setOnAction(e -> openInBrowser());
        resultsList.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> updateActionButtons());

        updateActionButtons();
        logger.debug("PluginsCard initialized for: %s  version: %s", serverName, card.getVersion());

        Platform.runLater(this::loadPopularPlugins);
    }

    // ── Header (title + version info + search bar) ────────────
    private VBox buildHeader() {
        Label title = new Label("🧩 Плагины Modrinth");
        title.getStyleClass().add(Theme.LABEL_TITLE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label versionInfo = new Label("Версия сервера: " + card.getVersion());
        versionInfo.getStyleClass().add(Theme.LABEL_SECONDARY);

        searchField.setPromptText("Введите название плагина…");
        searchField.setPrefHeight(38);
        searchField.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        Theme.applyPrimary(searchBtn);
        searchBtn.setPrefHeight(38);

        HBox searchRow = new HBox(10, versionInfo, searchField, searchBtn);
        searchRow.setAlignment(Pos.CENTER_LEFT);

        statusLabel.getStyleClass().add(Theme.LABEL_RUNNING);

        VBox header = new VBox(12, title, searchRow, statusLabel);
        header.setPadding(new Insets(0, 0, 16, 0));
        return header;
    }

    // ── Results list ──────────────────────────────────────────
    private ScrollPane buildList() {
        resultsList.getStyleClass().add(Theme.PLUGINS_LIST);
        resultsList.setCellFactory(lv -> new PluginCell());

        ScrollPane scroll = new ScrollPane(resultsList);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        return scroll;
    }

    // ── Bottom bar (pagination + action buttons) ──────────────
    private HBox buildBottomBar() {
        Theme.applySecondary(prevBtn);
        Theme.applySecondary(nextBtn);
        Theme.applyPrimary(downloadBtn);
        Theme.applySecondary(openBrowserBtn);

        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        downloadBtn.setDisable(true);
        openBrowserBtn.setDisable(true);

        HBox bar = new HBox(10, prevBtn, downloadBtn, openBrowserBtn, nextBtn);
        bar.setAlignment(Pos.CENTER);
        bar.setPadding(new Insets(14, 0, 0, 0));
        return bar;
    }

    // ── Load popular plugins ──────────────────────────────────
    private void loadPopularPlugins() {
        logger.info("Loading popular plugins for version: %s", card.getVersion());
        lastQuery     = "";
        currentOffset = 0;
        performSearchInternal("");
    }

    // ── Search ────────────────────────────────────────────────
    private void performSearch() {
        lastQuery     = searchField.getText().trim();
        currentOffset = 0;
        logger.info("Searching plugins: '%s' for version: %s", lastQuery, card.getVersion());
        performSearchInternal(lastQuery);
    }

    private void performSearchInternal(String query) {
        Platform.runLater(() -> {
            resultsList.getItems().clear();
            downloadBtn.setDisable(true);
            openBrowserBtn.setDisable(true);
            prevBtn.setDisable(true);
            nextBtn.setDisable(true);
            statusLabel.setText("⏳ Поиск…");
            statusLabel.getStyleClass().setAll(Theme.LABEL_SECONDARY);
        });

        String mcVersion = card.getVersion();
        CompletableFuture.runAsync(() -> {
            try {
                String facets = URLEncoder.encode(
                        "[[\"project_type:plugin\"],[\"categories:paper\"],[\"versions:" + mcVersion + "\"]]",
                        StandardCharsets.UTF_8);

                String url = "https://api.modrinth.com/v2/search"
                        + "?query=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                        + "&facets=" + facets
                        + "&limit=" + PAGE_SIZE
                        + "&offset=" + currentOffset;

                logger.debug("Modrinth request: %s", url);

                HttpClient  client = HttpClient.newHttpClient();
                HttpRequest req    = HttpRequest.newBuilder(URI.create(url)).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

                JsonObject json = JsonParser.parseString(resp.body()).getAsJsonObject();
                if (json.has("error"))
                    throw new Exception("Modrinth: " + json.get("description").getAsString());

                JsonArray hits = json.has("hits") ? json.getAsJsonArray("hits") : new JsonArray();
                totalHits = json.has("total_hits") ? json.get("total_hits").getAsInt() : hits.size();

                logger.debug("Got %s hits (total: %s)", hits.size(), totalHits);

                Platform.runLater(() -> {
                    for (JsonElement el : hits) {
                        JsonObject p = el.getAsJsonObject();
                        resultsList.getItems().add(new PluginItem(
                                p.get("slug").getAsString(),
                                p.get("title").getAsString(),
                                p.get("author").getAsString(),
                                p.has("description") ? p.get("description").getAsString() : "",
                                p.get("downloads").getAsInt(),
                                p.get("latest_version").getAsString()
                        ));
                    }
                    prevBtn.setDisable(currentOffset <= 0);
                    nextBtn.setDisable(currentOffset + hits.size() >= totalHits);

                    String page = String.valueOf(currentOffset / PAGE_SIZE + 1);
                    statusLabel.setText("✅ Показано " + hits.size() + " из " + totalHits + "  (стр. " + page + ")");
                    statusLabel.getStyleClass().setAll(Theme.LABEL_RUNNING);
                });

            } catch (Exception ex) {
                logger.error("Plugin search failed", ex);
                Platform.runLater(() -> {
                    statusLabel.setText("❌ " + ex.getMessage());
                    // Use LABEL_STOPPED class only — do not mix CSS class + inline -c-* var
                    statusLabel.getStyleClass().setAll(Theme.LABEL_STOPPED);
                });
            }
        });
    }

    // ── Pagination ────────────────────────────────────────────
    private void prevPage() {
        if (currentOffset >= PAGE_SIZE) {
            currentOffset -= PAGE_SIZE;
            logger.debug("prevPage → offset: %s", currentOffset);
            performSearchInternal(lastQuery);
        }
    }

    private void nextPage() {
        if (currentOffset + PAGE_SIZE < totalHits) {
            currentOffset += PAGE_SIZE;
            logger.debug("nextPage → offset: %s", currentOffset);
            performSearchInternal(lastQuery);
        }
    }

    // ── Update action buttons ─────────────────────────────────
    private void updateActionButtons() {
        boolean sel = resultsList.getSelectionModel().getSelectedItem() != null;
        downloadBtn  .setDisable(!sel);
        openBrowserBtn.setDisable(!sel);
    }

    // ── Download ──────────────────────────────────────────────
    private void downloadSelected() {
        PluginItem item = resultsList.getSelectionModel().getSelectedItem();
        if (item == null) return;

        logger.info("Downloading plugin: %s  version: %s", item.slug(), card.getVersion());

        downloadBtn.setText("⏳ Скачиваем…");
        downloadBtn.setDisable(true);

        CompletableFuture.runAsync(() -> {
            try {
                String loadersEnc  = URLEncoder.encode("[\"paper\"]", StandardCharsets.UTF_8);
                String versionsEnc = URLEncoder.encode("[\"" + card.getVersion() + "\"]", StandardCharsets.UTF_8);

                String versionUrl = "https://api.modrinth.com/v2/project/" + item.slug()
                        + "/version?loaders=" + loadersEnc + "&game_versions=" + versionsEnc;

                HttpClient  client   = HttpClient.newHttpClient();
                HttpRequest req      = HttpRequest.newBuilder(URI.create(versionUrl)).GET().build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

                JsonArray versions = JsonParser.parseString(resp.body()).getAsJsonArray();
                if (versions.isEmpty()) throw new Exception("Нет совместимой версии для " + card.getVersion());

                JsonObject fileObj   = versions.get(0).getAsJsonObject()
                        .getAsJsonArray("files").get(0).getAsJsonObject();
                String downloadUrl   = fileObj.get("url")     .getAsString();
                String fileName      = fileObj.get("filename").getAsString();

                logger.debug("Downloading: %s → %s", downloadUrl, fileName);

                Path pluginsDir = logic.getServerPath(serverName).resolve("plugins");
                Files.createDirectories(pluginsDir);
                Path target = pluginsDir.resolve(fileName);

                HttpRequest dlReq = HttpRequest.newBuilder(URI.create(downloadUrl)).GET().build();
                client.send(dlReq, HttpResponse.BodyHandlers.ofFile(target));

                logger.info("Plugin downloaded: %s", fileName);

                Platform.runLater(() -> {
                    downloadBtn.setText("⬇ Скачать");
                    downloadBtn.setDisable(false);
                    showAlert(Alert.AlertType.INFORMATION,
                            "✅ Плагин скачан!\n" + fileName + "\n→ папка plugins/");
                });

            } catch (Exception ex) {
                logger.error("Plugin download failed: " + item.slug(), ex);
                Platform.runLater(() -> {
                    downloadBtn.setText("⬇ Скачать");
                    downloadBtn.setDisable(false);
                    showAlert(Alert.AlertType.ERROR,
                            "❌ Ошибка скачивания:\n" + ex.getMessage());
                });
            }
        });
    }

    // ── Open in browser ───────────────────────────────────────
    private void openInBrowser() {
        PluginItem item = resultsList.getSelectionModel().getSelectedItem();
        if (item == null) return;
        try {
            logger.debug("Opening in browser: %s", item.slug());
            Desktop.getDesktop().browse(new URI("https://modrinth.com/plugin/" + item.slug()));
        } catch (Exception ex) {
            logger.error("Cannot open browser", ex);
        }
    }

    // ── Alert helper ──────────────────────────────────────────
    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        if (getScene() != null) alert.initOwner(getScene().getWindow());
        FxUtils.style(alert);   // was missing — dialog always appeared unstyled / wrong theme
        alert.showAndWait();
    }

    // ── Custom cell ───────────────────────────────────────────
    private static class PluginCell extends ListCell<PluginItem> {

        private final Label titleLbl  = new Label();
        private final Label authorLbl = new Label();
        private final Label descLbl   = new Label();
        private final Label dlLbl     = new Label();
        private final VBox  root;

        PluginCell() {
            titleLbl.getStyleClass().add("plugin-title");
            authorLbl.getStyleClass().add("plugin-author");
            descLbl.getStyleClass().add("plugin-desc");
            descLbl.setWrapText(true);
            dlLbl.getStyleClass().add("plugin-downloads");

            HBox topRow = new HBox(10, titleLbl, dlLbl);
            topRow.setAlignment(Pos.CENTER_LEFT);

            root = new VBox(4, topRow, authorLbl, descLbl);
            root.setPadding(new Insets(10, 14, 10, 14));
            setGraphic(root);
            setText(null);
        }

        @Override
        protected void updateItem(PluginItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
            } else {
                titleLbl.setText(item.title());
                authorLbl.setText(item.author());
                String desc = item.description();
                descLbl.setText(desc.length() > 90 ? desc.substring(0, 87) + "…" : desc);
                dlLbl.setText("↓ " + formatDownloads(item.downloads()));
                setGraphic(root);
            }
        }

        private String formatDownloads(int n) {
            if (n >= 1_000_000) return String.format("%.1fM", n / 1_000_000.0);
            if (n >= 1_000)     return String.format("%.1fK", n / 1_000.0);
            return String.valueOf(n);
        }
    }
}