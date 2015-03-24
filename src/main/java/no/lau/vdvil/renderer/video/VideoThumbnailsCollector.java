package no.lau.vdvil.renderer.video;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.stigs.Instruction;
import no.lau.vdvil.renderer.video.stigs.Composition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoThumbnailsCollector {

	// The video stream index, used to ensure we display frames from one and
	// only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
	private double mLastPtsWrite = Global.NO_PTS;
    private Logger logger = LoggerFactory.getLogger(VideoThumbnailsCollector.class);


    public void capture(String inputFilename, String outputFilePrefix, Composition composition){
        long start = System.currentTimeMillis();
        new File(outputFilePrefix).mkdirs();


        try {
            IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);

            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(composition, outputFilePrefix));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        }catch (Exception e) {
            logger.debug("Duration: " + (System.currentTimeMillis() - start) / 1000);
        }
    }

	private class ImageSnapListener extends MediaListenerAdapter {
        final Composition composition;
        private final String outputFilePrefix;

        private ImageSnapListener(Composition composition, String outputFilePrefix) {
            this.composition = composition;
            this.outputFilePrefix = outputFilePrefix;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            if(composition.isFinishedProcessing(event.getTimeStamp())) {
                throw new RuntimeException("End of compilation");
            }

            for (Instruction instruction : composition.isInterestedInThisPicture(event.getTimeStamp())) {
                logger.trace("Ping {}", event.getTimeStamp());
                try {
                    String imageUrl = fetchImage(event, instruction);
                    if(imageUrl != null) {
                        instruction.relevantFiles.add(imageUrl);
                    }
                }catch (Exception e) {
                    logger.error("Nothing exciting happened - could not fetch file: ", e);
                }
            }
        }

        public String fetchImage(IVideoPictureEvent event, Instruction instruction) throws Exception {

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
            double MICRO_SECONDS_BETWEEN_FRAMES = composition.bpm * clockRatio / (((ImageSampleInstruction)instruction).framesPerBeat * 60);

			// if uninitialized, back date mLastPtsWrite so we get the very first frame
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

			// if it's time to write the next frame
			if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {
                String outputFilename = outputFilePrefix + event.getTimeStamp() + ".png";
                ImageIO.write(event.getImage(), "png", new File(outputFilename));

				// indicate file written
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
                logger.info(
                        "at elapsed time of {} seconds wrote: {}",
                        seconds, outputFilename);


                // update last write time
				mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
                return outputFilename;
			}
            return null;
        }
	}
}
