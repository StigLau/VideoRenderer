package no.lau.vdvil.renderer.video;

import no.lau.vdvil.domain.*;
import no.lau.vdvil.domain.out.Instruction;
import no.lau.vdvil.domain.out.Komposition;
import no.lau.vdvil.domain.utils.KompositionUtils;
import no.lau.vdvil.renderer.video.creator.VideoImageStitcher;
import no.lau.vdvil.renderer.video.stigs.ImageSampleInstruction;
import no.lau.vdvil.renderer.video.testout.deprecated.Mp4FromPicsCreator;
import org.junit.Test;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 08/02/15.
 */
public class RestructureMediaTest {
    //First run ImageCapturingFromVideoTest with
    //String inputFilename = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
    //String outputFilePrefix = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";

    String downmixedOriginalVideo = "/tmp/CLMD-The_Stockholm_Syndrome_320.mp4";
    String snapshotFileStorage = "/tmp/snaps/CLMD-The_Stockholm_Syndrome_320/";
    String collectPicsFromVideo = "/tmp/Onewheel_The_World_is_Your_Playground.mp4";

    @Test
    public void testTimestampCalculation() throws Exception {
        Komposition kompost = new Komposition(120, new Instruction("id-1", -1, -1, new ImageSampleInstruction("id-1", -1, -1, -1)));

        int frame = 1;
        int frameRate = 25;
        long timestamp = KompositionUtils.findTimeStamp(frame, frameRate, kompost);
        assertEquals(20000, timestamp);
    }

    @Test
    public void buildWithXuggle() throws MalformedURLException {
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        new VideoThumbnailsCollector().capture(collectPicsFromVideo, snapshotFileStorage, fetchKomposition);
        assertEquals(719, ((ImageSampleInstruction) fetchKomposition.instructions.get(0).segment).collectedImages().size());
        assertEquals(359, ((ImageSampleInstruction) fetchKomposition.instructions.get(1).segment).collectedImages().size());

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 8, 7, fetchKomposition.instructions.get(0).segment));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/from_pix_with_xuggle.mp4"), 0f, 128f, "dunno yet");


        new VideoImageStitcher().createVideo(downmixedOriginalVideo,  buildKomposition);
    }

    @Test
    public void testBuildingVideoXugglerOld() throws MalformedURLException {
        List<File> fileset3 = Arrays.asList(new File(snapshotFileStorage).listFiles());
        VideoStillImageRepresentation[] asd = new VideoStillImageRepresentation[fileset3.size()];
        for (int i = 0; i < asd.length; i++) {
            asd[i] = new VideoStillImageRepresentation(fileset3.get(i).getAbsolutePath());
        }
        Instruction instruction201 = new Instruction("inst2.1", 0, 16, new VideoStillImageSegment("asd", 0, 64, asd));

        Komposition composition = new Komposition(120, instruction201);
        composition.storageLocation = new MediaFile(new URL("file:///tmp/from_pix_with_xuggler_old.mp4"), 0f, 128f, "dunno yet");
        new VideoImageStitcher().createVideo(downmixedOriginalVideo, composition);
    }

    @Test
    public void buildWithJCodec() throws Exception {
        Komposition fetchKomposition = new Komposition(128,
                new Instruction("Capture some pics", 64, 64, new ImageSampleInstruction("First capture sequence", 64, 64, 2)),
                new Instruction("Capture some pics 2", 256, 32, new ImageSampleInstruction("Second capture sequence", 256, 32, 1))
        );
        new VideoThumbnailsCollector().capture(collectPicsFromVideo, snapshotFileStorage, fetchKomposition);
        assertEquals(719, ((ImageSampleInstruction) fetchKomposition.instructions.get(0).segment).collectedImages().size());
        assertEquals(359, ((ImageSampleInstruction) fetchKomposition.instructions.get(1).segment).collectedImages().size());

        Komposition buildKomposition =  new Komposition(128,
                new Instruction("inst1", 0, 32, fetchKomposition.instructions.get(0).segment));
        buildKomposition.storageLocation = new MediaFile(new URL("file:///tmp/from_pics_with_jcodec.mp4"), 0f, 128f, "dunno yet");


        Mp4FromPicsCreator.SequenceEncoder.createVideo(buildKomposition, 25);
    }
}

