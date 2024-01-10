package no.lau.vdvil.plan;

import no.lau.vdvil.collector.ImageCollector;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.renderer.video.creator.ImageStore;

import java.awt.image.BufferedImage;

public interface ImageCollectShimInterface {
    ImageCollector extractShim(ImageCollectable imageCollectablePlan, ImageStore<BufferedImage> imageStore, int framerateMillis, FramePlan framePlan);
}