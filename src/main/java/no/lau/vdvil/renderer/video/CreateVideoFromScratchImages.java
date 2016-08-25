package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.collector.FrameRepresentation;
import no.lau.vdvil.domain.TransitionSegment;
import no.lau.vdvil.plan.AudioPlan;
import no.lau.vdvil.plan.Plan;
import no.lau.vdvil.plan.SuperPlan;
import no.lau.vdvil.renderer.video.builder.GenericBuilder;
import no.lau.vdvil.renderer.video.config.VideoConfig;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.builder.ImageCrossFader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.util.Collections;
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
     */
    public static void createVideo(Plan buildPlan, ImageStore<BufferedImage> imageStore, VideoConfig config) {
        try {
            Thread.sleep(5000);//Sleep to avoid hanging bug when audio is available in cache!
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        createVideo(buildPlan, imageStore, config, ToolFactory.makeWriter(buildPlan.ioFile()), true);
    }

    /**
     * Detailed instatiation
     */
    public static void createVideo(Plan buildPlan, ImageStore<BufferedImage> imageStore, VideoConfig config, IMediaWriter writer, boolean turboCharged) {
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
        if(buildPlan instanceof AudioPlan) {
            audioAdapter = new AudioAdapter(((AudioPlan)buildPlan).audioLocation().getFile(), writer);
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
                if(buildPlan instanceof AudioPlan) {
                    audioAdapter.writeNextPacket(clock, buildPlan);
                }
                totalSampleCount += sampleCount;
            }

            long now = System.currentTimeMillis();
            log.info("before - now {} - {}", startTime, now);
            long buildTime = (now - startTime) / 1000;
            log.info("Finished writing video {} seconds - {}", buildTime, buildPlan.ioFile());
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
    //Todo take this in as a param somehow
    final ImageCrossFader fader ;
    final GenericBuilder builder;


    public VideoAdapter(VideoConfig config, IMediaWriter writer, ImageStore<BufferedImage> imageStore) {
        this.writer = writer;
        this.imageStore = imageStore;
        this.fader = new ImageCrossFader(imageStore);
        this.builder = new GenericBuilder(imageStore);
        this.width = config.width;
        this.height = config.height;
        this.frameRate = config.framerate();

        // add audio and video streams
        writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

    }

    public void writeNextPacket(long clock, Plan buildPlan) {
        logger.debug("Clock ping: " + clock);
        while (clock >= nextFrameTime) {
            boolean nextFrameTimeUpdated = false;
            logger.trace("Time to write packets at {}", clock);
            List<FrameRepresentation> frameRepresentations = buildPlan.whatToDoAt(nextFrameTime);
            if(frameRepresentations.size() > 0) {
                List<TransitionSegment> transitionSegments = buildPlan instanceof SuperPlan ?
                        ImageCrossFader.extractTransitionSegment(nextFrameTime, (SuperPlan) buildPlan):
                        Collections.EMPTY_LIST;

                List<TransitionSegment> applicableTransitionSegments = fader.containsMetaSegment(frameRepresentations, transitionSegments);
                if (applicableTransitionSegments.size() >= 1) {
                    for (TransitionSegment segment : applicableTransitionSegments) {
                        logger.trace("Performing crossover");
                        BufferedImage theImage = fader.perform(segment, frameRepresentations);
                        writer.encodeVideo(videoStreamIndex, theImage, nextFrameTime, DEFAULT_TIME_UNIT);
                        for (FrameRepresentation frameRepresentation : frameRepresentations) {
                            frameRepresentation.use();
                        }
                    }
                } else if (buildPlan instanceof SuperPlan || frameRepresentations.size() > 0) {
                    logger.trace("Normal building video");
                    BufferedImage theImage = builder.build(nextFrameTime, frameRepresentations);
                    writer.encodeVideo(videoStreamIndex, theImage, nextFrameTime, DEFAULT_TIME_UNIT);
                    //TODO Other implementation
                    nextFrameTime += frameRate / frameRepresentations.size();
                    nextFrameTimeUpdated = true;
                    for (FrameRepresentation frameRepresentation : frameRepresentations) {
                        frameRepresentation.use();
                    }
                }
            }
            if(!nextFrameTimeUpdated) {
                nextFrameTime += frameRate;
            }
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