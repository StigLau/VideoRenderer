package no.lau.vdvil.renderer.video.testout;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;
import org.junit.Test;

/**
 * http://stackoverflow.com/questions/19812480/java-xuggler-combine-an-mp3-audio-file-and-a-mp4-movie
 * Created by stiglau on 03/04/15.
 */
public class CombineAudioAndVideoTest {
    @Test
    public void woopTest() {
        //String inputVideoFilePath = "/Users/stiglau/Downloads/NORWAY-A_Time-Lapse_Adventure.mp4"; Does not works
        String inputVideoFilePath = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String inputAudioFilePath = "/Users/stiglau/Downloads/5384094_The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";
        String outputVideoFilePath = "/tmp/some-timelapse.flv";

        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();

        // check files are readable
        if (containerVideo.open(inputVideoFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputVideoFilePath);
        if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputAudioFilePath);

        // read video file and create stream
        IStreamCoder coderVideo = containerVideo.getStream(0).getStreamCoder();
        if (coderVideo.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");
        IPacket packetvideo = IPacket.make();
        int width = coderVideo.getWidth();
        int height = coderVideo.getHeight();

        // read audio file and create stream
        IStreamCoder coderAudio = containerAudio.getStream(0).getStreamCoder();
        if (coderAudio.open(null, null) < 0)
            throw new RuntimeException("Cant open audio coder");
        IPacket packetaudio = IPacket.make();

        IMediaWriter mWriter = ToolFactory.makeWriter(outputVideoFilePath);
        try {
            mWriter.addAudioStream(1, 0, coderAudio.getChannels(), coderAudio.getSampleRate());
            mWriter.addVideoStream(0, 0, width, height);

            while (containerVideo.readNextPacket(packetvideo) >= 0) {

                containerAudio.readNextPacket(packetaudio);

                // video packet
                IVideoPicture picture = IVideoPicture.make(coderVideo.getPixelType(), width, height);
                coderVideo.decodeVideo(picture, packetvideo, 0);
                if (picture.isComplete())
                    mWriter.encodeVideo(0, picture);

                // audio packet
                IAudioSamples samples = IAudioSamples.make(512, coderAudio.getChannels(), IAudioSamples.Format.FMT_S32);
                coderAudio.decodeAudio(samples, packetaudio, 0);
                if (samples.isComplete())
                    mWriter.encodeAudio(1, samples);
            }
        }finally {
            coderAudio.close();
            coderVideo.close();
            containerAudio.close();
            containerVideo.close();
            mWriter.close();
        }
    }
}
