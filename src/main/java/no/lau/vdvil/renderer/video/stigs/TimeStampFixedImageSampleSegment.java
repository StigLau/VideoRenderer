package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.out.Instruction;

/**
 * A specific Segment which fetches only images at specific timestamps
 */
public class TimeStampFixedImageSampleSegment extends Instruction {
    public final double framesPerBeat;

    public TimeStampFixedImageSampleSegment(String id, long timestampStart, long timestampEnd, double framesPerBeat) {
        super(id, timestampStart, timestampEnd - timestampStart, null);
        this.framesPerBeat = framesPerBeat;
    }
}
