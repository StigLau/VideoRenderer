package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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

    public List<KompositionPlanner> createPlanners() {
        for (Komposition fetchKomposition : fetchKompositions) {
            performIdUniquenessCheck(fetchKomposition.segments);
        }//TODO Change back to fetchkompositions segments
        return createUniqueSegments(buildKomposition.segments, buildKomposition.segments).stream()
                .map(segment -> new KompositionPlanner(findMatchingSegment(segment, fetchKompositions), segment, buildKomposition.bpm, buildKomposition.framerate,
                        findMatchingKomposition(segment, fetchKompositions)))
                .collect(Collectors.toList());
    }

    private Komposition findMatchingKomposition(Segment extractedInSegment, List<Komposition> fetchKompositions) {
        for (Komposition fetchKomposition : fetchKompositions) {
            for (Segment fetchSegment : fetchKomposition.segments) {
                if(extractedInSegment.id().contains(fetchSegment.id()))
                    return fetchKomposition;
            }
        }
        throw new RuntimeException("Should not happen");
    }

    private Segment findMatchingSegment(Segment segment, List<Komposition> fetchKompositions) {
        for (Komposition fetchKomposition : fetchKompositions) {
            for (Segment fetchSegment : fetchKomposition.segments) {
                if(segment.id().contains(fetchSegment.id()))
                    return fetchSegment;
            }
        }
        throw new RuntimeException("Should not happen");
    }

    //TODO Factor out composition and thread startup
    public void startUpThreads(List<KompositionPlanner> planners) {
        for (KompositionPlanner planner : planners) {
            log.info("Fetching plan:{}", planner.plans.get(0).originalSegment.id());
            ImageCapturer imageCapturer = new ImageCapturer(planner, capturer, planner.fetchKomposition.storageLocation.fileName.getFile(), planner.fetchKomposition.bpm);
            imageCapturers.add(imageCapturer);
            new Thread(imageCapturer).start();
        }
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