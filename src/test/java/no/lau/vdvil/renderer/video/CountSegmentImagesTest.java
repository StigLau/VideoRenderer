package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.CountNumberOfEligableImagesBetweenTimestampsCollector;
import org.junit.Test;
import java.io.*;
import java.nio.file.Path;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Stig@Lau.no
 */
public class CountSegmentImagesTest {
    Path testVideoNorwayTimeLapseLocalStorage = fetch(TestData.norwayRemoteUrl);
    Path norwayDarkLakeLocalStorage = fetch(TestData.norwayDarkLakeRemoteUrl);
    Path norwayFlowerFjordLocalStorage = fetch(TestData.norwayFlowerFjordRemoteUrl);

    @Test
    public void countNr2() {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage));
        long estimatedAmountOfImages = VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration() / 46667;
        System.out.println("NOTE - Counting number of pics in video can take some time!");
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration(), testVideoNorwayTimeLapseLocalStorage);

        collector.run();
        long imagesFound = collector.imagesCollected();
        assertTrue("Images " + imagesFound, 7950 < imagesFound );
        assertTrue("Images " + imagesFound, imagesFound < 8000);
    }

    @Test
    public void countDarkLakeForYourself() {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(69375000, 74000000, testVideoNorwayTimeLapseLocalStorage);
        try {
            collector.run();
        }catch (VideoExtractionFinished e) {
            //This is ok
        }
        //This is the amount of images that were collected using the same parameters as the snippet collector method
        //Use SnipTest.testSegmentStrip to create the snippet!
        assertEquals(113, collector.imagesCollected());
    }

    @Test
    public void countDarkLakeSnippet() {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(norwayDarkLakeLocalStorage));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(norwayDarkLakeLocalStorage).getDuration(), norwayDarkLakeLocalStorage);
        collector.run();
        //This is the amount of images actually extractable from the snippet!!!! Compare 101 to the original expectation 113!
        long imagesFound = collector.imagesCollected();
        assertTrue("Images " + imagesFound, 95 <= imagesFound);
        assertTrue("Images " + imagesFound, imagesFound <= 110);
    }

    @Test
    public void countFlowerFjordSnippet() throws IOException {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(norwayFlowerFjordLocalStorage));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(norwayFlowerFjordLocalStorage).getDuration(), norwayFlowerFjordLocalStorage);
        collector.run();
        //This is the amount of images actually extractable from the snippet!!!! Compare 101 to the original expectation 113!
        long imagesFound = collector.imagesCollected();
        assertTrue("Images " + imagesFound, 240 <= imagesFound);
        assertTrue("Images " + imagesFound, imagesFound < 250);
    }
}

