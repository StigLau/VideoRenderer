package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IContainer;
import no.lau.vdvil.plan.ImageCollectable;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.List;

public class WaitingVideoThumbnailsCollector implements ImageCollector{

    private Logger logger = LoggerFactory.getLogger(getClass());
    private ImageCollectable collectPlan;
    private final ImageStore<BufferedImage> imageStore;

    private final boolean skipAhead;

    public WaitingVideoThumbnailsCollector(ImageCollectable collectPlan, ImageStore<BufferedImage> imageStore) {
        this(collectPlan, imageStore, true);
    }

    public WaitingVideoThumbnailsCollector(ImageCollectable collectPlan, ImageStore<BufferedImage> imageStore, boolean skipAhead) {
        this.collectPlan = collectPlan;
        this.imageStore = imageStore;
        this.skipAhead = skipAhead;
    }

    public void run() {
        logger.info("Starting capture {}", collectPlan.id());
        long start = System.currentTimeMillis();


        IContainer container = IContainer.make();
        int result = container.open(collectPlan.localStorage().toString(), IContainer.Type.READ, null);
        if (result<0)
            throw new RuntimeException("Failed to open media file");

        IMediaReader mediaReader = ToolFactory.makeReader(container);
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(collectPlan, imageStore));

            if(skipAhead && collectPlan instanceof SuperPlan) {
                long backoffMs = 65000;
                long startMs = ((TimeStampFixedImageSampleSegment) ((SuperPlan) collectPlan).getFramePlans().get(0).wrapper().segment).timestampStart - backoffMs;
                startMs = startMs > 0 ? startMs : 0;
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

            if (collectPlan.isFinishedProcessing(timestamp)) {
                logger.trace("{} Finished collecting images", timestamp);
                throw new VideoExtractionFinished("End of compilation");
            }
            BufferedImage newestImage = event.getImage();

            List<FrameRepresentation> frames = collectPlan.whatToDoAt(timestamp);
            if(frames.isEmpty()) {
                logger.debug("No frames found @{}. Throwing away img@{}", timestamp, Integer.toHexString(newestImage.hashCode()));
                /* To look at the thrown away images
                try {
                    ImageIO.write(newestImage, "png", new File("/tmp/komposttest.mp4snaps/" + timestamp + "_thrownaway" + ".png"));
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            if(frames.size() > 1) {
                logger.warn("Throwing away too many frames received - Frames:{} at @{}", frames.size()-1, timestamp);
                for (int i = 0; i < frames.size(); i++) {
                    FrameRepresentation frame = frames.get(i);
                    if(i > 0) {
                        logger.warn("Throwing frame {} {}", frame.frameNr, frame.timestamp);
                    }
                }
            }
            for (FrameRepresentation frameRepresentation : frames) {
                 logger.info("{} Collecting img@{} {}#{}", timestamp, Integer.toHexString(newestImage.hashCode()), frameRepresentation.getSegmentShortId(), frameRepresentation.frameNr);
                writeImage(newestImage, timestamp, frameRepresentation);
            }
            if(frames.isEmpty()) {
                logger.trace("@{} Not using any of these pics", timestamp);
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