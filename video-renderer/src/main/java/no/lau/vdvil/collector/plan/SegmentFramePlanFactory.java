package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.plan.SegmentFramePlan;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class SegmentFramePlanFactory implements SegmentFramePlan {

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
}
