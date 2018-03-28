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
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
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
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static no.lau.vdvil.renderer.video.TestData.norwayRemoteUrl;
import static no.lau.vdvil.renderer.video.TestData.sobotaMp3RemoteUrl;
import static no.lau.vdvil.renderer.video.ImageBuilderWrapper.createVideoPart;
import static no.lau.vdvil.renderer.video.ImageBuilderWrapper.imageStore;
import static no.lau.vdvil.snippets.FFmpegFunctions.concatVideoSnippets;
import static no.lau.vdvil.snippets.FFmpegFunctions.countNumberOfFrames;
import static no.lau.vdvil.snippets.FFmpegFunctions.performFFMPEG;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 11/04/15.
 */
public class BuildVideoFromScratchImagesTest {

    Path downmixedOriginalVideo;
    URL theSwingVideo;
    URL snapshotFileStorage;

    Path sobotaMp3;

    Komposition fetchKompositionNorway;
    Komposition fetchKompositionSwing;

    Komposition baseKomposition;

    private String result1 = "file:///tmp/from_scratch_images_test_1.mp4";
    private String result2 = "file:///tmp/from_scratch_images_test_2.mp4";
    private String result3 = "file:///tmp/from_scratch_images_test_3.mp4";

    //HighRez
    VideoConfig config = new VideoConfig(1280, 720,DEFAULT_TIME_UNIT.convert(24, MILLISECONDS));
    //Low Rez
    //Config config = new Config(320, 200,DEFAULT_TIME_UNIT.convert(15, MILLISECONDS));
    Logger logger = LoggerFactory.getLogger(BuildVideoFromScratchImagesTest.class);

    @Before
    public void setUp() throws MalformedURLException {
        //Low Rez
        //downmixedOriginalVideo = Paths.get("/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4").toUri().toURL();
        //theSwingVideo = Paths.get("/tmp/320_Worlds_Largest_Rope_Swing.mp4").toUri().toURL();
        downmixedOriginalVideo = fetch(norwayRemoteUrl);
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);

        //HighRez
        theSwingVideo = Paths.get("/tmp/kompost/Worlds_Largest_Rope_Swing/Worlds_Largest_Rope_Swing.mp4").toUri().toURL();

        snapshotFileStorage = Paths.get("/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/").toUri().toURL();
        sobotaMp3 = fetch(sobotaMp3RemoteUrl);

        fetchKompositionNorway = new Komposition(125,
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
        fetchKompositionNorway.storageLocation= new MediaFile(downmixedOriginalVideo.toUri().toURL(), 0l, -1f, "abc");

        fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34034000, 34993292, 15),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(theSwingVideo, 0l, 120F, "abc123");

        baseKomposition =  new Komposition(125,
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
    }

    @Test
    public void extractImagesFromNorwayVideo() throws IOException, InterruptedException {
        Komposition buildKomposition = baseKomposition.filter(24, 16);
        MediaFile mf = new MediaFile(new URL(result2), 0l, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");
        buildKomposition.storageLocation = mf;

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        KompositionPlanner planner = new KompositionPlanner(fetchKompositions, buildKomposition, sobotaMp3.toUri().toURL(), 24);

        CollectorWrapper callback = plan -> new WaitingVideoThumbnailsCollector(plan, imageStore);
        ExecutorService collector = Executors.newFixedThreadPool(1);
        for (Plan plan : planner.collectPlans()) {
            collector.execute(callback.callBack((ImageCollectable) plan));
        }
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, new VideoConfig(1280, 720, Math.round(1000000 / 24)));
    }

    @Test
    @Ignore
    public void testBuildingMinimally() throws IOException, InterruptedException {
        VideoConfig videoConfig = new VideoConfig(1280, 720, Math.round(1000000/24));
        URL muzik = sobotaMp3.toUri().toURL();

        List<Komposition> fetchKompositions = new ArrayList<>();
        fetchKompositions.add(fetchKompositionNorway);
        fetchKompositions.add(fetchKompositionSwing);

        for (int i = 0; i < 10; i++) {
            Segment seg = baseKomposition.segments.get(i);
            Komposition buildKomposition1 = baseKomposition.filter(seg.start(), seg.duration());
            buildKomposition1.storageLocation = new MediaFile(Paths.get("/tmp/as"+i+".mp4"), 0l, 125f, "7aa709f7caff0446a4a9aa2865f4efd2");
            createVideoPart(videoConfig, fetchKompositions, buildKomposition1, muzik, false);
        }
        //logger.info("Storing file at {}", mf.fileName);
        //assertEquals(mf.checksums, md5Checksum(mf.fileName));
    }

    @Test
    public void testCountNumberOfFrames() throws IOException {
        System.out.println(44 + 44+90 + 90 + 79 + 10+ 90 + 44 + 90 );
        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as0.mp4")));
        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as1.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as2.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as3.mp4")));
        assertEquals(79, countNumberOfFrames(Paths.get("/tmp/as4.mp4")));
        assertEquals(10, countNumberOfFrames(Paths.get("/tmp/as5.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as6.mp4")));
        assertEquals(44, countNumberOfFrames(Paths.get("/tmp/as7.mp4")));
        assertEquals(90, countNumberOfFrames(Paths.get("/tmp/as8.mp4")));
        assertEquals(598, countNumberOfFrames(Paths.get("/tmp/endRez.mp4")));
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
        KompositionPlanner planner = new KompositionPlanner(Collections.singletonList(fetchKompositionNorway), buildKomposition, sobotaMp3.toUri().toURL(), 15);
        PipeDream<BufferedImage> imageStore = new PipeDream<>();
        ThreadedImageCollector imageCollector = new ThreadedImageCollector(planner.collectPlans(), item -> new WaitingVideoThumbnailsCollector(item, imageStore));
        new Thread(imageCollector).run();
        Thread.sleep(5000);
        CreateVideoFromScratchImages.createVideo(planner.buildPlan(), imageStore, config);
        assertEquals(mf.checksums, md5Checksum(mf.fileName));
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
