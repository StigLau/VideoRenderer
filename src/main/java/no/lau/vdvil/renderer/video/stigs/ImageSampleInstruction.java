package no.lau.vdvil.renderer.video.stigs;

/**
 * A simple version of Instruction used by video sampler for extracting images.
 * FramesPerBeat is for picking a certain rate of pictures.
 * Not used when writing the images to video stream
 */
public class ImageSampleInstruction extends Instruction{
    public final double framesPerBeat;

    public ImageSampleInstruction(String id, int from, int duration, double bpm, double framesPerBeat) {
        super(id, from, duration, bpm);
        this.framesPerBeat = framesPerBeat;
    }
}
