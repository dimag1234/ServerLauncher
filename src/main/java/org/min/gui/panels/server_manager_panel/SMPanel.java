package org.min.gui.panels.server_manager_panel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.min.Parser.Parser;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.cards.ServerCard;

import java.io.IOException;
import java.util.List;

import static org.min.Parser.Parser.ServerType.PAPER;

public class SMPanel extends BorderPane {

    private final VBox     serverListPanel;
    private final SMLogic  logic;

    public SMPanel() {
        logic = new SMLogic(this);

        setStyle("-fx-background-color: -c-base;");
        setTop(createHeader());

        serverListPanel = new VBox(10);
        serverListPanel.setPadding(new Insets(20, 24, 20, 24));
        serverListPanel.setStyle("-fx-background-color: -c-base;");

        ScrollPane scroll = new ScrollPane(serverListPanel);
        scroll.setFitToWidth(true);
        setCenter(scroll);

        Platform.runLater(logic::loadExistingServers);
    }

    // ── Header ────────────────────────────────────────────────
    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("panel-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Серверы");
        title.getStyleClass().add(Theme.LABEL_TITLE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button createBtn = new Button("+ Создать сервер");
        Theme.applyPrimary(createBtn);
        createBtn.setOnAction(e -> showCreateServerDialog());

        header.getChildren().addAll(title, spacer, createBtn);
        return header;
    }

    // ── Create-server dialog ──────────────────────────────────
    private void showCreateServerDialog() {
        Stage dlg = new Stage();
        dlg.initOwner(getScene().getWindow());
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.setTitle("Создание нового сервера");
        dlg.setResizable(false);

        // ── Root ──────────────────────────────────────────────
        VBox root = new VBox(24);
        root.setStyle("-fx-background-color: -c-raised;");
        root.setPadding(new Insets(34));
        root.setPrefWidth(500);

        // Apply current theme to this dialog
        FxUtils.applyThemeClass(root);

        Label title = new Label("Новый Minecraft сервер");
        title.getStyleClass().add(Theme.LABEL_TITLE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // ── Form ──────────────────────────────────────────────
        VBox formSection = new VBox(14);
        formSection.getStyleClass().add("settings-section");

        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(140);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        // MOTD
        TextField motdField = new TextField("My Awesome Server");
        motdField.setPrefHeight(38);
        motdField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(motdField, Priority.ALWAYS);
        addRow(grid, 0, "MOTD сервера:", motdField);

        // Version
        ComboBox<String> versionBox = new ComboBox<>();
        versionBox.setPrefHeight(38);
        versionBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(versionBox, Priority.ALWAYS);
        versionBox.setPlaceholder(new Label("Загрузка версий..."));
        addRow(grid, 1, "Версия Paper:", versionBox);

        // Async version load — errors shown to user, not just printed to stderr
        new Thread(() -> {
            try {
                List<String> versions = Parser.getVersions(PAPER);
                versions.sort((a, b) -> {
                    String[] pa = a.split("\\.");
                    String[] pb = b.split("\\.");
                    for (int i = 0; i < Math.max(pa.length, pb.length); i++) {
                        int na = i < pa.length ? parseInt(pa[i]) : 0;
                        int nb = i < pb.length ? parseInt(pb[i]) : 0;
                        if (na != nb) return Integer.compare(nb, na);
                    }
                    return b.compareTo(a);
                });
                Platform.runLater(() -> {
                    versionBox.setItems(FXCollections.observableArrayList(versions));
                    if (!versions.isEmpty()) versionBox.getSelectionModel().selectFirst();
                });
            } catch (IOException ex) {
                Platform.runLater(() -> {
                    versionBox.setPlaceholder(new Label("Ошибка загрузки версий"));
                    showAlert(dlg, Alert.AlertType.ERROR,
                            "Не удалось загрузить версии Paper:\n" + ex.getMessage());
                });
            }
        }).start();

        // RAM
        Spinner<Integer> ramSpinner = new Spinner<>(2, 16, 4, 1);
        ramSpinner.setEditable(true);
        ramSpinner.setPrefHeight(38);
        addRow(grid, 2, "ОЗУ (GB):", ramSpinner);

        formSection.getChildren().add(grid);

        // ── Buttons ───────────────────────────────────────────
        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Отмена");
        Theme.applySecondary(cancelBtn);
        cancelBtn.setOnAction(e -> dlg.close());

        Button createBtn = new Button("Создать сервер");
        Theme.applyPrimary(createBtn);
        createBtn.setOnAction(e -> {
            String motd = motdField.getText().trim();
            if (motd.isEmpty()) { showAlert(dlg, Alert.AlertType.ERROR, "Введите MOTD сервера!"); return; }
            String version = versionBox.getValue();
            if (version == null || version.isBlank()) { showAlert(dlg, Alert.AlertType.ERROR, "Выберите версию!"); return; }
            logic.createNewServer(motd, version, ramSpinner.getValue());
            dlg.close();
        });

        buttons.getChildren().addAll(cancelBtn, createBtn);
        root.getChildren().addAll(title, formSection, buttons);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource(Theme.CSS_PATH).toExternalForm());
        dlg.setScene(scene);
        dlg.sizeToScene();
        dlg.showAndWait();
    }

    // ── Helpers ───────────────────────────────────────────────
    private void addRow(GridPane grid, int row, String label, javafx.scene.Node control) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s.replaceAll("[^0-9].*", "")); }
        catch (Exception e) { return 0; }
    }

    private void showAlert(Stage owner, Alert.AlertType type, String msg) {
        Alert a = new Alert(type);
        a.initOwner(owner);
        a.setHeaderText(null);
        a.setContentText(msg);
        FxUtils.style(a);   // was missing — dialogs appeared unstyled / wrong theme
        a.showAndWait();
    }

    // ── Public API ────────────────────────────────────────────
    public void addServerCard(ServerCard card) {
        Platform.runLater(() -> serverListPanel.getChildren().add(card));
    }

    public void showMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText(message);
            if (getScene() != null) alert.initOwner(getScene().getWindow());
            FxUtils.style(alert);   // was missing
            alert.showAndWait();
        });
    }
}