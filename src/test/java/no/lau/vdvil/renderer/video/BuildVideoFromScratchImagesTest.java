package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.collector.StreamingImageCapturer;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    URL downmixedOriginalVideo;
    URL theSwingVideo;
    URL snapshotFileStorage;

    URL sobotaMp3;

    Komposition fetchKompositionNorway;
    Komposition fetchKompositionSwing;

    private String result1 = "file:///tmp/from_scratch_images_test_1.mp4";
    private String result2 = "file:///tmp/from_scratch_images_test_2.mp4";
    private String result3 = "file:///tmp/from_scratch_images_test_3.mp4";

    //HighRez
    Config config = new Config(1280, 720,DEFAULT_TIME_UNIT.convert(30, MILLISECONDS));
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));
    Logger logger = LoggerFactory.getLogger(BuildVideoFromScratchImagesTest.class);

    @Before
    public void setUp() throws MalformedURLException {
        //Low Rez
        //downmixedOriginalVideo = Paths.get("/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        //theSwingVideo = Paths.get("/tmp/320_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        //HighRez
        downmixedOriginalVideo = Paths.get("/tmp/1280_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        theSwingVideo = Paths.get("/tmp/1280_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();

        snapshotFileStorage = Paths.get("/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/").toUri().toURL();
        sobotaMp3 = Paths.get("/Users/stiglau/vids/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3").toUri().toURL();

        fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 19750000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Norway showing", 30166667, 34541667, 4),
                new TimeStampFixedImageSampleSegment("Flower fjord", 35500000, 46250000, 8),
                new TimeStampFixedImageSampleSegment("Slide Blue mountain top lake", 47000000, 57000000, 8),
                new TimeStampFixedImageSampleSegment("Fjord foss", 58541667, 62875000, 8),
                new TimeStampFixedImageSampleSegment("Fjord like river", 64250000, 68125000, 8),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74000000, 8),
                new TimeStampFixedImageSampleSegment("Mountain range", 74750000, 79500000, 8),
                new TimeStampFixedImageSampleSegment("Omnious fjord Lightbrake", 79750000, 87750000, 8),
                new TimeStampFixedImageSampleSegment("Boat village panorama", 88125000, 96125000, 8),
                new TimeStampFixedImageSampleSegment("Village street", 96500000, 101750000, 8),
                new TimeStampFixedImageSampleSegment("Seaside houses Panorama", 102000000, 107125000, 8),
                new TimeStampFixedImageSampleSegment("Bergen movement", 107500000, 112750000, 8)
        );
        fetchKompositionNorway.storageLocation= new MediaFile(downmixedOriginalVideo, 0f, -1f, "abc");

        fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34034000, 34993292, 15),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0f, 120F, "abc123");
    }

    @Test
    @Ignore //Not sure how to keep this feature
    public void buildWithXuggle() throws IOException {
        Komposition fetchKomposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 64, 64, 4),
                new ImageSampleInstruction("Second capture sequence", 256, 32, 1)
        );

        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("First capture sequence", 0, 32),
                new VideoStillImageSegment("Second capture sequence", 32, 30),
                new VideoStillImageSegment("First capture sequence", 64, 30)
                );

        MediaFile mf = new MediaFile(new URL(result1), 0f, 128f, "49ed1e68ba7b600e1f55d1cb0a178b2b");
        buildKomposition.storageLocation = mf;

        KompositionPlanner planner = new KompositionPlanner(Collections.singletonList(fetchKomposition), buildKomposition, 15);
        PipeDream imageStore = new PipeDream();
        StreamingImageCapturer.startUpThreads(planner.collectPlans(), imageStore);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, sobotaMp3, config);

        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    @Test
    public void extractImagesFromNorwayVideo() throws IOException, InterruptedException {
        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("Dark lake", 0, 4),
                new VideoStillImageSegment("Dark lake", 4, 4).revert(),
                new VideoStillImageSegment("Dark lake", 8, 8).revert(),

                new VideoStillImageSegment("Purple Mountains Clouds", 16, 8),
                new VideoStillImageSegment("Purple Mountains Clouds", 24, 7).revert(), //Forsøplet
                new VideoStillImageSegment("Norway showing", 31, 1),
                new VideoStillImageSegment("Slide Blue mountain top lake", 32, 8), //Forsøplet
                new VideoStillImageSegment("Flower fjord", 40, 4),
                new VideoStillImageSegment("Slide Blue mountain top lake", 44, 8).revert(),
                new VideoStillImageSegment("Fjord like river", 52, 11),
                //new VideoStillImageSegment("Fjord foss", 56, 4),//Forsøplet Has parts of Slide Blue!!

                //new VideoStillImageSegment("Besseggen", 60, 3),
                new VideoStillImageSegment("Norway showing", 63, 1).revert(),
                new VideoStillImageSegment("Swing into bridge", 64, 8),
                new VideoStillImageSegment("Swing through bridge with mountain smile", 72, 8),
                new VideoStillImageSegment("Smile girl, smile", 80, 8),
                new VideoStillImageSegment("Swing out from bridge", 88, 12)
        );
        MediaFile mf = new MediaFile(new URL(result2), 0f, 128f, "68211470f443e842613b8d15c6a8174b");
        buildKomposition.storageLocation = mf;


        PipeDream imageStore = new PipeDream();
        imageStore.setBufferSize(100);


        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, 30);
        StreamingImageCapturer.startUpThreads(planner.collectPlans(), imageStore);

        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, sobotaMp3, config);
        logger.info("Storing file at {}", mf.fileName);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }

    @Test
    public void specificVideoCompositionTest() throws IOException {

        int bpm = 124;
        Komposition buildKomposition =  new Komposition(bpm,
                new VideoStillImageSegment("Dark lake", 0, 4).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Dark lake", 8, 4)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8)
        );

        MediaFile mf = new MediaFile(new URL(result3), 0f, 128f, "6b98411e786e2e0e0f3bf80d8fcd889b");
        buildKomposition.storageLocation = mf;
        KompositionPlanner planner = new KompositionPlanner(Collections.singletonList(fetchKompositionNorway), buildKomposition, 15);
        PipeDream imageStore = new PipeDream();
        StreamingImageCapturer.startUpThreads(planner.collectPlans(), imageStore);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, sobotaMp3, config);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
    }
}
