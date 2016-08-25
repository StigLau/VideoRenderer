package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.TransitionSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class SegmentFramePlanFactory {

    public static FramePlan createInstance(String collectId, SegmentWrapper wrapper) {
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
}
