package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.CountNumberOfEligableImagesBetweenTimestampsCollector;
import no.lau.vdvil.domain.PathRef;
import org.junit.jupiter.api.Test;
import static no.lau.vdvil.renderer.video.TestData.fetch;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Stig@Lau.no
 */
//@Tag("IntegrationTest")
public class CountSegmentImagesTest {
    PathRef testVideoNorwayTimeLapseLocalStorage = fetch(TestData.norwayRemoteUrl);
    PathRef norwayDarkLakeLocalStorage = fetch(TestData.norwayDarkLakeRemoteUrl);
    PathRef norwayFlowerFjordLocalStorage = fetch(TestData.norwayFlowerFjordRemoteUrl);

    @Test
    public void countNr2() {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage));
        long estimatedAmountOfImages = VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration() / 46667;
        System.out.println("NOTE - Counting number of pics in video can take some time!");
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration(), testVideoNorwayTimeLapseLocalStorage);

        collector.run();
        long imagesFound = collector.imagesCollected();
        System.out.println("imagesFound = " + imagesFound);
        assertTrue(6805 < imagesFound, "Images " + imagesFound);
        assertTrue(imagesFound < 8200, "Images " + imagesFound);
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
        assertTrue(95 <= imagesFound, "Images " + imagesFound);
        assertTrue(imagesFound <= 110, "Images " + imagesFound);
    }

    @Test
    public void countFlowerFjordSnippet() {
        VideoInfo.printProperties(VideoInfo.getVideoProperties(norwayFlowerFjordLocalStorage));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(norwayFlowerFjordLocalStorage).getDuration(), norwayFlowerFjordLocalStorage);
        collector.run();
        //This is the amount of images actually extractable from the snippet!!!! Compare 101 to the original expectation 113!
        long imagesFound = collector.imagesCollected();
        assertTrue(240 <= imagesFound, "Expecting more than 240 images. Found " + imagesFound);
        assertTrue(imagesFound < 255, "Expecting less than 255 images. Found " + imagesFound);
    }
}

