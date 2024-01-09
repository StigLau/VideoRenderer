package no.lau.vdvil.collector.plan;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.SegmentWrapper;
import java.util.List;

/**
 * @author Stig@Lau.no 10/05/15.
 */

public interface FramePlan {
    List<FrameRepresentation> calculateFramesFromSegment();

    SegmentWrapper wrapper();

    String getId();
}
