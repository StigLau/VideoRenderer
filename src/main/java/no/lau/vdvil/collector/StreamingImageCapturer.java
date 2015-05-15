package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static no.lau.vdvil.renderer.video.KompositionUtil.createUniqueSegments;
import static no.lau.vdvil.renderer.video.KompositionUtil.performIdUniquenessCheck;

/**
 * @author Stig@Lau.no 15/05/15.
 */
public class StreamingImageCapturer {

    final List<Komposition> fetchKompositions;
    final Komposition buildKomposition;
    List<ImageCapturer> imageCapturers = new ArrayList<>();
    final ImageStore capturer;
    final Logger log = LoggerFactory.getLogger(StreamingImageCapturer.class);

    public StreamingImageCapturer(List<Komposition> fetchKompositions, Komposition buildKomposition, ImageStore capturer) {
        this.fetchKompositions = fetchKompositions;
        this.buildKomposition = buildKomposition;
        this.capturer = capturer;
    }

    public void startUpThreads() {
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
            List<Segment> extractedInSegments = createUniqueSegments(fetchKomposition.segments, buildKomposition.segments);
            List<KompositionPlanner> planners = createPlanners(extractedInSegments, buildKomposition.bpm, buildKomposition.framerate);

            for (KompositionPlanner planner : planners) {
                log.info("Fetching plan:{}", planner.plans.get(0).originalSegment.id());
                ImageCapturer imageCapturer = new ImageCapturer(planner, capturer, fetchKomposition.storageLocation.fileName.getFile(), fetchKomposition.bpm);
                imageCapturers.add(imageCapturer);
                new Thread(imageCapturer).start();
            }
        }
    }

    List<KompositionPlanner> createPlanners(List<Segment> inSegments, float bpm, long framerate) {
        List<KompositionPlanner> plans = new ArrayList<>();
        for (Segment inSegment : inSegments) {
            plans.add(new KompositionPlanner(Collections.singletonList(inSegment), bpm, framerate));
        }
        return plans;

    }
}

//Responsible for extracting the images. Will wait for more work to do
class ImageCapturer implements Runnable {

    private final KompositionPlanner planner;
    private final ImageStore imageStoreCapturer;
    private final String videoFile;
    final float bpm;

    public ImageCapturer(KompositionPlanner planner, ImageStore imageStoreCapturer, String videoFile, float bpm) {
        this.planner = planner;
        this.imageStoreCapturer = imageStoreCapturer;
        this.videoFile = videoFile;
        this.bpm = bpm;
    }

    @Override
    public void run() {
        new WaitingVideoThumbnailsCollector(imageStoreCapturer).capture(videoFile, planner, bpm);
    }
}