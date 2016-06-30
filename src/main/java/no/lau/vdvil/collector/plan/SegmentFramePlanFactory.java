package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class SegmentFramePlanFactory {

    public static FramePlan createInstance(SegmentWrapper wrapper ) {
        if (wrapper.segment instanceof StaticImagesSegment) {
            return new StaticImagesFramePlan(wrapper);
        } else if(wrapper.segment instanceof KnownNumberOfFramesSegment) {
            return new KnownNumberOfFramesPlan(wrapper);
        } else if (wrapper.segment instanceof VideoStillImageSegment<?> || wrapper.segment instanceof TimeStampFixedImageSampleSegment) {
            return new ElseWrapper(wrapper);
        } else {
            return new ElseWrapper(wrapper);
        }
    }
}
