package no.lau.vdvil.collector;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.VideoExtractionFinished;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.List;

public class WaitingVideoThumbnailsCollector implements ImageCollector {

    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;

    // Time of last frame write
    private double mLastPtsWrite = Global.NO_PTS;
    private Logger logger = LoggerFactory.getLogger(WaitingVideoThumbnailsCollector.class);
    private final ImageStore<BufferedImage> imageStore;
    private final List<Plan> collectPlans;

    public WaitingVideoThumbnailsCollector(ImageStore<BufferedImage> imageStore, List<Plan> collectPlans) {
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

        private ImageSnapListener(Plan collectPlan, ImageStore imageStore) {
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
    //TODO What does this mean!?
/*
        public BufferedImage fetchImage(IVideoPictureEvent event, ImageSampleInstruction segment) throws Exception {

			if (event.getStreamIndex() != mVideoStreamIndex) {
				// if the selected video stream id is not yet set, go ahead an
				// select this lucky video stream
				if (mVideoStreamIndex == -1)
					mVideoStreamIndex = event.getStreamIndex();
				// no need to show frames from this video stream
				else
                    return null;
			}

            int clockRatio = 250000;


			// if uninitialized, back date mLastPtsWrite so we get the very first frame
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

			// if it's time to write the next frame
			if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {

				// indicate file written
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
                logger.info(
                        "at elapsed time of {} seconds",
                        seconds);
                // update last write time
				mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
                return event.getImage();
			}
            return null;
        }
	}*/
}