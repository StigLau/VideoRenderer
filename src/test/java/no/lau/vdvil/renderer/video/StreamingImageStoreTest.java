package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.*;
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
import static org.junit.Assert.assertTrue;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class StreamingImageStoreTest {

    URL downmixedOriginalVideo;
    URL theSwingVideo;
    URL result4;
    URL strippedResult;
    URL sobotaMp3;
    KompositionPlanner planner;
    MediaFile resultingMediaFile;

    @Before
    public void setUp() throws MalformedURLException {
        downmixedOriginalVideo = Paths.get("/tmp/videoTest/NORWAY-A_Time_Lapse_Adventure/NORWAY-A_Time_Lapse_Adventure.mp4").toUri().toURL();
        theSwingVideo = Paths.get("/tmp/videoTest/Worlds_Largest_Rope_Swing/Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        result4 = Paths.get("/tmp/from_scratch_images_test_v4.mp4").toUri().toURL();
        strippedResult = Paths.get("/tmp/streamingImagesStrippedResult.mp4").toUri().toURL();
        sobotaMp3 = Paths.get("/tmp/videoTest/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3").toUri().toURL();

        Komposition fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 19750000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74000000, 100)
        );
        fetchKompositionNorway.storageLocation = new MediaFile(downmixedOriginalVideo, 0f, 120F, "abc");
        //fetchKompositionNorway.framerate = 15;

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0f, 120F, "abc");
        //fetchKompositionSwing.framerate=60;

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
                //new VideoStillImageSegment("Purple Mountains Clouds", 0, 32).filter(new TaktSplitter(4)),
                //new VideoStillImageSegment("Dark lake", 32, 16).filter(new TaktSplitter(4))
                new VideoStillImageSegment("Dark lake", 0, 4).filter(new TaktSplitter(4)),
                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Dark lake", 8, 2)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Red bridge", 10, 2)
                        .filter(new TaktSplitter(1), new Reverter()) ,
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8),
                new VideoStillImageSegment("Smile girl, smile", 24, 16),
                new VideoStillImageSegment("Swing into bridge", 40, 4)
        );
        buildKomposition.framerate = 24;
        buildKomposition.storageLocation = new MediaFile(result4, 0f, 128f, "0e7d51d26f573386c229b772d126754a");
        this.resultingMediaFile = buildKomposition.storageLocation;
        planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24);//<!-- Here is the key!
    }

    @Test
    public void testWhatDoWeGetFromAPlan() {


        SuperPlan buildPlan = (SuperPlan) planner.buildPlan();

        assertEquals(8, buildPlan.getFramePlans().size());
        assertEquals(220, buildPlan.getFrameRepresentations().size());


        List<Plan> collectPlan = planner.collectPlans();
        assertEquals(8, collectPlan.size());
        List<SegmentFramePlan> collectFramePlans = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFramePlans.addAll(((SuperPlan) plan).getFramePlans());
        }
        assertEquals(8, collectFramePlans.size());

        List<FrameRepresentation> collectFrameRepresentations = new ArrayList<>();
        for (Plan plan : collectPlan) {
            collectFrameRepresentations.addAll(((SuperPlan)plan).getFrameRepresentations());
        }
        assertEquals(220, collectFrameRepresentations.size());

        assertEquals(0, buildPlan.getFrameRepresentations().get(0).timestamp);
        assertTrue(buildPlan.getFrameRepresentations().get(0).referenceId().contains("Dark lake"));
        assertEquals(967741, buildPlan.getFrameRepresentations().get(10).timestamp);
        assertTrue(buildPlan.getFrameRepresentations().get(200).referenceId().contains("Swing into bridge"));
        assertEquals(19354838, buildPlan.getFrameRepresentations().get(200).timestamp);

        //The collect timestamp should not start at 0
        assertEquals(69375000, collectFrameRepresentations.get(0).timestamp);

    }

    @Test
    public void testStreamingFromInVideoSource() throws InterruptedException, IOException {
        PipeDream<BufferedImage> imageStore = new PipeDream<>(200, 5000, 1000);
        new Thread(new WaitingVideoThumbnailsCollector(planner.collectPlans(), imageStore)).start();

        System.out.println("Need to know how many pics to retrieve (Preferrably in a planner) before proceeding!");
        Thread.sleep(2000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(),imageStore,new Config(480, 260, DEFAULT_TIME_UNIT.convert(15, MILLISECONDS)));
        assertEquals(220, ((SuperPlan)planner.buildPlan()).getFrameRepresentations().stream().filter(frame -> frame.used).count());

        assertEquals(20, ((SuperPlan) planner.collectPlans().get(0)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(20, ((SuperPlan) planner.collectPlans().get(1)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(10, ((SuperPlan) planner.collectPlans().get(2)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(10, ((SuperPlan) planner.collectPlans().get(3)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(20, ((SuperPlan) planner.collectPlans().get(4)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(40, ((SuperPlan) planner.collectPlans().get(5)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals(80, ((SuperPlan) planner.collectPlans().get(6)).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals("64db0f32dce8f0646ce110cfa0aca841", md5Checksum(resultingMediaFile.fileName));
    }

    @Test
    public void testSegmentStrip() throws InterruptedException, IOException {
        PipeDream<BufferedImage> imageStore = new PipeDream<>(200, 5000, 1000);
        TimeStampFixedImageSampleSegment segment = new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74000000, 100);
        new Thread(new StrippedWaitingVideoThumbnailsCollector(segment,downmixedOriginalVideo, imageStore)).start();

        System.out.println("Need to know how many pics to retrieve (Preferrably in a planner) before proceeding!");
        Plan buildPlan = new TimeStampFixedSegmentPlan(segment, strippedResult.getFile());
        long framerate = Math.round(new Double(1000000/24).doubleValue());
        System.out.println("Short wait to make sure collection thread starts before this (build).");
        Thread.sleep(2000);
        CreateVideoFromScratchImages.createVideo(buildPlan, imageStore, new Config(1280, 720, framerate));
        //assertEquals(111, ((SuperPlan)planner.buildPlan()).getFrameRepresentations().stream().filter(frame -> frame.used).count());
        assertEquals("a84f83f0d90ce68f03e23c20c849dfb6", md5Checksum(resultingMediaFile.fileName));
    }



    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}

