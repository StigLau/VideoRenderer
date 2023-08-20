package no.lau.vdvil.renderer.video;

import no.lau.CommonFunctions;
import no.lau.vdvil.collector.*;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Komposition;

import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.creator.VideoBuilderWrapper;
import no.lau.vdvil.snippets.FFmpegFunctions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static no.lau.vdvil.domain.MediaFile.md5Checksum;
import static no.lau.vdvil.renderer.video.TestData.*;
import static no.lau.vdvil.renderer.video.TestData.norwayBaseKomposition;
import static no.lau.vdvil.snippets.FFmpegFunctions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
@Tag("IntegrationTest")
class BuildVideoFromScratchImagesTest {


    Path sobotaMp3;

    private Path result1 = Path.of("file:///tmp/from_scratch_images_test_1.mp4");
    private Path result2 = Path.of("file:///tmp/from_scratch_images_test_2.mp4");
    private Path result3 = Path.of("file:///tmp/from_scratch_images_test_3.mp4");

    //HighRez
    VideoConfig config = new VideoConfig(1280, 720,DEFAULT_TIME_UNIT.convert(24, MILLISECONDS));
    PipeDream imageStore;
    VideoBuilderWrapper videoBuilder;
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));

    @BeforeEach
    public void setUp() {
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);
        imageStore = new PipeDream(250, 1000, 5000, 10);
        videoBuilder = new VideoBuilderWrapper(imageStore);
    }

    @Test
    void extractImagesFromNorwayVideo() throws IOException, URISyntaxException {
        Komposition buildKomposition = norwayBaseKomposition().filter(24, 16);
        buildKomposition.storageLocation = new MediaFile(result2, 0L, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchNorwayDVL());

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3, 24);

        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, new VideoConfig(1280, 720, Math.round(1000000 / 24)), false);
    }

    @Test
    //@Disabled
    void testBuildingMinimally() throws IOException {
        VideoConfig videoConfig = new VideoConfig(1280, 720, Math.round(1000000/24));
        Path muzik = sobotaMp3;

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchNorwayDVL());

        for (int i = 0; i < 1; i++) {
            Segment seg = norwayBaseKomposition().segments.get(i);
            Komposition buildKomposition1 = norwayBaseKomposition().filter(seg.start(), seg.duration());
            buildKomposition1.storageLocation = new MediaFile(CommonFunctions.createTempPath("testFile", ".mp4"), -1L, -1f, "");
            videoBuilder.createVideoPart(videoConfig, fetchKompositions, buildKomposition1, muzik, false);
        }
        //logger.info("Storing file at {}", mf.fileName);
        //assertEquals(mf.checksums, md5Checksum(mf.fileName));
    }

    @Test
    void testBuildingOnlyNorwayShowing12Frames() throws IOException, InterruptedException {
        VideoConfig videoConfig = new VideoConfig(1280, 720, Math.round(1000000 / 24));
        Komposition baseKomposition = norwayBaseKomposition();
        Path muzik = sobotaMp3;

        Segment seg = baseKomposition.segments.get(5);
        Komposition buildKomposition1 = baseKomposition.filter(seg.start(), seg.duration());
        buildKomposition1.storageLocation = new MediaFile(Path.of("/tmp/norway10.mp4"), 0L, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");
        videoBuilder.createVideoPart(videoConfig, Collections.singletonList(fetchNorwayDVL()), buildKomposition1, muzik, false);
        assertEquals(10, countNumberOfFrames(Path.of("/tmp/norway10.mp4"))); //TODO Test with 12 FRAMESSS!!!!!

        //logger.info("Storing file at {}", mf.fileName);
        //assertEquals(mf.checksums, md5Checksum(mf.fileName));
    }

    @Test
    @Disabled
    void testCountNumberOfFrames() throws IOException {

        assertEquals(182, countNumberOfFrames(Path.of("/tmp/from_scratch_images_test_2.mp4")));
        assertEquals(182, countNumberOfFrames(Path.of("/tmp/from_scratch_images_with_sound.mp4")));
        assertEquals(184, countNumberOfFrames(Path.of("/tmp/endRez.mp4")));

        assertEquals(44, countNumberOfFrames(Path.of("/tmp/as0.mp4")));
        assertEquals(44, countNumberOfFrames(Path.of("/tmp/as1.mp4")));
        assertEquals(90, countNumberOfFrames(Path.of("/tmp/as2.mp4")));
        assertEquals(90, countNumberOfFrames(Path.of("/tmp/as3.mp4")));
        assertEquals(79, countNumberOfFrames(Path.of("/tmp/as4.mp4")));
        assertEquals(10, countNumberOfFrames(Path.of("/tmp/as5.mp4")));
        assertEquals(90, countNumberOfFrames(Path.of("/tmp/as6.mp4")));
        assertEquals(44, countNumberOfFrames(Path.of("/tmp/as7.mp4")));
        assertEquals(90, countNumberOfFrames(Path.of("/tmp/as8.mp4")));

    }

    @Test
    @Disabled
    void testConcatStuff() throws IOException {
        Path result = protocolConcatVideoSnippets(ExtensionType.mp4,
//                , Paths.get("/tmp/as0.mp4")
//                , Paths.get("/tmp/as1.mp4")
//                , Paths.get("/tmp/as2.mp4")
//                , Paths.get("/tmp/as3.mp4")
                Paths.get("/tmp/as4.mp4")
                , Paths.get("/tmp/as5.mp4")
                , Paths.get("/tmp/as6.mp4")
//                , Paths.get("/tmp/as7.mp4")
//                , Paths.get("/tmp/as8.mp4")
        );
        System.out.println("result.getFileName() = " + result.getFileName());
        System.out.println("Files.size(result) = " + Files.size(result));
    }

    @Test
    //@Disabled
    void combiningVideoWithAudio() throws IOException {
        Path target = combineAudioAndVideo(Path.of("/tmp/endRez.mp4"), sobotaMp3);
        System.out.println(target);
    }

    @Test
    @Disabled //Not working yet
    void specificVideoCompositionTest() throws InterruptedException {

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

        MediaFile mf = new MediaFile(result3, 0L, 128f, "0362c495e294bac76458ca56cdee20ee");
        buildKomposition.storageLocation = mf;
        KompositionPlanner planner = new KompositionPlanner(Collections.singletonList(fetchNorwayDVL()), buildKomposition, sobotaMp3, 15);
        PipeDream<BufferedImage> imageStore = new PipeDream<>();
        ThreadedImageCollector imageCollector = new ThreadedImageCollector(planner.collectPlans(), item -> new WaitingVideoThumbnailsCollector(item, imageStore));
        new Thread(imageCollector).run();
        Thread.sleep(5000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, config);
        assertEquals(mf.getChecksums(), md5Checksum(mf.getReference()));
    }

    @Test
    void testSnippingStuff() throws IOException {
        Path testFile = Path.of("/tmp/testur/myTestFile.mp4");
        FFmpegFunctions.snippetSplitter(fetch(norwayRemoteUrl), 56222833, 60477083, testFile);
        assertEquals(103, countNumberOfFrames(testFile));

    }

    @Test
    void testSnippingStuffBergen() throws IOException {
        Path origFile = fetch(bergenRemoteUrl);
        Path testFile = Path.of("/tmp/testur/myTestFile2.mp4");
        FFmpegFunctions.snippetSplitter(origFile, 56222833, 60477083, testFile);
        assertEquals("144930000/6044789", FFmpegFunctions.fetchFrameInfo(origFile));
        assertEquals("24000/1001", FFmpegFunctions.fetchFrameInfo(testFile));
        assertEquals(102, countNumberOfFrames(testFile));

    }
}
