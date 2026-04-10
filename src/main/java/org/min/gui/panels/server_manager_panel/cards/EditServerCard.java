package org.min.gui.panels.server_manager_panel.cards;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.SMLogic;
import org.min.logging.ILogger;
import org.min.logging.Loggers;
import org.min.settings.ServerSettings;

public class EditServerCard extends BorderPane {

    private static final ILogger logger = Loggers.get(EditServerCard.class);

    private final String                            serverName;
    private final SMLogic                           logic;
    private final ServerSettings.ServerCardSettings card;

    private final TextField displayNameField;
    private final Label     versionField;
    private final Spinner<Integer> ramSpinner;
    private final Spinner<Integer> portSpinner;
    private final TextField motdField;

    public EditServerCard(String serverName, SMLogic logic) {
        this.serverName = serverName;
        this.logic      = logic;
        this.card       = logic.reloadCard(serverName);

        logger.info("Opening EditServerCard for: %s", serverName);

        setStyle("-fx-background-color: -c-base;");
        setPadding(new Insets(28));

        // ── Title ─────────────────────────────────────────────
        Label title = new Label("Редактирование сервера");
        title.getStyleClass().add(Theme.LABEL_TITLE);
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        setTop(wrapPad(title, 0, 0, 22, 0));

        // ── Form ──────────────────────────────────────────────
        VBox formSection = new VBox(14);
        formSection.getStyleClass().add("settings-section");

        GridPane grid = makeGrid();

        // Folder name (read-only info)
        Label folderVal = new Label(serverName);
        folderVal.getStyleClass().add(Theme.LABEL_SECONDARY);
        addRow(grid, 0, "Папка сервера:", folderVal);

        // Display name
        displayNameField = new TextField(card.getDisplayName());
        displayNameField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(displayNameField, Priority.ALWAYS);
        addRow(grid, 1, "Отображаемое имя:", displayNameField);

        // Version (read-only)
        versionField = new Label(card.getVersion());
        versionField.getStyleClass().add(Theme.LABEL_VERSION);
        addRow(grid, 2, "Версия:", versionField);

        // RAM
        ramSpinner = new Spinner<>(1, 32, card.getRamGB(), 1);
        ramSpinner.setEditable(true);
        addRow(grid, 3, "ОЗУ (GB):", ramSpinner);

        // Port
        portSpinner = new Spinner<>(1024, 65535, card.getPort(), 1);
        portSpinner.setEditable(true);
        addRow(grid, 4, "Порт:", portSpinner);

        // MOTD
        motdField = new TextField(card.getMotd());
        motdField.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(motdField, Priority.ALWAYS);
        addRow(grid, 5, "MOTD:", motdField);

        formSection.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(formSection);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        setCenter(scroll);

        // ── Buttons ───────────────────────────────────────────
        HBox buttons = new HBox(14);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.setPadding(new Insets(22, 0, 0, 0));

        Button cancelBtn = new Button("Отмена");
        Theme.applySecondary(cancelBtn);
        cancelBtn.setOnAction(e -> {
            logger.debug("EditServerCard cancelled for: %s", serverName);
            closeStage();
        });

        Button saveBtn = new Button("Сохранить");
        Theme.applyPrimary(saveBtn);
        saveBtn.setOnAction(e -> saveAndClose());

        buttons.getChildren().addAll(cancelBtn, saveBtn);
        setBottom(buttons);

        logger.debug("EditServerCard initialized for: %s", serverName);
    }

    // ── Helpers ───────────────────────────────────────────────
    private GridPane makeGrid() {
        GridPane g = new GridPane();
        g.setHgap(20);
        g.setVgap(12);
        ColumnConstraints c1 = new ColumnConstraints(155);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node control) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("form-label");
        grid.add(lbl, 0, row);
        grid.add(control, 1, row);
    }

    private static VBox wrapPad(javafx.scene.Node node, double top, double right, double bottom, double left) {
        VBox box = new VBox(node);
        box.setPadding(new Insets(top, right, bottom, left));
        return box;
    }

    private void closeStage() {
        if (getScene() != null && getScene().getWindow() != null)
            getScene().getWindow().hide();
    }

    // ── Save ──────────────────────────────────────────────────
    private void saveAndClose() {
        logger.info("Saving settings for: %s", serverName);
        try {
            card.setDisplayName(displayNameField.getText().trim());
            card.setVersion(versionField.getText().trim());
            card.setRamGB(ramSpinner.getValue());
            card.setPort(portSpinner.getValue());
            card.setMotd(motdField.getText().trim());

            ServerSettings.getInstance().saveServerCardSettings(serverName, card);

            logger.info("Settings saved successfully for: %s", serverName);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Успех");
            alert.setHeaderText(null);
            alert.setContentText("Настройки сохранены!");
            if (getScene() != null) alert.initOwner(getScene().getWindow());
            FxUtils.style(alert);   // was missing
            alert.showAndWait();

            closeStage();
        } catch (Exception ex) {
            logger.error("Error saving settings for: " + serverName, ex);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText("Ошибка сохранения:\n" + ex.getMessage());
            if (getScene() != null) alert.initOwner(getScene().getWindow());
            FxUtils.style(alert);   // was missing
            alert.showAndWait();
        }
    }
}