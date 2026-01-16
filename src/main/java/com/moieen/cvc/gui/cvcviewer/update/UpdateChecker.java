package com.moieen.cvc.gui.cvcviewer.update;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.moieen.cvc.gui.cvcviewer.Lunch;
import org.json.JSONObject;

public class UpdateChecker {

    private static final String GITHUB_API =
            "https://api.github.com/repos/realmoieen/CVC-Viewer/releases/latest";

    public static void checkForUpdate(Component paraent) {
        try {
            URL url = new URL(GITHUB_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );

            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
            reader.close();

            JSONObject obj = new JSONObject(json.toString());

            String latestVersion = obj.getString("tag_name").replace("v", "");
            String releaseUrl    = obj.getString("html_url");
            String releaseNotes  = obj.optString("body", "No release notes provided.");

            if (isNewerVersion(latestVersion, Lunch.APP_VERSION)) {
                showUpdateDialog(latestVersion, releaseUrl, releaseNotes, paraent);
            }

        } catch (Exception e) {
            System.err.println("Update check failed: " + e.getMessage());
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        return compareVersion(latest, current) > 0;
    }

    private static int compareVersion(String v1, String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");

        int len = Math.max(a1.length, a2.length);

        for (int i = 0; i < len; i++) {
            int n1 = i < a1.length ? Integer.parseInt(a1[i]) : 0;
            int n2 = i < a2.length ? Integer.parseInt(a2[i]) : 0;

            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    private static void showUpdateDialog(String latestVersion, String url, String releaseNotes,Component paraent) {

        JLabel header = new JLabel(
                "<html><b>A new version of CVC-Viewer is available!</b><br><br>" +
                        "Current Version: " + Lunch.APP_VERSION + "<br>" +
                        "Latest Version: " + latestVersion + "<br><br>" +
                        "What's New:</html>"
        );

        JTextArea notesArea = new JTextArea(15, 80);
        notesArea.setText(releaseNotes);
        notesArea.setEditable(false);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        notesArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(notesArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.add(header, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
                paraent,
                panel,
                "Update Available",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().browse(new URL(url).toURI());
            } catch (Exception ignored) {}
        }
    }
}
