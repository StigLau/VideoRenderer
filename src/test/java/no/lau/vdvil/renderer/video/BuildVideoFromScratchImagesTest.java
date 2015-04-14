package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    String downmixedOriginalVideo = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";
    String snapshotFileStorage = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";

    String sobotaMp3 = "/Users/stiglau/vids/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";

    private String result = "file:///tmp/from_scratch_images_test.mp4";


    @Test
    public void buildWithXuggle() throws MalformedURLException {
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 4)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        ImageBufferStore imageStore = new ImageBufferStore();
        //ImageStore imageStore = new ImageFileStore(fetchKomposition, "/tmp/snaps/");

        new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);

        assertEquals(221, imageStore.findImagesByInstructionId("First capture sequence").count());
        assertEquals(141, imageStore.findImagesByInstructionId("Second capture sequence").count());
        //140

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 0, 32, new VideoStillImageSegment("First capture sequence", 0, 32)),
                new Instruction("inst2", 32, 30, new VideoStillImageSegment("Second capture sequence", 32, 30)),
                new Instruction("inst3", 64, 30, new VideoStillImageSegment("First capture sequence", 64, 30))
                );

        buildKomposition.storageLocation = new MediaFile(new URL(result), 0f, 128f, "dunno yet");

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
    }
}
