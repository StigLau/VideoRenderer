package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import no.lau.vdvil.renderer.video.creator.filter.ListFilter;
import no.lau.vdvil.renderer.video.creator.filter.ListModificator;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class VideoStillImageSegment<TYPE> extends SuperSegment implements FilterableSegment<TYPE>, MovableSegment {

    //Whether the list is to be reverted
    //TODO Change to Reverse
    public boolean reverted = false;
    public double fromLimit = 0;
    public double untilLimit = 1;
    public int takter = -1;

    public VideoStillImageSegment(String id, int start, int duration) {
        super(id, start, duration);
    }

    public VideoStillImageSegment revert() {
        reverted = true;
        return this;
    }

    public boolean isReversed() {
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

    ListModificator modificator = new ListModificator();

    public VideoStillImageSegment filter(ListFilter... filters) {
        this.modificator = new ListModificator(filters);
        return this;
    }

    public List applyModifications(List<TYPE> inList) {
        return modificator.applyModifications(inList);
    }

    public void moveStart(long newStart) {
        super.start = newStart;
    }
}
