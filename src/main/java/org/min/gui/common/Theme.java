package org.min.gui.common;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * CSS class-name constants and helper methods for applying them.
 * All styling lives in styles.css; this class provides type-safe access.
 */
public final class Theme {

    private Theme() {}

    // ── CSS resource location ─────────────────────────────────
    public static final String CSS_PATH = "/org/min/gui/styles.css";

    // ── Button classes ────────────────────────────────────────
    public static final String BTN_PRIMARY   = "btn-primary";
    public static final String BTN_SECONDARY = "btn-secondary";
    public static final String BTN_DANGER    = "btn-danger";
    public static final String BTN_GHOST     = "btn-ghost";
    public static final String BTN_START     = "btn-start";
    public static final String BTN_STOP      = "btn-stop";

    // ── Label classes ─────────────────────────────────────────
    public static final String LABEL_TITLE     = "label-title";
    public static final String LABEL_SECTION   = "label-section";
    public static final String LABEL_BODY      = "label-body";
    public static final String LABEL_SECONDARY = "label-secondary";
    public static final String LABEL_MUTED     = "label-muted";
    public static final String LABEL_RUNNING   = "label-running";
    public static final String LABEL_STOPPED   = "label-stopped";
    public static final String LABEL_VERSION   = "label-version";

    // ── Component classes ─────────────────────────────────────
    public static final String SERVER_CARD   = "server-card";
    public static final String CONSOLE_AREA  = "console-area";
    public static final String COMMAND_INPUT = "command-input";
    public static final String PLUGINS_LIST  = "plugins-list";

    // ── Apply helpers ─────────────────────────────────────────
    public static void applyPrimary  (Button b) { setClass(b, BTN_PRIMARY);   }
    public static void applySecondary(Button b) { setClass(b, BTN_SECONDARY); }
    public static void applyDanger   (Button b) { setClass(b, BTN_DANGER);    }
    public static void applyGhost    (Button b) { setClass(b, BTN_GHOST);     }

    public static void applyStart(Button b) {
        b.getStyleClass().removeAll(BTN_STOP, BTN_START, BTN_PRIMARY, BTN_SECONDARY);
        b.getStyleClass().add(BTN_START);
    }

    public static void applyStop(Button b) {
        b.getStyleClass().removeAll(BTN_STOP, BTN_START, BTN_PRIMARY, BTN_SECONDARY);
        b.getStyleClass().add(BTN_STOP);
    }

    public static void applyRunning(Label l) {
        l.getStyleClass().removeAll(LABEL_STOPPED, LABEL_RUNNING, LABEL_SECONDARY, LABEL_MUTED);
        l.getStyleClass().add(LABEL_RUNNING);
    }

    public static void applyStopped(Label l) {
        l.getStyleClass().removeAll(LABEL_STOPPED, LABEL_RUNNING, LABEL_SECONDARY, LABEL_MUTED);
        l.getStyleClass().add(LABEL_STOPPED);
    }

    public static void applyConsole     (TextArea  a) { a.getStyleClass().add(CONSOLE_AREA);  }
    public static void applyCommandInput(TextField f) { f.getStyleClass().add(COMMAND_INPUT); }

    // ── Internal helper ───────────────────────────────────────
    private static void setClass(Button b, String cls) {
        b.getStyleClass().removeAll(BTN_PRIMARY, BTN_SECONDARY, BTN_DANGER, BTN_GHOST);
        b.getStyleClass().add(cls);
    }
}
