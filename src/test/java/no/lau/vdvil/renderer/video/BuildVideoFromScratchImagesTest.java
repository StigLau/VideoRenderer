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
import java.util.ArrayList;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    String downmixedOriginalVideo = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";
    String theSwingVideo = "/tmp/320_Worlds_Largest_Rope_Swing.mp4";
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

        assertEquals(460, imageStore.findImagesBySegmentId("First capture sequence").size());
        assertEquals(162, imageStore.findImagesBySegmentId("Second capture sequence").size());
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
    public void extractImagesFromNorwayVideo() throws IOException, InterruptedException {
        Komposition fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 20250000, 8),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 4),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Flower fjord", 35916667, 47000000, 8),
                new TimeStampFixedImageSampleSegment("Slide Blue mountain top lake", 47125000, 57750000, 8),
                new TimeStampFixedImageSampleSegment("Fjord like river", 64250000, 68750000, 8),
                new TimeStampFixedImageSampleSegment("Fjord foss", 58541667, 64041667, 8),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74375000, 8),
                new TimeStampFixedImageSampleSegment("Mountain range", 74833333, 80250000, 8),
                new TimeStampFixedImageSampleSegment("Omnious fjord Lightbrake", 80000000, 88125000, 8),
                new TimeStampFixedImageSampleSegment("Boat village panorama", 88750000, 97000000, 8),
                new TimeStampFixedImageSampleSegment("Village street", 97083333, 102541667, 8),
                new TimeStampFixedImageSampleSegment("Seaside houses Panorama", 102583333, 108791667, 8),
                new TimeStampFixedImageSampleSegment("Bergen movement", 108916667, 113541667, 8)
        );
        fetchKompositionNorway.storageLocation= new MediaFile(new URL("file://" + downmixedOriginalVideo), 0f, -1f, "abc");

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(new URL("file://" + theSwingVideo), 0f, 120F, "abc");

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
                //new VideoStillImageSegment("Omnious fjord Lightbrake", 64, 8),
                new VideoStillImageSegment("Smile girl, smile", 64, 8),
                new VideoStillImageSegment("Swing into bridge", 72, 8),
                new VideoStillImageSegment("Swing out from bridge", 80, 8),
                new VideoStillImageSegment("Swing through bridge with mountain smile", 88, 8)
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
        MediaFile mf = new MediaFile(new URL(result2), 0f, 128f, "ee990400c8bc69f4a10c18697f766461");
        buildKomposition.storageLocation = mf;


        ImageBufferStore imageStore = new ImageBufferStore();
        imageStore.setBufferSize(350);


        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);
        new StreamingImageCapturer(fetchKompositions, buildKomposition, imageStore).startUpThreads();

/*
        assertEquals(232, imageStore.findImagesBySegmentId("Purple Mountains Clouds").size());
        assertEquals(57, imageStore.findImagesBySegmentId("Besseggen").size());
        assertEquals(38, imageStore.findImagesBySegmentId("Norway showing").size());
        assertEquals(191, imageStore.findImagesBySegmentId("Flower fjord").size());
        assertEquals(188, imageStore.findImagesBySegmentId("Slide Blue mountain top lake").size());
        assertEquals(53, imageStore.findImagesBySegmentId("Fjord foss").size());
        assertEquals(58, imageStore.findImagesBySegmentId("Fjord like river").size());
        assertEquals(95, imageStore.findImagesBySegmentId("Dark lake").size());
        assertEquals(88, imageStore.findImagesBySegmentId("Mountain range").size());
        assertEquals(132, imageStore.findImagesBySegmentId("Omnious fjord Lightbrake").size());
        assertEquals(132, imageStore.findImagesBySegmentId("Boat village panorama").size());
        assertEquals(87, imageStore.findImagesBySegmentId("Village street").size());
        assertEquals(85, imageStore.findImagesBySegmentId("Seaside houses Panorama").size());
        assertEquals(48, imageStore.findImagesBySegmentId("Bergen movement").size());
        */



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
