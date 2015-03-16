package no.lau.vdvil.renderer.video;

import no.lau.vdvil.renderer.video.stigs.Composition;
import no.lau.vdvil.renderer.video.stigs.Instruction;
import org.junit.Test;

import java.util.Collections;

/**
 * Created by stiglau on 16/03/15.
 */
public class ImageCapturingFromVideoTest {

    @Test
    public void testCapturing() {
        String inputFilename = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String outputFilePrefix = "/tmp/snaps/";
        double SECONDS_BETWEEN_FRAMES = 1/10;

        Composition composition = new Composition(Collections.singletonList(new Instruction("Picture capture sequence", 0, 4, 120)));
        new VideoThumbnailsCollector(SECONDS_BETWEEN_FRAMES).run(inputFilename, outputFilePrefix, composition);
    }
}
