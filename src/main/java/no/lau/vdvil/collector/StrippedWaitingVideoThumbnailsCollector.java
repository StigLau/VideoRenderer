package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.net.URL;

public class StrippedWaitingVideoThumbnailsCollector implements ImageCollector{

    private Logger logger = LoggerFactory.getLogger(StrippedWaitingVideoThumbnailsCollector.class);
    private TimeStampFixedImageSampleSegment segment;
    private URL originalMediaFile;
    private final ImageStore<BufferedImage> imageStore;

    public StrippedWaitingVideoThumbnailsCollector(TimeStampFixedImageSampleSegment segment, URL originalMediaFile, ImageStore<BufferedImage> imageStore) {
        this.segment = segment;
        this.originalMediaFile = originalMediaFile;
        this.imageStore = imageStore;
    }

    public void run() {
        logger.info("Starting capture {}", segment.id());
        long start = System.currentTimeMillis();


        IMediaReader mediaReader = ToolFactory.makeReader(originalMediaFile.getFile());
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(segment, imageStore));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        } catch (VideoExtractionFinished finished) {
            logger.info("Work completed for {} - {}", segment.id(), finished.getMessage());
        } finally {
            mediaReader.close();
        }
        logger.debug("{} Duration: {}",segment.id(), (System.currentTimeMillis() - start) / 1000);
    }

    private class ImageSnapListener extends MediaListenerAdapter {
        TimeStampFixedImageSampleSegment segment;
        final ImageStore<BufferedImage> imageStore;
        BufferedImage previous = null;
        int framelopeNr = 0;

        private ImageSnapListener(TimeStampFixedImageSampleSegment segment, ImageStore<BufferedImage> imageStore) {
            this.segment = segment;
            this.imageStore = imageStore;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            //TODO How to halt video processing
            if (timestamp > segment.timestampEnd) {
                throw new VideoExtractionFinished("End of compilation");
            }
            else if(segment.timestampStart < timestamp) {
                BufferedImage image = (event.getImage() != null) ?
                        event.getImage() :
                        previous;
                FrameRepresentation frameRepresentation = new FrameRepresentation(timestamp, segment.id(), segment);
                frameRepresentation.frameNr = framelopeNr++;
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