package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Stig@Lau.no 23/05/15.
 */
public class ForNoTull {

    private final Map<String, Komposition> segmentKompositionMap;
    private final Map<String, Segment> collectSegmentMap;

    public ForNoTull(List<Komposition> fetchKompositions) {
        segmentKompositionMap = searchableKomposition(fetchKompositions);
        collectSegmentMap = searchableSegments(fetchKompositions);
    }



    public List<SegmentKompositionMap> alignSegments(List<Segment> inSegments, float bpm) {
        if(inSegments.isEmpty()) {
            return new ArrayList<>();
        }

        long lastBuildTimestamp = 0;
        long lastCollectTimestamp = 0;

        List<Segment> segmentRests = new ArrayList<>();

        //TODO Verify with Accompagnying FetchSegments timetable!
        Komposition kompositionOfFirstSegment = segmentKompositionMap.get(inSegments.get(0).id());
        SegmentKompositionMap found = new SegmentKompositionMap(kompositionOfFirstSegment);
        for (Segment inSegment : inSegments) {
            Komposition currentKomp = segmentKompositionMap.get(inSegment.id());


            Segment collectSegment = collectSegmentMap.get(inSegment.id());
            //Add if same komposition as first
            if(currentKomp == kompositionOfFirstSegment &&
                    inSegment.startCalculated(bpm) >= lastBuildTimestamp &&
                    collectSegment.startCalculated(bpm) >= lastCollectTimestamp) {
                found.segments.add(inSegment);
                lastBuildTimestamp = inSegment.startCalculated(bpm) + inSegment.durationCalculated(bpm);
                lastCollectTimestamp = collectSegment.startCalculated(bpm) + collectSegment.durationCalculated(bpm);
            } else {
                //This segment was of a different komposition or crashed with the last segment
                segmentRests.add(inSegment);
            }
        }

        List<SegmentKompositionMap> accumulated = alignSegments(segmentRests, bpm);
        accumulated.add(0, found);
        return accumulated;
    }

    private Map<String, Komposition> searchableKomposition(List<Komposition> kompositions) {
        Map<String, Komposition> result = new HashMap<>();
        for (Komposition komposition : kompositions) {
            for (Segment segment : komposition.segments) {
                result.put(segment.id(), komposition);
            }
        }
        return result;
    }

    private Map<String, Segment> searchableSegments(List<Komposition> kompositions) {
        Map<String, Segment> result = new HashMap<>();
        for (Komposition komposition : kompositions) {
            for (Segment segment : komposition.segments) {
                result.put(segment.id(), segment);
            }
        }
        return result;
    }
}

class SegmentKompositionMap {
    public final List<Segment> segments;
    public final Komposition komposition;

    SegmentKompositionMap(Komposition komposition) {
        this.komposition = komposition;
        segments = new ArrayList<>();
    }
}
