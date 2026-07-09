package io.github.realmoieen.cvcviewer.service.update;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import io.github.realmoieen.cvcviewer.ui.common.AppIcons;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UpdateNotifier {

    private static final Logger LOGGER = Logger.getLogger(UpdateNotifier.class.getName());

    private UpdateNotifier() {
    }

    public static void checkForUpdateAsync(HostServices hostServices) {
        Task<Optional<UpdateInfo>> task = new Task<>() {
            @Override
            protected Optional<UpdateInfo> call() throws Exception {
                return UpdateService.fetchAvailableUpdate();
            }
        };
        task.setOnSucceeded(e -> task.getValue().ifPresent(info -> showUpdateDialog(info, hostServices)));
        task.setOnFailed(e -> LOGGER.log(Level.WARNING, "Update check failed", task.getException()));

        Thread thread = new Thread(task, "update-check");
        thread.setDaemon(true);
        thread.start();
    }

    private static void showUpdateDialog(UpdateInfo info, HostServices hostServices) {
        Label header = new Label(
                "A new version of CVC-Viewer is available!\n\n"
                        + "Current Version: " + AppInfo.APP_VERSION + "\n"
                        + "Latest Version: " + info.latestVersion() + "\n\n"
                        + "What's New:");

        WebView notesView = new WebView();
        notesView.setPrefSize(560, 360);
        // Links clicked inside the WebView would otherwise navigate the chrome-less embedded
        // browser itself (no back/forward UI) - cancel that and open in the real browser instead.
        // loadContent() below never sets a location, so this only fires for actual link clicks.
        notesView.getEngine().locationProperty().addListener((obs, oldLoc, newLoc) -> {
            if (newLoc != null && (newLoc.startsWith("http://") || newLoc.startsWith("https://"))) {
                notesView.getEngine().getLoadWorker().cancel();
                hostServices.showDocument(newLoc);
            }
        });
        notesView.getEngine().loadContent(ReleaseNotesRenderer.toHtml(info.releaseNotes()));

        BorderPane content = new BorderPane();
        content.setTop(header);
        content.setCenter(notesView);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Update Available");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        AppIcons.applyTo(alert);

        alert.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                hostServices.showDocument(info.releaseUrl());
            }
        });
    }
}
