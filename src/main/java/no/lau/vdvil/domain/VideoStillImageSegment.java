package no.lau.vdvil.domain;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class VideoStillImageSegment implements Segment {

    private final String id;
    private final int start;
    private final int duration;
    //Whether the list is to be reverted
    public boolean reverted = false;

    public VideoStillImageSegment(String id, int start, int duration) {
        this.id = id;
        this.start = start;
        this.duration = duration;
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

    public Segment revert() {
        reverted = !reverted;
        return this;
    }

    public boolean isReverted() {
        return reverted;
    }
}
