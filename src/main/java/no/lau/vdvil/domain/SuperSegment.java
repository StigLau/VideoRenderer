package no.lau.vdvil.domain;

import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * An simple implementation of Segment to avoid some duplication
 * @author Stig@Lau.no 20/04/15.
 */
public abstract class SuperSegment implements Segment {
    protected String id;
    private final long start;
    private final long duration;

    public SuperSegment(String id, long start, long duration) {
        this.id = id;
        this.start = start;
        this.duration = duration;
    }

    public String id() {
        return id + " " + start() + " + " + duration();
    }

    /**
     * The shortId is used as an interoperable id between collectiond and build segments
     */
    public String shortId() {
        return id;
    }

    public long start() {
        return start;
    }

    public long duration() {
        return duration;
    }

    public long startCalculated(float bpm) {
        return calc(start(), bpm);
    }

    public long durationCalculated(float bpm) {
        return calc(duration(), bpm);
    }

    public int compareTo(Object otherSegment) {
        //ascending order
        return new Long(this.start()).compareTo(((Segment)otherSegment).start());
    }

    public String toString() {
        return "Segment: " + id + " start:" + start + " duration: " + duration;
    }
}
