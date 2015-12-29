package no.lau.vdvil.domain;

/**
 * @author Stig@Lau.no
 */
public class StaticImagesSegment extends SuperSegment {
    public final String[] images;

    public StaticImagesSegment(String id, String... images) {
        super(id, 0, 12);

        this.images = images;
    }
}
