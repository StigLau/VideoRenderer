package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.SuperSegment;

/**
 * A specific Segment which fetches only images at specific timestamps
 */
public class TimeStampFixedImageSampleSegment extends SuperSegment {
    public final double framesPerBeat;
    //Need to store these values in case needed for object-copying
    public long timestampStart;
    public final long timestampEnd;

    public TimeStampFixedImageSampleSegment(String id, long timestampStart, long timestampEnd, double framesPerBeat) {
        super(id, timestampStart, timestampEnd - timestampStart);
        this.framesPerBeat = framesPerBeat;
        this.timestampStart = timestampStart;
        this.timestampEnd = timestampEnd;
    }

    public long startCalculated(float bpm) {
        return timestampStart;
    }

    public long durationCalculated(float bpm) {
        return timestampEnd - timestampStart;
    }
}
