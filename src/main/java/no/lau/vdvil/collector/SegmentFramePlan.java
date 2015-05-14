package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class SegmentFramePlan implements Comparable {
    public final Segment originalSegment;
    final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(Segment segment, float bpm, long framerate) {
        this.originalSegment = segment;
        frameRepresentations = calculateFramesFromSegment(segment, bpm, framerate);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(Segment segment, float bpm, long framerate) {
        List<FrameRepresentation> plans = new ArrayList<>();

        long numberOfImages = segment.duration() * 60 / 120 * framerate;
        System.out.println("numberOfImages = " + numberOfImages);
        for (int i = 0; i < numberOfImages; i++) {
            long start = calc(segment.start(), bpm);
            long thisDuration = calc(segment.duration(), bpm) * i / numberOfImages;
            plans.add(new FrameRepresentation(start + thisDuration));
        }
        return plans;
    }

    public List<FrameRepresentation> findUnusedFramesAtTimestamp(long timestamp) {
        return frameRepresentations.stream()
                .filter(plan -> !plan.used && timestamp >= plan.timestamp)
                .collect(Collectors.toList());
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}

class FrameRepresentation {
    final long timestamp;
    public boolean used;

    FrameRepresentation(long timestamp) {
        this.timestamp = timestamp;
        used = false;
    }

    public void use() {
        this.used = true;
    }

    public boolean used() {
        return this.used;
    }
}