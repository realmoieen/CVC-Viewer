package com.moieen.cvc.gui.cvcviewer;

import com.secunet.testbedutils.cvc.cvcertificate.CVCertificate;
import com.secunet.testbedutils.cvc.cvcertificate.DataBuffer;
import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

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
            JFileChooser fileChooser = new JFileChooser("/");
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
        CVCertificate result = null;

        if (certificateFile == null) {
            return null;
        }

        if (!certificateFile.exists()) {
            throw new Exception("File does not exist: " + certificateFile.getAbsolutePath());
//            return null;
        }

        DataBuffer rawCert = null;
        try {
            rawCert = DataBuffer.readFromFile(certificateFile.getAbsolutePath());
            result = new CVCertificate(rawCert);
            System.out.println("Loaded " + result.getCertHolderRef() + " from " + certificateFile.getAbsolutePath());
        } catch (IOException e) {
            throw new Exception("Unable to read CV certificate from file:" + e.getMessage());
        } catch (Exception e) {
            rawCert = DataBuffer.decodeB64(new String(rawCert.toByteArray()));
            try {
                result = new CVCertificate(rawCert);
            } catch (Exception ex) {
                throw new Exception("Unable to read CV certificate from file:" + e.getMessage());

            }
        }

        return result;
    }

    public static void showError(Exception exceptionError) {
        String errorMessage = "Message: " + exceptionError.getMessage()
                + "\nStackTrace: " + Arrays.toString(exceptionError.getStackTrace()).replace(",", "\n");
        String title = exceptionError.getClass().getName();
        showError(errorMessage, title);
    }

    public static void showError(String errorMessage, String title) {
        JOptionPane.showMessageDialog(null, errorMessage, title, JOptionPane.ERROR_MESSAGE);
    }
}
