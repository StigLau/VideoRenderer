package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.junit.Ignore;
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
    @Ignore //Download first
    public void testCapturing() {
        String inputFilename = "/tmp/streamingImagesStrippedResult.mp4";
        String outputFilePrefix = "/tmp/snaps/1280_NORWAY-A_Time-Lapse_Adventure.mp4/";
        //String inputFilename = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";
        //String outputFilePrefix = "/tmp/snaps/320_NORWAY-A_Time-Lapse_Adventure.mp4/";
        //String inputFilename = "/tmp/NORWAY-A_Time-Lapse_Adventure.mp4";
        //String outputFilePrefix = "/tmp/snaps/NORWAY-A_Time-Lapse_Adventure.mp4_2/";
        Komposition komposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 0, 128, 15)
                //new ImageSampleInstruction("Second capture sequence", 300, 16, 1)
        );
        ImageStore ibs = new ImageFileStore<>(komposition, outputFilePrefix);
        new VideoThumbnailsCollector(ibs).capture(inputFilename, komposition);
        assertEquals(44, ibs.findImagesBySegmentId("First capture sequence 48 + 4").size());

        logger.info("Found files:");
        for (Segment instruction : komposition.segments) {
            logger.info("From: {} Duration: {}, BPM: {}, FramesPerBeat: {}", instruction.start(), instruction.duration(), komposition.bpm, ((ImageSampleInstruction)instruction).framesPerBeat);
        }
    }
}
