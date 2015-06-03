package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class SegmentFramePlan implements Comparable {

    static Logger logger = LoggerFactory.getLogger(SegmentFramePlan.class);
    public final Segment originalSegment;
    public final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        this.originalSegment = segment;
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate, frameCalculator);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        if(framerate <= 0) {
            throw new RuntimeException("framerate was " + framerate);
        }
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfFrames = frameCalculator.calculateNumberOfFrames(segment, bpm, framerate);
        logger.info("numberOfImages = {} id: {}", numberOfFrames, id);
        long start = segment.startCalculated(bpm);
        for (int i = 0; i < numberOfFrames; i++) {
            long thisDuration = segment.durationCalculated(bpm) * i / numberOfFrames;
            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment);
            frame.numberOfFrames = numberOfFrames;
            frame.frameNr = i;
            plans.add(frame);
        }
        return plans;
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}