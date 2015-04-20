package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import java.io.IOException;
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
    private String result2 = "file:///tmp/from_scratch_images_test_2.mp4";


    @Test
    public void buildWithXuggle() throws IOException {
        Komposition fetchKomposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 64, 64, 4),
                new ImageSampleInstruction("Second capture sequence", 256, 32, 1)
        );
        ImageBufferStore imageStore = new ImageBufferStore();
        //ImageStore imageStore = new ImageFileStore(fetchKomposition, "/tmp/snaps/");

        new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);

        assertEquals(460, imageStore.findImagesByInstructionId("First capture sequence").size());
        assertEquals(162, imageStore.findImagesByInstructionId("Second capture sequence").size());
        //140

        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("First capture sequence", 0, 32),
                new VideoStillImageSegment("Second capture sequence", 32, 30),
                new VideoStillImageSegment("First capture sequence", 64, 30)
                );

        MediaFile mf = new MediaFile(new URL(result), 0f, 128f, "dunno yet");
        buildKomposition.storageLocation = mf;

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    @Test
    public void extractImagesFromNorwayVideo() throws IOException {
        Komposition fetchKomposition = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 8),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 8),
                new TimeStampFixedImageSampleSegment("Flower fjord", 35916667, 47000000, 8),
                new TimeStampFixedImageSampleSegment("Slide Blue mountain top lake", 47083333, 58416667, 8),
                new TimeStampFixedImageSampleSegment("Fjord foss", 58541667, 64041667, 8),
                new TimeStampFixedImageSampleSegment("Fjord like river", 64250000, 68958333, 8),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74583333, 8),
                new TimeStampFixedImageSampleSegment("Mountain range", 74833333, 80250000, 8),
                new TimeStampFixedImageSampleSegment("Omnious fjord Lightbrake", 80291667, 88666667, 8),
                new TimeStampFixedImageSampleSegment("Boat village panorama", 88750000, 97000000, 8),
                new TimeStampFixedImageSampleSegment("Village street", 97083333, 102541667, 8),
                new TimeStampFixedImageSampleSegment("Seaside houses Panorama", 102583333, 108791667, 8),
                new TimeStampFixedImageSampleSegment("Bergen movement", 108916667, 113541667, 8)
        );
        ImageBufferStore imageStore = new ImageBufferStore();
        //ImageStore imageStore = new ImageFileStore(fetchKomposition, "/tmp/snaps/");

        new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);

        assertEquals(232, imageStore.findImagesByInstructionId("Purple Mountains Clouds").size());
        assertEquals(57, imageStore.findImagesByInstructionId("Besseggen").size());
        assertEquals(38, imageStore.findImagesByInstructionId("Norway showing").size());
        assertEquals(191, imageStore.findImagesByInstructionId("Flower fjord").size());
        assertEquals(188, imageStore.findImagesByInstructionId("Slide Blue mountain top lake").size());
        assertEquals(53, imageStore.findImagesByInstructionId("Fjord foss").size());
        assertEquals(58, imageStore.findImagesByInstructionId("Fjord like river").size());
        assertEquals(95, imageStore.findImagesByInstructionId("Dark lake").size());
        assertEquals(88, imageStore.findImagesByInstructionId("Mountain range").size());
        assertEquals(132, imageStore.findImagesByInstructionId("Omnious fjord Lightbrake").size());
        assertEquals(132, imageStore.findImagesByInstructionId("Boat village panorama").size());
        assertEquals(87, imageStore.findImagesByInstructionId("Village street").size());
        assertEquals(85, imageStore.findImagesByInstructionId("Seaside houses Panorama").size());
        assertEquals(48, imageStore.findImagesByInstructionId("Bergen movement").size());

        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("Dark lake", 0, 32),
                new VideoStillImageSegment("Purple Mountains Clouds", 32, 32),
                new VideoStillImageSegment("Slide Blue mountain top lake", 64, 32),
                new VideoStillImageSegment("Slide Blue mountain top lake", 64+32, 16),
                new VideoStillImageSegment("Norway showing", 64+48, 16),
                new VideoStillImageSegment("Fjord like river", 128, 16),
                new VideoStillImageSegment("Fjord foss", 128+16, 16),
                new VideoStillImageSegment("Besseggen", 128+32, 16),
                new VideoStillImageSegment("Omnious fjord Lightbrake", 128+48, 16),
                new VideoStillImageSegment("Seaside houses Panorama", 128+64, 32),
                new VideoStillImageSegment("Boat village panorama", 128+96, 16),

                new VideoStillImageSegment("Bergen movement", 256, 32)
                //new Instruction("inst2", 64, 8, new VideoStillImageSegment("Norway showing", 32, 8),
                //new Instruction("inst4", 56, 16, new VideoStillImageSegment("Flower fjord", 56, 16),
                //new Instruction("inst2", 72, 8, new VideoStillImageSegment("Norway showing", 72, 8))

        );

        MediaFile mf = new MediaFile(new URL(result2), 0f, 128f, "128e4c7ccfd695bebed3ced6774290ca");
        buildKomposition.storageLocation = mf;

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}
