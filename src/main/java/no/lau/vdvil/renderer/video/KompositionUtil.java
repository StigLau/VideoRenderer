package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no
 */
public class KompositionUtil {

    /**
     * Perform uniqueness check on Segment ID
     */
    public static void performIdUniquenessCheck(List<Segment> segments) {
        List<String> seenBeforeInSegmentIds = new ArrayList<>();
        for (Segment segment : segments) {
            if(seenBeforeInSegmentIds.contains(segment.id())) {
                throw new RuntimeException("Error - Segment ID " + segment.id() + " was not unique");
            } else {
                seenBeforeInSegmentIds.add(segment.id());
            }
        }
    }

    public static List<List<Segment>> alignSegments(List<Segment> inSegments, float bpm) {
        return alignSegments(inSegments, bpm, new ArrayList<>());
    }

    /**
     * Used for structuring the segments into  multiple lists of segments that are neatly following each other.
     */
    static List<List<Segment>> alignSegments(List<Segment> inSegments, float bpm, List<List<Segment>> resultSegments) {
        long current = 0;
        List<Segment> foundSegments = new ArrayList<>();
        List<Segment> segmentRests = new ArrayList<>();
        for (Segment segment : inSegments) {
            if(segment.startCalculated(bpm) >= current) {
                foundSegments.add(segment);
                current = segment.startCalculated(bpm) + segment.durationCalculated(bpm);
            } else {
                segmentRests.add(segment);
            }
        }
        resultSegments.add(foundSegments);
        if(!segmentRests.isEmpty()) {
            return alignSegments(segmentRests, bpm, resultSegments);
        } else {
            return resultSegments;
        }
    }
}
