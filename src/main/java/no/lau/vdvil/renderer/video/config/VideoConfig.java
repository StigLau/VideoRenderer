package no.lau.vdvil.renderer.video.config;

import no.lau.vdvil.renderer.video.Config;
import no.lau.vdvil.renderer.video.ExtensionType;

/**
 * @author Stig@Lau.no
 */
public class VideoConfig implements Config {

    //Width and height of the final Kompost to be produced
    public final int width;
    public final int height;
    public final long framerate;
    final ExtensionType extensionType;

    /**
     * Standard, simplified constructor
     */
    public VideoConfig(int width, int height, long framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.extensionType = ExtensionType.mp4;
    }

    public VideoConfig(int width, int height, long framerate, ExtensionType extensionType) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.extensionType = extensionType;
    }


    public ExtensionType extensionType() {
        return extensionType;
    }

    public int framerate() {
        return (int) (1000000 / framerate);
    }
}
