package no.lau.vdvil.renderer.video.stigs;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.SuperSegment;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple version of Instruction used by video sampler for extracting images.
 * FramesPerBeat is for picking a certain rate of pictures.
 * Not used when writing the images to video stream
 */
public class ImageSampleInstruction extends SuperSegment {
    public final double framesPerBeat;
    List<String> collectedImages = new ArrayList<>();

    public ImageSampleInstruction(String id, long start, long duration, double framesPerBeat) {
        super(id, start, duration);
        this.framesPerBeat = framesPerBeat;
    }

    public List<String> collectedImages() {
        return collectedImages;
    }

    public Segment createCopy(long idIncrementation){
        return new ImageSampleInstruction(id() + idIncrementation, start(), duration(), framesPerBeat);
    }
}
