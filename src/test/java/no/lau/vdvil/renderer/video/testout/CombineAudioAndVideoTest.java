package no.lau.vdvil.renderer.video.testout;

import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.renderer.video.concatenator.AudioVideoConcatenator;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;

/**

 * Created by stiglau on 03/04/15.
 */
public class CombineAudioAndVideoTest {
    @Test
    public void videoAudioCombinationTest() throws MalformedURLException {
        //String inputVideoFilePath = "/Users/stiglau/Downloads/NORWAY-A_Time-Lapse_Adventure.mp4"; Does not works
        //String inputVideoFilePath = "/Users/stiglau/Downloads/JavaZone_2014_10.sept.mp4";
        String inputVideoFilePath = "/Users/stiglau/Downloads/Olive-Youre_Not_Alone.webm";
        String inputAudioFilePath = "/Users/stiglau/Downloads/The_Hurt_feat__Sam_Mollison_Andre_Sobota_Remix.mp3";

        Komposition komposition = new Komposition(128);
        komposition.height = 360;
        komposition.width = 480;
        komposition.storageLocation = new MediaFile(new URL("file:///tmp/some-timelapse.mp4"), 0f, 128f, "checksuym");
        AudioVideoConcatenator.concatenateAudioAndVideo(inputAudioFilePath, inputVideoFilePath, komposition);
    }
}
