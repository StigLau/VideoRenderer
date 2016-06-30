package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stig@lau.no on 30/06/16.
 */
class Common {
    static FrameRepresentation createFrameRepresentation(Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, segment.id(), segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }

    public static List<FrameRepresentation> calculateFramesFromSegment(Segment segment, long start, long frameRateMillis, long numberOfAvailableFrames, SimpleCalculator frameCalculator, Logger origLogger) {
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0; //Always starts at 0 for static images and collect
        origLogger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, segment.id());

        for (int i = 0; i < numberOfAvailableFrames; i++) {
            long thisDuration = frameRateMillis * i;

            //Placing empty frames when there are too few collect images
            boolean emptyFrame = true;
            if (segment instanceof TimeStampFixedImageSampleSegment) {
                if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                    emptyFrame = false;
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, segment.id(), segment, emptyFrame);
            frame.numberOfFrames = numberOfAvailableFrames;
            frame.frameNr = i;
            plans.add(frame);

            origLogger.trace(segment.id() + " #" + (i + 1) + " duration:" + thisDuration + " Used:" + emptyFrame);
        }
        return plans;
    }
}