package io.github.realmoieen.cvcviewer.app;

import atlantafx.base.theme.PrimerLight;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import io.github.realmoieen.cvcviewer.log.LoggingBootstrap;
import io.github.realmoieen.cvcviewer.service.file.CertificateFileService;
import io.github.realmoieen.cvcviewer.service.update.UpdateNotifier;
import io.github.realmoieen.cvcviewer.ui.common.AlertFactory;
import io.github.realmoieen.cvcviewer.ui.main.MainController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.security.Security;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CvcViewerApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(CvcViewerApp.class.getName());

    @Override
    public void init() {
        Security.addProvider(new BouncyCastleProvider());
        LoggingBootstrap.init();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, "Uncaught exception on thread " + thread.getName(), throwable);
            Platform.runLater(() -> AlertFactory.showError(throwable, getHostServices()));
        });
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        List<CVCertificate> chain = loadInitialChain();
        if (chain == null) {
            Platform.exit();
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();
        MainController controller = loader.getController();

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/cvc-logo.png")));
        primaryStage.setScene(new Scene(root, 620, 640));
        controller.init(primaryStage, getHostServices(), chain, true);
        primaryStage.show();

        UpdateNotifier.checkForUpdateAsync(getHostServices());
    }

    private List<CVCertificate> loadInitialChain() {
        List<String> args = getParameters().getRaw();
        CertificateFileService fileService = new CertificateFileService();
        try {
            if (!args.isEmpty()) {
                return fileService.load(new File(args.get(0)));
            }
            File file = fileService.chooseOpenFile(null);
            if (file == null) {
                return null;
            }
            return fileService.load(file);
        } catch (Exception e) {
            AlertFactory.showError(e, getHostServices());
            return null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
