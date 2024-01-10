package no.lau.vdvil.collector;

import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.*;
import no.lau.vdvil.renderer.video.KompositionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Stig@Lau.no 10/05/15.
 * This is where the magic happens
 */
public class KompositionPlanner {
    final List<Plan> collectPlans = new ArrayList<>();
    final Plan buildPlan;
    Logger logger = LoggerFactory.getLogger(getClass());
    //Calculating last timestamp is to be done on each entity one is interested in
    //Internal maps
    private final Map<Segment, Komposition> segmentFetchKompositionMap;
    private final Map<String, Segment> segmentIdCollectSegmentMap;


    public KompositionPlanner(List<Komposition> fetchKompositions, Komposition buildKomposition, Path audioLocation, long finalFramerate, VideoSegmentPlanFactory videoShim) {
        List<Segment> buildSegments = buildKomposition.segments;
        Collections.sort(buildSegments);
        //verifyNonOverlappingSegments(buildSegments); //TODO Open this again!!!!1
        notifyOfGapsBetweenSegments(buildSegments);

        //Verify that all fetchSegments are unique
        for (Komposition fetchKomposition : fetchKompositions) {
            KompositionUtil.performIdUniquenessCheck(fetchKomposition.segments);
        }

        //Conveniencemap Segment <--> komposition
        segmentFetchKompositionMap = new HashMap<>();
        for (Komposition fetchKomposition : fetchKompositions) {
            for (Segment segment : fetchKomposition.segments) {
                segmentFetchKompositionMap.put(segment, fetchKomposition);
            }
        }
        //Conveniencemap SegmentId <--> Segment
        segmentIdCollectSegmentMap = new HashMap<>();
        for (Segment segment : segmentFetchKompositionMap.keySet()) {
            segmentIdCollectSegmentMap.put(segment.shortId(), segment);
        }
        {
            long lastTimeStamp = SuperPlan.calculateLastTimeStamp(buildKomposition.bpm, buildSegments);
            this.buildPlan = new SuperPlan(lastTimeStamp, buildKomposition.storageLocation,
                    SuperPlan.createBuildPlan(buildSegments, buildKomposition.bpm, finalFramerate, segmentIdCollectSegmentMap, videoShim))
                    .withImageCollector(videoShim) //Not in use!
                    .withAudioLocation(audioLocation);
        }

        for (FramePlan buildFramePlan : ((SuperPlan) this.buildPlan).getFramePlans()) {
            Segment collectSegment = segmentIdCollectSegmentMap.get(buildFramePlan.wrapper().segment.shortId());
            Komposition fetchKomposition = segmentFetchKompositionMap.get(collectSegment);
            Segment buildSegment = buildFramePlan.wrapper().segment;

            long lastTimeStamp;
            if (collectSegment instanceof StaticImagesSegment) {
                lastTimeStamp = SuperPlan.calculateEnd(buildKomposition.bpm, buildSegment);
            } else if(buildSegment instanceof TransitionSegment) {
                continue; //This is a build instruction and not collect segment
            } else {
                lastTimeStamp = SuperPlan.calculateEnd(fetchKomposition.bpm, collectSegment);
            }
            FramePlan framePlan = videoShim.createCollectPlan(collectSegment, buildFramePlan, finalFramerate, fetchKomposition.bpm);
            collectPlans.add(new SuperPlan(lastTimeStamp, fetchKomposition.storageLocation, framePlan)
                    .withImageCollector(videoShim)
            );
        }
        printStatus(buildPlan(), collectPlans());
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

    private void notifyOfGapsBetweenSegments(List<Segment> segments) {
        long last = 0;
        for (Segment segment : segments) {
            if(segment.start() > last) {
                logger.warn("Gap detected before segment {}", segment.id());
            }
            last = segment.start() + segment.duration();
        }
    }


    public List<Plan> collectPlans() {
        return collectPlans;
    }

    public Plan buildPlan() {
        return buildPlan;
    }

    public Map<Segment, Komposition> getSegmentFetchKompositionMap() {
        return segmentFetchKompositionMap;
    }

    public Map<String, Segment> getSegmentIdCollectSegmentMap() {
     return segmentIdCollectSegmentMap;
    }

    void printStatus(Plan buildPlan, List<Plan> collectPlans) {
        SuperPlan build = (SuperPlan) buildPlan;
        logger.debug("Build frameplans size: " + build.getFramePlans().size());
        logger.debug("Build plan frame reps size: " + build.getFrameRepresentations().size());
        logger.debug("Last timestamp {}", build.lastTimeStamp());
        logger.debug("Collect plans frames: ");

        for (Plan plan : collectPlans) {
            SuperPlan collectPlan = (SuperPlan) plan;
            logger.debug("{} Collect FramePlans: {}", collectPlan.id(), collectPlan.getFramePlans().size());
            logger.debug("Collect FrameReps {}", collectPlan.getFrameRepresentations().size());
        }
    }
}

