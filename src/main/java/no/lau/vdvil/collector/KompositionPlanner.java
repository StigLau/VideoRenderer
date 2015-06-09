package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import java.util.*;
import static no.lau.vdvil.renderer.video.KompositionUtil.performIdUniquenessCheck;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class KompositionPlanner {
    //public final List<SegmentFramePlan> plans = new ArrayList<>();

    final List<Plan> collectPlans = new ArrayList<>();
    final Plan buildPlan;
    //Calculating last timestamp is to be done on each entity one is interested in


    public KompositionPlanner(List<Komposition> fetchKompositions, Komposition buildKomposition, long finalFramerate) {
        List<Segment> buildSegments = buildKomposition.segments;
        Collections.sort(buildSegments);
        verifyNonOverlappingSegments(buildSegments);
        buildPlan = new SuperPlan(buildSegments, buildKomposition.storageLocation, buildKomposition.bpm, finalFramerate);

        //Verify that all fetchSegments are unique
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
        }

        //Conveniencemap Segment <--> komposition
        Map<Segment, Komposition> segmentFetchKompositionMap = new HashMap<>();
        for (Komposition fetchKomposition : fetchKompositions) {
            for (Segment segment : fetchKomposition.segments) {
                segmentFetchKompositionMap.put(segment, fetchKomposition);
            }
        }
        //Conveniencemap SegmentId <--> Segment
        Map<String, Segment> segmentIdCollectSegmentMap = new HashMap<>();
        for (Segment segment : segmentFetchKompositionMap.keySet()) {
            segmentIdCollectSegmentMap.put(segment.shortId(), segment);
        }


//        List<SegmentKompositionMap> alignedSegments = new ForNoTull(fetchKompositions)
//                .alignSegments(buildKomposition.segments, buildKomposition.bpm);
        for (SegmentFramePlan buildFramePlan : ((SuperPlan) buildPlan).getFramePlans()) {
            Segment collectSegment = segmentIdCollectSegmentMap.get(buildFramePlan.originalSegment.shortId());
            Komposition fetchKomposition = segmentFetchKompositionMap.get(collectSegment);

            SuperPlan collectPlan = new SuperPlan(collectSegment, buildFramePlan, fetchKomposition.storageLocation, finalFramerate, fetchKomposition.bpm);
            collectPlans.add(collectPlan);
        }
    }

    private void verifyNonOverlappingSegments(List<Segment> segments) {
        long i = 0;
        for (Segment segment : segments) {
            if(segment.start() < i) {
                throw new RuntimeException("Segment " + segment.id() + " collided with other segments");
            } else {
                i = segment.start() + segment.duration();
            }
        }
    }

    public List<Plan> collectPlans() {
        return collectPlans;
    }

    public Plan buildPlan() {
        return buildPlan;
    }
}

