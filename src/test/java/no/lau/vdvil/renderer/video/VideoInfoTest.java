package no.lau.vdvil.renderer.video;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Stig Lau 24/03/15.
 */
public class VideoInfoTest {
    String filename = "/tmp/Olive-Youre_Not_Alone.webm";

    @Test
    public void findOutShit() {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(filename);

        IStreamCoder asd = container.getStream(0).getStreamCoder();
        assertEquals(480, asd.getWidth());
        assertEquals(360, asd.getHeight());
    }

    @Test
    public void printProperties() {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(filename);
        videoInfo.printProperties(container);
    }
}
