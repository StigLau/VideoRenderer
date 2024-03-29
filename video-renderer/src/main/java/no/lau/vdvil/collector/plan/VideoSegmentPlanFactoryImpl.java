package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.plan.VideoSegmentPlanFactory;
import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import java.awt.image.BufferedImage;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class VideoSegmentPlanFactoryImpl implements VideoSegmentPlanFactory {

    public FramePlan createInstance(String collectId, SegmentWrapper wrapper) {
        if (wrapper.segment instanceof StaticImagesSegment) {
            return new StaticImagesFramePlan(collectId, wrapper);
        } else if (wrapper.segment instanceof KnownNumberOfFramesSegment) {
            return new KnownNumberOfFramesPlan(collectId, wrapper);
        } else if (wrapper.segment instanceof VideoStillImageSegment || wrapper.segment instanceof TransitionSegment) {
            return new VideoStillImagePlan(wrapper);
        } else if (wrapper.segment instanceof TimeStampFixedImageSampleSegment) {
            return new TimeStampFixedImageSamplePlan(collectId, wrapper);
        } else {
            return new ElseWrapper(wrapper);
        }
    }

    public FramePlan createCollectPlan(Segment originalSegment, FramePlan buildFramePlan, long finalFramerate, float collectBpm) {
        Segment buildSegment = buildFramePlan.wrapper().segment;
        float buildBpm = buildFramePlan.wrapper().bpm;
        long buildCalculatedBpm = buildSegment.durationCalculated(buildBpm);
        return createInstance(buildSegment.id(), new SegmentWrapper(originalSegment, collectBpm, finalFramerate, new SimpleCalculator(originalSegment.durationCalculated(collectBpm), buildCalculatedBpm)));
    }

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
