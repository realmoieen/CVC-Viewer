package io.github.realmoieen.cvcviewer.ui.main;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public final class ViewerWindowFactory {

    private ViewerWindowFactory() {
    }

    public static Stage open(List<CVCertificate> chain, HostServices hostServices, boolean exitOnClose) throws IOException {
        FXMLLoader loader = new FXMLLoader(ViewerWindowFactory.class.getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        Stage stage = new Stage();
        stage.getIcons().add(new Image(ViewerWindowFactory.class.getResourceAsStream("/images/cvc-logo.png")));
        stage.setScene(new Scene(root, 620, 640));

        controller.init(stage, hostServices, chain, exitOnClose);
        stage.show();
        return stage;
    }
}
