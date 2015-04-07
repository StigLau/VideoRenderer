package no.lau.vdvil.renderer.video.shrinker;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;

/**
 * http://www.jochus.be/site/2010-10-12/java/converting-resizing-videos-in-java-xuggler
 */
public class MediaConvertor {
    private static final Integer WIDTH = 640;
    private static final Integer HEIGHT = 360;

    private static final String INPUT_FILE = "/Users/stiglau/Downloads/CLMD-The_Stockholm_Syndrome.mp4";
    private static final String OUTPUT_FILE = "/tmp/output.mpg";

    public static void main(String[] args) {
        // create custom listeners
        MyVideoListener myVideoListener = new MyVideoListener(WIDTH, HEIGHT);
        Resizer resizer = new Resizer(WIDTH, HEIGHT);

        // reader
        IMediaReader reader = ToolFactory.makeReader(INPUT_FILE);
        reader.addListener(resizer);

        // writer
        IMediaWriter writer = ToolFactory.makeWriter(OUTPUT_FILE, reader);
        resizer.addListener(writer);
        writer.addListener(myVideoListener);

        // show video when encoding
        //reader.addListener(ToolFactory.makeViewer(true));

        while (reader.readPacket() == null) {
            // continue coding
        }
    }
}