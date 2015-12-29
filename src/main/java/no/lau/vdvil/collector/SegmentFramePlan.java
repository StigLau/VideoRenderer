package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 10/05/15.
 */
public class SegmentFramePlan implements Comparable {

    static Logger logger = LoggerFactory.getLogger(SegmentFramePlan.class);
    public final Segment originalSegment;
    public final float bpm;
    public final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        this.originalSegment = segment;
        this.bpm = bpm;
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate, frameCalculator);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        if(framerate <= 0) {
            throw new RuntimeException("framerate was " + framerate);
        }
        long frameRateMillis = 1000000/framerate;
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfFrames;

        //TODO Extract this for future implementations!!!!1
        if (segment instanceof VideoStillImageSegment<?> || segment instanceof StaticImagesSegment) {
            numberOfFrames = Math.round(segment.duration() * framerate * 60 / bpm);
        } else if(segment instanceof TimeStampFixedImageSampleSegment) {
            //Todo use buildSegments number of frames!
            SimpleCalculator calc = (SimpleCalculator) frameCalculator;
            long numberOfCollectFrames = ((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis;
            numberOfFrames = 1 + numberOfCollectFrames * calc.buildRatio / calc.collectRatio;
        } else {
            logger.error("Not implemented for {}", segment.getClass());
            numberOfFrames = -1;
        }

        long start = segment.startCalculated(bpm);

        if (segment instanceof StaticImagesSegment) {
            StaticImagesSegment staticImagesSegment = (StaticImagesSegment) segment;
            int nrOfImages = staticImagesSegment.images.length;
            int framezz = new Long(((SimpleCalculator) frameCalculator).buildRatio / frameRateMillis).intValue();
            int bucketSize = framezz / nrOfImages;
            int modulusRest = framezz % nrOfImages;

            int imageNr = 0;
            for (String image : staticImagesSegment.images) {
                //Eat up the rest and add image as long as rest is inverse

                int thisBucketSize = bucketSize;
                if(modulusRest > 0) {
                    thisBucketSize = bucketSize +1;
                    modulusRest--;
                }

                for (int i = 0;i < thisBucketSize ; i++, imageNr++) {


                    long thisDuration = frameRateMillis * imageNr  ;
                    FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment);
                    frame.numberOfFrames = framezz;
                    frame.frameNr = imageNr;

                    frame.setImageUrl(image);
                    logger.debug("Adding image #{}, {}", imageNr, frame);
                    plans.add(frame);
                }
            }
        } else {
            long numberOfCollectFrames = ((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis;
            //Logic for adding empty frames in case of more build frames is than collect frames
            long lastUsedFrame = 0; //Always starts at 0 for static images and collect
            logger.info("numberOfImages = {} id: {}", numberOfFrames, id);

            for (int i = 0; i < numberOfFrames; i++) {
                long thisDuration = frameRateMillis * i;

                //Placing empty frames when there are too few collect images
                boolean emptyFrame = true;
                if (segment instanceof TimeStampFixedImageSampleSegment) {
                    if (i > lastUsedFrame * (float) numberOfFrames / numberOfCollectFrames) {
                        emptyFrame = false;
                        lastUsedFrame++;
                    }
                }

                FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment, emptyFrame);
                frame.numberOfFrames = numberOfFrames;
                frame.frameNr = i;
                plans.add(frame);

                logger.trace(segment.id() + " #" + (i + 1) + " duration:" + thisDuration + " Used:" + emptyFrame);
            }
        }
        return plans;
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}