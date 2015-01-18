package com.shazam.fork.io;

public enum FileType {
    TEST ("tests", "xml"),
    RAW_LOG("logcat", "log"),
    JSON_LOG("logcat_json", "json"),
    SCREENSHOT ("screenshot", "png"),
    SCREENRECORD ("screenrecord", "mp4"),
    ;

    private String directory;
    private String suffix;

    private FileType(String directory, String suffix) {
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
