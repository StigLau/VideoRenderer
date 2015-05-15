package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no
 */
public class CountSegmentImages {
    @Test
    public void countImages() {
        Komposition buildKomposition = new Komposition(120,
                new VideoStillImageSegment("Red bridge", 0, 16),
                new VideoStillImageSegment("Swing into bridge", 16, 16),
                new VideoStillImageSegment("Red bridge", 32, 32)
        );
        buildKomposition.framerate = 15;

        KompositionPlanner planner = new KompositionPlanner(buildKomposition.segments, buildKomposition.bpm, buildKomposition.framerate);
        assertEquals(3, planner.plans.size());
        assertEquals(120, planner.plans.get(0).findUnusedFramesAtTimestamp(9999999).size());
        assertEquals(120, planner.plans.get(1).findUnusedFramesAtTimestamp(99999999999l).size());
        assertEquals(240, planner.plans.get(2).findUnusedFramesAtTimestamp(99999999999l).size());
    }
}

