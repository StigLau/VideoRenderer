package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

public class SegmentWrapper implements Comparable {
    public final Segment segment;
    public final SimpleCalculator frameCalculator;
    public final long finalFramerate;
    public final long frameRateMillis;
    public final float bpm;
    public final long numberOfNeededBuildFrames;
    final String id;
    public final long start;

    public SegmentWrapper(Segment segment, float bpm, long finalFramerate, SimpleCalculator calculator) {
        this.id = segment.id();
        this.segment = segment;
        this.frameCalculator = calculator;
        this.bpm = bpm;
        this.finalFramerate = finalFramerate;
        frameRateMillis = 1000000/finalFramerate;
        numberOfNeededBuildFrames = frameCalculator.buildRatio / frameRateMillis;
        start = (long) (frameRateMillis * (float) Math.ceil((double) segment.startCalculated(this.bpm) / frameRateMillis));
        if(finalFramerate <= 0) {
            throw new RuntimeException("framerate was " + finalFramerate);
        }
    }

    public int compareTo(Object other) {
        return Long.compare(segment.start(), ((SegmentWrapper) other).segment.start());
    }
}
