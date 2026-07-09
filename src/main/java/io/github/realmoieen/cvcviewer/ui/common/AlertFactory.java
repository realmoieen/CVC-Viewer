package io.github.realmoieen.cvcviewer.ui.common;

import io.github.realmoieen.cvcviewer.service.errorreport.GitHubIssueReporter;
import javafx.application.HostServices;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class AlertFactory {

    private static final Logger LOGGER = Logger.getLogger(AlertFactory.class.getName());

    private AlertFactory() {
    }

    public static void showError(Throwable throwable) {
        showError(throwable, null);
    }

    public static void showError(Throwable throwable, HostServices hostServices) {
        LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
        StringWriter sw = new StringWriter();
        throwable.printStackTrace(new PrintWriter(sw));
        showError(sw.toString(), throwable.getClass().getSimpleName(), () -> {
            if (hostServices != null) {
                hostServices.showDocument(GitHubIssueReporter.buildIssueUrl(throwable));
            }
        });
    }

    public static void showError(String message, String title) {
        showError(message, title, null);
    }

    private static void showError(String message, String title, Runnable onReportIssue) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(firstLine(message));

        TextArea textArea = new TextArea(message);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expandableContent = new GridPane();
        expandableContent.setMaxWidth(Double.MAX_VALUE);
        expandableContent.add(new Label("Details:"), 0, 0);
        expandableContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expandableContent);

        if (onReportIssue != null) {
            ButtonType reportIssue = new ButtonType("Report Issue...", ButtonBar.ButtonData.HELP_2);
            alert.getButtonTypes().add(reportIssue);
            alert.showAndWait().ifPresent(button -> {
                if (button == reportIssue) {
                    onReportIssue.run();
                }
            });
        } else {
            alert.showAndWait();
        }
    }

    public static boolean confirmOverwrite(String fileName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "File already exists!\nDo you want to overwrite " + fileName + "?",
                ButtonType.YES, ButtonType.NO);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private static String firstLine(String message) {
        int idx = message.indexOf('\n');
        return idx > 0 ? message.substring(0, idx) : message;
    }
}
