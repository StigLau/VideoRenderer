package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    String downmixedOriginalVideo = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
    String snapshotFileStorage = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";
    private String result = "file:///tmp/from_scratch_images_test.mp4";


    @Test
    public void buildWithXuggle() throws MalformedURLException {
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        new VideoThumbnailsCollector().capture(downmixedOriginalVideo, snapshotFileStorage, fetchKomposition);
        assertEquals(719, ((ImageSampleInstruction) fetchKomposition.instructions.get(0).segment).collectedImages().size());
        assertEquals(359, ((ImageSampleInstruction) fetchKomposition.instructions.get(1).segment).collectedImages().size());

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 4, 28, fetchKomposition.instructions.get(0).segment),
                new Instruction("inst2", 32, 32, fetchKomposition.instructions.get(1).segment));

        buildKomposition.storageLocation = new MediaFile(new URL(result), 0f, 128f, "dunno yet");

        CreateVideoFromScratchImages.createVideo(buildKomposition);
    }

}
