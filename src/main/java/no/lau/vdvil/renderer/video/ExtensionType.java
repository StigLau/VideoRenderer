package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no
 */
public enum ExtensionType {
    aac ("aac"),
    mp3 ("mp3"),
    mp4 ("mp4"),
    snippet("snippet"), //The snippet is pulled from a longer video. This ExtensionType is used by the Splitter functionality
    webm ("webm"),
    flac ("flac"),
    dvl ("dvl.xml"),
    kompo ("kompo.xml"),
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
        return this == mp4 || this == webm;
    }

    public String toString() {
        return stringValue;
    }
}
