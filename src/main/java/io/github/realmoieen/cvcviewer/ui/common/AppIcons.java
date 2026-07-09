package io.github.realmoieen.cvcviewer.ui.common;

import io.github.realmoieen.cvcviewer.ui.theme.ThemeManager;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public final class AppIcons {

    private static final Image APP_ICON = new Image(AppIcons.class.getResourceAsStream("/images/cvc-logo.png"));

    private AppIcons() {
    }

    public static void applyTo(Stage stage) {
        stage.getIcons().add(APP_ICON);
    }

    public static void applyTo(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        applyTo(stage);
        // Dialog's own onShown fires once its Stage's native peer is realized - the Stage's own
        // onShown isn't reliably usable here since Dialog owns the show()/showAndWait() call.
        dialog.setOnShown(e -> ThemeManager.applyTitleBar(stage));
    }
}
