package no.lau.vdvil.collector;

/**
 * @author Stig@Lau.no 12.05.2015.
 */
public class FrameRepresentation {
    public final long timestamp;
    public boolean used;
    private String referenceId;

    public FrameRepresentation(long timestamp, String referenceId) {
        this.timestamp = timestamp;
        this.referenceId = referenceId;
        used = false;
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
}
