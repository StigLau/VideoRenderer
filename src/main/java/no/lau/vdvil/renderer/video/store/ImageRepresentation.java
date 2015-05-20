package no.lau.vdvil.renderer.video.store;

import no.lau.vdvil.collector.FrameRepresentation;

/**
 * @author Stig Lau 26/04/15.
 */
public class ImageRepresentation<TYPE> {
    public final String imageId;
    public final String segmentId;
    public FrameRepresentation frameRepresentation;
    public TYPE image; //TODO Change name to payload

    public ImageRepresentation(String imageId, String segmentId, FrameRepresentation frameRepresentation) {
        this.imageId = imageId;
        this.segmentId = segmentId;
        this.frameRepresentation = frameRepresentation;
    }
}
