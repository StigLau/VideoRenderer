package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Stig@Lau.no 30/06/16.
 */
public class VideoStillImagePlan implements FramePlan {
    Logger logger = LoggerFactory.getLogger(getClass());
    final SegmentWrapper wrapper;

    public VideoStillImagePlan(SegmentWrapper wrapper) {
        this.wrapper = wrapper;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return Common.calculateFramesFromSegment(wrapper.segment.id(), wrapper.segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames(), wrapper.frameCalculator, logger);
    }

    private long numberOfAvailableFrames() {
        return Math.round(wrapper.segment.duration() * wrapper.finalFramerate * 60 / wrapper.bpm);
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }

    @Override
    public String getId() {
        return toString();
    }
}
