package io.github.realmoieen.cvcviewer.service.file;

import de.bsi.testbedutils.cvc.cvcertificate.exception.CVBaseException;
import io.github.realmoieen.cvcviewer.core.model.CVCertificate;
import io.github.realmoieen.cvcviewer.core.parser.CVCertificatePEMUtil;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class CertificateFileService {

    public enum SaveFormat {
        BINARY_DER("Binary DER"),
        BASE64("Base64"),
        BASE64_CHAIN("Base64 Chain (Comma Separated)"),
        PEM("PEM"),
        PEM_CHAIN("PEM Chain");

        private final String label;

        SaveFormat(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public File chooseOpenFile(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose file");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CVC File", "*.cvcert", "*.cvreq"));
        return chooser.showOpenDialog(owner);
    }

    public List<CVCertificate> load(File file) throws Exception {
        return CVCertificatePEMUtil.loadCVCertificate(file);
    }

    public File chooseSaveFile(Window owner, CVCertificate certificate, SaveFormat saveFormat) {
        String extension = certificate.isReqCert() ? ".cvreq" : ".cvcert";
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Certificate");
        chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        chooser.getExtensionFilters().add(new  FileChooser.ExtensionFilter(saveFormat.label(), extension));
        String fileName = certificate.getCertHolderRef() != null ? certificate.getCertHolderRef() : "certificate";
        fileName = fileName + extension;
        chooser.setInitialFileName(fileName);
        return chooser.showSaveDialog(owner);
    }

    public List<SaveFormat> availableFormats(int chainSize) {
        if (chainSize > 1) {
            return List.of(SaveFormat.BINARY_DER, SaveFormat.BASE64, SaveFormat.BASE64_CHAIN, SaveFormat.PEM, SaveFormat.PEM_CHAIN);
        }
        return List.of(SaveFormat.BINARY_DER, SaveFormat.BASE64, SaveFormat.PEM);
    }

    public void save(File file, SaveFormat format, CVCertificate certificate, List<CVCertificate> chain) throws CVBaseException, IOException {
        byte[] data = switch (format) {
            case BINARY_DER -> CVCertificatePEMUtil.encodeCertificateToDer(certificate);
            case BASE64 -> CVCertificatePEMUtil.encodeCertificateToBase64(certificate).getBytes(StandardCharsets.UTF_8);
            case BASE64_CHAIN -> CVCertificatePEMUtil.encodeCertificateChainToBase64(chain).getBytes(StandardCharsets.UTF_8);
            case PEM -> CVCertificatePEMUtil.encodeCertificateToPEM(certificate).getBytes(StandardCharsets.UTF_8);
            case PEM_CHAIN -> CVCertificatePEMUtil.encodeCertificateChainToPEM(chain).getBytes(StandardCharsets.UTF_8);
        };
        Files.write(file.toPath(), data);
    }
}
