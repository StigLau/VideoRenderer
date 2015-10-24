package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no
 */
public class AudioConfig implements Config {


    final ExtensionType extensionType;

    /**
     * Standard, simplified constructor
     */
    public AudioConfig() {
        this.extensionType = ExtensionType.mp3;
    }

    public AudioConfig(ExtensionType extensionType) {
        this.extensionType = extensionType;
    }

    public ExtensionType extensionType() {
        return extensionType;
    }
}
