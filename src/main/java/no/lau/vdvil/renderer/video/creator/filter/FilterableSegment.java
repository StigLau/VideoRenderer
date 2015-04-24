package no.lau.vdvil.renderer.video.creator.filter;

import no.lau.vdvil.domain.Segment;
import java.util.List;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public interface FilterableSegment<TYPE> extends Segment {
    public List<TYPE> applyModifications(List<TYPE> inList);
}
