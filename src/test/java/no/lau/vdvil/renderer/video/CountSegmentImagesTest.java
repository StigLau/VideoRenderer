package no.lau.vdvil.renderer.video;

import no.lau.vdvil.collector.CountNumberOfEligableImagesBetweenTimestampsCollector;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.nio.file.Path;
import static no.lau.vdvil.domain.utils.KompositionUtils.fetchRemoteFile;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no
 */
public class CountSegmentImagesTest {

    private static Logger logger = LoggerFactory.getLogger(CountSegmentImagesTest.class);

    String norwayRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure.mp4";
    String norwayDarkLakeRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Dark_lake_69375000___74000000.mp4";
    String norwayFlowerFjordRemoteUrl = "https://s3.amazonaws.com/dvl-test-music/test-data/NORWAY-A_Time-Lapse_Adventure/NORWAY-A_Time-Lapse_Adventure_Flower_fjord_35500000___46250000.mp4";

    @Test
    public void countNr2() throws IOException {
        Path testVideoNorwayTimeLapseLocalStorage = fetchRemoteFile("/tmp/komposttest/", norwayRemoteUrl);
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage));
        long estimatedAmountOfImages = VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration() / 46667;
        System.out.println("NOTE - Counting number of pics in video can take some time!");
        //CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(10000000, 100000000, testVideo);
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage).getDuration(), testVideoNorwayTimeLapseLocalStorage);

        collector.run();

        assertEquals(7975, collector.imagesCollected());
    }

    @Test
    public void countDarkLakeForYourself() throws IOException {
        Path testVideoNorwayTimeLapseLocalStorage = fetchRemoteFile("/tmp/komposttest/", norwayRemoteUrl);
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideoNorwayTimeLapseLocalStorage));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(69375000, 74000000, testVideoNorwayTimeLapseLocalStorage);
        try {
            collector.run();
        }catch (VideoExtractionFinished e) {
        }
        //This is the amount of images that were collected using the same parameters as the snippet collector method
        //Use SnipTest.testSegmentStrip to create the snippet!
        assertEquals(113, collector.imagesCollected());
    }

    @Test
    public void countDarkLakeSnippet() throws IOException {
        Path testVideo = fetchRemoteFile("/tmp/komposttest/", norwayDarkLakeRemoteUrl);
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideo));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideo).getDuration(), testVideo);
        collector.run();
        //This is the amount of images actually extractable from the snippet!!!! Compare 101 to the original expectation 113!
        assertEquals(101, collector.imagesCollected());
    }

    @Test
    public void countFlowerFjordSnippet() throws IOException {
        Path testVideo = fetchRemoteFile("/tmp/komposttest/", norwayFlowerFjordRemoteUrl);
        VideoInfo.printProperties(VideoInfo.getVideoProperties(testVideo));
        CountNumberOfEligableImagesBetweenTimestampsCollector collector = new CountNumberOfEligableImagesBetweenTimestampsCollector(0, VideoInfo.getVideoProperties(testVideo).getDuration(), testVideo);
        collector.run();
        //This is the amount of images actually extractable from the snippet!!!! Compare 101 to the original expectation 113!
        assertEquals(248, collector.imagesCollected());
    }
}

