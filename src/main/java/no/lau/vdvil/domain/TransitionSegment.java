package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import no.lau.vdvil.renderer.video.creator.filter.ListFilter;
import no.lau.vdvil.renderer.video.creator.filter.ListModificator;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 * Implementation of a TransitionSegment, used in CrossFading and possibly other kinds of effects requiring two or more video streams
 */
public class TransitionSegment<TYPE> extends SuperSegment implements FilterableSegment<TYPE>, MovableSegment, MetaSegment {

    final Segment startSegment;
    final Segment endSegment;

    public TransitionSegment(Segment startSegment, Segment endSegment, int start, int duration) {
        super("Transition: " + startSegment + " - " + endSegment, start,  duration);

        this.startSegment = startSegment;
        this.endSegment = endSegment;
    }

    ListModificator modificator = new ListModificator();

    public TransitionSegment filter(ListFilter... filters) {
        this.modificator = new ListModificator(filters);
        return this;
    }

    public List applyModifications(List<TYPE> inList) {
        return modificator.applyModifications(inList);
    }

    public void moveStart(long newStart) {
        super.start = newStart;
    }

    public List<String> references() {
        return Arrays.asList(startSegment.id(), endSegment.id());
    }
}
