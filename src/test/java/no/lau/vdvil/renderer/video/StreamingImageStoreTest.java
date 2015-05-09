package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static no.lau.vdvil.renderer.video.KompositionUtil.alignSegments;
import static no.lau.vdvil.renderer.video.KompositionUtil.createUniqueSegments;
import static no.lau.vdvil.renderer.video.KompositionUtil.performIdUniquenessCheck;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class StreamingImageStoreTest {

    String downmixedOriginalVideo = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";
    String theSwingVideo = "/tmp/320_Worlds_Largest_Rope_Swing.mp4";
    private String result4 = "file:///tmp/from_scratch_images_test_4.mp4";
    String sobotaMp3 = "/Users/stiglau/vids/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";

    @Test
    public void testStreamingFromInVideoSource() throws InterruptedException, IOException {
        Komposition fetchKompositionNorway = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74583333, 8)
        );
        fetchKompositionNorway.storageLocation = new MediaFile(new URL("file://" + downmixedOriginalVideo), 0f, 120F, "abc");

        Komposition fetchKompositionSwing = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Red bridge", 2919583, 6047708, 8),
                new TimeStampFixedImageSampleSegment("Swing into bridge", 22439083, 26484792, 8),
                new TimeStampFixedImageSampleSegment("Swing out from bridge", 26526500, 28194833, 8),
                new TimeStampFixedImageSampleSegment("Swing second out from bridge", 32323958, 33908875, 8),
                new TimeStampFixedImageSampleSegment("Smile girl, smile", 34200833, 34993292, 8),
                new TimeStampFixedImageSampleSegment("Swing through bridge with mountain smile", 45128417, 46713333, 8)
        );
        fetchKompositionSwing.storageLocation = new MediaFile(new URL("file://" + theSwingVideo), 0f, 120F, "abc");

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

        ImageBufferStore imageStore = new ImageBufferStore();
        imageStore.setBufferSize(400);

        new StreamingImageCapturer(fetchKompositions, buildKomposition, imageStore).startUpThreads();
        //new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);

        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        buildKomposition.width = 320;
        buildKomposition.height = 200;
        MediaFile mf = new MediaFile(new URL(result4), 0f, 128f, "0e7d51d26f573386c229b772d126754a");
        buildKomposition.storageLocation = mf;

        //Thread.sleep(10000);
        CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        assertEquals(mf.checksum, md5Checksum(mf.fileName));

        assertEquals(325, imageStore.findImagesBySegmentId("Purple Mountains Clouds").size());
        assertEquals(152, imageStore.findImagesBySegmentId("Besseggen").size());
        assertEquals(124, imageStore.findImagesBySegmentId("Dark lake").size());
    }

    public String md5Checksum(URL url) throws IOException {
        return DigestUtils.md5Hex(url.openStream());
    }
}

class StreamingImageCapturer {

    final List<Komposition> fetchKompositions;
    final Komposition buildKomposition;
    List<ImageCapturer> imageCapturers = new ArrayList<>();
    final ImageStore capturer;
    final Logger log = LoggerFactory.getLogger(StreamingImageCapturer.class);

    public StreamingImageCapturer(Komposition fetchKomposition, Komposition buildKomposition, ImageStore capturer) {
        this(Collections.singletonList(fetchKomposition), buildKomposition, capturer);
    }

    public StreamingImageCapturer(List<Komposition> fetchKompositions, Komposition buildKomposition, ImageStore capturer) {
        this.fetchKompositions = fetchKompositions;
        this.buildKomposition = buildKomposition;
        this.capturer = capturer;
    }

    public void startUpThreads() {
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
            List<Segment> extractedInSegments = createUniqueSegments(fetchKomposition.segments, buildKomposition.segments);

            int segmentGroup = 0;
            for (List<Segment> alignedSegments : alignSegments(extractedInSegments)) {
                String ids = "";
                for (Segment segment : alignedSegments) {
                    ids += "\n\t" + segment.id();
                }
                log.info("Segment group {}: {}", ++segmentGroup, ids);
                ImageCapturer imageCapturer = new ImageCapturer(alignedSegments, capturer, fetchKomposition.storageLocation.fileName.getFile(), fetchKomposition.bpm);
                imageCapturers.add(imageCapturer);
                new Thread(imageCapturer).start();
            }
        }
    }
}

//Responsible for extracting the images. Will wait for more work to do
class ImageCapturer implements Runnable {

    private final List<Segment> segments;
    private final ImageStore imageStoreCapturer;
    private final String videoFile;
    final float bpm;

    public ImageCapturer(List<Segment> segments, ImageStore imageStoreCapturer, String videoFile, float bpm) {
        this.segments = segments;
        this.imageStoreCapturer = imageStoreCapturer;
        this.videoFile = videoFile;
        this.bpm = bpm;
    }

    @Override
    public void run() {
        new WaitingVideoThumbnailsCollector(imageStoreCapturer).capture(videoFile, segments, bpm);
    }
}