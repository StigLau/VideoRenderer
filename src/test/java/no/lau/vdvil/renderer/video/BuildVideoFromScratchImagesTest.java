package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import java.io.IOException;
import java.net.URL;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    String downmixedOriginalVideo = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";
    String snapshotFileStorage = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";

    String sobotaMp3 = "/Users/stiglau/vids/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";

    private String result1 = "file:///tmp/from_scratch_images_test_1.mp4";
    private String result2 = "file:///tmp/from_scratch_images_test_2.mp4";
    private String result3 = "file:///tmp/from_scratch_images_test_3.mp4";


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

        MediaFile mf = new MediaFile(new URL(result1), 0f, 128f, "dunno yet");
        buildKomposition.storageLocation = mf;

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    @Test
    public void extractImagesFromNorwayVideo() throws IOException {
        Komposition fetchKomposition = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 4),
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
                new VideoStillImageSegment("Dark lake", 0, 4),
                new VideoStillImageSegment("Dark lake", 4, 4).revert(),
                new VideoStillImageSegment("Dark lake", 8, 8).revert(),
                new VideoStillImageSegment("Purple Mountains Clouds", 16, 8),
                new VideoStillImageSegment("Purple Mountains Clouds", 24, 7).revert(),
                new VideoStillImageSegment("Norway showing", 31, 1),
                new VideoStillImageSegment("Slide Blue mountain top lake", 32, 8),
                new VideoStillImageSegment("Flower fjord", 40, 4),
                new VideoStillImageSegment("Slide Blue mountain top lake", 44, 8).revert(),
                new VideoStillImageSegment("Fjord like river", 52, 11),
                //new VideoStillImageSegment("Fjord foss", 52, 11),//Has parts of Slide Blue!!

                //new VideoStillImageSegment("Besseggen", 60, 3),
                new VideoStillImageSegment("Norway showing", 63, 1).revert(),
                new VideoStillImageSegment("Omnious fjord Lightbrake", 64, 8)
                /*
                new VideoStillImageSegment("Seaside houses Panorama", 72, 4),
                new VideoStillImageSegment("Boat village panorama", 76, 8),
                new VideoStillImageSegment("Bergen movement", 84, 4)
                */
                //new Instruction("inst2", 64, 8, new VideoStillImageSegment("Norway showing", 32, 8),
                //new Instruction("inst4", 56, 16, new VideoStillImageSegment("Flower fjord", 56, 16),
                //new Instruction("inst2", 72, 8, new VideoStillImageSegment("Norway showing", 72, 8))

        );
        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        buildKomposition.width = 320;
        buildKomposition.height = 200;
        MediaFile mf = new MediaFile(new URL(result2), 0f, 128f, "c24f323e8ef4588ee30c78dccb9b3472");
        buildKomposition.storageLocation = mf;

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }

    @Test
    public void specificVideoCompositionTest() throws IOException {
        Komposition fetchKomposition = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 4),
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

        new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);


        int bpm = 124;
        Komposition buildKomposition =  new Komposition(bpm,
                /*
                new VideoStillImageSegment("Purple Mountains Clouds", 0, 16).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 16, 16).filter(new TaktSplitter(2)),
                new VideoStillImageSegment("Purple Mountains Clouds", 32, 16).filter(new TaktSplitter(4)),
                new VideoStillImageSegment("Purple Mountains Clouds", 48, 16).filter(new TaktSplitter(1))
                */
                new VideoStillImageSegment("Dark lake", 0, 4).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Dark lake", 8, 4)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8)
        );
        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        buildKomposition.width = 320;
        buildKomposition.height = 200;
        MediaFile mf = new MediaFile(new URL(result3), 0f, 128f, "9bf2c55d6ef8bc7c384ba21f2920e9d1");
        buildKomposition.storageLocation = mf;

        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }
}
