package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static no.lau.vdvil.renderer.video.KompositionUtil.performIdUniquenessCheck;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class KompositionPlanner {
    //public final List<SegmentFramePlan> plans = new ArrayList<>();

    final List<Plan> collectPlans = new ArrayList<>();
    final Plan buildPlan;
    //Calculating last timestamp is to be done on each entity one is interested in


    public KompositionPlanner(List<Komposition> fetchKompositions, Komposition buildKomposition) {
        //Verify that all fetchSegments are unique
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
        }
        //TODO Verify none-overlapping segments or handle overlapping segments through merging (?)!

        //Create collect plans - based on
        /*
        for (Komposition fetchKomposition : fetchKompositions) {
            collectPlans.add(new SuperPlan(fetchKomposition));
        }*/

        /*
        for (Komposition fetchKomposition : fetchKompositions) {
            List<List<Segment>> alignedSegmentList= alignSegments(fetchKomposition.segments, fetchKomposition.bpm);
            for (List<Segment> alignedSegment : alignedSegmentList) {
                collectPlans.add(new SuperPlan(fetchKomposition, alignedSegment));
            }
        }
        */


/*
            List<List<Segment>> alignedSegmentList= alignSegments(buildKomposition.segments, buildKomposition.bpm);
            for (List<Segment> alignedSegment : alignedSegmentList) {
                //collectPlans.add(new SuperPlan(fetchKomposition, alignedSegment));
            }
*/

        //Conveniencemap for finding out the Reference Id of a given Segment Id
        Map<String, String> segmentIdReferenceIdMap = new HashMap<>();

        for (SegmentKompositionMap komSegments : new ForNoTull(fetchKompositions)
                .alignSegments(buildKomposition.segments, buildKomposition.bpm)) {
            SuperPlan plan = new SuperPlan(komSegments.komposition);
            collectPlans.add(plan);
            for (Segment segment : komSegments.segments) {
                segmentIdReferenceIdMap.put(segment.id(), plan.id());
            }
        }

        //TODO Fortell BuildPlan'en hvilken kø + pipedream man skal finne FrameRepresentation' på!
        buildPlan = new SuperPlan(buildKomposition, segmentIdReferenceIdMap);//This SuperPlan is bad - all segments get the same refId

        //this.lastTimeStamp = calculateLastTimeStamp()
        //Something needs to be sorted
        //Collections.sort(plans);
    }

    public List<Plan> collectPlans() {
        return collectPlans;
    }

    public Plan buildPlan() {
        return buildPlan;
    }
}

