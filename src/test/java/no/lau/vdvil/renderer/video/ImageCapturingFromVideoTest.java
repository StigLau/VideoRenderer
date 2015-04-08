package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
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
        String inputFilename = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String outputFilePrefix = "/tmp/snaps/";
        Komposition komposition = new Komposition(128,
                new Instruction("Capture some pics", 0, 40, new ImageSampleInstruction("First capture sequence", 0, 4, 2)),
                new Instruction("Capture some pics 2", 0, 40, new ImageSampleInstruction("Second capture sequence", 32, 8, 1))
        );
        new VideoThumbnailsCollector().capture(inputFilename, outputFilePrefix, komposition);
        assertEquals(8, ((VideoStillImageSegment) komposition.instructions.get(0).segment).getStillImages().length);
        assertEquals(36, ((VideoStillImageSegment) komposition.instructions.get(1).segment).getStillImages().length);

        logger.info("Found files:");
        for (Instruction instruction : komposition.instructions) {
            logger.info("From: {} Duration: {}, BPM: {}, FramesPerBeat: {}", instruction.from, instruction.duration, komposition.bpm, ((ImageSampleInstruction)instruction.segment).framesPerBeat);
            //TODO Check this out
            //for (String file : instruction.segment.relevantFiles) {
            //    System.out.println("File: " + file);
            //}
        }
    }
}
