package no.lau.vdvil.renderer.video;

import java.awt.image.BufferedImage;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static no.lau.vdvil.domain.utils.KompositionUtils.isFinishedProcessing;
import static no.lau.vdvil.domain.utils.KompositionUtils.isInterestedInThisPicture;

public class VideoThumbnailsCollector {

	// The video stream index, used to ensure we display frames from one and
	// only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
	private double mLastPtsWrite = Global.NO_PTS;
    private Logger logger = LoggerFactory.getLogger(VideoThumbnailsCollector.class);
    private final ImageStore imageStore;

    public VideoThumbnailsCollector(ImageStore imageStore) {
        this.imageStore = imageStore;
    }

    public void capture(String inputFilename, Komposition komposition){
        long start = System.currentTimeMillis();


        IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);
        try {
            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(komposition, imageStore));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        } catch(VideoExtractionFinished finished) {
            logger.info("Work completed {}", finished.getMessage());
        } finally {
            mediaReader.close();
        }
        logger.debug("Duration: " + (System.currentTimeMillis() - start) / 1000);
    }

	private class ImageSnapListener extends MediaListenerAdapter {
        final Komposition komposition;
        ImageStore imageStore;

        private ImageSnapListener(Komposition komposition, ImageStore imageStore) {
            this.komposition = komposition;
            this.imageStore = imageStore;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            long timestamp = event.getTimeStamp();
            if(isFinishedProcessing(komposition, timestamp)) {
                throw new VideoExtractionFinished("End of compilation");
            }

            for (Segment segment : isInterestedInThisPicture(komposition, timestamp)) {
                logger.trace("Ping {}", timestamp);
                try {
                    if (segment instanceof ImageSampleInstruction) {
                        ImageSampleInstruction sampleInstruction = (ImageSampleInstruction) segment;
                        BufferedImage image = fetchImage(event, sampleInstruction);
                        imageStore.store(image, timestamp, sampleInstruction.id());
                    } else if(segment instanceof TimeStampFixedImageSampleSegment) {
                        BufferedImage image = event.getImage();
                        if(image != null) {
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
            double MICRO_SECONDS_BETWEEN_FRAMES = komposition.bpm * clockRatio / (segment.framesPerBeat * 60);

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