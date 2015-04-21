package no.lau.vdvil.domain;

import java.util.Random;

/**
 * An simple implementation of Segment to avoid some duplication
 * @author Stig@Lau.no 20/04/15.
 */
public abstract class SuperSegment implements Segment {
    private final String id;
    private final long start;
    private final long duration;


    public SuperSegment(long start, long duration) {
        this(start + " - " + duration + " Rand:" + new Random().nextLong(), start, duration);
    }

    public SuperSegment(String id, long start, long duration) {
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
}
