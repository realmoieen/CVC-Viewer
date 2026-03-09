package io.github.realmoieen.cvcviewer;

import io.github.realmoieen.cvcviewer.dto.CVCertificate;
import io.github.realmoieen.cvcviewer.ui.DialogsUtil;
import io.github.realmoieen.cvcviewer.util.CVCertificatePEMUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.io.File;
import java.security.Security;
import java.util.List;

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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        if (args.length < 1) {
            DialogsUtil.chooseFileAndShowCert(DialogsUtil.rootFrame);
        } else {
            try {
                List<CVCertificate> obj_CVCertificate = CVCertificatePEMUtil.loadCVCertificate(new File(args[0]));
                DialogsUtil.display(obj_CVCertificate, WindowConstants.EXIT_ON_CLOSE);
            } catch (Exception e) {
                DialogsUtil.showError(DialogsUtil.rootFrame, e);
                DialogsUtil.rootFrame.dispose();
                System.exit(0);
            }
        }
    }
}
