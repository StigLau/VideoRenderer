package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FromImageFileCollector;
import no.lau.vdvil.collector.ImageCollector;
import no.lau.vdvil.collector.WaitingVideoThumbnailsCollector;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.collector.plan.KnownNumberOfFramesPlan;
import no.lau.vdvil.collector.plan.StaticImagesFramePlan;
import no.lau.vdvil.collector.plan.TimeStampFixedImageSamplePlan;
import no.lau.vdvil.renderer.video.creator.ImageStore;

import java.awt.image.BufferedImage;

public class ImageCollectableShim implements ImageCollectShimInterface {
    int imageDownloadCacheSize = 10;

    public ImageCollector extractShim(ImageCollectable imageCollectablePlan, ImageStore<BufferedImage> imageStore, int framerateMillis, FramePlan framePlan) {
        if(framePlan instanceof KnownNumberOfFramesPlan || framePlan instanceof TimeStampFixedImageSamplePlan) {
            return new WaitingVideoThumbnailsCollector(imageCollectablePlan, imageStore, true);
        } else if(framePlan instanceof StaticImagesFramePlan) {
            return new FromImageFileCollector(imageCollectablePlan, imageStore, framerateMillis, imageDownloadCacheSize);
        } else {
            //TODO Simplify the different possibilities and maby move it out
            throw new RuntimeException("Not implemented collection type for " + framePlan.getClass());
        }
    }
}