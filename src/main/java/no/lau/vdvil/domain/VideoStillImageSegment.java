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
    public double fromLimit = 0;
    public double untilLimit = 1;
    public int takter = -1;

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

    public VideoStillImageSegment revert() {
        reverted = false;
        return this;
    }

    public boolean isReverted() {
        return reverted;
    }

    public VideoStillImageSegment limit(double from, double until) {
        this.fromLimit = from;
        this.untilLimit = until;
        return this;
    }

    public VideoStillImageSegment slagPerTakt(int takter) {
        this.takter = takter;
        return this;
    }

    public static int taktslagUtregning(int takterPerSlag, float bpm) {
        return (int) (takterPerSlag * bpm / 60);
        /*
        int asd = mikrosekunderPerMinutt / (bpm*slagPerTakt) <- innenfor riktig takt!


                mikrosekunderPerMinutt = takt511
                takt511


        slag /sekund      * 120 Beats


        15 takter * 120 / 60
        30 beats/sekund vs 15 takter * 2 slag/sekund

        30 / 30 = vis hvert bilde
        */
    }
}
