package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.PersistentWriter;
import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.*;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.domain.utils.KompositionUtils;
import no.lau.vdvil.plan.*;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.ImageFileStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static no.lau.CommonFunctions.md5Checksum;
import static no.lau.vdvil.domain.UrlHandler.urlCreator;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static no.lau.vdvil.renderer.video.TestData.norwayRemoteUrl;
import static no.lau.vdvil.renderer.video.TestData.sobotaMp3RemoteUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
@Tag("IntegrationTest")
public class BuildVideoFromStaticImagesAndVideoTest {

    private VideoSegmentPlanFactory framePlanFactory = new VideoSegmentPlanFactoryImpl();
    Path downmixedOriginalVideo;
    Path theSwingVideo;
    Path snapshotFileStorage;

    Path sobotaMp3;

    Komposition fetchKompositionNorway;
    Komposition fetchKompositionSwing;
    Komposition fetchKompositionStillImages;
    Komposition fetchKompositionStillImages2;

    private Path result1 = Path.of("/tmp/from_scratch_images_test_1.mp4");
    private Path result3a = Path.of("/tmp/from_scratch_images_test_3a.mp4");
    private Path result3b = Path.of("/tmp/from_scratch_images_test_3b.mp4");
    private Path result3c = Path.of("/tmp/from_scratch_images_test_3c.mp4");

