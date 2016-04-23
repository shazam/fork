package com.shazam.fork.system.io;

public enum FileType {
    TEST ("tests", "xml"),
    RAW_LOG("logcat", "log"),
    JSON_LOG("logcat_json", "json"),
    SCREENSHOT ("screenshot", "png"),
    ANIMATION ("animation", "gif"),
    SCREENRECORD ("screenrecord", "mp4"),
    COVERAGE ("coverage", "ec"),
    ;

    private final String directory;
    private final String suffix;

    FileType(String directory, String suffix) {
        this.directory = directory;
        this.suffix = suffix;
    }

    public String getDirectory() {
        return directory;
    }

    public String getSuffix() {
        return suffix;
    }
}
