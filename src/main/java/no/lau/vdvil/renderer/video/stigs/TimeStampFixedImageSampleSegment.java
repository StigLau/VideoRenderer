package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.Segment;

/**
 * A specific Segment which fetches only images at specific timestamps
 */
public class TimeStampFixedImageSampleSegment implements Segment {
    public final double framesPerBeat;
    private final String id;
    private final long timestampStart;
    private final long timestampDuration;

    public TimeStampFixedImageSampleSegment(String id, long timestampStart, long timestampEnd, double framesPerBeat) {
        this.id = id;
        this.timestampStart = timestampStart;
        this.timestampDuration = timestampEnd - timestampStart;
        this.framesPerBeat = framesPerBeat;
    }

    public String id() {
        return id;
    }

    public long start() {
        return timestampStart;
    }

    @Override
    public long duration() {
        return timestampDuration;
    }
}
