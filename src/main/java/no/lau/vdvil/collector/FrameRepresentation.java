package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

/**
 * @author Stig@Lau.no 12.05.2015.
 */
public class FrameRepresentation {
    public final long timestamp;
    private final boolean emptyFrame;
    public boolean used;
    private String referenceId;
    //Reference to Original segment for debugging purposes
    private Segment originalSegment;
    //To keep track of number of frames during building
    public long numberOfFrames = 0;
    public long frameNr = 0;

    public FrameRepresentation(long timestamp, String referenceId, Segment originalSegment) {
        this(timestamp, referenceId, originalSegment, false);
    }

    public FrameRepresentation(long timestamp, String referenceId, Segment originalSegment, boolean isEmptyFrame) {
        this.timestamp = timestamp;
        this.referenceId = referenceId;
        this.originalSegment = originalSegment;
        used = false;
        this.emptyFrame = isEmptyFrame;
    }

    public void use() {
        this.used = true;
    }

    public boolean used() {
        return this.used;
    }

    public String referenceId() {
        return referenceId;
    }

    public boolean isEmptyFrame() {
        return emptyFrame;
    }

    public String toString() {
        return originalSegment.id() + " " + timestamp + "_" + referenceId + (isEmptyFrame() ? " Empty Frame" : "");
    }
}
