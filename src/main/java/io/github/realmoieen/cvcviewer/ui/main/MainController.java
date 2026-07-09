package io.github.realmoieen.cvcviewer.ui.main;

import de.bsi.testbedutils.cvc.cvcertificate.exception.CVBaseException;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import io.github.realmoieen.cvcviewer.info.AppInfo;
import io.github.realmoieen.cvcviewer.service.file.CertificateFileService;
import io.github.realmoieen.cvcviewer.service.settings.AppSettings;
import io.github.realmoieen.cvcviewer.service.settings.SettingsService;
import io.github.realmoieen.cvcviewer.service.settings.ThemePreference;
import io.github.realmoieen.cvcviewer.service.update.UpdateNotifier;
import io.github.realmoieen.cvcviewer.ui.about.AboutController;
import io.github.realmoieen.cvcviewer.ui.common.AlertFactory;
import io.github.realmoieen.cvcviewer.ui.common.AppIcons;
import io.github.realmoieen.cvcviewer.ui.detail.DetailController;
import io.github.realmoieen.cvcviewer.ui.path.PathController;
import io.github.realmoieen.cvcviewer.ui.theme.ThemeManager;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainController {

    private final CertificateFileService fileService = new CertificateFileService();

    @FXML
    private MenuItem openMenuItem;
    @FXML
    private MenuItem saveAsMenuItem;
    @FXML
    private MenuItem checkForUpdatesMenuItem;
    @FXML
    private MenuItem aboutMenuItem;
    @FXML
    private RadioMenuItem lightThemeMenuItem;
    @FXML
    private RadioMenuItem darkThemeMenuItem;
    @FXML
    private RadioMenuItem systemThemeMenuItem;

    @FXML
    private GeneralController generalViewController;
    @FXML
    private DetailController detailViewController;
    @FXML
    private PathController pathViewController;

    private Stage stage;
    private HostServices hostServices;
    private List<CVCertificate> currentChain;
    private CVCertificate currentCertificate;
    private boolean exitOnClose;

    public void init(Stage stage, HostServices hostServices, List<CVCertificate> chain, boolean exitOnClose) {
        this.stage = stage;
        this.hostServices = hostServices;
        this.exitOnClose = exitOnClose;

        openMenuItem.setOnAction(e -> onOpen());
        saveAsMenuItem.setOnAction(e -> onSaveAs());
        aboutMenuItem.setOnAction(e -> onAbout());
        checkForUpdatesMenuItem.setOnAction(e -> UpdateNotifier.checkForUpdateManually(hostServices));
        initThemeMenu();

        pathViewController.setOnViewCertificate(this::openSubChainWindow);

        stage.setOnCloseRequest(this::onCloseRequest);
        stage.getScene().setOnDragOver(this::onDragOver);
        stage.getScene().setOnDragDropped(this::onDragDropped);

        setCertificateDetail(chain);
    }

    private void setCertificateDetail(List<CVCertificate> chain) {
        this.currentChain = chain;
        this.currentCertificate = chain.get(0);

        generalViewController.setCertificate(currentCertificate);
        detailViewController.setCertificate(currentCertificate);
        pathViewController.setChain(currentChain);

        stage.setTitle("CV Certificate Viewer - " + AppInfo.APP_VERSION);
        stage.getIcons().setAll(new Image(
                getClass().getResourceAsStream(generalViewController.isValid(currentCertificate)
                        ? "/images/cvc-valid.png" : "/images/cvc-warning.png")));
    }

    private void initThemeMenu() {
        ThemePreference current = SettingsService.load().getTheme();
        switch (current) {
            case LIGHT -> lightThemeMenuItem.setSelected(true);
            case DARK -> darkThemeMenuItem.setSelected(true);
            case SYSTEM -> systemThemeMenuItem.setSelected(true);
        }

        lightThemeMenuItem.setOnAction(e -> onThemeSelected(ThemePreference.LIGHT));
        darkThemeMenuItem.setOnAction(e -> onThemeSelected(ThemePreference.DARK));
        systemThemeMenuItem.setOnAction(e -> onThemeSelected(ThemePreference.SYSTEM));
    }

    private void onThemeSelected(ThemePreference theme) {
        AppSettings settings = SettingsService.load();
        settings.setTheme(theme);
        SettingsService.save(settings);
        ThemeManager.apply(theme);
    }

    @FXML
    private void onOk() {
        stage.close();
        if (exitOnClose) {
            Platform.exit();
        }
    }

    @FXML
    private void onSaveAs() {
        onCopyToFile();
    }

    @FXML
    private void onCopyToFile() {
        if (currentCertificate == null) {
            return;
        }

        List<CertificateFileService.SaveFormat> formats = fileService.availableFormats(currentChain.size());
        ChoiceDialog<CertificateFileService.SaveFormat> dialog = new ChoiceDialog<>(formats.get(0), formats);
        dialog.setTitle("Save As");
        dialog.setHeaderText(null);
        dialog.setContentText("Select output format:");
        AppIcons.applyTo(dialog);

        Optional<CertificateFileService.SaveFormat> chosen = dialog.showAndWait();
        if (chosen.isEmpty()) {
            return;
        }

        File file = fileService.chooseSaveFile(stage, currentCertificate,chosen.get());
        if (file == null) {
            return;
        }

        if (file.exists() && !AlertFactory.confirmOverwrite(file.getName())) {
            return;
        }

        try {
            fileService.save(file, chosen.get(), currentCertificate, currentChain);
            AlertFactory.showInfo("Saved", "Certificate saved successfully");
        } catch (CVBaseException | IOException ex) {
            AlertFactory.showError("Failed to save certificate:\n" + ex.getMessage(), "File Not Saved!");
        }
    }

    private void onOpen() {
        File file = fileService.chooseOpenFile(stage);
        if (file == null) {
            return;
        }
        openFile(file);
    }

    private void openFile(File file) {
        try {
            List<CVCertificate> chain = fileService.load(file);
            ViewerWindowFactory.open(chain, hostServices, true);
        } catch (Exception ex) {
            AlertFactory.showError(ex, hostServices);
        }
    }

    private void openSubChainWindow(List<CVCertificate> chain) {
        try {
            ViewerWindowFactory.open(chain, hostServices, false);
        } catch (IOException ex) {
            AlertFactory.showError(ex, hostServices);
        }
    }

    private void onAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AboutView.fxml"));
            Parent root = loader.load();
            AboutController controller = loader.getController();
            controller.setHostServices(hostServices);

            Stage aboutStage = new Stage();
            aboutStage.setTitle("About CVC-Viewer");
            aboutStage.initModality(Modality.WINDOW_MODAL);
            aboutStage.initOwner(stage);
            aboutStage.setResizable(false);
            aboutStage.setScene(new Scene(root));
            AppIcons.applyTo(aboutStage);
            aboutStage.showAndWait();
        } catch (IOException ex) {
            AlertFactory.showError(ex, hostServices);
        }
    }

    private void onCloseRequest(WindowEvent event) {
        if (exitOnClose) {
            Platform.exit();
        }
    }

    private void onDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void onDragDropped(DragEvent event) {
        List<File> files = event.getDragboard().getFiles();
        boolean success = false;
        if (files != null && !files.isEmpty()) {
            openFile(files.get(0));
            success = true;
        }
        event.setDropCompleted(success);
        event.consume();
    }
}
