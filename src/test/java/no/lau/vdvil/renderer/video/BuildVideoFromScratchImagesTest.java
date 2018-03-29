package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import static no.lau.vdvil.renderer.video.ImageBuilderWrapper.createVideoPart;
import static no.lau.vdvil.renderer.video.ImageBuilderWrapper.imageStore;
import static no.lau.vdvil.renderer.video.TestData.*;
import static no.lau.vdvil.renderer.video.TestData.norwayBaseKomposition;
import static no.lau.vdvil.snippets.FFmpegFunctions.concatVideoSnippets;
import static no.lau.vdvil.snippets.FFmpegFunctions.countNumberOfFrames;
import static no.lau.vdvil.snippets.FFmpegFunctions.performFFMPEG;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {


    Path sobotaMp3;

    private String result1 = "file:///tmp/from_scratch_images_test_1.mp4";
    private String result2 = "file:///tmp/from_scratch_images_test_2.mp4";
    private String result3 = "file:///tmp/from_scratch_images_test_3.mp4";

    //HighRez
    VideoConfig config = new VideoConfig(1280, 720,DEFAULT_TIME_UNIT.convert(24, MILLISECONDS));
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));

    @Before
    public void setUp() throws MalformedURLException {
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);
    }

    @Test
    public void extractImagesFromNorwayVideo() throws IOException, InterruptedException {
        Komposition buildKomposition = norwayBaseKomposition().filter(24, 16);
        buildKomposition.storageLocation = new MediaFile(new URL(result2), 0l, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchNorwayDVL());

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3.toUri().toURL(), 24);

        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, new VideoConfig(1280, 720, Math.round(1000000 / 24)), false);
    }

    @Test
    //@Ignore
    public void testBuildingMinimally() throws IOException, InterruptedException {
        VideoConfig videoConfig = new VideoConfig(1280, 720, Math.round(1000000/24));
        URL muzik = sobotaMp3.toUri().toURL();

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchNorwayDVL());

        for (int i = 0; i < 1; i++) {
            Segment seg = norwayBaseKomposition().segments.get(i);
            Komposition buildKomposition1 = norwayBaseKomposition().filter(seg.start(), seg.duration());
            buildKomposition1.storageLocation = MediaFile.createEmptyMediaFile(seg, buildKomposition1.bpm, ExtensionType.mp4);
            createVideoPart(videoConfig, fetchKompositions, buildKomposition1, muzik, false);
        }
        //logger.info("Storing file at {}", mf.fileName);
        //assertEquals(mf.checksums, md5Checksum(mf.fileName));
    }

    @Test
    public void testBuildingOnlyNorwayShowing12Frames() throws IOException, InterruptedException {
        VideoConfig videoConfig = new VideoConfig(1280, 720, Math.round(1000000 / 24));
        Komposition baseKomposition = norwayBaseKomposition();
        URL muzik = sobotaMp3.toUri().toURL();

        Segment seg = baseKomposition.segments.get(5);
        Komposition buildKomposition1 = baseKomposition.filter(seg.start(), seg.duration());
        buildKomposition1.storageLocation = new MediaFile(Paths.get("/tmp/norway10.mp4"), 0l, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");
        createVideoPart(videoConfig, Collections.singletonList(fetchNorwayDVL()), buildKomposition1, muzik, false);
        assertEquals(10, countNumberOfFrames(Paths.get("/tmp/norway10.mp4"))); //TODO Test with 12 FRAMESSS!!!!!

        //logger.info("Storing file at {}", mf.fileName);
        //assertEquals(mf.checksums, md5Checksum(mf.fileName));
    }

    @Test
    @Ignore
    public void testCountNumberOfFrames() throws IOException {

        assertEquals(182, countNumberOfFrames(Paths.get("/tmp/from_scratch_images_test_2.mp4")));
        assertEquals(182, countNumberOfFrames(Paths.get("/tmp/from_scratch_images_with_sound.mp4")));
        assertEquals(184, countNumberOfFrames(Paths.get("/tmp/endRez.mp4")));

        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as0.mp4")));
        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as1.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as2.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as3.mp4")));
        assertEquals(79, countNumberOfFrames(Paths.get("/tmp/as4.mp4")));
        assertEquals(10, countNumberOfFrames(Paths.get("/tmp/as5.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as6.mp4")));
        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as7.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as8.mp4")));

    }

    @Test
    @Ignore
    public void testConcatStuff() throws IOException {
        concatVideoSnippets(
                Paths.get("/tmp/endRez.mp4")
//                , Paths.get("/tmp/as0.mp4")
//                , Paths.get("/tmp/as1.mp4")
//                , Paths.get("/tmp/as2.mp4")
//                , Paths.get("/tmp/as3.mp4")
                , Paths.get("/tmp/as4.mp4")
                , Paths.get("/tmp/as5.mp4")
                , Paths.get("/tmp/as6.mp4")
//                , Paths.get("/tmp/as7.mp4")
//                , Paths.get("/tmp/as8.mp4")
        );
    }

    @Test
    //@Ignore
    public void combiningVideoWithAudio() throws IOException {
        Path target = Paths.get("/tmp/rezWithZound.mp4");
        if(Files.exists(target)) {
            Files.delete(target);
        }
        String asd = performFFMPEG("ffmpeg -i /tmp/endRez.mp4 -i "+sobotaMp3.toAbsolutePath().toString()+" -c:v copy -c:a aac -strict experimental " + target.toString());
        System.out.println(asd);
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }

    @Test
    @Ignore //Not working yet
    public void specificVideoCompositionTest() throws IOException, InterruptedException {

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

        MediaFile mf = new MediaFile(new URL(result3), 0l, 128f, "0362c495e294bac76458ca56cdee20ee");
        buildKomposition.storageLocation = mf;
        KompositionPlanner planner = new KompositionPlanner(Collections.singletonList(fetchNorwayDVL()), buildKomposition, sobotaMp3.toUri().toURL(), 15);
        PipeDream<BufferedImage> imageStore = new PipeDream<>();
        ThreadedImageCollector imageCollector = new ThreadedImageCollector(planner.collectPlans(), item -> new WaitingVideoThumbnailsCollector(item, imageStore));
        new Thread(imageCollector).run();
        Thread.sleep(5000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, config);
        assertEquals(mf.getChecksums(), md5Checksum(mf.getFileName()));
    }
}

class ImageBuilderWrapper {
    static ImageStore<BufferedImage> imageStore = new PipeDream<>(250, 1000, 5000, 10);

    static void createVideoPart(VideoConfig videoConfig, List<Komposition> fetchKompositions, Komposition buildKomposition, URL musicUrl, boolean useAudio) {
        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, musicUrl, 24);
        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, videoConfig, useAudio);
    }
}
