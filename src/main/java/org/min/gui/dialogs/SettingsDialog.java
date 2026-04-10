package org.min.gui.dialogs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.min.gui.WindowManager;
import org.min.gui.common.FxUtils;
import org.min.gui.common.Theme;
import org.min.logging.ILogger;
import org.min.logging.Loggers;
import org.min.settings.AppSettings;

public class SettingsDialog extends Stage {

    private static final ILogger logger = Loggers.get(SettingsDialog.class);
    private final AppSettings settings  = AppSettings.getInstance();

    private ComboBox<String>  fontFamilyBox;
    private Spinner<Integer>  fontSizeSpinner;
    private ComboBox<String>  fontStyleBox;
    private ComboBox<String>  themeBox;

    public SettingsDialog(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Настройки");
        setResizable(false);
        logger.info("Opening settings dialog");

        // ── Root ─────────────────────────────────────────────
        VBox root = new VBox(20);
        root.getStyleClass().add("settings-dialog");
        root.setStyle("-fx-background-color: -c-raised;");
        root.setPadding(new Insets(30));
        root.setPrefWidth(480);

        Label title = new Label("Настройки");
        title.getStyleClass().add(Theme.LABEL_TITLE);

        root.getChildren().addAll(title,
                buildFontSection(),
                buildThemeSection(),
                buildButtons());

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource(Theme.CSS_PATH).toExternalForm());

        // Apply current theme to this dialog's scene too
        if ("LIGHT".equals(settings.getTheme())) {
            root.getStyleClass().add("light-theme");
        }

        setScene(scene);

        loadSettings();
        sizeToScene();
        logger.debug("Settings dialog initialized");
    }

    // ── Font section ─────────────────────────────────────────
    private VBox buildFontSection() {
        VBox section = new VBox(14);
        section.getStyleClass().add("settings-section");

        Label head = new Label("Шрифт");
        head.getStyleClass().add(Theme.LABEL_SECTION);

        GridPane grid = makeGrid();

        fontFamilyBox = new ComboBox<>(FXCollections.observableArrayList(Font.getFamilies()));
        fontFamilyBox.setMaxWidth(Double.MAX_VALUE);
        addRow(grid, 0, "Семейство:", fontFamilyBox);

        fontSizeSpinner = new Spinner<>(8, 72, 14, 1);
        fontSizeSpinner.setEditable(true);
        fontSizeSpinner.setMaxWidth(110);
        addRow(grid, 1, "Размер:", fontSizeSpinner);

        fontStyleBox = new ComboBox<>(FXCollections.observableArrayList(
                "Обычный", "Жирный", "Курсив", "Жирный курсив"));
        fontStyleBox.setMaxWidth(200);
        addRow(grid, 2, "Стиль:", fontStyleBox);

        section.getChildren().addAll(head, grid);
        return section;
    }

    // ── Theme section ─────────────────────────────────────────
    private VBox buildThemeSection() {
        VBox section = new VBox(14);
        section.getStyleClass().add("settings-section");

        Label head = new Label("Тема");
        head.getStyleClass().add(Theme.LABEL_SECTION);

        GridPane grid = makeGrid();

        themeBox = new ComboBox<>(FXCollections.observableArrayList("Тёмная", "Светлая"));
        themeBox.setMaxWidth(180);
        addRow(grid, 0, "Тема:", themeBox);

        section.getChildren().addAll(head, grid);
        return section;
    }

    // ── Buttons row ───────────────────────────────────────────
    private HBox buildButtons() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = new Button("Отмена");
        Theme.applySecondary(cancel);
        cancel.setOnAction(e -> close());

        Button save = new Button("Сохранить");
        Theme.applyPrimary(save);
        save.setOnAction(e -> saveAndClose());

        row.getChildren().addAll(cancel, save);
        return row;
    }

    // ── Helpers ───────────────────────────────────────────────
    private GridPane makeGrid() {
        GridPane g = new GridPane();
        g.setHgap(18); g.setVgap(10);
        ColumnConstraints c1 = new ColumnConstraints(130);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    private void addRow(GridPane g, int row, String text, javafx.scene.Node control) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("form-label");
        g.add(lbl, 0, row);
        g.add(control, 1, row);
    }

    // ── Load / Save ───────────────────────────────────────────
    private void loadSettings() {
        logger.debug("Loading current settings into dialog");

        fontFamilyBox.setValue(settings.getFontFamily());
        fontSizeSpinner.getValueFactory().setValue(settings.getFontSize());

        int styleIdx = switch (settings.getFontStyle().toUpperCase()) {
            case "BOLD"       -> 1;
            case "ITALIC"     -> 2;
            case "BOLDITALIC" -> 3;
            default           -> 0;
        };
        fontStyleBox.getSelectionModel().select(styleIdx);
        themeBox.getSelectionModel().select("DARK".equals(settings.getTheme()) ? 0 : 1);

        logger.debug("Settings loaded – styleIdx=%s theme=%s", styleIdx, settings.getTheme());
    }

    private void saveAndClose() {
        logger.info("Saving settings");

        String family = fontFamilyBox.getValue();
        int    size   = fontSizeSpinner.getValue();
        String style  = switch (fontStyleBox.getSelectionModel().getSelectedIndex()) {
            case 1  -> "BOLD";
            case 2  -> "ITALIC";
            case 3  -> "BOLDITALIC";
            default -> "PLAIN";
        };
        String theme = themeBox.getSelectionModel().getSelectedIndex() == 0 ? "DARK" : "LIGHT";

        // Persist
        settings.setFont(family, size, style);
        settings.setTheme(theme);

        // ── Apply live — no restart needed ───────────────────
        WindowManager.applyTheme(theme);
        WindowManager.applyFont(family, size, style);

        logger.info("Settings saved and applied successfully");

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Успех");
        info.setHeaderText(null);
        info.setContentText("Настройки сохранены и применены!");
        info.initOwner(this);
        FxUtils.style(info);
        info.showAndWait();

        close();
    }
}