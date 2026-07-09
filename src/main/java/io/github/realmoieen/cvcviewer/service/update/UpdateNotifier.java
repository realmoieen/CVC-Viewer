package io.github.realmoieen.cvcviewer.service.update;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import io.github.realmoieen.cvcviewer.service.settings.AppSettings;
import io.github.realmoieen.cvcviewer.service.settings.SettingsService;
import io.github.realmoieen.cvcviewer.ui.common.AlertFactory;
import io.github.realmoieen.cvcviewer.ui.common.AppIcons;
import javafx.application.HostServices;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class UpdateNotifier {

    private static final Logger LOGGER = Logger.getLogger(UpdateNotifier.class.getName());
    private static final Integer DEFAULT_PAUSE_DAYS = 7;

    private UpdateNotifier() {
    }

    public static void checkForUpdateAsync(HostServices hostServices) {
        if (SettingsService.load().isUpdateNotificationsPaused()) {
            LOGGER.log(Level.FINE, "Update notifications are paused, skipping check");
            return;
        }

        runCheck(
                result -> result.ifPresent(info -> showUpdateDialog(info, hostServices)),
                ex -> LOGGER.log(Level.WARNING, "Update check failed", ex));
    }

    /**
     * Triggered from the Help menu: unlike the silent startup check, this always hits the
     * network (ignores any active pause) and reports its outcome either way.
     */
    public static void checkForUpdateManually(HostServices hostServices) {
        runCheck(
                result -> {
                    if (result.isPresent()) {
                        showUpdateDialog(result.get(), hostServices);
                    } else {
                        AlertFactory.showInfo("No Updates Available",
                                "You already have the latest version (CVC-Viewer " + AppInfo.APP_VERSION + ").");
                    }
                },
                ex -> {
                    LOGGER.log(Level.WARNING, "Manual update check failed", ex);
                    AlertFactory.showError("Failed to check for updates:\n" + ex.getMessage(), "Update Check Failed");
                });
    }

    private static void runCheck(Consumer<Optional<UpdateInfo>> onSuccess, Consumer<Throwable> onFailure) {
        Task<Optional<UpdateInfo>> task = new Task<>() {
            @Override
            protected Optional<UpdateInfo> call() throws Exception {
                return UpdateService.fetchAvailableUpdate();
            }
        };
        task.setOnSucceeded(e -> onSuccess.accept(task.getValue()));
        task.setOnFailed(e -> onFailure.accept(task.getException()));

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

        CheckBox pauseCheckBox = new CheckBox("Pause update notifications for:");
        ComboBox<Integer> pauseDaysCombo = new ComboBox<>(FXCollections.observableArrayList(1, 7, 14));
        pauseDaysCombo.setValue(DEFAULT_PAUSE_DAYS);
        pauseDaysCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer days) {
                return days == null ? "" : days + (days == 1 ? " day" : " days");
            }

            @Override
            public Integer fromString(String text) {
                return DEFAULT_PAUSE_DAYS;
            }
        });
        pauseDaysCombo.disableProperty().bind(pauseCheckBox.selectedProperty().not());

        HBox pauseBox = new HBox(8, pauseCheckBox, pauseDaysCombo);
        pauseBox.setAlignment(Pos.CENTER_LEFT);
        pauseBox.setPadding(new Insets(10, 0, 0, 0));

        BorderPane content = new BorderPane();
        content.setTop(header);
        content.setCenter(notesView);
        content.setBottom(pauseBox);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Update Available");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(content);
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        AppIcons.applyTo(alert);

        Optional<ButtonType> result = alert.showAndWait();

        if (pauseCheckBox.isSelected()) {
            pauseNotifications(pauseDaysCombo.getValue());
        }

        result.ifPresent(button -> {
            if (button == ButtonType.YES) {
                hostServices.showDocument(info.releaseUrl());
            }
        });
    }

    private static void pauseNotifications(int days) {
        AppSettings settings = SettingsService.load();
        settings.setUpdateNotificationsPausedUntilEpochMilli(
                System.currentTimeMillis() + Duration.ofDays(days).toMillis());
        SettingsService.save(settings);
        LOGGER.log(Level.INFO, "Update notifications paused for {0} day(s)", days);
    }
}
