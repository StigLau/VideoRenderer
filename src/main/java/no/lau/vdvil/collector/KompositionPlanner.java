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
        buildPlan = new SuperPlan(buildKomposition.segments, buildKomposition.storageLocation, buildKomposition.bpm, finalFramerate);

        //Verify that all fetchSegments are unique
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
        }

        //Sort buildSegments
        verifyNonOverlappingSegments(buildKomposition.segments);
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
            //CollectSegments should use collecting segments. This translation is to get the collection segments from segment identifiers.
            //List<Segment> colletionSegments = new ArrayList<>();
            //colletionSegments.addAll(convertBuildSegmentListToCollectSegments(komSegments.segments, fetchKompositions));
            Segment collectSegment = segmentIdCollectSegmentMap.get(buildFramePlan.originalSegment.shortId());
            Komposition fetchKomposition = segmentFetchKompositionMap.get(collectSegment);

            SuperPlan collectPlan = new SuperPlan(collectSegment, buildFramePlan, fetchKomposition.storageLocation, finalFramerate, fetchKomposition.bpm);
            collectPlans.add(collectPlan);
            /*
            for (Segment segment : komSegments.segments) {
                segmentIdReferenceIdMap.put(segment.id() + Math.abs(new Random().nextInt()), segment.id());
            }
            */
        }

        //this.lastTimeStamp = calculateLastTimeStamp()
        //Something needs to be sorted
        //Collections.sort(plans);
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

