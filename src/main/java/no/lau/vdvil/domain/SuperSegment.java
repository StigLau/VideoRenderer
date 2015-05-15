package no.lau.vdvil.domain;

import java.util.Random;
import static no.lau.vdvil.domain.utils.KompositionUtils.calc;

/**
 * An simple implementation of Segment to avoid some duplication
 * @author Stig@Lau.no 20/04/15.
 */
public abstract class SuperSegment implements Segment {
    private String id;
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

    /**
     * CreateCopy and changeId are special cases where Segments need to be duplicated because usage multiple places
     */
    public Segment createCopy(long idIncrementation){
        return new SuperSegment(id() + idIncrementation, start, duration()) {
            public String id() {
                return super.id();
            }

            public long start() {
                return super.start();
            }

            public long duration() {
                return super.duration();
            }

            public Segment createCopy(long idIncrementation) {
                return super.createCopy(idIncrementation);
            }

            public String toString() {
                return super.toString();
            }
        };
    }

    public void changeId(int idIncrementation) {
        this.id = id + idIncrementation;
    }

    public long startCalculated(float bpm) {
        return calc(start(), bpm);
    }

    public long durationCalculated(float bpm) {
        return calc(duration(), bpm);
    }

    public String toString() {
        return "Segment: " + id + " start:" + start + " duration: " + duration;
    }
}
