package no.lau.vdvil.renderer.video.store;

import java.awt.image.BufferedImage;

/**
 * @author Stig Lau 26/04/15.
 */
public class ImageRepresentation<TYPE> {
    public final String imageId;
    public final String segmentId;
    public TYPE image;

    public ImageRepresentation(String imageId, String segmentId) {
        this.imageId = imageId;
        this.segmentId = segmentId;
    }
}
