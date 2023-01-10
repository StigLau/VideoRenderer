package no.lau.vdvil.renderer.video;

import org.slf4j.LoggerFactory;

/**
 * @author Stig@Lau.no
 */
public enum ExtensionType {
    aac ("aac"),
    mp3 ("mp3"),
    mp4 ("mp4"),
    m4a ("m4a"),
    ts ("ts"),
    webm ("webm"),
    flac ("flac"),
    dvl ("dvl.json"),
    kompo ("kompo.json"),
    htmlImagelist ("htmlimagelist"),
    jpg ("jpg"),
    png ("png"),
    txt ("txt"),
    NONE ("");

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
        return this == aac || this == mp3 || this == flac;
    }

    public boolean isVideo() {
        return this == mp4 || this == webm || this == m4a || this == htmlImagelist;
    }

    public String toString() {
        return stringValue;
    }
}

