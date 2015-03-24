package no.lau.vdvil.renderer.video;

import no.lau.vdvil.renderer.video.stigs.Composition;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.Instruction;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
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
        int bpm = 120;
        List<Instruction> instr = new ArrayList<>();
        instr.add(new ImageSampleInstruction("First capture sequence", 0, 4, bpm, 2));
        instr.add(new ImageSampleInstruction("Second capture sequence", 32, 8, bpm, 1));
        Composition composition = new Composition(instr, bpm);
        new VideoThumbnailsCollector().capture(inputFilename, outputFilePrefix, composition);
        assertEquals(8, composition.instructions.get(0).relevantFiles.size());
        assertEquals(36, composition.instructions.get(1).relevantFiles.size());

        logger.info("Found files:");
        for (Instruction instruction : instr) {
            logger.info("From: {} Duration: {}, BPM: {}, FramesPerBeat: {}", instruction.from, instruction.duration, instruction.bpm, ((ImageSampleInstruction)instruction).framesPerBeat);
            for (String file : instruction.relevantFiles) {
                System.out.println("File: " + file);
            }
        }
    }
}
