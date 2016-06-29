package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;

import java.util.List;

/**
 * @author Stig@Lau.no 10/05/15.
 */

public interface SegmentWrapper {
    List<FrameRepresentation> calculateFramesFromSegment();

    Segment segment();

    float bpm();
}
