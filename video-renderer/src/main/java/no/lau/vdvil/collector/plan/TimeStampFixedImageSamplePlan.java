package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class TimeStampFixedImageSamplePlan implements FramePlan{
    private final String collectId;
    private final SegmentWrapper wrapper;
    private final TimeStampFixedImageSampleSegment segment;
    Logger logger = LoggerFactory.getLogger(getClass());

    public TimeStampFixedImageSamplePlan(String collectId, SegmentWrapper wrapper) {
        this.collectId = collectId;
        this.wrapper = wrapper;
        this.segment = (TimeStampFixedImageSampleSegment) wrapper.segment;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return Common.calculateFramesFromSegment(collectId, segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames(), wrapper.frameCalculator, logger);
    }

    private long numberOfAvailableFrames() {
        return wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis;
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }

    public String getId() {
        return collectId;
    }
}
