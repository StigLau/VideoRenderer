package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.VideoStillImageSegment;
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
        if (wrapper.segment instanceof VideoStillImageSegment<?>) {
            numberOfAvailableFrames = Math.round(wrapper.segment.duration() * wrapper.finalFramerate * 60 / wrapper.bpm);
        } else if (wrapper.segment instanceof TimeStampFixedImageSampleSegment) {
            logger.warn("Please run method to find out number of segments, resulting in KnownNumberOfFramesSegment");
            long numberOfCollectFrames = wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis;
            long tempnumberOfAvailableFrames = 1 + numberOfCollectFrames * wrapper.frameCalculator.buildRatio / wrapper.frameCalculator.collectRatio;
            numberOfAvailableFrames = tempnumberOfAvailableFrames / (wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis);
        } else {
            logger.error("Not implemented for {}", wrapper.segment.getClass());
            numberOfAvailableFrames =  -1;
        }
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfCollectFrames = wrapper.frameCalculator.collectRatio / wrapper.frameRateMillis;
        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0; //Always starts at 0 for static images and collect
        logger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, wrapper.segment.id());

        for (int i = 0; i < numberOfAvailableFrames; i++) {
            long thisDuration = wrapper.frameRateMillis * i;

            //Placing empty frames when there are too few collect images
            boolean emptyFrame = true;
            if (wrapper.segment instanceof TimeStampFixedImageSampleSegment) {
                if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                    emptyFrame = false;
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(wrapper.start + thisDuration, wrapper.segment.id(), wrapper.segment, emptyFrame);
            frame.numberOfFrames = numberOfAvailableFrames;
            frame.frameNr = i;
            plans.add(frame);

            logger.trace(wrapper.segment.id() + " #" + (i + 1) + " duration:" + thisDuration + " Used:" + emptyFrame);
        }
        return plans;
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }
}

