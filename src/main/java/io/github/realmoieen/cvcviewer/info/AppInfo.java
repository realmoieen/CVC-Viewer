package io.github.realmoieen.cvcviewer.info;

public class AppInfo {

    public static final String APP_VERSION = getVersion();

    private static String getVersion() {
        Package pkg = AppInfo.class.getPackage();
        String version = (pkg != null) ? pkg.getImplementationVersion() : null;
        return version != null ? version : "DEV";
    }
}
