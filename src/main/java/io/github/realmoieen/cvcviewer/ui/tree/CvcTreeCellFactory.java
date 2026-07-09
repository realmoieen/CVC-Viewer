package io.github.realmoieen.cvcviewer.ui.tree;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

public final class CvcTreeCellFactory implements Callback<TreeView<CVCertificate>, TreeCell<CVCertificate>> {

    private static final Image VALID_ICON = new Image(CvcTreeCellFactory.class.getResourceAsStream("/images/cvc-valid.png"));
    private static final Image WARNING_ICON = new Image(CvcTreeCellFactory.class.getResourceAsStream("/images/cvc-warning.png"));

    @Override
    public TreeCell<CVCertificate> call(TreeView<CVCertificate> treeView) {
        return new TreeCell<>() {
            @Override
            protected void updateItem(CVCertificate cert, boolean empty) {
                super.updateItem(cert, empty);
                if (empty || cert == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                setText(cert.getCertHolderRef());

                ImageView icon = new ImageView(isValid(cert) ? VALID_ICON : WARNING_ICON);
                icon.setFitWidth(20);
                icon.setFitHeight(16);
                setGraphic(icon);
            }
        };
    }

    private boolean isValid(CVCertificate cert) {
        try {
            cert.checkValidity();
            return true;
        } catch (CertificateNotYetValidException | CertificateExpiredException e) {
            return false;
        }
    }
}
