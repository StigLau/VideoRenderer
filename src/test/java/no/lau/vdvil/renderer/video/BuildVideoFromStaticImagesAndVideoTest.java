package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.collector.ThreadedImageCollector;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.StaticImagesSegment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromStaticImagesAndVideoTest {

    URL downmixedOriginalVideo;
    URL theSwingVideo;
    URL snapshotFileStorage;

    URL sobotaMp3;

    Komposition fetchKompositionNorway;
    Komposition fetchKompositionSwing;
    Komposition fetchKompositionStillImages;
    Komposition fetchKompositionStillImages2;

    private String result3 = "file:///tmp/from_scratch_images_test_3.mp4";

    //HighRez
    VideoConfig config = new VideoConfig(1280, 720,DEFAULT_TIME_UNIT.convert(24, MILLISECONDS));
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));
    Logger logger = LoggerFactory.getLogger(BuildVideoFromStaticImagesAndVideoTest.class);

    @Before
    public void setUp() throws MalformedURLException {
        //Low Rez
        //downmixedOriginalVideo = Paths.get("/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        //theSwingVideo = Paths.get("/tmp/320_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        //HighRez
        downmixedOriginalVideo = Paths.get("/tmp/kompost/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        theSwingVideo = Paths.get("/tmp/kompost/Worlds_Largest_Rope_Swing/Worlds_Largest_Rope_Swing.mp4").toUri().toURL();

        snapshotFileStorage = Paths.get("/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/").toUri().toURL();
        sobotaMp3 = Paths.get("/tmp/kompost/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3").toUri().toURL();


        fetchKompositionStillImages = new Komposition(128,
                new StaticImagesSegment("Still Image Fun 1",
                        getClass().getClassLoader().getResource("images/Cow_goes_moo.png").toString(),
                        getClass().getClassLoader().getResource("images/What_the_fox_sed_-Mouse.png").toString(),
                        getClass().getClassLoader().getResource("images/Slide_Blue_mountain_top_lake.png").toString()
                ));
        fetchKompositionStillImages2 = new Komposition(128,
                new StaticImagesSegment("Still Image Fun 2",
                        getClass().getClassLoader().getResource("images/Slide_Blue_mountain_top_lake2.png").toString()
                ));
        fetchKompositionStillImages.storageLocation = new MediaFile(new URL("file://tmp/kompost"), 0f, -1f, "abc");

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
    public void buildVideoFromStaticImagesAndVideo() throws IOException, InterruptedException {
        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("Still Image Fun 1", 0, 8),
                new VideoStillImageSegment("Besseggen", 8, 8),
                new VideoStillImageSegment("Still Image Fun 2", 16, 8)
                /*,
                new VideoStillImageSegment("Purple Mountains Clouds", 28, 4),
                new VideoStillImageSegment("Dark lake", 28, 4).revert(),

                new VideoStillImageSegment("Purple Mountains Clouds", 32, 4),
                new VideoStillImageSegment("Slide Blue mountain top lake", 36, 8), //Forsøplet
                new VideoStillImageSegment("Flower fjord", 44, 8),
                new VideoStillImageSegment("Fjord like river", 52, 12)*/
        );//.filter(16, 16);
        MediaFile mf = new MediaFile(new URL(result3), 0f, 128f, "0c5157a3e964d7d7e641e6f7aa3379b3");
        buildKomposition.storageLocation = mf;


        PipeDream<BufferedImage> pipeDream = new PipeDream<>(60, 250, 500, 10);

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionStillImages2);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24);

        new Thread(new ThreadedImageCollector(planner.collectPlans(),
                plan -> plan.collector(pipeDream, 41666))).start();
        Thread.sleep(1000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), pipeDream, new VideoConfig(1280, 720, Math.round(1000000/24)));

        logger.info("Storing file at {}", mf.fileName);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));
        pipeDream.printPipedreamSize();
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}
