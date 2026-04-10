package org.min.gui.panels.server_manager_panel.cards;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.SMLogic;
import org.min.settings.ServerSettings;

public class ManageServerCard extends BorderPane {

    private final String                            serverName;
    private final SMLogic                           logic;
    private final ServerSettings.ServerCardSettings card;

    private final TextArea  logArea    = new TextArea();
    private final TextField inputField = new TextField();
    private final Label     statusLabel = new Label("Остановлен");
    private final Button    toggleBtn   = new Button("Start");

    public ManageServerCard(String serverName, SMLogic logic) {
        this.serverName = serverName;
        this.logic      = logic;
        this.card       = logic.reloadCard(serverName);

        setStyle("-fx-background-color: -c-base;");
        setPadding(new Insets(22));

        // ── Initialise toggle state ───────────────────────────
        if (logic.isServerRunning(serverName)) {
            toggleBtn.setText("Stop");
            Theme.applyStop(toggleBtn);
            Theme.applyRunning(statusLabel);
            statusLabel.setText("Запущен");
        } else {
            toggleBtn.setText("Start");
            Theme.applyStart(toggleBtn);
            Theme.applyStopped(statusLabel);
        }
        logic.registerServerUI(serverName, statusLabel, toggleBtn);

        // ── Header ────────────────────────────────────────────
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 18, 0));

        Label title = new Label(serverName.toUpperCase());
        title.getStyleClass().add(Theme.LABEL_TITLE);

        statusLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic;");

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        header.getChildren().addAll(title, statusLabel, hSpacer);
        setTop(header);

        // ── Console ───────────────────────────────────────────
        logArea.setEditable(false);
        logArea.setWrapText(false);
        Theme.applyConsole(logArea);
        VBox.setVgrow(logArea, Priority.ALWAYS);

        inputField.setPromptText("Введите команду и нажмите Enter…");
        Theme.applyCommandInput(inputField);
        inputField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String cmd = inputField.getText().trim();
                if (!cmd.isEmpty()) {
                    logic.SendToServer(card.getServerFolderName(), cmd);
                    logArea.appendText("> " + cmd + "\n");
                    inputField.clear();
                    logArea.positionCaret(logArea.getLength());
                }
            }
        });


        logArea.setStyle("-fx-text-fill: #ffffff;");
        VBox consoleBox = new VBox(0, logArea, inputField);
        VBox.setVgrow(logArea, Priority.ALWAYS);
        setCenter(consoleBox);

        // ── Sidebar ───────────────────────────────────────────
        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("manage-sidebar");

        toggleBtn.setMaxWidth(Double.MAX_VALUE);
        toggleBtn.setOnAction(e ->
                logic.startstopbutton(toggleBtn, serverName, statusLabel, card));

        Button clearBtn = new Button("Очистить лог");
        Theme.applySecondary(clearBtn);
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.setOnAction(e -> { logic.clearServerLog(serverName); logArea.clear(); });

        sidebar.getChildren().addAll(toggleBtn, clearBtn);
        setRight(sidebar);

        logic.LoggingToConsole(card.getServerFolderName(), logArea);
    }
}
