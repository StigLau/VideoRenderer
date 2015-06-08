package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig Lau 16/03/15.
 */
public class ImageCapturingFromVideoTest {

    private Logger logger = LoggerFactory.getLogger(ImageCapturingFromVideoTest.class);

    @Test
    public void testCapturing() {
        String inputFilename = "/Users/stiglau/vids/NORWAY-A_Time-Lapse_Adventure.mp4";
        String outputFilePrefix = "/tmp/snaps/NORWAY-A_Time-Lapse_Adventure.mp4/";
        Komposition komposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 0, 256, 15),
                new ImageSampleInstruction("Second capture sequence", 256, 256, 1)
        );
        ImageStore ibs = new ImageFileStore<>(komposition, outputFilePrefix);
        new VideoThumbnailsCollector(ibs).capture(inputFilename, komposition);
        assertEquals(225, ibs.findImagesBySegmentId("First capture sequence").size());
        assertEquals(225, ibs.findImagesBySegmentId("Second capture sequence").size());

        logger.info("Found files:");
        for (Segment instruction : komposition.segments) {
            logger.info("From: {} Duration: {}, BPM: {}, FramesPerBeat: {}", instruction.start(), instruction.duration(), komposition.bpm, ((ImageSampleInstruction)instruction).framesPerBeat);
        }
    }
}
