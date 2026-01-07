package com.moieen.cvc.gui.cvcviewer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.security.Security;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Moieen Abbas
 * @version 1.0
 */
public class Lunch {

    /**
     * Main function.
     *
     * @param args
     */
    public static void main(String[] args) {
        Security.addProvider(new BouncyCastleProvider());
        if (args.length < 1) {
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
            fileChooser.setDialogTitle("Choose file");
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CVC File", "cvcert", "cvreq"));
            fileChooser.setApproveButtonText("Open");
            int showOpenDialog = fileChooser.showOpenDialog(null);
            if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
                try {
                    CVCertificate obj_CVCertificate = loadCVCertificate(fileChooser.getSelectedFile());
                    CVCViewer.display(obj_CVCertificate);
                } catch (Exception e) {
                    showError(e);
                }
            } else {
                showError("No file selected.", "Error!");
            }
        } else {
            try {
                CVCertificate obj_CVCertificate = loadCVCertificate(new File(args[0]));
                CVCViewer.display(obj_CVCertificate);
            } catch (Exception e) {
                showError(e);
            }
        }

    }

    /**
     * Loads CV certificate from file.
     *
     * @param certificateFile Certificate file.
     * @return CV certificate.
     */
    private static CVCertificate loadCVCertificate(File certificateFile) throws Exception {
        Objects.requireNonNull(certificateFile, "Certificate file is null");
        if (!certificateFile.exists()) {
            throw new Exception("File does not exist: " + certificateFile.getAbsolutePath());
        }
        List<CVCertificate> cvCertificates = CertificateParser.loadCertificates(certificateFile);
        return cvCertificates.get(0);
    }

    public static void showError(Exception exceptionError) {
        String errorMessage = "Message: " + exceptionError.getMessage()
                + "\nStackTrace: " + Arrays.toString(exceptionError.getStackTrace()).replace(",", "\n");
        exceptionError.printStackTrace();
        String title = exceptionError.getClass().getName();
        showError(errorMessage, title);
    }

    public static void showError(String errorMessage, String title) {
        JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }
}
