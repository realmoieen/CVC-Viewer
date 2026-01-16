package com.moieen.cvc.gui.cvcviewer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.security.Security;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Moieen Abbas
 * @version 1.0
 */
public class Lunch {
    public static String APP_VERSION="2.0";
    /**
     * Main function.
     *
     * @param args
     */
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        if (args.length < 1) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
            fileChooser.setDialogTitle("Choose file");
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CVC File", "cvcert", "cvreq"));
            fileChooser.setApproveButtonText("Open");
            int showOpenDialog = fileChooser.showOpenDialog(null);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                try {
                    List<CVCertificate> obj_CVCertificate = loadCVCertificate(fileChooser.getSelectedFile());
                    CVCViewer.display(obj_CVCertificate, WindowConstants.EXIT_ON_CLOSE);
                } catch (Exception e) {
                    ErrorDialogUtil.showError(null,e);
                }
            } else {
                ErrorDialogUtil.showError(null,"No file selected.", "Error!");
            }
        } else {
            try {
                List<CVCertificate> obj_CVCertificate = loadCVCertificate(new File(args[0]));
                CVCViewer.display(obj_CVCertificate, WindowConstants.EXIT_ON_CLOSE);
            } catch (Exception e) {
                ErrorDialogUtil.showError(null,e);
            }
        }
    }

    /**
     * Loads CV certificate from file.
     *
     * @param certificateFile Certificate file.
     * @return CV certificate.
     */
    private static List<CVCertificate> loadCVCertificate(File certificateFile) throws Exception {
        Objects.requireNonNull(certificateFile, "Certificate file is null");
        if (!certificateFile.exists()) {
            throw new Exception("File does not exist: " + certificateFile.getAbsolutePath());
        }
        List<CVCertificate> cvCertificates = CVCertificatePEMUtil.loadCertificates(certificateFile);
        return cvCertificates;
    }
}
