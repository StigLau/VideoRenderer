package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.IContainer;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CountNumberOfEligableImagesBetweenTimestampsCollector implements ImageCollector {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private final String mediaFile;
    ImageCounter counter;

    public CountNumberOfEligableImagesBetweenTimestampsCollector(long startMs, long endMs, String mediaFile) {
        this.mediaFile = mediaFile;
        this.counter = new ImageCounter(startMs, endMs);
    }

    public void runSingle() {
        long start = System.currentTimeMillis();
        IContainer container = IContainer.make();
        int result = container.open(mediaFile, IContainer.Type.READ, null);
        if (result < 0)
            throw new RuntimeException("Failed to open media file " + mediaFile);

        IMediaReader mediaReader = ToolFactory.makeReader(container);
        try {
            mediaReader.addListener(counter);
            while (mediaReader.readPacket() == null) ;
        }  finally {
            mediaReader.close();
            container.close();
        }
        logger.debug("Duration: {}", (System.currentTimeMillis() - start) / 1000);
    }

    public long imagesCollected() {
        return counter.imagesCollected;
    }

    private class ImageCounter extends MediaListenerAdapter {
        long imagesCollected;
        private final long start;
        final long end;

        private ImageCounter(long start, long end) {
            this.start = start;
            this.end = end;
            this.imagesCollected = 0;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            if (timestamp >= start) {
                logger.trace("Found image at " + timestamp);
                imagesCollected++;
            }
            if (timestamp > end)
                throw new VideoExtractionFinished("End of compilation");
        }
    }
}
