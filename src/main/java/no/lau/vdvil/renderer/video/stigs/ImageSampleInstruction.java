package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Instruction;

/**
 * A simple version of Instruction used by video sampler for extracting images.
 * FramesPerBeat is for picking a certain rate of pictures.
 * Not used when writing the images to video stream
 */
public class ImageSampleInstruction implements Segment {
    private final String id;
    private final long start;
    private final long duration;
    public final double framesPerBeat;

    public ImageSampleInstruction(String id, long start, long duration, double framesPerBeat) {
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.framesPerBeat = framesPerBeat;
    }

    public String id() {
        return id;
    }

    public long start() {
        return start;
    }

    public long duration() {
        return duration;
    }
}
