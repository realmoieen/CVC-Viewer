package io.github.realmoieen.cvcviewer.ui.about;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class AboutController {

    private static final String WEBSITE_URL = "https://realmoieen.github.io/CVC-Viewer/";

    @FXML
    private Label versionLabel;
    @FXML
    private Hyperlink websiteLink;

    private HostServices hostServices;

    @FXML
    private void initialize() {
        versionLabel.setText("Version: " + AppInfo.APP_VERSION);
        websiteLink.setText(WEBSITE_URL);
        websiteLink.setOnAction(e -> {
            if (hostServices != null) {
                hostServices.showDocument(WEBSITE_URL);
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
