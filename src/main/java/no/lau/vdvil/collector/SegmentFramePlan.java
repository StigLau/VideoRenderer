package no.lau.vdvil.collector;

import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import java.util.ArrayList;
import java.util.List;

class KnownNumberOfFramesSegmentImpl extends SegmentWrapperParent {

    protected KnownNumberOfFramesSegmentImpl(String id, Segment segment, float buildBpm, long finalFramerate, SimpleCalculator frameCalculator) {
        super(id, segment, buildBpm, finalFramerate, frameCalculator);
    }

    public long numberOfAvailableFrames() {
            return ((KnownNumberOfFramesSegment)segment).numberOfFrames;
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        long numberOfAvailableFrames = numberOfAvailableFrames();
        List<FrameRepresentation> plans = new ArrayList<>();
        logger.info("numberOfImages = {} id: {}", numberOfAvailableFrames, id);

        int manipulatedFrames = 0;
        long thisDuration = 0;


        if (numberOfAvailableFrames > numberOfNeededBuildFrames) { // remove a / (a-b)
            for (int i = 1; i <= numberOfAvailableFrames; i++) {
                thisDuration = frameRateMillis * i;

                if (Math.round(i * (numberOfAvailableFrames - numberOfNeededBuildFrames) / numberOfAvailableFrames) > manipulatedFrames) {
                    manipulatedFrames++;
                } else {
                    plans.add(Common.createFrameRepresentation(id, segment, numberOfAvailableFrames, start, i, thisDuration));
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

                if (frameNr % partitions == 0) {
                    manipulatedFrames++;
                    thisDuration = frameRateMillis * manipulatedFrames;

                    plans.add(Common.createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, manipulatedFrames, thisDuration));
                    status = " main";
                } else if (frameNr > usedLeftovers * leftoverPartitions) { //Evenly divide the rest frames
                    manipulatedFrames++;
                    thisDuration = frameRateMillis * manipulatedFrames;

                    usedLeftovers++;
                    plans.add(Common.createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, manipulatedFrames, thisDuration));
                    status = " leftover";
                } else {
                    plans.add(Common.createFrameRepresentation(id, segment, numberOfNeededBuildFrames, start, frameNr, thisDuration));
                    status = " copy";
                }
                logger.debug("frame: " + frameNr + " duration: " + "thisDuration = " + thisDuration + status);
            }
        } else {
            logger.warn("What to do when the number of buildframes == availableframes!!?!");
        }
        logger.debug("{} numberOfAvailableFrames: {} numberOfNeededBuildFrames: {} ", segment.shortId(), numberOfAvailableFrames, numberOfNeededBuildFrames);
        if (numberOfAvailableFrames > numberOfNeededBuildFrames) {
            logger.debug("{} subtracted {} frames", segment.shortId(), manipulatedFrames);
        } else {
            logger.debug("{} added {} duplicate frames", segment.shortId(), manipulatedFrames);
        }
        return plans;
    }

}

class StaticImagesSegmentWrapper extends SegmentWrapperParent {

    protected StaticImagesSegmentWrapper(String id, Segment segment, float buildBpm, long finalFramerate, SimpleCalculator frameCalculator) {
        super(id, segment, buildBpm, finalFramerate, frameCalculator);
    }

    public long numberOfAvailableFrames() {
        return Math.round(segment.duration() * finalFramerate * 60 / bpm);
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        List<FrameRepresentation> plans = new ArrayList<>();
        StaticImagesSegment staticImagesSegment = (StaticImagesSegment) segment;
        int nrOfImages = staticImagesSegment.images.length;
        int framezz = new Long(frameCalculator.buildRatio / frameRateMillis).intValue();
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
                FrameRepresentation frame = Common.createFrameRepresentation(id, segment, framezz, start, imageNr, thisDuration);
                frame.setImageUrl(image);
                logger.trace("Adding image #{}, {}", imageNr, frame);
                plans.add(frame);
            }
        }
        return plans;
    }
}
//Wraps all the other implementations
class ElseWrapper extends SegmentWrapperParent{
    final long numberOfAvailableFrames;

    protected ElseWrapper(String id, Segment segment, float buildBpm, long finalFramerate, SimpleCalculator frameCalculator) {
        super(id, segment, buildBpm, finalFramerate, frameCalculator);

        if (segment instanceof VideoStillImageSegment<?>) {
            numberOfAvailableFrames = Math.round(segment.duration() * finalFramerate * 60 / bpm);
        } else if (segment instanceof TimeStampFixedImageSampleSegment) {
            logger.warn("Please run method to find out number of segments, resulting in KnownNumberOfFramesSegment");
            long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
            long tempnumberOfAvailableFrames = 1 + numberOfCollectFrames * frameCalculator.buildRatio / frameCalculator.collectRatio;
            numberOfAvailableFrames = tempnumberOfAvailableFrames / (frameCalculator.collectRatio / frameRateMillis);
        } else {
            logger.error("Not implemented for {}", segment.getClass());
            numberOfAvailableFrames =  -1;
        }
    }

    public List<FrameRepresentation> calculateFramesFromSegment() {
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfCollectFrames = frameCalculator.collectRatio / frameRateMillis;
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
        return plans;
    }
}

class Common {
    static FrameRepresentation createFrameRepresentation(String id, Segment segment, long numberOfAvailableFrames, long start, int i, long thisDuration) {
        FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment);
        frame.numberOfFrames = numberOfAvailableFrames;
        frame.frameNr = i;
        return frame;
    }
}