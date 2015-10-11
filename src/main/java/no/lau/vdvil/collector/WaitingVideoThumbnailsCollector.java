package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.List;

public class WaitingVideoThumbnailsCollector implements ImageCollector{

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ImageStore<BufferedImage> imageStore;
    private final List<Plan> collectPlans;

    public WaitingVideoThumbnailsCollector(List<Plan> collectPlans, ImageStore<BufferedImage> imageStore) {
        this.imageStore = imageStore;
        this.collectPlans = collectPlans;
    }

    public void run() {
        for (Plan collectPlan : collectPlans) {
            runSingle(collectPlan);
        }
    }

    void runSingle(Plan collectPlan) {
        logger.info("Starting capture {}", collectPlan.id());
        long start = System.currentTimeMillis();


        IMediaReader mediaReader = ToolFactory.makeReader(collectPlan.ioFile());
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(collectPlan, imageStore));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        } catch (VideoExtractionFinished finished) {
            logger.info("Work completed for {} - {}", collectPlan.id(), finished.getMessage());
        } finally {
            mediaReader.close();
        }
        logger.debug("{} Duration: {}",collectPlan.id(), (System.currentTimeMillis() - start) / 1000);
    }

    private class ImageSnapListener extends MediaListenerAdapter {
        private Plan collectPlan;
        final ImageStore<BufferedImage> imageStore;
        BufferedImage previous = null;

        private ImageSnapListener(Plan collectPlan, ImageStore<BufferedImage> imageStore) {
            this.collectPlan = collectPlan;
            this.imageStore = imageStore;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            //TODO How to halt video processing
            if (collectPlan.isFinishedProcessing(timestamp)) {
                throw new VideoExtractionFinished("End of compilation");
            }
            for (FrameRepresentation frameRepresentation : collectPlan.whatToDoAt(timestamp)) {
                BufferedImage image = (event.getImage() != null)?
                        event.getImage() :
                        previous;
                writeImage(image, timestamp, frameRepresentation);
            }
        }

        private void writeImage(BufferedImage image, long timestamp, FrameRepresentation frameRepresentation) {
            try {
                imageStore.store(image, timestamp, frameRepresentation);
                frameRepresentation.use();
                logger.trace("Storing image {}@{} {}/{}", frameRepresentation.referenceId(), timestamp);
            } catch (Exception e) {
                logger.error("Nothing exciting happened - could not fetch file: ", e);
            }
        }
    }
}