    //HighRez
    //VideoConfig config = new VideoConfig(1280, 720,DEFAULT_TIME_UNIT.convert(24, MILLISECONDS));
    private VideoConfig config2 = new VideoConfig(1280, 720, Math.round(1000000/24));
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));
    Logger logger = LoggerFactory.getLogger(BuildVideoFromStaticImagesAndVideoTest.class);

    @BeforeEach
    public void setUp() {
        //Low Rez
        //downmixedOriginalVideo = Paths.get("/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        //theSwingVideo = Paths.get("/tmp/320_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        //HighRez
        downmixedOriginalVideo = fetch(norwayRemoteUrl);
        theSwingVideo = Path.of("/tmp/kompost/Worlds_Largest_Rope_Swing/Worlds_Largest_Rope_Swing.mp4");

        snapshotFileStorage = Path.of("/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/");
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);


        fetchKompositionStillImages = new Komposition(128,
                new StaticImagesSegment("Still Image Fun 1",
                        ClassLoader.getSystemResource("images/Cow_goes_moo.png").toString(),
                        ClassLoader.getSystemResource("images/What_the_fox_sed_-Mouse.png").toString(),
                        ClassLoader.getSystemResource("images/Slide_Blue_mountain_top_lake.png").toString(),
                        ClassLoader.getSystemResource("images/Slide_Blue_mountain_top_lake2.png").toString()
                ));
        fetchKompositionStillImages2 = new Komposition(128,
                new StaticImagesSegment("Still Image Fun 2",
                        ClassLoader.getSystemResource("images/Slide_Blue_mountain_top_lake2.png").toString()
                ));
        Path orgPath = Path.of("file://tmp/kompost");
        fetchKompositionStillImages.storageLocation = new LocalMediaFile(urlCreator(orgPath), orgPath, 0L, -1f, "abc");

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
        fetchKompositionNorway.storageLocation= new LocalMediaFile(urlCreator(downmixedOriginalVideo), downmixedOriginalVideo, 0L, -1f, "abc");

        fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34034000, 34993292, 15),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new LocalMediaFile(urlCreator(theSwingVideo), theSwingVideo, 0L, 120F, "abc123");
    }

    @Test
    public void buildVideoFromStaticImagesAndVideo() throws InterruptedException {
        Komposition buildKomposition =  new Komposition(124,
                new VideoStillImageSegment("Still Image Fun 1", 0, 8),
                new VideoStillImageSegment("Besseggen", 8, 8),
                new VideoStillImageSegment("Still Image Fun 1", 16, 4),
                new VideoStillImageSegment("Purple Mountains Clouds", 20, 12)

        );//.filter(16, 16);
        LocalMediaFile mf = new LocalMediaFile(urlCreator(result1), result1, 0L, 128f, "370b9b8ee872fe38342f1dd2e410ef0a");
        buildKomposition.storageLocation = mf;


        PipeDream<BufferedImage> pipeDream = new PipeDream<>(60, 250, 500, 10);

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionStillImages2);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, framePlanFactory);

        new Thread(new ThreadedImageCollector(planner.collectPlans(),
                plan -> plan.collector(pipeDream, 41666))).start();
        Thread.sleep(1000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), pipeDream, config2);

        logger.info("Storing file at {}", mf.getReference());
        assertEquals(mf.getChecksums(), md5Checksum(mf.getReference()));
    }

    @Test
    public void testStillImageCollection() {
        Komposition buildKomposition =  new Komposition(124,
                //new VideoStillImageSegment("Still Image Fun 1", 0, 8)
                new VideoStillImageSegment("Besseggen", 0, 8)
                , new VideoStillImageSegment("Purple Mountains Clouds", 8, 16)
        );
        buildKomposition.storageLocation = new LocalMediaFile(urlCreator(result3a), result3a, 0L, 128f, "7ea06f7a19fea1dfcefef1b6b30730b4");

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, framePlanFactory);

        ImageStore<BufferedImage> pipeDream = new ImageFileStore<>(buildKomposition, "/tmp/snaps");
        new ThreadedImageCollector(planner.collectPlans(),
                plan -> plan.collector(pipeDream, -1)).run();
    }


    @Test
    public void extractStillImagesFromVideoWriter() {
        Komposition buildKomposition =  new Komposition(124,
                //new VideoStillImageSegment("Still Image Fun 1", 0, 8)
                new VideoStillImageSegment("Besseggen", 0, 8)
                , new VideoStillImageSegment("Purple Mountains Clouds", 8, 16)
        );
        buildKomposition.storageLocation = new LocalMediaFile(urlCreator(result3b), result3b, 0L, 128f, "7ea06f7a19fea1dfcefef1b6b30730b4");

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, framePlanFactory);

        PipeDream<BufferedImage> pipeDream = new PipeDream<>(1000, 5000, 1000, 1);
        for (Plan planIter : planner.collectPlans()) {
            new Thread(new ThreadedImageCollector(Collections.singletonList(planIter),
                    plan -> plan.collector(pipeDream, -1))).start();
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), pipeDream, config2, PersistentWriter.create("/tmp/komposttest.mp4"), false, true);
        pipeDream.emptyCache();
    }

    @Test
    public void countPlanningResult() {
        VideoStillImageSegment first = new VideoStillImageSegment("Besseggen", 0, 12);
        VideoStillImageSegment second = new VideoStillImageSegment("Purple Mountains Clouds", 8, 12);
        Komposition buildKomposition = new Komposition(124,
                new TransitionSegment(first, second, 8, 4), first, second);
        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionStillImages2);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, framePlanFactory);
        Map<Long, List<FrameRepresentation>> representations = new TreeMap<>();
        for (FrameRepresentation rep : ((SuperPlan) planner.buildPlan()).getFrameRepresentations()) {
            if(representations.containsKey(rep.timestamp)) {
                representations.get(rep.timestamp).add(rep);
            } else {
                List<FrameRepresentation> newList  = new ArrayList<>();
                newList.add(rep);
                representations.put(rep.timestamp, newList);
            }
        }
        assertEquals(232, representations.size());
        assertEquals(1, representations.get(3833272L).size());
        assertEquals(3, representations.get(3874938L).size());
        assertEquals(3, representations.get(5749908L).size());
        assertEquals(1, representations.get(5791574L).size());
    }

    @Test
    public void segmentTransitions() throws InterruptedException {
        VideoStillImageSegment first = new VideoStillImageSegment("Besseggen", 0, 12);
        VideoStillImageSegment second = new VideoStillImageSegment("Purple Mountains Clouds", 8, 12);
        VideoStillImageSegment third = new VideoStillImageSegment("Besseggen", 16, 12);
        Komposition buildKomposition =  new Komposition(124,
                new TransitionSegment(first, second, 8, 4),
                new TransitionSegment(second, third, 16, 4),
                first,
                second,
                third);
        LocalMediaFile mf = new LocalMediaFile(urlCreator(result3c), result3c, 0L, 128f, "2a745683577324ee59421e6649f1a7de");
        buildKomposition.storageLocation = mf;

        PipeDream<BufferedImage> pipeDream = new PipeDream<>(30, 250, 500, 10);
        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionStillImages);
        fetchKompositions.add(fetchKompositionStillImages2);
        fetchKompositions.add(fetchKompositionNorway);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, framePlanFactory);


        KompositionUtils.printImageRepresentationImages(planner);

        CollectorWrapper callback = plan -> plan.collector(pipeDream, config2.framerate());
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (Plan plan : planner.collectPlans()) {
            executor.execute(callback.callBack((ImageCollectable) plan));
        }
        Thread.sleep(1000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), pipeDream, config2);

        logger.info("Storing file at {}", mf.getReference());
        assertEquals(mf.getChecksums(), md5Checksum(mf.getReference()));
    }
}
