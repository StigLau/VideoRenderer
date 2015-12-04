package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Stig@Lau.no 18/01/15.
 */
public class AudioExtractionTest {
    //May need multiplexing first
    String inFile = "/tmp/320_CLMD-The_Stockholm_Syndrome.mp4";
    //String inFile = "/tmp/320_Onewheel_The_World_is_Your_Playground.mp4";
    String outfile = "/tmp/an4.mp3";

    @Test
    @Ignore //Download first
    public void testExtractingAudion() {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(inFile);
        videoInfo.printProperties(container);

        IMediaReader reader = ToolFactory.makeReader(inFile);
        IMediaWriter writer = ToolFactory.makeWriter(outfile, reader);
        int sampleRate = 44100;
        int channels = 2;
        try {
            writer.setMaskLateStreamExceptions(true);
            writer.addAudioStream(1, 0, ICodec.ID.CODEC_ID_MP3, channels, sampleRate);
            reader.addListener(writer);
            while (reader.readPacket() == null) ;
        } finally {
            reader.close();
        }
    }

    /**
     * Example:
     Number of streams: 2
     Duration (ms): 165140000
     File Size (bytes): 44647898
     Bit Rate: 2162911
     *** Start of Stream Info ***
     stream 0: type: CODEC_TYPE_VIDEO; codec: CODEC_ID_H264; duration: 3962959; start time: 0; timebase: 1/24000; coder tb: 1001/48000;
     width: 1280; height: 720; format: YUV420P; frame-rate: 23.98;
     *** End of Stream Info ***
     *** Start of Stream Info ***
     stream 1: type: CODEC_TYPE_AUDIO; codec: CODEC_ID_AAC; duration: 7282688; start time: 0; timebase: 1/44100; coder tb: 1/44100;
     sample rate: 44100; channels: 2; format: FMT_S16
     *** End of Stream Info ***
     */
}
