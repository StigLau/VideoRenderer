package no.lau.vdvil.renderer.video;

public enum MediaType {
    video,
    audio,
    unknown;

    public static MediaType typeFinder(ExtensionType extensionType) {
        if(extensionType.isAudio()) {
           return MediaType.audio;
        } else if(extensionType.isVideo()) {
            return MediaType.video;
        } else {
            return MediaType.unknown;
        }
    }
}
