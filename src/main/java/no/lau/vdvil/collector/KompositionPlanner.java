package no.lau.vdvil.collector;

import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import java.net.URL;
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


    public KompositionPlanner(List<Komposition> fetchKompositions, Komposition buildKomposition, URL audioLocation, long finalFramerate) {
        List<Segment> buildSegments = buildKomposition.segments;
        Collections.sort(buildSegments);
        verifyNonOverlappingSegments(buildSegments);

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
        {
            SuperPlan superPlan = new SuperPlan(buildSegments, buildKomposition.storageLocation, buildKomposition.bpm, finalFramerate, segmentIdCollectSegmentMap);
            superPlan.audioLocation = audioLocation;
            this.buildPlan = superPlan;
        }

        for (FramePlan buildFramePlan : ((SuperPlan) this.buildPlan).getFramePlans()) {
            Segment collectSegment = segmentIdCollectSegmentMap.get(buildFramePlan.wrapper().segment.shortId());
            Komposition fetchKomposition = segmentFetchKompositionMap.get(collectSegment);
            collectPlans.add(new SuperPlan(collectSegment, buildFramePlan, fetchKomposition.storageLocation, finalFramerate, fetchKomposition.bpm));
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

