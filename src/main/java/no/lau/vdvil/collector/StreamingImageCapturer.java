package no.lau.vdvil.collector;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 * @author Stig@Lau.no 15/05/15.
 */
public class StreamingImageCapturer {

    final static Logger log = LoggerFactory.getLogger(StreamingImageCapturer.class);

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
    //TODO Align fetchPlans in a single thread!
    public static void startUpThreads(List<Plan> collectPlans, ImageStore pipeDream) {
            ImageCapturer imageCapturer = new ImageCapturer(collectPlans, pipeDream);
            new Thread(imageCapturer).start();
    }
}

//Responsible for extracting the images. Will wait for more work to do
class ImageCapturer implements Runnable {

    private final List<Plan> collectPlan;
    private final ImageStore pipeDream;

    public ImageCapturer(List<Plan> collectPlan, ImageStore pipeDream) {

        this.collectPlan = collectPlan;
        this.pipeDream = pipeDream;
    }

    @Override
    public void run() {
        new WaitingVideoThumbnailsCollector(pipeDream).capture(collectPlan);
    }
}