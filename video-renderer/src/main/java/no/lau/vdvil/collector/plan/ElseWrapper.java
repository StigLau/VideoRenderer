package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @author Stig@Lau.no
 * Wraps all the other implementations of Frame Plans
 */
class ElseWrapper implements FramePlan{
    final long numberOfAvailableFrames;
    private final SegmentWrapper wrapper;
    Logger logger = LoggerFactory.getLogger(getClass());

    public ElseWrapper(SegmentWrapper wrapper) {
        this.wrapper = wrapper;

        logger.error("Not implemented for {}", wrapper.segment.getClass());
        numberOfAvailableFrames =  -1;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return Common.calculateFramesFromSegment(wrapper.segment.id(), wrapper.segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames, wrapper.frameCalculator, logger);
    }



    public SegmentWrapper wrapper() {
        return wrapper;
    }

    @Override
    public String getId() {
        return toString();
    }
}

