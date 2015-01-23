package no.lau.vdvil.renderer.video;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

/**
 * Created by stiglau on 18/01/15.
 */
public class OwnExample {
    public static void main(String[] args) {
        IMediaReader reader = ToolFactory.makeReader("/tmp/SPACESHIP!!!!!!!!!!!.mp4");
        IMediaWriter writer = ToolFactory.makeWriter("/tmp/stripped.mp3", reader);
        int sampleRate = 44100;
        int channels = 2;
        //writer.setMaskLateStreamExceptions(true);
        writer.addAudioStream(1, 0, ICodec.ID.CODEC_ID_MP3, channels, sampleRate);
        reader.addListener(writer);
        while (reader.readPacket() == null);
    }
}
