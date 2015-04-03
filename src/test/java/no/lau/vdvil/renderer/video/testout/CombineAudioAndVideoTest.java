package no.lau.vdvil.renderer.video.testout;

import no.lau.vdvil.renderer.video.concatenator.AudioVideoConcatenator;
import org.junit.Test;

/**

 * Created by stiglau on 03/04/15.
 */
public class CombineAudioAndVideoTest {
    @Test
    public void videoAudioCombinationTest() {
        //String inputVideoFilePath = "/Users/stiglau/Downloads/NORWAY-A_Time-Lapse_Adventure.mp4"; Does not works
        String inputVideoFilePath = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String inputAudioFilePath = "/Users/stiglau/Downloads/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";
        String outputVideoFilePath = "/tmp/some-timelapse.flv";
        AudioVideoConcatenator.concatenateAudioAndVideo(inputAudioFilePath, inputVideoFilePath, outputVideoFilePath);
    }
}
