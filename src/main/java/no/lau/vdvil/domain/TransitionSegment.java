package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.creator.filter.FilterableSegment;
import no.lau.vdvil.renderer.video.creator.filter.ListFilter;
import no.lau.vdvil.renderer.video.creator.filter.ListModificator;

import java.util.Arrays;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class TransitionSegment<TYPE> extends SuperSegment implements FilterableSegment<TYPE>, MovableSegment, MetaSegment {

    final String startSegment;
    final String endSegment;

    public TransitionSegment(String startSegment, String endSegment, int start, int duration) {
        super("Transition: " + startSegment + " - " + endSegment, start,  duration);
        this.startSegment = asReferenceId(startSegment, start, duration);
        this.endSegment = asReferenceId(endSegment, start, duration);
    }

    private String asReferenceId(String segmentShortId, int start, int duration) {
        return segmentShortId + " " + start + " + " + duration;
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
        return Arrays.asList(startSegment, endSegment);
    }
}
