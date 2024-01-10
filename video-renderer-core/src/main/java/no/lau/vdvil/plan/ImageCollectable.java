package no.lau.vdvil.plan;

import no.lau.vdvil.collector.ImageCollector;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import java.awt.image.BufferedImage;

/**
 * @author Stig@Lau.no 11/07/16.
 */
public interface ImageCollectable extends Plan {
    ImageCollector collector(ImageStore<BufferedImage> imageStore, int framerateMillis);
}
