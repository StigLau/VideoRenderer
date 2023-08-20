package no.lau.vdvil.renderer.video.shrinker;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IAddStreamEvent;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.mediatool.event.VideoPictureEvent;
import com.xuggle.xuggler.*;
import no.lau.vdvil.renderer.video.VideoInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * http://www.jochus.be/site/2010-10-12/java/converting-resizing-videos-in-java-xuggler
 */
public class MediaConvertor {
    static Logger log = LoggerFactory.getLogger(MediaConvertor.class);

    public static void convert(Path pathRef, String output, int width, int height) {
        long startTime = System.currentTimeMillis();
        printWidthAndHeight(pathRef);

        // reader
        IMediaReader reader = ToolFactory.makeReader(pathRef.toString());

        // writer
        IMediaWriter writer = ToolFactory.makeWriter(output, reader);

        // create custom listeners
        MyVideoListener myVideoListener = new MyVideoListener(width, height);
        Resizer resizer = new Resizer(width, height);


        reader.addListener(resizer);
        resizer.addListener(writer);
        writer.addListener(myVideoListener);

        // show video when encoding
        //reader.addListener(ToolFactory.makeViewer(true));

        while (reader.readPacket() == null) {
            // continue coding
        }
        log.info("Time used: " + (System.currentTimeMillis() - startTime)/1000 + " seconds" );
    }

    private static void printWidthAndHeight(Path input) {
        IContainer container = VideoInfo.getVideoProperties(input);
        IStreamCoder stream = container.getStream(0).getStreamCoder();
        log.info("Width: " + stream.getWidth());
        log.info("Height: " + stream.getHeight());
        stream.close();
        container.close();
    }
}

class MyVideoListener extends MediaToolAdapter {
    private Integer width;
    private Integer height;

    public MyVideoListener(Integer aWidth, Integer aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    @Override
    public void onAddStream(IAddStreamEvent event) {
        int streamIndex = event.getStreamIndex();
        IStreamCoder streamCoder = event.getSource().getContainer().getStream(streamIndex).getStreamCoder();
        if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {

        } else if (streamCoder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
            streamCoder.setWidth(width);
            streamCoder.setHeight(height);
        }
        super.onAddStream(event);
    }
}

class Resizer extends MediaToolAdapter {
    private Integer width;
    private Integer height;

    private IVideoResampler videoResampler = null;

    public Resizer(Integer aWidth, Integer aHeight) {
        this.width = aWidth;
        this.height = aHeight;
    }

    @Override
    public void onVideoPicture(IVideoPictureEvent event) {
        IVideoPicture pic = event.getPicture();
        if (videoResampler == null) {
            videoResampler = IVideoResampler.make(width, height, pic.getPixelType(), pic.getWidth(), pic.getHeight(), pic.getPixelType());
        }
        IVideoPicture out = IVideoPicture.make(pic.getPixelType(), width, height);
        videoResampler.resample(out, pic);

        IVideoPictureEvent asc = new VideoPictureEvent(event.getSource(), out, event.getStreamIndex());
        super.onVideoPicture(asc);
        out.delete();
    }
}