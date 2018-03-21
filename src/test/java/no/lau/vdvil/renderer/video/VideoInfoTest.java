package no.lau.vdvil.renderer.video;

import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IStreamCoder;
import org.junit.Test;


import java.io.IOException;

import static no.lau.vdvil.domain.utils.KompositionUtils.fetchRemoteFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig Lau 24/03/15.
 */
public class VideoInfoTest {
    String norwayRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4";

    @Test
    public void findOutShit() throws IOException {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(fetchRemoteFile("/tmp/komposttest/", norwayRemoteUrl).toAbsolutePath());

        IStreamCoder asd = container.getStream(0).getStreamCoder();
        assertEquals(1280, asd.getWidth());
        assertEquals(720, asd.getHeight());
    }

    @Test
    public void printProperties() throws IOException {
        VideoInfo videoInfo = new VideoInfo();
        IContainer container = videoInfo.getVideoProperties(fetchRemoteFile("/tmp/komposttest/", norwayRemoteUrl).toAbsolutePath());
        videoInfo.printProperties(container);
    }
}
