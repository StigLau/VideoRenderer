package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.out.Instruction;

/**
 * A specific Segment which fetches only images at specific timestamps
 */
public class TimeStampFixedImageSampleSegment extends Instruction {

    public final double framesPerBeat;


    public TimeStampFixedImageSampleSegment(String id, long timestampFrom, long timestampUntil, double framesPerBeat) {
        super(id, timestampFrom, timestampUntil, null);
        this.framesPerBeat = framesPerBeat;
    }
}
