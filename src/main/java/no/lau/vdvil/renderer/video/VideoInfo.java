package no.lau.vdvil.renderer.video;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.domain.PathRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path;

public class VideoInfo {
	
	//private static final String filename = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
    private VideoInfo() {}
    private static Logger logger = LoggerFactory.getLogger(VideoInfo.class);

	public static IContainer getVideoProperties(PathRef pathRef) {

		// first we create a Xuggler container object
		IContainer container = IContainer.make();
		
		// we attempt to open up the container
		int result = container.open(pathRef.toString(), IContainer.Type.READ, null);
		
		// check if the operation was successful
		if (result<0)
			throw new RuntimeException("Failed to open media file " + pathRef);
        return container;
	}

    public static void printProperties(IContainer container) {
        // query how many streams the call to open found
        int numStreams = container.getNumStreams();
        // query for the total duration
        long duration = container.getDuration();

        // query for the file size
        long fileSize = container.getFileSize();

        // query for the bit rate
        long bitRate = container.getBitRate();

        logger.info("Number of streams: " + numStreams);
        logger.info("Duration (ms): " + duration);
        logger.info("File Size (bytes): " + fileSize);
        logger.info("Bit Rate: " + bitRate);

        // iterate through the streams to print their meta data
        for (int i=0; i<numStreams; i++) {

            // find the stream object
            IStream stream = container.getStream(i);

            // get the pre-configured decoder that can decode this stream;
            IStreamCoder coder = stream.getStreamCoder();

            logger.info("*** Start of Stream Info ***");

            System.out.printf("stream %d: ", i);
            System.out.printf("type: %s; ", coder.getCodecType());
            System.out.printf("codec: %s; ", coder.getCodecID());
            System.out.printf("duration: %s; ", stream.getDuration());
            System.out.printf("start time: %s; ", container.getStartTime());
            System.out.printf("timebase: %d/%d; ", stream.getTimeBase().getNumerator(), stream.getTimeBase().getDenominator());
            System.out.printf("coder tb: %d/%d; ", coder.getTimeBase().getNumerator(), coder.getTimeBase().getDenominator());
            logger.info("");

            if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
                System.out.printf("sample rate: %d; ", coder.getSampleRate());
                System.out.printf("channels: %d; ", coder.getChannels());
                System.out.printf("format: %s", coder.getSampleFormat());
            }
            else if (coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
                System.out.printf("width: %d; ", coder.getWidth());
                System.out.printf("height: %d; ", coder.getHeight());
                System.out.printf("format: %s; ", coder.getPixelType());
                System.out.printf("frame-rate: %5.2f; ", coder.getFrameRate().getDouble());
                logger.info("coder.getSampleRate() = " + coder.getSampleRate());
                logger.info("coder.getNumDroppedFrames() = " + coder.getNumDroppedFrames());
                logger.info("coder.getTimeBase() = " + coder.getTimeBase());
                logger.info("coder.getNumPicturesInGroupOfPictures() = " + coder.getNumPicturesInGroupOfPictures());

                logger.info("coder.getPropertyAsString(frame_number) = " + coder.getPropertyAsString("frame_number"));
                logger.info("coder.getPropertyAsString(frame_size) = " + coder.getPropertyAsString("frame_size"));
            }

            logger.info("");
            logger.info("*** End of Stream Info ***");
        }
    }
}
