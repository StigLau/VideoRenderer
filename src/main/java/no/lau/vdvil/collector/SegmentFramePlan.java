package no.lau.vdvil.collector;

import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
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
        long numberOfAvailableFrames;
        long numberOfNeededBuildFrames = ((SimpleCalculator) frameCalculator).buildRatio / frameRateMillis;

        //TODO Extract this for future implementations!!!!1
        if (segment instanceof VideoStillImageSegment<?> || segment instanceof StaticImagesSegment) {
            numberOfAvailableFrames = Math.round(segment.duration() * framerate * 60 / bpm);
        } else if(segment instanceof KnownNumberOfFramesSegment) {
            numberOfAvailableFrames = ((KnownNumberOfFramesSegment)segment).numberOfFrames;
        } else if(segment instanceof TimeStampFixedImageSampleSegment) {
            logger.warn("Please run method to find out number of segments, resulting in KnownNumberOfFramesSegment");
            SimpleCalculator calc = (SimpleCalculator) frameCalculator;
            long numberOfCollectFrames = ((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis;
            numberOfAvailableFrames = 1 + numberOfCollectFrames * calc.buildRatio / calc.collectRatio;
            numberOfAvailableFrames = numberOfAvailableFrames / (((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis);
        }
        else {
            logger.error("Not implemented for {}", segment.getClass());
            numberOfAvailableFrames = -1;
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
                    FrameRepresentation frame = createFrameRepresentation(id, segment, framezz, start, imageNr, thisDuration);
                    frame.setImageUrl(image);
                    logger.trace("Adding image #{}, {}", imageNr, frame);
                    plans.add(frame);
                }
            }
        } else if(segment instanceof KnownNumberOfFramesSegment){
            logger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, id);

            int manipulatedFrames = 0;
            long thisDuration = 0;


            if (numberOfAvailableFrames > numberOfNeededBuildFrames) { // remove a / (a-b)
                for (int i = 1; i <= numberOfAvailableFrames; i++) {
                    thisDuration = frameRateMillis * i;

                    if(Math.round(i * (numberOfAvailableFrames-numberOfNeededBuildFrames) / numberOfAvailableFrames) > manipulatedFrames) {
                        manipulatedFrames++;
                    } else {
                        plans.add(createFrameRepresentation(id, segment, numberOfAvailableFrames, start, i, thisDuration));
                    }
                }
            } else if (numberOfAvailableFrames < numberOfNeededBuildFrames) { //(b-a)/a

                long rest = numberOfNeededBuildFrames % numberOfAvailableFrames;

                double partitions = Math.ceil((numberOfNeededBuildFrames - rest) / numberOfAvailableFrames) + 1; //The +1 is to ensure that we use all available partitions

                double leftovers = numberOfAvailableFrames - Math.floor(numberOfNeededBuildFrames / partitions);

                double leftoverPartitions = Math.floor(numberOfNeededBuildFrames / leftovers);




                int usedLeftovers = 1;

                for (int frameNr = 0; frameNr < numberOfNeededBuildFrames; frameNr++) {
                    String status;

                    if(frameNr % partitions == 0) {
                        //result[frameNr] = frameNr + "_";

                        manipulatedFrames++;
                        thisDuration = frameRateMillis * manipulatedFrames;

                        plans.add(createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, manipulatedFrames, thisDuration));
                        status = " main";
                    } else if(frameNr > usedLeftovers * leftoverPartitions) { //Evenly divide the rest frames
                        //result[frameNr] = frameNr + "extra";

                        manipulatedFrames++;
                        thisDuration = frameRateMillis * manipulatedFrames;

                        usedLeftovers ++;
                        plans.add(createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, manipulatedFrames, thisDuration));
                        status = " leftover";
                    } else {
                        plans.add(createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, frameNr, thisDuration));
                        status = " copy";
                    }
                    logger.debug("frame: " + frameNr + " duration: " + "thisDuration = " + thisDuration + status);
                }
            } else {
                logger.warn("What to do when the number of buildframes == availableframes!!?!");
            }
            logger.debug("{} numberOfAvailableFrames: {} numberOfNeededBuildFrames: {} ", segment.shortId(), numberOfAvailableFrames, numberOfNeededBuildFrames);
            if(numberOfAvailableFrames > numberOfNeededBuildFrames) {
                logger.debug("{} subtracted {} frames", segment.shortId(), manipulatedFrames);
            } else {
                logger.debug("{} added {} duplicate frames", segment.shortId(), manipulatedFrames);
            }
        } else {
            long numberOfCollectFrames = ((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis;
            //Logic for adding empty frames in case of more build frames is than collect frames
            long lastUsedFrame = 0; //Always starts at 0 for static images and collect
            logger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, id);

            for (int i = 0; i < numberOfAvailableFrames; i++) {
                long thisDuration = frameRateMillis * i;

                //Placing empty frames when there are too few collect images
                boolean emptyFrame = true;
                if (segment instanceof TimeStampFixedImageSampleSegment) {
                    if (i > lastUsedFrame * (float) numberOfAvailableFrames / numberOfCollectFrames) {
                        emptyFrame = false;
                        lastUsedFrame++;
                    }
                }

                FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment, emptyFrame);
                frame.numberOfFrames = numberOfAvailableFrames;
                frame.frameNr = i;
                plans.add(frame);

                logger.trace(segment.id() + " #" + (i + 1) + " duration:" + thisDuration + " Used:" + emptyFrame);
            }
        }
        return plans;
    }

    private static FrameRepresentation createFrameRepresentation(String id, Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}