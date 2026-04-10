package org.min.gui.common;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import org.min.settings.AppSettings;

import java.util.Optional;

/**
 * Utility for creating styled JavaFX dialogs.
 * Alert.AlertType dialogs don't inherit CSS from the parent scene —
 * the stylesheet must be added to the DialogPane explicitly.
 */
public final class FxUtils {

    private FxUtils() {}

    private static String cssUrl() {
        return FxUtils.class.getResource(Theme.CSS_PATH).toExternalForm();
    }

    // ── Theme helper ──────────────────────────────────────────
    /**
     * Applies (or removes) the "light-theme" style class to any Node
     * that serves as the root of a scene or dialog.
     * Call this right after creating the root node of any new Scene.
     */
    public static void applyThemeClass(Node node) {
        if ("LIGHT".equals(AppSettings.getInstance().getTheme())) {
            if (!node.getStyleClass().contains("light-theme"))
                node.getStyleClass().add("light-theme");
        } else {
            node.getStyleClass().remove("light-theme");
        }
    }

    // ── Low-level: apply our CSS + current theme to any Alert ─
    public static void style(Alert alert) {
        alert.getDialogPane().getStylesheets().add(cssUrl());

        // Remove the default icon — looks out of place in custom themes
        alert.getDialogPane().setGraphic(null);

        // Apply current theme so dialogs match the main window
        applyThemeClass(alert.getDialogPane());
    }

    // ── Convenience factory methods ───────────────────────────

    public static void info(Window owner, String message) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(owner);
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        a.showAndWait();
    }

    public static void error(Window owner, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(owner);
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        a.showAndWait();
    }

    public static boolean confirm(Window owner, String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.initOwner(owner);
        a.setHeaderText(null);
        a.setContentText(message);
        style(a);
        Optional<ButtonType> result = a.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Returns null if owner scene is not yet attached. */
    public static Window ownerOf(Node node) {
        if (node.getScene() == null) return null;
        return node.getScene().getWindow();
    }
}