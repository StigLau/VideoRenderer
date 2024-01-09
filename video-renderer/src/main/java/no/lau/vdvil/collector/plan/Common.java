package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SimpleCalculator;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author stig@lau.no on 30/06/16.
 */
public class Common {
    static Logger logger = LoggerFactory.getLogger(Common.class);

    static FrameRepresentation createFrameRepresentation(String collectId, Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectId, segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String collectSegmentId, Segment segment, long start, long frameRateMillis, long numberOfAvailableFrames, SimpleCalculator frameCalculator, Logger origLogger) {
        List<FrameRepresentation> frameRepresentations = new ArrayList<>();
        long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0; //Always starts at 0 for static images and collect
        origLogger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, collectSegmentId);

        for (int i = 0; i < numberOfAvailableFrames; i++) {
            long thisDuration = frameRateMillis * i;

            if (segment instanceof TimeStampFixedImageSampleSegment) {
                if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, collectSegmentId, segment);
            frame.numberOfFrames = numberOfAvailableFrames;
            frame.frameNr = i;
            frameRepresentations.add(frame);

            origLogger.trace(collectSegmentId + " #" + (i + 1) + " duration:" + thisDuration);
        }
        return frameRepresentations;
    }

    public static void printStatus(Plan buildPlan, List<Plan> collectPlans) {
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