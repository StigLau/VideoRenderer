package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
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
        String inputFilename = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
        String outputFilePrefix = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";
        Komposition komposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        ImageStore ibs = new ImageBufferStore();
        new VideoThumbnailsCollector(ibs).capture(inputFilename, komposition);
        assertEquals(719, ibs.findImagesByInstructionId("First capture sequence").count());
        assertEquals(359, ibs.findImagesByInstructionId("Second capture sequence").count());

        logger.info("Found files:");
        for (Instruction instruction : komposition.instructions) {
            logger.info("From: {} Duration: {}, BPM: {}, FramesPerBeat: {}", instruction.from, instruction.duration, komposition.bpm, ((ImageSampleInstruction)instruction.segment).framesPerBeat);
        }
    }
}
