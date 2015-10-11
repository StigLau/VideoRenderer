package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.stigs.TimeStampFixedImageSampleSegment;
import no.lau.vdvil.renderer.video.store.ImageRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Queue;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

/**
 * @author Stig@Lau.no
 */

public class CreateVideoFromScratchImages {
    // the log

    private static final Logger log = LoggerFactory.getLogger(CreateVideoFromScratchImages.class);

    final static int sampleCount = 1000;

    public static void createVideo(TimeStampFixedImageSampleSegment segment,  String writeToUrl, ImageStore imageStore, Config config) {
        log.info("Init");

        final IMediaWriter writer = ToolFactory.makeWriter(writeToUrl);
        int numberOfPicsToExpect = ((Map<String, Queue>)((PipeDream)imageStore).segmentImageList).get(segment.id()).size();

        VideoAdapter videoAdapter = new VideoAdapter(config, writer, imageStore, numberOfPicsToExpect);

        try {
            // the total number of audio samples
            long totalSampleCount = 0;

            //TODO Use
            log.info("Continues until collector throws a finished exception");
            // loop through clock time, which starts at zero and increases based
            // on the total number of samples created thus far the clock time of the next frame
            for (long clock = 0; clock < segment.duration(); clock = IAudioSamples.samplesToDefaultPts(totalSampleCount, AudioAdapter.sampleRate)) {
                // while the clock time exceeds the time of the next video frame,
                // get and encode the next video frame
                videoAdapter.writeNextPacket(clock, segment);
                totalSampleCount += sampleCount;
            }
            log.info("Finished writing video");
        } catch (VideoExtractionFinished vidEx) {
            log.debug("Finished processing, {}", vidEx.getMessage());
        }
        catch(Exception e) {
            log.error("Sometin happened :´(", e);
        }
        finally {
            log.debug("Closing writer");
            writer.close();
        }
    }
}

class VideoAdapter {

    Logger logger = LoggerFactory.getLogger(VideoAdapter.class);
    final int videoStreamIndex = 0;
    final int videoStreamId = 0;
    final long frameRate;
    final int width;
    final int height;
    private final IMediaWriter writer;

    long nextFrameTime = 0;

    int lopenr = 0;

    final ImageStore<BufferedImage> imageStore;
    private int numberOfPicsToExpect;


    public VideoAdapter(Config config, IMediaWriter writer, ImageStore imageStore, int numberOfPicsToExpect) {
        this.writer = writer;
        this.imageStore = imageStore;
        this.numberOfPicsToExpect = numberOfPicsToExpect;
        this.width = config.width;
        this.height = config.height;
        this.frameRate = config.framerate;

        // add audio and video streams
        writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

    }
    BufferedImage previous = null;

    public void writeNextPacket(long clock, Segment segment) {
        while (clock >= nextFrameTime) {
            ImageRepresentation imageRep = imageStore.getNextImageRepresentation(segment.id());

            if (imageRep != null || previous != null) {
                if(numberOfPicsToExpect <= 0) {
                    logger.info("Got to end of pics!");
                    throw new VideoExtractionFinished("fin");
                }

                String imgid;
                BufferedImage theImage;
                if(imageRep != null) {
                    imgid= imageRep.imageId;
                    theImage = (BufferedImage) imageRep.image;
                } else {
                    imgid = "UNKNOWN IMAGE DUE TO NULL";
                    theImage = previous;
                }

                logger.debug("Pushing image Clock:{} {}@{}-{}/{} from from pipedream to video ", nextFrameTime, segment.id(), imgid, ++lopenr);
                //frameRepresentation.use();
                //In some circumstances, one must reuse the previous image

                writer.encodeVideo(videoStreamIndex, theImage, nextFrameTime, DEFAULT_TIME_UNIT);
                previous = theImage;

                numberOfPicsToExpect--;

            } else {
                logger.error("OMG OMG!!! Imagerep was null after waiting 10 seconds!! - {} - {}/{} is this related to division rest error?" + segment.id(), lopenr);
            }

            nextFrameTime += frameRate;
        }
    }
}

class AudioAdapter {
    // audio parameters
    public final int audioStreamIndex = 1;
    public final int audioStreamId = 0;
    public static final int sampleRate = 44100; // Hz


    public final IContainer containerAudio;
    public final IPacket packetaudio;
    public final IStreamCoder coderAudio;
    private final IMediaWriter writer;

    public AudioAdapter(String inputAudioFilePath, IMediaWriter writer) {
        this.writer = writer;
        //Audio
        containerAudio = IContainer.make();
        packetaudio = IPacket.make();
        if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0) {
            throw new IllegalArgumentException("Cant find " + inputAudioFilePath);
        }
        coderAudio = containerAudio.getStream(0).getStreamCoder();
        if (coderAudio.open(null, null) < 0) {
            throw new RuntimeException("Cant open audio coder");
        }
        writer.addAudioStream(audioStreamIndex, audioStreamId, coderAudio.getChannels(), coderAudio.getSampleRate());
    }

    public void writeNextPacket(long clock, Plan buildPlan) {
        //Clock not required by current implemenetation
        // Audio
        containerAudio.readNextPacket(packetaudio);
        // compute and encode the audio for the balls
        IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
        coderAudio.decodeAudio(samples, packetaudio, 0);
        if (samples.isComplete()){
            writer.encodeAudio(1, samples);
        }
    }
}