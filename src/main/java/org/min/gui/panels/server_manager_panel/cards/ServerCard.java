package org.min.gui.panels.server_manager_panel.cards;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import org.min.gui.common.Theme;
import org.min.gui.panels.server_manager_panel.SMLogic;
import org.min.settings.ServerSettings.ServerCardSettings;

public class ServerCard extends HBox {

    private final ServerCardSettings card;
    private final SMLogic            logic;
    private final Label              statusLabel;
    private final Button             startBtn;

    public ServerCard(ServerCardSettings card, SMLogic logic) {
        this.card  = card;
        this.logic = logic;

        getStyleClass().add(Theme.SERVER_CARD);
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(18);
        setMaxWidth(Double.MAX_VALUE);

        // ── Status dot ────────────────────────────────────────
        Circle dot = new Circle(5);
        dot.setStyle("-fx-fill: -c-text3;");   // will be updated by updateAllServerUI

        // ── Name + version ────────────────────────────────────
        VBox nameBox = new VBox(4);
        HBox.setHgrow(nameBox, Priority.ALWAYS);

        Label nameLbl = new Label(card.getDisplayName());
        nameLbl.getStyleClass().add(Theme.LABEL_SECTION);

        Label versionLbl = new Label(card.getVersion());
        versionLbl.getStyleClass().add(Theme.LABEL_VERSION);

        nameBox.getChildren().addAll(nameLbl, versionLbl);

        // ── Info (RAM / port) ─────────────────────────────────
        VBox infoBox = new VBox(3);
        infoBox.setAlignment(Pos.CENTER_RIGHT);
        infoBox.setMinWidth(120);

        Label ramLbl  = new Label("RAM  " + card.getRamGB() + " GB");
        ramLbl.getStyleClass().add(Theme.LABEL_MUTED);
        Label portLbl = new Label("Port " + card.getPort());
        portLbl.getStyleClass().add(Theme.LABEL_MUTED);
        infoBox.getChildren().addAll(ramLbl, portLbl);

        // ── Status text ───────────────────────────────────────
        statusLabel = new Label("Остановлен");
        statusLabel.getStyleClass().add(Theme.LABEL_STOPPED);
        statusLabel.setMinWidth(80);
        statusLabel.setAlignment(Pos.CENTER);

        // ── Manage button ─────────────────────────────────────
        Button manageBtn = new Button("Управление");
        Theme.applySecondary(manageBtn);
        manageBtn.setOnAction(e -> logic.openEditServer(card.getServerFolderName()));

        // ── Start / Stop button ───────────────────────────────
        startBtn = new Button("Start");
        Theme.applyStart(startBtn);
        startBtn.setOnAction(e ->
                logic.startstopbutton(startBtn, card.getServerFolderName(), statusLabel, card));

        getChildren().addAll(dot, nameBox, infoBox, statusLabel, manageBtn, startBtn);

        logic.registerServerUI(card.getServerFolderName(), statusLabel, startBtn);
    }
}
