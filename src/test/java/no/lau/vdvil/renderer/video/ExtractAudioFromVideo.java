package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.*;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import com.xuggle.mediatool.event.ICloseEvent;
import com.xuggle.mediatool.event.IOpenCoderEvent;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;
import no.lau.vdvil.domain.PathRef;
import java.io.IOException;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static no.lau.vdvil.renderer.video.TestData.norwayRemoteUrl;

/**
 * copied from the netz
 */
public class ExtractAudioFromVideo {
    public static void main(String[] args) {
        PathRef testVideoNorwayTimeLapseLocalStorage = fetch(norwayRemoteUrl);
        IContainer props = VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage);
        VideoInfo.printProperties(props);

        convert(testVideoNorwayTimeLapseLocalStorage.toString(), "/tmp/jalla.mp3");
    }

    public static void convert(String from, final String to) {
        IMediaReader mediaReader = ToolFactory.makeReader(from);
        final int mySampleRate = 44100;
        final int myChannels = 2;

        mediaReader.addListener(new MediaToolAdapter() {

            private IContainer container;
            private IMediaWriter mediaWriter;

            public void onOpenCoder(IOpenCoderEvent event) {
                container = event.getSource().getContainer();
                mediaWriter = null;
            }

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
                }
            }

            public void onClose(ICloseEvent event) {
                if (mediaWriter != null) {
                    mediaWriter.close();
                }
            }
        });

        while (mediaReader.readPacket() == null) {
        }
    }

    public static void whatIWantTheExtractorToLookLike(String filename, String to) {
        IContainer props = VideoInfo.getVideoProperties(new PathRef(filename));
        int channels = props.getNumStreams();
        IMediaReader reader = ToolFactory.makeReader(filename);
        IMediaWriter writer = ToolFactory.makeWriter(to, reader);
        int sampleRate = 44100;

        writer.addAudioStream(1, 0, ICodec.ID.CODEC_ID_MP3, channels, sampleRate);
        reader.addListener(writer);
        while (reader.readPacket() == null);
    }
}