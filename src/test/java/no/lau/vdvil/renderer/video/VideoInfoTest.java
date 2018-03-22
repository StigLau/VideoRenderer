package no.lau.vdvil.renderer.video;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;
import org.junit.Test;


import java.io.IOException;
import java.nio.file.Path;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig Lau 24/03/15.
 */
public class VideoInfoTest {
    Path norwayLocalStorage = fetch(TestData.norwayRemoteUrl);

    @Test
    public void findOutShit() throws IOException {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(norwayLocalStorage);

        IStreamCoder asd = container.getStream(0).getStreamCoder();
        assertEquals(1280, asd.getWidth());
        assertEquals(720, asd.getHeight());
    }

    @Test
    public void printProperties() throws IOException {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(norwayLocalStorage);
        videoInfo.printProperties(container);
    }
}
