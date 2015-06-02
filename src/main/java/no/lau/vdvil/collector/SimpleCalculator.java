package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

public class SimpleCalculator implements FrameCalculator{

    private final long collectRatio;
    private final long buildRatio;

    public SimpleCalculator(long collect, long build) {
        this.collectRatio = collect;
        this.buildRatio = build;
    }

    public long calculateNumberOfFrames(Segment segment, float bpm, long framerate) {
        long duration = segment.durationCalculated(bpm);
        return Math.round(duration * bpm * framerate / (60 * 1000 * 1000) * buildRatio / collectRatio);
    }
}
