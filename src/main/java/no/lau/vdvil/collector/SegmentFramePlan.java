package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.PipeDream;
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
    public final ImageStore pipe;
    public final boolean reversed;

    /**
     * Used during Build
     */
    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator) {
        this.originalSegment = segment;
        reversed = segment instanceof VideoStillImageSegment && ((VideoStillImageSegment) segment).isReversed();
        if(reversed) {
            pipe = new ImageFileStore(null, "/tmp/snaps/" + segment.id() + "/");
        } else {
            pipe = new PipeDream();
        }
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate, frameCalculator, reversed, pipe);
    }

    /**
     * Used during Collection
     */
    public SegmentFramePlan(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator, boolean reversed, ImageStore pipe) {
        this.originalSegment = segment;
        this.pipe = pipe;
        this.reversed = reversed;
        frameRepresentations = calculateFramesFromSegment(id, segment, bpm, framerate, frameCalculator, reversed, pipe);
    }

    static List<FrameRepresentation> calculateFramesFromSegment(String id, Segment segment, float bpm, long framerate, FrameCalculator frameCalculator, boolean reversed, ImageStore contentStore) {
        if(framerate <= 0) {
            throw new RuntimeException("framerate was " + framerate);
        }
        List<FrameRepresentation> plans = new ArrayList<>();
        long numberOfFrames = frameCalculator.calculateNumberOfFrames(segment, bpm, framerate);
        logger.info("numberOfImages = {} id: {}", numberOfFrames, id);
        long start = segment.startCalculated(bpm);

        for (long i = 0; i < numberOfFrames; i++) {
            long iteratore = reversed ? numberOfFrames - i : i;

            long thisDuration = segment.durationCalculated(bpm) * iteratore / numberOfFrames;
            FrameRepresentation frame = new FrameRepresentation(start + thisDuration, id, segment, contentStore);
            frame.numberOfFrames = numberOfFrames;
            frame.frameNr = i;
            plans.add(frame);
        }
        return plans;
    }


    public int compareTo(Object other) {
        return Long.compare(originalSegment.start(), ((SegmentFramePlan) other).originalSegment.start());
    }
}