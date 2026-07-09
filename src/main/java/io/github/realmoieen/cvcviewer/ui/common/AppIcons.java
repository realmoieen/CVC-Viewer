package io.github.realmoieen.cvcviewer.ui.common;

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
        applyTo((Stage) dialog.getDialogPane().getScene().getWindow());
    }
}
