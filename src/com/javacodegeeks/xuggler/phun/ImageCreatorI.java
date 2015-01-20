package com.javacodegeeks.xuggler.phun;

import java.awt.image.BufferedImage;

public interface ImageCreatorI {
    BufferedImage getVideoFrame(long var1);

    short[] getAudioFrame(int var1);
}
