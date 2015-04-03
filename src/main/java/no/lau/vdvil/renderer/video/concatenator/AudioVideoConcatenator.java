package no.lau.vdvil.renderer.video.concatenator;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.*;

/**
 * http://stackoverflow.com/questions/19812480/java-xuggler-combine-an-mp3-audio-file-and-a-mp4-movie
 * Created by stiglau on 03/04/15.
 */
public class AudioVideoConcatenator {

    public static void concatenateAudioAndVideo(String inputAudioFilePath, String inputVideoFilePath, String outputVideoFilePath) {
        IMediaWriter mWriter = ToolFactory.makeWriter(outputVideoFilePath);
        IContainer containerVideo = IContainer.make();
        IContainer containerAudio = IContainer.make();
        IPacket packetvideo = IPacket.make();
        IPacket packetaudio = IPacket.make();

        validate(inputAudioFilePath, inputVideoFilePath, containerVideo, containerAudio);

        // read video file and create stream
        IStreamCoder coderVideo = containerVideo.getStream(0).getStreamCoder();
        int width = coderVideo.getWidth();
        int height = coderVideo.getHeight();

        // read audio file and create stream
        IStreamCoder coderAudio = containerAudio.getStream(0).getStreamCoder();

        validateCodecs(coderVideo, coderAudio);

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
                System.out.println("samples.getPts() = " + samples.getPts());
                if (samples.isComplete())
                    mWriter.encodeAudio(1, samples);
            }
        } finally {
            coderAudio.close();
            coderVideo.close();
            containerAudio.close();
            containerVideo.close();
            mWriter.close();
        }
    }

    private static void validate(String inputAudioFilePath, String inputVideoFilePath, IContainer containerVideo, IContainer containerAudio) {
        // check files are readable
        if (containerVideo.open(inputVideoFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputVideoFilePath);
        if (containerAudio.open(inputAudioFilePath, IContainer.Type.READ, null) < 0)
            throw new IllegalArgumentException("Cant find " + inputAudioFilePath);

    }
    private static void validateCodecs(IStreamCoder coderVideo, IStreamCoder coderAudio) {
        if (coderVideo.open(null, null) < 0)
            throw new RuntimeException("Cant open video coder");
        if (coderAudio.open(null, null) < 0)
            throw new RuntimeException("Cant open audio coder");
    }
}
