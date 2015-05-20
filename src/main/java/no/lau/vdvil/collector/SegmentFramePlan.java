package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class SegmentFramePlan implements Comparable {
    public final Segment originalSegment;
    public final Segment builderSegment;
    public final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(Segment originalSegment, Segment builderSegment, float bpm, long framerate) {
        this.originalSegment = originalSegment;
        this.builderSegment = builderSegment;
        frameRepresentations = calculateFramesFromSegment(builderSegment, bpm, framerate);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(Segment segment, float bpm, long framerate) {
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfImages = Math.round(segment.durationCalculated(bpm) * bpm * framerate / (60 * 1000 * 1000));
        System.out.println("numberOfImages = " + numberOfImages);
        long start = segment.startCalculated(bpm);
        for (int i = 0; i < numberOfImages; i++) {
            long thisDuration = segment.durationCalculated(bpm) * i / numberOfImages;
            plans.add(new FrameRepresentation(start + thisDuration));
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