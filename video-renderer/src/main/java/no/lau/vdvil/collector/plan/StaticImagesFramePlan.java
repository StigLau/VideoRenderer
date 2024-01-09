package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import no.lau.vdvil.domain.StaticImagesSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no on 30/06/16.
 */
public class StaticImagesFramePlan implements FramePlan {

    private final String collectId;
    final SegmentWrapper wrapper;
    final StaticImagesSegment segment;
    Logger logger = LoggerFactory.getLogger(getClass());

    public StaticImagesFramePlan(String collectId, SegmentWrapper wrapper) {
        this.collectId = collectId;
        this.wrapper = wrapper;
        this.segment = (StaticImagesSegment) wrapper.segment;
    }

    public long numberOfAvailableFrames() {
        return Math.round(wrapper.segment.duration() * wrapper.finalFramerate * 60 / wrapper.bpm);
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        List<FrameRepresentation> plans = new ArrayList<>();

        int nrOfImages = segment.images.length;
        int framezz = new Long(wrapper.frameCalculator.buildRatio / wrapper.frameRateMillis).intValue();
        int bucketSize = framezz / nrOfImages;
        int modulusRest = framezz % nrOfImages;

        int imageNr = 0;
        for (String image : segment.images) {
            //Eat up the rest and add image as long as rest is inverse

            int thisBucketSize = bucketSize;
            if(modulusRest > 0) {
                thisBucketSize = bucketSize +1;
                modulusRest--;
            }

            for (int i = 0;i < thisBucketSize ; i++, imageNr++) {
                long thisDuration = wrapper.frameRateMillis * imageNr  ;
                FrameRepresentation frame = Common.createFrameRepresentation(collectId, segment, framezz, wrapper.start, imageNr, thisDuration);
                frame.setImageUrl(image);
                logger.trace("Adding image #{}, {}", imageNr, frame);
                plans.add(frame);
            }
        }
        return plans;
    }

    public SegmentWrapper wrapper() {
        return wrapper;
    }

    @Override
    public String getId() {
        return collectId;
    }
}