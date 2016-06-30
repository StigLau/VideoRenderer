package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no
 * Wraps all the other implementations of Frame Plans
 */
public class ElseWrapper implements FramePlan{
    final long numberOfAvailableFrames;
    private final SegmentWrapper wrapper;
    Logger logger = LoggerFactory.getLogger(getClass());

    public ElseWrapper(SegmentWrapper wrapper) {
        this.wrapper = wrapper;

        logger.error("Not implemented for {}", wrapper.segment.getClass());
        numberOfAvailableFrames =  -1;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        return Common.calculateFramesFromSegment(wrapper.segment, wrapper.start, wrapper.frameRateMillis, numberOfAvailableFrames, wrapper.frameCalculator, logger);
    }



    public SegmentWrapper wrapper() {
        return wrapper;
    }
}

