package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.domain.utils.KompositionUtils;
import no.lau.vdvil.renderer.video.creator.PipeDream;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import no.lau.vdvil.renderer.video.creator.VideoImageStitcher;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.testout.deprecated.Mp4FromPicsCreator;
import org.junit.Test;
import java.net.MalformedURLException;
import java.net.URL;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 08/02/15.
 */
public class RestructureMediaTest {
    //First run ImageCapturingFromVideoTest with
    //String inputFilename = "/tmp/320_CLMD-The_Stockholm_Syndrome.mp4";
    //String outputFilePrefix = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";

    String downmixedOriginalVideo = "/tmp/320_CLMD-The_Stockholm_Syndrome.mp4";
    String snapshotFileStorage = "/tmp/snaps/320_CLMD-The_Stockholm_Syndrome/";
    String collectPicsFromVideo = "/tmp/320_Onewheel_The_World_is_Your_Playground.mp4";

    @Test
    public void testTimestampCalculation() throws Exception {
        Komposition kompost = new Komposition(120, new ImageSampleInstruction("id-1", -1, -1, -1));

        int frame = 1;
        int frameRate = 25;
        long timestamp = KompositionUtils.findTimeStamp(frame, frameRate, kompost);
        assertEquals(20000, timestamp);
    }

    @Test
    public void buildWithXuggle() throws MalformedURLException {
        Komposition fetchKomposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 64, 64, 2),
                new ImageSampleInstruction("Second capture sequence", 256, 32, 1)
        );
        ImageStore ibs = new PipeDream();
        new VideoThumbnailsCollector(ibs).capture(collectPicsFromVideo, fetchKomposition);

        Komposition buildKomposition =  new Komposition(128,null);
                //new VideoStillImageSegment("inst1", 8, 7, ((ImageSampleInstruction)fetchKomposition.segments.get(0)).collectedImages()));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/from_pix_with_xuggle.mp4"), 0f, 128f, "dunno yet");


        new VideoImageStitcher().createVideo(downmixedOriginalVideo,  buildKomposition, snapshotFileStorage);
    }

    @Test
    public void buildWithJCodec() throws Exception {
        Komposition fetchKomposition = new Komposition(128,
                new ImageSampleInstruction("First capture sequence", 64, 64, 2),
                new ImageSampleInstruction("Second capture sequence", 256, 32, 1)
        );
        new VideoThumbnailsCollector(new PipeDream()).capture(collectPicsFromVideo, fetchKomposition);

        Komposition buildKomposition =  new Komposition(128,
                new ImageSampleInstruction("inst1", 0, 32, 2));// new Instruction("inst1", 0, 32, fetchKomposition.segments.get(0)));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/from_pics_with_jcodec.mp4"), 0f, 128f, "dunno yet");


        Mp4FromPicsCreator.SequenceEncoder.createVideo(buildKomposition, 25);
    }
}

