package io.github.realmoieen.cvcviewer.service.settings;

public class AppSettings {

    private ThemePreference theme = ThemePreference.SYSTEM;
    private Long updateNotificationsPausedUntilEpochMilli;

    public ThemePreference getTheme() {
        return theme;
    }

    public void setTheme(ThemePreference theme) {
        this.theme = theme;
    }

    public Long getUpdateNotificationsPausedUntilEpochMilli() {
        return updateNotificationsPausedUntilEpochMilli;
    }

    public void setUpdateNotificationsPausedUntilEpochMilli(Long updateNotificationsPausedUntilEpochMilli) {
        this.updateNotificationsPausedUntilEpochMilli = updateNotificationsPausedUntilEpochMilli;
    }

    public boolean isUpdateNotificationsPaused() {
        return updateNotificationsPausedUntilEpochMilli != null
                && updateNotificationsPausedUntilEpochMilli > System.currentTimeMillis();
    }
}
