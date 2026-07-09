package io.github.realmoieen.cvcviewer.service.update;

import io.github.realmoieen.cvcviewer.info.AppInfo;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public final class UpdateService {

    private static final String GITHUB_API = "https://api.github.com/repos/realmoieen/CVC-Viewer/releases/latest";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private UpdateService() {
    }

    public static Optional<UpdateInfo> fetchAvailableUpdate() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(GITHUB_API))
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("GitHub API returned HTTP " + response.statusCode());
        }

        JSONObject obj = new JSONObject(response.body());
        String latestVersion = obj.getString("tag_name").replace("v", "");
        String releaseUrl = obj.getString("html_url");
        String releaseNotes = obj.optString("body", "No release notes provided.");

        if (isNewerVersion(latestVersion, AppInfo.APP_VERSION)) {
            return Optional.of(new UpdateInfo(latestVersion, releaseUrl, releaseNotes));
        }
        return Optional.empty();
    }

    static boolean isNewerVersion(String latest, String current) {
        return compareVersion(latest, current) > 0;
    }

    private static int compareVersion(String v1, String v2) {
        String[] a1 = v1.split("\\.");
        String[] a2 = v2.split("\\.");

        int len = Math.max(a1.length, a2.length);

        for (int i = 0; i < len; i++) {
            int n1 = i < a1.length ? parseComponent(a1[i]) : 0;
            int n2 = i < a2.length ? parseComponent(a2[i]) : 0;

            if (n1 != n2) return Integer.compare(n1, n2);
        }
        return 0;
    }

    private static int parseComponent(String component) {
        try {
            return Integer.parseInt(component);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
