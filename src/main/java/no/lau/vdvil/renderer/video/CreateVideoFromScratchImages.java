package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.plan.AudioPlan;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.renderer.video.builder.GenericBuilder;
import no.lau.vdvil.renderer.video.builder.ImageBuilder;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.builder.ImageCrossFader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;

/**
 * @author Stig@Lau.no
 */

public class CreateVideoFromScratchImages {
    // the log

    private static final Logger log = LoggerFactory.getLogger(CreateVideoFromScratchImages.class);

    final static int sampleCount = 1000;

    /**
     * Standard instantioator
      * @param buildPlan the build plan
     * @param imageStore temp storage
     * @param config video config
     */
    public static void createVideo(Plan buildPlan, ImageStore<BufferedImage> imageStore, VideoConfig config) {
        createVideo(buildPlan, imageStore, config, true);
    }

    public static void createVideo(Plan buildPlan, ImageStore<BufferedImage> imageStore, VideoConfig config, boolean allowAudio) {
        Path parentDir = buildPlan.localStorage().getParent();
        try {
            Files.createDirectories(parentDir); //Create non-existant mature parent folder
            Thread.sleep(5000);//Sleep to avoid hanging bug when audio is available in cache!
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            log.error("Could not create directory {}", parentDir, e);
        }
        createVideo(buildPlan, imageStore, config, ToolFactory.makeWriter(buildPlan.localStorage().toString()), true, allowAudio);
    }

    /**
     * Detailed instatiation
      * @param buildPlan plan from which to build
     * @param imageStore the image store
     * @param config video config
     * @param writer video writer
     * @param turboCharged lolz, dunno
     * @param allowAudio if not using audio
     */
    public static void createVideo(Plan buildPlan, ImageStore<BufferedImage> imageStore, VideoConfig config, IMediaWriter writer, boolean turboCharged, boolean allowAudio) {
        log.info("Init");
        try {
            log.info("Just a short wait to make sure the writer starts as expected");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long startTime = System.currentTimeMillis();
        ToolFactory.setTurboCharged(turboCharged);

        VideoAdapter videoAdapter = new VideoAdapter(config, writer, imageStore);
        //AudioStream must be added after videostream!

        AudioAdapter audioAdapter = null;
        if(buildPlan instanceof AudioPlan && allowAudio) {
            audioAdapter = new AudioAdapter(((AudioPlan)buildPlan).audioLocation(), writer);
        }

        try {
            // the total number of audio samples
            long totalSampleCount = 0;

            // loop through clock time, which starts at zero and increases based
            // on the total number of samples created thus far the clock time of the next frame
            for (long clock = 0; !buildPlan.isFinishedProcessing(clock); clock = IAudioSamples.samplesToDefaultPts(totalSampleCount, AudioAdapter.sampleRate)) {
                // while the clock time exceeds the time of the next video frame,
                // get and encode the next video frame
                videoAdapter.writeNextPacket(clock, buildPlan);
                if(buildPlan instanceof AudioPlan && allowAudio) {
                    audioAdapter.writeNextPacket(clock, buildPlan);
                }
                totalSampleCount += sampleCount;
            }

            long now = System.currentTimeMillis();
            log.info("before - now {} - {}", startTime, now);
            long buildTime = (now - startTime) / 1000;
            log.info("Finished writing video {} seconds - {}", buildTime, buildPlan.localStorage());
        }catch(Exception e) {
            log.error("Sometin happened :´(", e);
        }
        finally {
            log.debug("Closing writer and cleaning up");
            writer.close();
            System.gc();
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

    final ImageStore<BufferedImage> imageStore;
    final List<ImageBuilder> builders;


    public VideoAdapter(VideoConfig config, IMediaWriter writer, ImageStore<BufferedImage> imageStore) {
        this.writer = writer;
        this.imageStore = imageStore;
        this.height = config.height;
        this.frameRate = config.framerate();
        this.width = config.width;
        builders = Arrays.asList(new ImageCrossFader(imageStore), new GenericBuilder(imageStore));

        // add audio and video streams
        writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

    }

    public void writeNextPacket(long clock, Plan buildPlan) {
        logger.debug("Clock ping: " + clock);
        while (clock >= nextFrameTime) {
            logger.trace("Time to write packets at {}", clock);
            List<FrameRepresentation> frameRepresentations = buildPlan.whatToDoAt(nextFrameTime);
            if(!frameRepresentations.isEmpty()) {
                boolean frameSuccess = false;
                for (ImageBuilder builder : builders) {
                    if (!frameSuccess) {
                        try {
                            builder.build(buildPlan, frameRepresentations, nextFrameTime,
                                    theImage -> writer.encodeVideo(videoStreamIndex, theImage, nextFrameTime, DEFAULT_TIME_UNIT));
                            logger.info("Building succeded {}", builder);
                            frameSuccess = true;
                        } catch (NullPointerException e) {
                            logger.error(e.getMessage(), e);
                        } catch (Exception e) {
                            //If building with builder fails
                            logger.trace(e.getMessage(), e);
                        }
                    }
                }
                if(!frameSuccess && frameRepresentations.size() <= 1) {
                    logger.warn("Building failed {} - {}", frameRepresentations.get(0).referenceId(), nextFrameTime);
                }
            }
            nextFrameTime += frameRate;
        }
    }
}

class AudioAdapter {
    // audio parameters
    public final int audioStreamIndex = 1;
    public final int audioStreamId = 0;
    public final static int sampleRate = 44100; // Hz


    public final IContainer containerAudio;
    public final IPacket packetaudio;
    public final IStreamCoder coderAudio;
    private final IMediaWriter writer;

    public AudioAdapter(Path inputAudioFilePath, IMediaWriter writer) {
        this.writer = writer;
        //Audio
        containerAudio = IContainer.make();
        packetaudio = IPacket.make();
        if (containerAudio.open(inputAudioFilePath.toString(), IContainer.Type.READ, null) < 0) {
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