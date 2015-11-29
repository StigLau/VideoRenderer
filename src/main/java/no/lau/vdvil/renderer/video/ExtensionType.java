package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no
 */
public enum ExtensionType {
    mp3 ("mp3"), mp4 ("mp4"), webm ("webm"), flac ("flac"), dvl ("dvl.xml"), kompo ("kompo.xml"), NONE ("");

    private final String stringValue;

    ExtensionType(String stringValue) {
        this.stringValue = stringValue;
    }

    public boolean isAudio() {
        return this == mp3 || this == flac;
    }

    public boolean isVideo() {
        return this == mp4 || this == webm;
    }

    public String toString() {
        return stringValue;
    }
}
