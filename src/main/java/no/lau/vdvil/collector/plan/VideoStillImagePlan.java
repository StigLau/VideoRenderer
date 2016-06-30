package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.VideoStillImageSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Stig@Lau.no 30/06/16.
 */
public class VideoStillImagePlan implements FramePlan {
    Logger logger = LoggerFactory.getLogger(getClass());
    final SegmentWrapper wrapper;
    private final VideoStillImageSegment segment;

    public VideoStillImagePlan(SegmentWrapper wrapper) {
        this.wrapper = wrapper;
        this.segment = (VideoStillImageSegment) wrapper.segment;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return ElseWrapper.calculateFramesFromSegment(segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames(), wrapper.frameCalculator, logger);
    }

    private long numberOfAvailableFrames() {
        return Math.round(wrapper.segment.duration() * wrapper.finalFramerate * 60 / wrapper.bpm);
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }
}
