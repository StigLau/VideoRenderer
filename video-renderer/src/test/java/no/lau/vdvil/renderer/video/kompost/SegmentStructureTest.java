package no.lau.vdvil.renderer.video.kompost;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static no.lau.vdvil.renderer.video.KompositionUtil.performIdUniquenessCheck;

/**
 * @author Stig@Lau.no
 */
public class SegmentStructureTest {
    @Test
    public void testStuff() {
        Komposition videoKomposition1 = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 19750000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74583333, 8)
        );
        Komposition videoKomposition2 = new Komposition(140,
                new TimeStampFixedImageSampleSegment("Bush bush", 41667, 125000, 8),
                new TimeStampFixedImageSampleSegment("Whatever", 11250000, 17625000, 3),
                new TimeStampFixedImageSampleSegment("OMG off", 29375000, 24583333, 2)
        );

        List<Segment> inSegments = new ArrayList<>();
        inSegments.addAll(videoKomposition1.segments);
        inSegments.addAll(videoKomposition2.segments);


        Komposition resultKomposition = new Komposition(130,
                new VideoStillImageSegment("Dark lake", 0, 2).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("OMG off", 2, 2).filter(new TaktSplitter(1)),

                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Whatever", 8, 1)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 9, 1),
                new VideoStillImageSegment("Dark lake", 10, 2)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8)
        );

        performIdUniquenessCheck(inSegments);
        /*
        List<Segment> extractedInSegments = createUniqueSegments(inSegments, resultKomposition.segments);

        List<List<Segment>> result = alignSegments(extractedInSegments);
        assertEquals(6, result.size());
        assertEquals(1, result.get(0).size());
        assertEquals(2, result.get(1).size());
        */
        //Make
    }


}