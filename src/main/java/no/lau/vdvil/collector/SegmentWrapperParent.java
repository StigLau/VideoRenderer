package no.lau.vdvil.collector;

import no.lau.vdvil.domain.KnownNumberOfFramesSegment;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SegmentWrapperParent implements SegmentWrapper, Comparable {
    Logger logger = LoggerFactory.getLogger(KnownNumberOfFramesSegmentImpl.class);
    final Segment segment;
    final SimpleCalculator frameCalculator;
    final long finalFramerate;
    final long frameRateMillis;
    final float bpm;
    final long numberOfNeededBuildFrames;
    final String id;
    final long start;

    protected SegmentWrapperParent(String id, Segment segment, float buildBpm, long finalFramerate, SimpleCalculator calculator) {
        this.id = id;
        this.segment = segment;
        this.frameCalculator = calculator;
        this.bpm = buildBpm;
        this.finalFramerate = finalFramerate;
        frameRateMillis = 1000000/finalFramerate;
        numberOfNeededBuildFrames = frameCalculator.buildRatio / frameRateMillis;
        start = segment.startCalculated(bpm);
        if(finalFramerate <= 0) {
            throw new RuntimeException("framerate was " + finalFramerate);
        }
    }

    public int compareTo(Object other) {
        return Long.compare(segment.start(), ((SegmentWrapperParent) other).segment.start());
    }

    public static SegmentWrapper chooseForMe(String id, Segment segment, float buildBpm, long finalFramerate, FrameCalculator frameCalculator) {
        SimpleCalculator calc = (SimpleCalculator) frameCalculator;
        if (segment instanceof StaticImagesSegment) {
            return new StaticImagesSegmentWrapper(id, segment, buildBpm, finalFramerate, calc);
        } else if(segment instanceof KnownNumberOfFramesSegment) {
            return new KnownNumberOfFramesSegmentImpl(id, segment, buildBpm, finalFramerate, calc);
        } else if (segment instanceof VideoStillImageSegment<?> || segment instanceof TimeStampFixedImageSampleSegment) {
            return new ElseWrapper(id, segment, buildBpm, finalFramerate, calc);
        } else {
            return new ElseWrapper(id, segment, buildBpm, finalFramerate, calc);
        }
    }

    public Segment segment() {
        return segment;
    }

    public float bpm() {
        return bpm;
    }
}
