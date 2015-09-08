package no.lau.vdvil.renderer.video;

/**
 * @author Stig@Lau.no 22/05/15.
 */
public class Config {
    //Width and height of the final Kompost to be produced
    public final int width;
    public final int height;
    public final long framerate;

    public Config(int width, int height, long framerate) {
        this.width = width;
        this.height = height;
        this.framerate = framerate;
    }

    public String toString() {
        return "W:" + width + " H:" + height + " rate:" + framerate;
    }
}