package no.lau.vdvil.domain;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class VideoStillImageSegment implements Segment {

    private final String id;
    private final int start;
    private final int duration;
    private final VideoStillImageRepresentation[] stillImages;

    public VideoStillImageSegment(String id, int start, int duration, VideoStillImageRepresentation... stillImages) {
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.stillImages = stillImages;
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

    public VideoStillImageRepresentation[] getStillImages() {
        return stillImages;
    }
}
