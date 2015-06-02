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
        List<SegmentKompositionMap> alignedSegments = new ForNoTull(fetchKompositions)
                .alignSegments(buildKomposition.segments, buildKomposition.bpm);
        for (SegmentKompositionMap komSegments : alignedSegments) {

            //CollectSegments should use collecting segments. This translation is to get the collection segments from segment identifiers.
            List<Segment> colletionSegments = new ArrayList<>();
            colletionSegments.addAll(convertBuildSegmentListToCollectSegments(komSegments.segments, fetchKompositions));

            SuperPlan collectPlan = new SuperPlan(komSegments.komposition, colletionSegments, komSegments.segments, finalFramerate); // 5 * 3  - 29 vs 16
            collectPlans.add(collectPlan);

            segmentIdReferenceIdMap.putAll(collectPlan.referenceIdSegmentIdMap);
            /*
            for (Segment segment : komSegments.segments) {
                segmentIdReferenceIdMap.put(segment.id() + Math.abs(new Random().nextInt()), segment.id());
            }
            */
        }

        buildPlan = new SuperPlan(buildKomposition, segmentIdReferenceIdMap, finalFramerate);//This SuperPlan is bad - all segments get the same refId

        //this.lastTimeStamp = calculateLastTimeStamp()
        //Something needs to be sorted
        //Collections.sort(plans);
    }

    private Set<Segment> convertBuildSegmentListToCollectSegments(List<Segment> segments, List<Komposition> fetchKompositions) {
        Set<Segment> foundSegments = new HashSet<>();
        for (Segment segment : segments) {
            String buildSegmentId = segment.id();
            for (Komposition fetchKomposition : fetchKompositions) {
                for (Segment collectSegment : fetchKomposition.segments) {
                    if(collectSegment.id().equals(buildSegmentId)) {
                        foundSegments.add(collectSegment);
                    }
                }
            }
        }
        return foundSegments;
    }

    public List<Plan> collectPlans() {
        return collectPlans;
    }

    public Plan buildPlan() {
        return buildPlan;
    }
}

