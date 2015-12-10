package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IContainer;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class WaitingVideoThumbnailsCollector implements ImageCollector{

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final ImageStore<BufferedImage> imageStore;
    private final List<Plan> collectPlans;
    Queue<BufferedImage> emptyFrameBuffer = new ArrayDeque<>();
    private final boolean skipAhead;

    public WaitingVideoThumbnailsCollector(List<Plan> collectPlans, ImageStore<BufferedImage> imageStore) {
        this(collectPlans, imageStore, false);
    }

    public WaitingVideoThumbnailsCollector(List<Plan> collectPlans, ImageStore<BufferedImage> imageStore, boolean skipAhead) {
        this.imageStore = imageStore;
        this.collectPlans = collectPlans;
        this.skipAhead = skipAhead;
    }

    public void run() {
        for (Plan collectPlan : collectPlans) {
            runSingle(collectPlan);
        }
        logger.info("Finished collecting");
    }

    void runSingle(Plan collectPlan) {
        logger.info("Starting capture {}", collectPlan.id());
        long start = System.currentTimeMillis();


        IContainer container = IContainer.make();
        int result = container.open(collectPlan.ioFile(), IContainer.Type.READ, null);
        if (result<0)
            throw new RuntimeException("Failed to open media file");

        IMediaReader mediaReader = ToolFactory.makeReader(container);
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(collectPlan, imageStore));

            if(skipAhead) {
                long startMs = ((TimeStampFixedImageSampleSegment) ((SuperPlan) collectPlan).getFramePlans().get(0).originalSegment).timestampStart;
                StrippedWaitingVideoThumbnailsCollector.seekToMs(container, startMs);
            }
            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        } catch (VideoExtractionFinished finished) {
            logger.info("Work completed for {} - {}", collectPlan.id(), finished.getMessage());
        } catch (OutOfMemoryError outOfMemoryError) {
            logger.error("Collector crapped itself: ", outOfMemoryError);
        } finally {
            mediaReader.close();
            container.close();
        }
        logger.debug("{} Duration: {}",collectPlan.id(), (System.currentTimeMillis() - start) / 1000);
    }

    private class ImageSnapListener extends MediaListenerAdapter {
        private Plan collectPlan;
        final ImageStore<BufferedImage> imageStore;

        private ImageSnapListener(Plan collectPlan, ImageStore<BufferedImage> imageStore) {
            this.collectPlan = collectPlan;
            this.imageStore = imageStore;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            BufferedImage newestImage = null;
            if (collectPlan.isFinishedProcessing(timestamp) && emptyFrameBuffer.isEmpty()) {
                throw new VideoExtractionFinished("End of compilation");
            } else if (!collectPlan.isFinishedProcessing(timestamp)) {
                newestImage = event.getImage();
                //Store last image to queue
                emptyFrameBuffer.add(newestImage);
            } else {
                logger.trace("No more images coming in. Emptying buffer. {}", timestamp);
            }

            //Get oldest image
            BufferedImage oldestImage = emptyFrameBuffer.poll();

            List<FrameRepresentation> frames = collectPlan.whatToDoAt(timestamp);
            for (FrameRepresentation frameRepresentation : frames) {
                if(frameRepresentation.isEmptyFrame() && newestImage != null) {
                    //Add duplicate if framerepresentation is empty
                    logger.debug("Storing image from {} because of empty frame", event.getTimeStamp());
                    emptyFrameBuffer.add(newestImage);
                } else if(frameRepresentation.isEmptyFrame()) {
                    logger.debug("Reusing the latest image at {}", timestamp);
                    ((ArrayDeque<BufferedImage>)emptyFrameBuffer).addFirst(oldestImage);
                }
                writeImage(oldestImage, timestamp, frameRepresentation);
            }
            if(frames.isEmpty()) {
                logger.trace("Not using any of these pics: {}", event.getTimeStamp());
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