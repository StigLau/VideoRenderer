package no.lau.vdvil.renderer.video.testout;

import no.lau.vdvil.renderer.video.concatenator.AudioVideoConcatenator;
import no.lau.vdvil.renderer.video.stigs.Composition;
import org.junit.Test;
import java.util.Collections;

/**

 * Created by stiglau on 03/04/15.
 */
public class CombineAudioAndVideoTest {
    @Test
    public void videoAudioCombinationTest() {
        //String inputVideoFilePath = "/Users/stiglau/Downloads/NORWAY-A_Time-Lapse_Adventure.mp4"; Does not works
        //String inputVideoFilePath = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String inputVideoFilePath = "/Users/stiglau/Downloads/Olive-Youre_Not_Alone.webm";
        String inputAudioFilePath = "/Users/stiglau/Downloads/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";

        Composition komposition = new Composition(Collections.emptyList(), 128);
        komposition.height = 360;
        komposition.width = 480;
        komposition.storageLocation = "/tmp/some-timelapse.mp4";
        AudioVideoConcatenator.concatenateAudioAndVideo(inputAudioFilePath, inputVideoFilePath, komposition);
    }
}
