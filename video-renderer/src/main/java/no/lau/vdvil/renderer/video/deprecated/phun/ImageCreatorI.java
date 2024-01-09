package no.lau.vdvil.renderer.video.deprecated.phun;

import java.awt.image.BufferedImage;

public interface ImageCreatorI {
    BufferedImage getVideoFrame(long var1);

    short[] getAudioFrame(int var1);
}
