package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.List;
import static no.lau.vdvil.domain.utils.KompositionUtils.isFinishedProcessing;
import static no.lau.vdvil.domain.utils.KompositionUtils.isInterestedInThisPicture;

public class WaitingVideoThumbnailsCollector {

	// The video stream index, used to ensure we display frames from one and
	// only one video stream from the media container.
	private static int mVideoStreamIndex = -1;

	// Time of last frame write
	private double mLastPtsWrite = Global.NO_PTS;
    private Logger logger = LoggerFactory.getLogger(WaitingVideoThumbnailsCollector.class);
    private final ImageStore imageStore;

    public WaitingVideoThumbnailsCollector(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    public void capture(String inputFilename, List<Segment> segment, float bpm){
        long start = System.currentTimeMillis();


        IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(segment, imageStore, bpm));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null)  ;
        } catch(VideoExtractionFinished finished) {
            logger.info("Work completed {}", finished.getMessage());
        } finally {
            mediaReader.close();
        }
        logger.debug("Duration: " + (System.currentTimeMillis() - start) / 1000);
    }

	private class ImageSnapListener extends MediaListenerAdapter {
        final List<Segment> segments;
        final ImageStore<BufferedImage> imageStore;
        final float bpm;

        private ImageSnapListener(List<Segment> segments, ImageStore imageStore, float bpm) {
            this.segments = segments;
            this.imageStore = imageStore;
            this.bpm = bpm;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            if(isFinishedProcessing(segments, timestamp, bpm)) {
                throw new VideoExtractionFinished("End of compilation");
            }

            for (Segment segment : isInterestedInThisPicture(segments, bpm, timestamp)) {
                try {
                    if (segment instanceof ImageSampleInstruction) {
                        ImageSampleInstruction sampleInstruction = (ImageSampleInstruction) segment;
                        BufferedImage image = fetchImage(event, sampleInstruction);
                        imageStore.store(image, timestamp, sampleInstruction.id());
                    } else if(segment instanceof TimeStampFixedImageSampleSegment) {
                        BufferedImage image = event.getImage();
                        if(image != null) {
                            //Wait until imageStore is ready to accept images
                            while(!imageStore.readyForNewImage(segment.id())) {
                                logger.trace("Buffer full - Waiting to write image to store");
                                Thread.sleep(100);
                            }
                            imageStore.store(event.getImage(), timestamp, segment.id());
                        }
                    }
                }catch (Exception e) {
                    logger.error("Nothing exciting happened - could not fetch file: ", e);
                }
            }
        }

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
            double MICRO_SECONDS_BETWEEN_FRAMES = bpm * clockRatio / (segment.framesPerBeat * 60);

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
	}
}