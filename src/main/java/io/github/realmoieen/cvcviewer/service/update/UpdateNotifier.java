package io.github.realmoieen.cvcviewer.service.update;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import javafx.application.HostServices;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

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

        TextArea notesArea = new TextArea(info.releaseNotes());
        notesArea.setEditable(false);
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(15);
        notesArea.setPrefColumnCount(60);

        BorderPane content = new BorderPane();
        content.setTop(header);
        content.setCenter(notesArea);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Update Available");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        alert.showAndWait().ifPresent(button -> {
            if (button == ButtonType.YES) {
                hostServices.showDocument(info.releaseUrl());
            }
        });
    }
}
