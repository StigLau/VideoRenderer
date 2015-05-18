package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.SuperSegment;

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

    public static List<List<Segment>> alignSegments(List<Segment> inSegments) {
        return alignSegments(inSegments, new ArrayList<>());
    }

    /**
     * Used for structuring the segments into  multiple lists of segments that are neatly following each other.
     */
    static List<List<Segment>> alignSegments(List<Segment> inSegments, List<List<Segment>> resultSegments) {
        long current = 0;
        List<Segment> foundSegments = new ArrayList<>();
        List<Segment> segmentRests = new ArrayList<>();
        for (Segment segment : inSegments) {
            if(segment.start() >= current) {
                foundSegments.add(segment);
                current = segment.start() + segment.duration();
            } else {
                segmentRests.add(segment);
            }
        }
        resultSegments.add(foundSegments);
        if(!segmentRests.isEmpty()) {
            return alignSegments(segmentRests, resultSegments);
        } else {
            return resultSegments;
        }
    }

    /**
     * Gives segments "unique" id's to avoid ID crashes.
     */
    public static List<Segment> createUniqueSegments(List<Segment> inSegments, List<Segment> outSegments) {
        int idIncrement = 0;
        List<Segment> uniqueSegments = new ArrayList<>();
        for (Segment outSegment : outSegments) {
            for (Segment inSegment : inSegments) {
                if(outSegment.id().contains(inSegment.id())) {
                    Segment copyOfInSegment = ((SuperSegment)inSegment).createCopy(idIncrement);
                    uniqueSegments.add(copyOfInSegment);
                    ((SuperSegment)outSegment).changeId(idIncrement);
                    idIncrement++;
                }
            }
        }
        return uniqueSegments;
    }

}
