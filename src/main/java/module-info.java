module org.min.javafxcoding {
    // ── JavaFX ────────────────────────────────────────────────
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;

    // ── JDK modules ───────────────────────────────────────────
    requires java.logging;
    requires java.desktop;
    requires java.net.http;
    requires java.prefs;

    // ── Third-party ───────────────────────────────────────────
    requires com.google.gson;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires kotlin.stdlib;

    // ── Opens / Exports ───────────────────────────────────────
    opens org.min.app to javafx.graphics, javafx.fxml;
    opens org.min.gui to javafx.fxml, javafx.graphics;
    opens org.min.gui.common to javafx.fxml;
    opens org.min.gui.dialogs to javafx.fxml, javafx.graphics;
    opens org.min.gui.panels.server_manager_panel to javafx.fxml;
    opens org.min.gui.panels.server_manager_panel.cards to javafx.fxml;
    opens org.min.settings to javafx.base;

    exports org.min.app;
    exports org.min.gui;
    exports org.min.gui.common;
    exports org.min.gui.panels.server_manager_panel;
    exports org.min.gui.panels.server_manager_panel.cards;
    exports org.min.settings;
    exports org.min.logging;
}