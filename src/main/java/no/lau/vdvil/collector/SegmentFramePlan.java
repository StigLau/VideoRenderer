package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class SegmentFramePlan implements Comparable {

    static Logger logger = LoggerFactory.getLogger(SegmentFramePlan.class);
    public final Segment originalSegment;
    public final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate) {
        this.originalSegment = segment;
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String id, Segment segment, float bpm, long framerate) {
        if(framerate <= 0) {
            throw new RuntimeException("framerate was " + framerate);
        }
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfImages = Math.round(segment.durationCalculated(bpm) * bpm * framerate / (60 * 1000 * 1000));
        logger.info("numberOfImages = " + numberOfImages);
        long start = segment.startCalculated(bpm);
        for (int i = 0; i < numberOfImages; i++) {
            long thisDuration = segment.durationCalculated(bpm) * i / numberOfImages;
            plans.add(new FrameRepresentation(start + thisDuration, id));
        }
        return plans;
    }

    public List<FrameRepresentation> findUnusedFramesAtTimestamp(long timestamp) {
        return frameRepresentations.stream()
                .filter(plan -> !plan.used && timestamp >= plan.timestamp)
                .collect(Collectors.toList());
    }

    public List<FrameRepresentation> findUnusedBuilderFramesAtTimestamp(long timestamp) {
        return frameRepresentations.stream()
                .filter(plan -> !plan.used && timestamp >= plan.timestamp)
                .collect(Collectors.toList());
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}