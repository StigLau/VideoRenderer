package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import static no.lau.vdvil.collector.FrameRepresentation.createFrameRepresentation;

/**
 * @author stig@lau.no on 30/06/16.
 */
public class KnownNumberOfFramesPlan implements FramePlan {

    Logger logger = LoggerFactory.getLogger(getClass());
    final KnownNumberOfFramesSegment segment;
    private final String collectId;
    final SegmentWrapper wrapper;

    public KnownNumberOfFramesPlan(String collectId, SegmentWrapper wrapper) {
        this.collectId = collectId;
        this.wrapper = wrapper;
        this.segment = (KnownNumberOfFramesSegment) wrapper.segment;

    }

    public long numberOfAvailableFrames() {
        return segment.numberOfFrames;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        long numberOfAvailableFrames = numberOfAvailableFrames();
        List<FrameRepresentation> plans = new ArrayList<>();
        logger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, collectId);

        int manipulatedFrames = 0;
        long thisDuration = 0;


        if (numberOfAvailableFrames > wrapper.numberOfNeededBuildFrames) { // remove a / (a-b)
            for (int i = 1; i <= numberOfAvailableFrames; i++) {
                thisDuration = wrapper.frameRateMillis * i;

                if (Math.round(i * (numberOfAvailableFrames - wrapper.numberOfNeededBuildFrames) / numberOfAvailableFrames) > manipulatedFrames) {
                    manipulatedFrames++;
                } else {
                    plans.add(createFrameRepresentation(collectId, segment, numberOfAvailableFrames, wrapper.start, i, thisDuration));
                }
            }
        } else if (numberOfAvailableFrames < wrapper.numberOfNeededBuildFrames) { //(b-a)/a

            long rest = wrapper.numberOfNeededBuildFrames % numberOfAvailableFrames;

            double partitions = Math.ceil((wrapper.numberOfNeededBuildFrames - rest) / numberOfAvailableFrames) + 1; //The +1 is to ensure that we use all available partitions

            double leftovers = numberOfAvailableFrames - Math.floor(wrapper.numberOfNeededBuildFrames / partitions);

            double leftoverPartitions = Math.floor(wrapper.numberOfNeededBuildFrames / leftovers);


            int usedLeftovers = 1;

            for (int frameNr = 0; frameNr < wrapper.numberOfNeededBuildFrames; frameNr++) {
                String status;

                if (frameNr % partitions == 0) {
                    manipulatedFrames++;
                    thisDuration = wrapper.frameRateMillis * manipulatedFrames;

                    plans.add(createFrameRepresentation(collectId, segment, wrapper.numberOfNeededBuildFrames, wrapper.start, manipulatedFrames, thisDuration));
                    status = " main";
                } else if (frameNr > usedLeftovers * leftoverPartitions) { //Evenly divide the rest frames
                    manipulatedFrames++;
                    thisDuration = wrapper.frameRateMillis * manipulatedFrames;

                    usedLeftovers++;
                    plans.add(createFrameRepresentation(collectId, segment, wrapper.numberOfNeededBuildFrames, wrapper.start, manipulatedFrames, thisDuration));
                    status = " leftover";
                } else {
                    plans.add(createFrameRepresentation(collectId, segment, wrapper.numberOfNeededBuildFrames, wrapper.start, frameNr, thisDuration));
                    status = " copy";
                }
                logger.debug("frame: " + frameNr + " duration: " + "thisDuration = " + thisDuration + status);
            }
        } else {
            logger.warn("What to do when the number of buildframes == availableframes!!?!");
        }
        logger.debug("{} numberOfAvailableFrames: {} numberOfNeededBuildFrames: {} ", segment.shortId(), numberOfAvailableFrames, wrapper.numberOfNeededBuildFrames);
        if (numberOfAvailableFrames > wrapper.numberOfNeededBuildFrames) {
            logger.debug("{} subtracted {} frames", segment.shortId(), manipulatedFrames);
        } else {
            logger.debug("{} added {} duplicate frames", segment.shortId(), manipulatedFrames);
        }
        return plans;
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }

    public String getId() {
        return collectId;
    }
}