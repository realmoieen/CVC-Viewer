package io.github.realmoieen.cvcviewer.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;

public class AboutDialog extends JDialog {

    private static final String GITHUB_URL = "https://github.com/realmoieen/CVC-Viewer";

    public AboutDialog(Frame parent) {
        super(parent, "About CVC-Viewer", true);

        setLayout(new BorderLayout(10,10));
        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(15,15));
        content.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        // Logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon icon = new ImageIcon(
                    getClass().getResource("/images/cvc-logo.png")
            );
            logoLabel.setIcon(icon);
        } catch (Exception e) {
            // ignore if logo missing
        }

        content.add(logoLabel, BorderLayout.WEST);

        // Text Panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("CVC-Viewer");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel version = new JLabel("Version: " + getManifestValue("Implementation-Version"));

        JLabel vendor = new JLabel("Vendor: " + getManifestValue("Implementation-Vendor"));

        JLabel github = new JLabel("<html><a href='" + GITHUB_URL + "'>" + GITHUB_URL + "</a></html>");
        github.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        github.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openGithub();
            }
        });

        JLabel description = new JLabel(
                "<html>Card Verifiable Certificate (CVC) Viewer.<br>" +
                        "A utility to inspect and analyze CVC certificates used in EAC systems.</html>"
        );

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(version);
        textPanel.add(vendor);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(github);
        textPanel.add(Box.createVerticalStrut(10));
        textPanel.add(description);

        content.add(textPanel, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);

        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private String getManifestValue(String key) {
        Package pkg = AboutDialog.class.getPackage();
        if (pkg == null) return "Unknown";

        switch (key) {
            case "Implementation-Version":
                return pkg.getImplementationVersion() != null ?
                        pkg.getImplementationVersion() : "DEV";

            case "Implementation-Vendor":
                return pkg.getImplementationVendor() != null ?
                        pkg.getImplementationVendor() : "Unknown";

            default:
                return "Unknown";
        }
    }

    private void openGithub() {
        try {
            Desktop.getDesktop().browse(new URI(GITHUB_URL));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to open browser.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}