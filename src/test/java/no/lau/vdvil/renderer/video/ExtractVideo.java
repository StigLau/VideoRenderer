package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.ICloseEvent;
import com.xuggle.mediatool.event.IOpenCoderEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;

/**
 * Created by stiglau on 07/01/16.
 */
public class ExtractVideo {
    public static void main(String[] args) {
        String filename = "/tmp/kompost/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4";
        printProperties(filename);

        separateAudioVideo.convert(filename, "/tmp/jalla.mp3");

    }

    public static void printProperties(String filename) {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(filename);
        videoInfo.printProperties(container);
    }

    public void try1(String filename) {
        IMediaReader reader = ToolFactory.makeReader(filename);
        IMediaWriter writer = ToolFactory.makeWriter("/tmp/a.mp3", reader);
        int sampleRate = 44100;
        int channels = 2;
        writer.addAudioStream(1, 0, ICodec.ID.CODEC_ID_MP3, channels, sampleRate);
        reader.addListener(writer);
        while (reader.readPacket() == null);
    }

}

class separateAudioVideo {


    public static void convert(String from, final String to) {
        IMediaReader mediaReader = ToolFactory.makeReader(from);
        final int mySampleRate = 44100;
        final int myChannels = 2;

        mediaReader.addListener(new MediaToolAdapter() {

            private IContainer container;
            private IMediaWriter mediaWriter;

            @Override
            public void onOpenCoder(IOpenCoderEvent event) {
                container = event.getSource().getContainer();
                mediaWriter = null;
            }

            @Override
            public void onAudioSamples(IAudioSamplesEvent event) {
                if (container != null) {
                    if (mediaWriter == null) {
                        mediaWriter = ToolFactory.makeWriter(to);

                        mediaWriter.addListener(new MediaListenerAdapter() {

                            @Override
                            public void onAddStream(IAddStreamEvent event) {
                                IStreamCoder streamCoder = event.getSource().getContainer().getStream(event.getStreamIndex()).getStreamCoder();
                                streamCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);
                                streamCoder.setBitRate(128);
                                streamCoder.setChannels(myChannels);
                                streamCoder.setSampleRate(mySampleRate);
                                streamCoder.setBitRateTolerance(0);
                            }
                        });

                        mediaWriter.addAudioStream(0, 0, myChannels, mySampleRate);
                    }
                    mediaWriter.encodeAudio(0, event.getAudioSamples());
                    //System.out.println(event.getTimeStamp() / 1000);
                }
            }

            @Override
            public void onClose(ICloseEvent event) {
                if (mediaWriter != null) {
                    mediaWriter.close();
                }
            }
        });

        while (mediaReader.readPacket() == null) {
        }
    }
}