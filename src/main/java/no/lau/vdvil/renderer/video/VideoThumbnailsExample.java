package no.lau.vdvil.renderer.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import no.lau.vdvil.renderer.video.stigs.Instruction;
import no.lau.vdvil.renderer.video.stigs.Composition;

public class VideoThumbnailsExample {
	
	public static final double SECONDS_BETWEEN_FRAMES = 1/10;

	private static final String inputFilename = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
	private static final String outputFilePrefix = "/tmp/snaps/";
	
	// The video stream index, used to ensure we display frames from one and
	// only one video stream from the media container.
	private static int mVideoStreamIndex = -1;
	
	// Time of last frame write
	private static long mLastPtsWrite = Global.NO_PTS;
	
	public static final long MICRO_SECONDS_BETWEEN_FRAMES = 
	    (long)(Global.DEFAULT_PTS_PER_SECOND * SECONDS_BETWEEN_FRAMES);

	public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new File(outputFilePrefix).mkdirs();

        Composition composition = new Composition(Collections.singletonList(new Instruction("Sometan", 0, 4, 120)));


        try {
            IMediaReader mediaReader = ToolFactory.makeReader(inputFilename);

            // stipulate that we want BufferedImages created in BGR 24bit color space
            mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);

            mediaReader.addListener(new ImageSnapListener(composition));

            // read out the contents of the media file and
            // dispatch events to the attached listener
            while (mediaReader.readPacket() == null) ;
        }catch (Exception e) {
            System.out.println("Duration: " + (System.currentTimeMillis() - start) / 1000);
        }
    }

	private static class ImageSnapListener extends MediaListenerAdapter {
        final Composition composition;

        private ImageSnapListener(Composition composition) {
            this.composition = composition;
        }

        public void onVideoPicture(IVideoPictureEvent event) {
            if(composition.isFinishedProcessing(event.getTimeStamp())) {
                throw new RuntimeException("End of compilation");
            }


            List<Instruction> result = composition.isInterestedInThisPicture(event.getTimeStamp());
            for (Instruction instruction : result) {
                System.out.println("Fetching image " + event.getTimeStamp());
                try {
                    instruction.relevantFiles.add(fetchImage(event));
                }catch (Exception e) {
                    System.out.println("Nothing exciting happened - could not fetch file" + e);
                }
            }
        }

        public String fetchImage(IVideoPictureEvent event) throws Exception {

			if (event.getStreamIndex() != mVideoStreamIndex) {
				// if the selected video stream id is not yet set, go ahead an
				// select this lucky video stream
				if (mVideoStreamIndex == -1)
					mVideoStreamIndex = event.getStreamIndex();
				// no need to show frames from this video stream
				else
                    return null;
			}

			// if uninitialized, back date mLastPtsWrite so we get the very first frame
			if (mLastPtsWrite == Global.NO_PTS)
				mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES;

			// if it's time to write the next frame
			if (event.getTimeStamp() - mLastPtsWrite >= MICRO_SECONDS_BETWEEN_FRAMES) {
								
				String outputFilename = dumpImageToFile(event.getImage());

				// indicate file written
				double seconds = ((double) event.getTimeStamp()) / Global.DEFAULT_PTS_PER_SECOND;
				System.out.printf(
						"at elapsed time of %6.3f seconds wrote: %s\n",
						seconds, outputFilename);

				// update last write time
				mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
                return outputFilename;
			}else {
                throw new Exception("couldn't fetch file");
            }
        }
		
		private String dumpImageToFile(BufferedImage image) {
			try {
				String outputFilename = outputFilePrefix + System.currentTimeMillis() + ".png";
				ImageIO.write(image, "png", new File(outputFilename));
				return outputFilename;
			} 
			catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

}
