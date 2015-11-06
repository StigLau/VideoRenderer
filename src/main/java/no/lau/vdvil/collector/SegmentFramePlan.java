package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
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
    public final List<FrameRepresentation> frameRepresentations;

    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        this.originalSegment = segment;
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate, frameCalculator);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        if(framerate <= 0) {
            throw new RuntimeException("framerate was " + framerate);
        }
        long frameRateMillis = 1000000/framerate;
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfFrames;
        long numberOfCollectFrames = ((SimpleCalculator) frameCalculator).collectRatio / frameRateMillis;
        if (segment instanceof VideoStillImageSegment<?>) {
            numberOfFrames = Math.round(segment.duration() * framerate * 60 / bpm);
        } else if(segment instanceof TimeStampFixedImageSampleSegment) {
            numberOfFrames = numberOfCollectFrames;
        } else {
            numberOfFrames = -1;
        }

        //Logic for adding empty frames in case of more build frames is than collect frames
        long lastUsedFrame = 0;
        logger.info("numberOfImages = {} id: {}", numberOfFrames, id);
        long start = segment.startCalculated(bpm);
        for (int i = 0; i < numberOfFrames; i++) {
            long thisDuration = frameRateMillis * i;

            //Placing empty frames when there are too few collect images
            boolean emptyFrame = true;
            if(segment instanceof VideoStillImageSegment) {
                if(i > lastUsedFrame * (float) numberOfFrames / numberOfCollectFrames) {
                    emptyFrame = false;
                    lastUsedFrame++;
                }
            }

            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment, emptyFrame);
            frame.numberOfFrames = numberOfFrames;
            frame.frameNr = i;
            plans.add(frame);

            logger.trace(segment.id() + " #" + i + 1 + " duration:" + thisDuration + " Used:" + emptyFrame);
        }
        return plans;
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}