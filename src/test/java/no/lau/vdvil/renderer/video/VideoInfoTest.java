package no.lau.vdvil.renderer.video;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

/**
 * @author Stig Lau 24/03/15.
 */
public class VideoInfoTest {
    String filename = "/tmp/videoTest/NORWAY-A_Time_Lapse_Adventure/NORWAY-A_Time_Lapse_Adventure.mp4";

    @Test
    public void findOutShit() {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(filename);

        IStreamCoder asd = container.getStream(0).getStreamCoder();
        assertEquals(1280, asd.getWidth());
        assertEquals(720, asd.getHeight());
    }

    @Test
    public void printProperties() {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(filename);
        videoInfo.printProperties(container);
    }
}
