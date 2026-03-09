package io.github.realmoieen.cvcviewer.ui;

import io.github.realmoieen.cvcviewer.dto.CVCertificate;
import io.github.realmoieen.cvcviewer.util.CVCertificatePEMUtil;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class DialogsUtil {
    public static final JFrame rootFrame = new JFrame("CVC-Viewer");

    static {
        rootFrame.setUndecorated(true);
        rootFrame.setSize(0, 0);
        rootFrame.setLocationRelativeTo(null);
        rootFrame.setVisible(true);
        rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        rootFrame.setIconImage(
                new ImageIcon(CVCViewer.class.getResource("/images/cvc-logo.png")).getImage()
        );
    }

    public static void showError(Component parentComponent, Exception exceptionError) {

        exceptionError.printStackTrace();

        String title = exceptionError.getClass().getName();

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exceptionError.printStackTrace(pw);
        String fullStackTrace = sw.toString();

        showError(parentComponent, fullStackTrace, title);
    }

    public static void showError(Component parentComponent, String stackTrace, String title) {

        JTextArea textArea = new JTextArea(20, 80);
        textArea.setText(stackTrace);
        textArea.setEditable(false);
        textArea.setCaretPosition(0); // Scroll to top
        textArea.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JOptionPane.showMessageDialog(
                parentComponent,
                scrollPane,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void chooseFileAndShowCert(Component parent) {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
        fileChooser.setDialogTitle("Choose file");
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CVC File", "cvcert", "cvreq"));
        fileChooser.setApproveButtonText("Open");
        int showOpenDialog = fileChooser.showOpenDialog(parent);
        if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
            try {
                List<CVCertificate> obj_CVCertificate = CVCertificatePEMUtil.loadCVCertificate(fileChooser.getSelectedFile());
                display(obj_CVCertificate, WindowConstants.EXIT_ON_CLOSE);
            } catch (Exception e) {
                showError(parent, e);
            }
        } else {
            showError(parent, "No file selected.", "Error!");
        }
    }

    /**
     * @param objCertificate the command line arguments
     */
    public static void display(List<CVCertificate> objCertificate, int defaultCloseOperation) {
        rootFrame.dispose();
        /* Create and display the form */
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CVCViewer(objCertificate, defaultCloseOperation).setVisible(true);
            }
        });
    }
}
