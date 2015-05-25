package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.collector.StreamingImageCapturer;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Test;
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
 * @author Stig@Lau.no 24/04/15.
 */
public class StreamingImageStoreTest {

     URL downmixedOriginalVideo;
    URL theSwingVideo;
    URL result4;
    String sobotaMp3 = "/tmp/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";
    KompositionPlanner planner;

    @Before
    public void setUp() throws MalformedURLException {
        downmixedOriginalVideo = Paths.get("/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        theSwingVideo = Paths.get("/tmp/320_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        result4 = Paths.get("/tmp/from_scratch_images_test_4.mp4").toUri().toURL();

        Komposition fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74583333, 8)
        );
        fetchKompositionNorway.storageLocation = new MediaFile(downmixedOriginalVideo, 0f, 120F, "abc");
        fetchKompositionNorway.framerate = 5;

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0f, 120F, "abc");
        fetchKompositionSwing.framerate=10;

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        int bpm = 124;
        Komposition buildKomposition = new Komposition(bpm,
                /*
                new VideoStillImageSegment("Purple Mountains Clouds", 0, 16).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 16, 16).filter(new TaktSplitter(2)),
                new VideoStillImageSegment("Purple Mountains Clouds", 32, 16).filter(new TaktSplitter(4)),
                new VideoStillImageSegment("Purple Mountains Clouds", 48, 16).filter(new TaktSplitter(1))
                */
                new VideoStillImageSegment("Dark lake", 0, 4).filter(new TaktSplitter(4)),
                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Dark lake", 8, 2)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Red bridge", 10, 2)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8),
                new VideoStillImageSegment("Smile girl, smile", 24, 16),
                new VideoStillImageSegment("Swing into bridge", 40, 4)
        );
        buildKomposition.framerate = 15;
        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        //buildKomposition.width = 320;
        //buildKomposition.height = 200;
        buildKomposition.storageLocation = new MediaFile(result4, 0f, 128f, "0e7d51d26f573386c229b772d126754a");
        planner = new KompositionPlanner(fetchKompositions, buildKomposition);
    }

    @Test
    public void testWhatDoWeGetFromAPlan() {


        SuperPlan buildPlan = (SuperPlan) planner.buildPlan();

        assertEquals(8, buildPlan.getFramePlans().size());
        assertEquals(660000, buildPlan.getFrameRepresentations().size());


        List<Plan> collectPlan = planner.collectPlans();
        assertEquals(5, collectPlan.size());
        List<SegmentFramePlan> collectFramePlans = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFramePlans.addAll(((SuperPlan) plan).getFramePlans());
        }
        assertEquals(21, collectFramePlans.size());

        List<FrameRepresentation> collectFrameRepresentations = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFrameRepresentations.addAll(((SuperPlan)plan).getFrameRepresentations());
        }
        assertEquals(1355, collectFrameRepresentations.size());

    }

    @Test
    public void testStreamingFromInVideoSource() throws InterruptedException, IOException {
        PipeDream imageStore = new PipeDream();
        imageStore.setBufferSize(10);


        StreamingImageCapturer.startUpThreads(planner.collectPlans(), imageStore);
        //new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);


        Thread.sleep(1000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(),imageStore, sobotaMp3, new Config(480, 260, DEFAULT_TIME_UNIT.convert(15, MILLISECONDS)));
        //assertEquals(mf.checksum, md5Checksum(mf.fileName)); //TODO Validate

        assertEquals(325, imageStore.findImagesBySegmentId("Purple Mountains Clouds").size());
        assertEquals(152, imageStore.findImagesBySegmentId("Besseggen").size());
        assertEquals(124, imageStore.findImagesBySegmentId("Dark lake").size());
    }

    /* TODO Do we need this?
    @Test
    public void testPlannerSorting() {


            for (SegmentFramePlan plan : planner.plans) {
                for (FrameRepresentation frameRepresentation : plan.frameRepresentations) {
                    System.out.println("" + frameRepresentation.timestamp + "  \t" + plan.originalSegment.id());
                }
                //System.out.println(plan.originalSegment.startCalculated(120) + "\t" + plan.originalSegment.id());
            }

        assertEquals(12, planners.size());
        SegmentFramePlan plan = planners.get(0).plans.get(0);
        assertEquals("Dark lake0", plan.originalSegment.id());
        assertEquals(0, plan.frameRepresentations.get(0).timestamp);
        SegmentFramePlan plan2 = planners.get(1).plans.get(0);
        assertEquals("Dark lake1", plan2.originalSegment.id());
        assertEquals(3870967, plan2.frameRepresentations.get(0).timestamp);


        assertEquals("Swing into bridge11", planners.get(11).plans.get(0).originalSegment.id());

    }
    */


    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}

