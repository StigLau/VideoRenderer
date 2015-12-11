package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no
 */
public class AudioConfig implements Config {


    final ExtensionType extensionType;
    final int framerate;

    /**
     * Standard, simplified constructor
     */
    public AudioConfig() {
        framerate = 44000;
        this.extensionType = ExtensionType.mp3;
    }

    public AudioConfig(ExtensionType extensionType, int framerate) {
        this.extensionType = extensionType;
        this.framerate = framerate;
    }

    public ExtensionType extensionType() {
        return extensionType;
    }

    public int framerate() {
        return framerate;
    }
}
