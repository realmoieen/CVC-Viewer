package io.github.realmoieen.cvcviewer.ui.main;

import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GeneralController {

    private static final Image VALID_ICON = new Image(GeneralController.class.getResourceAsStream("/images/cvc-valid.png"));
    private static final Image WARNING_ICON = new Image(GeneralController.class.getResourceAsStream("/images/cvc-warning.png"));

    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");

    @FXML
    private ImageView statusIcon;
    @FXML
    private Label holderReferenceValue;
    @FXML
    private Label caReferenceValue;
    @FXML
    private Label validFromLabel;
    @FXML
    private Label validFromValue;
    @FXML
    private Label validToLabel;
    @FXML
    private Label validToValue;
    @FXML
    private Label purposeLabel;
    @FXML
    private TextArea purposeTextArea;

    public void setCertificate(CVCertificate certificate) {
        holderReferenceValue.setText(certificate.getCertHolderRef());
        caReferenceValue.setText(certificate.getCertAuthRef());

        boolean valid = isValid(certificate);
        statusIcon.setImage(valid ? VALID_ICON : WARNING_ICON);

        String status = "\n\n" + certificate.getStatusString();

        if (certificate.isReqCert()) {
            if (certificate.hasOuterSignature()) {
                purposeLabel.setText("This is a CVC Authenticated Request with outer signature");
                purposeTextArea.setText("• Authenticated Request" + status);
                validFromLabel.setText("Outer CA Reference:");
                validFromValue.setText(certificate.getOuterAuthRef());
                validToLabel.setText("");
                validToValue.setText("");
            } else {
                purposeLabel.setText("This is a CV Certificate Request with no purpose(s):");
                purposeTextArea.setText("• CV Request" + status);
                validFromLabel.setText("");
                validFromValue.setText("");
                validToLabel.setText("");
                validToValue.setText("");
            }
        } else {
            purposeLabel.setText("This CV Certificate (CVC) is intended for the following purpose(s):");
            validFromLabel.setText("Valid from");
            validToLabel.setText("to");
            if (certificate.getCertHolderAuth() != null && certificate.getCertHolderAuth().getAuth() != null) {
                purposeTextArea.setText("• " + certificate.getCertHolderAuth().getAuth().getRole().name() + status);
            } else {
                purposeTextArea.setText(status);
            }
            if (certificate.getEffDate() != null && certificate.getEffDate().getDate() != null) {
                validFromValue.setText(dateFormat.format(certificate.getEffDate().getDate()));
            }
            if (certificate.getExpDate() != null && certificate.getExpDate().getDate() != null) {
                validToValue.setText(dateFormat.format(certificate.getExpDate().getDate()));
            }
        }
    }

    public boolean isValid(CVCertificate certificate) {
        try {
            certificate.checkValidity();
            return true;
        } catch (CertificateNotYetValidException | CertificateExpiredException e) {
            return false;
        }
    }
}
