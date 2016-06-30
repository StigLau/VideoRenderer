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
public class TimeStampFixedImageSamplePlan implements FramePlan {
    private final SegmentWrapper wrapper;
    private final TimeStampFixedImageSampleSegment segment;
    Logger logger = LoggerFactory.getLogger(getClass());

    public TimeStampFixedImageSamplePlan(SegmentWrapper wrapper) {
        this.wrapper = wrapper;
        this.segment = (TimeStampFixedImageSampleSegment) wrapper.segment;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return Common.calculateFramesFromSegment(segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames(), wrapper.frameCalculator, logger);
    }

    private long numberOfAvailableFrames() {
        logger.warn("Please run method to find out number of segments, resulting in KnownNumberOfFramesSegment");
        long numberOfCollectFrames = wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis;
        long tempnumberOfAvailableFrames = 1 + numberOfCollectFrames * wrapper.frameCalculator.buildRatio / wrapper.frameCalculator.collectRatio;
        return tempnumberOfAvailableFrames / (wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis);
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }
}
