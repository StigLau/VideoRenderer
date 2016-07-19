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
    static FrameRepresentation createFrameRepresentation(String collectId, Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectId, segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }

    @Deprecated //Old way of calculating frames from segment, using the original segments id. When using both build and collect segments, the id necesarily needs to change TODO Check if it can be removed
    public static List<FrameRepresentation> calculateFramesFromSegment(Segment segment, long start, long frameRateMillis, long numberOfAvailableFrames, SimpleCalculator frameCalculator, Logger origLogger) {
        return calculateFramesFromSegment(segment.id(), segment, start, frameRateMillis, numberOfAvailableFrames, frameCalculator,  origLogger);
    }

    public static List<FrameRepresentation> calculateFramesFromSegment(String collectSegmentId, Segment segment, long start, long frameRateMillis, long numberOfAvailableFrames, SimpleCalculator frameCalculator, Logger origLogger) {
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0; //Always starts at 0 for static images and collect
        origLogger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, collectSegmentId);

        for (int i = 0; i < numberOfAvailableFrames; i++) {
            long thisDuration = frameRateMillis * i;

            if (segment instanceof TimeStampFixedImageSampleSegment) {
                if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectSegmentId, segment);
            frame.numberOfFrames = numberOfAvailableFrames;
            frame.frameNr = i;
            plans.add(frame);

            origLogger.trace(collectSegmentId + " #" + (i + 1) + " duration:" + thisDuration);
        }
        return plans;
    }
}