package no.lau.vdvil.renderer.video;

import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Stig@Lau.no
 */
public enum ExtensionType {
    aac ("aac"),
    mp3 ("mp3"),
    flac ("flac"),
    mp4 ("mp4"),
    m4a ("m4a"),
    vp09 ("vp09"),
    ts ("ts"),
    webm ("webm"),
    dvl ("dvl.json"),
    kompo ("kompo.json"),
    htmlImagelist ("htmlimagelist"),
    jpg ("jpg"),
    png ("png"),
    txt ("txt"),
    NONE ("");

    List<String> audioList = List.of("aac", "mp3", "flac");
    List<String> videoList = List.of("mp4", "webm", "m4a", "vp09", "htmlImagelist");

    private final String stringValue;
    static org.slf4j.Logger logger = LoggerFactory.getLogger(ExtensionType.class);

    ExtensionType(String stringValue) {
        this.stringValue = stringValue;
    }

    public static ExtensionType typify(String name) {
        try {
            return ExtensionType.valueOf(name.toLowerCase().replace(".xml", "").replace(".json", ""));
        } catch (IllegalArgumentException ex) {
            logger.error("Could not parse extension type of value: {}", name);
            return NONE;
        }
    }

    public boolean isAudio() {
        return audioList.contains(this.stringValue);
    }

    public boolean isVideo() {
        return videoList.contains(this.stringValue);
    }

    public String toString() {
        return stringValue;
    }
}

