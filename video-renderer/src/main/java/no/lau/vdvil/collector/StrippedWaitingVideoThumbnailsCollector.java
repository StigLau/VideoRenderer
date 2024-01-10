package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.nio.file.Path;

public class StrippedWaitingVideoThumbnailsCollector implements ImageCollector{

    private static Logger logger = LoggerFactory.getLogger(StrippedWaitingVideoThumbnailsCollector.class);
    private TimeStampFixedImageSampleSegment segment;
    private Path originalMediaFile;
    private final ImageStore<BufferedImage> imageStore;
    //Testfeature for enabling scanning forward in large source-videos before starting collection of a snippet
    private final boolean skipFramesAhead;


    public StrippedWaitingVideoThumbnailsCollector(TimeStampFixedImageSampleSegment segment, Path originalMediaFile, ImageStore<BufferedImage> imageStore) {
        this(segment, originalMediaFile,   imageStore, false);
    }

    public StrippedWaitingVideoThumbnailsCollector(TimeStampFixedImageSampleSegment segment, Path originalMediaFile, ImageStore<BufferedImage> imageStore, boolean skipFramesAhead) {
        this.segment = segment;
        this.originalMediaFile = originalMediaFile;
        this.imageStore = imageStore;
        this.skipFramesAhead = skipFramesAhead;
    }

    public void run() {
        logger.info("Starting capture {}", segment.id());
        long start = System.currentTimeMillis();


        IContainer container = IContainer.make();
        int result = container.open(originalMediaFile.toString(), IContainer.Type.READ, null);
        if (result<0)
            throw new RuntimeException("Failed to open media file");

        IMediaReader mediaReader = ToolFactory.makeReader(container);
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(segment, imageStore));

            if(skipFramesAhead) {
                logger.debug("Skipping ahead in {} by ", segment.id(), segment.timestampStart);
                seekToMs(container, segment.timestampStart);
            }
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

    public static void seekToMs(IContainer container, long seekTo) {
        for(int videoStreamId = 0; videoStreamId < container.getNumStreams(); videoStreamId++) {
            IStream stream = container.getStream(videoStreamId);
            IStreamCoder coder = stream.getStreamCoder();
            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                double timeBase = stream.getTimeBase().getDouble();
                logger.debug("Found video stream on id {}", videoStreamId);
                double skipToFrame = seekTo / timeBase / 1000000;
                container.seekKeyFrame(videoStreamId, Math.round(skipToFrame), IContainer.SEEK_FLAG_BACKWARDS);
            }
        }
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
            logger.trace("Collect Video sample {}", timestamp);
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