package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.SuperSegment;

/**
 * A specific Segment which fetches only images at specific timestamps
 */
public class TimeStampFixedImageSampleSegment extends SuperSegment {
    public final double framesPerBeat;
    //Need to store these values in case needed for object-copying
    public final long timestampStart;
    public final long timestampEnd;

    public TimeStampFixedImageSampleSegment(String id, long timestampStart, long timestampEnd, double framesPerBeat) {
        super(id, -1, -1);
        this.framesPerBeat = framesPerBeat;
        this.timestampStart = timestampStart;
        this.timestampEnd = timestampEnd;
    }

    public Segment createCopy(long idIncrementation) {
        return new TimeStampFixedImageSampleSegment(id() + idIncrementation, timestampStart, timestampEnd, framesPerBeat);
    }

    public long startCalculated(float bpm) {
        return timestampStart;
    }

    public long durationCalculated(float bpm) {
        return timestampEnd - timestampStart;
    }
}
