package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.FramePlan;
import no.lau.vdvil.collector.plan.VideoSegmentPlanFactoryImpl;
import no.lau.vdvil.domain.LocalMediaFile;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static no.lau.CommonFunctions.md5Checksum;
import static no.lau.vdvil.domain.UrlHandler.urlCreator;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static no.lau.vdvil.renderer.video.TestData.norwayRemoteUrl;
import static no.lau.vdvil.renderer.video.TestData.sobotaMp3RemoteUrl;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Stig@Lau.no 24/04/15.
 */
@Tag("IntegrationTest")
public class StreamingImageStoreTest {

    Path downmixedOriginalVideo;
    Path theSwingVideo;
    Path result4;
    Path strippedResult;
    Path sobotaMp3;
    KompositionPlanner planner;
    MediaFile resultingMediaFile;

    @BeforeEach
    public void setUp() {
        downmixedOriginalVideo = fetch(norwayRemoteUrl);
        theSwingVideo = Path.of("/tmp/kompost/Worlds_Largest_Rope_Swing/Worlds_Largest_Rope_Swing.mp4");
        result4 = Path.of("/tmp/from_scratch_images_test_v4.mp4");
        strippedResult = Path.of("/tmp/streamingImagesStrippedResult.mp4");
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);

        Komposition fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 19750000, 8),
                new TimeStampFixedImageSampleSegment("Flower fjord", 35500000, 46250000, 24),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74000000, 100)
        );
        fetchKompositionNorway.storageLocation = new LocalMediaFile(urlCreator(downmixedOriginalVideo), downmixedOriginalVideo, 0L, 120F, "abc");

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new LocalMediaFile(urlCreator(theSwingVideo), theSwingVideo, 0L, 120F, "abc");
        //fetchKompositionSwing.framerate=60;

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        int bpm = 124;
        Komposition buildKomposition = new Komposition(bpm,
                new VideoStillImageSegment("Flower fjord", 0, 32)
        );
        buildKomposition.framerate = 24;
        buildKomposition.storageLocation = new LocalMediaFile(urlCreator(result4), result4, 0L, 128f, "0e7d51d26f573386c229b772d126754a");
        this.resultingMediaFile = buildKomposition.storageLocation;
        planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24, new VideoSegmentPlanFactoryImpl());//<!-- Here is the key!
    }

    @Test
    public void testWhatDoWeGetFromAPlan() {


        SuperPlan buildPlan = (SuperPlan) planner.buildPlan();

        assertEquals(1, buildPlan.getFramePlans().size());
        assertEquals(372, buildPlan.getFrameRepresentations().size());
        // want 447

        List<Plan> collectPlan = planner.collectPlans();
        assertEquals(1, collectPlan.size());
        List<FramePlan> collectFramePlans = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFramePlans.addAll(((SuperPlan) plan).getFramePlans());
        }
        assertEquals(1, collectFramePlans.size());

        List<FrameRepresentation> collectFrameRepresentations = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFrameRepresentations.addAll(((SuperPlan)plan).getFrameRepresentations());
        }
        assertEquals(372, buildPlan.getFrameRepresentations().size());

        assertEquals(0, buildPlan.getFrameRepresentations().get(0).timestamp);
        assertTrue(buildPlan.getFrameRepresentations().get(0).referenceId().contains("Flower fjord"));
        assertEquals(0, buildPlan.getFrameRepresentations().get(0).timestamp);
        assertEquals(41666, buildPlan.getFrameRepresentations().get(1).timestamp);
        assertEquals(416660, buildPlan.getFrameRepresentations().get(10).timestamp);
        assertTrue(buildPlan.getFrameRepresentations().get(200).referenceId().contains("Flower fjord"));
        assertEquals(15458086, buildPlan.getFrameRepresentations().get(372-1).timestamp);

        //The collect timestamp should not start at 0
        assertEquals(35541096, collectFrameRepresentations.get(0).timestamp);

    }

    @Test
    @Disabled //Test often fails!!
    public void testStreamingFromInVideoSource() throws InterruptedException {
        PipeDream<BufferedImage> imageStore = new PipeDream<>(200, 5000, 1000, 10);
        ThreadedImageCollector collector = new ThreadedImageCollector(planner.collectPlans(), plan -> new WaitingVideoThumbnailsCollector(plan, imageStore));
        new Thread(collector).start();

        System.out.println("Need to know how many pics to retrieve (Preferrably in a planner) before proceeding!");
        Thread.sleep(2000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(),imageStore,new VideoConfig(480, 260, 6000));
        assertEquals(372, ((SuperPlan)planner.buildPlan()).getFrameRepresentations().stream().filter(frame -> frame.used).count());

        assertEquals(258, ((SuperPlan) planner.collectPlans().get(0)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        /*
        assertEquals(258, ((SuperPlan) planner.collectPlans().get(1)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(258, ((SuperPlan) planner.collectPlans().get(2)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(258, ((SuperPlan) planner.collectPlans().get(3)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(258, ((SuperPlan) planner.collectPlans().get(4)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(192, ((SuperPlan) planner.collectPlans().get(5)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(384, ((SuperPlan) planner.collectPlans().get(6)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(96, ((SuperPlan) planner.collectPlans().get(7)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        */
        assertEquals("74a7aedb2e450e1c38ff99a7f7bd5ad3", md5Checksum(resultingMediaFile.getReference())); //Note something wrong with the result - it doesnt move!!?!
    }

    @Test
    @Disabled //Segment strip doesnt stop
    public void testSegmentStrip() throws InterruptedException {
        PipeDream<BufferedImage> imageStore = new PipeDream<>(200, 5000, 1000, 10);
        TimeStampFixedImageSampleSegment segment = new TimeStampFixedImageSampleSegment("Flower fjord", 35500000, 46250000, 24);
        new Thread(new ThreadedImageCollector(
                new StrippedWaitingVideoThumbnailsCollector(segment,downmixedOriginalVideo, imageStore))).start();

        System.out.println("Need to know how many pics to retrieve (Preferrably in a planner) before proceeding!");
        Plan buildPlan = new TimeStampFixedSegmentPlan(segment, strippedResult);
        long framerate = Math.round((double) (1000000 / 24));
        System.out.println("Short wait to make sure collection thread starts before this (build).");
        Thread.sleep(2000);
        CreateVideoFromScratchImages.createVideo(buildPlan, imageStore, new VideoConfig(1280, 720, framerate));
        //assertEquals(111, ((SuperPlan)planner.buildPlan()).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals("5156c7b907707065aa281e63065b4c37", md5Checksum(resultingMediaFile.getReference()));
    }
}

