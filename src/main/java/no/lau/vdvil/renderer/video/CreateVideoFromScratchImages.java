package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import static com.xuggle.xuggler.Global.DEFAULT_TIME_UNIT;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Stig@Lau.no
 *
 * @author trebor
 * @author aclarke
 */

public class CreateVideoFromScratchImages {
    // the log

    private static final Logger log = LoggerFactory.getLogger(CreateVideoFromScratchImages.class);

    final static int sampleCount = 1000;

    public static void createVideo(Komposition komposition, String inputAudioFilePath) {
        log.info("Init");
        // the number of balls to bounce around


        ImageStore imageStore = new ImageStore(komposition);

        // total duration of the media
        final long duration = DEFAULT_TIME_UNIT.convert(60, SECONDS);

        // video parameters
        final int videoStreamIndex = 0;
        final int videoStreamId = 0;
        final long frameRate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        final int width = 320;
        final int height = 200;

        // create a media writer and specify the output file
        final IMediaWriter writer = ToolFactory.makeWriter(komposition.storageLocation.fileName.getFile());

        // add audio and video streams
        writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);

        //AudioStream must be added after videostream!
        AudioAdapter audioAdapter = new AudioAdapter(inputAudioFilePath, writer);

        try {
            process(duration, videoStreamIndex, frameRate, writer, imageStore, audioAdapter);
            log.info("Finished writing video");
        }catch(Exception e) {
            log.error("Sometin happened :´(", e);
        }
        finally {
            log.debug("Closing writer");
            writer.close();
        }
    }

    private static void process(long duration, int videoStreamIndex, long frameRate, IMediaWriter writer, ImageStore imageStore, AudioAdapter audioStream) throws IOException {

        // loop through clock time, which starts at zero and increases based
        // on the total number of samples created thus far
// the clock time of the next frame

        long nextFrameTime = 0;

        // the total number of audio samples

        long totalSampleCount = 0;

        for (long clock = 0; clock < duration; clock = IAudioSamples.samplesToDefaultPts(totalSampleCount, audioStream.sampleRate)) {
            // while the clock time exceeds the time of the next video frame,
            // get and encode the next video frame
            while (clock >= nextFrameTime) {
                for (BufferedImage frame : imageStore.getImageAt(clock)) {
                    writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                }
                nextFrameTime += frameRate;
            }
            audioStream.writeNextPacket();

            //writer.encodeAudio(audioStreamIndex, samples, clock, DEFAULT_TIME_UNIT);
            totalSampleCount += sampleCount;
        }
    }
}

class AudioAdapter {
    // audio parameters
    public final int audioStreamIndex = 1;
    public final int audioStreamId = 0;
    public final int sampleRate = 44100; // Hz


    public final IContainer containerAudio;
    public final IPacket packetaudio;
    public final IStreamCoder coderAudio;
    private final IMediaWriter writer;

    public AudioAdapter(String inputAudioFilePath, IMediaWriter writer) {
        this.writer = writer;
        //Audio
        containerAudio = IContainer.make();
        packetaudio = IPacket.make();
        if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputAudioFilePath);

        coderAudio = containerAudio.getStream(0).getStreamCoder();
        if (coderAudio.open(null, null) < 0)
            throw new RuntimeException("Cant open audio coder");

        writer.addAudioStream(audioStreamIndex, audioStreamId, coderAudio.getChannels(), coderAudio.getSampleRate());
    }

    public void writeNextPacket() {
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
