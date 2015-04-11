package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaViewer;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IAudioSamples;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.deprecated.phun.ImageCreator;
import no.lau.vdvil.renderer.video.deprecated.phun.ImageCreatorI;
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

    public static void createVideo(Komposition komposition) {
        log.info("Init");
        // the number of balls to bounce around


        ImageStore imageStore = new ImageStore(komposition);

        final int ballCount = 0;

        // total duration of the media

        final long duration = DEFAULT_TIME_UNIT.convert(60, SECONDS);

        // video parameters

        final int videoStreamIndex = 0;
        final int videoStreamId = 0;
        final long frameRate = DEFAULT_TIME_UNIT.convert(15, MILLISECONDS);
        final int width = 320;
        final int height = 200;

        // audio parameters

        final int audioStreamIndex = 1;
        final int audioStreamId = 0;
        final int channelCount = 1;
        final int sampleRate = 44100; // Hz
        final int sampleCount = 1000;



        // create a media writer and specify the output file

        final IMediaWriter writer = ToolFactory.makeWriter(komposition.storageLocation.fileName.getFile());

        // add audio and video streams
        writer.addVideoStream(videoStreamIndex, videoStreamId, width, height);
        writer.addAudioStream(audioStreamIndex, audioStreamId, channelCount, sampleRate);

        // create some balls to show on the screen
        ImageCreatorI balls = new ImageCreator(ballCount, width, height, sampleCount);


        try {
            drawSomeBalls(duration, videoStreamIndex, frameRate, audioStreamIndex, sampleRate, sampleCount, writer, balls, imageStore);
        }catch(Exception e) {
            log.error("Sometin happened", e);
        }
        finally {
            // manually close the writer
            writer.close();
        }
    }

    private static void drawSomeBalls(long duration, int videoStreamIndex, long frameRate, int audioStreamIndex, int sampleRate, int sampleCount, IMediaWriter writer, ImageCreatorI balls, ImageStore imageStore) throws IOException {
        // loop through clock time, which starts at zero and increases based
        // on the total number of samples created thus far
// the clock time of the next frame

        long nextFrameTime = 0;

        // the total number of audio samples

        long totalSampleCount = 0;

        for (long clock = 0; clock < duration; clock = IAudioSamples.samplesToDefaultPts(totalSampleCount, sampleRate)) {
            // while the clock time exceeds the time of the next video frame,
            // get and encode the next video frame

            while (clock >= nextFrameTime) {

                for (BufferedImage frame : imageStore.getImageAt(clock)) {
                    writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                }
                //writer.encodeVideo(videoStreamIndex, frame, nextFrameTime, DEFAULT_TIME_UNIT);
                nextFrameTime += frameRate;
            }

            // compute and encode the audio for the balls

            short[] samples = balls.getAudioFrame(sampleRate);
            writer.encodeAudio(audioStreamIndex, samples, clock, DEFAULT_TIME_UNIT);
            totalSampleCount += sampleCount;
        }
    }
}
