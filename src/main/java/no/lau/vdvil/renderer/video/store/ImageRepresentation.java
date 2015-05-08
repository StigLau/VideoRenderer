package no.lau.vdvil.renderer.video.store;

/**
 * @author Stig Lau 26/04/15.
 */
public class ImageRepresentation<TYPE> {
    public final String imageId;
    public final String segmentId;
    public TYPE image; //TODO Change name to payload

    public ImageRepresentation(String imageId, String segmentId) {
        this.imageId = imageId;
        this.segmentId = segmentId;
    }
}
