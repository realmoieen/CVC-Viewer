package io.github.realmoieen.cvcviewer.ui.about;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutController {

    private static final String GITHUB_URL = "https://github.com/realmoieen/CVC-Viewer";

    @FXML
    private Label versionLabel;
    @FXML
    private Hyperlink githubLink;

    private HostServices hostServices;

    @FXML
    private void initialize() {
        versionLabel.setText("Version: " + AppInfo.APP_VERSION);
        githubLink.setText(GITHUB_URL);
        githubLink.setOnAction(e -> {
            if (hostServices != null) {
                hostServices.showDocument(GITHUB_URL);
            }
        });
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @FXML
    private void onClose() {
        ((Stage) versionLabel.getScene().getWindow()).close();
    }
}
