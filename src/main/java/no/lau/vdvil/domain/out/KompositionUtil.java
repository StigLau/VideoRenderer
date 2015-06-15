package no.lau.vdvil.domain.out;

import no.lau.vdvil.domain.MovableSegment;
import no.lau.vdvil.domain.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple filter to reduce the number of segments for kompost
 * @author Stig@Lau.no 15/06/15.
 */
public class KompositionUtil {

    static Logger logger = LoggerFactory.getLogger(KompositionUtil.class);

    public static List<Segment> filterByTime(List<Segment> unfileredSegments, long filterStart, long filterDuration) {
        long filterEnd = filterStart + filterDuration;
        List<Segment> filteredPartsList = new ArrayList<>();
        for (Segment segment  : unfileredSegments) {

            long end = segment.start() + segment.duration();
            if(filterStart <= segment.start() && end <= filterEnd) {
                if (segment instanceof MovableSegment) {
                    MovableSegment movableSegment = (MovableSegment) segment;
                    movableSegment.moveStart(segment.start() - filterStart);
                    filteredPartsList.add(segment);
                } else {
                    throw new RuntimeException(segment.id() + " was not a movable segment");
                }
            } else if(end <= filterStart || filterEnd <= segment.start()) {
                //Is outside filters range
                logger.debug("starting at {} was filtered out of the composition", segment.start());
            }
        }
        return filteredPartsList;
    }
}
