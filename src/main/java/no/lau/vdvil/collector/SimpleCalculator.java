package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

public class SimpleCalculator implements FrameCalculator{

    public long calculateNumberOfFrames(Segment segment, float bpm, long framerate) {
        return Math.round(segment.durationCalculated(bpm) * bpm * framerate / (60 * 1000 * 1000));
    }
}
