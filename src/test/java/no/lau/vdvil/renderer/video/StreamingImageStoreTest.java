package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageBufferStore;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class StreamingImageStoreTest {

    String downmixedOriginalVideo = "/tmp/320_NORWAY-A_Time-Lapse_Adventure.mp4";

    @Test
    public void testStreamingFromInVideoSource() throws InterruptedException {
        Komposition fetchKomposition = new Komposition(128,
                new TimeStampFixedImageSampleSegment("Purple Mountains Clouds", 7541667, 21125000, 8),
                new TimeStampFixedImageSampleSegment("Besseggen", 21250000, 27625000, 2),
                new TimeStampFixedImageSampleSegment("Dark lake", 69375000, 74583333, 8)
        );
        ImageBufferStore imageStore = new ImageBufferStore();

        new StreamingImageCapturer(fetchKomposition, imageStore, downmixedOriginalVideo).startUpThreads();
        //new VideoThumbnailsCollector(imageStore).capture(downmixedOriginalVideo, fetchKomposition);


        int bpm = 124;
        Komposition buildKomposition = new Komposition(bpm,
                /*
                new VideoStillImageSegment("Purple Mountains Clouds", 0, 16).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 16, 16).filter(new TaktSplitter(2)),
                new VideoStillImageSegment("Purple Mountains Clouds", 32, 16).filter(new TaktSplitter(4)),
                new VideoStillImageSegment("Purple Mountains Clouds", 48, 16).filter(new TaktSplitter(1))
                */
                new VideoStillImageSegment("Dark lake", 0, 4).filter(new TaktSplitter(1)),
                new VideoStillImageSegment("Purple Mountains Clouds", 4, 4)
                        .filter(new PercentageSplitter(0, 0.5), new TaktSplitter(1)),
                new VideoStillImageSegment("Dark lake", 8, 4)
                        .filter(new TaktSplitter(1), new Reverter()),
                new VideoStillImageSegment("Purple Mountains Clouds", 12, 4)
                        .filter(new PercentageSplitter(0.5, 1), new Reverter(), new TaktSplitter(2)),
                new VideoStillImageSegment("Dark lake", 16, 8)
        );
        buildKomposition.framerate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        buildKomposition.width = 320;
        buildKomposition.height = 200;
        //MediaFile mf = new MediaFile(new URL(result3), 0f, 128f, "9bf2c55d6ef8bc7c384ba21f2920e9d1");
        //buildKomposition.storageLocation = mf;

        //CreateVideoFromScratchImages.createVideo(buildKomposition, sobotaMp3, imageStore);
        //assertEquals(mf.checksum, md5Checksum(mf.fileName));
        Thread.sleep(10000);

        assertEquals(232, imageStore.findImagesByInstructionId("Purple Mountains Clouds").size());
        assertEquals(57, imageStore.findImagesByInstructionId("Besseggen").size());
        assertEquals(95, imageStore.findImagesByInstructionId("Dark lake").size());

    }

}


class StreamingImageCapturer {

    final Komposition komposition;
    List<ImageCapturer> imageCapturers = new ArrayList<>();
    final ImageStore capturer;
    final String videoFile;
    final Logger log = LoggerFactory.getLogger(StreamingImageCapturer.class);

    public StreamingImageCapturer(Komposition komposition, ImageStore capturer, String videoFile) {
        this.komposition = komposition;
        this.capturer = capturer;
        this.videoFile = videoFile;
    }

    public void startUpThreads() {
        for (Segment segment : komposition.segments) {
            log.info("Starting segment {}", segment.id());
            ImageCapturer imageCapturer = new ImageCapturer(segment, capturer, videoFile, komposition.bpm);
            imageCapturers.add(imageCapturer);
            new Thread(imageCapturer).start();
        }
    }


}

//Responsible for extracting the images. Will wait for more work to do
class ImageCapturer implements Runnable {

    private final Segment segment;
    private final ImageStore imageStoreCapturer;
    private final String videoFile;
    final float bpm;

    public ImageCapturer(Segment segment, ImageStore imageStoreCapturer, String videoFile, float bpm) {
        this.segment = segment;
        this.imageStoreCapturer = imageStoreCapturer;
        this.videoFile = videoFile;
        this.bpm = bpm;
    }

    @Override
    public void run() {
        new WaitingVideoThumbnailsCollector(imageStoreCapturer).capture(videoFile, Collections.singletonList(segment), bpm);
    }
}


//Responsible for gathering images, but also knowing when to acquire the next set og images
/*
class ImageStoreCapturer<TYPE> implements ImageStore<TYPE>{

    @Override
    public List<TYPE> getImageAt(Long timeStamp, Komposition komposition) {
        return null;
    }

    @Override
    public void store(TYPE image, Long timeStamp, String segmentId) {

    }

    @Override
    public List<TYPE> findImagesByInstructionId(String instructionId) {
        return null;
    }
}*/