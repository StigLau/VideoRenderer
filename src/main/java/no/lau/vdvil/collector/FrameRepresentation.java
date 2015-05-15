package no.lau.vdvil.collector;

/**
 * @author Stig@Lau.no 12.05.2015.
 */
public class FrameRepresentation {
    public final long timestamp;
    public boolean used;

    FrameRepresentation(long timestamp) {
        this.timestamp = timestamp;
        used = false;
    }

    public void use() {
        this.used = true;
    }

    public boolean used() {
        return this.used;
    }
}
