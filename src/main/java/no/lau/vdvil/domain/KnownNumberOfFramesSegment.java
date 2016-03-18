package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;

/**
 * This Segment is used when the number of frames is known beforehand!
 */
public class KnownNumberOfFramesSegment extends TimeStampFixedImageSampleSegment {
    public final Long numberOfFrames;

    public KnownNumberOfFramesSegment(String id, long timestampStart, long timestampEnd, double framesPerBeat, Long numberOfFrames) {
        super(id, timestampStart, timestampEnd, framesPerBeat);
        this.numberOfFrames = numberOfFrames;
    }
}
