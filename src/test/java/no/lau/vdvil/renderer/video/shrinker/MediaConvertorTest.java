package no.lau.vdvil.renderer.video.shrinker;

import org.junit.Test;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public class MediaConvertorTest {
    @Test
    public void testConverting() {
        Integer WIDTH = 320;
        Integer HEIGHT = 240;
        //String filename = "Onewheel_The_World_is_Your_Playground.mp4";
        String filename = "NORWAY-A_Time-Lapse_Adventure.mp4";
        MediaConvertor.convert("/tmp/videoTest/NORWAY-A_Time_Lapse_Adventure/" + filename, "/tmp/videoTest/NORWAY-A_Time_Lapse_Adventure" + WIDTH + "_" + filename, WIDTH, HEIGHT);
    }
}

//640 * 360
//output640.mp4  - 3:40 - 23.6mb
//output640.mpeg - 4:30 - 70.7mb

//320 x 240
//output320.mp4  - 1:45 - 12.4mb
//output320.mpeg - 1:28 - 42.9mb
