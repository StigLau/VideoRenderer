package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.CountNumberOfEligableImagesBetweenTimestampsCollector;
import org.junit.Test;
import java.net.MalformedURLException;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no
 */
public class CountSegmentImagesTest {

    @Test
    public void countNr2() throws MalformedURLException {
        String testVideo = "/tmp/kompost/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4";
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideo));
        long estimatedAmountOfImages = VideoInfo.getVideoProperties(testVideo).getDuration() / 46667;

        //CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(10000000, 100000000, testVideo);
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideo).getDuration(), testVideo);
        try {
            collector.runSingle();
        }catch (VideoExtractionFinished e) {
            //assertEquals(2162, collector.imagesCollected());
        }
        assertEquals(7974, collector.imagesCollected());
    }
}

