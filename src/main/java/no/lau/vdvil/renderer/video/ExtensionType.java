package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no
 */
public enum ExtensionType {
    aac ("aac"),
    mp3 ("mp3"),
    mp4 ("mp4"),
    webm ("webm"),
    flac ("flac"),
    dvl ("dvl.json"),
    kompo ("kompo.json"),
    htmlImagelist ("htmlimagelist"),
    jpg ("jpg"),
    png ("png"),
    NONE ("");

    private final String stringValue;

    ExtensionType(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isAudio() {
        return this == aac || this == mp3 || this == flac;
    }

    public boolean isVideo() {
        return this == mp4 || this == webm || this == htmlImagelist;
    }

    public String toString() {
        return stringValue;
    }
}

