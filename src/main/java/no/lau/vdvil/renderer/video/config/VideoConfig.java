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
    final ExtensionType extension;
    long duration;

    /**
     * Standard, simplified constructor
     * @param width video width
     * @param height video heigth
     * @param framerate video framerate
     */
    public VideoConfig(int width, int height, long framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.extension = ExtensionType.mp4;
    }

    public VideoConfig(int width, int height, long framerate, ExtensionType extension) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
        this.extension = extension;
    }


    public ExtensionType extension() {
        return extension;
    }

    public int framerate() {
        return (int) (1000000 / framerate);
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long duration() {
        return duration;
    }
}
