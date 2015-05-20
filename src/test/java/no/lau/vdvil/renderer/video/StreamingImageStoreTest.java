package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.collector.KompositionPlanner;
import no.lau.vdvil.collector.SegmentFramePlan;
import no.lau.vdvil.collector.StreamingImageCapturer;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
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
    private Komposition buildKomposition;
    private List<Komposition> fetchKompositions;

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

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0f, 120F, "abc");

        fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        int bpm = 124;
        buildKomposition = new Komposition(bpm,
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
    }

/*
    @Test
    public void testFile() throws MalformedURLException, URISyntaxException {
        //File file = new File(new URL(sobotaMp3).toURI());
        //assert file.canRead();

        File a = Paths.get("C:\\vids\\320_Worlds_Largest_Rope_Swing.mp4").toFile();
        System.out.println("a = " + a.canRead());
    }
*/
    @Test
    public void testStreamingFromInVideoSource() throws InterruptedException, IOException {
        ImageBufferStore imageStore = new ImageBufferStore();
        imageStore.setBufferSize(10);

        StreamingImageCapturer capturer = new StreamingImageCapturer(fetchKompositions, buildKomposition, imageStore);
        List<KompositionPlanner> planners = capturer.createPlanners();
        capturer.startUpThreads(planners);
        //new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);

        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        buildKomposition.width = 320;
        buildKomposition.height = 200;
        MediaFile mf = new MediaFile(result4, 0f, 128f, "0e7d51d26f573386c229b772d126754a");
        buildKomposition.storageLocation = mf;

        Thread.sleep(10000);
        CreateVideoFromScratchImages.createVideo(buildKomposition, planners, imageStore, sobotaMp3);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));

        assertEquals(325, imageStore.findImagesBySegmentId("Purple Mountains Clouds").size());
        assertEquals(152, imageStore.findImagesBySegmentId("Besseggen").size());
        assertEquals(124, imageStore.findImagesBySegmentId("Dark lake").size());
    }

    @Test
    public void testPlannerSorting() {
        StreamingImageCapturer capturer = new StreamingImageCapturer(fetchKompositions, buildKomposition, new ImageBufferStore());
        List<KompositionPlanner> planners = capturer.createPlanners();

        for (KompositionPlanner planner : planners) {
            for (SegmentFramePlan plan : planner.plans) {
                for (FrameRepresentation frameRepresentation : plan.frameRepresentations) {
                    System.out.println("" + frameRepresentation.timestamp + "  \t" + plan.originalSegment.id());
                }
                //System.out.println(plan.originalSegment.startCalculated(120) + "\t" + plan.originalSegment.id());
            }
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


    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}

